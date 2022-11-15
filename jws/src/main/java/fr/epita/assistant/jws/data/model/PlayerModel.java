package fr.epita.assistant.jws.data.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "player")
@AllArgsConstructor
@NoArgsConstructor @With @ToString
public class PlayerModel extends PanacheEntityBase {
    public @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    public String name;
    public LocalDateTime lastBomb;
    public LocalDateTime lastMovement;
    public int position;
    public int lives;
    public int posX;
    public int posY;
    public @ManyToOne GameModel game_id;
}
