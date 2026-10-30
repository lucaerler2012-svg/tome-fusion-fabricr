# Tome Fusion

Kombiniert verzauberte Bücher im Amboss durch **Addition der Level**, begrenzt auf **Vanilla-Maximum x 2**.
Fabric, Minecraft 26.3, Java 25.

**Status: frühes Skeleton. Alle `TODO`-Kommentare im Code abarbeiten (Kosten, Umbenennen, Tests).**

## Struktur
```
build.gradle / settings.gradle / gradle.properties / gradlew / gradle/
src/main/resources/fabric.mod.json, tomefusion.mixins.json
src/main/java/io/github/lucaerler2012svg/tomefusion/TomeFusion.java      <- Einstiegspunkt
src/main/java/io/github/lucaerler2012svg/tomefusion/BookMerger.java      <- Level addieren + Limit
src/main/java/io/github/lucaerler2012svg/tomefusion/mixin/AnvilMenuMixin.java <- Amboss-Eingriff
.github/workflows/build.yml                                              <- Build auf GitHub
```

## Bauen
- Lokal: JDK 25, dann `./gradlew build` -> `build/libs/tome-fusion-0.3.0.jar` (NICHT die `-sources.jar`).
- GitHub: jeder Push startet den Build. Die Jar liegt unter Actions -> Build anklicken -> Artifacts.

## Auf Modrinth veröffentlichen
1. modrinth.com -> Create a project -> Mod. Name, Slug `tome-fusion`, Beschreibung (siehe `MODRINTH.md`), Lizenz MIT.
2. Environment: Client required, Server required (läuft im Singleplayer auf dem integrierten Server).
3. Versions -> Create version: Jar hochladen, Version 0.3.0, Loader Fabric, Game version 26.3, Dependency Fabric API (required).
4. Zur Prüfung einreichen.
