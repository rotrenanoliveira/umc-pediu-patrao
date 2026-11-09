package com.umc.pediupatrao.dto.dashboard;

public record PedidosPorStatusResponse(
        String status,
        long quantidade) {
}
