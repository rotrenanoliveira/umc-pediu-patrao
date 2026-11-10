package com.umc.pediupatrao.dto.dashboard;

import java.math.BigDecimal;

public record ProdutoMaisVendidoResponse(
        String produtoId,
        String nomeProduto,
        long quantidadeVendida,
        BigDecimal valorTotal) {
}
