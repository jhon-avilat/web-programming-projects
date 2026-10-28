package uce.edu.ProgramacionWeb.Exception;

public class FavoriteNotFoundException extends RuntimeException {

    public FavoriteNotFoundException(int characterId) {
        super("Character " + characterId + " is not in favorites");
    }
}
