package com.umc.pediupatrao.dto;

import com.umc.pediupatrao.entity.HorarioAtendimento;
import java.util.ArrayList;
import java.util.List;

public class HorariosForm {

    private List<HorarioAtendimento> horarios = new ArrayList<>();

    public List<HorarioAtendimento> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<HorarioAtendimento> horarios) {
        this.horarios = horarios;
    }
}
