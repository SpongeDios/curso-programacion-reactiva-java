package com.hector.springboot.webflux.app;

import com.hector.springboot.webflux.app.dao.ProductoDao;
import com.hector.springboot.webflux.app.models.Categoria;
import com.hector.springboot.webflux.app.models.Producto;
import com.hector.springboot.webflux.app.services.ProductoService;
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

    private final ProductoService productoDao;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public IntroduccionSpringWebfluxApplication(ProductoService productoDao, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.productoDao = productoDao;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(IntroduccionSpringWebfluxApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        reactiveMongoTemplate.dropCollection("productos").subscribe();
        reactiveMongoTemplate.dropCollection("categorias").subscribe();

        Categoria electronico = new Categoria("Electronico");
        Categoria moda = new Categoria("Moda");
        Categoria computacion = new Categoria("Computacion");
        Categoria muebles = new Categoria("Muebles");

        Flux.just(electronico, moda, computacion, muebles)
                .flatMap(productoDao::saveCategory)
                .doOnNext(categoria -> log.info("Categoria creada : {}, Id: {}", categoria.getNombre(), categoria.getId()))
                .thenMany(Flux.just(
                                new Producto("Televeision Panasonic", 456.34, electronico),
                                new Producto("Polera Balenciaga", 322.45, moda),
                                new Producto("Sony Camara HD", 177.79, electronico),
                                new Producto("Curso Libertad Financiera", 997.77, computacion),
                                new Producto("Audifonos Nike", 399.33, muebles)
                        )
                        .flatMap(producto -> {
                            producto.setDate(new Date());
                            return productoDao.save(producto);
                        }))
                .subscribe(producto -> log.info(producto.getId()));
    }
}
