package com.umc.pediupatrao.dto.dashboard;

import java.math.BigDecimal;

public record VendaPeriodoResponse(
        String periodo,
        BigDecimal totalVendas,
        long quantidadePedidos) {
}
