package com.sathya.restaurant_management.service;

import com.sathya.restaurant_management.dto.CustomerDto;
import com.sathya.restaurant_management.dto.CustomerRequest;
import com.sathya.restaurant_management.model.Customer;
import com.sathya.restaurant_management.repository.CustomerRepository;
import com.sathya.restaurant_management.util.Ids;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<CustomerDto> findAll() {
        return customerRepository.findAllByOrderByCreatedAtAscIdAsc().stream().map(this::toDto).toList();
    }

    @Transactional
    public CustomerDto create(CustomerRequest request) {
        String phone = request.phone() == null ? "" : request.phone().trim();
        Customer saved = customerRepository.save(
                new Customer(Ids.next("c"), request.name().trim(), phone, Instant.now()));
        return toDto(saved);
    }

    private CustomerDto toDto(Customer c) {
        return new CustomerDto(c.getId(), c.getName(), c.getPhone() == null ? "" : c.getPhone());
    }
}
