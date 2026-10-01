# Legacy Emotecraft (1.12.2 Forge)

[![Minecraft 1.12.2](https://img.shields.io/badge/Minecraft-1.12.2-blue.svg)](https://www.minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-14.23.5.2860-orange.svg)](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.12.2.html)
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL_v3-blue.svg)](LICENSE)

A backport of **Emotecraft 2.4.12** to **Minecraft 1.12.2 Forge**, bringing modern custom animations, bendable cuboids, and rich emote playback to 1.12.2.

---

## ✨ Features

- **Full Modern Emotecraft Support**: Play custom emotes created for modern Emotecraft versions (`.json` format).
- **Bendable Limbs & Body Parts**: Built-in bendable cuboids support for smooth, realistic body curvature and limb deformation during animations.
- **Audio & NBS Playback**: Supports emote sound effects and Noteblock Studio (`.nbs`) music synchronized with animations.
- **Hot-Reloading**: Automatically detects new or edited emotes placed in your `emotes/` folder without needing to restart the game.
- **Interactive Radial & Full Menus**:
  - **Fast Choose Wheel**: Quick radial menu for favorite emotes (default key: `B`).
  - **All Emotes Screen**: Browse, preview, configure, and assign emotes to hotkeys or the wheel (default key: `H`).
- **Legacy Female Gender Mod (LFGM) Integration**: Realistic breast physics that dynamically react to player model rotation, torso tilt, bending, and motion during emote animations.
- **Multiplayer Synchronization**: Server-side and client-side networking for playing and syncing emotes with other players.

---

## 📋 Requirements

- **Minecraft**: `1.12.2`
- **Minecraft Forge**: `14.23.5.2860` or newer (Cleanroom Loader also supported)
- **Dependencies**:
  - [MixinBooter](https://www.curseforge.com/minecraft/mc-mods/mixinbooter) (v10.5 or newer)

---

## 🎮 Default Controls

| Action | Default Key |
|---|---|
| Open Emote Wheel (Fast Menu) | `B` |
| Open Full Emotes Menu | `H` |
| Stop Current Emote | `C` |

Keybindings can be customized in Minecraft's **Controls** settings under the **Emotecraft** category.

---

## 🛠️ Building from Source

To compile the mod yourself:

```bash
git clone https://github.com/azubangithub/Legacy-EmoteCraft.git
cd Legacy-EmoteCraft
./gradlew build
```

The compiled mod JAR will be located in `build/libs/`.

---

## 👥 Credits & Authors

- **1.12.2 Forge Port**: [azuban](https://github.com/azubangithub)
- **Original Mod Authors**:
  - **KosmX** (Original Creator & Main Developer)
  - **dima_dencep**
  - **ZigyTheBird**
  - **Kale Ko**

Original Mod Repository: [KosmX/emotes](https://github.com/KosmX/emotes)

---

## 📄 License

This project is licensed under the [GNU Lesser General Public License v3.0 (LGPL-3.0)](LICENSE) in accordance with the original mod's licensing.
