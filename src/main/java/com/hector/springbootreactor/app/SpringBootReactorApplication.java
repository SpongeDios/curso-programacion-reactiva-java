package com.hector.springbootreactor.app;

import com.hector.springbootreactor.app.dto.Comentarios;
import com.hector.springbootreactor.app.dto.Usuario;
import com.hector.springbootreactor.app.dto.UsuarioComentario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger  log = LoggerFactory.getLogger(SpringBootReactorApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ejemploIterable();
        ejemploFlatMap();
        ejemploToString();
        ejemploConvirtiendoAMono();
        ejemploUsuarioComentarioFlatMap();
    }

    private void ejemploFlatMap(String... args) throws Exception {
        //Crear un flux a partir de una lista Flux.fromIterable
        List<String> nombresLista = Arrays.asList("Andres", "Pedro", "Diego", "Juan", "xd", "Hector");
        Flux<String> listToFluxResult = Flux.fromIterable(nombresLista);

        listToFluxResult.subscribe(nombre -> System.out.println(nombre.toUpperCase(Locale.ROOT) + " desde la lista convertida en flux"));
    }

    private void ejemploIterable(String... args) throws Exception {
        Flux<Usuario> nombres = Flux.just("Andres", "Pedro", "Diego", "Juan", "xd", "Hector")
                .doOnNext(elemento -> {
                    if (Objects.isNull(elemento) || elemento.isEmpty()) {
                        throw new RuntimeException("El nombre no puede estar vacio");
                    }
                })
                .map(nombre -> new Usuario(nombre.toUpperCase(Locale.ROOT), ""));

        //Segun la prueba que acabo de hacer el subscribe esta funcionando igual que un foreach
        nombres.subscribe(System.out::println,
                error -> System.out.println(error.getMessage()),
                () -> {
                    System.out.println("Ha finalizado el flujo");
                }
        );

    }

    private void ejemploToString(String... args) throws Exception {
        List<Usuario> usuarioList = new ArrayList<>();
        Usuario hector = new Usuario("Hector", "Alvarado");
        Usuario vanessa = new Usuario("Vanessa", "Maravez");
        Usuario rosa = new Usuario("Rosa", "Campos");
        Usuario deivid = new Usuario("David", "Ruiz");
        usuarioList.add(hector);
        usuarioList.add(vanessa);
        usuarioList.add(rosa);
        usuarioList.add(deivid);

        Flux.fromIterable(usuarioList)
                .map(usuario -> usuario.getNombre().toUpperCase(Locale.ROOT).concat(" "+ usuario.getApellido().toUpperCase(Locale.ROOT)))
                .flatMap(nombre -> {
                    if (nombre.contains("HECTOR")){
                        return Mono.just(nombre);
                    }else{
                        return Mono.empty();
                    }
                })
                .map(nombre -> nombre.toLowerCase(Locale.ROOT))
                .subscribe(System.out::println);
    }

    private void ejemploConvirtiendoAMono(String... args) throws Exception {
        List<Usuario> usuarioList = new ArrayList<>();
        Usuario hector = new Usuario("Hector", "Alvarado");
        Usuario vanessa = new Usuario("Vanessa", "Maravez");
        Usuario rosa = new Usuario("Rosa", "Campos");
        Usuario deivid = new Usuario("David", "Ruiz");
        usuarioList.add(hector);
        usuarioList.add(vanessa);
        usuarioList.add(rosa);
        usuarioList.add(deivid);

        Flux.fromIterable(usuarioList)
                //Este metodo permite pasar de una lista a un mono
                //El mono quedaria asi: Mono<List<Usuario>>
                .collectList()
                .subscribe(System.out::println);

        Flux.fromIterable(usuarioList)
                //Este metodo permite pasar de una lista a un mono
                //El mono quedaria asi: Mono<List<Usuario>>
                .collectList()
                .subscribe(listaMono -> listaMono.forEach(System.out::println));
    }

    private void ejemploUsuarioComentarioFlatMap(){
        Mono<Usuario> usuarioMono = Mono.just(new Usuario("Hector", "Alvarado"));
        Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
            Comentarios comentarios = new Comentarios();
            comentarios.addComentario("Hola desde un comentario");
            comentarios.addComentario("Manana es viernes");
            comentarios.addComentario("Tengo que comprarme pantalones");
            return comentarios;
        });

        usuarioMono.flatMap(usuario -> comentariosMono.map(comentario -> new UsuarioComentario(usuario, comentario)))
                .subscribe(System.out::println);
    }
}
