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

// Turns our exceptions into clean JSON errors (RFC 9457 "problem details").
// Only applies to @RestController classes; the Thymeleaf pages keep their normal error pages.
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler({CharacterNotFoundException.class, FavoriteNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException ex) {
        return problem(HttpStatus.NOT_FOUND, "Not found", ex.getMessage());
    }

    @ExceptionHandler(FavoriteAlreadyExistsException.class)
    public ProblemDetail handleConflict(FavoriteAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "Already a favorite", ex.getMessage());
    }

    // The external API failed: 502 Bad Gateway (the problem is not in our app)
    @ExceptionHandler(ExternalApiException.class)
    public ProblemDetail handleExternalApi(ExternalApiException ex) {
        return problem(HttpStatus.BAD_GATEWAY, "External API error", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Invalid data", "Some fields are not valid");
        problem.setProperty("errors", errors);
        return problem;
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
