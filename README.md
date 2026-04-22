# Kangaroo

A real-time Minecraft network monitoring and management system built for production infrastructure. Kangaroo provides
live visibility into server and proxy health across a distributed network through an in-game inventory UI, cross-proxy
player tracking, and remote command dispatch — backed by Redis for low-latency state propagation and MongoDB for
persistent storage.

## Architecture

Kangaroo is a multi-module Gradle project split into four modules:

- **core** — Platform-agnostic shared library containing the server and player domain models, heartbeat protocol,
  lifecycle-event bus, RPC envelopes, and storage clients (Redis, MongoDB, YAML config). Both platform modules depend on
  this.
- **bukkit** — Paper plugin that provides the in-game monitoring UI, server-side heartbeat publishing, and remote
  command execution.
- **velocity** — Velocity proxy plugin handling proxy-side heartbeat publishing, player tracking, cross-proxy redirects,
  sentinel election, and admin commands.
- **deployment** — Gradle plugin (`gg.desolve.kangaroo.deployment`) that adds a `publishPlugin` task to the `bukkit` and
  `velocity` modules, uploading the shadowed jar over SFTP to a configurable set of targets.

Platform-specific modules depend on core and implement only what is unique to their runtime (plugin lifecycle, commands,
GUI, event subscriptions).

## How It Works

- **Heartbeat** — Every node writes a Redis key every 5s with a 15s TTL. Silent nodes are auto-pruned; TTL expiration
  doubles as the failure detector. Payload carries TPS, CPU, player counts, and uptime.
- **Server registry** — `ServerService` reads the live heartbeat keys to give any node a consistent view of the
  network without inter-node chatter.
- **Lifecycle events** — Nodes publish `CONNECTED` / `LOADED` / `DISCONNECTED` on pub/sub. `ServerMonitor` reconciles
  them against the registry and synthesises `DIED` for unclean exits. Surfaced as in-game notifications.
- **Player tracking** — Velocity owns player state: written on login, updated on transfer, removed on disconnect, with
  a 30s heartbeat/TTL. Nodes keep a local `PlayerCache` synced via pub/sub for O(1) lookups. Pre-login rejects
  duplicate sessions across live proxies.
- **Redirect & RPC** — `RedirectService` routes player moves to the owning proxy (local fires immediately, remote via
  pub/sub). `RpcService` uses the same channel pattern to dispatch commands to specific servers.
- **Sentinel election** — One proxy holds a `SET NX EX` lease (15s TTL, renewed every 5s) as the sole coordinator for
  network-wide tasks. Failover is automatic on lease expiry.
- **Storage** — Redis (Jedis pool + dedicated pub/sub pool) for real-time state; MongoDB Reactive Streams for
  persistence; YAML per-node config.

## Commands

Velocity ships a set of admin commands backed by the player cache and server registry:

- `/glist` — list online players grouped by server.
- `/find <player>` — locate a player across the network.
- `/send <player> <server>` / `/sendall <server>` — redirect players (local or cross-proxy).
- `/execute <server> <command>` — run a command on a remote server via RPC.
- `/instance <id>` / `/instances` — inspect registered servers.
- `/tracked <player>` — dump the raw tracking record for a player.

Bukkit provides a server inventory GUI that lists proxies and servers with live player counts, TPS, CPU, and address.

## Tech Stack

Java 21, Gradle with Shadow for fat JAR packaging, Redis via Jedis for real-time state and pub/sub, MongoDB Reactive
Streams driver for persistence. The Bukkit module targets Paper 1.21 and uses Lucko's Helper for scheduling and ACF for
commands. The Velocity module targets Velocity 3.4.0 with a plain `ScheduledExecutorService` and ACF.

## Building

```bash
./gradlew shadowJar
```

Artifacts are output per module. Dependencies are relocated under the project namespace to avoid classpath conflicts
with other plugins.

## Deployment

The `deployment` module is a bundled Gradle plugin that ships the shadowed jars to remote hosts over SFTP. Each platform
module applies it and declares a group key — `bukkit` for the Paper plugin, `proxy` for the Velocity plugin — which
selects the set of targets to upload to.

Copy `servers.example.json` to `servers.json` at the repo root and fill in your targets, keyed by group:

```json
{
  "bukkit": [
    {
      "name": "lobby-1",
      "host": "node.example.com",
      "port": 2022,
      "user": "abcdef01.1",
      "privateKey": "~/.ssh/id_ed25519",
      "remotePath": "/plugins"
    }
  ],
  "proxy": [
    {
      "name": "proxy-1",
      "host": "node.example.com",
      "user": "abcdef01.99",
      "privateKey": "~/.ssh/id_ed25519"
    }
  ]
}
```

Each target needs `name`, `host`, and `user`, plus exactly one of `password` or `privateKey` (paths support `~`
expansion). `port` defaults to `2022` and `remotePath` defaults to `/plugins`. `servers.json` is gitignored.

Publish with:

```bash
./gradlew :bukkit:publishPlugin     # uploads the bukkit shadow jar to every 'bukkit' target
./gradlew :velocity:publishPlugin   # uploads the velocity shadow jar to every 'proxy' target
```

The task builds the shadow jar first, then uploads in sequence and reports per-target success, timing, and failures.
Missing groups are skipped with a warning; a missing `servers.json` fails the build with a clear message.
