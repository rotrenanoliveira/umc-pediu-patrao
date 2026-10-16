package com.umc.pediupatrao.repository;

import com.umc.pediupatrao.entity.HorarioAtendimento;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HorarioAtendimentoRepository extends MongoRepository<HorarioAtendimento, String> {

    Optional<HorarioAtendimento> findByDiaSemana(String diaSemana);

    List<HorarioAtendimento> findAllByOrderByOrdemAsc();
}
