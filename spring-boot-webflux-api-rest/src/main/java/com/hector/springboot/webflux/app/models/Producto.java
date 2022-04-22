package com.hector.springboot.webflux.app.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter
@Setter
@Document(collection = "productos")
public class Producto {

    @Id
    private String id;
    @NotEmpty
    private String nombre;
    @NotNull
    private Double precio;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;
    @Valid
    @NotNull
    private Categoria categoria;
    private String foto;

    public Producto(String nombre, Double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    public Producto(String nombre, Double precio, Categoria categoria) {
        this(nombre,precio);
        this.categoria = categoria;
    }

    public Producto() {
    }


}
