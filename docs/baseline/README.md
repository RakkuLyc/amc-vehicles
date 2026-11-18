# Foto da 0.8.1 (Fase 0 da preparação do 1.0.0, 2026-11-18)

A referência para provar, nas fases seguintes (limpeza, refatoração, segurança, bugs, otimização, tradução), que nada mudou
sem querer. Tirada do código da 0.8.1 (commit `c1d134a`), antes de qualquer mudança.

## Arquivos
- `resources_json_sha256.txt`: SHA-256 dos 21 JSON de `src/main/resources` e do `spots.txt`. Conferir:
  `(cd src/main/resources && find . -name '*.json' | sort | xargs sha256sum) | diff - <(head -21 docs/baseline/resources_json_sha256.txt)`.
- `jar_entries_0.8.1.txt`: as 60 entradas do jar (23 classes; o jar tinha 72.015 bytes).
- `constants_0.8.1.txt`: `javap -constants -p` das constantes `static final` de todas as classes (sorteio, chances, lava 0,05,
  tanque 0,15, bateria 0,0, `EVERY` 10, `NAME` do `Pending` etc.).
- O teste dourado: `src/test/java/com/amcvehicles/GoldenDrawTest.java` e a referência
  `src/test/resources/golden/draw_golden.txt` (4.708 linhas, SHA-256 `717de76353b328e4bf37b2dd6c02d50b5d7ad5038f3be386432ed99b25e4d8b7`).
  Parte A: o sorteio puro, 200 sementes de cópia x todas as vagas (civil e delegacia) x todas as bombas: 2.160 carros e
  2.000 linhas de bomba. Parte B: o planejador real do AMC (sementes de mundo 20261027 e 42, terreno plano em y 70), a
  posição e o ângulo no mundo de cada carro sorteado (174) e de cada bomba (374). Se a referência faltar, o teste a grava e
  falha uma vez (para ela ser olhada); numa diferença, a saída vai para `build/golden/draw_golden.actual.txt` e a falha
  mostra a primeira linha diferente.

- `src/test/resources/golden/pending_old_world.dat` e `pending_phase0_world.dat`: amostras do `Pending` gravadas por
  versões reais (o mundo de teste anterior, feito até a 0.8.1: `chunks` vazio, 43 bytes; o mundo da Fase 0: 1 chunk
  pendente, ChunkPos 18,71, um carro esperando os chunks sob o corpo). Para um teste futuro de "mundo antigo ainda abre".
  O formato salvo é só `data.chunks` (um long array de ChunkPos). As chaves `Generation.resolvedKey` (que usam a ordem da
  vaga em `spots.txt`) ficam só na memória (`RESOLVED`, limpo ao parar o servidor): não são gravadas.
- O mundo de teste anterior foi guardado em `run/world_before_phase0` (fora do git).

## Números
- Build (`./gradlew clean build --rerun-tasks`, Java 21): 25 testes passam em 21 s, nenhum aviso do compilador. Com o
  teste dourado: 26 testes em 40 s.
- Projeção do `DrawTest.projectionPerCity`: 492 cidades, 18.549 vagas (37,7 por cidade), 1.683 carros (3,42 por cidade,
  9,07% das vagas), 41 cidades (8,3%) sem carro.
- Compilador: nenhuma linha `warning:`, `Note:`, `deprecat` ou `unchecked` no log do build.
- Loot: `python3 -I tools/gen_loot.py --write` seguido de `git diff --stat`: vazio.
- Prova de que o teste dourado pega uma mudança: `DOOR_OPEN_CHANCE` de 0,5 para 0,51 fez o teste falhar na linha 205
  (uma porta `doors=01` virou `11`); com o arquivo de volta (`git checkout`), passou.

## Servidor de teste (mundo novo, semente 20261027)
- Subiu em 17,8 s. `check_generation.py --load` em 6 caixas (achadas por `/amcs locate structure <categoria> <nome>`):
  casa de vidro (-351,434 .. -312,481), delegacia (-351,314 .. -298,351), oficina média azul e preta (-460,385 .. -426,410),
  casa abandonada (-351,172 .. -323,217), prédio largo (321,1154 .. 351,1237) e posto amarelo (-2036,458 .. -2000,502):
  **4 carros nas caixas, 4 sorteados, 4 no lugar** (`cagouille_police` na delegacia; `escargot_lightyellow`, `scout_yellow`
  e `camaro_seagreen` no prédio largo); as 8 bombas do posto postas ("already there" no `/amcv draw`).
- Tanques das bombas (`data get block`): a sorteada com 3 baldes tem `fluidLevel` 3000 de `lava`, a de 1 balde 1000, uma
  vazia `currentFluid ""`.
- `/amcv fill` na casa de vidro: "2 bays, 2 cars [scout_red, escargot_brown], 0 skipped".
- Log (166 linhas; guardado fora do git em `run/baseline-0.8.1-server.log`, porque tem caminhos da máquina): 12 ERROR e 15
  WARN, **nenhum do mod**: 11 `MTSERROR` do IV ao ler os packs (os mesmos da 0.8.1), a tag `minecraft:placeable`, 2 refmaps
  do TACZ, 5 ambiguidades do comando `teleport` do Minecraft, o AMC levando 15,3 s numa tarefa de carregamento, o aviso de
  modo offline do servidor de teste e um "Can't keep up" de 2,7 s ao gerar as 6 áreas à força. As linhas do mod: a lava a
  0,05 ao iniciar, 4 carros e 8 bombas "(generated)".
- Para a Fase 3 (anotado, não mexido): o servidor de teste roda com `online-mode=false`, RCON com a senha `teste` e
  `server-ip=` vazio (escuta em todas as interfaces).
