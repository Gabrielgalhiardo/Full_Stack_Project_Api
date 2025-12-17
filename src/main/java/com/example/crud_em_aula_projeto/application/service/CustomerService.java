package com.example.crud_em_aula_projeto.application.service;

import com.example.crud_em_aula_projeto.application.dto.customerDTOs.CustomerRequestDTO;
import com.example.crud_em_aula_projeto.application.dto.customerDTOs.CustomerResponseDTO;
import com.example.crud_em_aula_projeto.domain.exception.BusinessRuleException;
import com.example.crud_em_aula_projeto.domain.exception.ResourceNotFoundException;
import com.example.crud_em_aula_projeto.domain.model.entity.Customer;
import com.example.crud_em_aula_projeto.domain.repository.CustomerRepository;
import com.example.crud_em_aula_projeto.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerResponseDTO createCustomer(CustomerRequestDTO customerRequestDTO) {
        if (usuarioRepository.findByEmail(customerRequestDTO.email()).isPresent()) {
            throw new BusinessRuleException("E-mail já está em uso");
        }
        Customer customer = customerRequestDTO.toEntity(passwordEncoder);
        Customer savedCustomer = customerRepository.save(customer);
        return new CustomerResponseDTO(savedCustomer);
    }


    public Optional<CustomerResponseDTO> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email).map(CustomerResponseDTO::new);
    }

    public Optional<CustomerResponseDTO> getCustomerById(UUID id){
        return customerRepository.findById(id).map(CustomerResponseDTO::new);
    }

    public Optional<CustomerResponseDTO> deactivateCustomerById(UUID id) {
        return customerRepository.findById(id).map(customer -> {
            customer.setActive(false);
            Customer savedCustomer = customerRepository.save(customer);
            return new CustomerResponseDTO(savedCustomer);
        });
    }

    public Optional<CustomerResponseDTO> activeCustomerById(UUID id) {
        return customerRepository.findById(id).map(customer -> {
            customer.setActive(true);
            Customer savedCustomer = customerRepository.save(customer);
            return new CustomerResponseDTO(savedCustomer);
        });
    }

    public void deleteCustomerById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
        customerRepository.delete(customer);
    }

    public List<CustomerResponseDTO> findAllActive(){
        return customerRepository.findAllByActiveTrue().stream()
                .map(CustomerResponseDTO::new)
                .collect(Collectors.toList());
    }

    public Optional<CustomerResponseDTO> updateCustomer(UUID id, CustomerRequestDTO customerRequestDTO){
        return customerRepository.findById(id).map(customer -> {
            if (!customer.getEmail().equals(customerRequestDTO.email()) && usuarioRepository.findByEmail(customerRequestDTO.email()).isPresent()) {
                throw new BusinessRuleException("E-mail já está em uso");
            }
            customer.setName(customerRequestDTO.name());
            customer.setEmail(customerRequestDTO.email());
            customer.setPasswordHash(passwordEncoder.encode(customerRequestDTO.password()));
            Customer updatedCustomer = customerRepository.save(customer);
            return new CustomerResponseDTO(updatedCustomer);
        });
    }





}
