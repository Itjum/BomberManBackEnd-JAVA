package fr.epita.assistant.jws.data.repository;

import fr.epita.assistant.jws.data.model.PlayerModel;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PlayerRepository implements PanacheRepositoryBase<PlayerModel, Long> {
}
