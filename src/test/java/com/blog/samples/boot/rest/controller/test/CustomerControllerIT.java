package com.blog.samples.boot.rest.controller.test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import com.blog.rest.hateoas.Application;
import com.blog.rest.hateoas.model.Customer;
import com.blog.rest.hateoas.model.CustomerOrder;
import com.blog.rest.hateoas.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest( webEnvironment=WebEnvironment.DEFINED_PORT, classes={ Application.class })
public class CustomerControllerIT {

	@Value("${local.server.port}")
	private int port;		
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private RestTemplate template;
	private URL base;	
	private static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8"; 
	
	
	@Before
	public void setUp() throws Exception {
		this.base = new URL("http://localhost:" + port + "/api");
		template = new RestTemplate();				
	}
	
	@Test
	public void getCustomer() throws Exception{
		
		ResponseEntity<String> customerResponse = template.getForEntity(base.toString() + "/customer/" + 1, String.class);
		assertThat(customerResponse.getStatusCode(), is(HttpStatus.OK));
		assertThat(customerResponse.getHeaders().getContentType().toString(), is(JSON_CONTENT_TYPE));
		
		Customer customer = serialize(customerResponse.getBody(), Customer.class);
		
		assertThat(customer.getFirstName(), is("Joe"));
		assertThat(customer.getLastName(), is("Smith"));
		assertThat(customer.getDateOfBirth(), is(LocalDate.of(1982, 1, 10)));
		assertThat(customer.getAddress().getStreet(), is("High Street"));
		
		assertThat(customer.getLink("self").getHref(), is("http://localhost:8080/api/customer/1"));
		assertThat(customer.getLink("update").getHref(), is("http://localhost:8080/api/customer/1"));
		assertThat(customer.getLink("delete").getHref(), is("http://localhost:8080/api/customer/1"));
		assertThat(customer.getLink("orders").getHref(), is("http://localhost:8080/api/customer/1/orders"));		
	}
	
	
	@Test
	public void getCustomerThenOrdersThenProducts() throws Exception{
		
		ResponseEntity<String> customerResponse = template.getForEntity(base.toString() + "/customer/" + 1, String.class);
		assertThat(customerResponse.getStatusCode(), is(HttpStatus.OK));
		assertThat(customerResponse.getHeaders().getContentType().toString(), is(JSON_CONTENT_TYPE));
		
		Customer customer = serialize(customerResponse.getBody(), Customer.class);
		
		/* assert customer data */
		assertThat(customer.getCustomerId(), is(1L));
		assertThat(customer.getFirstName(), is("Joe"));
		assertThat(customer.getLastName(), is("Smith"));
		assertThat(customer.getDateOfBirth(), is(LocalDate.of(1982, 01, 10)));
		assertThat(customer.getAddress().getStreet(), is("High Street"));
		assertThat(customer.getAddress().getTown(), is("Newry"));
		assertThat(customer.getAddress().getPostcode(), is("BT893PY"));
		
		/* assert hypermedia links */
		assertThat(customer.getLink("self").getHref(), is("http://localhost:8080/api/customer/1"));
		assertThat(customer.getLink("update").getHref(), is("http://localhost:8080/api/customer/1"));
		assertThat(customer.getLink("delete").getHref(), is("http://localhost:8080/api/customer/1"));
		assertThat(customer.getLink("orders").getHref(), is("http://localhost:8080/api/customer/1/orders"));		
	
		/* use orders link to access this customer orders */
		ResponseEntity<String> customerOrdersResponse = template.getForEntity(customer.getLink("orders").getHref(), String.class);
		assertThat(customerOrdersResponse.getStatusCode(), is(HttpStatus.OK));
		assertThat(customerOrdersResponse.getHeaders().getContentType().toString(), is(JSON_CONTENT_TYPE));		
		
		List<CustomerOrder> orders = deserializeList(customerOrdersResponse.getBody(), CustomerOrder.class);
		
		/* assert order data */
		assertThat(orders.size(), is(3));
		assertOrder(orders, LocalDate.now(), LocalDate.now().plusDays(1), 69.98);
		assertOrder(orders, LocalDate.now(), LocalDate.now().plusDays(2), 619.99);
		assertOrder(orders, LocalDate.now(), LocalDate.now().plusDays(3), 783.99);
		
		/* assert hypermedia links */
		assertHref(orders, "self", "http://localhost:8080/api/order/2");
		assertHref(orders, "delete", "http://localhost:8080/api/order/2");
		assertHref(orders, "products", "http://localhost:8080/api/order/2/products");
		
		/* get products for order Id 1 */		
		CustomerOrder orderToRetrieve = orders.stream()
											  .filter(order -> order.getDispatchDate()
													   .equals(LocalDate.now().plusDays(1)))
											  .findFirst()
											  .get();
		ResponseEntity<String> productsResponse = template.getForEntity(orderToRetrieve.getLink("products").getHref(), String.class);
		assertThat(productsResponse.getStatusCode(), is(HttpStatus.OK));
		assertThat(productsResponse.getHeaders().getContentType().toString(), is(JSON_CONTENT_TYPE));		
		
		List<Product> products = deserializeList(productsResponse.getBody(), Product.class);
			
		/* assert product data */
		assertThat(products.size(), is(2));
		assertProduct(products, "prod 1 name", "prod 1 decription", 45.99);
		assertProduct(products, "prod 2 name", "prod 2 decription", 23.99);		
		
		/* assert hypermedia links */
		assertHref(products, "self", "http://localhost:8080/api/product/1");
		assertHref(products, "delete-from-order", "http://localhost:8080/api/order/1/product/1");			
	}
	
	
	private <T extends ResourceSupport> void assertHref(List<T> resources, String rel, String href){
		
		assertTrue(resources.stream().anyMatch(resource -> {
					 	return resource.getLink(rel) != null && 
						   resource.getLink(rel).getHref() != null &&
					       resource.getLink(rel).getHref().equals(href);
			}));		
	}
	
	private void assertOrder(List<CustomerOrder> orders, LocalDate orderDate, 
							 LocalDate dispatchDate, Double totalOrderAmount){
		
		assertTrue(orders.stream().anyMatch(order -> {
				return order.getOrderDate().isEqual(orderDate) &&
					   order.getDispatchDate().isEqual(dispatchDate) &&
					   order.getTotalOrderAmount().equals(totalOrderAmount);
 		}));
	}
	
	private void assertProduct(List<Product> products, String name, 
							   String description, Double price){

		assertTrue(products.stream().anyMatch(product -> {
		return product.getName().equals(name) &&
			   product.getDescription().equals(description) &&
			   product.getPrice().equals(price);
		}));
	}
	
	private <T> List<T> deserializeList(String json, Class<T> clazz) throws Exception {			
		return objectMapper.readValue(json, TypeFactory.defaultInstance().constructCollectionType(List.class, clazz));
	}
	
	private <T> T serialize(String json, Class<T> clazz) throws Exception {				
		return objectMapper.readValue(json, clazz);
	}
}