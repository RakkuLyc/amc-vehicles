# AMC Vehicles — regras de trabalho (obrigatórias)

Seguem as regras do projeto Abandoned Modern City (escolha do usuário, 16 Nov 2026):
1. **Perguntar ao longo do caminho**: decisões de design, números e estilo são do usuário; com mais de uma solução,
   apresentar as opções com uma recomendação.
2. **Gerar o `.jar` a cada mudança pequena/média** e dizer o que mudou nele.
3. **GitHub** (`gh`, repositório privado `RakkuLyc/amc-vehicles`): commits frequentes e claros; atualizar o
   repositório a cada jar testável.
4. **Iterativo**: etapas pequenas e testáveis; ao fim de cada uma, resumo + próximo passo recomendado.
5. **Não passar à próxima etapa antes de o usuário testar e aprovar a atual.**
6. **Documentação sempre atualizada** (este arquivo, `README.md`, `docs/TESTING.md`) no mesmo commit da mudança; os
   valores vêm do código e dos testes, nunca da memória.
7. **O AMC não referencia o Immersive Vehicles** (garantia do README do AMC): toda a integração fica neste mod. Uma
   mudança no AMC (por exemplo, um acesso neutro a dados) só com o OK do usuário e com a documentação do AMC.

## O que é
Mod de integração (NeoForge 1.21.1, Java) entre o **Abandoned Modern City** (AMC, `abandonedmoderncity`) e o
**Immersive Vehicles** (IV, mod id `mts`) com o **Official Content Pack** (`mtsofficialpack`). Mod id `amcvehicles`,
pacote `com.amcvehicles`, pasta `Projetos Claude/AMCVehicles`. Criado em 16 Nov 2026.

Objetivo: carros abandonados aparecendo de forma aleatória e rara nos estacionamentos e garagens das estruturas do
AMC (respeitando as vagas, inclusive as subterrâneas), dentro das oficinas (nunca fora) e nos postos de gasolina (ao
lado das bombas: lá não há vaga pintada). Até a 0.4.2 só a **Mercedes 230** do Official Pack; desde a 0.5.0 (etapa 5)
**8 modelos**: Mercedes 230, Mustang 69 e Scout do Official Pack (`mtsofficialpack`) e Cagouille, Camaro, Escargot,
Highwayman e Station Wagon do **Official Automobile Pack** (OAmP, mod id `oamp`, dependência obrigatória).

## Decisões do usuário (16 Nov 2026)
- **Estado da Mercedes, sempre**: sem motor, sem rodas, sem bancos, **15% de gasolina** (10% até a 0.7.3; subiu a pedido do usuário em 17 Nov 2026), **velocímetro (km/h) e
  marcador de combustível** no painel.
- **Sorteados**: capô aberto e porta-malas aberto (cada um à parte; 50% cada, valor meu, a confirmar) e **cada
  porta lateral aberta ou fechada, 50% a esquerda e 50% a direita, à parte** (pedido do usuário, 16 Nov 2026).
- **Cores**: as 15 civis sorteadas com a mesma chance; `_police` e `_police2` **só na delegacia** (e lá só elas).
- **Raridade**: **9% por vaga** desde a 0.7.2 (17 Nov 2026; era 3% até a 0.7.1; cada vaga sorteia sozinha). Com isso a
  garagem subterrânea do prédio largo (muitas vagas) tem em média ~3 carros e as estruturas com poucas vagas, 0 ou 1.
  Projeção com as 57 vagas: **3,20 carros por cidade**, 9,3% das cidades sem carro (com 3% eram 1,07 e 38%); com as 80 vagas da 0.8.0: 3,63 por cidade, 7,9% sem carro; **com as 59 vagas da 0.8.1 (casa de vidro, sem o prédio-rampa): 3,42 por cidade**, 8,3% sem carro.
- **Comando de debug** que põe um carro em **todas as vagas da estrutura onde o jogador está** (para conferir a
  detecção das vagas).
- Nome AMC Vehicles, repositório privado, documentação em português.
- **Só chunks novos** recebem carros (16 Nov 2026, etapa 3): as áreas geradas antes de instalar o mod (ou antes desta
  versão) ficam sem carros; para elas, e para testar, `/amcv draw` aplica o sorteio real na estrutura onde se está.
- **Etapa 5 (16 Nov 2026)**: os 8 modelos com a **mesma chance**, depois a cor com a mesma chance entre as civis do
  modelo; **as variantes police são exclusivas da delegacia** (lembrado pelo usuário), e lá o sorteio é **por modelo**
  (Mercedes, Cagouille e Mustang, 1/3 cada; na Mercedes `_police` ou `_police2` a 50%); o mesmo estado da Mercedes em
  todos (sem motor, rodas e bancos, 15% de combustível desde a 0.7.4, capô, porta-malas e cada porta a 50%); painel **só velocímetro
  km/h + marcador de combustível** em todos (escolha dele, entre manter os mostradores do pack ou não).
- **Etapa 6 (16 Nov 2026)**: bombas de combustível nos postos, 2 por ilha, sempre presentes (posições aprovadas:
  `docs/spots/bombas_propostas.png`). Modelo: o usuário pediu `mts:fuelpump` (o do IV base) no lugar do
  `mtsofficialpack:fuelpump` que tinha escolhido antes. Conteúdo: primeiro "0 a 3 baldes de lava" (confirmado por ele);
  depois do balanceamento abaixo, 70% vazias e 30% com 1 balde (0.6.0); na 0.6.2 o usuário mudou para **50% vazias e
  50% com 1, 2 ou 3 baldes** (a mesma chance cada: 1/6), ~1 balde por bomba em média, ~7 por cidade; **na 0.7.13 (18 Nov
  2026, pedido do usuário: "de 0 a 3 para 0 a 4") o máximo subiu para 4: 50% vazias e 1, 2, 3 ou 4 baldes com 12,5% cada,
  1,25 balde por bomba, ~9 por cidade** (as ~7,3 bombas por cidade: 0,61 posto x 8 + 1,21 posto pequeno x 2).
- **Balanceamento do combustível (16 Nov 2026)**: o usuário mediu que 5 baldes de lava levam um carro por 6 a 7
  cidades (~1,3 cidade por balde) e pediu um balanço. Conta: com 0 a 3 baldes por bomba seriam ~11 baldes por cidade
  (~7 bombas por cidade: 0,61 posto × 8 + 1,21 posto pequeno × 2), e a lava é infinita nos lagos. Escolhas dele:
  **lava rende 0,35** como combustível de motor (cerca de 0,45 cidade por balde; 5 baldes ≈ 2 cidades; **removido na
  0.7.5**, ver "Balanceamento"), aplicado pelo mod ao iniciar; **bombas 70% vazias, 30% com 1 balde** (~2 baldes por cidade); **tanque dos carros
  continua 10%**.

