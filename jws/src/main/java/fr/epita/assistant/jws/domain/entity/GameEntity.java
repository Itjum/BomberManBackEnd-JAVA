package fr.epita.assistant.jws.domain.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor @NoArgsConstructor
@With
public class GameEntity {
    public LocalDateTime startTime;
    public String state;
    public List<PlayerEntity> players;
    public List<String> map;
    public Long id;
}
