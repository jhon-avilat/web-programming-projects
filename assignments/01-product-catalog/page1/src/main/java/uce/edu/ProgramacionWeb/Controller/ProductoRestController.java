package uce.edu.ProgramacionWeb.Controller;

import java.net.URI;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uce.edu.ProgramacionWeb.Model.Producto;
import uce.edu.ProgramacionWeb.Service.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    private final ProductoService productoService;

    // Constructor injection: Spring passes the dependency in, the field can be final
    // and the class is easy to test with a mock.
    public ProductoRestController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> listar() {
        return productoService.listarProducto();
    }

    // If the id does not exist the service throws ProductNotFoundException
    // and ApiExceptionHandler turns it into a 404
    @GetMapping("/{id}")
    public Producto obtener(@PathVariable long id) {
        return productoService.getProductoPorNumProd(id);
    }

    // 201 Created + Location header pointing to the new product
    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        producto.setNumProducto(null);
        Producto creado = productoService.insertarProducto(producto);
        return ResponseEntity.created(URI.create("/api/productos/" + creado.getNumProducto())).body(creado);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable long id, @Valid @RequestBody Producto producto) {
        return productoService.actualizarProducto(id, producto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable long id) {
        productoService.eliminarProdPorNumProd(id);
        return ResponseEntity.noContent().build();
    }
}
