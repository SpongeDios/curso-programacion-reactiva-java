package com.hector.springboot.webflux.app;

import com.hector.springboot.webflux.app.handler.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouterFunctionConfig {
    //Creando y configurando componentes Router functions y Handler

    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandler productoHandler){
        return route(GET("/api/v2/productos").or(GET("/api/v3/productos")), request -> productoHandler.listar(request))
                .andRoute(GET("/api/v2/productos/{id}"), request -> productoHandler.ver(request))
                .andRoute(POST("/api/v2/productos"), request -> productoHandler.crear(request))
                .andRoute(PUT("/api/v2/productos/{id}"), request -> productoHandler.update(request))
                .andRoute(DELETE("/api/v2/productos/{id}"), request -> productoHandler.deleteById(request))
                .andRoute(POST("/api/v2/productos/upload/{id}"), request -> productoHandler.upload(request))
                .andRoute(POST("/api/v2/productos/crear"), request -> productoHandler.crearConFoto(request));
    }




}
