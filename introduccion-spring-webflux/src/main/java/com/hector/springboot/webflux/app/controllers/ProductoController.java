package com.hector.springboot.webflux.app.controllers;

import com.hector.springboot.webflux.app.models.Producto;
import com.hector.springboot.webflux.app.services.ProductoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Locale;

@Controller
@Slf4j
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping({"/listar", "/"})
    public String listar(Model model){
        Flux<Producto> productos = productoService.findAllNombreUpperCase();
        productos.subscribe(producto -> log.info(producto.getNombre()));
        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");
        return "listar";
    }

    @GetMapping("/listar-datadriver")
    public String listarDataDriver(Model model){
        Flux<Producto> productos = productoService.findAllNombreUpperCase()
                .delayElements(Duration.ofSeconds(1));

        productos.subscribe(producto -> log.info(producto.getNombre()));
        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 1));
        model.addAttribute("titulo", "Listado de productos");
        return "listar";
    }

    @GetMapping("listar-full")
    public String listarFull(Model model){
        Flux<Producto> productos = productoService.findAllNombreUpperCaseWithRepeat();
        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");
        return "listar";
    }

    @GetMapping("listar-chunked")
    public String listarChunked(Model model){
        Flux<Producto> productos = productoService.findAll()
                .map(producto -> {
                    producto.setNombre(producto.getNombre().toUpperCase(Locale.ROOT));
                    return producto;
                }).repeat(10000);
        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");
        return "listarchunked";
    }

    @GetMapping("/form")
    public String crear(Model model){
        model.addAttribute("producto", new Producto());
        model.addAttribute("titulo", "Formulario de producto");
        return "form";
    }

    @PostMapping("/form")
    public Mono<String> save(Producto producto){
        return productoService.save(producto).doOnNext(p -> {
            log.info(String.format("El producto %s se ha almacenado", p.getNombre()));
        }).then(Mono.just("redirect:/listar"));

    }
}
