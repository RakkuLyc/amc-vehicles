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

## Etapa 4 — as outras estruturas (16 Nov 2026, versão 0.4.0)

### Levantamento e proposta
- Varredura de todos os 30 modelos, todas as camadas, todo bloco com 8+ células na camada como possível faixa
  (`find_spots.py` por bloco): fora o prédio largo, só falsos positivos (tábuas do piso das casas e do prédio das
  rampas, a grade de concreto branco do saguão da torre, o piso do prédio azulado e do apocalíptico) e, no posto, as
  próprias ilhas das bombas (contornadas de terracota vermelha). Mapas de piso coberto/aberto para achar as garagens.
- Proposta (`docs/spots/etapa4_propostas.png`), aprovada pelo usuário; ele acrescentou a farmácia grande e escolheu o
  nariz sorteado nos postos. Ajustes antes da proposta, pela checagem de `find_spots.py --bays`: oficina média (uma
  coluna em x 6: vagas em x 8..10 e 13..15), casa moderna (blocos em z 9: vagas de z 10), farmácia grande (a sebe da
  fachada em z 8..9: vagas até z 7).

### Testes de unidade
- `PlacementTest.floorAndRoomInEveryRotationAndVariant` (novo): toda vaga, nas 4 rotações, em **toda variante de cor**
  do modelo, tem piso em todas as células e 2 blocos livres (ar, nada salvo, plantas, tapetes, cipós) em cima, em
  `Def.code` do AMC (com o mato): 10.628 células conferidas. **O teste pega erro**: com a vaga da oficina média de volta
  em x 6..8, falhou ("workshop_medium_blue_black ... cell 6,4: minecraft:polished_blackstone 1 over the floor"); o
  arquivo foi devolvido.
- `stripesOnBothSidesInEveryRotation`: agora só para os modelos de faixa pintada (o prédio largo).
- `carInsideItsBayAndAlongIt`: os dois sentidos das vagas de nariz sorteado.
- `DrawTest.projectionPerCity` (as 57 vagas): 16.567 vagas em 492 cidades (33,7 por cidade), 497 carros (1,01 por
  cidade, 3,00%); cidades com 0 carros 196 (39,8%), 1 166 (33,7%), 2 80 (16,3%), 3 31 (6,3%), 4 18 (3,7%), 6 1 (0,2%).

### No mundo (semente 20261027, mundo novo)
- Uma cópia de cada estrutura nova (`/amcs locate structure ...` de 0, 100, 0). **Geração, contagem exata**
  (`check_generation.py --load` nas 9 caixas): 8 sem carro sorteado e a delegacia com 1 (`Mercedes_police2`, vaga 1):
  **1 carro, 1 sorteado, 1 no lugar**.
- **`/amcv fill`** em cada uma: oficina média 2/2 (rotação 2), oficina pequena 2/2 (rotação 2), posto 4/4 (rotação 3),
  posto pequeno 2/2 (rotação 0), farmácia grande 4/4 (rotação 0), delegacia 4/4 (rotação 2), casa com garagem 1/1,
  casa suburbana 2/2 (rotação 2); casa moderna **0/2 na primeira rodada: "Moss Carpet at 242 70 184"**: o tapete de
  musgo do mato do AMC tem colisão de 1/16 e a checagem o tratava como obstáculo; corrigido (`AmcWorld.THIN`: no
  primeiro bloco sobre o piso, até 1/4 de altura não barra) e de novo: **2/2**. Total 23 carros.
- **Delegacia**: os 4 carros só de cores de polícia (2 `_police`, 2 `_police2`); `check_bays.py ... white_concrete`:
  6, 7, 8 e 7 de 10 células de faixa (as linhas são gastas no modelo), nariz para o prédio.
- **Farmácia grande**: `check_bays.py` dá 2 a 4/10, porque ele procura a faixa a 2 blocos do meio e estas vagas têm 4 de
  largura (a faixa fica a 3 de um lado); conferido pela conta: vaga x 2..5, z 1..7 da cópia de canto -317,172, rotação
  0, meio em x -313,0, z 176,5; o carro está em x -313,00, z 175,075 (= 176,5 - 1,425, nariz para o sul). Exato.
