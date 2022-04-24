package com.hector.springboot.client.app.handler;

import com.hector.springboot.client.app.models.Producto;
import com.hector.springboot.client.app.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.rsocket.RSocketProperties;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ProductoHandler {

    private final ProductoService productoService;

    public Mono<ServerResponse> listar(ServerRequest serverRequest){
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productoService.findAll(), Producto.class);
    }

    public Mono<ServerResponse> ver(ServerRequest serverRequest){
        String id = serverRequest.pathVariable("id");
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productoService.findById(id), Producto.class)
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(error -> {
                    WebClientResponseException errorResponse = (WebClientResponseException) error;
                    if (errorResponse.getStatusCode().is4xxClientError()){
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(errorResponse.getResponseBodyAsString());
                    }
                    return Mono.error(errorResponse);
                });
    }

    public Mono<ServerResponse> save(ServerRequest serverRequest){
        Mono<Producto> producto = serverRequest.bodyToMono(Producto.class);
        return producto.flatMap(p ->  {
                    if (Objects.isNull(p.getDate())){
                        p.setDate(new Date());
                    }
                    return productoService.save(p);
                })
                .flatMap(p -> ServerResponse
                        .created(URI.create("/api/client".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p)
                )
                .onErrorResume(Mono::error);
    }

    public Mono<ServerResponse> update(ServerRequest serverRequest){
        String id = serverRequest.pathVariable("id");
        Mono<Producto> productoNew = serverRequest.bodyToMono(Producto.class);

        return productoNew.flatMap(p -> ServerResponse
                .created(URI.create("/api/client".concat(id)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(productoService.update(p, id), Producto.class));
    }

    public Mono<ServerResponse> delete(ServerRequest serverRequest){
        String id = serverRequest.pathVariable("id");
        return productoService.delete(id)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> upload(ServerRequest serverRequest){
        String id = serverRequest.pathVariable("id");
        return serverRequest.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> productoService.upload(file, id))
                .flatMap(p -> ServerResponse
                        .created(URI.create("/api/client/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p));
    }
}
