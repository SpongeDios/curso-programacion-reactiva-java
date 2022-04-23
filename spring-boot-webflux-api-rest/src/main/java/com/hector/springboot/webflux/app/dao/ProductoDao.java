package com.hector.springboot.webflux.app.dao;

import com.hector.springboot.webflux.app.models.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductoDao extends ReactiveMongoRepository<Producto, String> {

    Mono<Producto> findByNombre(String nombre);

}
