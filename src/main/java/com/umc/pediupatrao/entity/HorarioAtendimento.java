package com.umc.pediupatrao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "horariosAtendimento")
public class HorarioAtendimento {

    @Id
    private String id;
    private String diaSemana;
    private Integer ordem;
    private String horaAbertura;
    private String horaFechamento;
    private Boolean fechado = false;

    public HorarioAtendimento() {
    }

    public HorarioAtendimento(String diaSemana, Integer ordem, String horaAbertura, String horaFechamento, Boolean fechado) {
        this.diaSemana = diaSemana;
        this.ordem = ordem;
        this.horaAbertura = horaAbertura;
        this.horaFechamento = horaFechamento;
        this.fechado = fechado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public String getHoraAbertura() {
        return horaAbertura;
    }

    public void setHoraAbertura(String horaAbertura) {
        this.horaAbertura = horaAbertura;
    }

    public String getHoraFechamento() {
        return horaFechamento;
    }

    public void setHoraFechamento(String horaFechamento) {
        this.horaFechamento = horaFechamento;
    }

    public Boolean getFechado() {
        return fechado;
    }

    public void setFechado(Boolean fechado) {
        this.fechado = fechado;
    }
}
