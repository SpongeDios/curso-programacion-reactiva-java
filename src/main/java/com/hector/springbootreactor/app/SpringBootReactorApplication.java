package com.hector.springbootreactor.app;

import com.hector.springbootreactor.app.dto.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

import java.util.Locale;
import java.util.Objects;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger  log = LoggerFactory.getLogger(SpringBootReactorApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Flux<Usuario> nombres = Flux.just("Andres", "Pedro", "Diego", "Juan", "xd", "Hector")
                .doOnNext(elemento -> {
                    if (Objects.isNull(elemento) || elemento.isEmpty()) {
                        throw new RuntimeException("El nombre no puede estar vacio");
                    }
                })
                .map(nombre -> new Usuario(nombre.toUpperCase(Locale.ROOT), ""));

        //Segun la prueba que acabo de hacer el subscribe esta funcionando igual que un foreach
        nombres.subscribe(System.out::println,
                error -> System.out.println(error.getMessage()),
                () -> {
                    System.out.println("Ha finalizado el flujo");
                }
        );

    }
}
