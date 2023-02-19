package com.api.rest.springboot.webflux.controller;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.api.rest.springboot.webflux.documents.Client;
import com.api.rest.springboot.webflux.services.ClientService;
import com.fasterxml.jackson.databind.type.MapType;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

	@Autowired
	private ClientService service;

	@Value("${config.uploads.path}")
	private String path;

	@PostMapping("/resgisterClientWithImage")
	public Mono<ResponseEntity<Client>> resgisterClientWithImage(Client client, @RequestPart FilePart file) {
		client.setImage(UUID.randomUUID().toString() + "-"
				+ file.filename().replace(" ", "").replace(":", "").replace("//", ""));

		return file.transferTo(new File(path + client.getImage())).then(service.save(client))
				.map(c -> ResponseEntity.created(URI.create("/api/clients/".concat(c.getId())))
						.contentType(MediaType.APPLICATION_JSON_UTF8).body(c));

	}

	@PostMapping("/upload/{id}")
	public Mono<ResponseEntity<Client>> uploadImage(@PathVariable String id, @RequestPart FilePart file) {
		return service.findById(id).flatMap(c -> {
			c.setImage(UUID.randomUUID().toString() + "-"
					+ file.filename().replace(" ", "").replace(":", "").replace("//", ""));

			return file.transferTo(new File(path + c.getImage())).then(service.save(c));
		}).map(c -> ResponseEntity.ok(c)).defaultIfEmpty(ResponseEntity.notFound().build());

	}

	@GetMapping
	public Mono<ResponseEntity<Flux<Client>>> ClientsList() {
		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(service.findAll()));

	}

	@GetMapping("/{id}")
	public Mono<ResponseEntity<Client>> clientDetails(@PathVariable String id) {
		return service.findById(id).map(c -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(c))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PostMapping
	public Mono<ResponseEntity<Map<String, Object>>> saveClient(@Valid @RequestBody Mono<Client> monoClient) {
		Map<String, Object> response = new HashMap<>();

		return monoClient.flatMap(client -> {
			return service.save(client).map(c -> {
				response.put("client", c);
				response.put("message", "Client saved successfully");
				response.put("timestamp", new Date());
				return ResponseEntity.created(URI.create("/api/clients".concat(c.getId())))
						.contentType(MediaType.APPLICATION_JSON_UTF8).body(response);
			});
		}).onErrorResume(t -> {
			return Mono.just(t).cast(WebExchangeBindException.class).flatMap(e -> Mono.just(e.getFieldErrors()))
					.flatMapMany(Flux::fromIterable)
					.map(fieldError -> "In: " + fieldError.getField() + " " + fieldError.getDefaultMessage())
					.collectList().flatMap(list -> {
						response.put("error", list);
						response.put("timestamp", new Date());
						response.put("status", HttpStatus.BAD_REQUEST.value());

						return Mono.just(ResponseEntity.badRequest().body(response));
					});

		});

	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Client>> editClient(@RequestBody Client client, @PathVariable String id){
		return service.findById(id).flatMap(c ->{
			c.setName(client.getName());
			c.setSurname(client.getSurname());
			c.setAge(client.getAge());
			c.setSalary(client.getSalary());
			return service.save(c);
		}).map(c -> ResponseEntity.created(URI.create("/api/clients/".concat(c.getId())))
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.body(c))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deleteClient(@PathVariable String id){
		return service.findById(id).flatMap(c ->{
			return service.delete(c).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
		}).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}

}
 







































