package uce.edu.ProgramacionWeb.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Thrown when we look for a product number that is not in the database.
// @ResponseStatus makes the Thymeleaf pages answer 404 instead of a 500 error.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(long numProducto) {
        super("Product not found with ID: " + numProducto);
    }
}
