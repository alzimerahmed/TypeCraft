# TypeCraft — a private, smart, deeply customizable Android keyboard

<div align="center">

[![Release](https://img.shields.io/github/v/release/alzimerahmed/TypeCraft?style=flat-square&logo=github&label=Release)](https://github.com/alzimerahmed/TypeCraft/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/alzimerahmed/TypeCraft/total?style=flat-square&logo=github&label=Downloads)](https://github.com/alzimerahmed/TypeCraft/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7f52ff?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-5.0%2B-3ddc84?style=flat-square&logo=android)](https://developer.android.com)
[![License: GPL v3](https://img.shields.io/badge/License-GPL_v3-blue?style=flat-square)](LICENSE)

*An open-source keyboard forked from OpenBoard / AOSP LatinIME, extended with on-device AI — voice typing, handwriting, OCR, and proofreading — while keeping your data on your device.*

[Download](#download) • [Features](#features) • [Building](#building)

</div>

---

## Features

- **AI proofreading** — Google Gemini, Groq, OpenAI, or any self-hosted OpenAI-compatible endpoint (Ollama, LM Studio); offline GGUF models via plugin on the offline flavor
- **On-device voice typing** — quantized Whisper models via the companion voice plugin; no audio leaves the device
- **Handwriting input** — draw characters and words on a full-width canvas (ML Kit Digital Ink plugin)
- **OCR** — in-keyboard camera viewfinder and screenshot text extraction
- **Inline math** — type `25*4=` and get the answer in the suggestion strip
- **Glide typing** — native gesture decoding with a three-tier library fallback
- **Clipboard tools** — searchable history, pinning, text expander with variables (`%date%`, `%clipboard%`), OTP detection
- **Customization** — themes, 12+ keypress sound packs, custom layouts, floating and resizable keyboard, touchpad cursor mode
- **Dictionaries on demand** — language dictionaries download on first use instead of bloating the APK

## Flavors

| | Standard Full | Standard (FOSS) | Offline |
| :--- | :---: | :---: | :---: |
| Cloud AI | Yes | Yes | No |
| Offline AI (GGUF plugin) | No | No | Yes |
| Voice / Handwriting / OCR / Translation plugins | Yes | Yes | Yes (file import) |
| Internet permission | Optional | Optional | None |
| Min Android | 6.0 | 6.0 | 5.0 |

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| Language | Kotlin, C++ (JNI) |
| UI | Android Views + Jetpack Compose, Material 3 |
| Build | Gradle 8.13, AGP, NDK ndk-build |
| AI / ML | Gemini / Groq / OpenAI APIs, llama.cpp, whisper.cpp, ML Kit |
| IPC | AIDL plugin system for voice, OCR, handwriting, translation, offline AI |

## Project Structure

```
app/src/main/java/alzimerahmed84/keyboard/
├── latin/          # core IME service, input logic, dictionaries, plugins
│   ├── ai/ ocr/ handwriting/ translation/ voice/   # plugin integrations
│   └── plugin/     # shared plugin loader core
├── keyboard/       # keyboard view, themes, emoji
├── settings/       # settings UI (Compose screens)
└── event/ compat/ accessibility/
app/src/main/jni/   # native decoder (ProximityInfo, BinaryDictionary)
tools/              # release scripts, emoji key generator
fastlane/           # store metadata
```

## Download

Grab the latest APKs from [GitHub Releases](https://github.com/alzimerahmed/TypeCraft/releases/latest). Three flavors are published per release: `standard` (FOSS), `standardfull` (recommended), and `offline` (no internet permission). If your browser blocks APK installs, use [Obtainium](https://apps.obtainium.imranr.dev/redirect.html?r=obtainium://add/https://github.com/alzimerahmed/TypeCraft).

## Building

```bash
git clone https://github.com/alzimerahmed/TypeCraft.git
cd TypeCraft
./gradlew assembleStandardfullDebug
```

Requires JDK 17 and the Android SDK with NDK 28. On Windows, a checkout path containing spaces breaks ndk-build — use `subst X: <path>` and build from `X:\`. See [CONTRIBUTING.md](CONTRIBUTING.md) for details and test commands.

## Contributing

Fork, create a branch, and open a PR describing the change. Keep PRs to a single concern. Translations for new string keys are welcome directly as PRs; existing strings are managed on Weblate. See [CONTRIBUTING.md](CONTRIBUTING.md).

## Changelog

See [GitHub Releases](https://github.com/alzimerahmed/TypeCraft/releases).

## License

TypeCraft is licensed under the GNU General Public License v3.0 — see [LICENSE](LICENSE). Portions carry Apache-2.0 and CC-BY-SA-4.0 licenses ([LICENSE-Apache-2.0](LICENSE-Apache-2.0), [LICENSE-CC-BY-SA-4.0](LICENSE-CC-BY-SA-4.0)).

TypeCraft is maintained by Alzimer Ahmed ([alzimerahmed84@gmail.com](mailto:alzimerahmed84@gmail.com)).
