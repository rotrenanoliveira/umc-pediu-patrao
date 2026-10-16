package com.umc.pediupatrao.entity;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "auditorias")
public class Auditoria {
    @Id
    private String id;
    private String usuario;
    private PerfilUsuario perfil;
    private Instant dataHora;
    private String operacao;
    private String entidade;
    private String entidadeId;
    private Map<String, AlteracaoAuditoria> alteracoes = new LinkedHashMap<>();
    private String justificativa;
    public String getId() { return id; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public PerfilUsuario getPerfil() { return perfil; }
    public void setPerfil(PerfilUsuario perfil) { this.perfil = perfil; }
    public Instant getDataHora() { return dataHora; }
    public void setDataHora(Instant dataHora) { this.dataHora = dataHora; }
    public String getOperacao() { return operacao; }
    public void setOperacao(String operacao) { this.operacao = operacao; }
    public String getEntidade() { return entidade; }
    public void setEntidade(String entidade) { this.entidade = entidade; }
    public String getEntidadeId() { return entidadeId; }
    public void setEntidadeId(String entidadeId) { this.entidadeId = entidadeId; }
    public Map<String, AlteracaoAuditoria> getAlteracoes() { return alteracoes; }
    public void setAlteracoes(Map<String, AlteracaoAuditoria> alteracoes) { this.alteracoes = alteracoes; }
    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
}
