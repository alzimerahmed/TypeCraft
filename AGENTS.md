# Project Rules — TypeCraft

This project uses a structured agent workflow system defined in `.devin/`. Read the system map before starting any task.

## Entry Point (read before starting work)

1. **`.devin/prompt/quick.md`** — the 8 commandments every task follows
2. **`.devin/prompt/map.md`** — system map: reading order, task lifecycle, agent delegation, write-back order
3. **`.devin/prompt/rules.md`** — quick-task scoping, verification, escalation rules
4. **`.devin/prompt/phase.md`** — phased plan, design quality gate (§9), report format (§10), definition of done (§11)

## Task Lifecycle

1. **Understand** — README → `docs/project.md` → `docs/agent.md` → `docs/plan.md`
2. **Scope check** — if too big (DB migration, >5 files, new dependency, API/security change), escalate to `.devin/prompt/phase.md`
3. **Plan minimally** — read target files + callers
4. **Implement** — follow `.devin/prompt/rules.md` + design gate (`.devin/prompt/phase.md` §9), consult `docs/research.md` for tech decisions, reuse existing patterns/tokens
5. **Verify** — `./gradlew assembleDebug` + tests + light/dark theme check
6. **Code review** — run the `code-reviewer` subagent on the final diff before declaring done
7. **Document** — update `docs/project.md` + `docs/agent.md` + `docs/research.md`; report changes, files, verification, follow-ups

## Key Resources

| Resource | Location | Purpose |
|----------|----------|---------|
| Prompt engine | `.devin/prompt/` | Task instructions, system map, phased plan, quick-task rules |
| Domain rules | `.devin/rules/` (39 files) | Mandatory rules for each domain (security, testing, design, etc.) |
| Agents | `.devin/agents/` (38 files) | 38 domain specialist subagent profiles (spawnable by Devin) |
| Orchestrators | `.devin/orchestrators/` (9 files) | 9 main orchestrator reference docs (not subagent profiles) |
| Skills | `.devin/skills/` (38 dirs) | Invokable via `/skill-name` slash commands |
| Workflows | `~/.codeium/windsurf/windsurf/workflows/` | 38 step-by-step execution plans (global) |
| Knowledge | `docs/` | Per-project: `project.md`, `agent.md`, `plan.md`, `research.md` |

## Conventions

- **Conventional commits**: `type(scope): description` (e.g., `fix(reader): handle empty article body`)
- **All documentation** goes in `docs/` — never scatter `.md` files in repo root or source folders
- **`.devin/` and `docs/` are gitignored** — private, never pushed to GitHub
- **Run tests** before declaring any task done
- **Code review required** on the final diff before completion
- **No secrets** in code — API keys, tokens, passwords go in gitignored config files
- **Design quality gate** (`.devin/prompt/phase.md` §9): Material 3, dark mode, 48dp touch targets, WCAG AA contrast, polished empty/loading/error states
- **Ownership**: Alzimer Ahmed (alzimerahmed84@gmail.com)

## Agent Delegation

When a task touches a specific domain, delegate to the relevant subagent:
- **Code review** → `code-reviewer` subagent (required on final diff)
- **Security** → `security-auditor` subagent
- **Testing** → `test-engineer` subagent
- **Performance** → `performance-engineer` subagent
- **Accessibility** → `a11y-specialist` subagent
- **Debugging** → `debugger` subagent
- **Documentation** → `docs-writer` subagent

Priority when agents disagree: **security > performance > design > DX**.

## Project Context

**TypeCraft** is a private, smart, and deeply customizable open-source Android keyboard. Forked from OpenBoard / AOSP LatinIME. Built with Kotlin, Jetpack Compose, Material 3. Three flavors: Standard Full, Standard FOSS, and Offline. Features include multi-provider cloud AI, on-device Whisper voice typing, handwriting recognition, OCR, inline math calculations, custom sound packs, and a self-updater.

- **Build**: `./gradlew assembleDebug`
- **Package**: `alzimerahmed84.keyboard.latin`
- **License**: GPL v3
- **Repo**: github.com/alzimerahmed84/TypeCraft
