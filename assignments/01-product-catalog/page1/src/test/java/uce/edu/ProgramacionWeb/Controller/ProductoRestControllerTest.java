package uce.edu.ProgramacionWeb.Controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uce.edu.ProgramacionWeb.Exception.ProductNotFoundException;
import uce.edu.ProgramacionWeb.Model.Producto;
import uce.edu.ProgramacionWeb.Service.ProductoService;

@WebMvcTest(ProductoRestController.class)
@ActiveProfiles("test")
class ProductoRestControllerTest {

    private static final String VALID_PRODUCT = """
            {"codigo":"P001","nomProducto":"Notebook","precio":12.50,"stock":3}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    // Listing products returns status 200 and a JSON array of products.
    @Test
    void listReturns200() throws Exception {
        when(productoService.listarProducto()).thenReturn(List.of(product()));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].numProducto").value(1))
                .andExpect(jsonPath("$[0].nomProducto").value("Notebook"));
    }

    // Getting an existing product returns status 200 and its data.
    @Test
    void getReturns200WhenProductExists() throws Exception {
        when(productoService.getProductoPorNumProd(1L)).thenReturn(product());

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numProducto").value(1))
                .andExpect(jsonPath("$.codigo").value("P001"));
    }

    // Creating a valid product returns status 201 and its resource location.
    @Test
    void postReturns201WithLocation() throws Exception {
        when(productoService.insertarProducto(any(Producto.class))).thenReturn(product());

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PRODUCT))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/productos/1"))
                .andExpect(jsonPath("$.numProducto").value(1))
                .andExpect(jsonPath("$.nomProducto").value("Notebook"));

        verify(productoService).insertarProducto(any(Producto.class));
    }

    // A negative price returns status 400 with a price error and is not saved.
    @Test
    void postReturns400WhenPriceIsInvalid() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"P001","nomProducto":"Notebook","precio":-1,"stock":3}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.precio").isNotEmpty());

        verify(productoService, never()).insertarProducto(any(Producto.class));
    }

    // Getting a missing product returns status 404 as a problem-details response.
    @Test
    void getReturns404WhenProductDoesNotExist() throws Exception {
        when(productoService.getProductoPorNumProd(1L))
                .thenThrow(new ProductNotFoundException(1L));

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Product not found"))
                .andExpect(jsonPath("$.detail").value(new ProductNotFoundException(1L).getMessage()));
    }

    // Updating an existing product returns status 200 and the saved product.
    @Test
    void putReturns200WhenProductExists() throws Exception {
        when(productoService.actualizarProducto(eq(1L), any(Producto.class))).thenReturn(product());

        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PRODUCT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numProducto").value(1))
                .andExpect(jsonPath("$.nomProducto").value("Notebook"));

        verify(productoService).actualizarProducto(eq(1L), any(Producto.class));
    }

    // Updating a missing product returns status 404.
    @Test
    void putReturns404WhenProductDoesNotExist() throws Exception {
        when(productoService.actualizarProducto(eq(1L), any(Producto.class)))
                .thenThrow(new ProductNotFoundException(1L));

        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PRODUCT))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Product not found"));
    }

    // Deleting an existing product returns status 204 with no response body.
    @Test
    void deleteReturns204WhenProductExists() throws Exception {
        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(productoService).eliminarProdPorNumProd(1L);
    }

    // Deleting a missing product returns status 404.
    @Test
    void deleteReturns404WhenProductDoesNotExist() throws Exception {
        doThrow(new ProductNotFoundException(1L))
                .when(productoService).eliminarProdPorNumProd(1L);

        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Product not found"));
    }

    private Producto product() {
        Producto producto = new Producto();
        producto.setNumProducto(1L);
        producto.setCodigo("P001");
        producto.setNomProducto("Notebook");
        producto.setPrecio(new BigDecimal("12.50"));
        producto.setStock(3);
        return producto;
    }
}
