package uce.edu.ProgramacionWeb.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uce.edu.ProgramacionWeb.Exception.ProductNotFoundException;
import uce.edu.ProgramacionWeb.Model.Producto;
import uce.edu.ProgramacionWeb.Repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    // Getting an existing product returns the product from the repository.
    @Test
    void getReturnsProductWhenFound() {
        Producto producto = new Producto();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertSame(producto, productoService.getProductoPorNumProd(1L));
        verify(productoRepository).findById(1L);
    }

    // Getting a missing product throws a product-not-found exception.
    @Test
    void getThrowsWhenProductDoesNotExist() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productoService.getProductoPorNumProd(1L));
    }

    // Updating uses the requested ID even if the submitted product has another ID.
    @Test
    void updateSetsIdAndSavesWhenFound() {
        Producto producto = new Producto();
        producto.setNumProducto(99L);
        when(productoRepository.existsById(1L)).thenReturn(true);
        when(productoRepository.save(producto)).thenAnswer(invocation -> {
            assertEquals(Long.valueOf(1L), producto.getNumProducto());
            return producto;
        });

        assertSame(producto, productoService.actualizarProducto(1L, producto));
        assertEquals(Long.valueOf(1L), producto.getNumProducto());
        verify(productoRepository).save(producto);
    }

    // Updating a missing product throws an exception without saving anything.
    @Test
    void updateThrowsWhenProductDoesNotExist() {
        when(productoRepository.existsById(1L)).thenReturn(false);

        assertThrows(ProductNotFoundException.class,
                () -> productoService.actualizarProducto(1L, new Producto()));
        verify(productoRepository, never()).save(any(Producto.class));
    }

    // Deleting an existing product asks the repository to delete its ID.
    @Test
    void deleteRemovesProductWhenFound() {
        when(productoRepository.existsById(1L)).thenReturn(true);

        productoService.eliminarProdPorNumProd(1L);

        verify(productoRepository).deleteById(1L);
    }

    // Deleting a missing product throws an exception without deleting anything.
    @Test
    void deleteThrowsWhenProductDoesNotExist() {
        when(productoRepository.existsById(1L)).thenReturn(false);

        assertThrows(ProductNotFoundException.class,
                () -> productoService.eliminarProdPorNumProd(1L));
        verify(productoRepository, never()).deleteById(anyLong());
    }
}
