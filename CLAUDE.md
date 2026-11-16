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

Objetivo da fase de testes: a **Mercedes 230** do Official Pack (`mtsofficialpack:merc230`) aparecendo de forma
aleatória e rara nos estacionamentos e garagens das estruturas do AMC (respeitando as vagas, inclusive as
subterrâneas), dentro das oficinas (nunca fora) e nos postos de gasolina (ao lado das bombas: lá não há vaga pintada).

## Decisões do usuário (16 Nov 2026)
- **Estado da Mercedes, sempre**: sem motor, sem rodas, sem bancos, **10% de gasolina**, **velocímetro (km/h) e
  marcador de combustível** no painel.
- **Sorteados**: capô aberto e porta-malas aberto (cada um à parte; 50% cada, valor meu, a confirmar) e **cada
  porta lateral aberta ou fechada, 50% a esquerda e 50% a direita, à parte** (pedido do usuário, 16 Nov 2026).
- **Cores**: as 15 civis sorteadas com a mesma chance; `_police` e `_police2` **só na delegacia** (e lá só elas).
- **Raridade**: **3% por vaga** (cada vaga sorteia sozinha). Com isso a garagem subterrânea do prédio largo (muitas
  vagas) pode ter vários carros e as estruturas com poucas vagas quase nunca têm.
- **Comando de debug** que põe um carro em **todas as vagas da estrutura onde o jogador está** (para conferir a
  detecção das vagas).
- Nome AMC Vehicles, repositório privado, documentação em português.
- **Só chunks novos** recebem carros (16 Nov 2026, etapa 3): as áreas geradas antes de instalar o mod (ou antes desta
  versão) ficam sem carros; para elas, e para testar, `/amcv draw` aplica o sorteio real na estrutura onde se está.

## Etapas
1. **(aprovada pelo usuário, 16 Nov 2026)** Comando `/amcv mercedes`: uma Mercedes no estado pedido na frente de
   quem executa. Depois do primeiro teste (0.1.0), pedidos da 0.1.1: portas sorteadas e o marcador de combustível à
   direita do velocímetro. O usuário testou a 0.1.1 ("está tudo funcionando") e confirmou capô e porta-malas 50% e o
   combustível lava.
2. **(aprovada pelo usuário, 16 Nov 2026)** As vagas da garagem subterrânea do prédio largo
   (`abandoned_wide_building`) e o comando de debug `/amcv fill`, que enche todas as vagas da estrutura onde o
   jogador está.
3. **(aprovada pelo usuário, 16 Nov 2026)** Geração no mundo: 3% por vaga, cores sorteadas, só chunks novos,
   `/amcv draw`.
4. **(feita na 0.4.0, aguardando o teste do usuário)** As outras estruturas: imagem das vagas propostas
   (`docs/spots/etapa4_propostas.png`) aprovada pelo usuário ("Aprovar todas"), com o nariz **sorteado** nos postos
   (escolha dele) e a **farmácia grande** acrescentada por ele (vagas da frente). Ver "Vagas" abaixo.

## Como a Mercedes é montada (`Mercedes.spawn`)
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
- Rotação: o ângulo do IV é o do Minecraft com o sinal trocado (`WrapperEntity.getYaw`); `Mercedes.spawn` recebe o
  ângulo do Minecraft para onde o **nariz** aponta (0 sul, 90 oeste, 180 norte, 270 leste). A origem do modelo é o
  eixo traseiro: a carroceria vai de ~1,3 bloco atrás dele a ~4,15 à frente (rodas em z 0 e 3,19 do `merc230.json`).

## Vagas (`Spots`, `Placement`, `AmcWorld`)
- **Lista**: `src/main/resources/amcvehicles/spots.txt`, uma linha por vaga:
  `<modelo> bay <x0> <y> <z0> <x1> <z1> <nariz>`, em coordenadas do `.nbt` do modelo (as do structure block). O
  modelo é a coluna `model` do `index.txt` do AMC, senão o nome do arquivo: as variantes de cor dividem as vagas.
  x0..x1, z0..z1 = as células da vaga; y = a camada do piso; nariz = para onde o nariz aponta no `.nbt` (N = -Z,
  S = +Z, E = +X, W = -X). O carro fica no meio da vaga (o corpo centrado: o eixo traseiro 1,425 atrás do meio).