- **Postos**: narizes misturados como pedido (posto: 3 N e 1 S; posto pequeno: 1 E e 1 W).

- **O segundo carro do `kill`** (2 apagados, 1 nas 9 caixas): `Mercedes_blue in gas_station_small_yellow bay 2 at 309
  70 338 (generated)`, um posto pequeno vizinho, num chunk da borda da área carregada.
- **Geração em todas as vagas novas (chance trocada para 100% só neste teste, depois devolvida a 3%: `git diff` vazio
  e `javap -constants` do jar com `CHANCE_PER_BAY = 0.03d`, build com `--rerun-tasks`)**: mundo novo, as mesmas 9
  caixas: **23 carros, 23 sorteados, 23 no lugar**, nenhuma vaga pulada; 28 linhas `(generated)` (as 23 e 5 de
  estruturas vizinhas nos chunks da borda), nenhuma repetida; narizes do posto 2 N e 2 S, do posto pequeno 1 E e 1 W
  (o caminho do nariz sorteado na geração); `pending chunks 0`.
- **Carros sobre o tapete de musgo**: os 2 da casa moderna, segundos depois, em y 69,991 (o piso da camada do chão
  está em y 69 e o carro fica em cima dele, como os outros: ~0,01 abaixo do topo do bloco); os tapetes continuam lá
  (`execute if block 242 70 184 minecraft:moss_carpet` passou).

**Não visto no jogo:** a aparência nestas estruturas (o carro dentro da garagem sem atravessar parede, ao lado da ilha
sem atravessar a bomba, antes da sebe da farmácia); as outras variantes de cor só nos testes de unidade (no mundo,
uma variante de cada).

## Versão 0.4.1 — garagens da casa abandonada e do prédio pequeno (16 Nov 2026)
- Apontadas pelo usuário. Casa abandonada: a sala da frente à esquerda não tem parede na frente (z 13, ar até 4 de
  altura), piso de pedra; prédio pequeno: térreo de pilotis aberto na frente, camadas 1 a 5 livres. Proposta
  (`docs/spots/garagens_propostas.png`) conferida com `find_spots.py --bays`: a vaga 3 do prédio pequeno batia num banco
  de escada de abeto em x 15..16, z 12 e foi para x 12..14. Aprovada pelo usuário ("Aprovar as 6").
- Testes de unidade: todos passam; `floorAndRoom` 11.204 células; projeção 19.905 vagas em 492 cidades (40,5 por
  cidade), 599 carros (1,22 por cidade, 3,01%), 162 cidades sem carro (32,9%); casa abandonada 0,07 por cópia, prédio
  pequeno 0,11.
- No mundo (semente 20261027, mundo novo): 2 cópias de cada (`/amcs locate` de 0,0, 1500,1500 e -1500,1500).
  Geração: nenhum carro sorteado nas 4 (3%), 0 no mundo, igual. `/amcv fill`: casa abandonada 2/2 (rotações 0 e 2),
  prédio pequeno 4/4 (duas cópias de rotação 1), nenhuma vaga pulada. O caminho da geração é o mesmo das outras vagas
  (só os dados mudaram); não foi refeita a rodada a 100%.

## Versão 0.4.2 — carros apoiados nos blocos de apoio, não enterrados (16 Nov 2026)
- O usuário testou com um jar de teste a 100% (não versionado) e viu os carros meio enterrados (print: a soleira e as
  caixas de roda abaixo do piso, um bloco escuro afundado sob a caixa da roda dianteira).
- Causa, medida no pack: o `merc230.obj` tem os objetos `cube_fl/fr/rl/rr` (blocos de apoio nas posições das rodas,
  visíveis quando falta a roda: `part_present_N` = 0 e `terrain_distance` 0..0,7 no `merc230.json`) com o ponto mais
  baixo em y -0,46875 (o mais baixo do modelo; a carroceria -0,1875); as caixas de colisão mais baixas também descem a
  -0,47. A origem era posta no piso, então tudo afundava 0,47. O ponto mais alto do modelo: 1,625 (giroflex das cores
  de polícia), carroceria 1,5625.
