package fr.epita.assistant.jws.presentation.rest.response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor @NoArgsConstructor
public class GameDetailResponseDTO {
    public LocalDateTime startTime;
    public String state;
    public List<Player> players;
    public List<String> map;
    public Long id;

    @AllArgsConstructor @NoArgsConstructor
    public static class Player {
        public Long id;
        public String name;
        public int lives;
        public int posX;
        public int posY;
    }
}
