# System Map — How the Prompt System Connects

Referenced by `quick.md` (point 8). Everything flows from `quick.md` — it is the **entry point**. Read the system in the order defined below before starting any task.

## 0. Visual Overview

```
                                ┌─────────────────────┐
                                │      quick.md       │  ← ENTRY POINT
                                │  (task instructions) │
                                └──────────┬──────────┘
                                           │
        ┌──────────────────┬───────────────┼────────────────┬─────────────────┐
        ▼                  ▼               ▼                ▼                 ▼
┌───────────────┐  ┌──────────────┐  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐
│  ORCHESTRATION │  │    PROMPT    │  │    RULES    │  │  KNOWLEDGE  │  │    AGENTS    │
│   (.devin/)    │  │   LAYER      │  │   LAYER     │  │  LAYER      │  │   & SKILLS   │
├───────────────┤  ├──────────────┤  ├─────────────┤  ├─────────────┤  ├──────────────┤
│ agents/       │  │ phase.md     │  │ rules.md    │  │ README.md   │  │ sub-agents/  │
│ sub-agents/   │  │ (phased plan │  │ (quick-task │  │ project.md  │  │ skills/      │
│ workflows/    │  │  + design    │  │  rules)     │  │ agent.md    │  │ workflows/   │
│ skills/       │  │  gate)       │  │             │  │ plan.md     │  │ rules/ (39)  │
│               │  │ map.md (you  │  │             │  │ idea.md     │  │              │
│               │  │  are here)   │  │             │  │ research.md │  │              │
└───────────────┘  └──────────────┘  └─────────────┘  └─────────────┘  └──────────────┘
        │                  │               │                │                 │
        └──────────────────┴───────────────┴───────┬────────┴─────────────────┘
                                                   ▼
                                     ┌──────────────────────────┐
                                     │     TASK LIFECYCLE       │
                                     │                          │
                                     │  Understand → Scope? ──┐ │
                                     │      │        │        │ │
                                     │      │     too big      │ │
                                     │      │        └─→ Escalate to phase.md
                                     │      ▼                 │ │
                                     │  Implement → Verify →  │ │
                                     │  Review ↺ → Document → │ │
                                     │  ✅ Done               │ │
                                     └──────────────────────────┘
```

```
File tree:
.devin/
├── agents/            ← 9 orchestrators (Quality, Design, Infra, Docs Engineers…)
├── sub-agents/        ← 37 domain specialists
├── skills/            ← deep methodology per domain
├── workflows/         ← executable step-by-step plans (/slash-commands)
├── rules/             ← 39 mandatory domain rules (35-agent-system.md = index)
└── prompt/
    ├── quick.md       ← entry point for quick tasks
    ├── phase.md       ← phased plan + research system (§6) + design quality gate (§9)
    ├── rules.md       ← quick-task scoping / verification / escalation rules
    └── map.md         ← this file — the system map

docs/                  ← per-project knowledge — FRESH for every new project
├── project.md         ← project state & structure
├── agent.md           ← past implementations & decisions
├── plan.md            ← phase status & ordering
├── idea.md            ← competitive analysis (what to build)
└── research.md        ← technical research: libraries, best practices, ADRs, gotchas (how to build)

NOTE: .devin/prompt/ is the portable, project-agnostic engine — copy it into any new project as-is.
docs/ contents are disposable per-project artifacts, created fresh by following phase.md points 3–7.
```

## 1. Document & Resource Map

```mermaid
graph TD
    QUICK["📌 quick.md<br/>(entry point — you are here)"]

    subgraph ORCHESTRATION["⚙️ Orchestration Layer (.devin/)"]
        AGENTS["Agents / Sub-Agents<br/>.devin/agents/, .devin/sub-agents/<br/>orchestrators + 37 domain specialists"]
        SKILLS["Skills / Workflows<br/>.devin/skills/, .devin/workflows/<br/>methodology + execution steps"]
        RULES["Rules<br/>.devin/rules/ (39 rule files)<br/>mandatory domain rules"]
        PRULES["rules.md<br/>(quick-task rules: scoping,<br/>verification, escalation)"]
    end

    subgraph PROMPTS["📝 Prompt Layer (.devin/prompt/)"]
        PHASE["phase.md<br/>(phased plan + design quality gate)"]
    end

    subgraph DOCS["📚 Knowledge Layer (docs/)"]
        README["README.md<br/>(project overview)"]
        PROJECT["project.md<br/>(project state & structure)"]
        AGENTMD["agent.md<br/>(past implementations & decisions)"]
        PLAN["plan.md<br/>(phase status & ordering)"]
        IDEA["idea.md<br/>(competitive analysis)"]
        RESEARCH["research.md<br/>(technical research & ADRs)"]
    end

    QUICK --> ORCHESTRATION
    QUICK --> PROMPTS
    QUICK --> DOCS
    PHASE --> PLAN
    PHASE --> RESEARCH
    PHASE --> PRULES
```

