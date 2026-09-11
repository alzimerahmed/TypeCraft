1) Clone the source repository into the root of this project — not a subfolder — and fully detach it from its origin (remove all remote links, git history references, and upstream configuration so it stands alone).

2) Transfer complete ownership of the repository to me. Replace every credential, author attribution, and ownership reference throughout the codebase with my details:
   - Name: Alzimer Ahmed
   - Email: alzimerahmed84@gmail.com
   Perform a full sweep of the repo (READMEs, package manifests, license files, config files, CI workflows, docs, and commit metadata) to locate all ownership info and replace it.

3) Analyze the codebase thoroughly: map the project structure, understand the core functionality, and document the existing feature set before making any changes.

4) Clean up the repository — remove all unused files, dead code, and redundant folders that add no value to the project.

5) Research best-in-class apps in the same domain and compile a competitive analysis into `docs/idea.md` (create it fresh if it doesn't exist), covering: quality-of-life improvements, missing features, functional enhancements, design systems, UX patterns, and layout/architecture improvements we could adopt. Approach this as a software engineering audit, not just a feature wishlist:
   - **Architecture review**: assess the current app's architecture (layering, separation of concerns, dependency injection, data flow patterns) against modern Android best practices (e.g., MVVM/Clean Architecture, unidirectional data flow, repository pattern). Identify structural weaknesses and refactoring opportunities.
   - **Code quality assessment**: flag tech debt — duplicated logic, god classes, long methods, poor naming, missing abstractions, inconsistent error handling, and hardcoded values that should be resources or constants.
   - **Testing strategy**: evaluate existing test coverage (unit, integration, UI). Identify untested critical paths and propose a testing pyramid plan with tooling (JUnit, Robolectric, Compose testing, Turbine for flows).
   - **Performance & reliability**: note opportunities for profiling-driven improvements — startup time, memory leaks, main-thread I/O, redundant recompositions in Compose, database query efficiency, and network caching.
   - **Maintainability & DX**: recommend improvements to module structure, build configuration, lint/static analysis (ktlint, detekt, Android Lint), CI pipelines, and documentation so the codebase stays maintainable as features grow.
   - **Scalability & extensibility**: identify where feature additions would currently require invasive changes, and propose abstractions or patterns (interfaces, use cases, feature modules) that make future phases cheaper to implement.
   For each competitor analyzed, also note which engineering practices they likely use to deliver their UX quality, so the plan can adopt both features and the engineering discipline behind them.

6) Maintain a living technical research system in `docs/research.md` (create it fresh if it doesn't exist) — this is distinct from the competitive analysis in `idea.md`: `idea.md` captures *what* to build, `research.md` captures *how* to build it well. Structure it as:
   - **Technology & library research**: for every new dependency, API, or platform capability a phase needs, research the current best option — compare alternatives on maintenance status, community health, API stability, performance, license, and compatibility with our stack (Kotlin, Compose, Room, Hilt). Record the decision and rationale so future phases don't re-litigate it.
   - **Best-practice research**: for each implementation domain (networking, caching, background work, link metadata extraction, security), gather current official documentation and community-accepted patterns before writing code. Cite sources (official docs, ADRs, well-regarded OSS implementations).
   - **Gotchas & pitfalls**: document known issues, version-specific bugs, and breaking changes encountered during implementation so they are never rediscovered the hard way.
   - **Decision records**: for every significant technical choice, append a short entry — context, options considered, decision, consequences (a lightweight ADR format).
   - **Open questions**: track unresolved technical questions and revisit them before the phases that depend on them.
   Update `research.md` continuously during every phase — before implementation (research), during (findings), and after (retrospective notes). It is a required input when planning any new phase.
   If `docs/research.md` does not exist yet, create it fresh using this exact template:
   ```markdown
   # Technical Research

   ## Technology & Library Decisions
   | Date | Decision | Alternatives Considered | Rationale |
   |------|----------|------------------------|-----------|

   ## Best Practices & Sources
   (domain → pattern adopted → source link)

   ## Gotchas & Pitfalls
   (issue → cause → fix/workaround)

   ## Decision Records (ADR)
   ### ADR-001: <title>
   - Context:
   - Options:
   - Decision:
   - Consequences:

   ## Open Questions
   - [ ] <question> (blocking: <phase>)
   ```

7) Based on `docs/idea.md` and `docs/research.md`, produce a comprehensive, phased plan of action into `docs/plan.md` (create it fresh if it doesn't exist) detailing how each improvement and enhancement will be implemented, with clear scope and ordering.

8) Execute the plan phase by phase. After completing each phase, run a code review using the agents defined in `.devin` before moving on to the next one. A phase is only considered complete when:
   - The project builds successfully (`./gradlew assembleDebug`) and the full test suite passes.
   - A short phase-completion report is written to `docs/` summarizing what was implemented, files touched, and any known limitations or follow-ups.
   - A retrospective is written: what went wrong, what was slow, what to avoid next time. Convert durable lessons into new rules in `rules.md` or updates to `map.md` — the system must learn from every phase, not repeat mistakes. Log prompt-system changes in `.devin/prompt/CHANGELOG.md`.

