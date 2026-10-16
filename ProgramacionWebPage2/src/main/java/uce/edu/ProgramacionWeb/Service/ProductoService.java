package uce.edu.ProgramacionWeb.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uce.edu.ProgramacionWeb.Model.Producto;
import uce.edu.ProgramacionWeb.Repository.ProductoRepository;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    // Inserts (if numProducto is null) or updates (if it already exists)
    public Producto insertarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public List<Producto> listarProducto() {
        return productoRepository.findAll();
    }

    public Producto getProductoPorNumProd(long numProducto) {
        Optional<Producto> optional = productoRepository.findById(numProducto);
        return optional.orElseThrow(() ->
            new RuntimeException("Product not found with ID: " + numProducto)
        );
    }

    public void eliminarProdPorNumProd(long numProd) {
        this.productoRepository.deleteById(numProd);
    }
}
