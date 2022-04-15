package com.hector.springboot.webflux.app.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
@Document(collection = "productos")
public class Producto {

    @Id
    private String id;
    private String nombre;
    private Double precio;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    public Producto(String nombre, Double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    public Producto() {
    }


}
