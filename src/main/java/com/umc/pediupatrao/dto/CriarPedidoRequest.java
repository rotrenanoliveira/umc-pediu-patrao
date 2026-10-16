package com.umc.pediupatrao.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CriarPedidoRequest(
        @NotBlank String clienteId,
        @NotEmpty List<@Valid ItemPedidoRequest> itens) { }
