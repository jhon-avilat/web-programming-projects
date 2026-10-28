package uce.edu.ProgramacionWeb.Controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uce.edu.ProgramacionWeb.Dto.FavoriteRequest;
import uce.edu.ProgramacionWeb.Exception.FavoriteAlreadyExistsException;
import uce.edu.ProgramacionWeb.Exception.FavoriteNotFoundException;
import uce.edu.ProgramacionWeb.Model.Favorite;
import uce.edu.ProgramacionWeb.Service.FavoriteService;

@WebMvcTest(FavoriteRestController.class)
@ActiveProfiles("test")
class FavoriteRestControllerTest {

    private static final String RICK = """
            {"characterId":1,"name":"Rick Sanchez","image":"https://rickandmortyapi.com/api/character/avatar/1.jpeg","status":"Alive","species":"Human"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoriteService favoriteService;

    // Listing favorites returns 200 and the saved characters.
    @Test
    void listReturns200() throws Exception {
        when(favoriteService.listar()).thenReturn(List.of(rick()));

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].characterId").value(1))
                .andExpect(jsonPath("$[0].name").value("Rick Sanchez"));
    }

    // Saving a new favorite returns 201 and where to find it.
    @Test
    void postReturns201() throws Exception {
        when(favoriteService.agregar(any(FavoriteRequest.class))).thenReturn(rick());

        mockMvc.perform(post("/api/favorites").contentType(MediaType.APPLICATION_JSON).content(RICK))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/favorites/1"))
                .andExpect(jsonPath("$.name").value("Rick Sanchez"));
    }

    // Saving the same character twice returns 409 Conflict.
    @Test
    void postReturns409WhenAlreadyFavorite() throws Exception {
        when(favoriteService.agregar(any(FavoriteRequest.class))).thenThrow(new FavoriteAlreadyExistsException(1));

        mockMvc.perform(post("/api/favorites").contentType(MediaType.APPLICATION_JSON).content(RICK))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Already a favorite"));
    }

    // A body without characterId or name returns 400 and nothing is saved.
    @Test
    void postReturns400WhenDataIsMissing() throws Exception {
        mockMvc.perform(post("/api/favorites").contentType(MediaType.APPLICATION_JSON).content("{\"image\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.characterId").exists())
                .andExpect(jsonPath("$.errors.name").exists());

        verify(favoriteService, never()).agregar(any());
    }

    // Removing a favorite returns 204 No Content.
    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/favorites/1"))
                .andExpect(status().isNoContent());

        verify(favoriteService).eliminar(1);
    }

    // Removing a character that is not a favorite returns 404.
    @Test
    void deleteReturns404WhenNotFavorite() throws Exception {
        doThrow(new FavoriteNotFoundException(99)).when(favoriteService).eliminar(99);

        mockMvc.perform(delete("/api/favorites/99"))
                .andExpect(status().isNotFound());
    }

    private Favorite rick() {
        Favorite favorite = new Favorite();
        favorite.setId(10L);
        favorite.setCharacterId(1);
        favorite.setName("Rick Sanchez");
        favorite.setStatus("Alive");
        favorite.setSpecies("Human");
        favorite.setAddedAt(LocalDateTime.of(2026, 10, 28, 12, 0));
        return favorite;
    }
}