- Correção: origem 0,46875 acima do piso (`Placement.CAR_LIFT`) e 3 blocos livres exigidos (`Placement.HEADROOM`,
  topo do carro a 2,09). Testes de unidade: todos passam com 3 blocos (`floorAndRoom`, 11.204 células: as 63 vagas
  cabem).
- No servidor (mundo novo): `/amcv fill` na delegacia 4/4 e no prédio largo 34/34 (3 blocos livres no mundo);
  posição logo depois e 30 s depois, em chunks que rodam: y 69,460175 (piso em y 69) e 57,460175 (piso em 57): parados,
  apoiados nos blocos (a base deles 0,0086 dentro do piso, o mesmo assentamento de antes).
- Jar de teste a 100% refeito com a correção (`amcvehicles-0.4.2-TESTE-100pct.jar`, só local; `javap -constants`:
  `CHANCE_PER_BAY = 1.0d`, `CAR_LIFT = 0.46875d`); o jar normal tem `CHANCE_PER_BAY = 0.03d`.

**Não visto no jogo:** a aparência corrigida (precisa do cliente do usuário).

## Etapa 5 — 8 modelos (16 Nov 2026, versão 0.5.0)

### Prova da bomba (para a etapa 6)
- `/amcv pump` no servidor: `execute positioned -320 69 330 rotated 0 0 run amcv pump 2` e `... -325 69 330 rotated 90
  0 run amcv pump 0`: as duas postas; `data get block`: `id: "mts:builder_fluidtank"`, `packID: "mtsofficialpack"`,
  `systemName: "fuelpump"`, `anglesy` -180 e 90, `tank` com `currentFluid: "lava"`, `currentFluidMod: "minecraft"`,
  `fluidLevel: 2000.0` na primeira e vazio na segunda. Depois de reiniciar o servidor: iguais. A reflexão nos dois
  membros `protected` do IV funcionou.

### Testes de unidade (15, todos passam)
- `CarsTest.tableMatchesThePacks`: a tabela de `Cars` contra os jars (lidos como zip): cada cor existe, as de polícia
  são todas as do pack, as variáveis das portas e do porta-malas existem (o Mustang não tem `door_trunk`), o capô
  existe, `lift` = base dos blocos de apoio e `top` = ponto mais alto do `.obj` (até 1e-4), e cabem em 3 blocos.
- `DrawTest.chancesModelsAndColours` (4.000.000 vagas): 3,00% com carro; os 8 modelos entre 14.838 e 15.152 (1/8 ±
  0,6%); cada cor civil de cada modelo perto de 1/n do modelo; nenhuma de polícia; capô 50,1%, porta-malas 50,1% (só
  nos modelos com porta-malas), portas 50,1%.
- `DrawTest.policeOnlyPoliceModelsAndColours` (300.000): `cagouille_police` 100.105, `mustang_police` 99.956,
  `mercedes_police` 49.779, `mercedes_police2` 50.160 (1/3, 1/3, 1/6, 1/6).
- `PlacementTest.carInsideItsBayAndAlongIt`: os **8 modelos** dentro de todas as 63 vagas, nas 4 rotações e nos dois
  sentidos das vagas sorteadas (o Mustang, 2,97 de largura, e a Cagouille, 6,38 de comprimento, os mais justos).
- A projeção por cidade não muda (o índice 0, "a vaga tem carro", é o mesmo): 599 carros, 1,22 por cidade.

### No mundo (semente 20261027)
- `/amcv lineup` (de -335, 69, 305, nariz para o sul): **12 carros**. Respostas: só o motor tirado em cada um (no
  Escargot também o estepe `wheellarge`); combustível 1.500 (Mercedes, Cagouille, Escargot, Station Wagon, Scout), 1.000
  (Camaro, Highwayman), 800 (Mustang), lava; mostradores: Mercedes e Cagouille 1 e 2, Camaro e Highwayman 0 e 1, Escargot
  2 e 1, Station Wagon 0 e 2, Mustang e Scout 2 e 3; portas por modelo (a Station Wagon com as 4, o Mustang sem
  porta-malas, o Scout com `door_tailgate`).
