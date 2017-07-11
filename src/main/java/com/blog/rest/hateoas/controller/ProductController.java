package com.blog.rest.hateoas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.blog.rest.hateoas.exception.InvalidProductRequestException;
import com.blog.rest.hateoas.exception.ProductNotFoundException;
import com.blog.rest.hateoas.model.Product;
import com.blog.rest.hateoas.repository.ProductRepository;

@RestController
public class ProductController {

	@Autowired
	private ProductRepository ProductRepository;	
	
	
	/**
	 * Get Product using id. Returns HTTP 404 if Product not found
	 * 
	 * @param ProductId
	 * @return retrieved Product
	 */
	@RequestMapping(value = "/api/product/{productId}", method = RequestMethod.GET)
	public ResponseEntity<Product> getProduct(@PathVariable("productId") Long productId) {
		
		/* validate Product Id parameter */
		if (null==productId) {
			throw new InvalidProductRequestException();
		}
		
		Product product = ProductRepository.findOne(productId);
		
		if(null==product){
			throw new ProductNotFoundException();
		}
		
		return ResponseEntity.ok(product);
	}
	
	
	/**
	 * Update Product with given Product id.
	 *
	 * @param Product the Product
	 */
	@RequestMapping(value = { "/api/product/{productId}" }, method = { RequestMethod.PUT })
	public ResponseEntity<Void> updateProduct(@RequestBody Product product, 
											  @PathVariable("productId") Long productId) {

		if(!ProductRepository.exists(productId)){
			return ResponseEntity.notFound().build();
		}
		else{
			ProductRepository.save(product);
			return ResponseEntity.noContent().build();			
		}
	}	
	
	
	/**
	 * Deletes the Product with given Product id if it exists and returns HTTP204.
	 *
	 * @param ProductId the Product id
	 */
	@RequestMapping(value = "/api/product/{productId}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> removeProduct(@PathVariable("productId") Long productId) {

		if(ProductRepository.exists(productId)){
			ProductRepository.delete(productId);	
		}
		
		return ResponseEntity.noContent().build();
	}
	
}