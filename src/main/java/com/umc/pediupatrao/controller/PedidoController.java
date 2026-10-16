package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.dto.CancelamentoRequest;
import com.umc.pediupatrao.dto.CriarPedidoRequest;
import com.umc.pediupatrao.dto.DescontoRequest;
import com.umc.pediupatrao.dto.TransicaoPedidoRequest;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private PedidoService pedidoService;
    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public Pedido criarPedido(@Valid @RequestBody CriarPedidoRequest pedido) {
        return pedidoService.criarPedido(pedido);
    }

    @GetMapping
    public List<Pedido> listarPedidos() {
        return pedidoService.listarPedidos();
    }

    @PutMapping("/{id}/status")
    public Pedido atualizarStatus(@PathVariable String id, @Valid @RequestBody TransicaoPedidoRequest request) {
        return pedidoService.atualizarStatus(id, request.status());
    }
    @PutMapping("/{id}/desconto")
    public Pedido aplicarDesconto(@PathVariable String id, @Valid @RequestBody DescontoRequest request) {
        return pedidoService.aplicarDesconto(id, request.percentual());
    }
    @PutMapping("/{id}/cancelamento")
    public Pedido cancelar(@PathVariable String id, @Valid @RequestBody CancelamentoRequest request) {
        return pedidoService.cancelar(id, request.justificativa());
    }
    
    
}