## 2. Task Lifecycle — How a Quick Task Actually Flows

```mermaid
graph TD
    START["Receive task"] --> UNDERSTAND["1️⃣ Understand<br/>README → project.md → agent.md → plan.md"]
    UNDERSTAND --> SCOPE{"2️⃣ Scope check<br/>(rules.md §6 escalation)"}
    SCOPE -- "too big: DB migration, >5 files,<br/>new dependency, API/security change" --> ESCALATE["⬆️ Escalate: add as a new phase<br/>in docs/plan.md, then execute<br/>via the phase.md flow"]
    SCOPE -- "fits quick task" --> PLAN2["3️⃣ Plan minimally<br/>read target files + callers"]
    PLAN2 --> IMPLEMENT["4️⃣ Implement<br/>follow rules.md + design gate (phase.md §9)<br/>consult research.md for tech decisions<br/>reuse existing patterns/tokens"]
    IMPLEMENT --> VERIFY["5️⃣ Verify<br/>./gradlew assembleDebug + tests<br/>light/dark check"]
    VERIFY --> REVIEW{"6️⃣ Code-review sub-agent<br/>(rules/05-code-review.md)"}
    REVIEW -- "Blocker/Critical" --> IMPLEMENT
    REVIEW -- "clean" --> DOCS2["7️⃣ Document<br/>update project.md + agent.md + research.md<br/>report in docs/"]
    DOCS2 --> DONE["✅ Done — report:<br/>changes, files, verification, follow-ups"]

    DOCS2 -. "lessons learned" .-> LESSONS["Convert durable lessons into<br/>rules.md / map.md updates;<br/>log in CHANGELOG.md"]
```

## 3. Agent Delegation Map

```mermaid
graph LR
    QUICK["quick.md"] --> QE["Quality Engineer<br/>code review, debug, testing,<br/>security, performance, a11y"]
    QUICK --> DE["Design Engineer<br/>UI/UX, content, animation,<br/>design system, i18n"]
    QUICK --> IE["Infrastructure Engineer<br/>git, build, DX, deploy"]
    QUICK --> DE2["Docs Engineer<br/>documentation, type safety"]
    QE --> SA["code-reviewer · debugger · test-engineer<br/>security-auditor · performance-engineer · a11y-specialist"]
    DE --> DS["frontend-designer · content-writer ·<br/>animation-engineer · design-system-builder"]
    IE --> GI["git-master · build-optimizer · dx-optimizer"]
    DE2 --> DW["docs-writer · type-safety-engineer"]
```

Full mapping: `.devin/rules/35-agent-system.md`. Priority when agents disagree: **security > performance > design > DX**.

## 4. Reading Order

1. `README.md` — what the project is
2. `docs/project.md` — current project state and structure (if absent, it will be created per phase.md §3)
3. `docs/agent.md` — past implementations and decisions (if absent, it will be created)
4. `docs/plan.md` — where we are in the phased plan (if absent, it will be created per phase.md §7)
5. `.devin/prompt/phase.md` — standards every change must meet (research system §6, design gate §9, report format §10, definition of done §11)
6. `.devin/prompt/rules.md` — quick-task scoping, verification, and escalation rules
7. Relevant `.devin/rules/*` for the task domain (see rule index in `35-agent-system.md`)
8. `docs/idea.md` — what to build (competitive analysis; created per phase.md §5)
9. `docs/research.md` — how to build it (tech decisions, best practices, gotchas) — consult before adding any dependency or pattern (if absent, create from the template in phase.md §6)

## 5. Write-Back Order (task end)

1. `docs/project.md` — update project state
2. `docs/agent.md` — record what was implemented and decisions made
3. `docs/research.md` — append any new tech findings, decisions, or gotchas encountered
4. Task-specific docs — always inside `docs/`
5. Completion report: what changed, files touched, verification results, follow-ups