## Etapas
1. **(aprovada pelo usuário, 16 Nov 2026)** Comando `/amcv mercedes`: uma Mercedes no estado pedido na frente de
   quem executa. Depois do primeiro teste (0.1.0), pedidos da 0.1.1: portas sorteadas e o marcador de combustível à
   direita do velocímetro. O usuário testou a 0.1.1 ("está tudo funcionando") e confirmou capô e porta-malas 50% e o
   combustível lava.
2. **(aprovada pelo usuário, 16 Nov 2026)** As vagas da garagem subterrânea do prédio largo
   (`abandoned_wide_building`) e o comando de debug `/amcv fill`, que enche todas as vagas da estrutura onde o
   jogador está.
3. **(aprovada pelo usuário, 16 Nov 2026)** Geração no mundo: 3% por vaga (9% desde a 0.7.2), cores sorteadas, só chunks novos,
   `/amcv draw`.
4. **(feita na 0.4.0, aguardando o teste do usuário)** As outras estruturas: imagem das vagas propostas
   (`docs/spots/etapa4_propostas.png`) aprovada pelo usuário ("Aprovar todas"), com o nariz **sorteado** nos postos
   (escolha dele) e a **farmácia grande** acrescentada por ele (vagas da frente). Ver "Vagas" abaixo. Na 0.4.1 o
   usuário apontou as garagens da **casa abandonada** e do **prédio pequeno** (proposta
   `docs/spots/garagens_propostas.png`, "Aprovar as 6"). Na 0.4.2 os carros passaram a ficar apoiados nos blocos de
   apoio do modelo (o usuário os viu meio enterrados com um jar de teste a 100%).
5. **(feita na 0.5.0, aguardando o teste do usuário)** Os 8 modelos (ver "Modelos" abaixo); `/amcv car` e
   `/amcv lineup` no lugar de `/amcv mercedes`.
6. **(feita na 0.6.0, aguardando o teste do usuário)** As bombas de combustível dos postos (`mts:fuelpump`, 10 posições)
   e o balanceamento da lava (ver "Bombas" e "Balanceamento" abaixo). Depois de testar, o usuário pediu (0.6.1) para
   **tirar os carros dos postos**: os carros estacionados ao lado das bombas atrapalhavam abastecer um carro montado.
   As 6 vagas dos postos saíram de `spots.txt` (ficam num comentário); as bombas ficam.
7. **(feita na 0.7.0, aguardando o teste do usuário)** Loot de oficinas e postos (ver "Loot de oficinas e postos").
**Preparação do 1.0.0 (desde 2026-11-18; o roteiro fica fora do repositório):** seis fases, cada uma aprovada pelo usuário
antes da seguinte. Fase 0 (rede de segurança: `GoldenDrawTest` e `docs/baseline/`) **aprovada em 2026-11-19**. Fase 1
(código morto) **feita na 0.9.0, aguardando aprovação**: saiu o nariz sorteado (ver "Vagas"); `Spots.all`,
`Spots.allPumps` e `Spots.Bay.alongZ` ficam (só os testes os usam, mas esses testes conferem as vagas e bombas reais:
escolha do usuário); texto velho corrigido. O push passa a ser feito por mim (o usuário libera a permissão).
**Pendente (16 Nov 2026):** o diagnóstico dos problemas do Immersive Vehicles (som e combustível, `docs/diagnostico_iv.md`)
foi deixado pendente pelo usuário (aguarda os testes e as 4 respostas dele; nenhuma correção proposta). A lava a 0,35 foi
removida na 0.7.5 (17 Nov 2026), mas o `mtsconfig.json` da instância dele ficou com 0,35 gravado pelo IV: ver "Balanceamento".

## Modelos (`Cars`, `Vehicles`; etapa 5)
- **Tabela** (`Cars.java`, sem classes do Minecraft): para cada modelo a chave dos comandos, o pack e o nome, as cores
  civis e as de polícia, as medidas do modelo `.obj` (carroceria atrás e à frente da origem, meia largura, altura dos
  blocos de apoio = `lift`, ponto mais alto = `top`) e as variáveis das portas e do porta-malas. Tirada dos jars por
  `python3 -I tools/car_info.py <pack.jar> <pack id> <nomes...>` e conferida contra os jars pelo `CarsTest`.

  | Chave | Pack:nome | Civis | Polícia | Comprimento | Largura | lift | top | Portas | Porta-malas |
  |---|---|---|---|---|---|---|---|---|---|
  | mercedes | mtsofficialpack:merc230 | 15 | _police, _police2 | 5,77 | 2,50 | 0,469 | 1,625 | door_l, door_r | door_trunk |
  | cagouille | oamp:cagouille | 18 | _police | 6,38 | 2,38 | 0,438 | 1,688 | door_l, door_r | door_trunk |
  | camaro | oamp:camaro | 14 | - | 6,32 | 2,45 | 0,500 | 1,193 | door_l, door_r | door_trunk |
  | escargot | oamp:escargot | 16 | - | 6,35 | 2,44 | 0,469 | 1,506 | door_l, door_r | door_trunk |
  | highwayman | oamp:highwayman | 9 | - | 5,56 | 2,25 | 0,438 | 1,250 | door_left, door_right | door_trunk |
  | station_wagon | oamp:stationmerc | 14 | - | 5,82 | 2,50 | 0,469 | 1,406 | door_fl, door_fr, door_rl, door_rr | door_trunk |
  | mustang | mtsofficialpack:fordmustang69 | 12 | _police | 6,21 | 2,97 | 0,438 | 1,813 | door_l, door_r | (nenhum) |
  | scout | mtsofficialpack:scout | 14 | - | 5,00 | 2,64 | 0,469 | 1,647 | door_l, door_r | door_tailgate (a tampa da caçamba) |

  Todos têm os blocos de apoio `cube_fl/fr/rl/rr`. O mais alto apoiado é o Mustang (2,25): `Cars.HEADROOM` = 3 blocos
  livres. Os 8 cabem em todas as vagas (59 desde a 0.8.1) nas 4 rotações (`PlacementTest.carInsideItsBayAndAlongIt`).
