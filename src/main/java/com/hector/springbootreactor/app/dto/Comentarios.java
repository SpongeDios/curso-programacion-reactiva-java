package com.hector.springbootreactor.app.dto;

import java.util.ArrayList;
import java.util.List;

public class Comentarios {

    private List<String> comentarios = new ArrayList<>();

    public Comentarios(List<String> comentarios) {
        this.comentarios = comentarios;
    }

    public Comentarios() {
    }

    public List<String> getComentarios() {
        return comentarios;
    }

    public void addComentario(String comentario) {
        this.comentarios.add(comentario);
    }

    @Override
    public String toString() {
        return "Comentarios{" +
                "comentarios=" + comentarios +
                '}';
    }
}
