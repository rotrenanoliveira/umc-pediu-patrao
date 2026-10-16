package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.HorarioAtendimento;
import com.umc.pediupatrao.service.HorarioAtendimentoService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/horarios")
public class HorarioAtendimentoController {

    private final HorarioAtendimentoService horarioService;

    public HorarioAtendimentoController(HorarioAtendimentoService horarioService) {
        this.horarioService = horarioService;
    }

    @GetMapping
    public List<HorarioAtendimento> listar() {
        return horarioService.listarOuInicializar();
    }

    @PutMapping
    public ResponseEntity<List<HorarioAtendimento>> salvarTodos(@RequestBody List<HorarioAtendimento> horarios) {
        return ResponseEntity.ok(horarioService.salvarTodos(horarios));
    }
}
