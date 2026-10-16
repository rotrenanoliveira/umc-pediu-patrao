package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Entregador;
import com.umc.pediupatrao.service.EntregadorService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/entregadores")
public class EntregadorController {

    private final EntregadorService entregadorService;

    public EntregadorController(EntregadorService entregadorService) {
        this.entregadorService = entregadorService;
    }

    @GetMapping
    public List<Entregador> listar() {
        return entregadorService.listar();
    }

    @PostMapping
    public ResponseEntity<Entregador> salvar(@RequestBody Entregador entregador) {
        return ResponseEntity.status(HttpStatus.CREATED).body(entregadorService.salvar(entregador));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Entregador> atualizar(@PathVariable String id, @RequestBody Entregador entregador) {
        entregador.setId(id);
        return ResponseEntity.ok(entregadorService.salvar(entregador));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        entregadorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
