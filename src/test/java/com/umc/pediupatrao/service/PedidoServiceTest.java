package com.umc.pediupatrao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.umc.pediupatrao.dto.CriarPedidoRequest;
import com.umc.pediupatrao.dto.ItemPedidoRequest;
import com.umc.pediupatrao.entity.PerfilUsuario;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import com.umc.pediupatrao.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class PedidoServiceTest {
    @AfterEach void limparContexto() { SecurityContextHolder.clearContext(); }

    @Test
    void calculaTotalComPrecoPersistido() {
        UsuarioRepository usuarios = Mockito.mock(UsuarioRepository.class);
        ProdutoRepository produtos = Mockito.mock(ProdutoRepository.class);
        PedidoRepository pedidos = Mockito.mock(PedidoRepository.class);
        AuditoriaService auditoria = Mockito.mock(AuditoriaService.class);
        Usuario atendente = new Usuario();
        atendente.setUsername("atendente");
        atendente.setPerfil(PerfilUsuario.ATENDENTE);
        when(usuarios.findByUsername("atendente")).thenReturn(Optional.of(atendente));
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("atendente", "n/a", "ROLE_ATENDENTE"));
        Produto produto = new Produto();
        produto.setId("p1"); produto.setNome("Pizza"); produto.setPreco(42.50); produto.setAtivo(true);
        when(produtos.findById("p1")).thenReturn(Optional.of(produto));
        when(pedidos.save(any(Pedido.class))).thenAnswer(invocacao -> {
            Pedido pedido = invocacao.getArgument(0); pedido.setId("pedido1"); return pedido;
        });
        PedidoService service = new PedidoService(pedidos, produtos, new UsuarioAutenticadoService(usuarios), auditoria);

        Pedido pedido = service.criarPedido(new CriarPedidoRequest("cliente1", List.of(new ItemPedidoRequest("p1", 2))));

        assertEquals(new BigDecimal("85.00"), pedido.getValorTotal());
        assertEquals("atendente", pedido.getResponsavelEntrada());
    }
}
