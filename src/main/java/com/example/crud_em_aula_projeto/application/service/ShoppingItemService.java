package com.example.crud_em_aula_projeto.application.service;

import com.example.crud_em_aula_projeto.application.dto.shoppingDTOs.ShoppingItemRequestDTO;
import com.example.crud_em_aula_projeto.application.dto.shoppingDTOs.ShoppingItemResponseDTO;
import com.example.crud_em_aula_projeto.domain.exception.BusinessRuleException;
import com.example.crud_em_aula_projeto.domain.exception.ResourceNotFoundException;
import com.example.crud_em_aula_projeto.domain.model.entity.Customer;
import com.example.crud_em_aula_projeto.domain.model.entity.Product;
import com.example.crud_em_aula_projeto.domain.model.entity.Shopping;
import com.example.crud_em_aula_projeto.domain.model.entity.ShoppingItem;
import com.example.crud_em_aula_projeto.domain.model.enuns.ProductStatus;
import com.example.crud_em_aula_projeto.domain.repository.CustomerRepository;
import com.example.crud_em_aula_projeto.domain.repository.ProductRepository;
import com.example.crud_em_aula_projeto.domain.repository.ShoppingItemRepository;
import com.example.crud_em_aula_projeto.domain.repository.ShoppingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingItemService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final ShoppingRepository shoppingRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public ShoppingItemResponseDTO getItemById(UUID itemId) {
        ShoppingItem item = shoppingItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado com o ID: " + itemId));
        
        // Verifica se o item pertence ao carrinho do cliente autenticado
        validateItemOwnership(item);
        
        return new ShoppingItemResponseDTO(item);
    }

    @Transactional(readOnly = true)
    public List<ShoppingItemResponseDTO> getItemsByShoppingId(UUID shoppingId) {
        Shopping shopping = shoppingRepository.findById(shoppingId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrinho de compras não encontrado com o ID: " + shoppingId));
        
        // Verifica se o carrinho pertence ao cliente autenticado
        validateShoppingOwnership(shopping);
        
        return shoppingItemRepository.findByShoppingId(shoppingId)
                .stream()
                .map(ShoppingItemResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShoppingItemResponseDTO> getItemsByAuthenticatedCustomer() {
        Customer customer = getAuthenticatedCustomer();
        Shopping shopping = shoppingRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrinho de compras não encontrado para este cliente"));
        
        return shoppingItemRepository.findByShoppingId(shopping.getId())
                .stream()
                .map(ShoppingItemResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShoppingItemResponseDTO createItem(ShoppingItemRequestDTO requestDTO) {
        Customer customer = getAuthenticatedCustomer();
        Shopping shopping = getOrCreateShopping(customer);
        
        Product product = productRepository.findById(requestDTO.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com o ID: " + requestDTO.productId()));

        // Valida se o produto está disponível
        if (product.getProductStatus() != ProductStatus.AVAILABLE) {
            throw new BusinessRuleException("Produto não está disponível para compra");
        }

        // Verifica se o item já existe no carrinho
        Optional<ShoppingItem> existingItem = shoppingItemRepository.findByShoppingIdAndProductId(
                shopping.getId(), 
                requestDTO.productId()
        );

        ShoppingItem item;
        if (existingItem.isPresent()) {
            // Atualiza a quantidade do item existente
            item = existingItem.get();
            item.setQuantity(item.getQuantity() + requestDTO.quantity());
        } else {
            // Cria um novo item
            item = ShoppingItem.builder()
                    .shopping(shopping)
                    .product(product)
                    .quantity(requestDTO.quantity())
                    .build();
        }

        ShoppingItem savedItem = shoppingItemRepository.save(item);
        return new ShoppingItemResponseDTO(savedItem);
    }

    @Transactional
    public ShoppingItemResponseDTO updateItem(UUID itemId, Integer quantity) {
        if (quantity <= 0) {
            throw new BusinessRuleException("A quantidade deve ser maior que zero");
        }

        ShoppingItem item = shoppingItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado com o ID: " + itemId));

        // Verifica se o item pertence ao carrinho do cliente autenticado
        validateItemOwnership(item);

        item.setQuantity(quantity);
        ShoppingItem updatedItem = shoppingItemRepository.save(item);
        return new ShoppingItemResponseDTO(updatedItem);
    }

    @Transactional
    public void deleteItem(UUID itemId) {
        ShoppingItem item = shoppingItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado com o ID: " + itemId));

        // Verifica se o item pertence ao carrinho do cliente autenticado
        validateItemOwnership(item);

        shoppingItemRepository.delete(item);
    }

    @Transactional
    public void deleteAllItemsByShoppingId(UUID shoppingId) {
        Shopping shopping = shoppingRepository.findById(shoppingId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrinho de compras não encontrado com o ID: " + shoppingId));

        // Verifica se o carrinho pertence ao cliente autenticado
        validateShoppingOwnership(shopping);

        List<ShoppingItem> items = shoppingItemRepository.findByShoppingId(shoppingId);
        shoppingItemRepository.deleteAll(items);
    }

    @Transactional
    public void deleteAllItemsByAuthenticatedCustomer() {
        Customer customer = getAuthenticatedCustomer();
        Shopping shopping = shoppingRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrinho de compras não encontrado"));

        List<ShoppingItem> items = shoppingItemRepository.findByShoppingId(shopping.getId());
        shoppingItemRepository.deleteAll(items);
    }

    private Customer getAuthenticatedCustomer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o e-mail: " + email));
    }

    private Shopping getOrCreateShopping(Customer customer) {
        return shoppingRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    Shopping newShopping = new Shopping();
                    newShopping.setCustomer(customer);
                    return shoppingRepository.save(newShopping);
                });
    }

    private void validateItemOwnership(ShoppingItem item) {
        Customer customer = getAuthenticatedCustomer();
        if (!item.getShopping().getCustomer().getId().equals(customer.getId())) {
            throw new BusinessRuleException("Você não tem permissão para acessar este item");
        }
    }

    private void validateShoppingOwnership(Shopping shopping) {
        Customer customer = getAuthenticatedCustomer();
        if (!shopping.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessRuleException("Você não tem permissão para acessar este carrinho");
        }
    }
}

