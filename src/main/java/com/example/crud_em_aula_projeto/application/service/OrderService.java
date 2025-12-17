package com.example.crud_em_aula_projeto.application.service;

import com.example.crud_em_aula_projeto.application.dto.orderDTOs.OrderResponseDTO;
import com.example.crud_em_aula_projeto.application.dto.orderDTOs.SalesResponseDTO;
import com.example.crud_em_aula_projeto.domain.exception.BusinessRuleException;
import com.example.crud_em_aula_projeto.domain.exception.ResourceNotFoundException;
import com.example.crud_em_aula_projeto.domain.model.entity.Collaborator;
import com.example.crud_em_aula_projeto.domain.model.entity.Customer;
import com.example.crud_em_aula_projeto.domain.model.entity.Order;
import com.example.crud_em_aula_projeto.domain.model.entity.OrderItem;
import com.example.crud_em_aula_projeto.domain.model.entity.Product;
import com.example.crud_em_aula_projeto.domain.model.entity.Shopping;
import com.example.crud_em_aula_projeto.domain.model.entity.ShoppingItem;
import com.example.crud_em_aula_projeto.domain.model.enuns.OrderStatus;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductStatus;
import com.example.crud_em_aula_projeto.domain.repository.CollaboratorRepository;
import com.example.crud_em_aula_projeto.domain.repository.CustomerRepository;
import com.example.crud_em_aula_projeto.domain.repository.OrderRepository;
import com.example.crud_em_aula_projeto.domain.repository.ShoppingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ShoppingRepository shoppingRepository;
    private final CustomerRepository customerRepository;
    private final CollaboratorRepository collaboratorRepository;

    @Transactional
    public OrderResponseDTO createOrderFromShopping() {
        Customer customer = getAuthenticatedCustomer();
        Shopping shopping = shoppingRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrinho de compras não encontrado"));

        if (shopping.getItems().isEmpty()) {
            throw new BusinessRuleException("Não é possível criar um pedido com carrinho vazio");
        }

        // Cria o pedido
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);

        // Converte itens do carrinho para itens do pedido
        for (ShoppingItem shoppingItem : shopping.getItems()) {
            Product product = shoppingItem.getProduct();
            
            // Valida estoque
            if (product.getQuantity() < shoppingItem.getQuantity()) {
                throw new BusinessRuleException(
                    String.format("Produto '%s' não possui estoque suficiente. Disponível: %d, Solicitado: %d",
                        product.getTitle(), product.getQuantity(), shoppingItem.getQuantity())
                );
            }

            // Valida se produto ainda está disponível
            if (product.getProductStatus() != ProductStatus.AVAILABLE) {
                throw new BusinessRuleException(
                    String.format("Produto '%s' não está mais disponível para compra", product.getTitle())
                );
            }

            // Cria item do pedido
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(shoppingItem.getQuantity())
                    .unitPrice(product.getPrice()) // Salva o preço no momento da compra
                    .build();

            order.getItems().add(orderItem);

            // Atualiza estoque do produto
            product.setQuantity(product.getQuantity() - shoppingItem.getQuantity());
            if (product.getQuantity() == 0) {
                product.setProductStatus(ProductStatus.OUT_OF_STOCK);
            }
        }

        // Salva o pedido
        Order savedOrder = orderRepository.save(order);

        // Limpa o carrinho após criar o pedido
        shopping.getItems().clear();
        shoppingRepository.save(shopping);

        return new OrderResponseDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getMyOrders() {
        Customer customer = getAuthenticatedCustomer();
        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
        return orders.stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado com o ID: " + orderId));

        // Verifica se o pedido pertence ao cliente autenticado
        Customer customer = getAuthenticatedCustomer();
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessRuleException("Você não tem permissão para acessar este pedido");
        }

        return new OrderResponseDTO(order);
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado com o ID: " + orderId));

        // Validações de mudança de status
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException("Não é possível alterar o status de um pedido cancelado");
        }

        if (order.getStatus() == OrderStatus.DELIVERED && newStatus != OrderStatus.DELIVERED) {
            throw new BusinessRuleException("Não é possível alterar o status de um pedido entregue");
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return new OrderResponseDTO(updatedOrder);
    }

    @Transactional
    public void cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado com o ID: " + orderId));

        // Verifica se o pedido pertence ao cliente autenticado
        Customer customer = getAuthenticatedCustomer();
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessRuleException("Você não tem permissão para cancelar este pedido");
        }

        // Valida se pode cancelar
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException("Este pedido já está cancelado");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessRuleException("Não é possível cancelar um pedido já entregue");
        }

        // Restaura estoque dos produtos
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            if (product.getProductStatus() == ProductStatus.OUT_OF_STOCK && product.getQuantity() > 0) {
                product.setProductStatus(ProductStatus.AVAILABLE);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SalesResponseDTO> getMySales() {
        Collaborator collaborator = getAuthenticatedCollaborator();
        List<Order> orders = orderRepository.findOrdersByCollaboratorId(collaborator.getId());
        
        return orders.stream()
                .map(order -> new SalesResponseDTO(order, collaborator.getId()))
                .filter(sale -> !sale.myItems().isEmpty()) // Filtra apenas vendas que têm itens do colaborador
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SalesResponseDTO getSaleById(UUID orderId) {
        Collaborator collaborator = getAuthenticatedCollaborator();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado com o ID: " + orderId));

        // Verifica se o pedido contém produtos do colaborador
        boolean hasCollaboratorProducts = order.getItems().stream()
                .anyMatch(item -> item.getProduct() != null 
                        && item.getProduct().getCollaborator() != null
                        && item.getProduct().getCollaborator().getId().equals(collaborator.getId()));

        if (!hasCollaboratorProducts) {
            throw new BusinessRuleException("Este pedido não contém produtos seus");
        }

        return new SalesResponseDTO(order, collaborator.getId());
    }

    private Customer getAuthenticatedCustomer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o e-mail: " + email));
    }

    private Collaborator getAuthenticatedCollaborator() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return collaboratorRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado com o e-mail: " + email));
    }
}

