package uce.edu.ProgramacionWeb.Client;

import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import uce.edu.ProgramacionWeb.Exception.CharacterNotFoundException;
import uce.edu.ProgramacionWeb.Exception.ExternalApiException;

// Talks to the public Rick and Morty API (https://rickandmortyapi.com).
// The browser now calls OUR backend and the backend calls this API, so we can
// add timeouts, clean error messages and later things like caching.
@Component
public class RickAndMortyClient {

    private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT =
            new ParameterizedTypeReference<>() { };

    private final RestClient restClient;

    public RickAndMortyClient(@Value("${rickandmorty.api-url:https://rickandmortyapi.com/api}") String baseUrl) {
        // If the external API is slow we give up instead of leaving the user waiting forever
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    // One page of characters, optionally filtered by name and status (alive, dead, unknown).
    // The JSON is returned as it comes ({ info, results }) so the front end keeps working the same way.
    public Map<String, Object> searchCharacters(int page, String name, String status) {
        try {
            return restClient.get()
                    .uri(uri -> uri.path("/character")
                            .queryParam("page", page)
                            .queryParam("name", name == null ? "" : name.trim())
                            .queryParam("status", status == null ? "" : status.trim())
                            .build())
                    .retrieve()
                    .body(JSON_OBJECT);
        } catch (HttpClientErrorException.NotFound e) {
            throw new CharacterNotFoundException("No characters found with that name");
        } catch (RestClientException e) {
            throw new ExternalApiException(e);
        }
    }

    public Map<String, Object> getCharacter(int id) {
        try {
            return restClient.get().uri("/character/{id}", id).retrieve().body(JSON_OBJECT);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new CharacterNotFoundException("Character not found with ID: " + id);
            }
            throw new ExternalApiException(e);
        } catch (RestClientException e) {
            throw new ExternalApiException(e);
        }
    }
}
