# AMC Vehicles

Mod de integração para NeoForge 1.21.1 entre o **Abandoned Modern City** (AMC) e o **Immersive Vehicles** (IV) com o
**Official Content Pack**: carros abandonados nas estruturas do AMC.

**Versão 0.4.1** (16 Nov 2026), **etapa 4 de 4**: as Mercedes **aparecem sozinhas** quando o mundo é gerado (3% por
vaga) nas 63 vagas de 12 estruturas do AMC (lista abaixo). Só as áreas geradas depois de instalar o mod recebem
carros.

## A Mercedes abandonada
- Sempre: **sem motor, sem rodas, sem bancos**, **10% de combustível** (1.500 de 15.000 mB), **velocímetro (km/h)**
  e **marcador de combustível** no painel.
- Sorteados: capô, porta-malas, porta esquerda e porta direita, cada um aberto ou fechado com 50%, à parte.
- Painel: velocímetro no mostrador do meio e marcador de combustível à direita dele.
- Cores: as 15 civis com a mesma chance; as 2 de polícia só na delegacia (que ganha vagas na etapa 4).
- Raridade: **3% por vaga**, sorteada uma vez por cópia da estrutura (a mesma em qualquer ordem de carregamento).
  Em média ~1,2 carro por cidade (32,9% das cidades sem nenhum). Uma vaga com algo no caminho (por exemplo, um baú que
  o AMC sorteou ali) fica sem o carro.
- O combustível é o que o IV usaria num carro novo com o motor da Mercedes (tipo `gasoline`): sem um mod de
  gasolina, lava.

## Vagas
| Estrutura | Vagas | Onde |
|---|---|---|
| Prédio largo | 34 | garagem subterrânea, entre as faixas brancas, nariz para a parede |
| Delegacia | 4 | estacionamento da frente (linhas brancas gastas), nariz para o prédio, **só cores de polícia** |
| Farmácia grande (5 cores) | 4 | estacionamento da frente (faixas amarelas gastas), nariz para a loja |
| Posto (5 cores) | 4 | ao lado das ilhas das bombas, nariz para um dos dois lados (sorteado) |
| Posto pequeno (5 cores) | 2 | dos dois lados da ilha, nariz sorteado |
| Oficina média (3 cores) | 2 | dentro, nariz para o fundo |
| Oficina pequena (5 cores) | 2 | dentro, uma em cada box |
| Casa moderna com garagem | 2 | dentro da garagem |
| Casa suburbana | 2 | dentro da garagem (porta do lado oeste) |
| Prédio pequeno | 4 | térreo de pilotis (2 na entrada, 2 ao fundo) |
| Casa abandonada | 2 | sala-garagem da frente |
| Casa com garagem | 1 | dentro da garagem |

Imagens: `docs/spots/abandoned_wide_building.png`, `docs/spots/etapa4_vagas.png` e
`docs/spots/garagens_propostas.png`. O carro fica centrado na
vaga. Uma vaga onde o carro não cabe no mundo (bloco no caminho, sem piso) é pulada.

## Comandos (op 2)
- `/amcv mercedes [cor] [capô porta-malas [porta-esquerda porta-direita]]`: exemplo
  `/amcv mercedes white true false true false` (branca, capô aberto, porta-malas fechado, porta esquerda aberta,
  direita fechada). O que não for dito é sorteado.
- `/amcv fill`: uma Mercedes em cada vaga da estrutura do AMC onde você está (para conferir as vagas). Não funciona
  em estruturas postas com `/amcs spawn` (elas não fazem parte do plano da cidade).
- `/amcv draw`: o sorteio real (o da geração) na estrutura onde você está: diz quais vagas têm carro e põe os que
  faltam (útil em áreas geradas antes do mod e para testar).

## Requisitos
- Immersive Vehicles 24.0.0+ (`mts`) e MTS Official Pack V29+ (`mtsofficialpack`).
- Abandoned Modern City (0.1.0, build de 16 Nov 2026 ou mais novo).

## Build
`JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build -Dorg.gradle.java.installations.paths=$JAVA_HOME` →
`build/libs/amcvehicles-0.4.1.jar` (roda também os testes). A pasta `libs/` (fora do git) precisa dos jars do
Immersive Vehicles, do Official Pack e do AMC.
Detalhes técnicos, decisões e testes: `CLAUDE.md` e `docs/TESTING.md`.
