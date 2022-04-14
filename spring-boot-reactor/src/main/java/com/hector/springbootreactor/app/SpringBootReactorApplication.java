package com.hector.springbootreactor.app;

import com.hector.springbootreactor.app.dto.Comentarios;
import com.hector.springbootreactor.app.dto.Usuario;
import com.hector.springbootreactor.app.dto.UsuarioComentario;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger  log = LoggerFactory.getLogger(SpringBootReactorApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        ejemploIterable();
//        ejemploFlatMap();
//        ejemploToString();
//        ejemploConvirtiendoAMono();
//        ejemploUsuarioComentarioFlatMap();
//        ejemploUsuarioComentarioZipWithCombinandoFlujos();
//        ejemploUsuarioComentarioZipWithFormaDos();
//        ejemploUsuarioComentarioZipWithConRangos();
//        ejemploIntervalo();
//        ejemploDelayElements();
//        ejemploIntervaloInfinito();
//        ejemploIntervaloInfinitoDesdeCreate();
        ejemploContrapresion();

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
    private void ejemploUsuarioComentarioZipWithCombinandoFlujos(){
        Mono<Usuario> usuarioMono = Mono.just(new Usuario("Hector", "Alvarado"));
        Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
            Comentarios comentarios = new Comentarios();
            comentarios.addComentario("Hola desde un comentario");
            comentarios.addComentario("Manana es viernes");
            comentarios.addComentario("Tengo que comprarme pantalones");
            return comentarios;
        });

        Mono<UsuarioComentario> usuarioComentarioMono = usuarioMono.zipWith(comentariosMono, (usuario, comentarios) -> new UsuarioComentario(usuario, comentarios));
        usuarioComentarioMono.subscribe(usuarioComentario -> System.out.println(usuarioComentario + " desde el mundo de los zipWIth que combinan dos flujos y lo convierten en 1"));
    }

    private void ejemploUsuarioComentarioZipWithFormaDos(){
        Mono<Usuario> usuarioMono = Mono.just(new Usuario("Hector", "Alvarado"));
        Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
            Comentarios comentarios = new Comentarios();
            comentarios.addComentario("Hola desde un comentario");
            comentarios.addComentario("Manana es viernes");
            comentarios.addComentario("Tengo que comprarme pantalones");
            return comentarios;
        });

        Mono<UsuarioComentario> usuarioComentarioMono = usuarioMono.zipWith(comentariosMono)
                .map(tuple ->  {
                    Usuario u = tuple.getT1();
                    Comentarios c = tuple.getT2();
                    return new UsuarioComentario(u, c);
                });
        usuarioComentarioMono.subscribe(usuarioComentario -> System.out.println(usuarioComentario + " desde el mundo de los zipWIth de otra forma"));
    }

    private void ejemploUsuarioComentarioZipWithConRangos(){
        //Otra forma de utilizar el range
        Flux<Integer> rangos = Flux.range(0,4);

        Flux.just(1,2,3,4)
                .map(n -> n*2)
                .zipWith(Flux.range(0,4), (uno, dos) -> String.format("Primer flux: %d y Segundo flux: %d", uno, dos))
                .subscribe(System.out::println);

    }

    //Dos formas de aplicar intervalos de tiempo
    private void ejemploIntervalo(){
        Flux<Integer> rango = Flux.range(1,12);
        Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

        rango.zipWith(retraso,(ra, re) -> ra)
                .doOnNext(i -> System.out.println(i.toString()) )
                .subscribe();
    }

    private void ejemploDelayElements(){
        Flux<Integer> rango = Flux.range(1,12)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(System.out::println);
        rango.subscribe();
    }

    private void ejemploIntervaloInfinito() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Flux.interval(Duration.ofSeconds(1))
                .doOnTerminate(latch::countDown)
                .flatMap(i -> {
                    if (i == 5){
                        return Flux.error(new InterruptedException("Solo hasta 5!"));
                    }
                    return Flux.just(i);
                })
                .map(i -> "Hola "+i)
                .retry(2)
                .subscribe(System.out::println, e -> System.out.println(e.getMessage()));
        latch.await();
    }

    //El metodo create para crear un Observable desde cero

    private void ejemploIntervaloInfinitoDesdeCreate() throws InterruptedException {
        Flux.create(emitter -> {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                private int contador = 0;
                @Override
                public void run() {
                    emitter.next(++contador);
                    if (contador == 10){
                        timer.cancel();
                        emitter.complete();
                    }
                }
            }, 1000, 1000);
        })
                .doOnNext(System.out::println)
                .doOnComplete(() -> System.out.println("Hemos terminado"))
                .subscribe();
    }

    ///////Manejando la contrapresion//////
    //Controlando la cantidad de elementos que se envian///
    //PROCESAMOS LO QUE PODEMOS PROCESAR//
    private void ejemploContrapresion(){
        Flux.range(1,10)
                .log()
                .limitRate(2)
                //.subscribe(System.out::println);
                .subscribe(new Subscriber<Integer>() {

                    private Subscription subscription;
                    private final int limite = 2;
                    private int consumido = 0;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.subscription = s;
                        s.request(limite);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        System.out.println(integer);
                        consumido++;
                        if (consumido == limite){
                            consumido = 0;
                            subscription.request(limite);
                            System.out.println("TERMINO UNA PETICION");
                        }
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


}
