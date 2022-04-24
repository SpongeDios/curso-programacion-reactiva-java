package com.hector.springboot.webflux.app.handler;

import com.hector.springboot.webflux.app.models.Categoria;
import com.hector.springboot.webflux.app.models.Producto;
import com.hector.springboot.webflux.app.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.web.reactive.function.BodyInserters.*;


@Component
@RequiredArgsConstructor
public class ProductoHandler {

    private final ProductoService productoService;
    private final Validator validator;

    public Mono<ServerResponse> listar (ServerRequest request){
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productoService.findAll().repeat(500), Producto.class);
    }

    @Value("${config.uploads.path}")
    private String path;

    public Mono<ServerResponse> ver(ServerRequest request){
        String id = request.pathVariable("id");
        return productoService.findById(id)
                .flatMap(p -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(p)))
                        .switchIfEmpty(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> crear(ServerRequest request){
        Mono<Producto> producto = request.bodyToMono(Producto.class);
        return producto.flatMap(p -> {

            Errors errors = new BeanPropertyBindingResult(p, Producto.class.getName());
            validator.validate(p, errors);

            if (errors.hasErrors()) {
                return Flux.fromIterable(errors.getFieldErrors())
                        .map(fieldError -> "El campo "+ fieldError.getField() + " " + fieldError.getDefaultMessage())
                        .collectList()
                        .flatMap(lista -> ServerResponse.badRequest().body(fromValue(lista)));

            } else {
                if (Objects.isNull(p.getDate())) {
                    p.setDate(new Date());
                }
                return productoService.save(p).flatMap(pdb -> ServerResponse
                        .created(URI.create("/api/v2/productos".concat(pdb.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(pdb)));
            }
        });
    }

    public Mono<ServerResponse> update(ServerRequest request){
        String id = request.pathVariable("id");
        Mono<Producto> productoFromDB = productoService.findById(id);
        Mono<Producto> productoFromBody = request.bodyToMono(Producto.class);
        return productoFromDB.zipWith(productoFromBody, (pDB, pBODY) -> {
            pDB.setNombre(pBODY.getNombre());
            pDB.setCategoria(pBODY.getCategoria());
            pDB.setPrecio(pBODY.getPrecio());
            return pDB;
        }).flatMap(p -> ServerResponse
                .created(URI.create("/api/v2/productos/"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(productoService.save(p), Producto.class))
                .switchIfEmpty(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> deleteById(ServerRequest serverRequest){
        String id = serverRequest.pathVariables().get("id");
        Mono<Producto> producto = productoService.findById(id);
        return producto
                .flatMap(p -> productoService.delete(p)
                        .then(ServerResponse
                        .noContent()
                        .build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> upload(ServerRequest request){
        String id = request.pathVariable("id");
        return request.multipartData()
                .map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(filePart -> productoService.findById(id)
                        .flatMap(p -> {
                            p.setFoto(UUID.randomUUID()+" - "+filePart.filename()
                                    .replace(" ", "")
                                    .replace(":","")
                                    .replace("\\", ""));
                            return filePart.transferTo(new File(path + p.getFoto()))
                                    .then(productoService.save(p));
                        }))
                .flatMap(producto -> ServerResponse.created(URI.create("/api/v2/productos".concat(producto.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(producto)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> crearConFoto(ServerRequest request){
        Mono<Producto> producto = request.multipartData()
                .map(multipart -> {
                    FormFieldPart nombre = (FormFieldPart) multipart.toSingleValueMap().get("nombre");
                    FormFieldPart precio = (FormFieldPart) multipart.toSingleValueMap().get("precio");
                    FormFieldPart categoriaId = (FormFieldPart) multipart.toSingleValueMap().get("categoria.id");
                    FormFieldPart categoriaNombre = (FormFieldPart) multipart.toSingleValueMap().get("categoria.nombre");

                    Categoria categoria = new Categoria(categoriaId.value(),categoriaNombre.value());
                    return new Producto(nombre.value(), Double.parseDouble(precio.value()), categoria);
                });
        return request.multipartData()
                .map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(filePart -> producto
                        .flatMap(p -> {
                            p.setFoto(UUID.randomUUID()+" - "+filePart.filename()
                                    .replace(" ", "")
                                    .replace(":","")
                                    .replace("\\", ""));
                            p.setDate(new Date());
                            return filePart.transferTo(new File(path + p.getFoto()))
                                    .then(productoService.save(p));
                        }))
                .flatMap(productoxd -> ServerResponse.created(URI.create("/api/v2/productos".concat(productoxd.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromValue(productoxd)));
    }

}