- **Montagem** (`Vehicles.spawn`, a antiga `Mercedes.spawn` para qualquer modelo): carro novo (dados `null`), capô,
  porta-malas e cada porta no valor sorteado; **painel**: velocímetro `instrument_car_spedometer_kph` no lugar do
  velocímetro do pack (um mostrador padrão com "spedometer" no nome), senão no do meio (pela posição `hudX`); marcador
  `instrument_car_fuelqty` no lugar do do pack ("fuelqty"), senão no primeiro à direita do velocímetro (o menor `hudX`
  acima), senão o mais perto à esquerda; os outros mostradores do pack saem (`removeIntrument`, o método do IV, com o
  erro de grafia dele). Na Mercedes isso dá o mesmo de antes (meio e direita). Peças: as padrão do pack, depois saem os
  motores, **todas as rodas** (o estepe do Escargot também) e os bancos não permanentes; ficam o porta-luvas, o assento
  invisível do porta-malas, a caçamba e o adesivo do Scout e o enfeite de espelho do Mustang. Combustível: 15% do
  `fuelCapacity` do modelo (0.7.4; Camaro e Highwayman 1.500, Mustang 1.200, os outros 2.250; eram 10%: 1.000, 800 e 1.500), o fluido do tipo do motor padrão
  dele (gasoline: lava, sem mod de gasolina), gravado com o mod real do fluido (`minecraft` para a lava; 0.6.0): com
  `""`, como o IV faz nos carros novos, um balde despejado no carro (mod `minecraft`) seria recusado pelo
  `EntityFluidTank.fill`, que só junta o mesmo fluido do mesmo mod ou `wildcard` (o que a bomba manda).
- **Sorteio** (`Draw`): índices novos, sem mudar o 0 (a vaga tem carro): 7 o modelo, 8 e 9 as portas de trás da
  Station Wagon; o 1 (cor) passa a sortear entre as cores do modelo.

- **Bateria** (0.7.3, 17 Nov 2026, pedido do usuário): os carros nascem com a **bateria descarregada** (0%;
  `Vehicles.BATTERY_FRACTION`, que multiplica a capacidade do modelo e vai para `electricPower`, o campo que o IV grava).
  O IV põe 85,7% num carro novo; medido em 17 Nov 2026 num carro gerado pelo mod: `electricPower` 12,0001 de capacidade
  14. Com isso o jumper pack (Raro no loot das oficinas) passa a ser necessário para ligar um carro. **Sem teste**: o
  usuário mandou ignorar os testes desta mudança.

## Como a Mercedes é montada (`Mercedes.spawn`, até a 0.4.2; desde a 0.5.0 `Vehicles.spawn`, ver "Modelos")
**Confirmado** (código do IV 24.0.0 descompilado + dados salvos no servidor de teste, antes e depois de reiniciar):
as peças que ficam, o combustível, os instrumentos gravados, os valores de `door_hood`/`door_trunk`, o ângulo
gravado, e que o carro nasce **destrancado** (sem a variável `locked`; o IV não guarda dono).
**Visto pelo usuário no jogo:** na 0.1.0, o nariz para o lado anunciado (sul) e o porta-malas aberto; na 0.1.1,
"está tudo funcionando" (16 Nov 2026). **Teste de unidade** (`NoseYawTest`, com a matemática de rotação do próprio
IV): o nariz aponta para sul, oeste, norte e leste com os ângulos 0, 90, 180 e 270. Se algum dia aparecer um motor
fantasma num cliente, a alternativa é pôr só as peças permanentes em vez de pôr tudo e tirar o motor.

- `new EntityVehicleF_Physics(world, null, item, null)`: dados `null` = carro novo, como de um item recém-feito. O
  pack põe `door_l`, `door_r` e `door_hood` em 1 (abertos) pelas `initialVariables`; o mod então põe `door_hood`,
  `door_trunk`, `door_l` (a porta do lado +x do modelo, a esquerda de quem está sentado) e `door_r` no valor sorteado.
- Instrumentos (antes de pôr o carro no mundo, para irem nos dados iniciais): o carro tem 3 lugares no painel (0
  esquerda, 1 meio e maior, 2 direita); velocímetro `instrument_car_spedometer_kph` no 1 (meio), marcador
  `instrument_car_fuelqty` no 2 (à direita do velocímetro, pedido do usuário); o 0 fica vazio.
- `world.spawnEntity` e `addPartsPostAddition(null, null)`: põe as peças padrão do pack: o motor
  `enginemercedesm102` e as permanentes (porta-luvas `gloveboxscout`, slot 11, e o "assento" invisível em pé do
  porta-malas `mts:invisible_standing`, slot 9, que o pack usa para carregar coisas no porta-malas). O merc230 **não
  tem rodas nem bancos padrão**. O mod tira o motor (`removePart`, avisando os clientes).
- Combustível: `fuelCapacity` 15.000 mB → **1.500 mB**. O fluido é o que o próprio IV escolheria para o motor do
  pack: o mais potente do tipo `gasoline` na config de combustíveis do IV que existe no jogo (padrão do IV: lava 1,0,
  gasoline 1,0, ethanol 0,85). **Sem mod de gasolina, é lava** (como em qualquer carro novo do IV).
- Rotação: o ângulo do IV é o do Minecraft com o sinal trocado (`WrapperEntity.getYaw`); `Vehicles.spawn` (antes `Mercedes.spawn`) recebe o
  ângulo do Minecraft para onde o **nariz** aponta (0 sul, 90 oeste, 180 norte, 270 leste). A origem do modelo é o
  eixo traseiro: a carroceria vai de ~1,3 bloco atrás dele a ~4,15 à frente (rodas em z 0 e 3,19 do `merc230.json`).
- **Altura (0.4.2, o usuário viu os carros meio enterrados)**: sem rodas, o merc230 se apoia nos 4 blocos de apoio do
  modelo (`cube_fl/fr/rl/rr`, que o pack mostra quando falta a roda daquele canto: `part_present_N` = 0 e perto do
  chão), com a base em y -0,46875 do modelo (as caixas de colisão mais baixas também: y -0,37, altura 0,2). Com a
  origem no piso o carro afundava 0,47; agora a origem fica **0,46875 acima do piso** (era `Placement.CAR_LIFT`; desde a 0.5.0 a altura de
  cada modelo, `Cars.Model.lift`, usada por `Placement.car` e pelo `/amcv car`). O ponto mais alto do modelo é 1,625 (a giroflex das cores de polícia; a carroceria 1,5625), então o
  carro chega a 2,09 sobre o piso e precisa de **3 blocos livres** (`Placement.HEADROOM`), não 2.

## Vagas (`Spots`, `Placement`, `AmcWorld`)
- **Lista**: `src/main/resources/amcvehicles/spots.txt`, uma linha por vaga:
  `<modelo> bay <x0> <y> <z0> <x1> <z1> <nariz>`, em coordenadas do `.nbt` do modelo (as do structure block). O
  modelo é a coluna `model` do `index.txt` do AMC, senão o nome do arquivo: as variantes de cor dividem as vagas.
  x0..x1, z0..z1 = as células da vaga; y = a camada do piso; nariz = para onde o nariz aponta no `.nbt` (N = -Z,
  S = +Z, E = +X, W = -X). O carro fica no meio da vaga (o corpo centrado: o eixo traseiro 1,425 atrás do meio).
