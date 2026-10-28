package uce.edu.ProgramacionWeb.Controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uce.edu.ProgramacionWeb.Model.Producto;
import uce.edu.ProgramacionWeb.Service.ProductoService;

@WebMvcTest(ProductoController.class)
@ActiveProfiles("test")
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    // The list page renders the products returned by the service.
    @Test
    void listRendersProducts() throws Exception {
        Producto producto = new Producto();
        producto.setNumProducto(1L);
        producto.setNomProducto("Notebook");
        List<Producto> productos = List.of(producto);
        when(productoService.listarProducto()).thenReturn(productos);

        mockMvc.perform(get("/listarProductos"))
                .andExpect(status().isOk())
                .andExpect(view().name("listar_productos"))
                .andExpect(model().attribute("listProductos", productos))
                .andExpect(content().string(containsString("Notebook")));
    }

    // A valid new product is saved and redirects to the product list.
    @Test
    void validFormSavesAndRedirects() throws Exception {
        mockMvc.perform(post("/grabarProducto")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("codigo", "P001")
                        .param("nomProducto", "Notebook")
                        .param("precio", "12.50")
                        .param("stock", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/listarProductos"));

        verify(productoService).insertarProducto(argThat(producto ->
                producto.getNumProducto() == null
                        && "P001".equals(producto.getCodigo())
                        && "Notebook".equals(producto.getNomProducto())
                        && new BigDecimal("12.50").equals(producto.getPrecio())
                        && Integer.valueOf(3).equals(producto.getStock())));
    }

    // Invalid fields show errors on the new-product form without saving.
    @Test
    void invalidFormShowsErrorsWithoutSaving() throws Exception {
        mockMvc.perform(post("/grabarProducto")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("codigo", "")
                        .param("nomProducto", "")
                        .param("precio", "-1")
                        .param("stock", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("nuevo_producto"))
                .andExpect(model().attributeHasFieldErrors(
                        "producto", "codigo", "nomProducto", "precio", "stock"));

        verify(productoService, never()).insertarProducto(any(Producto.class));
    }

    // A POST request deletes the product and redirects to the list.
    @Test
    void postDeleteRedirects() throws Exception {
        mockMvc.perform(post("/eliminarProducto/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/listarProductos"));

        verify(productoService).eliminarProdPorNumProd(1L);
    }

    // A GET request cannot delete a product and receives status 405.
    @Test
    void getDeleteReturns405() throws Exception {
        mockMvc.perform(get("/eliminarProducto/1"))
                .andExpect(status().isMethodNotAllowed());

        verify(productoService, never()).eliminarProdPorNumProd(anyLong());
    }
}
