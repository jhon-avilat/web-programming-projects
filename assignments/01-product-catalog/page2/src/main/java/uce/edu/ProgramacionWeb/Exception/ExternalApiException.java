package uce.edu.ProgramacionWeb.Exception;

// The Rick and Morty API is down, too slow or answered with an error.
// We answer 502 Bad Gateway: the problem is not in our app but in the service we depend on.
public class ExternalApiException extends RuntimeException {

    public ExternalApiException(Throwable cause) {
        super("The Rick and Morty API is not available right now", cause);
    }
}
