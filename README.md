# Pediu Patrão

Sistema de pedidos para pizzaria desenvolvido com Java 21, Spring Boot, Spring Security, MongoDB e Thymeleaf.

## Execução

1. Instale Java 21 e tenha um MongoDB disponível.
2. Defina `MONGODB_URI` e, opcionalmente, `MONGODB_DATABASE`. Sem variável, a aplicação usa `mongodb://localhost:27017/pediupatrao`.
3. Execute `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).

Não versionar URI, senha, token ou arquivos `.env`.

## Regras implementadas

- Perfis: `ADMIN`, `GERENTE` e `ATENDENTE`.
- Apenas GERENTE pode dar desconto (máximo 20%) ou cancelar pedido.
- Pedidos recebem itens e total calculado com os preços persistidos.
- Fluxo obrigatório: `RECEBIDO → EM_PREPARACAO → PRONTO → SAIU_PARA_ENTREGA/RETIRADO → FINALIZADO`.
- Cancelamentos exigem justificativa e não apagam histórico.
- Inclusões/alterações de pedidos e clientes geram auditoria Mongo. Apenas ADMIN e GERENTE a consultam.

## Demonstração

No vídeo, autentique os três perfis, tente uma operação proibida diretamente pela API, crie um pedido, percorra os estados, aplique desconto e cancele como GERENTE, altere um cliente e consulte os eventos de auditoria.
