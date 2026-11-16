# AMC Vehicles — testes

## Servidor de teste
- `run/` (fora do git): `server.properties` com mundo plano (`level-type=minecraft:flat`), sem estruturas, RCON na
  porta 25576 com a senha `teste`, servidor na 25598; `run/mods` com `Immersive Vehicles-1.21.1-24.0.0.jar` e
  `MTS Official Pack-1.21.1-V29.jar`.
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

**Não testado (precisa do cliente do usuário):** a aparência (capô e porta-malas abertos na animação, ausência das
rodas e bancos no modelo, os instrumentos no painel) e se o nariz aponta mesmo para o lado anunciado.