- **Detecção** (`tools/find_spots.py <arquivo.nbt> <y do piso> [--png] [--out] [--model]`, Python, fora do mod):
  vaga = o piso entre duas faixas pintadas paralelas (linhas retas do bloco da faixa, 4+ de comprimento) com 2 a 5
  células entre elas e 6+ de sobreposição; precisa de piso e 3 blocos livres em todas as células (senão sai como
  BLOCKED e não entra na lista). O nariz vai para a ponta fechada (o carro entra de frente): a outra ponta, com piso e
  altura livre além dela, é o corredor. A imagem (`--png`) mostra o piso, as faixas, as vagas (verde; vermelho =
  bloqueada), o carro (azul) e o nariz (ponto vermelho). Imagens aprovadas em `docs/spots/`.
- **Nariz sorteado: removido na 0.9.0** (2026-11-19, aprovado pelo usuário na Fase 1 da preparação do 1.0.0). Até a 0.8.1 o
  nariz podia ser `X` ou `Z` em `spots.txt` (ao longo desse eixo, o sentido sorteado por carro: `Draw.Car.flip`, 50%,
  índice 6 do sorteio; `Spots.Bay.oriented`); só as vagas dos postos o usavam, e elas saíram na 0.6.1. Hoje o parser só
  aceita N, S, E, W. O índice 6 fica livre (os outros não mudam: o teste dourado deu o mesmo resultado). Para voltar as
  vagas dos postos (comentadas em `spots.txt`): um nariz fixo para cada uma, ou o código da 0.8.1 do histórico do git.
- **Estruturas com vagas** (a lista da 0.4.1, 63 vagas em 12 modelos; **hoje 59 em 11**: sem os postos desde a 0.6.1 e com a
  casa de vidro desde a 0.8.0): prédio largo 34 (faixas pintadas, detectadas); e, escritas
  à mão e conferidas (`find_spots.py --bays`): oficina média 2 e pequena 2 (dentro, uma por box, nariz para o fundo),
  posto 4 e posto pequeno 2 (ao lado das ilhas das bombas, ao longo delas, nariz sorteado), farmácia grande 4 (vagas da
  frente entre as faixas amarelas gastas, nariz para a loja, até a sebe da fachada), delegacia 4 (vagas da frente entre
  as linhas brancas gastas, nariz para o prédio, só cores de polícia), casa com garagem 1, casa moderna com garagem 2 e
  casa suburbana 2 (dentro da garagem), casa abandonada 2 (a sala-garagem da frente, sem parede na frente) e prédio
  pequeno 4 (o térreo de pilotis: 2 na entrada, 2 ao fundo). As coordenadas e o porquê de cada uma estão nos
  comentários de `spots.txt`.
  Imagem das vagas aplicadas, gerada de `spots.txt`: `docs/spots/etapa4_vagas.png` (a proposta aprovada, sem a
  farmácia grande e com os narizes do posto ainda fixos: `docs/spots/etapa4_propostas.png`). Olhados e sem vagas: a
  torre (a grade de concreto branco é o piso do saguão), o prédio azulado e o apocalíptico (desenhos do piso), o prédio
  das rampas (salas no térreo; ganhou vagas na 0.8.0 e as perdeu na 0.8.1, ver abaixo). Mapas de piso coberto/aberto da camada do chão das outras 9 casas, da farmácia pequena
  e do subsolo da delegacia (`docs/spots/levantamento.png`): nenhuma garagem ou estacionamento evidente (o subsolo da
  delegacia são salas pequenas); nas casas de piso elevado (mansão, piscina, torre, vitoriana, grande) a camada do chão
  sai bloqueada e o mapa não é conclusivo. O detector só vê faixas pintadas inteiras: não acha vagas de pintura gasta
  (a farmácia grande e a delegacia foram vistas a olho) nem garagens sem pintura. O usuário acrescentou a farmácia
  grande e, na 0.4.1, a casa abandonada e o prédio pequeno (que o levantamento não tinha visto como garagens); outras
  estruturas com vagas entram quando ele apontar.
- **Prédio-rampa REMOVIDO na 0.8.1 (18 Nov 2026)**: o usuário testou a 0.8.0 e pediu para manter as vagas da casa de vidro e
  tirar as do prédio-rampa. As 21 linhas ficam comentadas em `spots.txt` (para voltar). Hoje: **59 vagas em 11 modelos**, 3,42
  carros por cidade, 8,3% sem carro. O texto abaixo é o histórico da 0.8.0.
- **Casa de vidro e prédio-rampa (0.8.0, 18 Nov 2026, pedido do usuário; proposta `docs/spots/vidro_rampa_propostas.png`)**:
  **casa de vidro** (`abandoned_glass_house`) 2 vagas na sala do canto nordeste (a da foto que ele mandou: aberta para o
  oeste entre dois pilares em x 32, com bigorna, baú e barris na parede norte): `35 0 12 41 14 E` e `35 0 16 41 18 E`,
  nariz para a parede leste, corredor em z 15; uma terceira não cabe (pilar e barril em z 20). A primeira proposta minha
  (a varanda da frente, 3 vagas) estava no lugar errado: ele mandou a foto para dizer onde era. **Prédio-rampa**
  (`abandoned_ramp_building`, estrutura `special`, ~0,11 cópia por cidade) 21 vagas de 3 × 7 no salão comprido (x 8..92,
  z 49..57), nariz para a parede norte (N), propostas a cada 4 células a partir de x 10 e mantidas só as que o detector
  deu como livres (as outras têm entulho): **10 no andar y=10** (a céu aberto, marcas brancas e linha amarela) e **11 no
  térreo y=0** (liso); o andar y=5 não tem nenhuma que caiba. Escolha dele: y=10 e y=0, 21 vagas. Com 9% são ~1,9 carro por
  prédio-rampa e ~0,2 por casa de vidro. Total: **80 vagas em 12 modelos** (eram 57 em 10).
- **Prédio largo** (`abandoned_wide_building`, garagem com o piso na camada 0 do `.nbt`, 8 camadas enterradas, 7 de
  altura livre; faixas de `white_concrete`, linhas amarelas no corredor): 36 vagas achadas, **34 na lista**; as 2 da
  fileira sul em x 43..45 e 47..49 ficam de fora (a escada está nelas). Vagas de 3 × 8 (fileira norte, nariz N) e
  3 × 9 (fileira sul, nariz S). Imagem: `docs/spots/abandoned_wide_building.png`.
- **Do `.nbt` ao mundo** (`Placement.car`): o mesmo giro do AMC (`Structures.Def.template`, lido ao contrário): a caixa
  dos blocos sólidos começa em ox, oy, oz do `.nbt` e mede w × h × d; gira `(rot do lote + turn do modelo) & 3`
  quartos de volta no sentido horário; vai para o canto do lote (x0, z0); o piso da camada y fica em
  `base + y - oy - ground`, sendo `base` = a altura do chão da cidade naquela coluna (`ChunkPlan.h2 / 2 - 1`, como o
  `ChunkBuilder` do AMC) e o carro fica em cima dele.
