package com.hector.springboot.reactor.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

@SpringBootApplication
@Slf4j
public class SpringBootReactorApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        //Este es un observabable, si nadie esta suscrito a este observable, nunca sera llamado

        Flux<String> nombres = Flux.just("Andres","Hector", "Diego", "")
                .doOnNext(e -> {
                    if (e.isEmpty()){
                        throw new RuntimeException("Esta vacio");
                    }
                    System.out.println(e);
                });

        nombres.subscribe(log::info,
                error -> log.error(error.getMessage()));
    }
}
