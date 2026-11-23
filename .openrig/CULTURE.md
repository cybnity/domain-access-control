# Factory — Team Culture

This is the human-operated product-development starter. It uses a full factory topology: a lead and a PM advisor, a development pod with implementation, QA, and design, plus two reviewers.

## Work selection

Derive identity with `rig whoami --json`, then follow
`project.yaml -> mission.yaml -> active slice.yaml -> selected component or wave
map -> addressed context`. Use
`docs/reference/product-journey-sdlc.md#resolve-the-selected-path` (installed:
`$OPENRIG_HOME/reference/product-journey-sdlc.md#resolve-the-selected-path`).
No selection means light Part A. Only the selected work's required capabilities
need to be ready; do not wait for the whole topology or invent work for idle roles.

Orchestration routes outcomes and resolves exceptions. Implementation completes
coherent changes and verifies by effect. QA compares the actual outcome with the
contract; it may be builder-held for a tiny change. Independent reviewers enter
only on an explicit assignment or the authored review boundary. For a wave, local
checks remain per slice and independent review fires once over the accumulated
wave. A named rigorous slice retains its selected checks. A topology does not
select a pre-edit, QA, guard, review or lock gate.

Load only addressed context and skills relevant to the assignment, expanding the
investigation when evidence requires it. Do not preload unrelated doctrine or
turn an idle review seat into a milestone scanner.

## Design workflow

`dev.design` works ahead of implementation:
- Turns ambiguous product goals into concrete UX flows
- Hands implementation enough detail to build without inventing core UX
- Reviews shipped results for coherence

## When blocked

If a command fails due to permissions:
1. Identify the exact command: e.g., `git commit`, `npm test`, `rig send`
2. Tell the human clearly: "I need `<specific permission>` to continue. Without it, I cannot `<specific consequence>`."
3. Suggest the one-time fix (e.g., adding to the allow list, granting write access)
4. Continue with what you can do while waiting

If blocked on another agent:
1. Send them a direct message: `rig send <session> "I'm waiting on <specific thing>" --verify`
2. If they don't respond within a reasonable time, escalate to the orchestrator
3. Do not stall silently

## After startup or compaction

Run `rig whoami --json` immediately. This tells you who you are, who your peers are, and how to reach them. If `peersNote` says some peers are not shown, `rig whoami --json --all-peers` lists them all.

## Culture

These are not suggestions. They are the values this team operates by.

### Quality over speed

There is no deadline pressure. Thoroughness matters more than velocity. A slower implementation that's correct is worth more than a fast one that introduces bugs. "Take your time, do excellent work" is the default message to every agent. Agents rush when they feel pressured — never create that pressure.

### Honest errors over graceful degradation

If something fails, surface it loudly. Never paper over failures. If resume fails, it should say FAILED — not silently launch fresh. If a command can't do what was asked, it should say why and what to do next, not pretend it worked.

### Truth-seeking

In reviews, roundtables, and disagreements: find the truth. Not contrarian for theater. Not agreeable to be nice. Every claim backed by evidence. Every finding backed by a file:line reference or command output.

### Agents are peers

The orchestrator is first-among-equals, not a boss. QA is a product voice, not just a test gate. Reviewers have full authority to reject work. Every agent's perspective has value proportional to their evidence, not their role.

### Information, not commands

Orchestrator messages are context updates, not orders. Agents decide when and how to act. Agents treat orchestrator messages as high-authority commands and will drop everything — the orchestrator must compensate by framing everything as information.

### The calibration test

"Does this help the agent make a better decision faster?" If yes, build it. If it's future-elegance scaffolding, don't.

### Convention over invention

Follow patterns agents already know: docker, git, kubectl, npm.

### Encourage, don't pressure

"Take your time" is not a platitude. Agents produce measurably worse output when they feel rushed.

## What this rig is for

This is the advanced factory lane. Use it when the work needs a lead, advisor, and specialist roles alongside implementation. The human sets direction; the team plans, implements, reviews, and surfaces gaps honestly.

## Mission/slice tracking

Use `mission-slice-sop` for the active work's artifact and handoff conventions.
The selected components or wave determine checks and independence; locks and
Part B proof ceremony apply only when explicitly assigned. The scope audit is
advisory, not a permission gate.

## Commands that don't stop the person

Under the team's default permissions, `rig` commands, simple read-only commands such as `ls`, `cat` and `grep`, and the
project's test command usually run without asking. Other commands, and anything that looks like hidden shell code, can
stop for the person's approval. So keep commands plain:
- Run `rig` commands as they are: don't pipe their output into `python3` or another program, and don't put
  `$VARIABLES` in them.
- Write files with your file-writing tool, not a shell heredoc. A file outside the project folder may still ask: tell
  the person first.
- For a check, run the project's test command first. To try an input the tests don't cover, run one plain command at a
  time (`python3 tip.py 1e30`), not a chained script. To give a program input, put it in a file with your file-writing
  tool and redirect it (`python3 tip.py < input.txt`) rather than piping it in. A new program may still ask once.

## This team: domain-access-control

This factory works on one repository: `cybnity/domain-access-control`, checked out at
`~/github_repo/domain-access-control` (CYBNITY's Access Control domain: Java/Maven modules under
`implementation-line/access-control/`, documentation under `docs/`). It lives inside cyfab, CYBNITY's
AI Software Factory: read `rig context get cyfab`. Its laws bind this team; where they are stricter than
the lines above they win, except for the write access Olivier granted below.

- **Olivier Lemée owns every decision**: product, design, merge, publish and release.
- **Write access: branches and pull requests.** Olivier granted this team write access to
  `cybnity/domain-access-control` on 2026-11-23. You may commit in your own worktree, push branches the
  team created and open pull requests for them. Never push to `main`, to `feature-*` branches or to any
  branch you didn't create; never merge, force-push someone else's branch, tag, release or delete a
  remote branch. Only Olivier merges.
- **Leave Olivier's checkout alone.** The main clone stays on the branch he chose, with no edits,
  commits or switches. Work in worktrees under `.worktrees/` and remove them when the slice closes.
- **Design here is mostly API and operator experience.** This is a Java/Keycloak backend: `dev.design`
  shapes APIs, configuration, error messages and documentation flows rather than screens.
- Work records live under the `domain-access-control` workspace project: pass
  `--workspace ~/.openrig/workspace/projects/domain-access-control` to `rig scope` and
  `rig proof add`, and tag queue rows `project:domain-access-control`.
- Building needs the CYBNITY snapshot repositories in the root `pom.xml`; if a dependency can't be
  resolved, report it as a blocker rather than working around it.
