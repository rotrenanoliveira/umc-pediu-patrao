package com.umc.pediupatrao.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.pediupatrao.config.GlobalModelAdvice;
import com.umc.pediupatrao.config.SecurityConfig;
import com.umc.pediupatrao.dto.dashboard.DashboardResumoResponse;
import com.umc.pediupatrao.service.DashboardService;
import com.umc.pediupatrao.service.UsuarioAutenticadoService;
import com.umc.pediupatrao.service.UsuarioDetailsService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = DashboardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalModelAdvice.class))
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class DashboardSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @MockitoBean
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Test
    void adminPodeAcessarTodasAsRotas() throws Exception {
        stubServico();

        mockMvc.perform(get("/api/dashboard/resumo").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/dashboard/vendas-por-periodo").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/dashboard/pedidos-por-status").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/dashboard/produtos-mais-vendidos").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void gerentePodeAcessarTodasAsRotas() throws Exception {
        stubServico();

        mockMvc.perform(get("/api/dashboard/resumo").with(user("gerente").roles("GERENTE")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/dashboard/vendas-por-periodo").with(user("gerente").roles("GERENTE")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/dashboard/pedidos-por-status").with(user("gerente").roles("GERENTE")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/dashboard/produtos-mais-vendidos").with(user("gerente").roles("GERENTE")))
                .andExpect(status().isOk());
    }

    @Test
    void atendenteNaoAcessaDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard/resumo")
                        .with(user("atendente").roles("ATENDENTE"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/dashboard/vendas-por-periodo")
                        .with(user("atendente").roles("ATENDENTE"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/dashboard/pedidos-por-status")
                        .with(user("atendente").roles("ATENDENTE"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/dashboard/produtos-mais-vendidos")
                        .with(user("atendente").roles("ATENDENTE"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void semAutenticacaoRedirecionaParaLogin() throws Exception {
        mockMvc.perform(get("/api/dashboard/resumo").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        mockMvc.perform(get("/api/dashboard/vendas-por-periodo").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        mockMvc.perform(get("/api/dashboard/pedidos-por-status").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        mockMvc.perform(get("/api/dashboard/produtos-mais-vendidos").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    private void stubServico() {
        when(dashboardService.obterResumo(any(), any(), any()))
                .thenReturn(new DashboardResumoResponse(
                        BigDecimal.ZERO, 0, BigDecimal.ZERO, 0, BigDecimal.ZERO));
        when(dashboardService.vendasPorPeriodo(any(), any(), any())).thenReturn(List.of());
        when(dashboardService.pedidosPorStatus(any(), any(), any())).thenReturn(List.of());
        when(dashboardService.produtosMaisVendidos(any(), any(), any(), isNull())).thenReturn(List.of());
    }
}
