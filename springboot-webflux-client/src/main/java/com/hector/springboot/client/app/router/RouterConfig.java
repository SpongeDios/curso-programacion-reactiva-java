package com.hector.springboot.client.app.router;

import com.hector.springboot.client.app.handler.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {

    private static final String PATH = "/api/client";

    @Bean
    public RouterFunction<ServerResponse> rutas(ProductoHandler productoHandler){
        return RouterFunctions.route(RequestPredicates.GET(PATH), request -> productoHandler.listar(request))
                .andRoute(RequestPredicates.GET(PATH.concat("/{id}")), request -> productoHandler.ver(request))
                .andRoute(RequestPredicates.POST(PATH), request -> productoHandler.save(request))
                .andRoute(RequestPredicates.PUT(PATH.concat("/{id}")), request -> productoHandler.update(request))
                .andRoute(RequestPredicates.DELETE(PATH.concat("/{id}")), request -> productoHandler.delete(request))
                .andRoute(RequestPredicates.POST(PATH.concat("/upload/{id}")), request -> productoHandler.upload(request));
    }
}