- **Detecção** (`tools/find_spots.py <arquivo.nbt> <y do piso> [--png] [--out] [--model]`, Python, fora do mod):
  vaga = o piso entre duas faixas pintadas paralelas (linhas retas do bloco da faixa, 4+ de comprimento) com 2 a 5
  células entre elas e 6+ de sobreposição; precisa de piso e 2 blocos livres em todas as células (senão sai como
  BLOCKED e não entra na lista). O nariz vai para a ponta fechada (o carro entra de frente): a outra ponta, com piso e
  altura livre além dela, é o corredor. A imagem (`--png`) mostra o piso, as faixas, as vagas (verde; vermelho =
  bloqueada), o carro (azul) e o nariz (ponto vermelho). Imagens aprovadas em `docs/spots/`.
- **Nariz sorteado**: nas vagas sem pintura que diga como o carro para (os postos), o nariz é `X` ou `Z` em
  `spots.txt`: ao longo desse eixo, o sentido sorteado por carro (`Draw.Car.flip`, 50%; `Spots.Bay.oriented`).
- **Estruturas com vagas (0.4.0, 57 vagas em 10 modelos)**: prédio largo 34 (faixas pintadas, detectadas); e, escritas
  à mão e conferidas (`find_spots.py --bays`): oficina média 2 e pequena 2 (dentro, uma por box, nariz para o fundo),
  posto 4 e posto pequeno 2 (ao lado das ilhas das bombas, ao longo delas, nariz sorteado), farmácia grande 4 (vagas da
  frente entre as faixas amarelas gastas, nariz para a loja, até a sebe da fachada), delegacia 4 (vagas da frente entre
  as linhas brancas gastas, nariz para o prédio, só cores de polícia), casa com garagem 1, casa moderna com garagem 2 e
  casa suburbana 2 (dentro da garagem). As coordenadas e o porquê de cada uma estão nos comentários de `spots.txt`.
  Olhados e sem vagas: a torre (a grade de concreto branco é o piso do saguão), o prédio azulado e o apocalíptico
  (desenhos do piso), o prédio das rampas (salas no térreo), a farmácia pequena e as outras casas (o usuário confirmou:
  só a farmácia grande a mais).
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
  Antes de pôr um carro, todas as colunas sob o corpo (5,45 × 2) precisam de piso (bloco com colisão) e 2 blocos sem
  colisão em cima (ar, grama, cipós; no primeiro, um bloco fino de até 1/4 de altura, como o tapete de musgo do mato
  do AMC, `AmcWorld.THIN`) e nenhum veículo do IV ali (um carro de um `fill` anterior: não empilha); senão a vaga é
  pulada e o motivo vai na resposta do comando e no log.
  As estruturas postas com `/amcs spawn` não estão no plano: o `/amcv fill` não as reconhece.

## Geração no mundo (`Draw`, `Generation`, `Pending`; etapa 3)
- **Sorteio** (`Draw.car`): função pura da semente do lote (a cópia da estrutura; o AMC a tira da semente do mundo) e
  da **posição da vaga** no `.nbt` (`Spots.Bay.key`: x0, y, z0; não a ordem em `spots.txt`, para que acrescentar ou
  reordenar vagas de um modelo não mude o sorteio das outras), com um sal próprio (SplitMix64): a mesma vaga do mesmo mundo dá sempre o mesmo
  resultado, em qualquer ordem de carregamento. **3% por vaga** (`CHANCE_PER_BAY`); depois, cada um à parte: cor (as
  15 civis com a mesma chance; na delegacia, `type` `police_station`, só as 2 de polícia), capô 50%, porta-malas 50%,
  porta esquerda 50%, porta direita 50%. As cores e as chances moram em `Draw` (sem classes do Minecraft, para os
  testes); `Mercedes` as repete.
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
  2 de altura livre, nenhum veículo do IV, `mts:builder_existing`, ali) e põe o carro, ou o descarta de vez se estiver
  bloqueado (por exemplo, um baú sorteado pelo AMC dentro da vaga). Quando nenhum carro do chunk espera mais, ele sai
  de `Pending`. Num chunk com um carro esperando, os outros já resolvidos não são tentados de novo na mesma sessão
  (sem log repetido). A checagem de "veículo ali" impede carro duplicado se o servidor cair entre pôr o carro e
  salvar.
- **Limite conhecido**: `Pending` é salvo com o mundo; se o chunk novo for salvo (ao descarregar) e o servidor cair
  antes de o mundo salvar `Pending`, os carros daquele chunk se perdem (ele não é "novo" de novo).
