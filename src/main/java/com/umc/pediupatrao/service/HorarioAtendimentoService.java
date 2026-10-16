package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.HorarioAtendimento;
import com.umc.pediupatrao.repository.HorarioAtendimentoRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HorarioAtendimentoService {

    private static final String[][] DIAS = {
        {"DOMINGO", "Domingo"},
        {"SEGUNDA", "Segunda-feira"},
        {"TERCA", "Terça-feira"},
        {"QUARTA", "Quarta-feira"},
        {"QUINTA", "Quinta-feira"},
        {"SEXTA", "Sexta-feira"},
        {"SABADO", "Sábado"}
    };

    private final HorarioAtendimentoRepository horarioRepository;

    public HorarioAtendimentoService(HorarioAtendimentoRepository horarioRepository) {
        this.horarioRepository = horarioRepository;
    }

    public List<HorarioAtendimento> listarOuInicializar() {
        List<HorarioAtendimento> existentes = horarioRepository.findAllByOrderByOrdemAsc();
        if (!existentes.isEmpty()) {
            return existentes;
        }

        List<HorarioAtendimento> padrao = new ArrayList<>();
        for (int i = 0; i < DIAS.length; i++) {
            boolean domingo = i == 0;
            HorarioAtendimento horario = new HorarioAtendimento(
                    DIAS[i][0],
                    i,
                    domingo ? null : "18:00",
                    domingo ? null : "23:30",
                    domingo
            );
            padrao.add(horarioRepository.save(horario));
        }
        return padrao;
    }

    public List<HorarioAtendimento> salvarTodos(List<HorarioAtendimento> horarios) {
        List<HorarioAtendimento> salvos = new ArrayList<>();
        for (HorarioAtendimento horario : horarios) {
            if (Boolean.TRUE.equals(horario.getFechado())) {
                horario.setHoraAbertura(null);
                horario.setHoraFechamento(null);
            }
            salvos.add(horarioRepository.save(horario));
        }
        return salvos;
    }

    public String rotuloDia(String diaSemana) {
        for (String[] dia : DIAS) {
            if (dia[0].equals(diaSemana)) {
                return dia[1];
            }
        }
        return diaSemana;
    }
}
