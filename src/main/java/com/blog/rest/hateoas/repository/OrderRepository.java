package com.blog.rest.hateoas.repository;

import org.springframework.data.repository.CrudRepository;

import com.blog.rest.hateoas.model.CustomerOrder;

public interface OrderRepository extends CrudRepository<CustomerOrder, Long> {

}