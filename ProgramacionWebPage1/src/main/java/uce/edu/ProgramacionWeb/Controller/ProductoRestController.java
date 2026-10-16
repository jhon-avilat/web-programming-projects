package uce.edu.ProgramacionWeb.Controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uce.edu.ProgramacionWeb.Model.Producto;
import uce.edu.ProgramacionWeb.Service.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public List<Producto> listar() {
        return productoService.listarProducto();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable long id) {
        try {
            return ResponseEntity.ok(productoService.getProductoPorNumProd(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public Producto crear(@RequestBody Producto producto) {
        producto.setNumProducto(null);
        return productoService.insertarProducto(producto);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable long id, @RequestBody Producto producto) {
        producto.setNumProducto(id);
        return productoService.insertarProducto(producto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable long id) {
        productoService.eliminarProdPorNumProd(id);
        return ResponseEntity.noContent().build();
    }
}
