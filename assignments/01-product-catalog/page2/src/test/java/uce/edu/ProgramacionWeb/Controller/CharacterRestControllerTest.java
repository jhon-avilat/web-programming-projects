package uce.edu.ProgramacionWeb.Controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uce.edu.ProgramacionWeb.Client.RickAndMortyClient;
import uce.edu.ProgramacionWeb.Exception.CharacterNotFoundException;
import uce.edu.ProgramacionWeb.Exception.ExternalApiException;

// The real Rick and Morty API is never called here: the client is a mock,
// so the tests work offline and always give the same result.
@WebMvcTest(CharacterRestController.class)
@ActiveProfiles("test")
class CharacterRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RickAndMortyClient client;

    // Searching passes page, name and status to the client and returns its JSON as it is.
    @Test
    void searchReturns200() throws Exception {
        when(client.searchCharacters(2, "rick", "alive")).thenReturn(Map.of(
                "info", Map.of("count", 1, "pages", 1),
                "results", List.of(Map.of("id", 1, "name", "Rick Sanchez"))));

        mockMvc.perform(get("/api/characters").param("page", "2").param("name", "rick").param("status", "alive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.count").value(1))
                .andExpect(jsonPath("$.results[0].name").value("Rick Sanchez"));
    }

    // A page below 1 is treated as page 1 instead of sending a bad request upstream.
    @Test
    void searchUsesPageOneWhenPageIsTooSmall() throws Exception {
        when(client.searchCharacters(1, "", "")).thenReturn(Map.of("results", List.of()));

        mockMvc.perform(get("/api/characters").param("page", "0"))
                .andExpect(status().isOk());

        verify(client).searchCharacters(1, "", "");
    }

    // No character with that name returns 404.
    @Test
    void searchReturns404WhenNothingMatches() throws Exception {
        when(client.searchCharacters(1, "zzz", "")).thenThrow(new CharacterNotFoundException("No characters found with that name"));

        mockMvc.perform(get("/api/characters").param("name", "zzz"))
                .andExpect(status().isNotFound());
    }

    // Getting one character returns 200 with its data.
    @Test
    void getReturns200() throws Exception {
        when(client.getCharacter(1)).thenReturn(Map.of("id", 1, "name", "Rick Sanchez"));

        mockMvc.perform(get("/api/characters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rick Sanchez"));
    }

    // If the external API is down we answer 502 Bad Gateway.
    @Test
    void getReturns502WhenExternalApiFails() throws Exception {
        when(client.getCharacter(1)).thenThrow(new ExternalApiException(new RuntimeException("timeout")));

        mockMvc.perform(get("/api/characters/1"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("External API error"));
    }
}
