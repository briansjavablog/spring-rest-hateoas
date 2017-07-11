package com.blog.rest.hateoas.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.hateoas.ResourceSupport;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity(name="")
@ToString
@EqualsAndHashCode(callSuper=false)
public class Product extends ResourceSupport {

	public Product(){}
	
	public Product(String name, String description, Double price) {
		this.name = name;
		this.description = description;
		this.price = price;
	}
	
	@Id
	@Getter
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long productId;
	
	@Setter
	@Getter
	private String name;
	
	@Setter
	@Getter
	private String description;
	
	@Setter
	@Getter
	private Double price;	
}