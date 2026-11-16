# AMC Vehicles — testes

## Servidor de teste
- `run/` (fora do git): `server.properties` com RCON na porta 25576 e senha `teste`, servidor na 25598. Etapa 1:
  mundo plano (`level-type=minecraft:flat`); desde a etapa 2: mundo normal, semente `20261027` (a mesma da bateria
  do AMC). `run/mods` com `Immersive Vehicles-1.21.1-24.0.0.jar`, `MTS Official Pack-1.21.1-V29.jar` e (desde a
  etapa 2) `abandonedmoderncity-0.1.0.jar`.
- Subir: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew runServer -Dorg.gradle.java.installations.paths=$JAVA_HOME`.
- Comandos: `python3 -I tools/rcon.py "comando 1" "comando 2"`.
- As linhas `MTSERROR: Welcome to MTS...`, `Could not do model-based legacy compats on mtsofficialpack:engineamci4` e
  `For input string: "roller2"` no log são mensagens do próprio IV ao carregar o pack, antes de este mod fazer qualquer coisa (não testei o
  servidor sem este mod).

## Etapa 1 — a Mercedes (16 Nov 2026, versão 0.1.0)
Rodado no servidor de teste, do console, em (0, -60, 0) e vizinhanças, com o chão plano em y -61:

| Comando | Resposta |
|---|---|
| `execute positioned 0 -60 0 rotated 0 0 run amcv mercedes white true false` | `_white`, nariz para o sul, capô aberto, porta-malas fechado, 1500/15000 mB (lava), 1 motor tirado, peças `9:mts:invisible_standing`, `11:mtsofficialpack:gloveboxscout` |
| `... positioned 10 -60 0 rotated 90 0 run amcv mercedes police false true` | `_police`, oeste, capô fechado, porta-malas aberto, idem |
| `... positioned 20 -60 0 rotated 180 0 run amcv mercedes` | `_beige` (sorteada), norte, capô aberto, porta-malas fechado (sorteados), idem |
| `... positioned -10 -60 0 rotated 270 0 run amcv mercedes salmon true true` | `_salmon`, leste, capô e porta-malas abertos, idem |

Conferido nos dados salvos (`save-all flush` e `data get entity`), **e de novo depois de reiniciar o servidor**:
- só as peças `part_9` (`mts:invisible_standing`) e `part_11` (`gloveboxscout`): sem motor, rodas ou bancos;
- `fuelTank`: `currentFluid: "lava"`, `fluidLevel: 1500.0`;
- `instrument0_systemName: "instrument_car_fuelqty"`, `instrument1_systemName: "instrument_car_spedometer_kph"`;
- `door_hood: 1.0` na branca (capô aberto); ausente na de polícia (fechado = 0, o IV não grava zero);
  `door_trunk: 1.0` na de polícia; `door_l` e `door_r` 1,0 nas duas (portas abertas, padrão do pack);
- `anglesy: -90.0` na de polícia (nariz para o oeste, ângulo do Minecraft 90 com o sinal trocado).

**Teste do usuário no jogo (0.1.0):** `Mercedes_salmon`, nariz para o sul (o lado anunciado), capô fechado e
porta-malas aberto como na resposta do comando; porta lateral aberta (padrão do pack na 0.1.0).

**Teste do usuário no jogo (0.1.1):** "está tudo funcionando" (16 Nov 2026); etapa 1 aprovada.

## Versão 0.1.1 — portas sorteadas e marcador à direita (16 Nov 2026)
Servidor de teste, nariz para o sul:

| Comando | Resposta |
|---|---|
| `amcv mercedes red false false true false` | porta esquerda aberta, direita fechada |
| `amcv mercedes` (3 vezes) | `_brown` as duas fechadas; `_silver` e `_black` só a direita aberta |

Dados salvos da vermelha: `door_l: 1.0`, sem `door_r` (fechada = 0), `instrument1_systemName:
"instrument_car_spedometer_kph"` (meio) e `instrument2_systemName: "instrument_car_fuelqty"` (direita), sem
instrumento no 0.

## Etapa 2 — vagas do prédio largo e `/amcv fill` (16 Nov 2026, versão 0.2.0)

### Detecção (`tools/find_spots.py`)
`python3 -I tools/find_spots.py <abandoned_wide_building.nbt> 0`: 36 vagas entre as faixas de `white_concrete` do
piso (camada 0), 34 livres: fileira norte x 11..77 (17 vagas de 3 × 8, nariz N), fileira sul x 3..77 (19 de 3 × 9,
nariz S), das quais x 43..45 e 47..49 (z 21..27) BLOCKED pela escada. Imagem: `docs/spots/abandoned_wide_building.png`.

### Testes de unidade (`PlacementTest`, `./gradlew test`, contra o código do AMC)
- `everyModelOfSpotsExists`: todo modelo de `spots.txt` existe no AMC.
- `stripesOnBothSidesInEveryRotation`: para cada vaga e cada rotação 0..3, as células logo fora dos dois lados longos
  da vaga, giradas por `Placement.turn`, são `white_concrete` em `Def.code` do AMC (os blocos que ele põe, com o
  mato) em 60%+ (passou).
- `carInsideItsBayAndAlongIt`: o corpo do carro (as colunas que ele cobre) fica dentro da vaga girada, o nariz ao
  longo do lado comprido e a altura em cima do piso.
- `turnedDirectionMatchesTurnedPoints` e `yawOfDirections`: a direção girada bate com os pontos girados; o ângulo de
  cada direção.
- **O teste pega erros**: trocando de propósito o giro de 1 quarto de volta pelo de 3, `stripesOnBothSides...` falhou
  ("rot 1: 0 of 16 stripe cells") e `turnedDirection...` também; o código foi devolvido e os 5 passaram.

### No mundo (semente 20261027)
- `/amcs locate structure building_large abandoned_wide_building` (de 0, 100, 0): caixa 321,1154 .. 351,1237 (girada:
  31 × 84), canto 321 1154, rotação 1, chão da cidade y 67 (piso da garagem y 59).
- `forceload add 300 1130 380 1260` e `execute positioned 336 60 1195 run amcv fill`: **34 vagas, 34 carros, 0
  pulados**.
- `python3 -I tools/check_bays.py 336 1195 60`: os 34 carros em y 59,99 (no piso), **10 de 10 células de faixa
  branca** nas duas colunas a 2 blocos de cada lado do meio do corpo, ao longo dos 5 blocos do corpo, em todos; a
  fileira de x 342,57 com o nariz para o leste e a de x 329,93 para o oeste: os dois para a borda da caixa (x 351 e
  321), ou seja, de frente para a parede.

### Vaga bloqueada e `fill` repetido (0.2.0)
- Segundo `/amcv fill` com os 34 carros lá: **0 carros, 34 pulados** ("occupied by a vehicle at 342 59 1166"...):
  não empilha.
- `kill` dos carros, `setblock 343 60 1170 minecraft:stone` (dentro da vaga 2) e `fill`: **33 carros, 1 pulado**
  ("bay 2: Stone at 343 60 1170"); a resposta em várias linhas aparece inteira no console.

### Direção do nariz (`NoseYawTest`, com o jar do IV no classpath dos testes)
`RotationMatrix.setToAngles(0, -yaw, 0)` aplicado ao +Z do modelo (a frente do merc230): yaw 0 → +Z (sul), 90 → -X
(oeste), 180 → -Z (norte), 270 → +X (leste); e `Placement.yaw` concorda com isso nas 4 direções. Assim o "nariz para
o leste/oeste" do `check_bays.py` (que lê `anglesy` com a mesma regra) não depende só da suposição do sinal.

**Não visto no jogo:** a aparência na garagem (o carro inteiro dentro da vaga, sem atravessar paredes ou pilares) e
a garagem de outras rotações no mundo (só a rotação 1 foi testada no mundo; as 4 nos testes de unidade).

## Etapa 3 — geração no mundo (16 Nov 2026, versão 0.3.0)

### Testes de unidade (`DrawTest`)
- `sameInputSameCar`: o sorteio é determinístico.
- `chancesAndColours` (2.000.000 vagas): 2,99% com carro; capô 50,1%, porta-malas 50,0%, porta esquerda 49,8%,
  direita 50,1%; as 15 cores civis entre 3.870 e 4.088 (1/15 ± 0,5%); nenhuma de polícia.
- `policeOnlyPoliceColours`: na delegacia só `_police` e `_police2`.
- `projectionPerCity`: o planejador real do AMC (terreno plano, sementes 20261027, 3478477863671517581,
  -3297019724594858492, 1 e 42; as cidades de 12 × 12 células de cada), com o sorteio pela posição da vaga: 492
  cidades, 172 com prédio largo (35,0%), 5.848 vagas (11,9 por cidade), 159 carros (0,32 por cidade; 2,72% das vagas,
  dentro do acaso para 3%: o esperado seria 175 ± 13); cidades com 0 carros 380 (77,2%), 1 74 (15,0%), 2 30 (6,1%),
  3 7 (1,4%), 4 1 (0,2%); 0,92 carro por prédio largo.

### No mundo (semente 20261027)
Primeira rodada com o sorteio pela ordem da vaga e uma fila em memória; o revisor apontou dois caminhos que o jogo
normal alcança e os testes não (um chunk que cai abaixo de completo e volta sem novo evento de carregamento; um chunk
com dois carros em que um espera, que repetia o log a cada 10 ticks), e que o sorteio pela ordem mudaria se as vagas
de um modelo fossem reordenadas. Corrigido (o mod percorre `Pending`; carros resolvidos não são tentados de novo; o
sorteio usa a posição da vaga; "ocupado" só por `mts:builder_existing`) e **tudo rodado de novo, num mundo novo**:

- **Contagem exata**: 12 prédios largos (`/amcs locate` de uma grade de 7 × 7 pontos, 736 de distância),
  `tools/check_generation.py --load` (forceload de cada caixa, que a gera): em todos, carros na caixa = sorteados =
  "occupied by a vehicle" no `/amcv draw`; **11 carros, 11 sorteados, 11 no lugar**; 11 linhas `(generated)` no log,
  nenhuma vaga pulada.
- **Muitos carros por chunk (chance trocada para 100% só neste teste, depois devolvida a 3%: o diff do `Draw.java`
  não tem a linha, e `javap -constants` do jar mostra `CHANCE_PER_BAY = 0.03d`)**: mundo novo; `forceload` só do chunk
  162,207 (perto da caixa 2593,3265): as vagas 8 a 17 (nos chunks completos 162..164 da fileira 205) postas, 10 linhas
  no log, as outras esperando os chunks delas nascerem; `forceload` da caixa inteira: **33 carros e 1 vaga pulada**
  ("bay 25 skipped (generated): Chest at 2643 64 3272", um baú sorteado pelo AMC dentro da vaga), **34 linhas, uma
  por vaga, nenhuma repetida** (iguais 10 s depois), `pending chunks 0`.
- **Faixas, 4 rotações no mundo** (`check_bays.py`, na primeira rodada; o giro e a posição não mudaram com as
  correções): os carros gerados das caixas -1300,-1976 (3) e 385,1857 (3) e -2559,-1240 (2), e `/amcv fill` nas
  caixas 1000,-2410 (rotação 2) e 2593,-1087 (rotação 0): **todos com 10/10 células de faixa**, 17 narizes N e 17 S nas
  caixas de rotação 0 e 2, E/W nas de rotação 1 e 3.
- **Reinício** (primeira rodada): 79 carros antes e 79 depois; nenhuma linha `[AMCV] Mercedes` nova; as 10 caixas
  geradas de novo 11/11/11; `pending chunks 0`.
- **Borda (chunk completo que não roda)** (primeira rodada): `forceload add` só do chunk 166,204: a Mercedes cujo corpo
  fica todo no chunk 164,204 (a 2 do forçado: completo, sem tick) foi posta ("(generated)"); dados salvos iguais aos de
  um carro normal (só invisible_standing e gloveboxscout, 1.500 de lava, velocímetro no 1, combustível no 2, portas e
  porta-malas sorteados).
- **Pendente sobrevive a um reinício** (primeira rodada): dono no chunk -243,-304 (a 2 do forçado -245,-304: completo)
  e corpo também em -242,-304 (a 3: não completo): `pending chunks 1`, sem carro; servidor parado e reiniciado: ainda
  `pending chunks 1`, sem carro; `forceload add` de -242,-304: o carro apareceu, `pending chunks 0`.
- **Pendente sobrevive a descarregar** (segunda rodada): a Mercedes prata da vaga 13 da caixa 4648,-415 (dono 291,-25,
  corpo também em 292,-25): `forceload` de 289,-25 → `pending chunks 1`, sem carro; `forceload remove` e 20 s → ainda
  1, sem carro; `forceload add` de novo → 1, sem carro; `forceload add` de 292,-25 → **o carro apareceu**,
  `pending chunks 0`.

### Ferramentas corrigidas no caminho (não eram defeitos do mod)
- `check_bays.py` pareava a lista de posições com a de `anglesy`, mas o IV não grava `anglesy` quando é 0 (nariz para
  o sul): as listas desalinhavam e os narizes saíam errados. Agora pergunta o ângulo de cada carro (ausente = 0).
- O RCON parte respostas acima de 4096 bytes; o cliente lia só o primeiro pedaço (`tools/rconlib.py` agora lê
  todos). Mandar o comando e um marcador colados derrubava a conexão: o `RconClient` do jogo lê até 1.460 bytes, trata
  um pacote por leitura e fecha quando uma leitura traz menos de 10 bytes (conferido no código do Minecraft 1.21.1);
  agora vai um pacote por vez.
- O primeiro comando do AMC depois de iniciar o servidor planeja as cidades (~10 s, "Planning cities"; o servidor fica
  parado nesse tempo).

**Não visto no jogo:** um carro gerado sozinho, com um jogador andando (os testes usaram forceload; a borda da visão
do jogador é o caso "completo sem tick", testado acima).
