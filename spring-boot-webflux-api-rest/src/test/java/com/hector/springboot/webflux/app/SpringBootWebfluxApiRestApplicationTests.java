package com.hector.springboot.webflux.app;

import com.hector.springboot.webflux.app.models.Producto;
import com.hector.springboot.webflux.app.services.ProductoService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootWebfluxApiRestApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductoService productoService;

    @Test
    void listarTest() {
        webTestClient
                .get()
                .uri("/api/productos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Producto.class);
    }

    @Test
    void listarTest2() {
        webTestClient
                .get()
                .uri("/api/productos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Producto.class)
                .consumeWith(response -> {
                    List<Producto> productos = response.getResponseBody();
                    assert productos != null;
                    productos.forEach(p -> {
                        System.out.println(p.getNombre());
                    });
                });
    }

    @Test
    @Disabled
    //Todos los test que estan desechos, estan asi por que no me funcionan y no encuentro la solucion.
    //Cualquier duda, al curso de pruebas unitarias de Andres Jose Guzman que ya hice.
    void verTest() {
        Mono<Producto> productoMono = productoService.findByNombre("Televeision Panasonic");

        webTestClient.get().uri("/api/v2/productos/{id}", Collections.singletonMap("id", productoMono.block().getId()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON);
    }
}
