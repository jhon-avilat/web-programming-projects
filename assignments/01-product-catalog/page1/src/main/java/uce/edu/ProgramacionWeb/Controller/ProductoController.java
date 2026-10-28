package uce.edu.ProgramacionWeb.Controller;

import org.springframework.stereotype.Controller;
import jakarta.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import uce.edu.ProgramacionWeb.Model.Producto;
import uce.edu.ProgramacionWeb.Service.ProductoService;

@Controller
public class ProductoController {

    private final ProductoService productoService;

    // Constructor injection: Spring passes the dependency in, the field can be final
    // and the class is easy to test with a mock.
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // Main menu
    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    // LIST
    @GetMapping("listarProductos")
    public String listarProductos(Model model) {
        model.addAttribute("listProductos", productoService.listarProducto());
        return "listar_productos";
    }

    // NEW FORM
    @GetMapping("mostrarFormNuevoProducto")
    public String mostrarFormNuevoProducto(Model model) {
        Producto producto = new Producto();
        model.addAttribute("producto", producto);
        return "nuevo_producto";
    }

    // SAVE (used for both insert and update)
    // @Valid checks the rules in Producto. If something is wrong we show the same
    // form again (with the error messages) instead of saving bad data.
    @PostMapping("grabarProducto")
    public String grabarProducto(@Valid @ModelAttribute("producto") Producto producto, BindingResult result) {
        if (result.hasErrors()) {
            return producto.getNumProducto() == null ? "nuevo_producto" : "actualizar_producto";
        }
        productoService.insertarProducto(producto);
        return "redirect:/listarProductos";
    }

    // UPDATE FORM
    @GetMapping("mostrarFormActualizarProd/{numProd}")
    public String mostrarFormActualizarProd(@PathVariable(value = "numProd") long numProd, Model model) {
        Producto producto = productoService.getProductoPorNumProd(numProd);
        model.addAttribute("producto", producto);
        return "actualizar_producto";
    }

    // DELETE
    @GetMapping("/eliminarProducto/{numProd}")
    public String eliminarProducto(@PathVariable(value = "numProd") long numProd) {
        productoService.eliminarProdPorNumProd(numProd);
        return "redirect:/listarProductos";
    }

}
