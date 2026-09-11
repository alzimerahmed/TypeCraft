# Rules for Quick Tasks (companion to `quick.md`)

These rules govern every task executed via `quick.md`. They exist so quick tasks ship fast **without** violating the quality standards of our full phase system (`phase.md`) or the agent system (`.devin/rules/35-agent-system.md`).

## 1. Scoping Rules

- **One task, one outcome.** A quick task must have a single, clearly defined deliverable. If the task expands into multiple features, stop and split it into separate quick tasks or escalate it to the phased plan (`phase.md`).
- **No scope creep.** Fix only what the task asks. If you discover adjacent problems, note them in the completion report — do not fix them silently.
- **Minimal upstream fixes.** Prefer fixing the root cause with the smallest possible change over downstream workarounds. No over-engineering: if a single-line change is sufficient, make a single-line change.
- **Respect existing architecture.** Quick tasks must not introduce new architectural patterns, new dependencies, or new modules. Work within the existing structure (MVVM, Hilt, Room, Compose conventions already in the codebase).

## 2. Before Editing

- **Read before write.** Always read the target file(s) and their callers/consumers before editing. Never edit from assumption.
- **Check for existing patterns.** Reuse existing utilities, components, design tokens, and ViewModels. Duplicating logic that already exists is a blocker.
- **Consult the docs — if they exist.** Read `docs/project.md`, `docs/agent.md`, and `docs/plan.md` for past implementations, known constraints, and current phase status; check `docs/research.md` for prior tech decisions. These are per-project files: if absent, they will be created by the phase flow (phase.md §3–7) — don't block on them.

## 3. During Implementation

- **Follow code style.** Match the surrounding code exactly — naming, formatting, comment density (no new comments unless asked), and Kotlin/Compose idioms.
- **Design quality gate applies.** Even quick UI changes must follow the design gate in `phase.md` (§9): design tokens only (no hardcoded colors/spacing), Material 3, dark mode support, 48dp touch targets, and polished empty/loading/error states.
- **No weakened tests.** Never delete or weaken existing tests to make a change pass. Add a regression test for every bug fix when a test harness exists.
- **Conventional commits.** One logical change per commit, message format: `type(scope): description` (e.g., `fix(reader): handle empty article body`).

## 4. Verification Rules

- **Build must pass.** No task is done until `./gradlew assembleDebug` succeeds.
- **Tests must pass.** Run the relevant test module(s); if the change touches core logic, run the full suite.
- **Verify in context.** For UI changes, confirm the change renders correctly in both light and dark themes and does not regress adjacent screens.
- **Agent verification.** Run the code-review sub-agent (`.devin/rules/05-code-review.md`) on the diff before declaring the task complete. Quick tasks skip nothing except ceremony — Blockers and Criticals must still be resolved.

## 5. Documentation & Handoff Rules

- **Research before new tech.** Before adding any dependency, API, or unfamiliar pattern, consult `docs/research.md` (phase.md §6) — check prior decisions and record the new decision, alternatives considered, and rationale there.
- **Update `docs/project.md` and `docs/agent.md`** at the end of the task with what was implemented and any decisions made. Append any new technical findings, decisions, or gotchas to `docs/research.md`.
- **All new documentation goes in `docs/`** — never scatter `.md` files in the repo root or source folders.
- **Gitignore hygiene.** `.devin/` and `docs/` must always be gitignored (never pushed to GitHub). Keep `.gitignore` minimal per phase.md §13 — only real artifacts, no dead entries.
- **Completion report.** End every quick task with a brief summary: what changed, files touched, verification results, and any noted follow-ups.

## 6. Escalation Rules

Escalate out of quick-task mode (into a planned phase) when:
- The change requires a database migration or schema change.
- The change touches more than ~5 files or crosses module boundaries.
- The change requires a new third-party dependency.
- The change alters public APIs, security behavior, or data handling.
- Verification cannot be completed in the current environment.
