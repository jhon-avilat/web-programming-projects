package uce.edu.ProgramacionWeb.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
}
