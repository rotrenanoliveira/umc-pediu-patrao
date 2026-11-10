# API do Dashboard Administrativo

Base: `/api/dashboard`  
Autenticação: sessão Spring Security (form login)  
Autorização: papéis `ADMIN` ou `GERENTE`


---

## 1. Resumo de indicadores

### `GET /api/dashboard/resumo`

Retorna os indicadores gerais de vendas do período.

#### Exemplo de requisição

```http
GET /api/dashboard/resumo?dataInicio=2026-11-01&dataFim=2026-11-09 HTTP/1.1
Host: localhost:8080
```

#### Exemplo de resposta `200 OK`

```json
{
  "totalVendas": 1250.50,
  "quantidadePedidos": 18,
  "ticketMedio": 69.47,
  "quantidadeCancelados": 2,
  "taxaCancelamento": 10.00
}
```

#### Observações

- `totalVendas`, `quantidadePedidos` e `ticketMedio` consideram apenas pedidos **não cancelados**
- `taxaCancelamento` é percentual (`quantidadeCancelados / totalDePedidosNoPeriodo * 100`)

---

## 2. Vendas por período

### `GET /api/dashboard/vendas-por-periodo`

Retorna a série diária de vendas (pedidos não cancelados).

#### Exemplo de requisição

```http
GET /api/dashboard/vendas-por-periodo?dataInicio=2026-11-01&dataFim=2026-11-09 HTTP/1.1
Host: localhost:8080
```

#### Exemplo de resposta `200 OK`

```json
[
  {
    "periodo": "2026-11-01",
    "totalVendas": 320.00,
    "quantidadePedidos": 4
  },
  {
    "periodo": "2026-11-03",
    "totalVendas": 510.50,
    "quantidadePedidos": 7
  }
]
```

#### Observações

- Dias sem vendas não aparecem na lista
- Lista vazia (`[]`) quando não há vendas no período

---

## 3. Pedidos por status

### `GET /api/dashboard/pedidos-por-status`

Retorna a quantidade de pedidos agrupados por status.

#### Exemplo de requisição

```http
GET /api/dashboard/pedidos-por-status?dataInicio=2026-11-01&dataFim=2026-11-09 HTTP/1.1
Host: localhost:8080
```

#### Exemplo de resposta `200 OK`

```json
[
  { "status": "FINALIZADO", "quantidade": 10 },
  { "status": "EM_PREPARACAO", "quantidade": 5 },
  { "status": "CANCELADO", "quantidade": 2 },
  { "status": "RECEBIDO", "quantidade": 3 }
]
```

#### Exemplo com filtro de status

```http
GET /api/dashboard/pedidos-por-status?dataInicio=2026-11-01&dataFim=2026-11-09&status=FINALIZADO HTTP/1.1
```

```json
[
  { "status": "FINALIZADO", "quantidade": 10 }
]
```

---

## 4. Produtos mais vendidos

### `GET /api/dashboard/produtos-mais-vendidos`

Retorna o ranking de produtos (itens de pedidos não cancelados).

#### Parâmetros adicionais

| Parâmetro | Tipo | Obrigatório | Descrição |
|-----------|------|-------------|-----------|
| `limite` | integer | Não | Quantidade máxima de produtos. Padrão: `10`. Intervalo: `1`–`50`. |

#### Exemplo de requisição

```http
GET /api/dashboard/produtos-mais-vendidos?dataInicio=2026-11-01&dataFim=2026-11-09&limite=5 HTTP/1.1
Host: localhost:8080
```

#### Exemplo de resposta `200 OK`

```json
[
  {
    "produtoId": "65f1a2b3c4d5e6f7a8b9c0d1",
    "nomeProduto": "Pizza Calabresa",
    "quantidadeVendida": 24,
    "valorTotal": 960.00
  },
  {
    "produtoId": "65f1a2b3c4d5e6f7a8b9c0d2",
    "nomeProduto": "Refrigerante 2L",
    "quantidadeVendida": 18,
    "valorTotal": 180.00
  }
]
```

---

## Erros possíveis

Todas as respostas de erro seguem o formato:

```json
{
  "erro": "mensagem descritiva"
}
```

| HTTP | Situação | Exemplo de mensagem |
|------|----------|---------------------|
| `400 Bad Request` | `dataInicio` posterior a `dataFim` | `dataInicio não pode ser posterior a dataFim.` |
| `400 Bad Request` | Data em formato inválido | `Datas devem estar no formato YYYY-MM-DD.` |
| `400 Bad Request` | `limite` fora do intervalo | `limite deve estar entre 1 e 50.` |
| `400 Bad Request` | `status` inválido (conversão de enum) | Mensagem padrão do Spring (parâmetro inválido) |
| `401 Unauthorized` / redirecionamento para login | Sessão ausente ou expirada | — |
| `403 Forbidden` | Usuário autenticado sem perfil ADMIN/GERENTE | Acesso negado pela configuração de segurança |

### Exemplos de erro

```http
GET /api/dashboard/resumo?dataInicio=2026-11-10&dataFim=2026-11-01 HTTP/1.1
```

```json
{
  "erro": "dataInicio não pode ser posterior a dataFim."
}
```

```http
GET /api/dashboard/produtos-mais-vendidos?limite=100 HTTP/1.1
```

```json
{
  "erro": "limite deve estar entre 1 e 50."
}
```

---

## Página associada

- Interface web: `GET /dashboard` (Thymeleaf, mesma autorização ADMIN/GERENTE)
- Consome os quatro endpoints acima via `fetch` em paralelo ao filtrar ou atualizar
