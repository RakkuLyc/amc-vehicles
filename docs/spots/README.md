# Images of the parking bays

The images were made while the documents were in Portuguese, and their captions are drawn **inside** the pictures, in
Portuguese. They were composed by one-off scripts that are not in `tools/` (`tools/find_spots.py --png` draws only the
numbers), so they were not regenerated: that would need a new compositing script (from AMC's `.nbt` files with
`find_spots.py --png` and English titles). This page explains them instead (0.9.5, 2026-11-19).

## Colour key (all the maps)
- **Blue** = covered floor; **grey** = open floor; **brown** = blocked (a wall, furniture, no room for a car); dark =
  outside the structure. The numbers on the edges are the template's x (top) and z (left) coordinates.
- **Green rectangle** = a parking bay (red outline = a blocked bay); **light blue rectangle** = the car's body; **red dot**
  = the car's nose; **yellow number** = the bay's number.
- In the proposals, "frente = topo da imagem = rua" means "front = top of the image = street".

## The images
| File | What it shows | Captions (Portuguese → English) |
|---|---|---|
| `abandoned_wide_building.png` | The underground garage of the wide building, layer 0, the 36 bays found (34 used; the 2 crossed by the stairwell blocked). Made by `find_spots.py --png`. | numbers only |
| `etapa4_propostas.png` | Stage 4: the proposed bays of 8 structures (approved by the user). | "Etapa 4 - vagas propostas" = "Stage 4 - proposed bays"; "Oficina média (2 vagas, dentro, nariz para o fundo)" = "Medium workshop (2 bays, inside, nose to the back)"; "Oficina pequena (2 vagas, uma em cada box)" = "Small workshop (2 bays, one in each box)"; "Posto (4: ao lado das ilhas das bombas)" = "Gas station (4: beside the pump islands)"; "Posto pequeno (2: dos dois lados da ilha)" = "Small gas station (2: on both sides of the island)"; "Delegacia (4 vagas da frente, linhas brancas gastas, só cores de polícia)" = "Police station (4 front bays, worn white lines, police colours only)"; "Casa com garagem (1, dentro da garagem)" = "Garage house (1, inside the garage)"; "Casa moderna com garagem (2, garagem aberta na frente)" = "Modern garage house (2, garage open at the front)"; "Casa suburbana (2, garagem-oficina com porta no lado oeste)" = "Suburban house (2, garage-workshop with a door on the west side)" |
| `etapa4_vagas.png` | The bays applied in stage 4, drawn from `spots.txt`. | not transcribed one by one; the structure names follow the table above |
| `garagens_propostas.png` | 0.4.1: the proposed garages of the abandoned house and of the small building ("Aprovar as 6" = "Approve the 6"). | not transcribed one by one |
| `bombas_propostas.png` | Stage 6: the proposed positions of the fuel pumps (2 per island). | not transcribed one by one |
| `vidro_rampa_propostas.png` | 0.8.0: the bays proposed at the glass house and at the ramp building (the ramp building's left in 0.8.1). | not transcribed one by one |
| `levantamento.png` | Stage 4 survey: covered/open/blocked floor of the ground layer of 9 houses, the small pharmacy and the police station (y0 = basement, y1). | "Levantamento: piso coberto (azul) / aberto (cinza) / bloqueado (marrom) no chão de cada estrutura" = "Survey: covered (blue) / open (grey) / blocked (brown) floor on the ground of each structure"; "police y0 (subsolo)" = "police y0 (basement)" |
