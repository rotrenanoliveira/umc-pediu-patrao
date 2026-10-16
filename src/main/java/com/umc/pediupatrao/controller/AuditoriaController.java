package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.service.AuditoriaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auditorias")
public class AuditoriaController {
    private final AuditoriaService auditoriaService;
    public AuditoriaController(AuditoriaService auditoriaService) { this.auditoriaService = auditoriaService; }
    @GetMapping
    public List<Auditoria> listar(@RequestParam(required = false) String entidade) {
        return auditoriaService.listar(entidade);
    }
}
