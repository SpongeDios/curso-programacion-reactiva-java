package com.hector.springboot.webflux.app.services.impl;

import com.hector.springboot.webflux.app.dao.CategoriaDao;
import com.hector.springboot.webflux.app.dao.ProductoDao;
import com.hector.springboot.webflux.app.models.Categoria;
import com.hector.springboot.webflux.app.models.Producto;
import com.hector.springboot.webflux.app.services.ProductoService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Locale;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoDao productoDao;
    private final CategoriaDao categoriaDao;

    public ProductoServiceImpl(ProductoDao productoDao, CategoriaDao categoriaDao) {
        this.productoDao = productoDao;
        this.categoriaDao = categoriaDao;
    }

    @Override
    public Flux<Producto> findAll() {
        return productoDao.findAll();
    }

    @Override
    public Flux<Producto> findAllNombreUpperCase() {
        return productoDao.findAll()
                .map(producto -> {
                    producto.setNombre(producto.getNombre().toUpperCase(Locale.ROOT));
                    return producto;
                });
    }

    @Override
    public Flux<Producto> findAllNombreUpperCaseWithRepeat() {
        return findAllNombreUpperCase().repeat(10000);
    }

    @Override
    public Mono<Producto> findById(String id) {
        return productoDao.findById(id);
    }

    @Override
    public Mono<Producto> save(Producto producto) {
        return productoDao.save(producto);
    }

    @Override
    public Mono<Void> delete(Producto producto) {
        return productoDao.delete(producto);
    }

    @Override
    public Flux<Categoria> findAllCategories() {
        return categoriaDao.findAll();
    }

    @Override
    public Mono<Categoria> findCategoryById(String id) {
        return categoriaDao.findById(id);
    }

    @Override
    public Mono<Categoria> saveCategory(Categoria categoria) {
        return categoriaDao.save(categoria);
    }
}
