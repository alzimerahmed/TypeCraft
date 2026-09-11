# Prompt System Changelog

Every change to the files in `.devin/prompt/` is logged here: date, file, what changed, and why. This lets us see how the system evolved and roll back bad rules.

## 2026-09-08
- **phase.md** — added SWE audit focus to competitive analysis (§5); added `research.md` technical research system with template (§6); added phase-completion criteria (§8), design quality gate (§9), report format (§10), project definition of done (§11).
- **rules.md** — created: scoping, pre-edit, implementation, verification, documentation, and escalation rules for quick tasks.
- **quick.md** — added references to `rules.md` (§5) and `map.md` (§8); clarified agent usage (§1) and `research.md` usage (§6).
- **map.md** — created: system map with visual overview, document map, task lifecycle, agent delegation, reading order, and write-back order; integrated `research.md` throughout.

## 2026-09-08 (later)
- **Portability restructure** — `docs/` contents (`idea.md`, `research.md`, `plan.md`, `project.md`, `agent.md`) are now defined as per-project artifacts, created fresh for each new project; `.devin/prompt/` is the portable, project-agnostic engine.
  - **phase.md** — new §12 (portability); §5–7 and §11 now reference `docs/` paths with "create fresh if absent" instructions; research.md template serves as the bootstrap for new projects.
  - **map.md** — file tree marks `docs/` as fresh-per-project with a portability note; reading order entries made conditional ("if absent, it will be created").
  - **rules.md** — §2 doc-reading made conditional; agents must not block on missing per-project docs.
  - **quick.md** — point 6 updated: create `project.md`/`agent.md` fresh if absent.
- **phase.md** — new §13 (gitignore system): always ignore `.devin/` and `docs/`; keep `.gitignore` minimal — only real artifacts, no dead entries.
- **phase.md** — new §14 (README system): signature personal template — centered cover (logo, shields.io badges, tagline, inline nav), plain section headers, scannable Features/Screenshots/Tech Stack tables, ≤3-step Quick Start with collapsible advanced setup, Roadmap checklist, License; tagline must answer what/who/why in the first 50 words; explicit anti-slop rule: no emojis, no marketing fluff or buzzwords — facts only; README kept current with feature changes.
- **phase.md** — README system upgraded: added Project Structure (ASCII tree), Usage example, FAQ/Troubleshooting (real content only), Contributing, Changelog link sections; Mermaid architecture diagram option for complex projects; alt-text requirement; discipline rules — GitHub About panel sync, optional sections earned not defaulted, above-the-fold first screen, mandatory real screenshots for UI projects, Table of Contents for READMEs >6 sections.