- **Log**: cada carro posto (`[AMCV] Mercedes<cor> in <estrutura> bay <n> at x y z (generated)`) e cada vaga
  descartada, com o motivo.
- **Projeção** (`DrawTest.projectionPerCity`: o planejador real do AMC num terreno plano, 5 sementes, 492 cidades,
  com o sorteio real; 0.4.0, as 57 vagas): 33,7 vagas por cidade, **1,01 carro por cidade**; cidades com 0 carros
  39,8%, 1 33,7%, 2 16,3%, 3 6,3%, 4 3,7%, 6 0,2%. Por cópia: prédio largo 0,92, delegacia 0,15, posto 0,13, farmácia
  grande 0,12, casa moderna e suburbana 0,07, posto pequeno e oficina pequena 0,06, casa com garagem 0,04, oficina
  média 0,03. (Até a 0.3.0, só o prédio largo: 0,32 por cidade.)

## Arquitetura
```
AmcVehicles    o @Mod: registra os comandos
Mercedes       monta a Mercedes abandonada (estado, cores, instrumentos, combustível)
Spots          as vagas de spots.txt, por modelo (sem classes do Minecraft)
Placement      vaga do .nbt -> posição, nariz e colunas do carro no mundo (sem classes do Minecraft; testado)
AmcWorld       lê o plano do AMC (a estrutura de uma coluna) e confere se o carro cabe no mundo
Draw           o sorteio dos carros (3% por vaga, cor, capô, porta-malas, portas), puro; cores e chances
Generation     a geração: sorteia ao gerar um chunk, põe no tick quando os chunks sob o carro estão prontos
Pending        os chunks com carro sorteado ainda não posto (salvo no mundo; percorrido a cada 10 ticks)
AmcvCommands   /amcv (op 2): comandos de teste
```

## Comandos (op 2)
- `/amcv mercedes [cor] [capô porta-malas [porta-esquerda porta-direita]]`: uma Mercedes com o eixo traseiro 3
  blocos à frente de quem executa, o nariz para onde ele olha (arredondado para N, S, L ou O). Cor sem o `_` (`white`, `police`...); capô e
  porta-malas e portas `true` (aberto) ou `false`; o que faltar é sorteado (a cor entre as civis). Do console:
  `execute positioned X Y Z rotated YAW 0 run amcv mercedes ...`. Responde com a cor, a posição, o capô, o
  porta-malas, as portas, o combustível, o fluido, os motores tirados e as peças que ficaram.
- `/amcv fill`: uma Mercedes em **cada vaga** da estrutura do AMC onde quem executa está (dentro do lote dela, em
  qualquer altura; na garagem também); cor, capô, porta-malas e portas sorteados (na delegacia, as cores de polícia).
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
- `libs/` (não versionada) precisa de `Immersive Vehicles-1.21.1-24.0.0.jar`, `MTS Official Pack-1.21.1-V29.jar`
  (cópias em `AbandonedModernCity/libs/`) e `abandonedmoderncity-0.1.0.jar` (o jar do AMC, de
  `AbandonedModernCity/build/libs/`; o usado na 0.2.0 é o de 16 Nov 2026, 02:40). O build compila contra eles e os
  testes usam o código puro do AMC (estruturas e `Def.code`); nenhum vai dentro do jar deste mod.
- Testes de unidade: `./gradlew test` (mesmo `JAVA_HOME`; ~15 s, o AMC carrega as 52 estruturas).
- Servidor de teste: `./gradlew runServer` (mesmo `JAVA_HOME`), com os jars do IV, do pack e do AMC em `run/mods`;
  mundo normal na semente 20261027, RCON na porta 25576 (senha `teste`), `python3 -I tools/rcon.py "comando" ...`;
  `python3 -I tools/check_bays.py <x> <z> <raio>` confere se cada carro está entre duas faixas;
  `python3 -I tools/check_generation.py [--load] "x0,z0 .. x1,z1" ...` compara os carros de cada caixa (as que o
  `/amcs locate` mostra) com o sorteio. Os scripts usam `tools/rconlib.py` (lê respostas longas inteiras: o RCON as
  parte em pacotes de 4096; e manda um pacote por vez: o `RconClient` do jogo lê até 1.460 bytes, trata um pacote
  por leitura e fecha a conexão quando uma leitura traz menos de 10 bytes, conferido no código). Detalhes e resultados em
  `docs/TESTING.md`.
