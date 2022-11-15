package fr.epita.assistant.jws.converter;

import fr.epita.assistant.jws.data.model.GameModel;
import fr.epita.assistant.jws.data.model.PlayerModel;
import fr.epita.assistant.jws.domain.entity.GameEntity;
import fr.epita.assistant.jws.domain.entity.PlayerEntity;
import groovy.lang.Singleton;

import javax.enterprise.context.ApplicationScoped;
import java.util.stream.Collectors;

@ApplicationScoped
public class GameModelToGameEntity {
    public GameEntity convert(GameModel model) {
        return new GameEntity(
                model.startTime,
                model.state,
                model.players.stream().map(item -> convertPlayer(item))
                        .collect(Collectors.toList()),
                model.map,
                model.id
        );
    }

    public PlayerEntity convertPlayer(PlayerModel player) {
        return new PlayerEntity(
                player.id,
                player.name,
                player.lastBomb,
                player.lastMovement,
                player.position,
                player.lives,
                player.posX,
                player.posY
        );
    }
}