- **No mundo** (`AmcWorld`): a estrutura de uma coluna vem do plano do AMC (`Plans.get(level).chunk(cx, cz).lot[i]`).
  Antes de pôr um carro, todas as colunas sob o corpo (5,45 × 2) precisam de piso (bloco com colisão) e 3 blocos sem
  colisão em cima (`Placement.HEADROOM`; 2 até a 0.4.1) (ar, grama, cipós; no primeiro, um bloco fino de até 1/4 de altura, como o tapete de musgo do mato
  do AMC, `AmcWorld.THIN`) e nenhum veículo do IV ali (um carro de um `fill` anterior: não empilha); senão a vaga é
  pulada e o motivo vai na resposta do comando e no log.
  As estruturas postas com `/amcs spawn` não estão no plano: o `/amcv fill` não as reconhece.

## Geração no mundo (`Draw`, `Generation`, `Pending`; etapa 3)
- **Sorteio** (`Draw.car`): função pura da semente do lote (a cópia da estrutura; o AMC a tira da semente do mundo) e
  da **posição da vaga** no `.nbt` (`Spots.Bay.key`: x0, y, z0; não a ordem em `spots.txt`, para que acrescentar ou
  reordenar vagas de um modelo não mude o sorteio das outras), com um sal próprio (SplitMix64): a mesma vaga do mesmo mundo dá sempre o mesmo
  resultado, em qualquer ordem de carregamento. **9% por vaga** (`CHANCE_PER_BAY`; 3% até a 0.7.1); depois, cada um à parte: cor (as
  15 civis com a mesma chance; na delegacia, `type` `police_station`, só as 2 de polícia), capô 50%, porta-malas 50%,
  porta esquerda 50%, porta direita 50%. As chances moram em `Draw` e as cores em `Cars` (sem classes do Minecraft, para os
  testes).
- **Dono**: um carro pertence ao chunk da sua origem (o eixo traseiro).
- **Quando**: no `ChunkEvent.Load` de um chunk **recém-gerado** (`isNewChunk`; o evento roda na thread principal,
  depois de o chunk virar `LevelChunk` com status FULL, conferido no código do NeoForge 21.1.219,
  `ChunkStatusTasks`), o mod sorteia os carros daquele chunk (as estruturas com vagas que têm coluna nele, pelo
  `ChunkPlan.lot` do AMC); se houver algum, o chunk vai para `Pending` (salvo no mundo,
  `data/amcvehicles_pending.dat`). **Nunca dentro da geração do AMC.**
- **Pôr** (`Generation.onLevelTick`, a cada 10 ticks): o mod percorre o próprio `Pending` (não uma fila alimentada
  pelos carregamentos: assim pega também um chunk que cai abaixo de completo e volta na borda da visão de um jogador
  sem um novo evento de carregamento, e os pendentes depois de reiniciar); para cada chunk pendente que é um chunk
  completo carregado, cada carro dele espera até todos os chunks sob o corpo serem chunks completos carregados
  (`getChunkNow`, que nunca força um carregamento); então `Generation.place` confere o mundo (`AmcWorld.blocked`: piso,
  3 de altura livre (`Placement.HEADROOM`), nenhum veículo do IV, `mts:builder_existing`, ali) e põe o carro, ou o descarta de vez se estiver
  bloqueado (por exemplo, um baú sorteado pelo AMC dentro da vaga). Quando nenhum carro do chunk espera mais, ele sai
  de `Pending`. Num chunk com um carro esperando, os outros já resolvidos não são tentados de novo na mesma sessão
  (sem log repetido). A checagem de "veículo ali" impede carro duplicado se o servidor cair entre pôr o carro e
  salvar.
- **Limite conhecido**: `Pending` é salvo com o mundo; se o chunk novo for salvo (ao descarregar) e o servidor cair
  antes de o mundo salvar `Pending`, os carros daquele chunk se perdem (ele não é "novo" de novo).
- **Log**: cada carro posto (`[AMCV] <modelo><cor> in <estrutura> bay <n> at x y z (generated)`) e cada vaga
  descartada, com o motivo.
- **Projeção** (`DrawTest.projectionPerCity`: o planejador real do AMC num terreno plano, 5 sementes, 492 cidades,
  com o sorteio real; 0.4.1, as 63 vagas): 40,5 vagas por cidade, **1,22 carro por cidade**; 32,9% das cidades sem
  carro. Por cópia: prédio largo 0,92, delegacia 0,15, posto 0,13, farmácia grande 0,12, prédio pequeno 0,11, casa
  abandonada, casa moderna e suburbana 0,07, posto pequeno e oficina pequena 0,06, casa com garagem 0,04, oficina média
  0,03. (0.4.0: 1,01 por cidade; até a 0.3.0, só o prédio largo: 0,32.) **0.6.1, sem as 6 vagas dos postos (57 vagas
  em 10 modelos)**: 35,6 vagas por cidade, **1,07 carro por cidade**, 38,0% das cidades sem carro.

## Bombas (`FuelPumps`, `Generation`; etapa 6)
- **Modelo**: `mts:fuelpump`, do IV base (`decor.type` `fuel_pump`, tanque de 15.000 mB; o do Official Pack é igual
  por dentro). Posições em `spots.txt` (linhas `<modelo> pump <x> <y> <z> <frente>`, coordenadas do `.nbt`, y = a
  camada sobre o piso da ilha): posto 8 (2 em cada ilha: x 15 e 17 / 27 e 29, z 9 e 16, frente S), posto pequeno 2
  (x 8 e 12, z 9, frente N). Conferidas por `find_spots.py --bays` e pelo `PumpTest` (piso embaixo, a célula e a de
  cima livres, em toda cor e rotação; fora das vagas).
- **Lava**: `Draw.buckets`, função pura da semente da cópia e da posição da bomba (`Spots.Pump.key`, com uma marca que
  nunca coincide com a chave de uma vaga): 50% vazias (`Draw.PUMP_EMPTY_CHANCE`), as outras 1 a `MAX_BUCKETS` (4 desde a 0.7.13; 3 antes) baldes
  (`MAX_BUCKETS`) com a mesma chance (o índice 0 decide se tem, o 1 quantos); independentes entre as bombas de um posto
  (testado).
