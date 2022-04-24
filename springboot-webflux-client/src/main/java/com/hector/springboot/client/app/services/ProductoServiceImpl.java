package com.hector.springboot.client.app.services;

import com.hector.springboot.client.app.exceptions.ProductoException;
import com.hector.springboot.client.app.models.Producto;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
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
                .retrieve()
                .bodyToMono(Producto.class);
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return webClient
                .post()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(producto))
                .exchangeToMono(response -> response.bodyToMono(Producto.class))
                .onErrorResume(Mono::error);
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
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<Producto> upload(FilePart filePart, String id) {
        MultipartBodyBuilder parts = new MultipartBodyBuilder();
        parts.asyncPart("file", filePart.content(), DataBuffer.class).headers(h -> {
            h.setContentDispositionFormData("file", filePart.filename());
        });

        return webClient
                .post()
                .uri("/upload/{id}", Collections.singletonMap("id", id))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(parts.build())
                .retrieve()
                .bodyToMono(Producto.class);
    }
}
