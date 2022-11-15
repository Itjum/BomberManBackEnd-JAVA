package fr.epita.assistant.jws.domain.service;
import fr.epita.assistant.jws.converter.GameModelToGameEntity;
import fr.epita.assistant.jws.data.model.GameModel;
import fr.epita.assistant.jws.data.model.PlayerModel;
import fr.epita.assistant.jws.data.repository.GameRepository;
import fr.epita.assistant.jws.domain.entity.GameEntity;
import fr.epita.assistant.jws.utils.MapUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ApplicationScoped
public class GameService {
    @Inject GameRepository gameRepository;
    @Inject GameModelToGameEntity gameModelToGameEntity;
    @Inject MapUtils mapService;

    public List<GameEntity> getAll() {
        var todos = gameRepository.findAll().stream()
                .map(game -> gameModelToGameEntity.convert(game)).collect(Collectors.toList());
        return todos;
    }

    public GameEntity getByID(Long id) {
        var gameByID = gameRepository.findById(id);
        if (gameByID == null) {
            return null;
        }
        var realMap = new ArrayList<>(gameByID.map);
        gameByID.map = realMap;
        return gameModelToGameEntity.convert(gameByID);
    }

    public GameModel getModelByID(Long id) {
        var gameByID = gameRepository.findById(id);
        if (gameByID == null) {
            return null;
        }
        var realMap = new ArrayList<>(gameByID.map);
        gameByID.map = realMap;
        return gameByID;
    }

    @Transactional
    public GameEntity createGame(String name) {
        var gameModel = new GameModel()
                .withState("STARTING")
                .withStartTime(LocalDateTime.now())
                .withPlayers(new ArrayList<>())
                .withMap(mapService.getMap());
        var playerModel = new PlayerModel()
                .withName(name)
                .withPosX(1)
                .withPosY(1)
                .withLives(3);
        playerModel.game_id = gameModel;
        gameModel.players.add(playerModel);
        gameRepository.persistAndFlush(gameModel);
        return gameModelToGameEntity.convert(gameModel);
    }

    @Transactional
    public GameEntity startGameEntity (GameModel game) {
        if (game.state.equals("STARTING") && game.players.size() > 1) {
            game.state = "RUNNING";
            return gameModelToGameEntity.convert(game);
        }
        else if (game.players.size() == 1 && !game.state.equals("FINISHED")) {
            game.state = "FINISHED";
            return gameModelToGameEntity.convert(game);
        }
        else {
            return new GameEntity()
                    .withState("404");
        }
    }


    @Transactional
    public GameEntity movePlayer (Long id, Long playerId, int posX, int posY) {
        var gameModel = gameRepository.findById(id);
        if (gameModel == null) {
            return new GameEntity()
                    .withState("404");
        }
        //game is not running
        if (!gameModel.state.equals("RUNNING")) {
            return new GameEntity()
                    .withState("400");
        }

        PlayerModel playerModel = null;
        for (int i = 0; i < gameModel.players.size(); i++) {
            if (gameModel.players.get(i).id.equals(playerId)) {
                playerModel = gameModel.players.get(i);
                break;
            }
        }
        //player not found
        if (playerModel == null) {
            return new GameEntity()
                    .withState("404");
        }
        //player is dead
        if (playerModel.lives == 0) {
            return new GameEntity()
                    .withState("400");
        }
        //player cannot be moved to the specific position
        if (!checkMove(gameModel.map, posX, posY)) {
            return new GameEntity()
                    .withState("400");
        }
        //player has already moved during the last X ticks
        if (playerModel.lastMovement != null) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            Timestamp lastMovement = Timestamp.valueOf(playerModel.lastMovement);
            if (now.getTime() - lastMovement.getTime() < getTickDuration() * getMoveDelay()) {
                return new GameEntity()
                        .withState("429");
            }
        }

        playerModel.posY = posY;
        playerModel.posX = posX;
        playerModel.lastMovement = LocalDateTime.now();

