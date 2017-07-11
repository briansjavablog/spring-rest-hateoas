package com.blog.rest.hateoas.repository;

import org.springframework.data.repository.CrudRepository;

import com.blog.rest.hateoas.model.Customer;

public interface CustomerRepository extends CrudRepository<Customer, Long> {

}