- Dados salvos (`save-all`, `data get entity`): em todos, **só** `instrument_car_spedometer_kph` e
  `instrument_car_fuelqty` nos mostradores acima (sem conta-giros, relógio ou temperatura); peças: as permanentes, mais
  a caçamba e o adesivo do Scout e o enfeite de espelho do Mustang. Altura 30 s depois, piso em y 69: Mercedes, Escargot
  69,460; Cagouille, Highwayman 69,429; Camaro 69,491; Mustang 69,4375; Station Wagon e Scout 69,46875 (chunk sem tick):
  cada um no `lift` dele.
- Geração (`check_generation.py`, num mundo onde algumas áreas já tinham sido geradas pela 0.4.2): as 3 caixas geradas
  agora batem (prédios largos -1300,-1976: `mustang_whitestripe`; 385,1857: `highwayman_blue`, `camaro_brown`,
  `scout_tan`; posto 2593,-351: nenhum); as 2 geradas antes (prédio largo 1000,-2410 e delegacia) têm os carros do
  `fill` antigo e não contam.
- `/amcv fill` na delegacia: `[mustang_police, cagouille_police, mercedes_police, cagouille_police]`, só viaturas.

**Não visto no jogo:** a aparência dos 7 modelos novos (altura, portas, painel), que só o cliente do usuário mostra.

## Etapa 6 — bombas de combustível e balanceamento (16 Nov 2026, versão 0.6.0)

### Testes de unidade (22, todos passam)
- `PumpTest.floorAndRoomInEveryRotationAndVariant`: as 10 bombas, nas 5 cores e 4 rotações (200 posições): piso
  embaixo, a célula e a de cima livres em `Def.code` do AMC. **Pega erro**: com uma bomba de propósito em x 22, z 11
  (um pilar da cobertura) falhou ("minecraft:chiseled_polished_blackstone at +0"); o arquivo foi devolvido.
- `pumpsOutOfTheBays`, `facingTurnsWithTheStructure`, `pumpKeysNeverBayKeys`, `pumpsOfTheGasStations` (8 e 2).
- `thirtyPercentWithOneBucket` (1.000.000): 30,00% com 1 balde. `pumpsOfAStationIndependent` (200.000 postos): média
  2,400 bombas com lava por posto e variância 1,679 (binomial de 8 com 0,3: 2,4 e 1,68), histograma
  [11542, 39476, 59288, 50937, 27201, 9326, 1951, 263, 16]: 6 ou mais em 1,1% dos postos.

### No mundo (semente 20261027, mundo novo)
- **Balanceamento**: no log ao iniciar, `[AMCV] Lava as a fuel set to 0.35 (server starting; was: diesel 1.0, avgas
  1.0, gasoline 1.0); IV's config file is not changed`. A primeira rodada tinha mudado também `furnace` e
  `brewing_stand` (não são de motor): corrigido. `config/mtsconfig.json` com `"lava": 1.0` nos 5 tipos antes e depois
  de duas partidas do servidor.
- **`/amcv draw` antes de gerar** (posto 2593,-351 e posto pequeno 385,172): lista as bombas com posição, chunk e lava
  sorteada, "chunks around not ready", sem forçar carregamento.
- **Espera e o reparo do AMC** (posto pequeno, bombas no chunk 24,11): `forceload` do chunk 22,11 (o 24 completo, o
  vizinho 25 não): `pending chunks 1`, nenhuma bomba. Uma bomba posta à mão (`/amcv pump 1`) em 397, 70, 181 (célula de
  ar no `.nbt`). `forceload` do 26,11 (gera o 25): a bomba gerada 1 apareceu (0 balde, "generated"), `pending chunks 0`,
  e a posta à mão **continuou lá** (1.000 mB): o reparo do AMC não passou na célula; a regra dos 8 vizinhos fica como
  margem (ver `CLAUDE.md`).
