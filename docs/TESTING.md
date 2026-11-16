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

**Ainda não visto no jogo:** o resto da aparência (o capô aberto na animação, os instrumentos no painel) e o
nariz nas outras direções (oeste, norte, leste).

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

**Não visto no jogo:** a aparência na garagem (o carro inteiro dentro da vaga, sem atravessar paredes ou pilares) e
a garagem de outras rotações no mundo (só a rotação 1 foi testada no mundo; as 4 nos testes de unidade).
