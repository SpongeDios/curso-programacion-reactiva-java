package com.hector.springboot.webflux.app.controllers;

import com.hector.springboot.webflux.app.dao.ProductoDao;
import com.hector.springboot.webflux.app.models.Producto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Locale;

@RestController
@RequestMapping("/api/productos")
@Slf4j
public class ProductoRestController {
    private final ProductoDao productoDao;

    public ProductoRestController(ProductoDao productoDao) {
        this.productoDao = productoDao;
    }

    @GetMapping
    public Flux<Producto> index(){
        return productoDao.findAll()
                .map(producto -> {
                    producto.setNombre(producto.getNombre().toUpperCase(Locale.ROOT));
                    return producto;
                })
                .doOnNext(producto -> log.info(producto.getNombre()));
    }

    @GetMapping("/{id}")
    public Mono<Producto> show(@PathVariable String id){
        //dos formas de hacer lo mismo
        Mono<Producto> producto = productoDao.findById(id);
        return productoDao.findAll().filter(p -> p.getId().equals(id)).next();
    }

}
