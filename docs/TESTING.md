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

## Versão 0.7.0 — loot de oficinas e postos (16 Nov 2026)

### Testes de unidade (`LootDataTest`, 25 no total, todos passam)
- `everyItemExistsAndNothingExcluded`: as 57 entradas de item existem nos jars (IV base e os dois packs) e nenhuma é de
  polícia, `_legacy`, `huge` ou de arnês. **Pega erro**: com `blowtorchh` falhou ("no item
  mts:mtsofficialpack.blowtorchh"); o arquivo foi devolvido e regerado.
- `referencedTablesExist`, `everyPoolAddsUpToTenThousand` (cada sorteio = peso + vazio = 10.000),
  `modifiersAreRegisteredAndPointAtTheAmcTables` (8 modificadores, a lista global confere, cada um aponta para a tabela
  certa do AMC).

### No servidor (mundo novo, AMC + AMC TACZ Loot + TACZ + LesRaisins + First Aid + AMC Vehicles juntos)
- Nenhum erro de carregamento de loot no log. `tools/sample_loot.py` gera a tabela do AMC N vezes num baú (Sorte 0) e
  compara cada item com o esperado (desvio em desvios-padrão da binomial). Resultados:
  - `workshop/chest` (16.000): banco 9,38% (esperado 9,20), roda 9,34, motor 2,04 (2,00), blowtorch 4,29 (4,40), repair
    kit 2,10, jumper pack 1,98; todos entre -0,7 e +0,9 desvio.
  - `workshop/barrel` (16.000): banco 4,74 (4,60), roda 4,49, motor 1,03 (1,00), blowtorch 2,24 (2,20), repair kit 1,19
    (1,00; +2,4 desvios, o único acima de 2), jumper pack 1,04.
  - `gas_station/chest` (8.000): galão vazio 4,55 (4,40), cheio 4,58; `gas_station/barrel` (16.000): vazio 2,11 (2,20),
    cheio 2,24; em todos os galões cheios o fluido foi `lava` (366 no baú de 8.000 e 359 no barril de 16.000).
  - As 4 tabelas "difíceis" (4.000 cada): todos dentro de 2 desvios.
  - Nenhum item além de 3 desvios. Com amostras de 8.000, dois itens (roda no baú, jumper pack no barril) tinham saído
    perto de -2,4 desvios e voltaram ao esperado com 16.000: ruído (com 36 comparações, 1 a 2 acima de 1,96 desvio são
    esperados só por acaso).
- **Soma, não substitui**: com o meu loot ligado, os itens do AMC TACZ Loot (`tacz:`, `lrtactical:`, `firstaid:`)
  continuam saindo em ~57% dos baús e ~34% dos barris de oficina e posto. Não comparei com uma rodada sem o meu mod
  (o servidor de teste carrega o AMC Vehicles pelo código-fonte, não da pasta `mods`); o argumento é que as tabelas e
  modificadores do AMC TACZ Loot não foram tocados.
- **Vasos** (`workshop/pot`, `gas_station/pot` e as versões difíceis, 6.000 vasos): 0 itens do AMC Vehicles.
- **Variantes excluídas**: nas amostras das 4 tabelas "difíceis", nenhum id com `police`, `legacy`, `huge` ou `harness`.

### Defeitos do amostrador achados e corrigidos no caminho (não eram das tabelas)
- Ele cortava o texto dos itens esperando `count` primeiro: o galão cheio (que tem `components`) era atribuído ao item
  vizinho. Agora separa pelas chaves `{...}` da lista.
- A chave `id` era procurada como `id: "...`, que também casa com o fim de `jerrycanFluid: "lava"`: o galão cheio
  virava item "lava". Agora só casa `id` inteiro.
- Efeito: nas primeiras rodadas o galão cheio saiu 0% e alguns itens "fora" do intervalo; depois das correções, tudo
  dentro do esperado.

**Lido no código do IV** (`ItemPartInteractable.doEntityInteraction`, não rodado): um galão com `jerrycanFluid` despeja
no carro com `fuelTank.fill(fluido, "wildcard", 1000)`: o `wildcard` passa pela checagem de mod do tanque (então o tanque
dos carros do AMC Vehicles, marcado `minecraft`, não atrapalha); precisa de **um motor instalado** (sem motor o IV avisa
"sem motor" e não despeja), de fluido compatível com o motor (a lava é) e de espaço de 1.000 mB no tanque. O galão vazio
se enche numa bomba (`TileEntityFuelPump.interact`: bomba com 1.000 mB ou mais e galão sem `jerrycanFluid`).

**Não visto no jogo:** usar um galão cheio do loot num carro com motor (confirmado só pela leitura do código acima); a Sorte (o console não a aplica; `quality` = 25%
do peso como no AMC TACZ Loot) e abrir os containers num mundo gerado de verdade; e a lista de ids foi conferida nos
jars, não no registro de itens do jogo (que o servidor de teste usou nas amostras).

## Versão 0.7.1 — banco genérico no loot e só rodas offroad e vintage (16 Nov 2026)
- Pedido do usuário: incluir o banco genérico `seat` (10 cores, ids `mts:mtsofficialpack.seat<cor>`, sem `_`) e deixar
  só as rodas offroad e vintage. `tools/gen_loot.py --write`; 53 entradas de item (eram 57), todas nos jars; 25 testes de
  unidade passam.
- Servidor (`workshop/chest`, 12.000 containers): banco 9,46% (esperado 9,20; +1,0 desvio), roda 8,97% (-0,9), motor
  2,09, blowtorch 4,32, repair kit 2,26 (+2,0), jumper pack 1,89; nenhum além de 3 desvios. Bancos por modelo: genérico
  258, sport 280, vintage 293, car 304 (de 1.135): os 4 com a mesma chance; 34 cores distintas vistas (7 + 7 + 10 +
  10). Rodas vistas: só `wheeloffroad_default` e `wheelvintage_lightblue`, `_mint`, `_sinty`. As 8 tabelas e o resto
  não mudaram (só a escolha dentro de banco e roda).

## Desempenho dos veículos parados (17 Nov 2026, versão 0.7.1)
Pergunta do usuário: o que muda a longo prazo com muitos chunks carregados e carros gerados; os carros ficam longe do jogador?

**Lido no código do IV** (`BuilderEntityExisting`, `EntityManager`, `AEntityVehicleD_Moving`; descompilado, não rodado):
- O carro é uma entidade comum do Minecraft (`mts:builder_existing`) gravada no NBT do chunk (`saveWithoutId`) e
  restaurada quando o chunk carrega (`loadFromSavedNBT`); sem distância de despawn. Só é descartado se o veículo ficar
  inválido, cair abaixo do mundo ou sair da borda do mundo.
- Chunk descarregado: o carro sai da memória (`onRemovedFromLevel` chama `remove()`) e fica só no disco.
- Não há modo "dormindo" para carro parado: o `EntityManager.tickAll` roda o `update()` de todo veículo registrado a cada
  tick, com a física completa (`getForcesAndMotions`, `moveVehicle`, caixas de colisão), exceto na configuração
  `noclipVehicles`.

**Medido** (servidor de teste, mundo novo, 4 áreas de prédio largo com `forceload`, tudo entidade-ticking, 20 s
depois de cada `/amcv fill`, `tick query` 3 vezes): MSPT médio com 4 carros 1,4 ms; 36 carros 3,0; 67 carros 4,5; 98 carros
6,0; 131 carros 7,8 (P99 de 5 a 12 ms). Crescimento **linear**, ~0,05 ms por carro (+1,5 ms a cada ~31 carros), sem sinal
de custo quadrático até 131. O orçamento de um tick é 50 ms: ~300 carros ticando = ~15 ms.
Disco (`run/world/entities`, depois de `save-all flush`): 81.175 bytes antes e 258.550 depois de 127 carros novos:
~1,4 KB por carro.

**Não medido:** o cliente (renderização, som, a cópia do cliente do `EntityManager`); carros em chunks carregados mas
**fora da distância de simulação** (o servidor de teste sem jogador só tem chunks forçados, que tickam). Hipótese pelo
código: o `EntityManager` do IV só registra o carro no primeiro tick da entidade (o `baseTick` restaura o IV a partir do
NBT), então um carro nunca ticado pelo Minecraft não custa física, mas um que já foi ticado e depois ficou fora da
simulação continuaria registrado (e ticando) até o chunk descarregar. Teste que decide: no jogo, com um jar de teste a
100% perto de uma garagem cheia, `/tick query` parado ao lado e depois a uma distância entre a simulação e a visão
(`simulation-distance` menor que `view-distance`).

### Custo do próprio mod ao criar carros (17 Nov 2026, 0.7.1)
`/amcv fill` numa garagem do prédio largo (33-34 carros, tempo da resposta pelo RCON, inclui sorteio, checagem do
mundo e criação do carro no IV): 32 ms na primeira vez (aquecimento), depois 21, 16 e 15 ms = **~0,5 ms por carro**.
`/amcv draw` (só conferir 34 vagas): 3 ms. Isso é um custo **único por carro**, na hora de gerar; o custo contínuo
é o do IV (~0,05 ms por carro por tick, medido acima).

## Versão 0.7.2 — 9% por vaga (17 Nov 2026)
- Pedido do usuário: a chance dos carros estacionados sobe de 3% para 9% (`Draw.CHANCE_PER_BAY`). O sorteio é o mesmo
  (a vaga tem carro se o número de índice 0 < a chance): quem tinha carro com 3% continua tendo, e entram mais.
- Testes de unidade: 25 passam. `DrawTest.chancesModelsAndColours` (4.000.000 vagas): 9,02% com carro, os 8 modelos entre
  44.872 e 45.491, capô, porta-malas e portas 50%. `projectionPerCity` (492 cidades, as 57 vagas): 17.507 vagas, 1.575
  carros = **3,20 por cidade** (9,00% das vagas); cidades sem carro 46 (9,3%), com 1: 15,2%, 2: 19,5%, 3: 18,5%, 4: 11,2%,
  5: 10,2%, 6: 6,9%, 7: 3,9%, 8 a 11: 5,2% juntas.
- Servidor (mundo novo, semente 20261027, 6 prédios largos gerados): carros nas caixas 6, 4, 4, 3, 3 e 3 = 23, sorteados
  23, no lugar 23, nenhuma vaga pulada (25 linhas `(generated)` no log: 2 de estruturas vizinhas nos chunks da borda).
- O jar de teste a 100% continua existindo (`CHANCE_PER_BAY = 1.0d`, só local).

## Versão 0.7.3 — carros com a bateria descarregada (17 Nov 2026)
- Pedido do usuário. Antes: `electricPower` 12,0001 de capacidade 14 (85,7%, o padrão do IV), medido em 3 carros gerados
  (Mercedes, Camaro, Mustang). Agora `Vehicles.spawn` põe `electricPower = batteryCapacity * 0` (0%).
- **NÃO TESTADO** (o usuário pediu para ignorar os testes desta mudança): nem os testes de unidade, nem o servidor. Só foi
  compilado (`./gradlew build -x test`). Não sei se o IV recarrega o campo na primeira atualização do carro nem se o
  painel ou o jumper pack se comportam como esperado. Para conferir: `/amcv car mercedes` e
  `execute as @e[type=mts:builder_existing,limit=1,sort=nearest] run data get entity @s electricPower` (esperado 0.0).

## Versão 0.7.4 — tanque dos carros a 15% (17 Nov 2026)
- Pedido do usuário (junto com o da bateria, e na mesma instrução de ignorar os testes): `Vehicles.FUEL_FRACTION` de 0,10
  para 0,15. Quantidades: Camaro e Highwayman 1.500 mB (eram 1.000), Mustang 1.200 (800), Mercedes, Cagouille, Escargot,
  Station Wagon e Scout 2.250 (1.500).
- **NÃO TESTADO** (por pedido do usuário): só compilado (`./gradlew build -x test`); a constante dentro do jar foi lida com
  `javap`. A 0.7.3 (bateria 0%) também continua sem teste.

## Versão 0.7.5 — lava volta a render 1,0 (17 Nov 2026)
- Pedido do usuário: remover a configuração que faz o combustível ser drenado mais rápido (a lava a 0,35 do `FuelBalance`).
  Apagadas `FuelBalance` e `ClientHooks` e os ganchos em `AmcVehicles`.
- Testes: 25 de unidade passam. Servidor (mundo novo): 0 linhas "Lava as a fuel" no log (antes havia uma ao iniciar e outra ao
  parar), 0 erros do mod, `/amcv car mercedes` responde `fuel 2250/15000 mB (lava)` (15%), `mtsconfig.json` do servidor de
  teste com `"lava": 1.0` nos 5 tipos.
- **Achado**: o `config/mtsconfig.json` da instância "MAZ 2.0.0 (TESTE)" (gravado em 17 Nov 2026, 01:09) tem `gasoline`,
  `diesel` e `avgas` em **0,35**: o IV gravou a própria configuração com o valor do mod em memória (o limite documentado
  na 0.6.0). Remover o mod não desfaz isso. Pendente: voltar esses três a 1,0 (precisa do OK do usuário e do jogo fechado).
- Não testado no jogo: o consumo com a lava a 1,0 (depende do arquivo acima).

## Versão 0.7.6 — repair kit 10% mais comum (17 Nov 2026)
- Pedido do usuário: repair kit de 1,0% para 1,1% (barril; baú 2,0% para 2,2%), "para evitar competir com o motor e o jumper
  pack". Os sorteios são independentes (cada item tem o seu pool): não há competição; a mudança só ordena o repair kit
  acima dos outros dois. `ITEM_MULT = {"repairkit": 1.1}` em `tools/gen_loot.py`; só `workshop/chest.json` e
  `workshop/barrel.json` mudaram (pesos 220 e 110).
- Testes: 25 de unidade passam. Servidor (16.000 containers de cada): barril repair kit 1,22% (esperado 1,10; +1,4
  desvio), motor 0,92 (1,00), jumper pack 0,89 (1,00); baú repair kit 2,27% (2,20; +0,6), motor 2,12, jumper pack 1,93.
  Nenhum item além de 3 desvios; 0 erros de loot. A diferença de 10% é menor que o ruído de uma amostra pequena: aparece
  só na média de muitos containers (esperado por oficina média: repair kit 0,69, motor e jumper pack 0,63).

## Versão 0.7.7 — wrench no loot das oficinas; `mtsconfig.json` corrigido (17 Nov 2026)
- **`mtsconfig.json`** (pedido do usuário, jogo fechado: `ps` sem processo do jogo): backup `~/backups/mtsconfig-2026-11-17/`,
  troca de `"lava": 0.35` por `"lava": 1.0` (3 linhas: `gasoline`, `diesel`, `avgas`), conferido com `json.load`
  (lava 1,0 nos 5 tipos) e `diff` contra o backup (só essas 3 linhas). Para reverter: `cp -p` do backup sobre o arquivo.
- **Wrench** (`mts:mts.wrench`, IV base) Comum nas oficinas (baú 9,2% / barril 4,6%), como bancos e rodas. Prévia: 2,90 por
  oficina média, 1,98 por pequena, 4,44 por cidade. 25 testes de unidade passam (55 entradas de item, todas nos jars).
- Servidor (mundo novo, 0 erros de loot): `workshop/barrel` (16.000) wrench 4,92% (esperado 4,60; +1,9 desvio);
  `workshop/chest` (16.000) 8,74% (9,20; -2,0); `workshop/barrel_hard` (4.000) 4,58%; os outros itens inalterados; nenhum
  além de 3 desvios. `gas_station/barrel` (3.000): nenhum wrench.

## Versão 0.7.8 — galão sempre cheio no loot dos postos (17 Nov 2026)
- Pedido do usuário: remover o galão vazio; o galão passa a sair sempre cheio, mantendo a raridade (Incomum: baú 4,4% / barril
  2,2%). Consequência: de ~1,2 para ~0,6 galão por cidade. `GAS = [("jerrycan_full", "Incomum")]` em `tools/gen_loot.py`;
  só `gas_station/chest.json` e `gas_station/barrel.json` mudaram (um sorteio em vez de dois).
- Teste de unidade: `LootDataTest.everyPoolAddsUpToTenThousand` exigia 2 sorteios por tabela e passou a exigir 1 (o teste
  estava desatualizado, não os dados); com uma tabela de posto sem sorteio nenhum ele falha (conferido). 25 testes passam.
- `tools/sample_loot.py` agora acusa qualquer item do AMC Vehicles que não deveria sair na tabela amostrada.
- Servidor (mundo novo, 0 erros de loot): `gas_station/chest` (12.000) galão cheio 4,22% (esperado 4,40; -1,0 desvio),
  `barrel` (16.000) 2,34% (2,20; +1,2), `chest_hard` (4.000) 4,20%, `barrel_hard` (4.000) 2,38%; os 1.143 galões vistos
  tinham `jerrycanFluid: "lava"`; nenhum item indevido (nem galão vazio, nem wrench) e `workshop/barrel` (3.000) sem item indevido.
- Nota de método: uma primeira rodada passou "tabela N" como um argumento só (o zsh não separa `$t`) e o amostrador não
  imprimiu nada; foi refeita com argumentos separados.

## Versão 0.7.9 — `FuelBalance` de volta, com a lava a 0,60 (17 Nov 2026)
- Pedido do usuário: "mudar a regra do FuelBalance de novo, dessa vez para 0,60". A regra tinha sido removida na 0.7.5; as
  classes `FuelBalance` e `ClientHooks` foram restauradas do commit `36560dd` (`git checkout 36560dd -- ...`) e os ganchos
  de `AmcVehicles` religados; `LAVA = 0.60`.
- Testes: 25 de unidade passam; a constante dentro do jar (`javap -constants`) é `LAVA = 0.6d`.
- Servidor (mundo novo): log ao iniciar `[AMCV] Lava as a fuel set to 0.6 (server starting; was: diesel 1.0, avgas 1.0,
  gasoline 1.0); IV's config file is not changed`; ao parar `[AMCV] Lava as a fuel put back to IV's values (server stopped)`;
  `/amcv car mercedes` responde `fuel 2250/15000 mB (lava)`; `mtsconfig.json` do servidor de teste com `"lava": 1.0` nos
  5 tipos depois de parar.
- Não testado no jogo: o consumo real com 0,60, e o `ClientHooks` num cliente (a parte de entrar e sair de um servidor).
  O `mtsconfig.json` da instância do usuário segue em 1,0 (corrigido na 0.7.7), a menos que o IV o grave de novo.

## Versão 0.7.10 — lava a 0,45 (17 Nov 2026)
- Pedido do usuário: reduzir de 0,60 para 0,45 (`FuelBalance.LAVA`). Nada mais mudou.
- Testes: 25 de unidade passam; `javap -constants` no jar: `LAVA = 0.45d`. Servidor (mundo novo): `[AMCV] Lava as a fuel set to
  0.45 (server starting; was: diesel 1.0, avgas 1.0, gasoline 1.0)`; ao parar `... put back to IV's values (server
  stopped)`; `mtsconfig.json` do servidor de teste com `"lava": 1.0` nos 5 tipos depois de parar.
- Instância do usuário (lida, não alterada): `mtsconfig.json` com `gasoline`, `diesel` e `avgas` em 0,6, gravado pelo IV em
  17 Nov 2026, 21:33 (0.7.9). Com o mod, o valor em memória (0,45) manda; o arquivo só muda se o IV o regravar. Com o 0,6
  no arquivo, o mod guarda 0,6 como "valor do IV" e o devolve ao parar (a devolução não restaura 1,0).
- Não testado no jogo: o consumo real com 0,45.

## Versão 0.7.11 — lava a 0,35 de novo (17 Nov 2026)
- Pedido do usuário: de 0,45 para 0,35 (`FuelBalance.LAVA`); nada mais mudou.
- Testes: 25 de unidade passam; `javap -constants`: `LAVA = 0.35d`. Servidor (mundo novo): `[AMCV] Lava as a fuel set to 0.35
  (server starting; was: diesel 1.0, avgas 1.0, gasoline 1.0)`; ao parar `... put back to IV's values`; `mtsconfig.json` do
  servidor de teste com `"lava": 1.0` nos 5 tipos depois de parar.
- Não testado no jogo: o consumo real com 0,35. O `mtsconfig.json` da instância do usuário não foi alterado nesta versão.

## Versão 0.7.12 — lava a 0,05 como padrão (17 Nov 2026)
- O usuário testou um jar de teste com a lava a 0,05 (`amcvehicles-0.7.11-TESTE-100pct-lava0.05.jar`, não versionado) no
  jogo dele: "o consumo está muito rápido, funcionando normalmente" (confirmação do consumo no jogo, feita por ele). Pediu
  para ser o padrão: `FuelBalance.LAVA` de 0,35 para 0,05.
- Testes: 25 de unidade passam; `javap -constants`: `LAVA = 0.05d`. Servidor (mundo novo): `[AMCV] Lava as a fuel set to 0.05
  (server starting; was: diesel 1.0, avgas 1.0, gasoline 1.0)` e a devolução ao parar; `mtsconfig.json` do servidor de teste
  em 1,0 depois de parar.

## Versão 0.7.13 — bombas com 0 a 4 baldes (18 Nov 2026)
- Pedido do usuário: "passar de 0 a 3 para 0 a 4 de lava nas bombas". Lido como: continua 50% vazias e o máximo sobe de 3
  para 4 (`Draw.MAX_BUCKETS`): 1, 2, 3 ou 4 baldes com 12,5% cada, média 1,25 balde por bomba (era 1). Se ele quis 0 a 4 com a
  mesma chance (20% cada), é outra regra: perguntado a ele na entrega.
- Testes: 25 de unidade passam. `PumpTest.halfEmptyHalfOneToThree` (1.200.000): 0 balde 599.997 (50,0%) e 1, 2, 3, 4 baldes
  149.791, 150.063, 149.697, 150.452 (12,5% cada). `pumpsOfAStationIndependent` (200.000 postos de 8 bombas): média 9,995
  baldes (esperado 10) e variância 17,431 (esperado 17,5; antes eram 8 e 10,67); o teste foi atualizado para esses valores.
- Servidor (mundo novo, 8 áreas de posto geradas uma a uma): 34 bombas, **o tanque de cada uma bate com o sorteado (0
  diferenças)**; baldes sorteados: 0 em 20, 1 em 8, 3 em 5, 4 em 1 (uma bomba de 4 baldes, 4.000 mB, apareceu). 4 avisos
  "Can't keep up" (2 a 4 s de atraso) enquanto gerava várias cidades; nenhuma exceção. Uma primeira tentativa com 21 postos de
  uma vez derrubou o servidor de teste (sobrecarga ao gerar tudo junto; o log foi apagado por engano, então a causa
  exata não foi lida).
- Só valem para chunks novos: as bombas já geradas nos mundos do usuário não mudam.

## Versão 0.8.0 — vagas na casa de vidro e no prédio-rampa (18 Nov 2026)
- Pedido do usuário: "adicionar vaga de estacionamento na abandoned_glass_house e no prédio-rampa". Proposta em
  `docs/spots/vidro_rampa_propostas.png` (gerada com `tools/find_spots.py --bays`). Respostas dele: andares y=10 e y=0 do
  prédio-rampa (21 vagas), quantidade boa, e, na casa de vidro, uma foto que mostrou que o lugar é a sala do canto nordeste
  (não a varanda da frente que eu tinha proposto); aplicadas as 2 vagas da sala depois da confirmação dele.
- Detector: casa de vidro, as 2 vagas livres; prédio-rampa, 20 propostas por andar: y=10 10 livres, y=0 11 livres, y=5 0
  livres (por isso fora). Entram só as livres.
- Testes: 25 de unidade passam (inclui o `PlacementTest`, que põe os 8 modelos em todas as vagas nas 4 rotações, agora com
  as 23 novas). Projeção com o planejador real do AMC (5 sementes, 492 cidades): 19.704 vagas (40,0 por cidade), **1.788
  carros (3,63 por cidade, 9,07% das vagas)**; cidades com 0 carro 39 (7,9%); casa de vidro 521 cópias, 108 carros
  (0,21 por cópia); prédio-rampa 55 cópias (0,11 por cidade), 105 carros (1,91 por cópia). Antes: 3,20 por cidade e 9,3%
  sem carro.
- Servidor (mundo novo, semente 20261027): `amcs locate` achou uma casa de vidro (x -351..-312, z 434..481, rotação 3) e
  um prédio-rampa (x 172..243, z 1121..1222, rotação 3). `/amcv fill` na casa de vidro: 2 vagas, 2 carros, 0 pulados, nos
  centros esperados pelo cálculo manual do `.nbt` (x -337,5 e -333,5; eixo traseiro em z 445), nariz para o norte (a parede
  leste do modelo) e piso certo (y 69,4). No prédio-rampa: 21 vagas, 18 carros postos e 3 "occupied by a vehicle" (carros
  do sorteio real de 9% feitos na geração do mundo, o que prova que a geração também vê as vagas novas); carros do y=10 em
  y 78,4 e os do y=0 em y 68,4 (10 acima), em z a cada 4 blocos, todos com o nariz para oeste (o norte do modelo girado).
- `tools/check_bays.py` não serve para estas vagas (procura faixas compridas ao lado do carro; no prédio-rampa as marcas
  brancas são pontinhos nas bordas e na casa de vidro não há pintura): mostrou "0 cars between stripes", sem significado.
- Não testado no jogo: o aspecto visual (carro encostado em alguma coisa, a céu aberto no y=10 do prédio-rampa), a geração
  natural na casa de vidro (o `fill` a testou; o sorteio real só foi visto no prédio-rampa) e o tamanho do prédio-rampa
  (102 × 137 × 72) em desempenho com carros.

## Versão 0.8.1 — prédio-rampa sem vagas, casa de vidro mantida (18 Nov 2026)
- O usuário testou a 0.8.0: "pode manter as vagas da casa de vidro, mas do prédio-rampa pode remover". As 21 linhas
  `abandoned_ramp_building bay ...` de `spots.txt` ficaram comentadas (não apagadas), com uma nota; nada mais mudou.
- Testes: 25 de unidade passam. Projeção (planejador real do AMC, 5 sementes, 492 cidades): 18.549 vagas (37,7 por cidade),
  **1.683 carros (3,42 por cidade, 9,07% das vagas)**, cidades com 0 carro 41 (8,3%). Jar: `spots.txt` com 59 vagas.
- Servidor (mundo novo): `/amcv fill` na casa de vidro: 2 vagas, 2 carros, 0 pulados; no prédio-rampa: "abandoned_ramp_building
  has no bays yet". O log tem 12 linhas ERROR/Exception, todas anteriores ao mundo e do IV (`MTSERROR` ao ler modelos dos
  packs, como "roller2" e `engineikarus.obj`, e uma tag do Minecraft); nenhuma cita `amcvehicles`. (Uma primeira rodada
  contou esses 12 e eu apaguei o log antes de lê-lo; refiz guardando o log.)
- Só vale para chunks novos: um prédio-rampa que o mundo do usuário gerou com a 0.8.0 já pode ter carros nele (ficam).

## Preparação do 1.0.0, Fase 0 — rede de segurança (2026-11-18, ainda 0.8.1)
- Nenhuma mudança no mod: só o teste dourado novo (`GoldenDrawTest`, 26 testes no total, todos passam) e a foto da 0.8.1 em
  `docs/baseline/` (hashes dos JSON, entradas do jar, constantes, projeção, servidor). Detalhes e números em
  `docs/baseline/README.md`.
- O teste dourado foi conferido contra uma mudança de propósito (`DOOR_OPEN_CHANCE` 0,5 para 0,51): falhou na linha 205;
  desfeita a mudança, passou.
- Servidor (mundo novo): 6 caixas geradas, 4 carros = 4 sorteados = 4 no lugar, 8 bombas com a lava sorteada, `/amcv fill`
  na casa de vidro com 2 carros; nenhum ERROR ou WARN do mod no log.
