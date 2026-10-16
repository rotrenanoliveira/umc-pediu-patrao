package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Funcionario;
import com.umc.pediupatrao.repository.FuncionarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    public List<Funcionario> listar() {
        return funcionarioRepository.findAll();
    }

    public Optional<Funcionario> buscarPorId(String id) {
        return funcionarioRepository.findById(id);
    }

    public Funcionario salvar(Funcionario funcionario) {
        if (funcionario.getAtivo() == null) {
            funcionario.setAtivo(true);
        }
        if (funcionario.getId() != null && !funcionarioRepository.existsById(funcionario.getId())) {
            throw new IllegalArgumentException("Funcionário não encontrado para atualização.");
        }
        return funcionarioRepository.save(funcionario);
    }

    public void excluir(String id) {
        funcionarioRepository.deleteById(id);
    }
}
