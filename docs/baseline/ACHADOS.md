# Achados para as fases seguintes (anotados na leitura da Fase 2, 2026-11-19)

Nada disto foi corrigido na Fase 2, porque essa fase não muda comportamento. Cada item é **hipótese** (visto lendo o código)
até ser testado na fase indicada.

## Fase 3 (segurança e robustez)
- **[CONFIRMADO e CORRIGIDO na 0.9.2]** **Erro do IV dentro do tick da geração**: `Generation.place` chama `Vehicles.spawn` sem `try`. Se o IV lançar um erro (um
  item do pack que não existe mais, por exemplo depois de atualizar ou tirar o OAmP, ou uma cor renomeada), a exceção sai de
  `Generation.onLevelTick` (evento `LevelTickEvent.Post`). Hipótese: derruba o servidor, ou repete a cada 10 ticks, porque o
  chunk continua em `Pending`. Os comandos (`AmcvCommands`) já pegam o erro; a geração não.
- **Falha da reflexão em `FuelPumps`** (IV futuro): o `catch` devolve o motivo, `Generation.placePump` registra e devolve, e o
  `resolve` conta a bomba como resolvida. Pela leitura, o chunk **sai** de `Pending` (não trava). Confirmar com um teste.
- **Respostas de comando com `e.toString()`** (`car`, `lineup`, `fill`): mostram o nome da classe da exceção ao operador.
  Gravidade baixa (só op 2); o log já tem o stack trace.
- **[REFUTADO na 0.9.2]** **`FuelBalance.ORIGINAL`**: o `FuelBalanceTest` mostra que, nas duas ordens (servidor ou cliente
  primeiro), os valores do IV voltam; a segunda aplicação já acha 0,05 e não grava nada como original.

## Fase 4 (defeitos)
- **[CONFIRMADO e CORRIGIDO na 0.9.3]** **Carro duplicado logo depois de o servidor subir**: no mundo de teste, `/amcv draw` na delegacia
  logo depois de subir o servidor disse "placed" para um carro que já estava lá; depois havia **2** veículos na vaga
  (`execute if entity ...` deu `count: 2`). Causa provável (hipótese): as entidades de um chunk carregam depois do chunk, e
  `AmcWorld.blocked` procura o veículo antes disso. O mesmo vale para `Generation` num chunk pendente depois de uma queda.

## Fase 5 (otimização; medir antes)
- `Generation.resolve` recalcula a cada 10 ticks, para cada chunk pendente carregado, o sorteio e a posição de todos os carros
  e bombas das estruturas do chunk (`carsOf`/`pumpsOf`), mesmo quando nada mudou.
- `FuelPumps.place` procura por reflexão `BuilderBlock.blockMap` e `BuilderTileEntity.setTileEntity` a cada bomba.
- `AmcWorld.blocked` faz uma busca de entidades na caixa do carro antes de olhar os blocos.
