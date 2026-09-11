Research and implement the task in phase.md with below commandments.

1) Use agents and sub-agents from `.devin` where they add value — not continuously. Required: the code-review sub-agent on the final diff before declaring the task done. Optional: domain specialists (security, performance, a11y) when the task touches their domain.

2) Leverage all relevant skills and workflows defined in `.devin` to achieve the highest-quality outcome for the tasks listed below.

3) First, understand our workflow and prompt system as defined in `.devin`.

4) Study the project structure, codebase, and design system before making any changes.

5) Strictly follow all rules and instructions — those given in the prompt, derived from research, and issued by agents. Additionally, comply with every rule in `rules.md` (same folder), which defines scoping, verification, design, and escalation rules for all quick tasks.

6) Read `project.md` and `agent.md` (in `docs/`) to understand past implementations; update both at the end of the implementation. These are per-project files — if absent, create them fresh. Consult `research.md` before adding any dependency or unfamiliar pattern, and append new tech decisions/findings to it.

7) Place all documentation (`.md`) files inside the `docs` folder.

8) Before starting, read `map.md` (same folder) — it is the system map showing how every document, agent, rule, and workflow in this project connects, including the required reading order, task lifecycle, agent delegation, and write-back order. Follow it exactly.