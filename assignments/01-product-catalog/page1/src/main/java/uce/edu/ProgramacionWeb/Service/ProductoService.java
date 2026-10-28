package uce.edu.ProgramacionWeb.Service;

import java.util.List;
import org.springframework.stereotype.Service;
import uce.edu.ProgramacionWeb.Exception.ProductNotFoundException;
import uce.edu.ProgramacionWeb.Model.Producto;
import uce.edu.ProgramacionWeb.Repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    // Constructor injection: Spring passes the dependency in, the field can be final
    // and the class is easy to test with a mock.
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // Inserts (if numProducto is null) or updates (if it already exists)
    public Producto insertarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    // Updates only if the product exists, so a PUT with a wrong id
    // does not create a new product by accident
    public Producto actualizarProducto(long numProducto, Producto producto) {
        if (!productoRepository.existsById(numProducto)) {
            throw new ProductNotFoundException(numProducto);
        }
        producto.setNumProducto(numProducto);
        return productoRepository.save(producto);
    }

    public List<Producto> listarProducto() {
        return productoRepository.findAll();
    }

    public Producto getProductoPorNumProd(long numProducto) {
        return productoRepository.findById(numProducto)
                .orElseThrow(() -> new ProductNotFoundException(numProducto));
    }

    public void eliminarProdPorNumProd(long numProd) {
        if (!productoRepository.existsById(numProd)) {
            throw new ProductNotFoundException(numProd);
        }
        productoRepository.deleteById(numProd);
    }
}
