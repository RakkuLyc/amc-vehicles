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

## Etapas
1. **(aprovada pelo usuário, 16 Nov 2026)** Comando `/amcv mercedes`: uma Mercedes no estado pedido na frente de
   quem executa. Depois do primeiro teste (0.1.0), pedidos da 0.1.1: portas sorteadas e o marcador de combustível à
   direita do velocímetro. O usuário testou a 0.1.1 ("está tudo funcionando") e confirmou capô e porta-malas 50% e o
   combustível lava.
2. **(feita na 0.2.0, aguardando o teste do usuário)** As vagas da garagem subterrânea do prédio largo
   (`abandoned_wide_building`) e o comando de debug `/amcv fill`, que enche todas as vagas da estrutura onde o
   jogador está.
3. Geração no mundo: 3% por vaga, cores, delegacia.
4. As outras estruturas (oficinas por dentro, postos ao lado das bombas, casas com garagem, delegacia...), cada uma
   com uma imagem das vagas propostas para o usuário aprovar.

## Como a Mercedes é montada (`Mercedes.spawn`)
**Confirmado** (código do IV 24.0.0 descompilado + dados salvos no servidor de teste, antes e depois de reiniciar):
as peças que ficam, o combustível, os instrumentos gravados, os valores de `door_hood`/`door_trunk`, o ângulo
gravado, e que o carro nasce **destrancado** (sem a variável `locked`; o IV não guarda dono).
**Visto pelo usuário no jogo (0.1.0):** o nariz para o lado anunciado (sul) e o porta-malas aberto.
**Hipóteses, a conferir no cliente do usuário:** que o valor 1 de `door_hood` e das portas aparece aberto; as outras
direções do nariz; que a origem é o eixo traseiro; que um cliente conectado vê os
instrumentos e não vê motor (no teste não havia cliente para receber os pacotes de "motor posto" e "motor tirado";
se aparecer um motor fantasma, a alternativa é pôr só as peças permanentes em vez de pôr tudo e tirar o motor).

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
  colisão em cima (ar, tapetes, grama, cipós); senão a vaga é pulada e o motivo vai na resposta do comando e no log.
  As estruturas postas com `/amcs spawn` não estão no plano: o `/amcv fill` não as reconhece.

## Arquitetura
```
AmcVehicles    o @Mod: registra os comandos
Mercedes       monta a Mercedes abandonada (estado, cores, instrumentos, combustível)
Spots          as vagas de spots.txt, por modelo (sem classes do Minecraft)
Placement      vaga do .nbt -> posição, nariz e colunas do carro no mundo (sem classes do Minecraft; testado)
AmcWorld       lê o plano do AMC (a estrutura de uma coluna) e confere se o carro cabe no mundo
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
  `python3 -I tools/check_bays.py <x> <z> <raio>` confere se cada carro está entre duas faixas. Detalhes e
  resultados em `docs/TESTING.md`.
