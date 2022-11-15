package fr.epita.assistant.jws.domain.entity;

import fr.epita.assistant.jws.data.model.GameModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@AllArgsConstructor @NoArgsConstructor
public class PlayerEntity {
    public Long id;
    public String name;
    public LocalDateTime lastBomb;
    public LocalDateTime lastMovement;
    public int position;
    public int lives;
    public int posX;
    public int posY;
}
