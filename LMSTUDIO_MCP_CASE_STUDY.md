# Configuring MCP (Model Context Protocol) Tool Access for Local LLM Agents in LM Studio: A Cross-Platform Debugging Case Study

**Author:** Jerry Abramson
**Date:** 2026-07-14
**Context:** Boston University research on local LLM tooling for agentic workflows across heterogeneous hardware (Apple Silicon macOS, Nvidia GB10/Spark ARM devkit, AMD Ryzen AI Strix Halo devkit).

## 1. Overview

This document records the process of enabling web search tool access for locally-hosted LLMs running in [LM Studio](https://lmstudio.ai/), via the Model Context Protocol (MCP), across three different machines. What began as a routine configuration task surfaced three independent, stacked failure modes — one specific to macOS GUI application PATH resolution, one an OS-level network privacy permission, and one a latent bug in a personal shell configuration file that silently broke any tool relying on shell-environment introspection. The debugging methodology and root causes are documented here because they generalize beyond this specific setup and are relevant to reproducibility of local-LLM-agent research environments.

## 2. Motivation

For agentic workflows that require web search (e.g., retrieval-augmented tasks, fact-checking, current-events queries), a locally hosted LLM needs a tool-use mechanism. LM Studio supports MCP, the same tool-integration protocol used by Claude Desktop and other agent frontends. [SearXNG](https://docs.searxng.org/), a self-hosted metasearch engine, was already running and reachable at `http://gb10.jabramson.tech:8080` on the local network. The `mcp-searxng` npm package (an MCP server that wraps SearXNG's search API) was chosen as the tool-access layer, run via `npx` rather than a container, to minimize the runtime dependency surface for what is otherwise a lightweight process.

The goal: get a `searxng` MCP tool working identically in LM Studio on three research machines:

| Machine | Role | OS / Arch | Shell |
|---|---|---|---|
| macOS laptop | primary dev machine | macOS, arm64 | zsh (login), bash (`nvm`) |
| `gb10` (`gb10.jabramson.tech`) | Nvidia GB10 "Spark" devkit | Linux, aarch64 | bash |
| `amd-halo` | AMD Ryzen AI "Strix Halo" devkit | Linux, x86_64 | bash |

## 3. Initial Configuration and First Failure

LM Studio reads MCP server definitions from `mcp.json` (`~/.lmstudio/mcp.json` on macOS, `~/.lmstudio/mcp.json` on the Linux machines), using the same `mcpServers` schema as Claude Desktop:

```json
{
  "mcpServers": {
    "searxng": {
      "command": "npx",
      "args": ["-y", "mcp-searxng"],
      "transport": "stdio",
      "env": {
        "SEARXNG_URL": "http://gb10.jabramson.tech:8080"
      }
    }
  }
}
```

On macOS, enabling this server in LM Studio's UI produced an indefinite **"Initializing plugin"** state that never resolved.

## 4. Root Cause #1 (macOS): GUI Applications Do Not Inherit Shell PATH

**Finding:** The user's `node`/`npm`/`npx` toolchain on macOS was installed exclusively via `nvm`, at `/Users/jerry/.nvm/versions/node/v23.6.0/bin/`. This directory is added to `PATH` only by interactive shell startup files (`.zshrc`/`.bashrc`). macOS GUI applications launched from Finder/Dock/Spotlight — including Electron apps like LM Studio — do **not** source shell rc files, and instead receive a minimal default `PATH` (`/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin`, plus `/etc/paths.d/*` entries).

Verification was done by simulating the GUI PATH directly:

```sh
env -i PATH="/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin" which node
# no output — node not found
```

`/usr/local/bin/npx` on this system was itself a symlink into a Homebrew-managed npm install's `npx-cli.js`, a script with a `#!/usr/bin/env node` shebang — so even the symlink target required `node` to be resolvable via `PATH` at the moment the shebang was interpreted, which it was not.

A second, subtler layer: even invoking `node <path-to-npx-cli.js>` directly bypasses the *outer* shebang, but `npx` itself then re-spawns the target package's own bin script (`mcp-searxng`), which has its **own** `#!/usr/bin/env node` shebang — so the fix had to ensure `node` was resolvable via `PATH` for the *entire* process tree, not just the initial command.

**Fix:** Set the MCP server's `env.PATH` explicitly to include the `nvm` Node's `bin` directory, and point `command` at that Node's `npx` via absolute path:

```json
{
  "mcpServers": {
    "searxng": {
      "command": "/Users/jerry/.nvm/versions/node/v23.6.0/bin/npx",
      "args": ["-y", "mcp-searxng"],
      "transport": "stdio",
      "env": {
        "SEARXNG_URL": "http://gb10.jabramson.tech:8080",
        "PATH": "/Users/jerry/.nvm/versions/node/v23.6.0/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"
      }
    }
  }
}
```

This was verified in isolation with `env -i PATH="<minimal-path>" npx -y mcp-searxng`, confirming the server connects and emits its STDIO startup banner in under a second once the PATH included the correct Node directory.

## 5. Root Cause #2 (macOS): Local Network Privacy Permission

Even after the PATH fix, the plugin still failed, though the symptom changed from an indefinite hang to a hard **"plugin initialization timed out"** error after ~60 seconds.

**Finding:** `gb10.jabramson.tech` resolves to a private LAN address (`192.168.1.209`). Since macOS Big Sur, connections from an application to devices on the local network are gated behind a distinct **Local Network** privacy permission (System Settings → Privacy & Security → Local Network), separate from general internet access. Checking this setting confirmed LM Studio was listed but **toggled off** — meaning any local-network connection attempt made by LM Studio or its child processes would be blocked at the OS level.

**Fix:** Enable the Local Network toggle for LM Studio, then fully quit and relaunch the application (the permission is evaluated at process start).

## 6. Root Cause #3 (macOS): A Personal Shell Configuration Bug Breaking Non-Interactive Shell Invocation

After both fixes above, the timeout **persisted unchanged** — same exact ~60-second window, regardless of server (it affected an unrelated, previously-configured `filesystem` MCP server identically). This ruled out both prior explanations, since the process was never even observed to spawn (`ps` polling across multiple live retry attempts, synchronized with LM Studio's log timestamps, never showed an `npx`/`mcp-searxng` process during the entire failure window).

**Finding:** Static analysis of LM Studio's bundled application code (`mcpbridgeworker.js`) revealed that before spawning a configured MCP server, LM Studio attempts to **capture the user's real shell environment** (a common technique in Electron/GUI applications, used specifically to work around the PATH problem in §4) — with a literal fallback string `"Failed to get shell environment, using process.env"` in the code, indicating it invokes the user's login shell non-interactively (functionally equivalent to `$SHELL -ilc '...'`) to capture environment variables such as `PATH`.

The user's `~/.zshrc` contained:

```sh
if [ "$SHELL" = "/bin/zsh" -o "$TERM_PROGRAM" = "Termius" ]; then
    if [ -x /opt/local/bin/bash ]; then
        exec /opt/local/bin/bash --login
    fi
else
    exec /bin/bash --login
fi
```

This unconditionally `exec`s into a bare, interactive login `bash` whenever zsh starts with `$SHELL=/bin/zsh` — a personal convenience redirect (originally intended to normalize the interactive terminal experience). Critically, **`exec` replaces the process image outright**, discarding any command passed via zsh's `-c` flag. Any tool — not just LM Studio — that shells out via `zsh -c '...'` or `zsh -ilc '...'` to introspect the environment would have its intended command silently dropped, landing instead in a bare interactive `bash --login` session that sits reading from `stdin` indefinitely. When such a caller does not close `stdin` immediately, this manifests as a hang bounded only by whatever timeout the calling application enforces — in LM Studio's case, ~60 seconds.

This was confirmed empirically:

```sh
$ time /bin/zsh -ilc 'echo SHELL_ENV_OK'
[Using Native macOS bash via /bin/zsh]
# "SHELL_ENV_OK" never printed — command was silently dropped
```

**Fix:** zsh sets `$ZSH_EXECUTION_STRING` internally whenever it is invoked with an explicit `-c` command. Guarding the redirect on the *absence* of this variable preserves the original interactive-terminal behavior while allowing non-interactive, command-bearing invocations to proceed normally:

```sh
if [ "$STARTING_X" != "true" ] && [ -z "$ZSH_EXECUTION_STRING" ]; then
    # ... original redirect logic, now only reached for genuinely
    # interactive shells with no explicit command ...
fi
```

Verified post-fix:

```sh
$ time /bin/zsh -ilc 'echo SHELL_ENV_OK; env | wc -l' </dev/null
SHELL_ENV_OK
111
real    0m0.698s
```

After this fix, LM Studio's MCP plugin connected successfully within seconds.

**Broader significance:** this bug was not LM-Studio-specific — it would silently break *any* tool relying on `zsh -c`/`zsh -ilc` to introspect the user's environment (a pattern also used by some other Electron apps, e.g. VS Code's `fix-path`-style shell resolution). Its root cause lived entirely in a personal dotfile, invisible to the affected application's own diagnostics, and produced a symptom (a fixed-duration timeout with zero subprocess evidence) that pointed away from the actual defect until process-spawn observation (via synchronized `ps` polling and LM Studio's internal log timestamps) ruled out the two more "obvious" causes.

## 7. Cross-Platform Replication: `gb10` and `amd-halo`

Having a working macOS reference configuration, the same `searxng` MCP tool was deployed to the two Linux devkits. Each had its own, different obstacle — underscoring that "the same config" does not mean "the same fix" across heterogeneous environments.

### 7.1 `gb10` (Nvidia GB10/Spark, aarch64)

- Was previously running the MCP server via Docker (`docker run --rm isokoliuk/mcp-searxng`), chosen specifically because the system's package-managed Node.js was v18.19.1, while `mcp-searxng` requires Node ≥20 (confirmed via a direct failure: `ReferenceError: File is not defined` inside `undici`, a dependency requiring Node's native `File` global introduced in v20).
- Rather than modify the system Node install (used elsewhere) or introduce a version manager (which, per §4 and §6, adds its own PATH-resolution risk surface), a self-contained Node v24.18.0 LTS (arm64) tarball was downloaded directly from nodejs.org and extracted to `/home/jerry/.local/node-v24.18.0/`, with no shell integration.
- `mcp.json` was updated to invoke this Node's `npx` by absolute path, eliminating the Docker dependency.
- Shell startup files were checked for the class of bug in §6; none was present (default shell is plain bash with no `exec` redirects active).