- **Posto grande**: `forceload` da caixa: as 8 bombas postas ("generated"), lava 1, 0, 0, 1, 1, 1, 1, 1 balde como o
  sorteio do `draw`; `data get block`: `systemName "fuelpump"`, `fluidLevel 1000` nas com lava, sem nível nas vazias;
  `anglesy` 90 nas 8 (a cópia está na rotação 3).
- **Reinício**: as bombas continuam (mesma lava); `/amcv fill` no posto pequeno: "already there" nas 2.

### Revisão antes da entrega (0.6.0)
- **Consumo**: no `PartEngine` do IV o consumo é `getTotalFuelConsumption() * fuelUsageFactor / fuelFactor`: a potência
  divide, então 0,35 faz cada balde render ~1/3 (a estimativa de ~0,45 cidade por balde vale, a partir da medida do
  usuário).
- **Arquivo de config**: o `saveToDisk` do IV também é chamado pelo `ControlSystem` e pelo `GUIConfig` (controles e tela
  de configuração do IV, durante o jogo). Acrescentado: os valores voltam ao parar o servidor e ao sair de um servidor;
  no servidor de teste, o log diz `[AMCV] Lava as a fuel put back to IV's values (server stopped): {diesel=1.0,
  avgas=1.0, gasoline=1.0}` e o arquivo segue com 1,0 nos 5 tipos. Uma gravação feita com o mundo aberto ainda leva o
  0,35 para o arquivo (documentado).
- **Tanque dos carros**: o `EntityFluidTank.fill` só junta o mesmo fluido do mesmo mod (ou `wildcard`): com o mod `""`
  um balde de lava (`minecraft`) seria recusado. Agora o tanque é gravado com `currentFluidMod: "minecraft"`
  (conferido em `data get entity` de um carro do `/amcv car`). Despejar o balde não foi testado (precisa de jogador).
- **Células das bombas**: nas 10 variantes, todas ar no `.nbt`; a regra do jogo aceita agora também plantas, cipós,
  teias e blocos finos, como o `PumpTest`.

**Não visto no jogo:** a aparência e a frente das bombas, abastecer um carro na bomba e com balde, o consumo novo da
lava (precisa dirigir) e o mod carregando no cliente (`ClientHooks` é nova).

## Versão 0.6.1 — sem carros nos postos (16 Nov 2026)
- Pedido do usuário depois de testar a 0.6.0: os carros nos postos atrapalhavam abastecer um carro montado. As 6 vagas
  dos postos saíram de `spots.txt`; as bombas ficam. O sorteio das outras vagas não muda (é pela posição da vaga).
- Testes de unidade: todos passam; projeção 17.507 vagas em 492 cidades (35,6 por cidade), 527 carros (1,07 por
  cidade, 3,01%), 187 cidades sem carro (38,0%).
- Servidor: `/amcv fill` no posto pequeno e no posto: "0 bays, 0 cars", bombas "already there"; `/amcv draw` no posto:
  0 vagas, as 8 bombas listadas.

## Versão 0.6.2 — lava das bombas: 50% vazias, 50% com 1 a 3 baldes (16 Nov 2026)
- Pedido do usuário (primeiro "de volta ao original, 0 a 3", depois "50% de chance de ter 0, 50% de chance de ter 1, 2
  ou 3"; lido como 1/6 para cada uma de 1, 2 e 3).
- `PumpTest.halfEmptyHalfOneToThree` (1.200.000): 0 balde 599.997 (50,0%), 1 199.731, 2 199.912, 3 200.360 (16,7%
  cada). `pumpsOfAStationIndependent` (200.000 postos): média 7,998 baldes por posto grande (esperado 8) e variância
  10,636 (esperado 32/3 = 10,67).
- Mundo novo: posto grande e posto pequeno gerados; as 10 bombas com a lava do sorteio (posto: 0, 1, 1, 0, 0, 0, 0, 0
  baldes; posto pequeno: 2 e 0), `fluidLevel` 1000 e 2000 onde há, sem nível nas vazias.
