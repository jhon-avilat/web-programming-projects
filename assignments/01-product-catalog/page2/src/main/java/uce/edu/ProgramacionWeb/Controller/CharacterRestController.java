package uce.edu.ProgramacionWeb.Controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uce.edu.ProgramacionWeb.Client.RickAndMortyClient;

@RestController
@RequestMapping("/api/characters")
public class CharacterRestController {

    private final RickAndMortyClient client;

    public CharacterRestController(RickAndMortyClient client) {
        this.client = client;
    }

    // GET /api/characters?page=2&name=rick&status=alive
    @GetMapping
    public Map<String, Object> search(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "") String name,
                                      @RequestParam(defaultValue = "") String status) {
        return client.searchCharacters(Math.max(page, 1), name, status);
    }

    // GET /api/characters/1
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable int id) {
        return client.getCharacter(id);
    }
}
