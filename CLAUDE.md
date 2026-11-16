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
- **Sorteados**: capô aberto e porta-malas aberto (cada um à parte; 50% cada, valor meu, a confirmar).
- **Cores**: as 15 civis sorteadas com a mesma chance; `_police` e `_police2` **só na delegacia** (e lá só elas).
- **Raridade**: **3% por vaga** (cada vaga sorteia sozinha). Com isso a garagem subterrânea do prédio largo (muitas
  vagas) pode ter vários carros e as estruturas com poucas vagas quase nunca têm.
- **Comando de debug** que põe um carro em **todas as vagas da estrutura onde o jogador está** (para conferir a
  detecção das vagas).
- Nome AMC Vehicles, repositório privado, documentação em português.

## Etapas
1. **(feita, aguardando o teste do usuário)** Comando `/amcv mercedes [cor] [capô] [porta-malas]`: uma Mercedes no
   estado pedido na frente de quem executa.
2. Vagas de uma estrutura com vagas pintadas (a garagem subterrânea do prédio largo), com o comando de debug que
   enche todas as vagas da estrutura onde o jogador está.
3. Geração no mundo: 3% por vaga, cores, delegacia.
4. As outras estruturas (oficinas por dentro, postos ao lado das bombas, casas com garagem, delegacia...), cada uma
   com uma imagem das vagas propostas para o usuário aprovar.

## Como a Mercedes é montada (`Mercedes.spawn`, confirmado no código do IV 24.0.0 e testado no servidor)
- `new EntityVehicleF_Physics(world, null, item, null)`: dados `null` = carro novo, como de um item recém-feito. O
  pack põe `door_l`, `door_r` e `door_hood` em 1 (abertos) pelas `initialVariables`; o mod então põe `door_hood` e
  `door_trunk` no valor sorteado. **As portas laterais ficam abertas** (padrão do pack; pergunta pendente ao usuário).
- Instrumentos (antes de pôr o carro no mundo, para irem nos dados iniciais): o carro tem 3 lugares no painel (0
  esquerda, 1 meio e maior, 2 direita); velocímetro `instrument_car_spedometer_kph` no 1, marcador
  `instrument_car_fuelqty` no 0; o 2 fica vazio.
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

## Arquitetura
```
AmcVehicles    o @Mod: registra os comandos
Mercedes       monta a Mercedes abandonada (estado, cores, instrumentos, combustível)
AmcvCommands   /amcv (op 2): comandos de teste
```

## Comandos (op 2)
- `/amcv mercedes [cor] [capô] [porta-malas]`: uma Mercedes com o eixo traseiro 3 blocos à frente de quem executa,
  o nariz para onde ele olha (arredondado para N, S, L ou O). Cor sem o `_` (`white`, `police`...); capô e
  porta-malas `true` (aberto) ou `false`; o que faltar é sorteado (a cor entre as civis). Do console:
  `execute positioned X Y Z rotated YAW 0 run amcv mercedes ...`. Responde com a cor, a posição, o capô, o
  porta-malas, o combustível, o fluido, os motores tirados e as peças que ficaram.

## Build e teste
- Build (Java 21; o Java padrão do sistema é o 27, que o Gradle não aceita):
  `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build -Dorg.gradle.java.installations.paths=$JAVA_HOME`
  → `build/libs/amcvehicles-<versão>.jar`.
- `libs/` (não versionada) precisa de `Immersive Vehicles-1.21.1-24.0.0.jar` (compilação) e
  `MTS Official Pack-1.21.1-V29.jar`; cópias em `AbandonedModernCity/libs/`.
- Servidor de teste: `./gradlew runServer` (mesmo `JAVA_HOME`), com os jars do IV e do pack em `run/mods`; mundo
  plano, RCON na porta 25576 (senha `teste`), `python3 -I tools/rcon.py "comando" ...`. Detalhes e resultados em
  `docs/TESTING.md`.
