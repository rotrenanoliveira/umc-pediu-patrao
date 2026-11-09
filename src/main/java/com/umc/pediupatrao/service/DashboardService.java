package com.umc.pediupatrao.service;

import com.umc.pediupatrao.dto.dashboard.DashboardResumoResponse;
import com.umc.pediupatrao.dto.dashboard.PedidosPorStatusResponse;
import com.umc.pediupatrao.dto.dashboard.ProdutoMaisVendidoResponse;
import com.umc.pediupatrao.dto.dashboard.VendaPeriodoResponse;
import com.umc.pediupatrao.entity.StatusPedido;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final String COLLECTION = "pedidos";
    private static final int LIMITE_PADRAO = 10;
    private static final int LIMITE_MAXIMO = 50;

    private final MongoTemplate mongoTemplate;

    public DashboardService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public DashboardResumoResponse obterResumo(String dataInicio, String dataFim, StatusPedido status) {
        Periodo periodo = resolverPeriodo(dataInicio, dataFim);
        Criteria base = criteriosPeriodo(periodo, status);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(base),
                context -> new Document("$facet", new Document()
                        .append("vendas", Arrays.asList(
                                new Document("$match", new Document("status",
                                        new Document("$ne", StatusPedido.CANCELADO.name()))),
                                new Document("$group", new Document("_id", null)
                                        .append("totalVendas", new Document("$sum", "$valorTotal"))
                                        .append("quantidadePedidos", new Document("$sum", 1)))
                        ))
                        .append("cancelados", Arrays.asList(
                                new Document("$match", new Document("status", StatusPedido.CANCELADO.name())),
                                new Document("$group", new Document("_id", null)
                                        .append("quantidade", new Document("$sum", 1)))
                        ))
                        .append("total", Arrays.asList(
                                new Document("$count", "quantidade")
                        ))
                )
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, COLLECTION, Document.class);
        Document root = results.getUniqueMappedResult();
        if (root == null) {
            return resumoVazio();
        }

        BigDecimal totalVendas = BigDecimal.ZERO;
        long quantidadePedidos = 0;
        List<Document> vendas = root.getList("vendas", Document.class);
        if (vendas != null && !vendas.isEmpty()) {
            Document v = vendas.get(0);
            totalVendas = toBigDecimal(v.get("totalVendas"));
            quantidadePedidos = toLong(v.get("quantidadePedidos"));
        }

        long quantidadeCancelados = 0;
        List<Document> cancelados = root.getList("cancelados", Document.class);
        if (cancelados != null && !cancelados.isEmpty()) {
            quantidadeCancelados = toLong(cancelados.get(0).get("quantidade"));
        }

        long totalGeral = 0;
        List<Document> total = root.getList("total", Document.class);
        if (total != null && !total.isEmpty()) {
            totalGeral = toLong(total.get(0).get("quantidade"));
        }

        BigDecimal ticketMedio = quantidadePedidos == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : totalVendas.divide(BigDecimal.valueOf(quantidadePedidos), 2, RoundingMode.HALF_UP);

        BigDecimal taxaCancelamento = totalGeral == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(quantidadeCancelados)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalGeral), 2, RoundingMode.HALF_UP);

        return new DashboardResumoResponse(
                totalVendas.setScale(2, RoundingMode.HALF_UP),
                quantidadePedidos,
                ticketMedio,
                quantidadeCancelados,
                taxaCancelamento);
    }

    public List<VendaPeriodoResponse> vendasPorPeriodo(String dataInicio, String dataFim, StatusPedido status) {
        Periodo periodo = resolverPeriodo(dataInicio, dataFim);
        Criteria base = criteriosPeriodoSemCancelados(periodo, status);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(base),
                context -> new Document("$group", new Document("_id",
                        new Document("$dateToString", new Document()
                                .append("format", "%Y-%m-%d")
                                .append("date", "$dataHoraEntrada")
                                .append("timezone", ZONE.getId())))
                        .append("totalVendas", new Document("$sum", "$valorTotal"))
                        .append("quantidadePedidos", new Document("$sum", 1))),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "_id"))
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, COLLECTION, Document.class);
        List<VendaPeriodoResponse> lista = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            lista.add(new VendaPeriodoResponse(
                    String.valueOf(doc.get("_id")),
                    toBigDecimal(doc.get("totalVendas")).setScale(2, RoundingMode.HALF_UP),
                    toLong(doc.get("quantidadePedidos"))));
        }
        return lista;
    }

    public List<PedidosPorStatusResponse> pedidosPorStatus(String dataInicio, String dataFim, StatusPedido status) {
        Periodo periodo = resolverPeriodo(dataInicio, dataFim);
        Criteria base = criteriosPeriodo(periodo, status);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(base),
                Aggregation.group("status").count().as("quantidade"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "quantidade"))
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, COLLECTION, Document.class);
        List<PedidosPorStatusResponse> lista = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            Object id = doc.get("_id");
            lista.add(new PedidosPorStatusResponse(
                    id == null ? "DESCONHECIDO" : String.valueOf(id),
                    toLong(doc.get("quantidade"))));
        }
        return lista;
    }

    public List<ProdutoMaisVendidoResponse> produtosMaisVendidos(
            String dataInicio, String dataFim, StatusPedido status, Integer limite) {
        Periodo periodo = resolverPeriodo(dataInicio, dataFim);
        int top = normalizarLimite(limite);
        Criteria base = criteriosPeriodoSemCancelados(periodo, status);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(base),
                Aggregation.unwind("itens"),
                context -> new Document("$group", new Document("_id", new Document()
                        .append("produtoId", "$itens.produtoId")
                        .append("nomeProduto", "$itens.nomeProduto"))
                        .append("quantidadeVendida", new Document("$sum", "$itens.quantidade"))
                        .append("valorTotal", new Document("$sum", "$itens.subtotal"))),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "quantidadeVendida")),
                Aggregation.limit(top)
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, COLLECTION, Document.class);
        List<ProdutoMaisVendidoResponse> lista = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            Document id = doc.get("_id", Document.class);
            String produtoId = id == null ? null : id.getString("produtoId");
            String nomeProduto = id == null ? null : id.getString("nomeProduto");
            lista.add(new ProdutoMaisVendidoResponse(
                    produtoId,
                    nomeProduto == null ? "Produto" : nomeProduto,
                    toLong(doc.get("quantidadeVendida")),
                    toBigDecimal(doc.get("valorTotal")).setScale(2, RoundingMode.HALF_UP)));
        }
        return lista;
    }

    private DashboardResumoResponse resumoVazio() {
        return new DashboardResumoResponse(
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                0,
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                0,
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    private Criteria criteriosPeriodo(Periodo periodo, StatusPedido status) {
        Criteria criteria = Criteria.where("dataHoraEntrada")
                .gte(periodo.inicio())
                .lt(periodo.fimExclusivo());
        if (status != null) {
            criteria = criteria.and("status").is(status.name());
        }
        return criteria;
    }

    private Criteria criteriosPeriodoSemCancelados(Periodo periodo, StatusPedido status) {
        Criteria criteria = Criteria.where("dataHoraEntrada")
                .gte(periodo.inicio())
                .lt(periodo.fimExclusivo());
        if (status == null) {
            return criteria.and("status").ne(StatusPedido.CANCELADO.name());
        }
        if (status == StatusPedido.CANCELADO) {
            // Incompatível com métricas de venda: força resultado vazio.
            return criteria.and("status").is("__NENHUM__");
        }
        return criteria.and("status").is(status.name());
    }

    private Periodo resolverPeriodo(String dataInicio, String dataFim) {
        LocalDate hoje = LocalDate.now(ZONE);
        LocalDate inicio;
        LocalDate fim;
        try {
            inicio = (dataInicio == null || dataInicio.isBlank())
                    ? hoje.withDayOfMonth(1)
                    : LocalDate.parse(dataInicio.trim());
            fim = (dataFim == null || dataFim.isBlank())
                    ? hoje
                    : LocalDate.parse(dataFim.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Datas devem estar no formato YYYY-MM-DD.");
        }
        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("dataInicio não pode ser posterior a dataFim.");
        }
        Instant inicioInstant = inicio.atStartOfDay(ZONE).toInstant();
        Instant fimExclusivo = fim.plusDays(1).atStartOfDay(ZONE).toInstant();
        return new Periodo(inicioInstant, fimExclusivo);
    }

    private int normalizarLimite(Integer limite) {
        if (limite == null) {
            return LIMITE_PADRAO;
        }
        if (limite < 1 || limite > LIMITE_MAXIMO) {
            throw new IllegalArgumentException("limite deve estar entre 1 e " + LIMITE_MAXIMO + ".");
        }
        return limite;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof org.bson.types.Decimal128 decimal128) {
            return decimal128.bigDecimalValue();
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(value.toString());
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private record Periodo(Instant inicio, Instant fimExclusivo) {
    }
}
