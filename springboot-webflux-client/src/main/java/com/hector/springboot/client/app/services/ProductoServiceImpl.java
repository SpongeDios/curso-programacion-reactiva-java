package com.hector.springboot.client.app.services;

import com.hector.springboot.client.app.models.Producto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProductoServiceImpl implements ProductoService{

    private final WebClient webClient;

    public ProductoServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Flux<Producto> findAll() {
        return webClient
                .get()
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToFlux(response -> response.bodyToFlux(Producto.class));
    }

    @Override
    public Mono<Producto> findById(String id) {
        Map<String, Object> params = Collections.singletonMap("id", id);
        return webClient
                .get()
                .uri("/{id}", params)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> response.bodyToMono(Producto.class));
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return webClient
                .post()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(producto))
                .exchangeToMono(response -> response.bodyToMono(Producto.class));
    }

    @Override
    public Mono<Producto> update(Producto producto, String id) {
        Map<String, Object> params = Collections.singletonMap("id", id);
        return webClient
                .put()
                .uri("/{id}", params)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(producto))
                .exchangeToMono(response -> response.bodyToMono(Producto.class));
    }

    @Override
    public Mono<Void> delete(String id) {
        return webClient
                .delete()
                .uri("/{id}", Collections.singletonMap("id", id))
                .exchangeToMono(response -> response.bodyToMono(Void.class));
    }
}
