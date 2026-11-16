# AMC Vehicles

Mod de integração para NeoForge 1.21.1 entre o **Abandoned Modern City** (AMC) e o **Immersive Vehicles** (IV):
carros abandonados nas estruturas do AMC.

**Versão 0.5.0** (16 Nov 2026), **etapa 5**: 8 modelos de carro. Os carros **aparecem sozinhos** quando o mundo é
gerado (3% por vaga) nas 63 vagas de 12 estruturas do AMC (lista abaixo). Só as áreas geradas depois de instalar o mod
recebem carros. Próximo passo (etapa 6, proposta): bombas de combustível nos postos.

## Os carros abandonados
- **Modelos** (a mesma chance cada): do Official Pack, **Mercedes 230**, **Mustang 69** e **Scout**; do Official
  Automobile Pack, **Cagouille**, **Camaro**, **Escargot**, **Highwayman** e **Station Wagon**. Depois a cor, com a
  mesma chance entre as cores civis do modelo.
- **Viaturas só na delegacia** (e lá só elas): Mercedes (`_police`/`_police2`), Cagouille e Mustang, 1/3 cada.
- Sempre: **sem motor, sem rodas** (nem estepe), **sem bancos**, **10% de combustível** do tanque de cada modelo e, no
  painel, **só velocímetro (km/h) e marcador de combustível**. Apoiados nos blocos de apoio que o próprio modelo mostra
  quando faltam as rodas.
- Sorteados: capô, porta-malas (no Scout, a tampa da caçamba; o Mustang não tem) e cada porta lateral (a Station Wagon
  tem 4), cada um aberto ou fechado com 50%, à parte.
- Raridade: **3% por vaga**, sorteada uma vez por cópia da estrutura (a mesma em qualquer ordem de carregamento). Em
  média ~1,2 carro por cidade (32,9% das cidades sem nenhum). Uma vaga com algo no caminho (por exemplo, um baú que o
  AMC sorteou ali) fica sem o carro.
- O combustível é o que o IV usaria num carro novo com o motor do modelo (tipo `gasoline`): sem um mod de gasolina,
  lava.

## Vagas
| Estrutura | Vagas | Onde |
|---|---|---|
| Prédio largo | 34 | garagem subterrânea, entre as faixas brancas, nariz para a parede |
| Delegacia | 4 | estacionamento da frente (linhas brancas gastas), nariz para o prédio, **só viaturas** |
| Farmácia grande (5 cores) | 4 | estacionamento da frente (faixas amarelas gastas), nariz para a loja |
| Posto (5 cores) | 4 | ao lado das ilhas das bombas, nariz para um dos dois lados (sorteado) |
| Posto pequeno (5 cores) | 2 | dos dois lados da ilha, nariz sorteado |
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
- `/amcv pump [baldes]`: teste da etapa 6, uma bomba de combustível na sua frente com tantos baldes de lava.

## Requisitos
- Immersive Vehicles 24.0.0+ (`mts`), MTS Official Pack V29+ (`mtsofficialpack`) e **Official Automobile Pack V3+
  (`oamp`)**.
- Abandoned Modern City (0.1.0, build de 16 Nov 2026 ou mais novo).

## Build
`JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build -Dorg.gradle.java.installations.paths=$JAVA_HOME` →
`build/libs/amcvehicles-0.5.0.jar` (roda também os testes). A pasta `libs/` (fora do git) precisa dos jars do
Immersive Vehicles, dos dois packs e do AMC.
Detalhes técnicos, decisões e testes: `CLAUDE.md` e `docs/TESTING.md`.
