package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Entregador;
import com.umc.pediupatrao.repository.EntregadorRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class EntregadorService {

    private final EntregadorRepository entregadorRepository;

    public EntregadorService(EntregadorRepository entregadorRepository) {
        this.entregadorRepository = entregadorRepository;
    }

    public List<Entregador> listar() {
        return entregadorRepository.findAll();
    }

    public Optional<Entregador> buscarPorId(String id) {
        return entregadorRepository.findById(id);
    }

    public Entregador salvar(Entregador entregador) {
        if (entregador.getAtivo() == null) {
            entregador.setAtivo(true);
        }
        if (entregador.getId() != null && !entregadorRepository.existsById(entregador.getId())) {
            throw new IllegalArgumentException("Entregador não encontrado para atualização.");
        }
        return entregadorRepository.save(entregador);
    }

    public void excluir(String id) {
        entregadorRepository.deleteById(id);
    }
}
