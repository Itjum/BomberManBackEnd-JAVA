package fr.epita.assistant.jws.data.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity @Table(name = "game")
@AllArgsConstructor @NoArgsConstructor @With @ToString
public class GameModel extends PanacheEntityBase {
    public @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    public LocalDateTime startTime;
    public String state;
    public @OneToMany(cascade = CascadeType.ALL) List<PlayerModel> players;
    public @ElementCollection @CollectionTable(name = "game_map") List<String> map;
}
