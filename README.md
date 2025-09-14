# eindino-bedwars

> Heads up: this is an older project I wrote a while ago. It was originally built for the einDino infrastructure. If you run it elsewhere, expect to tweak configs, dependencies, and maybe some code.

### What this is
This is my BedWars plugin for PaperMC. It includes lobby mechanics, team setup, map voting, a shop, scoreboards, a spectator mode, a Top‑5 display, and a MySQL‑backed stats system (via my `ServerAPI`).

- Main class: `de.t0bx.eindino.BedWarsPlugin`
- Paper API: `1.21.4`
- Build: Maven (shade + optional ProGuard obfuscation)
- Java target: `24` (can be adjusted in `pom.xml` if you run on 21)

### Features (short list)
- Team presets for multiple play types (e.g. 2x2, 4x2, 4x4, 8x2)
- Map voting and a simple navigator inventory
- In‑game shop inventory
- Scoreboards and colored team names via MiniMessage
- Spectator handling
- Top‑5 heads + signs in the lobby (fixed coordinates)
- Player stats: K/D, wins, beds destroyed, placement (stored in MySQL)

### Requirements
- PaperMC `1.21.4` (or compatible)
- A Java runtime that matches the compiled bytecode (default target is 24)
- MySQL reachable via the einDino `ServerAPI` (or replace the DB layer for your setup)
- Hard deps from `plugin.yml` you should have on your server:
  - `ServerAPI`
  - `CloudNet-Bridge`
  - `Multiverse-Core`
  - `SentienceEntity`
  - `LuckPerms`

### Dependencies (Maven)
See `pom.xml`. Notable ones:
- `de.eindino.api:ServerAPI:0.0.9-SNAPSHOT` (scope: provided, classifier: shaded)
- `io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT` (provided)
- `eu.cloudnetservice.cloudnet:driver-api` and `bridge-api` (4.0.0-RC12, provided)
- `net.luckperms:api:5.4` (provided)
- Local system dependency: `libs/NickSystem-1.0.0-shaded.jar`

Repos like `https://repo.papermc.io` and project‑specific ones (einDino, t0bx) are configured in `pom.xml`.

### Install (server)
1. Build it (see Build section).
2. Drop the produced JAR into your Paper `plugins/` folder. You can use the shaded JAR or the `-obf.jar` if you want the obfuscated one.
3. Make sure the `plugin.yml` dependencies are installed and enabled.
4. Configure `ServerAPI` so MySQL is reachable. The plugin will auto‑create `bedwars_players`.
5. Restart your server.

#### Lobby / world assumptions
- A world named `world` is expected. The plugin uses static lobby coordinates (heads/signs/holograms).
- Set a lobby spawn with `/setspawn`.

### Commands & permissions
From `plugin.yml` and code:
- `/setspawn` — permission: `bedwars.admin`
- `/map` — permission: `bedwars.admin`
- `/troll` — permission: `bedwars.admin`
- `/start` — permission: `bedwars.vip`
- `/forcemap` — permission: `bedwars.vip`
- `/stats [player]` — no specific perm set in `plugin.yml`; shows your or another player's stats

### Config
- Play type / teams are driven by `ConfigManager` (property like `2x2`, `4x2`, `4x4`, `8x2`).
- On first run, the plugin creates its data folder and initializes teams/scoreboard based on the play type.
- Colors, team names, and display formats can be adjusted in the source if you want different defaults.

### Database
- Table: `bedwars_players` with columns `uuid`, `kills`, `deaths`, `wins`, `gamesPlayed`, `bedsDestroyed` (auto‑created).
- Access is via `ServerAPI.getInstance().getMySQLManager()`.

### Build
Requires Maven and a suitable JDK.

```bash
mvn clean package
```

Artifacts in `target/`:
- `eindino-bedwars-1.0.0.jar` (base)
- `eindino-bedwars-1.0.0-shaded.jar` (recommended for deployment)
- `eindino-bedwars-1.0.0-obf.jar` (obfuscated, optional)

> If your production server isn’t on Java 24, adjust the `maven-compiler-plugin` (`source`/`target`) in `pom.xml` (e.g. 21) and rebuild.

### Dev notes
- Use any IDE (I used IntelliJ). Import as a Maven project.
- Paper API and other deps are `provided` and come from the server at runtime.
- Local test: Paper 1.21.4 with the plugins listed in `plugin.yml`.

### License / usage
There’s no explicit license file here. If you plan to redistribute or use parts of it publicly, please reach out.

### Disclaimer
Again: this is older code and has infra‑specific assumptions. Running it outside the original einDino environment may need changes in code, config, and dependencies. That’s normal for legacy server plugins like this. Have fun. 👍
