package com.blog.rest.hateoas.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity(name="")
@ToString
@EqualsAndHashCode(callSuper=false)
public class CustomerOrder extends ResourceSupport {

	public CustomerOrder(){}
	
	public CustomerOrder(LocalDate orderDate, LocalDate dispatchDate) {		
		this.orderDate = orderDate;
		this.dispatchDate = dispatchDate;
		this.totalOrderAmount = 0.0;
	}
	
    private long orderId;
	
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	public Long getOrderId(){
		return orderId;
	}
	
	public void setOrderId(long orderId){
		this.orderId = orderId;
	}
	
	@Setter
	@Getter
	private LocalDate orderDate;
	
	@Setter
	@Getter
	private LocalDate dispatchDate;
	
	@Setter
	@Getter
	private Double totalOrderAmount;
	
	@Setter
	private Set<Product> products;
	
	@JsonBackReference
	@OneToMany(cascade = { CascadeType.ALL })
	public Set<Product> getProducts(){
		return products;
	}
	
	public void addProduct(Product product){
		if(products == null){
			products = new HashSet<>();
		}
		products.add(product);
		this.totalOrderAmount = totalOrderAmount + product.getPrice();
	}
}