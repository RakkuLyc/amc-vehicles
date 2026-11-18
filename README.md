# AMC Vehicles

Mod de integração para NeoForge 1.21.1 entre o **Abandoned Modern City** (AMC) e o **Immersive Vehicles** (IV):
carros abandonados nas estruturas do AMC.

**Versão 0.7.11** (17 Nov 2026), **etapa 6**: 8 modelos de carro e bombas de combustível nos postos. Os carros
**aparecem sozinhos** quando o mundo é gerado (9% por vaga) nas 57 vagas de 10 estruturas do AMC (lista abaixo; desde a
0.6.1 nenhum carro nos postos, para não atrapalhar quem vai abastecer), e as bombas em todos os postos. Só as áreas geradas depois de instalar o mod recebem carros e bombas.

## Os carros abandonados
- **Modelos** (a mesma chance cada): do Official Pack, **Mercedes 230**, **Mustang 69** e **Scout**; do Official
  Automobile Pack, **Cagouille**, **Camaro**, **Escargot**, **Highwayman** e **Station Wagon**. Depois a cor, com a
  mesma chance entre as cores civis do modelo.
- **Viaturas só na delegacia** (e lá só elas): Mercedes (`_police`/`_police2`), Cagouille e Mustang, 1/3 cada.
- Sempre: **bateria descarregada** (0.7.3; precisa de jumper pack), **sem motor, sem rodas** (nem estepe), **sem bancos**, **15% de combustível** do tanque de cada modelo (10% até a 0.7.3) e, no
  painel, **só velocímetro (km/h) e marcador de combustível**. Apoiados nos blocos de apoio que o próprio modelo mostra
  quando faltam as rodas.
- Sorteados: capô, porta-malas (no Scout, a tampa da caçamba; o Mustang não tem) e cada porta lateral (a Station Wagon
  tem 4), cada um aberto ou fechado com 50%, à parte.
- Raridade: **9% por vaga**, sorteada uma vez por cópia da estrutura (a mesma em qualquer ordem de carregamento). Em
  média ~3,2 carros por cidade (9% das cidades sem nenhum; com 3% eram ~1,1). Uma vaga com algo no caminho (por exemplo,
  um baú que o AMC sorteou ali) fica sem o carro.
  AMC sorteou ali) fica sem o carro.
- O combustível é o que o IV usaria num carro novo com o motor do modelo (tipo `gasoline`): sem um mod de gasolina,
  lava.

## Bombas de combustível e combustível
- Bombas `mts:fuelpump` (do IV) sempre presentes nos postos: 8 no posto (2 em cada ilha) e 2 no posto pequeno
  (`docs/spots/bombas_propostas.png`). **50% vazias; as outras com 1, 2 ou 3 baldes de lava** (a mesma chance cada; ~1
  balde por bomba, ~7 por cidade).
- **A lava rende 0,35** como combustível de motor (gasolina, diesel e avgas) desde a 0.7.11: ~0,45 cidade por balde (no IV
  puro, ~1,3). O mod aplica isso ao iniciar o servidor (ou ao entrar num servidor), só na memória, e devolve o valor do IV
  ao parar; o arquivo `config/mtsconfig.json` não é alterado pelo mod. **Cuidado**: se o IV gravar a sua configuração com o
  mundo aberto, o 0,35 vai para o arquivo; para desfazer, edite `"lava"` para `1.0` em `gasoline`, `diesel` e `avgas`.
  (Histórico: 0,35 na 0.6.0 a 0.7.4, removido na 0.7.5, 0,60 na 0.7.9, 0,45 na 0.7.10.)

## Loot de oficinas e postos
Complementa o AMC TACZ Loot (soma às tabelas do AMC, não substitui nada; vasos ficam sem nada). Chance por container
(barril tem a metade): Comum 9,2% baú / 4,6% barril; Incomum 4,4% / 2,2%; Raro 2,0% / 1,0%.
- **Oficinas**: bancos, rodas e wrench (Comum), blowtorch (Incomum), motor, repair kit e jumper pack (Raro). Bancos: Car Seat,
  Sport Seat, Vintage Seat e o banco genérico (34 cores); rodas: só offroad e vintage; motores: os 5 dos 8 carros;
  nada de polícia.
- **Postos**: só o galão do IV, **sempre cheio** (1 balde de lava), Incomum (baú 4,4% / barril 2,2%).
- O repair kit é 10% mais comum que o motor e o jumper pack (baú 2,2% / barril 1,1% contra 2,0% / 1,0%).
- Em média, por cidade: ~4,4 bancos, ~4,4 rodas, ~4,4 wrenches, ~2 blowtorch, ~1 motor, ~1,1 repair kit, ~1 jumper pack e ~0,6 galão (sempre cheio).

## Vagas
| Estrutura | Vagas | Onde |
|---|---|---|
| Prédio largo | 34 | garagem subterrânea, entre as faixas brancas, nariz para a parede |
| Delegacia | 4 | estacionamento da frente (linhas brancas gastas), nariz para o prédio, **só viaturas** |
| Farmácia grande (5 cores) | 4 | estacionamento da frente (faixas amarelas gastas), nariz para a loja |
| Oficina média (3 cores) | 2 | dentro, nariz para o fundo |
| Oficina pequena (5 cores) | 2 | dentro, uma em cada box |
| Prédio pequeno | 4 | térreo de pilotis (2 na entrada, 2 ao fundo) |
| Casa moderna com garagem | 2 | dentro da garagem |
| Casa suburbana | 2 | dentro da garagem (porta do lado oeste) |
| Casa abandonada | 2 | sala-garagem da frente |
| Casa com garagem | 1 | dentro da garagem |

Imagens: `docs/spots/abandoned_wide_building.png`, `docs/spots/etapa4_vagas.png` e
`docs/spots/garagens_propostas.png`. O carro fica centrado na vaga.

## Comandos (op 2)
- `/amcv car <modelo> [cor] [capô porta-malas]`: um carro na sua frente (ex.: `/amcv car mustang police`). O que não for
  dito é sorteado.
- `/amcv lineup`: os 8 modelos e as 4 viaturas lado a lado na sua frente.
- `/amcv fill`: um carro em cada vaga da estrutura do AMC onde você está (para conferir as vagas). Não funciona em
  estruturas postas com `/amcs spawn` (elas não fazem parte do plano da cidade).
- `/amcv draw`: o sorteio real (o da geração) na estrutura onde você está: diz quais vagas têm carro e põe os que
  faltam (útil em áreas geradas antes do mod e para testar).
- `/amcv pump [baldes]`: uma bomba de combustível na sua frente com tantos baldes de lava.
- `/amcv fill` e `/amcv draw` também põem as bombas do posto onde você está.

## Requisitos
- Immersive Vehicles 24.0.0+ (`mts`), MTS Official Pack V29+ (`mtsofficialpack`) e **Official Automobile Pack V3+
  (`oamp`)**.
- Abandoned Modern City (0.1.0, build de 16 Nov 2026 ou mais novo).

## Build
`JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build -Dorg.gradle.java.installations.paths=$JAVA_HOME` →
`build/libs/amcvehicles-0.7.11.jar` (roda também os testes). A pasta `libs/` (fora do git) precisa dos jars do
Immersive Vehicles, dos dois packs e do AMC.
Detalhes técnicos, decisões e testes: `CLAUDE.md` e `docs/TESTING.md`.
