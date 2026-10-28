package uce.edu.ProgramacionWeb.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uce.edu.ProgramacionWeb.Dto.FavoriteRequest;
import uce.edu.ProgramacionWeb.Exception.FavoriteAlreadyExistsException;
import uce.edu.ProgramacionWeb.Exception.FavoriteNotFoundException;
import uce.edu.ProgramacionWeb.Model.Favorite;
import uce.edu.ProgramacionWeb.Repository.FavoriteRepository;

class FavoriteServiceTest {

    private FavoriteRepository repository;
    private FavoriteService service;

    @BeforeEach
    void setUp() {
        repository = mock(FavoriteRepository.class);
        service = new FavoriteService(repository);
    }

    // A new favorite is saved with the data from the request and the current date.
    @Test
    void addSavesNewFavorite() {
        when(repository.existsByCharacterId(1)).thenReturn(false);
        when(repository.save(any(Favorite.class))).thenAnswer(call -> call.getArgument(0));

        Favorite saved = service.agregar(new FavoriteRequest(1, "Rick Sanchez", "img", "Alive", "Human"));

        assertThat(saved.getCharacterId()).isEqualTo(1);
        assertThat(saved.getName()).isEqualTo("Rick Sanchez");
        assertThat(saved.getAddedAt()).isNotNull();
    }

    // The same character can't be added twice.
    @Test
    void addThrowsWhenAlreadyFavorite() {
        when(repository.existsByCharacterId(1)).thenReturn(true);

        assertThatThrownBy(() -> service.agregar(new FavoriteRequest(1, "Rick Sanchez", null, null, null)))
                .isInstanceOf(FavoriteAlreadyExistsException.class);
        verify(repository, never()).save(any());
    }

    // Removing an existing favorite deletes it.
    @Test
    void removeDeletesFavorite() {
        Favorite favorite = new Favorite();
        when(repository.findByCharacterId(1)).thenReturn(Optional.of(favorite));

        service.eliminar(1);

        verify(repository).delete(favorite);
    }

    // Removing a character that is not a favorite throws FavoriteNotFoundException.
    @Test
    void removeThrowsWhenNotFavorite() {
        when(repository.findByCharacterId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(99)).isInstanceOf(FavoriteNotFoundException.class);
    }
}
