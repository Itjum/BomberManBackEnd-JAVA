package fr.epita.assistant.jws.converter;

import fr.epita.assistant.jws.domain.entity.GameEntity;
import fr.epita.assistant.jws.domain.entity.PlayerEntity;
import fr.epita.assistant.jws.presentation.rest.response.GameDetailResponseDTO;

import javax.enterprise.context.ApplicationScoped;
import java.util.stream.Collectors;

@ApplicationScoped
public class PlayerEntityToGameDetailResponseDTO {
    public GameDetailResponseDTO convert(GameEntity game) {
        return new GameDetailResponseDTO(
                game.startTime,
                game.state,
                game.players.stream().map(this::convertPlayer).collect(Collectors.toList()),
                game.map,
                game.id
        );
    }

    public GameDetailResponseDTO.Player convertPlayer(PlayerEntity player) {
        return new GameDetailResponseDTO.Player(
                player.id,
                player.name,
                player.lives,
                player.posX,
                player.posY
        );
    }
}
