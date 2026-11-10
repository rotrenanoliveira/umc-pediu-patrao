package com.umc.pediupatrao.dto.dashboard;

import java.math.BigDecimal;

public record DashboardResumoResponse(
        BigDecimal totalVendas,
        long quantidadePedidos,
        BigDecimal ticketMedio,
        long quantidadeCancelados,
        BigDecimal taxaCancelamento) {
}
