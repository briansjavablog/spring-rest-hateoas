package com.blog.rest.hateoas.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.blog.rest.hateoas.exception.InvalidOrderRequestException;
import com.blog.rest.hateoas.exception.OrderNotFoundException;
import com.blog.rest.hateoas.model.CustomerOrder;
import com.blog.rest.hateoas.model.Product;
import com.blog.rest.hateoas.repository.CustomerRepository;
import com.blog.rest.hateoas.repository.OrderRepository;
import com.blog.rest.hateoas.repository.ProductRepository;


@RestController
public class OrderController {

	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ProductRepository productRepository;

	
	/**
	 * Get order using id. Returns HTTP 404 if order not found
	 * 
	 * @param orderId
	 * @return retrieved order
	 */
	@RequestMapping(value = "/api/order/{orderId}", method = RequestMethod.GET)
	public CustomerOrder getOrder(@PathVariable("orderId") Long orderId) {
		
		/* validate order Id parameter */
		if (null==orderId) {
			throw new InvalidOrderRequestException();
		}
		
		CustomerOrder order = orderRepository.findOne(orderId);
		
		if(null==order){
			throw new OrderNotFoundException();
		}
		
		order.add(linkTo(methodOn(OrderController.class)
   				.getOrder(order.getOrderId()))
   				.withSelfRel());
		
		order.add(linkTo(methodOn(OrderController.class)
				.removeorder(order.getOrderId()))
		   		.withRel("delete"));

		order.add(linkTo(methodOn(OrderController.class)
	   			.getProductsFromOrder(order.getOrderId()))
				.withRel("products"));
		
		return order;
	}

	
	/**
	 * Gets all orders for customer
	 *
	 * @return the orders
	 */
	@RequestMapping(value = "/api/customer/{customerId}/orders", method = RequestMethod.GET)
	public ResponseEntity<Set<CustomerOrder>> getCustomerOrders(@PathVariable("customerId") Long customerId) {
		
		Set<CustomerOrder> orders = customerRepository.findOne(customerId).getOrders();
		
		orders.forEach(order -> {
			
			order.add(linkTo(methodOn(OrderController.class)
	   				.getOrder(order.getOrderId()))
	   				.withSelfRel());
			
			order.add(linkTo(methodOn(OrderController.class)
					.removeorder(order.getOrderId()))
			   		.withRel("delete"));

			order.add(linkTo(methodOn(OrderController.class)
		   			.getProductsFromOrder(order.getOrderId()))
					.withRel("products"));							
		});
		
		return ResponseEntity.ok(orders);
	}

		
	@RequestMapping(value = "/api/order/{orderId}/products", method = RequestMethod.GET)
	public ResponseEntity<Set<Product>> getProductsFromOrder(@PathVariable("orderId") Long orderId) {
		
		Set<Product> products = orderRepository.findOne(orderId).getProducts();
		
		products.forEach(product -> {
			
			product.add(linkTo(methodOn(ProductController.class)
	   				.getProduct(product.getProductId()))
	   				.withSelfRel());
			
			product.add(linkTo(methodOn(OrderController.class)
					.deleteProductFromOrder(orderId, product.getProductId()))
			   		.withRel("delete-from-order"));
		});
		
		return ResponseEntity.ok(products);
	}

	
	@RequestMapping(value = "/api/order/{orderId}/product/{productId}", method = RequestMethod.DELETE)
	public ResponseEntity<CustomerOrder> deleteProductFromOrder(@PathVariable("orderId") Long orderId,
															    @PathVariable("productId") Long productId) {
		
		CustomerOrder order = orderRepository.findOne(orderId);					
		Iterator<Product> productIterator = order.getProducts().iterator();
		
		productIterator.forEachRemaining(prod -> {
			if(prod.getProductId().equals(productId)){
				productIterator.remove();
			}
		});
		
		orderRepository.save(order);
		
		return ResponseEntity.ok(order);
	}
		
		
	@RequestMapping(value = { "/api/order/{orderId}/product/{productId}/quantity/{quantity}" }, method = { RequestMethod.POST })
	public ResponseEntity<CustomerOrder> addProductToOrder(@PathVariable("orderId") Long orderId,
												   		   @PathVariable("productId") Long productId,
												           @PathVariable("quantity") Integer quantity) {
		
		CustomerOrder order = orderRepository.findOne(orderId);
		Product product = productRepository.findOne(productId);		
		
		IntStream.of(quantity).forEach(i-> order.addProduct(product));		
		orderRepository.save(order);
		
		orderRepository.findOne(orderId).getProducts()
					   .forEach(prod -> {
						   order.add(linkTo(methodOn(ProductController.class)	
									.getProduct(prod.getProductId()))
									.withRel("product"));
					   });
		
		return ResponseEntity
				 .created(URI.create(order.getLink("self").getHref()))
				 .body(order);		
	}
	
	
	/**
	 * Update order with given order id.
	 *
	 * @param order the order
	 */
	@RequestMapping(value = { "/api/order/{orderId}" }, method = { RequestMethod.PUT })
	public ResponseEntity<Void> updateorder(@RequestBody CustomerOrder order, 
			                                @PathVariable("orderId") Long orderId) {

		if(!orderRepository.exists(orderId)){
			return ResponseEntity.notFound().build();
		}
		else{
			orderRepository.save(order);
			return ResponseEntity.noContent().build();	
		}
	}

	
	/**
	 * Deletes the order with given order id if it exists and returns HTTP204.
	 *
	 * @param orderId the order id
	 */
	@RequestMapping(value = "/api/order/{orderId}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> removeorder(@PathVariable("orderId") Long orderId) {

		if(orderRepository.exists(orderId)){
			orderRepository.delete(orderId);	
		}
		
		return ResponseEntity.noContent().build();
	}

}