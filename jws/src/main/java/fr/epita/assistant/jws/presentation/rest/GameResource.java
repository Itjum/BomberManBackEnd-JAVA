package fr.epita.assistant.jws.presentation.rest;

import fr.epita.assistant.jws.converter.GameModelToGameEntity;
import fr.epita.assistant.jws.converter.PlayerEntityToGameDetailResponseDTO;
import fr.epita.assistant.jws.data.model.PlayerModel;
import fr.epita.assistant.jws.data.repository.GameRepository;
import fr.epita.assistant.jws.domain.service.GameService;
import fr.epita.assistant.jws.presentation.rest.request.CreateGameDTO;
import fr.epita.assistant.jws.presentation.rest.request.DropBombDTO;
import fr.epita.assistant.jws.presentation.rest.request.MovePlayerDTO;
import fr.epita.assistant.jws.presentation.rest.response.GameResponseDTO;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("/games")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameResource {

    @Inject GameService service;
    @Inject PlayerEntityToGameDetailResponseDTO converter1;
    @Inject GameModelToGameEntity gameModelToGameEntity;
    @Inject GameRepository gameRepository;

    @GET
    @Transactional
    public List<GameResponseDTO> getAll() {
        var todos = service.getAll();
        return todos.stream()
                .map(toto -> new GameResponseDTO(
                        toto.id,
                        toto.players.size(),
                        toto.state
                )).collect(Collectors.toList());
    }

    @GET
    @Transactional
    @Path("/{id}")
    public Response getByID(@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(400).build();
        }
        var gameByID = service.getByID(id);
        if (gameByID == null) {
            return Response.status(404).build();
        }
        return Response.ok(converter1.convert(gameByID)).build();
    }

    @POST
    public Response createGame(CreateGameDTO request) {

        if (request == null || request.name == null) {
            return Response.status(400).build();
        }

        var gameEntity = service.createGame(request.name);
        var gameEntityDTO = converter1.convert(gameEntity);

        return Response.ok(gameEntityDTO).build();
    }

    @POST
    @Path("/{id}")
    @Transactional
    public Response addPlayerToGameById(@PathParam("id") Long id, CreateGameDTO request) {

        if (request == null || request.name == null) {
            return Response.status(400).build();
        }

        var gameById = service.getModelByID(id);
        if (gameById == null) {
            return Response.status(404).build();
        }
        if (gameById.players.size() >= 4 || gameById.state.equals("RUNNING") || gameById.state.equals("FINISHED")) {
            return Response.status(400).build();
        }
        List<Integer> temp = getPlayerPos(gameById.players.size());
        var playerModel = new PlayerModel()
                .withName(request.name)
                .withPosX(temp.get(0))
                .withPosY(temp.get(1))
                .withLives(3);
        playerModel.game_id = gameById;
        gameById.players.add(playerModel);
        gameRepository.persistAndFlush(gameById);
        var gameByIdEntity = gameModelToGameEntity.convert(gameById);
        return Response.ok(converter1.convert(gameByIdEntity)).build();
    }

    private List<Integer> getPlayerPos(int nbPlayer) {
        List<Integer> newL = new ArrayList<>();
        if (nbPlayer == 0) {
            newL.add(1);
            newL.add(1);
        }
        else if (nbPlayer == 1) {
            newL.add(15);
            newL.add(1);
        }
        else if (nbPlayer == 2) {
            newL.add(15);
            newL.add(13);
        }
        else if (nbPlayer == 3) {
            newL.add(1);
            newL.add(13);
        }
        return newL;
    }

    @PATCH
    @Path("/{id}/start")
    public Response startGame (@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(404).build();
        }

        var gameById = service.getModelByID(id);
        if (gameById == null) {
            return Response.status(404).build();
        }

        var gameEntityById = service.startGameEntity(gameById);
        if (gameEntityById == null || gameEntityById.state.equals("404")) {
            return Response.status(404).build();
        }

        var gameEntityDTO = converter1.convert(gameEntityById);
        return Response.ok(gameEntityDTO).build();
    }
    
    
    @POST
    @Path("/{gameId}/players/{playerId}/move")
    @Transactional
    public Response movePlayer(@PathParam("gameId") Long id, @PathParam("playerId") Long playerId, MovePlayerDTO request) {
        if (id == null || playerId == null || request == null) {
            return Response.status(404).build();
        }
        var gameEntity = service.movePlayer(id, playerId, request.posX, request.posY);
        if (gameEntity.state.equals("400")) {
            return Response.status(400).build();
        }
        if (gameEntity.state.equals("404")) {
            return Response.status(404).build();
        }
        if (gameEntity.state.equals("429")) {
            return Response.status(429).build();
        }

        var gameEntityDTO = converter1.convert(gameEntity);
        return Response.ok(gameEntityDTO).build();
    }

    @POST
    @Path("/{gameId}/players/{playerId}/bomb")
    @Transactional
    public Response putBomb(@PathParam("gameId") Long gameId, @PathParam("playerId") Long playerId, DropBombDTO request) {
        if (request == null || playerId == null) {
            return Response.status(400).build();
        }
        if (gameId == null) {
            return Response.status(404).build();
        }

        var gameEntity = service.putBomb(gameId, playerId, request.posX, request.posY);
        if (gameEntity.state.equals("404")) {
            return Response.status(404).build();
        }
        if (gameEntity.state.equals("429")) {
            return Response.status(429).build();
        }
        if (gameEntity.state.equals("400")) {
            return Response.status(400).build();
        }
        var gameEntityDTO = converter1.convert(gameEntity);
        return Response.ok(gameEntityDTO).build();
    }
}
