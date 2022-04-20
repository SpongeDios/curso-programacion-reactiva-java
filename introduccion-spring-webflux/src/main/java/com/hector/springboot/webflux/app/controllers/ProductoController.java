package com.hector.springboot.webflux.app.controllers;

import com.hector.springboot.webflux.app.models.Categoria;
import com.hector.springboot.webflux.app.models.Producto;
import com.hector.springboot.webflux.app.services.ProductoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Controller
@Slf4j
@SessionAttributes("producto")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Value("${config.uploads.path}")
    private String pathToImages;

    @ModelAttribute("categorias")
    public Flux<Categoria> categorias(){
        return productoService.findAllCategories();
    }

    @GetMapping("/uploads/img/{nombreFoto:.+}")
    public Mono<ResponseEntity<Resource>> verFoto(@PathVariable String nombreFoto) throws MalformedURLException {
        Path path = Paths.get(pathToImages).resolve(nombreFoto).toAbsolutePath();
        Resource imagen = new UrlResource(path.toUri());
        return Mono.just(
                ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename\"" + imagen.getFilename() + "\"")
                        .body(imagen)
        );
    }

    @GetMapping("/ver/{id}")
    public Mono<String> ver(Model model, @PathVariable String id){
        return productoService.findById(id)
                .doOnNext(p -> {
                    model.addAttribute("producto", p);
                    model.addAttribute("titulo", "Detalle Producto");
                }).switchIfEmpty(Mono.just(new Producto()))
                .flatMap(p -> {
                    if (Objects.isNull(p.getId())){
                        return Mono.error(new InterruptedException("No existe el producto"));
                    }
                    return Mono.just(p);
                }).then(Mono.just("ver"))
                .onErrorResume(err -> Mono.just("redirect:/listar?error=el+producto+no+existe"));
    }

    @GetMapping({"/listar", "/"})
    public String listar(Model model){
        Flux<Producto> productos = productoService.findAllNombreUpperCase();
        productos.subscribe(producto -> log.info(producto.getNombre()));
        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "Listado de productos");
        return "listar";
    }

    @GetMapping("/eliminar/{id}")
    public Mono<String> eliminar (@PathVariable String id){
        return productoService.findById(id)
                .defaultIfEmpty(new Producto())
                .flatMap(p ->{
                    if (Objects.isNull(p.getId())){
                        return Mono.error(new InterruptedException("No existe el producto a eliminar"));
                    }
                    return Mono.just(p);
                }).flatMap(p ->{
                    log.info("Eliminando producto: {} con nombre: {}", p.getId(), p.getNombre());
                    return productoService.delete(p);
                }).then(Mono.just("redirect:/listar?success=producto+eliminado+con+exito"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto+a+eliminar"));
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
        model.addAttribute("boton", "Crear");
        return "form";
    }

    @GetMapping("/form-v2/{id}")
    public Mono<String> editarv2(@PathVariable String id, Model model){
        return productoService.findById(id)
                .doOnNext(producto -> {
                    model.addAttribute("titulo", "Editar producto");
                    model.addAttribute("producto", producto);
                    model.addAttribute("boton", "Editar");
                }).defaultIfEmpty(new Producto())
                .flatMap(p -> {
                    if (Objects.isNull(p.getId())){
                        return Mono.error(new InterruptedException("No existe el producto a editar"));
                    }
                    return Mono.just(p);
                })
                .then(Mono.just("form"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error+no+existe+el+producto"));
    }


    @GetMapping("/form/{id}")
    public String editar(@PathVariable String id, Model model){
        Mono<Producto> producto = productoService.findById(id)
                .defaultIfEmpty(new Producto());
        model.addAttribute("titulo", "Editar producto");
        model.addAttribute("producto", producto);
        model.addAttribute("boton", "Editar");
        return "form";
    }


    @PostMapping("/form")
    public Mono<String> save(@Valid Producto producto, BindingResult result, SessionStatus status, Model model, @RequestPart FilePart file){
        if (result.hasErrors()){
            model.addAttribute("titulo", "Error en el formulario de producto");
            model.addAttribute("boton", "Guardar");
            return Mono.just("form");
        }else{
            status.setComplete();
            if (producto.getDate() == null){
                producto.setDate(new Date());
            }

            if (!file.filename().isEmpty()){
                producto.setFoto(UUID.randomUUID().toString()+ "-" +file.filename()
                        .replace(" ", "")
                        .replace(":","")
                        .replace("\\", "")
                );

            }

            Mono<Categoria> categoria = productoService.findCategoryById(producto.getCategoria().getId());
            return categoria.flatMap(c-> {
                producto.setCategoria(c);
                return productoService.save(producto);
            }).doOnNext(p -> {
                log.info("La categoria: {} se ha almacenado correctamente", p.getCategoria().getNombre());
                log.info(String.format("El producto %s se ha almacenado", p.getNombre()));
            })
            .flatMap(p -> {
                if (!file.filename().isEmpty()){
                    return file.transferTo(new File(pathToImages + p.getFoto()));
                }
                return Mono.empty();
            })
            .then(Mono.just("redirect:/listar?success=producto+guardado+con+exito"));
        }
    }
}
