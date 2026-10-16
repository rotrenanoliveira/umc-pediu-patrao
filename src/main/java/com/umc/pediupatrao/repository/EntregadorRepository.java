package com.umc.pediupatrao.repository;

import com.umc.pediupatrao.entity.Entregador;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntregadorRepository extends MongoRepository<Entregador, String> {
}
