# Kangaroo

A real-time Minecraft network monitoring and management system built for production infrastructure. Kangaroo provides
live visibility into server and proxy health across a distributed network through an in-game inventory UI, cross-proxy
player tracking, and remote command dispatch — backed by Redis for low-latency state propagation and MongoDB for
persistent storage.

## Architecture

Kangaroo is a multi-module Gradle project split into three modules:

- **core** — Platform-agnostic shared library containing the server and player domain models, heartbeat protocol,
  lifecycle-event bus, RPC envelopes, and storage clients (Redis, MongoDB, YAML config). Both platform modules depend on
  this.
- **bukkit** — Paper plugin that provides the in-game monitoring UI, server-side heartbeat publishing, and remote
  command execution.
- **velocity** — Velocity proxy plugin handling proxy-side heartbeat publishing, player tracking, cross-proxy redirects,
  sentinel election, and admin commands.

Platform-specific modules depend on core and implement only what is unique to their runtime (plugin lifecycle, commands,
GUI, event subscriptions).

## How It Works

### Heartbeat Protocol

Every node in the network (servers and proxies) publishes a heartbeat to Redis every 5 seconds. Each heartbeat writes a
key with a 15-second TTL, meaning nodes that go silent are automatically pruned from the registry — no explicit
deregistration or health-check polling required. Redis TTL expiration acts as a distributed failure detector with zero
coordination overhead. Heartbeats carry live TPS, CPU load, player counts, and uptime.

### Server Registry

`ServerService` queries Redis for all active heartbeat keys and deserializes them into `Server` domain objects. This
gives any node a consistent, near-real-time view of the entire network topology without inter-node communication.

### Lifecycle Events

Alongside the key-based heartbeat, nodes publish `CONNECTED`, `LOADED`, and `DISCONNECTED` events to a Redis pub/sub
channel. `ServerMonitor` on each node consumes those events, reconciles them against the live registry, and synthesises
`DIED` events for nodes that vanished without a clean shutdown. Events surface as in-game notifications to operators.

### Player Tracking

The Velocity proxy is the source of truth for online players. `PlayerTrackingService` writes a `KangarooPlayer` entry
to Redis on login, updates the current server on transfer, and removes it on disconnect — with a 30-second heartbeat
and TTL-based cleanup matching the server registry. Each node also maintains a local `PlayerCache` kept in sync via a
`PlayerEvent` pub/sub channel, so lookups (`getByName`, `getByUuid`) stay O(1) without hitting Redis. A pre-login check
rejects duplicate sessions against live proxies to prevent double-logins.

### Cross-Proxy Redirect & RPC

`RedirectService` lets any node ask the correct proxy to move a player to a target server. Local redirects fire
immediately; remote ones publish a `RedirectRequest` on the owning proxy's redirect channel. `RpcService` uses the same
pattern to dispatch `RemoteCommand`s to specific servers — powering `/execute <server> <command>` from any proxy.

### Sentinel Election

`SentinelService` elects a single proxy as the "sentinel" via a Redis `SET NX EX` lease with a 15-second TTL, renewed
every 5 seconds. The sentinel is the authoritative coordinator for tasks that must run exactly once network-wide (e.g.
cleanup sweeps); any other proxy seamlessly takes over if the lease expires.

### Storage Layer

Redis handles all real-time state — heartbeats, server and player registries, lifecycle events, RPC, and TTL-based
liveness detection — with connections managed through Jedis pooling and a dedicated pub/sub subscription pool. MongoDB
serves as the persistent storage layer using the Reactive Streams async driver for non-blocking I/O. Per-node
configuration is managed through YAML files.

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
