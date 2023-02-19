package com.api.rest.springboot.webflux.DAO;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.api.rest.springboot.webflux.documents.Client;


public interface ClientDAO extends ReactiveMongoRepository<Client, String> {
	

}
