# domain-access-control-squad

A software factory for [cybnity/domain-access-control](https://github.com/cybnity/domain-access-control),
CYBNITY's Access Control domain. It is OpenRig's built-in `factory` team (OpenRig CLI 0.x launch specs),
vendored here with its agent specs under `agents/` so the folder is self-contained.

| Seat | Runtime | Role |
| --- | --- | --- |
| `orch.lead` | Codex | Runs the team, scopes and routes work |
| `orch.advisor` | Claude Code | Product intent partner (PM) |
| `dev.build` | Claude Code | Implements changes |
| `dev.qa` | Codex | Verifies outcomes by effect |
| `dev.design` | Claude Code | API, configuration and operator experience |
| `review.reviewer1` | Claude Code | Independent reviewer |
| `review.reviewer2` | Codex | Independent reviewer |

Changes from the built-in factory: the rig name, every seat's `cwd`, pinned models, `permission_policy:
builtin:auto`, instruction blocks in `CLAUDE.local.md`, and the "This team" section of `CULTURE.md`.

Launch: `rig specs add <this folder>/rig.yaml`, then `rig up domain-access-control-squad`.

Keep OpenRig's per-seat files out of commits by adding `CLAUDE.local.md`, `AGENTS.md`, `/.openrig/` and
`.claude/settings.local.json` to `.git/info/exclude` in your clone.
