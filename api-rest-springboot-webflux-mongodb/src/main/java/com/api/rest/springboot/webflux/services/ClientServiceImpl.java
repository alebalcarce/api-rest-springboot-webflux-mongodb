package com.api.rest.springboot.webflux.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.rest.springboot.webflux.DAO.ClientDAO;
import com.api.rest.springboot.webflux.documents.Client;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ClientServiceImpl implements ClientService {

	@Autowired
	private ClientDAO clientDAO;
	
	@Override
	public Flux<Client> findAll() {
		
		return clientDAO.findAll();
	}

	@Override
	public Mono<Client> findById(String id) {
		
		return clientDAO.findById(id) ;
	}

	@Override
	public Mono<Client> save(Client client) {
		
		return clientDAO.save(client);
	}

	@Override
	public Mono<Void> delete(Client client) {
		
		return clientDAO.delete(client);
	}

}
