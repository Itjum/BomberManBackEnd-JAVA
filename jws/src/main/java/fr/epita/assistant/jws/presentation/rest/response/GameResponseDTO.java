package fr.epita.assistant.jws.presentation.rest.response;

import lombok.*;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @With @ToString
public class GameResponseDTO {
    public Long id;
    public int players;
    public String state;
}