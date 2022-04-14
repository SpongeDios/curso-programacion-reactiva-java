package com.hector.springboot.webflux.app;

import com.hector.springboot.webflux.app.dao.ProductoDao;
import com.hector.springboot.webflux.app.models.Producto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
@Slf4j
public class IntroduccionSpringWebfluxApplication implements CommandLineRunner {

    private final ProductoDao productoDao;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public IntroduccionSpringWebfluxApplication(ProductoDao productoDao, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.productoDao = productoDao;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(IntroduccionSpringWebfluxApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        reactiveMongoTemplate.dropCollection("productos").subscribe();
        Flux.just(
            new Producto("Televeision Panasonic", 456.34),
            new Producto("Polera Balenciaga", 322.45),
            new Producto("Sony Camara HD", 177.79),
            new Producto("Curso Libertad Financiera", 997.77),
            new Producto("Audifonos Nike", 399.33)
        )
                .flatMap(producto -> {
                    producto.setDate(new Date());
                    return productoDao.save(producto);
                })
                .subscribe(producto -> log.info(producto.getId()));
    }
}