        return gameModelToGameEntity.convert(gameModel);
    }

    public int getTickDuration() {
        String getTick = System.getenv("JWS_TICK_DURATION");
        if (getTick == null) {
            getTick = "100";
        }
        return Integer.parseInt(getTick);
    }

    public int getMoveDelay() {
        String getTick = System.getenv("JWS_DELAY_MOVEMENT");
        if (getTick == null) {
            getTick = "1";
        }
        return Integer.parseInt(getTick);
    }

    public int getBombDelay() {
        String getTick = System.getenv("JWS_DELAY_BOMB");
        if (getTick == null) {
            getTick = "20";
        }
        return Integer.parseInt(getTick);
    }

    public boolean checkMove(List<String> myList, int posX, int posY) {
        String line = myList.get(posY);
        int i = 0;
        int num = 0;
        while (num <= posX) {
            num += Integer.parseInt(String.valueOf(line.charAt(i)));
            i += 2;
        }
        if (line.charAt(i - 1) == 'G') {
            return true;
        }
        return false;
    }

    @Transactional
    public GameEntity putBomb(Long id, Long playerId, int posX, int posY) {
        var gameModel = gameRepository.findById(id);
        //game does not exist
        if (gameModel == null) {
            return new GameEntity()
                    .withState("404");
        }

        //get player
        PlayerModel playerModel = null;
        for (int i = 0; i < gameModel.players.size(); i++) {
            if (gameModel.players.get(i).id.equals(playerId)) {
                playerModel = gameModel.players.get(i);
                break;
            }
        }
        //player not found
        if (playerModel == null) {
            return new GameEntity()
                    .withState("404");
        }
        //player dead
        if (playerModel.lives == 0) {
            return new GameEntity()
                    .withState("400");
        }
        
        //player has already moved during the last X ticks
        if (playerModel.lastBomb != null) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            Timestamp lastMovement = Timestamp.valueOf(playerModel.lastBomb);
            if (now.getTime() - lastMovement.getTime() <= getTickDuration() * getBombDelay()) {
                return new GameEntity()
                        .withState("429");
            }
        }
        //game is not running
        if (!gameModel.state.equals("RUNNING")) {
            return new GameEntity()
                    .withState("400");
        }
        if (playerModel.posX != posX || playerModel.posY != posY) {
            return new GameEntity()
                    .withState("400");
        }
        playerModel.lastBomb = LocalDateTime.now();
        //check if you can set bomb and set bomb
        SetBombCheck(gameModel, posX, posY);
        int JWS_TICK_DURATION = getTickDuration();
        int JWS_DELAY_BOMB = getBombDelay();
        var executor = CompletableFuture.delayedExecutor((long) JWS_TICK_DURATION * JWS_DELAY_BOMB, TimeUnit.MILLISECONDS);
        CompletableFuture.runAsync(() -> RemoveBombCheck(id, posX, posY), executor);
        return gameModelToGameEntity.convert(gameModel);
    }

    @Transactional
    public void RemoveBombCheck(Long id, int posX, int posY) {
        var gameModel = gameRepository.findById(id);
        String mapLine = gameModel.map.get(posY);
        StringBuilder decodeLine = decode(mapLine);
        if (decodeLine.charAt(posX) == 'B') {
            decodeLine.setCharAt(posX, 'G');
            String res = encode(decodeLine);
            gameModel.map.set(posY, res);
        }
        gameRepository.persistAndFlush(gameModel);
        RemoveBlock(id, posX, posY);
        RemoveHealth(id, posX, posY);
    }

    @Transactional
    public void RemoveBlock(Long id, int posX, int posY) {
        var gameModel = gameRepository.findById(id);
        String mapLine1 = gameModel.map.get(posY);
        StringBuilder decodeLine = decode(mapLine1);
        if (decodeLine.charAt(posX + 1) == 'W') {
            decodeLine.setCharAt(posX + 1, 'G');
            String res = encode(decodeLine);
            gameModel.map.set(posY, res);
        }
        if (decodeLine.charAt(posX - 1) == 'W') {
            decodeLine.setCharAt(posX - 1, 'G');
            String res = encode(decodeLine);
            gameModel.map.set(posY, res);
        }
        String mapLine2 = gameModel.map.get(posY - 1);
        decodeLine = decode(mapLine2);
        if (decodeLine.charAt(posX) == 'W') {
            decodeLine.setCharAt(posX, 'G');
            String res = encode(decodeLine);
            gameModel.map.set(posY - 1, res);
        }
        String mapLine3 = gameModel.map.get(posY + 1);
        decodeLine = decode(mapLine3);
        if (decodeLine.charAt(posX) == 'W') {
            decodeLine.setCharAt(posX, 'G');
            String res = encode(decodeLine);
            gameModel.map.set(posY + 1, res);
        }
        gameRepository.persistAndFlush(gameModel);
    }

    @Transactional
    public void RemoveHealth(Long id, int posX, int posY) {
        var gameModel = gameRepository.findById(id);
        int count = gameModel.players.size();
        for (int i = 0; i < gameModel.players.size(); i++) {
            if (gameModel.players.get(i).posX == posX - 1 && gameModel.players.get(i).posY == posY) {
                gameModel.players.get(i).lives -= 1;
                if (gameModel.players.get(i).lives == 0) {
                    count -= 1;
                }
            }
            if (gameModel.players.get(i).posX == posX + 1 && gameModel.players.get(i).posY == posY) {
                gameModel.players.get(i).lives -= 1;
                if (gameModel.players.get(i).lives == 0) {
                    count -= 1;
                }
            }
            if (gameModel.players.get(i).posX == posX && gameModel.players.get(i).posY == posY - 1) {
                gameModel.players.get(i).lives -= 1;
                if (gameModel.players.get(i).lives == 0) {
                    count -= 1;
                }
            }
            if (gameModel.players.get(i).posX == posX && gameModel.players.get(i).posY == posY + 1) {
                gameModel.players.get(i).lives -= 1;
                if (gameModel.players.get(i).lives == 0) {
                    count -= 1;
                }
            }
        }
        if (count <= 1) {
            gameModel.state = "FINISHED";
        }
        gameRepository.persistAndFlush(gameModel);
    }

    public void SetBombCheck(GameModel gameModel, int posX, int posY) {
        String mapLine = gameModel.map.get(posY);
        StringBuilder decodeLine = decode(mapLine);
        if (decodeLine.charAt(posX) == 'G') {
            decodeLine.setCharAt(posX, 'B');
            String res = encode(decodeLine);
            gameModel.map.set(posY, res);
        }
    }

    public static StringBuilder decode(String line) {
        StringBuilder resStr = new StringBuilder();
        for (int i = 0; i < line.length(); i += 2) {
            int mul = Integer.parseInt(String.valueOf(line.charAt(i)));
            for (int j = 0; j < mul; j++) {
                resStr.append(line.charAt(i + 1));
            }
        }
        return resStr;
    }

    public static String encode(StringBuilder line) {
        StringBuilder encode = new StringBuilder();
        for (int i = 0; i < line.length();) {
            int count = 0;
            char temp = line.charAt(i);
            for (int j = i; j < line.length() && line.charAt(j) == temp; j++) {
                count++;
            }
            encode.append(String.valueOf(count));
            encode.append(temp);
            i += count;
        }
        return encode.toString();
    }

}
