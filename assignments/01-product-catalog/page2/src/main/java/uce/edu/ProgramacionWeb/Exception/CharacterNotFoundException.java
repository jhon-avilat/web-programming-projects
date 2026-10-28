package uce.edu.ProgramacionWeb.Exception;

// The Rick and Morty API answered 404 (no character with that id / name)
public class CharacterNotFoundException extends RuntimeException {

    public CharacterNotFoundException(String message) {
        super(message);
    }
}
