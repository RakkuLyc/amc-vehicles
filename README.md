# AMC Vehicles

Mod de integração para NeoForge 1.21.1 entre o **Abandoned Modern City** (AMC) e o **Immersive Vehicles** (IV) com o
**Official Content Pack**: carros abandonados nas estruturas do AMC.

**Versão 0.1.0** (16 Nov 2026), **etapa 1 de 4**: por enquanto só o comando de teste que coloca uma Mercedes 230
abandonada na sua frente. A geração no mundo vem nas próximas etapas (`CLAUDE.md`, seção "Etapas").

## A Mercedes abandonada
- Sempre: **sem motor, sem rodas, sem bancos**, **10% de combustível** (1.500 de 15.000 mB), **velocímetro (km/h)**
  e **marcador de combustível** no painel.
- Sorteados: capô aberto ou fechado, porta-malas aberto ou fechado (50% cada).
- Cores: as 15 civis com a mesma chance; as 2 de polícia só na delegacia.
- O combustível é o que o IV usaria num carro novo com o motor da Mercedes (tipo `gasoline`): sem um mod de
  gasolina, lava.

## Comando (op 2)
`/amcv mercedes [cor] [capô] [porta-malas]`: exemplo `/amcv mercedes white true false` (branca, capô aberto,
porta-malas fechado). O que não for dito é sorteado.

## Requisitos
- Immersive Vehicles 24.0.0+ (`mts`) e MTS Official Pack V29+ (`mtsofficialpack`).
- A partir da etapa 2: Abandoned Modern City.

## Build
`JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build -Dorg.gradle.java.installations.paths=$JAVA_HOME` →
`build/libs/amcvehicles-0.1.0.jar`. A pasta `libs/` (fora do git) precisa do jar do Immersive Vehicles para compilar.
Detalhes técnicos, decisões e testes: `CLAUDE.md` e `docs/TESTING.md`.
