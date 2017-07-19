package com.blog.rest.hateoas.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.blog.rest.hateoas.exception.CustomerNotFoundException;
import com.blog.rest.hateoas.exception.InvalidCustomerRequestException;
import com.blog.rest.hateoas.model.Customer;
import com.blog.rest.hateoas.repository.CustomerRepository;

@RestController
public class CustomerController {

	@Autowired
	private CustomerRepository customerRepository;	
	
		
	/**
	 * Get Customer using id. Returns HTTP 404 if Customer not found
	 * 
	 * @param CustomerId
	 * @return retrieved Customer
	 */
	@RequestMapping(value = "/api/customer/{customerId}", method = RequestMethod.GET)
	public ResponseEntity<Customer> getCustomer(@PathVariable("customerId") Long customerId) {
		
		/* validate Customer Id parameter */
		if (null==customerId) {
			throw new InvalidCustomerRequestException();
		}
		
		Customer customer = customerRepository.findOne(customerId);
		
		if(null==customer){
			throw new CustomerNotFoundException();
		}

		customer.add(linkTo(methodOn(CustomerController.class)
   				.getCustomer(customer.getCustomerId()))
   				.withSelfRel());
		
		customer.add(linkTo(methodOn(CustomerController.class)
   				.updateCustomer(customer, customer.getCustomerId()))
   				.withRel("update"));
		
		customer.add(linkTo(methodOn(CustomerController.class)
				.removeCustomer(customer.getCustomerId()))
		   		.withRel("delete"));
		
		customer.add(linkTo(methodOn(OrderController.class)
	   				.getCustomerOrders(customer.getCustomerId()))
					.withRel("orders"));
		
		return ResponseEntity.ok(customer);
	}
	
	
	/**
	 * Update Customer with given Customer id.
	 *
	 * @param Customer the Customer
	 */
	@RequestMapping(value = { "/api/customer/{customerId}" }, method = { RequestMethod.PUT })
	public ResponseEntity<Void> updateCustomer(@RequestBody Customer customer, 
											  @PathVariable("customerId") Long customerId) {

		if(!customerRepository.exists(customerId)){
			return ResponseEntity.notFound().build();
		}
		else{
			customerRepository.save(customer);
			return ResponseEntity.noContent().build();			
		}
	}	
	
	/**
	 * Deletes the Customer with given Customer id if it exists and returns HTTP204.
	 *
	 * @param CustomerId the Customer id
	 */
	@RequestMapping(value = "/api/customer/{customerId}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> removeCustomer(@PathVariable("customerId") Long customerId) {

		if(customerRepository.exists(customerId)){
			customerRepository.delete(customerId);	
		}
		
		return ResponseEntity.noContent().build();
	}	
}