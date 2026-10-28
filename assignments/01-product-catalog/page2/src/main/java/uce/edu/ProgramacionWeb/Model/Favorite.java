package uce.edu.ProgramacionWeb.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

// A Rick and Morty character the user marked as favorite.
// We save a small copy (name, image, status, species) so the Favorites tab
// can be shown without calling the external API again.
@Entity
@Table(name = "favorite")
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Id of the character in the Rick and Morty API. Unique: no duplicates.
    @Column(nullable = false, unique = true)
    private Integer characterId;

    @Column(nullable = false)
    private String name;

    private String image;
    private String status;
    private String species;
    private LocalDateTime addedAt;

    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Integer characterId) {
        this.characterId = characterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
