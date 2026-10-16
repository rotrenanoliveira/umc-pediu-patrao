package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.AlteracaoAuditoria;
import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.AuditoriaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {
    private final AuditoriaRepository repository;
    private final UsuarioAutenticadoService usuarioAtual;
    public AuditoriaService(AuditoriaRepository repository, UsuarioAutenticadoService usuarioAtual) {
        this.repository = repository;
        this.usuarioAtual = usuarioAtual;
    }
    public void registrar(String operacao, String entidade, String entidadeId,
            Map<String, AlteracaoAuditoria> alteracoes, String justificativa) {
        Usuario usuario = usuarioAtual.atual();
        Auditoria evento = new Auditoria();
        evento.setUsuario(usuario.getUsername());
        evento.setPerfil(usuario.getPerfil());
        evento.setDataHora(Instant.now());
        evento.setOperacao(operacao);
        evento.setEntidade(entidade);
        evento.setEntidadeId(entidadeId);
        evento.setAlteracoes(alteracoes);
        evento.setJustificativa(justificativa);
        repository.save(evento);
    }
    public List<Auditoria> listar(String entidade) {
        return entidade == null || entidade.isBlank()
                ? repository.findAll() : repository.findByEntidadeOrderByDataHoraDesc(entidade);
    }
}
