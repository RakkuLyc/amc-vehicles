# Diagnóstico: problemas do Immersive Vehicles (16 Nov 2026)

Pedido do usuário: sons do carro em conflito com sons vanilla, marcador de combustível errado, carro que não abastece.
Fontes: o `latest.log`, o `debug.log` e os 21 logs anteriores da instância "MAZ 2.0.0 (TESTE)"; o código do IV 24.0.0
(descompilado), do Minecraft 1.21.1 (fontes do NeoForge 21.1.219) e do Superb Warfare 0.8.9.2; e um teste isolado com
a biblioteca de áudio do jogo (LWJGL 3.3.3, OpenAL Soft 1.23.1), num dispositivo virtual (sem som). **Nada foi
alterado.**

Legenda: **Confirmado** = visto no código, no log ou num teste. **Hipótese** = provável, sem prova. **Teste** = o que
decide.

## 1. Sons (754 erros `Allocate new source: Invalid parameter parameter value.` no `latest.log`)

### Confirmado
- **Frequência**: os 754 erros estão só na última sessão (16 Nov, 23:07). Nas 21 sessões anteriores houve 0 ou 1
  erro por sessão, inclusive com o AMC Vehicles 0.5.0 e 0.6.0.
- **Gatilho na sessão**: 23:23:18 e 23:23:23, `System default audio device has changed!`: o dispositivo padrão foi para
  `sink-sunshine-stereo` (o dispositivo virtual do Sunshine, servidor de streaming) e voltou para o áudio da placa. Nas
  duas vezes o Minecraft reiniciou o motor de som. A primeira rajada de erros começa às 23:24:09 (o único erro anterior
  é um `Stop: Invalid name parameter.` às 23:23:18, durante a própria troca).
- **O IV guarda os buffers dos sons** (os dados de áudio carregados) numa lista (`InterfaceSound.dataSourceBuffers`) e
  só a limpa quando percebe a reinicialização, testando um som de carro **que esteja tocando**. A mensagem dele para
  isso ("Had an invalid sound name. Was the sound system reset?") **não aparece** no log: a lista não foi limpa.
- **Teste isolado**: um ID de buffer do dispositivo antigo, usado depois de reabrir o dispositivo, dá
  `AL_INVALID_VALUE` (o texto exato do log) quando o ID não existe mais, e, quando o ID foi reaproveitado, **liga sem
  erro o buffer do Minecraft** (um som de carro tocaria um som vanilla).
- **O IV deixa o erro pendente**: ao tocar um som ele limpa os erros, liga o buffer (`alSourcei(AL_BUFFER, id)`) e não
  lê o erro depois.
- **Teste isolado**: um erro deixado por uma linha de execução é lido pela checagem de outra (o IV atualiza os sons no
  "Render thread"; o Minecraft cria os dele no "Sound engine").
- **O Minecraft** (`Channel.create`) cria o canal, lê qualquer erro pendente e, se houver, **descarta o som vanilla**;
  o canal criado não é apagado (fica perdido).
- **Segundo caminho para o mesmo erro, sem troca de dispositivo**: quando o IV não consegue um canal, ele apaga o buffer
  (`alDeleteBuffers`) mas **não o tira da lista**; depois usa um buffer apagado.
- **Retirado** (eu tinha suposto): falta de canais. O teste isolado mostra que ela dá outro erro (`AL_OUT_OF_MEMORY`,
  "Unable to allocate memory", que não aparece no log); e o Superb Warfare já sobe o limite do OpenAL para 4.096 (no
  teste: 4.095 canais com a configuração dele). Também retirado: tom 0 no som de partida (o OpenAL aceita tom 0; tom
  negativo, volume negativo ou NaN é que dão o erro).
- **Superb Warfare**: `SoundLimit` (põe `sources = 4096` no OpenAL) e `LibraryMixin` (sobe os limites de canais do
  Minecraft para 4.096 e 64). Nenhum dos dois faz chamadas que gerem esse erro.

### Hipótese
- Na sua sessão: troca de dispositivo de áudio (Sunshine) sem nenhum som de carro tocando, a lista do IV ficou com
  buffers inválidos, cada som de carro novo deixou um `INVALID_VALUE` pendente, e o Minecraft o leu e descartou sons
  vanilla (e, onde o ID foi reaproveitado, carros tocaram sons vanilla).
