package uce.edu.ProgramacionWeb.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// JSON body of POST /api/favorites. A record is a short way to write a class that only holds data.
public record FavoriteRequest(
        @NotNull(message = "characterId is required")
        @Positive(message = "characterId must be a positive number")
        Integer characterId,

        @NotBlank(message = "name is required")
        @Size(max = 255)
        String name,

        @Size(max = 500)
        String image,

        @Size(max = 50)
        String status,

        @Size(max = 100)
        String species) {
}
