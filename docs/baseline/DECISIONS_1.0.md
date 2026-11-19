# The user's decisions for phases 3 to 6 (2026-11-19)

The user asked to go through phases 3 to 6 without stopping between them and answered at once:
- **LICENSE**: MIT, "RakkuLyc", 2026.
- **E-mail in the git history**: new commits use GitHub's noreply address (set only in this repository,
  `git config --local user.email`); the old ones stay as they are (the history is not rewritten).
- **IV version accepted**: only the 24 series (`[24.0.0,25)` in `neoforge.mods.toml`).
- **An IV error in the generation** (if confirmed): log it and skip that car.
- **mtsconfig.json**: if there is a way that keeps lava at 0.05 in the game and the file with IV's values, apply it
  directly (tested); if the only way out changes the game, do not apply it and explain.
- **Rarities in the translation**: English, with AMC TACZ Loot's Portuguese name in brackets the first time; AMC TACZ Loot
  is not touched.
- **Documents of 1.0**: a short README for whoever installs + technical documentation in `docs/`; CLAUDE.md and TESTING.md
  translated with all their history.
- **Limits of work per pass**: if the measurement shows they are needed, I choose generous numbers that never change what
  appears in the world (they only spread the work in time). (Not needed: see `docs/TESTING.md`, 0.9.2.)
