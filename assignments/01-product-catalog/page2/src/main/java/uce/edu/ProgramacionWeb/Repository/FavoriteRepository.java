package uce.edu.ProgramacionWeb.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uce.edu.ProgramacionWeb.Model.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // Spring Data builds these queries from the method names
    boolean existsByCharacterId(Integer characterId);

    Optional<Favorite> findByCharacterId(Integer characterId);

    List<Favorite> findAllByOrderByAddedAtDesc();
}
