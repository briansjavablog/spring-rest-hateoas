package com.blog.rest.hateoas.repository;

import org.springframework.data.repository.CrudRepository;

import com.blog.rest.hateoas.model.Product;

public interface ProductRepository extends CrudRepository<Product, Long> {

}