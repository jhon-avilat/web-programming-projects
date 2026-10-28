package uce.edu.ProgramacionWeb.Controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uce.edu.ProgramacionWeb.Dto.FavoriteRequest;
import uce.edu.ProgramacionWeb.Model.Favorite;
import uce.edu.ProgramacionWeb.Service.FavoriteService;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteRestController {

    private final FavoriteService favoriteService;

    public FavoriteRestController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // GET /api/favorites
    @GetMapping
    public List<Favorite> listar() {
        return favoriteService.listar();
    }

    // POST /api/favorites  -> 201, or 409 if it was already saved
    @PostMapping
    public ResponseEntity<Favorite> agregar(@Valid @RequestBody FavoriteRequest request) {
        Favorite saved = favoriteService.agregar(request);
        return ResponseEntity.created(URI.create("/api/favorites/" + saved.getCharacterId())).body(saved);
    }

    // DELETE /api/favorites/5  -> 204, or 404 if it was not a favorite
    @DeleteMapping("/{characterId}")
    public ResponseEntity<Void> eliminar(@PathVariable int characterId) {
        favoriteService.eliminar(characterId);
        return ResponseEntity.noContent().build();
    }
}
