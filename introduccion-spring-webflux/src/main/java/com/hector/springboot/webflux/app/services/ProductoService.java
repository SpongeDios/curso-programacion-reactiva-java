package com.hector.springboot.webflux.app.services;

import com.hector.springboot.webflux.app.models.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {
    Flux<Producto> findAll();
    Flux<Producto> findAllNombreUpperCase();
    Flux<Producto> findAllNombreUpperCaseWithRepeat();
    Mono<Producto> findById(String id);
    Mono<Producto> save(Producto producto);
    Mono<Void> delete(Producto producto);
}
