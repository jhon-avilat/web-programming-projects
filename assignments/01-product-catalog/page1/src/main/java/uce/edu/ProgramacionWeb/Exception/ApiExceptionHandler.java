package uce.edu.ProgramacionWeb.Exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Only applies to @RestController classes (the JSON API).
// The Thymeleaf pages keep their normal error pages.
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

    // Answers with the standard "problem details" JSON (RFC 9457), e.g.
    // { "status": 404, "title": "Product not found", "detail": "Product not found with ID: 99" }
    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleNotFound(ProductNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Product not found");
        return problem;
    }

    // @Valid failed on a JSON body: 400 with one message per wrong field, e.g.
    // { "status": 400, "title": "Invalid product data", "errors": { "precio": "Price cannot be negative" } }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Some fields are not valid");
        problem.setTitle("Invalid product data");
        problem.setProperty("errors", errors);
        return problem;
    }
}