9) Design quality gate — every UI-facing change must meet a high design bar, not just "work":
   - **Design system consistency**: all new screens/components must use the app's existing design tokens (colors, typography, spacing, shapes) — no one-off hardcoded values. Extend the token set deliberately when new styles are needed.
   - **Material 3 compliance**: follow current Material 3 guidance for components, elevation, state layers, and dynamic color support.
   - **Accessibility (a11y)**: minimum 48dp touch targets, sufficient contrast ratios (WCAG AA), content descriptions for icons/images, support for dynamic font scaling, and TalkBack-navigable screens.
   - **Dark mode & theming**: every new screen must be verified in both light and dark themes; never hardcode colors.
   - **Motion & micro-interactions**: use purposeful animations (state transitions, list item feedback, screen transitions) with sensible durations/easing — follow Material motion principles, respect reduced-motion settings.
   - **Responsive & adaptive layouts**: screens must handle different screen sizes, orientations, and foldables gracefully (no clipped or stretched content).
   - **Empty, loading, and error states**: every screen must define polished states for empty data, loading, errors, and offline — never a blank screen or raw stack trace.
   - **UX polish details**: consistent iconography, sensible keyboard/input handling, haptic feedback where appropriate, and edge-case handling (very long link titles, RTL text, unusual characters).

10) Phase-completion report format — every report in `docs/` uses exactly these sections, nothing more:
   ```markdown
   # Phase N Report
   ## Implemented
   ## Files Touched
   ## Verification (build/tests/review verdict)
   ## Limitations & Follow-ups
   ```

11) Project definition of done — the plan is complete when every phase in `docs/plan.md` is marked done, all phases passed review with an Approved verdict, the build and full test suite are green, all docs (`docs/project.md`, `docs/agent.md`, `docs/research.md`, `docs/plan.md`) are current, and no open questions in `research.md` remain unresolved. When this is met, stop proposing new phases and deliver a final summary.

12) Portability — this prompt system (`.devin/prompt/`) is project-agnostic and is copied into new projects as-is. Everything in `docs/` is per-project knowledge: `idea.md`, `research.md`, `plan.md`, `project.md`, and `agent.md` are created fresh for each new project by following points 3–7. Never assume these files exist — create them when a phase calls for them.

13) Gitignore system — maintain a minimal, intentional `.gitignore`:
   - **Always ignore** `.devin/` and `docs/` — the prompt system and per-project knowledge are private and must never be committed or pushed to GitHub.
   - **Keep it lean**: only ignore files/folders that actually exist or will exist and genuinely shouldn't be tracked (build outputs, IDE files, local config, secrets/keys, OS files). No speculative or copy-pasted entries that have no effect on the project.
   - **Audit it**: when a new ignore-worthy artifact appears (e.g., a new build directory or local config file), add it. When an entry matches nothing in the project, remove it. The `.gitignore` should stay short enough to read at a glance.

14) README system — every project's `README.md` follows this signature structure (our personal template, based on the LinkNest README plus proven open-source README patterns — centered cover, badges, inline nav, scannable sections, no emojis):
   ```markdown
   # <Project Name> — <one-line what-it-is>

   <div align="center">

   <img src="<logo path>" alt="<Name>" width="128"/>

   <!-- badges: platform, language, main framework, design system, license — shields.io, with logos -->
   [![Badge 1](...)](...) [![Badge 2](...)](...) ...

   *<tagline — project essence in one sentence>*

   [Download/Quick Start](...) • [Features](#features) • [Building](#building)

   </div>

   ---

   ## Features
   (scannable list or table — never walls of text; screenshots/GIFs where they help)

   ## Screenshots
   (side-by-side device frames or a table, if the project has a UI)

   ## Tech Stack
   (table: layer → technology; for complex projects add a Mermaid architecture diagram)

   ## Project Structure
   (compact ASCII file tree of the important directories — instant architecture overview)

   ## Quick Start / Building
   (≤3 copy-paste steps to get running; advanced setup in <details> collapsibles)

   ## Usage
   (one concrete example of the core workflow — code block or short GIF)

   ## FAQ / Troubleshooting
   (real questions/issues only — add entries as they actually come up; delete this section if empty)

   ## Contributing
   (fork → branch → PR, one short paragraph; omit for personal projects)

   ## Roadmap
   (checklist of planned features — shows the project is alive)

   ## Changelog
   (link to CHANGELOG.md or GitHub Releases — one line, not duplicated content)

   ## License
   (license + author line)
   ```
   Rules: badges must be real (shields.io, matching the actual stack); the tagline answers "what is this, who is it for, why is it different" in the first 50 words; no emojis anywhere in the README; plain section headers; no AI-slop patterns — no marketing fluff ("blazingly fast", "supercharge your workflow"), no filler praise, no empty buzzwords — every sentence states a fact about the project; `---` dividers between major sections; no broken links; every image has descriptive alt text; keep it current — update the README whenever features change.

   Additional README discipline:
   - **GitHub About sync**: the repo's About panel (description, topics, website) must mirror the README tagline and keywords — this is what shows in search and link previews.
   - **Optional sections are earned, not default**: include FAQ, Contributing, Acknowledgments, or a comparison table only when they contain real content. An empty or padded section is worse than no section.
   - **First screen matters most**: logo, badges, tagline, and nav must fully render above the fold with no scrolling — this is the project's landing page.
   - **Screenshots are mandatory for UI projects**: capture real screens, not mockups; store them in the repo and reference with relative paths so they survive forks.
   - **Long READMEs (>6 sections) get a Table of Contents** after the cover; short ones rely on the inline nav only.
