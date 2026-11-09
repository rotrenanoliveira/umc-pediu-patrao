package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.PerfilUsuario;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.StatusPedido;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import com.umc.pediupatrao.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer {

    static final String SEED_DASHBOARD = "seed-dashboard";
    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarios, ProdutoRepository produtos,
            ClienteRepository clientes, PedidoRepository pedidos) {
        return args -> {
            BCryptPasswordEncoder encoder = encoder();
            garantirUsuario(usuarios, encoder, "admin", PerfilUsuario.ADMIN);
            garantirUsuario(usuarios, encoder, "gerente", PerfilUsuario.GERENTE);
            garantirUsuario(usuarios, encoder, "atendente", PerfilUsuario.ATENDENTE);
            semearDashboard(produtos, clientes, pedidos);
        };
    }

    private void semearDashboard(ProdutoRepository produtos, ClienteRepository clientes, PedidoRepository pedidos) {
        List<Produto> cardapio = garantirProdutos(produtos);
        List<Cliente> pessoas = garantirClientes(clientes);

        pedidos.deleteByResponsavelEntrada(SEED_DASHBOARD);

        LocalDate hoje = LocalDate.now(ZONE);
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        int dias = hoje.getDayOfMonth();
        StatusPedido[] status = StatusPedido.values();

        int criados = 0;
        for (int i = 0; i < status.length; i++) {
            Cliente cliente = pessoas.get(i % pessoas.size());
            List<ItemPedido> itens = List.of(
                    item(cardapio.get(i % cardapio.size()), 2),
                    item(cardapio.get((i + 2) % cardapio.size()), 1));
            pedidos.save(montarPedido(cliente, itens, status[i], hoje, 12 + (i % 8), 10 + i, false));
            criados++;
        }

        for (int dia = 0; dia < dias; dia++) {
            LocalDate data = inicioMes.plusDays(dia);
            int pedidosNoDia = 2 + (dia % 2);
            for (int n = 0; n < pedidosNoDia; n++) {
                StatusPedido situacao = status[(dia + n) % status.length];
                Cliente cliente = pessoas.get((dia + n) % pessoas.size());
                int produtoBase = (dia * 3 + n) % cardapio.size();
                List<ItemPedido> itens = new ArrayList<>();
                itens.add(item(cardapio.get(produtoBase), 1 + ((dia + n) % 3)));
                itens.add(item(cardapio.get((produtoBase + 1) % cardapio.size()), 1 + (n % 2)));
                if ((dia + n) % 4 == 0) {
                    itens.add(item(cardapio.get((produtoBase + 3) % cardapio.size()), 1));
                }
                boolean comDesconto = situacao != StatusPedido.CANCELADO && (dia + n) % 5 == 0;
                int hora = 11 + ((dia + n * 3) % 10);
                int minuto = (n * 17) % 60;
                pedidos.save(montarPedido(cliente, itens, situacao, data, hora, minuto, comDesconto));
                criados++;
            }
        }
        System.out.println("Seed do dashboard: " + criados
                + " pedidos entre " + inicioMes + " e " + hoje + " (todos os status).");
    }

    private Pedido montarPedido(Cliente cliente, List<ItemPedido> itens, StatusPedido status,
            LocalDate data, int hora, int minuto, boolean comDesconto) {
        BigDecimal subtotal = itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        Pedido pedido = new Pedido();
        pedido.setClienteId(cliente.getId());
        pedido.setItens(itens);
        pedido.setSubtotal(subtotal);
        pedido.setStatus(status);
        pedido.setResponsavelEntrada(SEED_DASHBOARD);
        pedido.setDataHoraEntrada(data.atTime(LocalTime.of(hora, minuto)).atZone(ZONE).toInstant());

        if (comDesconto) {
            BigDecimal percentual = BigDecimal.TEN;
            BigDecimal desconto = subtotal.multiply(percentual)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            pedido.setPercentualDesconto(percentual);
            pedido.setValorDesconto(desconto);
            pedido.setValorTotal(subtotal.subtract(desconto));
        } else {
            pedido.setPercentualDesconto(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            pedido.setValorDesconto(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            pedido.setValorTotal(subtotal);
        }

        if (status == StatusPedido.SAIU_PARA_ENTREGA
                || status == StatusPedido.RETIRADO
                || status == StatusPedido.FINALIZADO) {
            pedido.setResponsavelSaida("atendente");
            pedido.setDataHoraSaida(data.atTime(LocalTime.of(Math.min(hora + 1, 22), minuto)).atZone(ZONE).toInstant());
        }
        if (status == StatusPedido.CANCELADO) {
            pedido.setJustificativaCancelamento("Cliente desistiu do pedido (dado de demonstração).");
        }
        return pedido;
    }

    private ItemPedido item(Produto produto, int quantidade) {
        BigDecimal preco = BigDecimal.valueOf(produto.getPreco()).setScale(2, RoundingMode.HALF_UP);
        ItemPedido item = new ItemPedido();
        item.setProdutoId(produto.getId());
        item.setNomeProduto(produto.getNome());
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(preco);
        item.setSubtotal(preco.multiply(BigDecimal.valueOf(quantidade)).setScale(2, RoundingMode.HALF_UP));
        return item;
    }

    private List<Produto> garantirProdutos(ProdutoRepository repo) {
        ProdutoSeed[] cardapio = {
                new ProdutoSeed("Pizza Calabresa", "Pizza", 49.90),
                new ProdutoSeed("Pizza Mussarela", "Pizza", 45.00),
                new ProdutoSeed("Pizza Portuguesa", "Pizza", 52.90),
                new ProdutoSeed("Pizza Frango com Catupiry", "Pizza", 51.50),
                new ProdutoSeed("Esfiha de Carne", "Esfiha", 8.00),
                new ProdutoSeed("Esfiha de Queijo", "Esfiha", 7.50),
                new ProdutoSeed("Refrigerante 2L", "Bebida", 12.00),
                new ProdutoSeed("Suco Natural", "Bebida", 9.00)
        };
        List<Produto> existentes = repo.findAll();
        List<Produto> resultado = new ArrayList<>();
        for (ProdutoSeed seed : cardapio) {
            Produto produto = existentes.stream()
                    .filter(p -> seed.nome().equalsIgnoreCase(p.getNome()))
                    .findFirst()
                    .orElseGet(() -> {
                        Produto novo = new Produto();
                        novo.setNome(seed.nome());
                        novo.setTipo(seed.tipo());
                        novo.setPreco(seed.preco());
                        novo.setAtivo(true);
                        Produto salvo = repo.save(novo);
                        System.out.println("Produto '" + seed.nome() + "' criado para o dashboard.");
                        return salvo;
                    });
            if (produto.getPreco() == null || produto.getAtivo() == null) {
                if (produto.getPreco() == null) {
                    produto.setPreco(seed.preco());
                }
                if (produto.getAtivo() == null) {
                    produto.setAtivo(true);
                }
                produto = repo.save(produto);
            }
            resultado.add(produto);
        }
        return resultado;
    }

    private List<Cliente> garantirClientes(ClienteRepository repo) {
        ClienteSeed[] pessoas = {
                new ClienteSeed("Ana Souza", "11900001001", "01310-100", "Av. Paulista", "1000", "Bela Vista", "São Paulo", "SP"),
                new ClienteSeed("Bruno Lima", "11900001002", "04001-000", "Rua Vergueiro", "250", "Liberdade", "São Paulo", "SP"),
                new ClienteSeed("Carla Mendes", "11900001003", "05422-000", "Rua Harmonia", "88", "Vila Madalena", "São Paulo", "SP"),
                new ClienteSeed("Diego Alves", "11900001004", "03031-000", "Rua da Mooca", "420", "Mooca", "São Paulo", "SP")
        };
        List<Cliente> resultado = new ArrayList<>();
        for (ClienteSeed seed : pessoas) {
            Cliente cliente = repo.findByTelefone(seed.telefone()).stream().findFirst().orElseGet(() -> {
                Cliente novo = new Cliente();
                novo.setNome(seed.nome());
                novo.setTelefone(seed.telefone());
                novo.setCep(seed.cep());
                novo.setLogradouro(seed.logradouro());
                novo.setNumero(seed.numero());
                novo.setBairro(seed.bairro());
                novo.setCidade(seed.cidade());
                novo.setEstado(seed.estado());
                Cliente salvo = repo.save(novo);
                System.out.println("Cliente '" + seed.nome() + "' criado para o dashboard.");
                return salvo;
            });
            resultado.add(cliente);
        }
        return resultado;
    }

    private BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    private void garantirUsuario(UsuarioRepository repo, BCryptPasswordEncoder encoder,
            String username, PerfilUsuario perfil) {
        repo.findByUsername(username).ifPresentOrElse(usuario -> {
            if (usuario.getPerfil() != perfil) {
                usuario.setPerfil(perfil);
                repo.save(usuario);
                System.out.println("Perfil de '" + username + "' corrigido para " + perfil);
            }
        }, () -> {
            Usuario usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setPassword(encoder.encode("teste123"));
            usuario.setPerfil(perfil);
            repo.save(usuario);
            System.out.println("Usuário '" + username + "' criado com perfil " + perfil);
        });
    }

    private record ProdutoSeed(String nome, String tipo, double preco) {
    }

    private record ClienteSeed(String nome, String telefone, String cep, String logradouro,
            String numero, String bairro, String cidade, String estado) {
    }
}
