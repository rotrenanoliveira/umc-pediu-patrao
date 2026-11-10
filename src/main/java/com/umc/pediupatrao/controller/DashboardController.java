package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.dto.dashboard.DashboardResumoResponse;
import com.umc.pediupatrao.dto.dashboard.PedidosPorStatusResponse;
import com.umc.pediupatrao.dto.dashboard.ProdutoMaisVendidoResponse;
import com.umc.pediupatrao.dto.dashboard.VendaPeriodoResponse;
import com.umc.pediupatrao.entity.StatusPedido;
import com.umc.pediupatrao.service.DashboardService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumo")
    public DashboardResumoResponse resumo(
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @RequestParam(required = false) StatusPedido status) {
        return dashboardService.obterResumo(dataInicio, dataFim, status);
    }

    @GetMapping("/vendas-por-periodo")
    public List<VendaPeriodoResponse> vendasPorPeriodo(
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @RequestParam(required = false) StatusPedido status) {
        return dashboardService.vendasPorPeriodo(dataInicio, dataFim, status);
    }

    @GetMapping("/pedidos-por-status")
    public List<PedidosPorStatusResponse> pedidosPorStatus(
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @RequestParam(required = false) StatusPedido status) {
        return dashboardService.pedidosPorStatus(dataInicio, dataFim, status);
    }

    @GetMapping("/produtos-mais-vendidos")
    public List<ProdutoMaisVendidoResponse> produtosMaisVendidos(
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(required = false) Integer limite) {
        return dashboardService.produtosMaisVendidos(dataInicio, dataFim, status, limite);
    }
}
