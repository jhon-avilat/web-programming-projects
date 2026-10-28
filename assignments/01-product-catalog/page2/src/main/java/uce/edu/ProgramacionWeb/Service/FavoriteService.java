package uce.edu.ProgramacionWeb.Service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uce.edu.ProgramacionWeb.Dto.FavoriteRequest;
import uce.edu.ProgramacionWeb.Exception.FavoriteAlreadyExistsException;
import uce.edu.ProgramacionWeb.Exception.FavoriteNotFoundException;
import uce.edu.ProgramacionWeb.Model.Favorite;
import uce.edu.ProgramacionWeb.Repository.FavoriteRepository;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    // Newest favorites first
    public List<Favorite> listar() {
        return favoriteRepository.findAllByOrderByAddedAtDesc();
    }

    // Saves a character as favorite. The same character can't be saved twice.
    public Favorite agregar(FavoriteRequest request) {
        if (favoriteRepository.existsByCharacterId(request.characterId())) {
            throw new FavoriteAlreadyExistsException(request.characterId());
        }
        Favorite favorite = new Favorite();
        favorite.setCharacterId(request.characterId());
        favorite.setName(request.name());
        favorite.setImage(request.image());
        favorite.setStatus(request.status());
        favorite.setSpecies(request.species());
        favorite.setAddedAt(LocalDateTime.now());
        return favoriteRepository.save(favorite);
    }

    @Transactional
    public void eliminar(int characterId) {
        Favorite favorite = favoriteRepository.findByCharacterId(characterId)
                .orElseThrow(() -> new FavoriteNotFoundException(characterId));
        favoriteRepository.delete(favorite);
    }
}