- **Geração**: como os carros. Um chunk novo com bomba entra em `Pending`. A bomba só é posta quando o chunk dela
  **e os 8 vizinhos** são chunks completos carregados (`Generation.pumpReady`), porque o `NeighbourRepair` do AMC
  repõe os blocos do plano (a célula da bomba é ar no `.nbt`) num chunk já construído quando um vizinho é gerado
  depois. No teste, uma bomba posta à mão num chunk com o vizinho ainda por gerar **sobreviveu** à geração do vizinho
  (hipótese: para um chunk ficar completo os vizinhos já passaram pela etapa em que o AMC repara, e não a repetem); a
  regra fica como margem de segurança (custo: a bomba aparece quando os vizinhos estão prontos). `placePump` também
  não põe nada se os chunks em volta não estão prontos (o `/amcv draw` numa área não carregada não força
  carregamento), nem onde já há um bloco do IV ("already there"), nem onde a célula não é ar, planta, cipó, teia ou
  bloco fino (até 1/4, como um tapete; a mesma regra do `PumpTest`), nem sem piso. Nas 10 variantes de cor, as células
  das bombas são ar no `.nbt` (conferido).
- **Como a bomba é posta**: o `WrapperWorld.setBlock` do IV sem jogador só põe o bloco, sem a parte que guarda o estado (o tile entity).
  `FuelPumps.place` faz o que o IV faz para um jogador: põe o bloco do IV (o `BuilderBlock` do item, achado no mapa
  `BuilderBlock.blockMap`), cria o tile entity com dados que levam o ângulo (`angles`, lido pelo construtor antes de
  medir a caixa da bomba) e o tanque (`currentFluid` `lava`, `currentFluidMod` `minecraft`, o que o IV grava quando um
  jogador despeja um balde, e `fluidLevel`), e o liga ao bloco (`BuilderTileEntity.setTileEntity`). Dois membros do IV
  são `protected` e são usados por reflexão; funcionou no NeoForge (testado). Alternativa, se um dia falhar: pôr o
  bloco e carregar no block entity um NBT, que o IV reconstrói sozinho (`lastLoadedNBT`).

