package uce.edu.ProgramacionWeb.Exception;

public class FavoriteAlreadyExistsException extends RuntimeException {

    public FavoriteAlreadyExistsException(int characterId) {
        super("Character " + characterId + " is already in favorites");
    }
}