### 7.2 `amd-halo` (AMD Ryzen AI "Strix Halo", x86_64)

- Had no existing MCP configuration (`mcpServers: {}`).
- System Node was v20.19.2 (version-adequate), but the Debian `nodejs` package installed did **not** include `npm`/`npx` — only non-functional `corepack` shims were present at `/usr/share/nodejs/corepack/shims/`.
- The same self-contained Node v24.18.0 LTS (x64) approach used for `gb10` was applied here, installed to `/home/jerry/.local/node-v24.18.0/`, sidestepping the incomplete system package.
- No shell-startup redirect bugs were found.

### 7.3 Final Configuration Pattern (all three machines)

```json
{
  "mcpServers": {
    "searxng": {
      "command": "<absolute-path-to-node-install>/bin/npx",
      "args": ["-y", "mcp-searxng"],
      "transport": "stdio",
      "env": {
        "SEARXNG_URL": "http://gb10.jabramson.tech:8080",
        "PATH": "<absolute-path-to-node-install>/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"
      }
    }
  }
}
```

## 8. Methodology Notes

The debugging approach that ultimately isolated the (non-obvious) third root cause relied on:

1. **Falsifiable, timestamped evidence over speculation.** Each hypothesis (PATH, Local Network permission, sandboxing) was tested by direct reproduction (`env -i PATH=... npx ...`) rather than assumed correct from documentation or plausibility alone.
2. **Correlating application logs with live process observation.** LM Studio's own server logs recorded exact `Client created` / `Client disconnected` timestamps; synchronizing a `ps` polling loop with a live retry in LM Studio's UI established, empirically, that the target subprocess never spawned — which eliminated entire classes of otherwise-plausible explanations (e.g., the SearXNG connection itself failing) and redirected investigation to LM Studio's *pre-spawn* logic.
3. **Reading the actual (minified) application code** when documentation and log output were insufficient — locating the literal fallback string `"Failed to get shell environment, using process.env"` was the pivot point that identified the shell-environment-capture step as the actual point of failure.
4. **Not assuming a fix is complete because a plausible cause was found and addressed.** Two real, independently-fixable issues (§4, §5) were identified and corrected first, and both were necessary — but neither was sufficient, and the timeout symptom was unchanged until the third cause (§6) was found. Each fix was verified in isolation before moving on.

## 9. Relevance to the Research

This case study is included in the project's documentation because it captures a reproducible environment-configuration procedure for enabling MCP-based tool access in locally-hosted LLM agents across the three heterogeneous hardware targets used in this research (Apple Silicon, Nvidia GB10, AMD Strix Halo). Future environment setup — or replication by collaborators — on any of these platform types can reference §4–§7 directly rather than re-deriving the same fixes.

# 10. Costs of using Calude

```
   Total cost:            $5.41
   Total duration (API):  18m 11s
   Total duration (wall): 1h 18m 41s
   Total code changes:    261 lines added, 7 lines removed
   Usage by model:                                                                                                                                                                                       claude-opus-4-8:  3.7k input, 4.9k output, 133.4k cache read, 23.6k cache write ($0.4436)
    claude-haiku-4-5: 536 input, 15 output, 0 cache read, 0 cache write ($0.0006)
    claude-sonnet-5:  10.9k input, 70.0k output, 10.4m cache read, 127.7k cache write ($4.96)

 Current session
   ███████                                          14% used
   Resets 1:40am (America/New_York)

   Current week (all models)
   █                                                 2% used
 Resets Jul 16 at 2pm (America/New_York)

 Current week (Fable)                                0% used
```