- O jogo usa o OpenAL que vem com o LWJGL (1.23.1), o mesmo do teste: a configuração da instância não pede o OpenAL do
  sistema (1.25.2), mas não conferi com o jogo aberto.

### Teste (sem mudar nada)
1. Abrir o jogo, ficar **sem carro ligado por perto** e trocar o dispositivo de áudio padrão (ou conectar e desconectar
   o Sunshine); depois ligar um carro e andar entre mobs. Esperado pela hipótese: os erros voltam e sons ficam
   estranhos.
2. Repetir **com um carro ligado** durante a troca. Esperado: o IV percebe ("Had an invalid sound name" no log) e não
   há erros.
3. Sair e entrar no jogo depois da troca: esperado que tudo volte ao normal.

## 2. Combustível

### Confirmado (código do IV)
- **A bomba abastece o veículo mais próximo dela** (origem do veículo, até 16 blocos), não o que o jogador quer
  (`ATileEntityFuelPump.interact`). Se o mais próximo não tem motor: "sem motor" (`NOENGINE`); se tem outro fluido:
  "não contém o mesmo fluido" (`MISMATCH`).
- **"There is no fuel in this vehicle"** é decidido no **cliente**, pelo tanque da cópia do cliente
  (`PartEngine.autoStartEngine`: nível 0 no cliente).
- **A cópia do cliente** só muda por pacotes "some/tire X" (`PacketFluidTankChange`), que acham o tanque pelo
  **identificador** (UUID) no mapa de entidades do IV (`EntityManager.trackedEntityMap`).
- **O tanque de um veículo nunca é removido desse mapa**: o veículo o registra (`world.addEntity(fuelTank)`) e nenhuma
  classe do IV chama `remove()` num tanque de veículo (só nos de peças, bombas e carregadores de fluido).
- **Ao criar um tanque com um identificador que já existe no mapa**, o IV dá a ele um identificador aleatório
  (`AEntityA_Base`).
- **A bomba, ao encher um tanque vazio, grava o mod `wildcard`** nele (`fill(fluido, "wildcard")`); um balde depois
  (mod `minecraft`) é recusado (o `fill` só junta o mesmo fluido com o mesmo mod, ou `wildcard` vindo).

### Hipótese (marcador errado e "sem combustível" com combustível)
- O carro sai do alcance do cliente (morte, distância, troca de dimensão) e volta; o tanque novo do cliente ganha um
  identificador aleatório (o velho continua no mapa); os pacotes do servidor vão para o tanque velho; o marcador e a
  checagem de partida olham o novo, parado. Na sessão: morte às 23:21:33 (seguida de avisos "unknown entity"); às 23:25
  "Conectado e abastecendo" e, 7 s depois, "There is no fuel in this vehicle".

### Teste (sem mudar nada)
1. Abastecer um carro e olhar o marcador. Ir para longe (além da distância de visão, ou `/tp` longe e voltar) ou morrer
   e voltar. Abastecer ou dirigir de novo. Esperado pela hipótese: o marcador para de mudar e pode aparecer "sem
   combustível" com combustível.
2. Sair para o menu e entrar de novo: esperado que o marcador volte a mostrar o certo.

### Efeitos do próprio AMC Vehicles (suspeitos nossos)
- **Carros nos postos gerados pela 0.6.0** (antes da 0.6.1) continuam no mundo e "roubam" a bomba (o mais próximo).
- **Carros gerados até a primeira 0.6.0** têm o tanque com mod `""`: recusam balde.
- **Lava a 0,35**: o consumo com lava é ~3× maior que o normal do IV (o marcador cai mais rápido).
- Para separar: uma rodada **sem o `amcvehicles`** na pasta `mods`, com um carro do item do IV e uma bomba posta à mão,
  só em sobrevivência.

## 3. Perguntas ao usuário
- O que foi posto na bomba antes das 23:26:59 ("Esta bomba não contém o mesmo fluido... lava está no veículo")?
- Qual carro você tentava ligar às 23:25 e às 23:26 (um abandonado do mod ou um do item, com motor) e qual estava mais
  perto da bomba?
- O "conflito" de som: sons vanilla somem, saem errados, ou carros tocam sons estranhos? Acontece em outras sessões, e
  essas coincidiram com o Sunshine ou com troca de saída de áudio?
- O marcador: o que exatamente mostra (parado, zerado, cheio) e se volta ao normal ao sair e entrar no mundo.
