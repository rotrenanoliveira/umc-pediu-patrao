package com.umc.pediupatrao.service;

import com.umc.pediupatrao.dto.CriarPedidoRequest;
import com.umc.pediupatrao.entity.AlteracaoAuditoria;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.StatusPedido;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PedidoService {

    private PedidoRepository pedidoRepository;
    private ProdutoRepository produtoRepository;
    private UsuarioAutenticadoService usuarioAtual;
    private AuditoriaService auditoriaService;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository,
            UsuarioAutenticadoService usuarioAtual, AuditoriaService auditoriaService) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioAtual = usuarioAtual;
        this.auditoriaService = auditoriaService;
    }

    public Pedido criarPedido(CriarPedidoRequest request) {
        Usuario usuario = usuarioAtual.atual();
        Pedido pedido = new Pedido();
        pedido.setClienteId(request.clienteId());
        List<ItemPedido> itens = request.itens().stream().map(item -> criarItem(item.produtoId(), item.quantidade())).toList();
        BigDecimal subtotal = itens.stream().map(ItemPedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setItens(itens);
        pedido.setSubtotal(subtotal);
        pedido.setValorTotal(subtotal);
        pedido.setStatus(StatusPedido.RECEBIDO);
        pedido.setResponsavelEntrada(usuario.getUsername());
        pedido.setDataHoraEntrada(Instant.now());
        Pedido salvo = pedidoRepository.save(pedido);
        auditoriaService.registrar("INCLUSAO_PEDIDO", "PEDIDO", salvo.getId(), mudanca("status", null, salvo.getStatus()), null);
        return salvo;
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    public Pedido atualizarStatus(String id, StatusPedido destino) {
        Pedido pedido = obter(id);
        StatusPedido origem = pedido.getStatus();
        if (!transicaoPermitida(origem, destino)) throw new IllegalStateException("Transição de pedido inválida.");
        pedido.setStatus(destino);
        if (destino == StatusPedido.SAIU_PARA_ENTREGA || destino == StatusPedido.RETIRADO) {
            pedido.setResponsavelSaida(usuarioAtual.atual().getUsername());
            pedido.setDataHoraSaida(Instant.now());
        }
        Pedido salvo = pedidoRepository.save(pedido);
        auditoriaService.registrar("MUDANCA_STATUS_PEDIDO", "PEDIDO", id, mudanca("status", origem, destino), null);
        return salvo;
    }

    public Pedido aplicarDesconto(String id, BigDecimal percentual) {
        usuarioAtual.exigirGerente();
        if (percentual == null || percentual.compareTo(BigDecimal.ZERO) <= 0
                || percentual.compareTo(BigDecimal.valueOf(20)) > 0) {
            throw new IllegalArgumentException("O desconto deve ser maior que zero e limitado a 20%.");
        }
        Pedido pedido = obter(id);
        if (pedido.getStatus() == StatusPedido.SAIU_PARA_ENTREGA || pedido.getStatus() == StatusPedido.RETIRADO
                || pedido.getStatus() == StatusPedido.FINALIZADO || pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new IllegalStateException("Desconto não permitido neste status.");
        }
        BigDecimal anterior = pedido.getPercentualDesconto();
        BigDecimal valor = pedido.getSubtotal().multiply(percentual).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        pedido.setPercentualDesconto(percentual);
        pedido.setValorDesconto(valor);
        pedido.setValorTotal(pedido.getSubtotal().subtract(valor));
        Pedido salvo = pedidoRepository.save(pedido);
        auditoriaService.registrar("DESCONTO_PEDIDO", "PEDIDO", id, mudanca("percentualDesconto", anterior, percentual), null);
        return salvo;
    }

    public Pedido cancelar(String id, String justificativa) {
        usuarioAtual.exigirGerente();
        Pedido pedido = obter(id);
        if (!(pedido.getStatus() == StatusPedido.RECEBIDO || pedido.getStatus() == StatusPedido.EM_PREPARACAO
                || pedido.getStatus() == StatusPedido.PRONTO)) throw new IllegalStateException("Cancelamento não permitido neste status.");
        StatusPedido anterior = pedido.getStatus();
        pedido.setStatus(StatusPedido.CANCELADO);
        pedido.setJustificativaCancelamento(justificativa);
        Pedido salvo = pedidoRepository.save(pedido);
        auditoriaService.registrar("CANCELAMENTO_PEDIDO", "PEDIDO", id, mudanca("status", anterior, StatusPedido.CANCELADO), justificativa);
        return salvo;
    }

    private ItemPedido criarItem(String produtoId, int quantidade) {
        Produto produto = produtoRepository.findById(produtoId).filter(p -> Boolean.TRUE.equals(p.getAtivo()))
                .orElseThrow(() -> new IllegalArgumentException("Produto inexistente ou inativo."));
        if (produto.getPreco() == null) throw new IllegalArgumentException("Produto sem preço.");
        ItemPedido item = new ItemPedido();
        item.setProdutoId(produto.getId()); item.setNomeProduto(produto.getNome()); item.setQuantidade(quantidade);
        item.setPrecoUnitario(BigDecimal.valueOf(produto.getPreco()).setScale(2, RoundingMode.HALF_UP));
        item.setSubtotal(item.getPrecoUnitario().multiply(BigDecimal.valueOf(quantidade)));
        return item;
    }
    private Pedido obter(String id) { return pedidoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado.")); }
    private boolean transicaoPermitida(StatusPedido origem, StatusPedido destino) {
        return (origem == StatusPedido.RECEBIDO && destino == StatusPedido.EM_PREPARACAO)
                || (origem == StatusPedido.EM_PREPARACAO && destino == StatusPedido.PRONTO)
                || (origem == StatusPedido.PRONTO && (destino == StatusPedido.SAIU_PARA_ENTREGA || destino == StatusPedido.RETIRADO))
                || ((origem == StatusPedido.SAIU_PARA_ENTREGA || origem == StatusPedido.RETIRADO) && destino == StatusPedido.FINALIZADO);
    }
    private LinkedHashMap<String, AlteracaoAuditoria> mudanca(String campo, Object anterior, Object novo) {
        LinkedHashMap<String, AlteracaoAuditoria> resultado = new LinkedHashMap<>();
        resultado.put(campo, new AlteracaoAuditoria(anterior, novo));
        return resultado;
    }
}
