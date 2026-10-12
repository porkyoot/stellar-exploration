<div align="center">

<img src="src/main/resources/assets/stellar_exploration/icon.png" alt="Stellar Exploration Icon" width="128" height="128" />

# 🧭 Stellar Exploration

**Purely client-side exploration framework, telemetry, and navigation mod for Minecraft.**

[![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2+%20%2F%2026.2-blue.svg)](https://www.minecraft.net/)
[![Loader](https://img.shields.io/badge/Loader-Quilt-purple.svg)](https://quiltmc.org/)
[![Language](https://img.shields.io/badge/Kotlin-2.4-orange.svg)](https://kotlinlang.org/)
[![Java](https://img.shields.io/badge/Java-21-red.svg)](https://adoptium.net/)
[![Environment](https://img.shields.io/badge/Environment-100%25%20Client--Side-blueviolet.svg)](#-technical-details)

</div>

---

## 📖 About

**Stellar Exploration** is an extensible client-side framework and navigation suite in the Stellar mod ecosystem. It provides unified exploration telemetry, an extensible exploration feature architecture, HUD telemetry providers, and navigation aids without requiring any server-side installations.

---

## ⚙️ Architecture & Features

* **Exploration Framework (`ExplorationEngine`):** Central registry and lifecycle coordinator for client-side exploration modules.
* **Extensible Feature SPI (`ExplorationFeature`):** Pluggable interface for exploration components with clean lifecycle dispatch (`onInitialize`, `onTick`, `onRender`).
* **Real-time Exploration Telemetry (`ExplorationTelemetry`):** Immutable snapshots of coordinates, cardinal directions, biome, and dimension.
* **Cloth Config & ModMenu:** Fully integrated GUI configuration with hot-reloading settings and cloth config UI.
* **Pure Client Execution:** Zero server-side packets or requirements; safe on any multiplayer server.

---

## 🛠️ Build & Development

```bash
# Build mod JAR
./gradlew :stellar-exploration:build

# Run tests
./gradlew :stellar-exploration:test

# Run Detekt linter
./gradlew :stellar-exploration:detekt
```
