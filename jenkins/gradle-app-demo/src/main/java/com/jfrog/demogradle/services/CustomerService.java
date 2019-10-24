package com.jfrog.demogradle.services;


import com.jfrog.demogradle.entities.Customer;
import com.jfrog.demogradle.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void deleteAll() {
        customerRepository.deleteAll();
    }

    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    public void insert(String firstName, String lastName) {
        customerRepository.insert(new Customer(firstName ,lastName));
    }

}
