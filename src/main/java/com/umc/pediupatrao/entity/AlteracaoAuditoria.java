package com.umc.pediupatrao.entity;

public class AlteracaoAuditoria {
    private Object anterior;
    private Object novo;
    public AlteracaoAuditoria() { }
    public AlteracaoAuditoria(Object anterior, Object novo) {
        this.anterior = anterior;
        this.novo = novo;
    }
    public Object getAnterior() { return anterior; }
    public void setAnterior(Object anterior) { this.anterior = anterior; }
    public Object getNovo() { return novo; }
    public void setNovo(Object novo) { this.novo = novo; }
}
