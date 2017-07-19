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
import javax.persistence.OneToOne;

import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@ToString
public class Customer extends ResourceSupport {

	public Customer() {
	}
	
	public Customer(String firstName, String lastName, LocalDate dateOfBirth, Address address) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.dateOfBirth = dateOfBirth;
		this.address = address;
	}

	@Id
	@Getter
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long customerId;
	
	@Setter
	@Getter
	private String firstName;
	
	@Setter
	@Getter
	private String lastName;
	
	@Setter	
	@Getter
	private LocalDate dateOfBirth;

	@Setter
	@Getter
	@OneToOne(cascade = {CascadeType.ALL})
	private Address address;
	
	@Setter
	@Getter
	@JsonBackReference
	@OneToMany(cascade = { CascadeType.ALL })
	private Set<CustomerOrder> orders;
	
	public void addOrder(CustomerOrder order){
		if(orders == null){
			orders = new HashSet<>();
		}
		orders.add(order);
	}
}