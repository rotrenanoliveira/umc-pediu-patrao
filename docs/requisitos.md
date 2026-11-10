# Requisitos — Dashboard Administrativo (Pediu Patrão)

## 1. Contexto

O Dashboard Administrativo permite que responsáveis pelo estabelecimento (perfis **ADMIN** e **GERENTE**) acompanhem o desempenho das vendas, a situação dos pedidos e os produtos mais vendidos, com base nos dados do sistema de pedidos Pediu Patrão.

## 2. Requisitos funcionais

### RF01 — Indicadores gerais de vendas

O painel deve exibir, para o período filtrado:

- Total de vendas (soma de `valorTotal` dos pedidos não cancelados)
- Quantidade de pedidos não cancelados
- Ticket médio (total de vendas / quantidade de pedidos não cancelados)
- Quantidade de pedidos cancelados
- Taxa de cancelamento (cancelados / total de pedidos do período × 100)

### RF02 — Análise de vendas (gráficos)

O painel deve apresentar:

- Gráfico de linha com o total de vendas por dia no período
- Gráfico de distribuição (rosca) com a quantidade de pedidos por status

### RF03 — Situação dos pedidos

O painel deve listar a quantidade de pedidos agrupados por status (`RECEBIDO`, `EM_PREPARACAO`, `PRONTO`, `SAIU_PARA_ENTREGA`, `RETIRADO`, `FINALIZADO`, `CANCELADO`), considerando o período e filtros aplicados.

### RF04 — Produtos mais vendidos

O painel deve exibir um ranking dos produtos mais vendidos no período, com:

- Nome do produto
- Quantidade total vendida
- Valor total (soma dos subtotais dos itens)

Pedidos cancelados não entram no ranking.

### RF05 — Filtros de consulta

O usuário deve poder filtrar os dados por:

- Data início (`YYYY-MM-DD`)
- Data fim (`YYYY-MM-DD`)
- Status do pedido (opcional)

Quando as datas não forem informadas na API, o sistema assume o mês corrente (do dia 1 até a data de hoje, fuso `America/Sao_Paulo`).

### RF06 — Integração com a API

O dashboard web deve consumir exclusivamente os endpoints REST sob `/api/dashboard/**` para obter os dados exibidos (resumo, vendas por período, pedidos por status e produtos mais vendidos).

### RF07 — Atualização e tratamento de dados

O painel deve permitir:

- Aplicar filtros e atualizar todos os indicadores/gráficos/tabelas
- Exibir estados de carregamento
- Exibir mensagens quando não houver dados no período
- Exibir erro amigável quando a API falhar ou o período for inválido

## 3. Requisitos não funcionais

### RNF01 — Segurança e autorização

- Acesso à página `/dashboard` e aos endpoints `/api/dashboard/**` restrito aos perfis **ADMIN** e **GERENTE**
- Demais perfis autenticados não devem visualizar o menu nem consumir a API do dashboard

### RNF02 — Formato de dados

- A API deve responder em JSON
- Erros de regra/validação devem seguir o padrão `{ "erro": "mensagem" }`

### RNF03 — Usabilidade

- Interface integrada ao layout existente da aplicação (Bootstrap 5 + sidebar)
- Textos e rótulos em português
- Valores monetários formatados em Real (BRL)

### RNF04 — Desempenho

- Agregações realizadas no MongoDB (pipeline de aggregation), evitando processamento desnecessário no cliente
- Carregamento paralelo dos endpoints no frontend

### RNF05 — Disponibilidade de dados

- O dashboard depende da disponibilidade do MongoDB e da autenticidade da sessão do usuário
- Períodos sem pedidos devem resultar em indicadores zerados e estados vazios, sem falha da aplicação

### RNF06 — Consistência de regras de negócio

- Faturamento e ranking de produtos consideram apenas pedidos com status diferente de `CANCELADO`
- Cancelamentos entram apenas na contagem/taxa de cancelamento e na distribuição por status