## Balanceamento (`FuelBalance`: 0,35 na 0.6.0 a 0.7.4; removido na 0.7.5; 0,60 na 0.7.9; 0,45 na 0.7.10; 0,35 na 0.7.11; **0,05 desde a 0.7.12**)
- **0.7.12 (17 Nov 2026)**: o usuário testou um jar com a lava a 0,05 ("o consumo está muito rápido, funcionando normalmente.
  Gostei dessa maneira, quero que seja o padrão"): só a constante `LAVA` (0,35 para 0,05). Cada balde rende 5% do normal do
  IV: ~0,07 cidade por balde (pela medida dele de ~1,3 a 1,0), ~14 baldes por cidade de viagem; as bombas dão ~7 baldes
  por cidade e os galões ~0,6, então o combustível fica bem escasso (nota ao usuário: talvez mais lava nas bombas). O tanque
  de 15% de um carro (2.250 mB) rende ~0,15 cidade. O texto abaixo vale com 0,05.
- **0.7.11 (17 Nov 2026, pedido do usuário: "diminuir novamente para 0,35")**: só a constante `LAVA` (0,45 para 0,35): cada
  balde rende 35% do normal do IV, ~0,45 cidade por balde (pela medida dele de ~1,3 a 1,0; 5 baldes ≈ 2,3 cidades). O texto
  abaixo vale com 0,35.
- **0.7.10 (17 Nov 2026, pedido do usuário: "reduzir para 0,45")**: só a constante `LAVA` mudou (0,60 para 0,45): cada balde
  rende 45% do normal do IV, ~0,6 cidade por balde (pela medida dele de ~1,3 a 1,0; 5 baldes ≈ 2,9 cidades). O texto
  abaixo vale com 0,45. Na instância dele o `mtsconfig.json` estava com 0,6 gravado pelo IV (17 Nov 2026, 21:33); com o mod
  o valor efetivo é o do mod (0,45) e o arquivo só muda se o IV o regravar com o mundo aberto.
- **0.7.9 (17 Nov 2026, pedido do usuário: "mudar a regra do FuelBalance de novo, dessa vez para 0,60")**: as classes
  `FuelBalance` e `ClientHooks` voltaram do histórico do git (commit `36560dd`) com `LAVA = 0.60` e os ganchos em
  `AmcVehicles`; o resto do texto abaixo vale com 0,60 no lugar de 0,35. Cada balde rende 60% do normal do IV: ~0,8 cidade
  por balde (pela medida dele de ~1,3 a 1,0; 5 baldes ≈ 3,9 cidades). Mesmo comportamento: só na memória ao iniciar o
  servidor e ao entrar num servidor, valores do IV de volta ao parar/sair, `gasoline`, `diesel` e `avgas`.
  **Aviso**: se o IV gravar o `mtsconfig.json` com o mundo aberto (controles ou tela de configuração dele), o 0,60 vai
  para o arquivo e fica depois de tirar o mod (aconteceu com o 0,35 em 17 Nov 2026 e foi corrigido à mão).
- (Histórico abaixo: a remoção da 0.7.5 e o texto original.)
- **Removido a pedido do usuário** ("remover a configuração que faz o combustível ser drenado mais rápido"): apagadas as
  classes `FuelBalance` e `ClientHooks` e os ganchos em `AmcVehicles`; o mod não mexe mais no consumo (a lava volta a
  1,0 nos tipos de combustível do IV, ~1,3 cidade por balde pela medida dele). **Mas o arquivo ficou sujo**: em 17 Nov
  2026, 01:09, o `config/mtsconfig.json` da instância "MAZ 2.0.0 (TESTE)" estava com `gasoline`, `diesel` e `avgas` em 0,35
  (o IV gravou a configuração com o valor do mod em memória, o limite descrito abaixo). Remover o mod não desfaz isso:
  o arquivo precisa voltar a `"lava": 1.0` nesses três tipos (`furnace` e `brewing_stand` já estão em 1,0).
- **Arquivo corrigido** em 17 Nov 2026 (com o jogo fechado, a pedido do usuário): `config/mtsconfig.json` da instância
  "MAZ 2.0.0 (TESTE)", as 3 linhas `"lava": 0.35` (`gasoline`, `diesel`, `avgas`) voltaram a `1.0`; backup em
  `~/backups/mtsconfig-2026-11-17/mtsconfig.json`; a única diferença para o backup são essas 3 linhas.
- Como era: (texto original abaixo)
- Ao iniciar um servidor (`ServerAboutToStartEvent`, o mundo single-player também) e, num cliente, ao entrar num
  servidor (`ClientHooks`, só carregada no cliente), o mod põe a potência da lava em **0,35** nos tipos de combustível
  de motor do IV que a aceitam (`gasoline`, `diesel`, `avgas` por padrão; `furnace` e `brewing_stand`, que não são de
  motor, ficam como estão: a primeira rodada da 0.6.0 tinha mudado esses também). Só na memória: o IV grava o
  `config/mtsconfig.json` no fim do carregamento do jogo (no cliente, `FMLLoadCompleteEvent`, que roda em paralelo com
  os outros mods), então o valor é posto depois disso; ao parar o servidor (`ServerStoppedEvent`) e ao sair de um
  servidor (cliente) os valores do IV voltam (`FuelBalance.restore`). O motor do IV divide o consumo pela potência
  (`PartEngine`: `getTotalFuelConsumption() * fuelUsageFactor / fuelFactor`, lida a cada uso), então 0,35 faz cada balde
  render ~1/3 e vale na hora. O log diz `[AMCV] Lava as a fuel set to 0.35 (...; was: ...)` e, ao parar,
  `... put back to IV's values`. **Limite**: o IV também grava o arquivo durante a partida quando o jogador muda os
  controles ou a tela de configuração dele (`ControlSystem`, `GUIConfig` chamam `saveToDisk`); uma gravação dessas,
  com o mundo aberto, leva 0,35 para o `mtsconfig.json` (para desfazer: editar o arquivo). O usuário aprovou a lava a
  0,35 citando `fuels.gasoline.lava`; o mod aplica também a `diesel` e `avgas` (senão um motor a diesel seria uma brecha;
  a confirmar com ele).

## Loot de oficinas e postos (`tools/gen_loot.py`; etapa 7, 0.7.0, 16 Nov 2026)
- **Pedido**: loot para a oficina (peças de carro, blowtorch, repair kit, jumper pack) e para o posto (galão de
  combustível vazio e cheio), **complementando** o AMC TACZ Loot, sem substituir nada. O usuário sugeriu os itens e as
  raridades de blowtorch (Incomum), repair kit e jumper pack (Raro) e dos galões (Épico e Incomum, depois **os dois
  Incomum**: "1 balde não faz tanta diferença"); as de bancos, rodas (Comum) e motor (Raro) são propostas minhas que ele
  aprovou, junto com "aumentar as chances no geral": **todas x2** (aprovado em 16 Nov 2026).
- **Mecanismo**: um modificador de loot `neoforge:add_table` por tabela do AMC (`abandonedmoderncity:chests/<tema>/<tier>`),
  o mesmo do AMC TACZ Loot; vários mods somam à mesma tabela e a lista `global_loot_modifiers.json` de cada mod é lida.
  Temas `workshop` (as duas oficinas, todas as cores) e `gas_station` (os dois postos); tiers `chest`, `chest_hard`,
  `barrel`, `barrel_hard` (o difícil usa a mesma tabela do comum: como no AMC TACZ Loot, a chance de item não sobe no
  difícil); **vasos não recebem nada**. 12 tabelas em `data/amcvehicles/loot_table` (4 de loot e 8 de escolha em `pick/`)
  e 8 modificadores em `data/amcvehicles/loot_modifiers`. O mod não substitui nenhuma tabela.
- **Formato (o do AMC TACZ Loot)**: cada item tem um sorteio próprio e independente: peso em 10.000 e um "vazio" com o
  resto; `quality` = 25% do peso (a Sorte funciona como lá, o `/loot` do console não aplica Sorte).
- **Chances por container** (classes do AMC TACZ Loot x2; barril = metade do baú): Comum 9,2% baú / 4,6% barril; Incomum
  4,4% / 2,2%; Raro 2,0% / 1,0%. Tudo no topo do `gen_loot.py` (`BASE_CHEST`, `FATOR`, `BARREL_MULT`).

  | Item | Classe | Onde | Detalhe |
  |---|---|---|---|
  | Banco | Comum | oficinas | 4 modelos com a mesma chance, depois a cor (34 ids): `carseat` (7 cores), `sportseat` (7), `vintageseat` (10) e o genérico `seat` (10 cores, ids sem `_`: `mts:mtsofficialpack.seatblack`...; incluído a pedido do usuário em 16 Nov 2026); fora o `seat_harness` (avião) |
  | Roda | Comum | oficinas | **só offroad e vintage** (pedido do usuário, 16 Nov 2026), com a mesma chance, depois a variante (`wheeloffroad_default`; `wheelvintage_lightblue`, `_mint`, `_sinty`: 4 ids); 1 por vez; sem `wheelvintage_police` (polícia é exclusiva da delegacia); diâmetro 1,0, aceito pelos 8 carros (0,4 a 1,0); as rodas pequena, média e grande não entram |
  | Motor | Raro | oficinas | os 5 motores dos 8 carros (M102, FE428, AMC I4, Escargot I6, Camaro V8) |
  | Wrench (chave) | Comum | oficinas | `mts:mts.wrench`, do IV base (0.7.7, pedido do usuário em 17 Nov 2026; nas oficinas, não nos postos) |
  | Blowtorch | Incomum | oficinas | `mtsofficialpack:blowtorch` (conserta 50) |
  | Repair kit | Raro, **x1,1** | oficinas | `mtsofficialpack:repairkit` (conserta carro destruído); a chance é a do Raro mais 10% (**baú 2,2% / barril 1,1%**, `ITEM_MULT` no `gen_loot.py`; pedido do usuário em 17 Nov 2026, 0.7.6) para ele ficar um pouco mais comum que o motor e o jumper pack. Os sorteios são independentes: um item não "compete" com outro, isto só os ordena |
  | Jumper pack | Raro | oficinas | `mts:jumperpack` |
  | ~~Galão vazio~~ | removido | - | saiu do loot na 0.7.8 (17 Nov 2026, pedido do usuário); o jogador fabrica um (7 lingotes de ferro e um balde) e o enche numa bomba |
  | Galão cheio | Incomum | postos | `mts:jerrycan` com `{jerrycanFluid:"lava"}` em `minecraft:custom_data` (o que o IV grava ao encher na bomba: 1.000 mB, 1 balde) |
- **Esperado por estrutura** (containers medidos em `docs/containers.json` do AMC TACZ Loot; barril difícil conta como
  barril): oficina média (1 baú, 61 barris) banco, roda e wrench 2,9, blowtorch 1,4, motor e jumper 0,63, repair kit 0,69; oficina
  pequena (2 baús, 39 barris) 2,0, 0,95, 0,43; posto (4 baús, 26 barris) 0,75 galão cheio; posto pequeno (1 baú, 3
  barris) 0,11. **Por cidade** (cópias por cidade do `DrawTest`): 4,4 bancos, 4,4 rodas e 4,4 wrenches, 2,1 blowtorch, 0,97 de motor,
  de repair kit e de jumper pack, 0,59 galão cheio (0.7.8: só o cheio; antes eram 0,59 vazios + 0,59 cheios).
- **Ids**: `mts:<pack>.<nome><variante>` (ex.: `mts:mtsofficialpack.carseat_grey`, `mts:mts.jerrycan`); o gerador confere
  cada id contra o modelo de item nos jars de `libs/` e o `LootDataTest` também.
- **Uso**: `python3 -I tools/gen_loot.py` (prévia), `--write` regrava `src/main/resources/data/...`.
  `python3 -I tools/sample_loot.py <tema>/<tier> [N]` amostra no servidor de teste e compara com o esperado.

## Arquitetura
```
AmcVehicles    o @Mod: registra os comandos e os eventos (geração, FuelBalance ao iniciar e parar o servidor,
               ClientHooks no cliente)
Cars           os 8 modelos: cores, medidas, portas, porta-malas (puro; conferido contra os jars)
Vehicles       monta um carro abandonado de qualquer modelo (estado, painel, peças, combustível)
FuelPumps      põe uma bomba de combustível do IV (sem jogador) com lava
Spots          as vagas de spots.txt, por modelo (sem classes do Minecraft)
Placement      vaga do .nbt -> posição, nariz e colunas do carro no mundo (sem classes do Minecraft; testado)
AmcWorld       lê o plano do AMC (a estrutura de uma coluna) e confere se o carro cabe no mundo
Draw           o sorteio dos carros (9% por vaga, modelo, cor, capô, porta-malas, portas), puro
Generation     a geração: sorteia ao gerar um chunk, põe no tick quando os chunks sob o carro (ou em volta da
               bomba) estão prontos
Pending        os chunks com carro sorteado ainda não posto (salvo no mundo; percorrido a cada 10 ticks)
FuelBalance    a lava a 0,05 como combustível de motor do IV, só na memória (aplica ao iniciar, devolve ao parar)
ClientHooks    o mesmo do FuelBalance no cliente, ao entrar e sair de um servidor (só carregada no cliente)
AmcvCommands   /amcv (op 2): comandos de teste
```

## Comandos (op 2)
- `/amcv car <modelo> [cor] [capô porta-malas]` (0.5.0; até a 0.4.2 `/amcv mercedes`): um carro com o eixo traseiro
  3 blocos à frente de quem executa, apoiado nos blocos de apoio, o nariz para onde ele olha (arredondado para N, S, L
  ou O). Modelo pela chave (`mercedes`, `cagouille`, `camaro`, `escargot`, `highwayman`, `station_wagon`, `mustang`,
  `scout`); cor sem o `_` (as de polícia também); capô e porta-malas `true`/`false`; o que faltar é sorteado (as portas
  sempre). Do console: `execute positioned X Y Z rotated YAW 0 run amcv car ...`. Responde com a posição, o modelo e a
  cor, o capô, o porta-malas, cada porta, o combustível, o fluido, os mostradores usados, as peças tiradas e as que
  ficaram.
- `/amcv lineup` (0.5.0): os 8 modelos lado a lado (4 blocos entre eles, para a direita de quem olha), cor civil
  sorteada, e depois as 4 viaturas (`mercedes_police`, `mercedes_police2`, `cagouille_police`, `mustang_police`):
  para olhar altura, portas e painel de todos. Responde uma linha por carro.
- `/amcv pump [baldes]`: uma bomba de combustível (`mts:fuelpump` desde a 0.6.0) 3 blocos à frente, a frente dela para
  quem executa, com tantos baldes de lava (0 a 15).
- `/amcv fill` e `/amcv draw` também põem as bombas da estrutura (o `fill` sorteia a lava na hora, com a chance da
  geração; o `draw` usa a lava sorteada da cópia e lista cada bomba com o chunk dela).
- `/amcv fill`: um carro em **cada vaga** da estrutura do AMC onde quem executa está (dentro do lote dela, em
  qualquer altura; na garagem também); modelo, cor, capô, porta-malas e portas sorteados (na delegacia, as viaturas).
  Responde com a estrutura, o canto da caixa, a rotação, a altura do chão, quantas vagas, quantos carros e as vagas
  puladas com o motivo. Para tirar os carros: `/kill @e[type=mts:builder_existing,distance=..80]`.
- `/amcv draw`: o **sorteio real** (o mesmo da geração) na estrutura onde quem executa está: põe agora os carros
  sorteados que ainda não estão lá (num chunk gerado com o mod eles já estão: a resposta diz "occupied by a vehicle").
  Responde com a semente da cópia, as vagas, quantas sorteadas, quantas postas agora, os chunks pendentes e,
  para cada vaga sorteada, a cor, a origem (eixo traseiro), os chunks sob o corpo e o que aconteceu.

## Build e teste
- Build (Java 21; o Java padrão do sistema é o 27, que o Gradle não aceita):
  `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build -Dorg.gradle.java.installations.paths=$JAVA_HOME`
  → `build/libs/amcvehicles-<versão>.jar`.
- `libs/` (não versionada) precisa de `Immersive Vehicles-1.21.1-24.0.0.jar`, `MTS Official Pack-1.21.1-V29.jar`,
  `OAmP-1.21.1-V3.jar` (cópias em `AbandonedModernCity/libs/`; os dois packs para o `CarsTest`) e `abandonedmoderncity-0.1.0.jar` (o jar do AMC, de
  `AbandonedModernCity/build/libs/`; o usado na 0.2.0 é o de 16 Nov 2026, 02:40). O build compila contra eles e os
  testes usam o código puro do AMC (estruturas e `Def.code`); nenhum vai dentro do jar deste mod.
- Testes de unidade: `./gradlew test` (mesmo `JAVA_HOME`; ~15 s, o AMC carrega as 52 estruturas).
  26 testes desde 2026-11-18; o `GoldenDrawTest` compara o sorteio e a posição de cada carro e bomba com
  `src/test/resources/golden/draw_golden.txt` (rede de segurança da preparação do 1.0.0; a foto da 0.8.1 está em
  `docs/baseline/README.md`). Se ele falhar, o sorteio ou a posição mudou: só se regrava a referência (apagar o arquivo e
  rodar de novo) quando a mudança for pedida pelo usuário.
- Servidor de teste: `./gradlew runServer` (mesmo `JAVA_HOME`), com os jars do IV, dos dois packs e do AMC em `run/mods`;
  mundo normal na semente 20261027, RCON na porta 25576 (senha `teste`), `python3 -I tools/rcon.py "comando" ...`;
  `python3 -I tools/check_bays.py <x> <z> <raio>` confere se cada carro está entre duas faixas;
  `python3 -I tools/check_generation.py [--load] "x0,z0 .. x1,z1" ...` compara os carros de cada caixa (as que o
  `/amcs locate` mostra) com o sorteio. Os scripts usam `tools/rconlib.py` (lê respostas longas inteiras: o RCON as
  parte em pacotes de 4096; e manda um pacote por vez: o `RconClient` do jogo lê até 1.460 bytes, trata um pacote
  por leitura e fecha a conexão quando uma leitura traz menos de 10 bytes, conferido no código). Detalhes e resultados em
  `docs/TESTING.md`.
