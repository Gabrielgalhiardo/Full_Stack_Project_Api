package com.example.crud_em_aula_projeto.application.service;

import com.example.crud_em_aula_projeto.application.dto.shoppingDTOs.ShoppingResponseDTO;
import com.example.crud_em_aula_projeto.domain.exception.BusinessRuleException;
import com.example.crud_em_aula_projeto.domain.exception.ResourceNotFoundException;
import com.example.crud_em_aula_projeto.domain.model.entity.Customer;
import com.example.crud_em_aula_projeto.domain.model.entity.Shopping;
import com.example.crud_em_aula_projeto.domain.repository.CustomerRepository;
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
public class ShoppingService {

    private final ShoppingRepository shoppingRepository;
    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public ShoppingResponseDTO getShoppingByAuthenticatedCustomer() {
        Customer customer = getAuthenticatedCustomer();
        Shopping shopping = shoppingRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrinho de compras não encontrado para este cliente"));
        return new ShoppingResponseDTO(shopping);
    }

    @Transactional(readOnly = true)
    public ShoppingResponseDTO getShoppingById(UUID shoppingId) {
        Shopping shopping = shoppingRepository.findById(shoppingId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrinho de compras não encontrado com o ID: " + shoppingId));
        return new ShoppingResponseDTO(shopping);
    }

    @Transactional
    public ShoppingResponseDTO createShopping() {
        Customer customer = getAuthenticatedCustomer();
        
        // Verifica se já existe um carrinho para este cliente
        if (shoppingRepository.findByCustomerId(customer.getId()).isPresent()) {
            throw new BusinessRuleException("Cliente já possui um carrinho de compras");
        }

        Shopping shopping = new Shopping();
        shopping.setCustomer(customer);
        Shopping savedShopping = shoppingRepository.save(shopping);
        return new ShoppingResponseDTO(savedShopping);
    }

    @Transactional
    public void clearShopping() {
        Customer customer = getAuthenticatedCustomer();
        Shopping shopping = shoppingRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrinho de compras não encontrado"));

        shopping.getItems().clear();
        shoppingRepository.save(shopping);
    }

    @Transactional
    public void deleteShopping(UUID shoppingId) {
        Shopping shopping = shoppingRepository.findById(shoppingId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrinho de compras não encontrado com o ID: " + shoppingId));

        // Verifica se o carrinho pertence ao cliente autenticado
        Customer customer = getAuthenticatedCustomer();
        if (!shopping.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessRuleException("Você não tem permissão para deletar este carrinho");
        }

        shoppingRepository.delete(shopping);
    }

    @Transactional(readOnly = true)
    public List<ShoppingResponseDTO> getAllShoppings() {
        return shoppingRepository.findAll()
                .stream()
                .map(ShoppingResponseDTO::new)
                .collect(Collectors.toList());
    }

    private Customer getAuthenticatedCustomer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o e-mail: " + email));
    }
}

