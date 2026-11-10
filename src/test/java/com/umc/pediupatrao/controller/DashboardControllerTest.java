package com.umc.pediupatrao.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.pediupatrao.dto.dashboard.DashboardResumoResponse;
import com.umc.pediupatrao.dto.dashboard.PedidosPorStatusResponse;
import com.umc.pediupatrao.dto.dashboard.ProdutoMaisVendidoResponse;
import com.umc.pediupatrao.dto.dashboard.VendaPeriodoResponse;
import com.umc.pediupatrao.entity.StatusPedido;
import com.umc.pediupatrao.service.DashboardService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(dashboardService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void resumoRetornaIndicadores() throws Exception {
        when(dashboardService.obterResumo("2026-11-01", "2026-11-09", null))
                .thenReturn(new DashboardResumoResponse(
                        new BigDecimal("1250.50"),
                        18,
                        new BigDecimal("69.47"),
                        2,
                        new BigDecimal("10.00")));

        mockMvc.perform(get("/api/dashboard/resumo")
                        .param("dataInicio", "2026-11-01")
                        .param("dataFim", "2026-11-09")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVendas").value(1250.50))
                .andExpect(jsonPath("$.quantidadePedidos").value(18))
                .andExpect(jsonPath("$.ticketMedio").value(69.47))
                .andExpect(jsonPath("$.quantidadeCancelados").value(2))
                .andExpect(jsonPath("$.taxaCancelamento").value(10.00));

        verify(dashboardService).obterResumo("2026-11-01", "2026-11-09", null);
    }

    @Test
    void resumoComFiltroDeStatus() throws Exception {
        when(dashboardService.obterResumo("2026-11-01", "2026-11-09", StatusPedido.FINALIZADO))
                .thenReturn(new DashboardResumoResponse(
                        new BigDecimal("800.00"),
                        10,
                        new BigDecimal("80.00"),
                        0,
                        new BigDecimal("0.00")));

        mockMvc.perform(get("/api/dashboard/resumo")
                        .param("dataInicio", "2026-11-01")
                        .param("dataFim", "2026-11-09")
                        .param("status", "FINALIZADO")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadePedidos").value(10));

        verify(dashboardService).obterResumo("2026-11-01", "2026-11-09", StatusPedido.FINALIZADO);
    }

    @Test
    void resumoSemParametrosDelegaAoServico() throws Exception {
        when(dashboardService.obterResumo(null, null, null))
                .thenReturn(new DashboardResumoResponse(
                        BigDecimal.ZERO.setScale(2),
                        0,
                        BigDecimal.ZERO.setScale(2),
                        0,
                        BigDecimal.ZERO.setScale(2)));

        mockMvc.perform(get("/api/dashboard/resumo").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadePedidos").value(0));

        verify(dashboardService).obterResumo(null, null, null);
    }

    @Test
    void vendasPorPeriodoRetornaSerieDiaria() throws Exception {
        when(dashboardService.vendasPorPeriodo("2026-11-01", "2026-11-09", null))
                .thenReturn(List.of(
                        new VendaPeriodoResponse("2026-11-01", new BigDecimal("320.00"), 4),
                        new VendaPeriodoResponse("2026-11-03", new BigDecimal("510.50"), 7)));

        mockMvc.perform(get("/api/dashboard/vendas-por-periodo")
                        .param("dataInicio", "2026-11-01")
                        .param("dataFim", "2026-11-09")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].periodo").value("2026-11-01"))
                .andExpect(jsonPath("$[0].totalVendas").value(320.00))
                .andExpect(jsonPath("$[0].quantidadePedidos").value(4))
                .andExpect(jsonPath("$[1].periodo").value("2026-11-03"))
                .andExpect(jsonPath("$[1].totalVendas").value(510.50));

        verify(dashboardService).vendasPorPeriodo("2026-11-01", "2026-11-09", null);
    }

    @Test
    void vendasPorPeriodoSemDadosRetornaListaVazia() throws Exception {
        when(dashboardService.vendasPorPeriodo(isNull(), isNull(), isNull())).thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/vendas-por-periodo").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void pedidosPorStatusRetornaAgrupamento() throws Exception {
        when(dashboardService.pedidosPorStatus("2026-11-01", "2026-11-09", null))
                .thenReturn(List.of(
                        new PedidosPorStatusResponse("FINALIZADO", 10),
                        new PedidosPorStatusResponse("EM_PREPARACAO", 5),
                        new PedidosPorStatusResponse("CANCELADO", 2)));

        mockMvc.perform(get("/api/dashboard/pedidos-por-status")
                        .param("dataInicio", "2026-11-01")
                        .param("dataFim", "2026-11-09")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("FINALIZADO"))
                .andExpect(jsonPath("$[0].quantidade").value(10))
                .andExpect(jsonPath("$[2].status").value("CANCELADO"))
                .andExpect(jsonPath("$[2].quantidade").value(2));

        verify(dashboardService).pedidosPorStatus("2026-11-01", "2026-11-09", null);
    }

    @Test
    void pedidosPorStatusComFiltro() throws Exception {
        when(dashboardService.pedidosPorStatus("2026-11-01", "2026-11-09", StatusPedido.FINALIZADO))
                .thenReturn(List.of(new PedidosPorStatusResponse("FINALIZADO", 10)));

        mockMvc.perform(get("/api/dashboard/pedidos-por-status")
                        .param("dataInicio", "2026-11-01")
                        .param("dataFim", "2026-11-09")
                        .param("status", "FINALIZADO")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("FINALIZADO"));

        verify(dashboardService).pedidosPorStatus("2026-11-01", "2026-11-09", StatusPedido.FINALIZADO);
    }

    @Test
    void produtosMaisVendidosRetornaRanking() throws Exception {
        when(dashboardService.produtosMaisVendidos("2026-11-01", "2026-11-09", null, 5))
                .thenReturn(List.of(
                        new ProdutoMaisVendidoResponse(
                                "prod-1", "Pizza Calabresa", 24, new BigDecimal("960.00")),
                        new ProdutoMaisVendidoResponse(
                                "prod-2", "Refrigerante 2L", 18, new BigDecimal("180.00"))));

        mockMvc.perform(get("/api/dashboard/produtos-mais-vendidos")
                        .param("dataInicio", "2026-11-01")
                        .param("dataFim", "2026-11-09")
                        .param("limite", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].produtoId").value("prod-1"))
                .andExpect(jsonPath("$[0].nomeProduto").value("Pizza Calabresa"))
                .andExpect(jsonPath("$[0].quantidadeVendida").value(24))
                .andExpect(jsonPath("$[0].valorTotal").value(960.00))
                .andExpect(jsonPath("$[1].nomeProduto").value("Refrigerante 2L"));

        verify(dashboardService).produtosMaisVendidos("2026-11-01", "2026-11-09", null, 5);
    }

    @Test
    void produtosMaisVendidosSemLimiteUsaPadraoDoServico() throws Exception {
        when(dashboardService.produtosMaisVendidos(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/produtos-mais-vendidos").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(dashboardService).produtosMaisVendidos(null, null, null, null);
    }

    @Test
    void resumoComPeriodoInvalidoRetorna400() throws Exception {
        when(dashboardService.obterResumo("2026-11-10", "2026-11-01", null))
                .thenThrow(new IllegalArgumentException("dataInicio não pode ser posterior a dataFim."));

        mockMvc.perform(get("/api/dashboard/resumo")
                        .param("dataInicio", "2026-11-10")
                        .param("dataFim", "2026-11-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("dataInicio não pode ser posterior a dataFim."));
    }

    @Test
    void vendasPorPeriodoComDataInvalidaRetorna400() throws Exception {
        when(dashboardService.vendasPorPeriodo("01-11-2026", "2026-11-09", null))
                .thenThrow(new IllegalArgumentException("Datas devem estar no formato YYYY-MM-DD."));

        mockMvc.perform(get("/api/dashboard/vendas-por-periodo")
                        .param("dataInicio", "01-11-2026")
                        .param("dataFim", "2026-11-09")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Datas devem estar no formato YYYY-MM-DD."));
    }

    @Test
    void pedidosPorStatusComPeriodoInvalidoRetorna400() throws Exception {
        when(dashboardService.pedidosPorStatus("2026-11-10", "2026-11-01", null))
                .thenThrow(new IllegalArgumentException("dataInicio não pode ser posterior a dataFim."));

        mockMvc.perform(get("/api/dashboard/pedidos-por-status")
                        .param("dataInicio", "2026-11-10")
                        .param("dataFim", "2026-11-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("dataInicio não pode ser posterior a dataFim."));
    }

    @Test
    void produtosMaisVendidosComLimiteForaDoIntervaloRetorna400() throws Exception {
        when(dashboardService.produtosMaisVendidos(any(), any(), any(), eq(100)))
                .thenThrow(new IllegalArgumentException("limite deve estar entre 1 e 50."));

        mockMvc.perform(get("/api/dashboard/produtos-mais-vendidos")
                        .param("limite", "100")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("limite deve estar entre 1 e 50."));
    }

    @Test
    void statusInvalidoNaoChamaServicoERetornaErro() throws Exception {
        mockMvc.perform(get("/api/dashboard/pedidos-por-status")
                        .param("status", "INEXISTENTE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
