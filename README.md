# Kangaroo

A real-time Minecraft network monitoring and management system built for production infrastructure. Kangaroo provides
live visibility into server and proxy health across a distributed network through an in-game inventory UI, backed by
Redis for low-latency state propagation and MongoDB for persistent storage.

## Architecture

Kangaroo is a multi-module Gradle project split into three modules:

- **core** — Platform-agnostic shared library containing the server domain model, heartbeat protocol, and storage
  clients (Redis, MongoDB, YAML config). Both platform modules depend on this.
- **bukkit** — Paper plugin that provides the in-game monitoring UI, and server-side heartbeat publishing.
- **velocity** — Velocity proxy plugin handling proxy-side heartbeat publishing and player count tracking.

Platform-specific modules depend on core and implement only what is unique to their runtime (plugin lifecycle, commands,
GUI).

## How It Works

### Heartbeat Protocol

Every node in the network (servers and proxies) publishes a heartbeat to Redis every 5 seconds. Each heartbeat writes a
key with a 15-second TTL, meaning nodes that go silent are automatically pruned from the registry — no explicit
deregistration or health-check polling required. Redis TTL expiration acts as a distributed failure detector with zero
coordination overhead.

### Server Registry

`ServerService` queries Redis for all active heartbeat keys and deserializes them into `Server` domain objects. This
gives any node a consistent, near-real-time view of the entire network topology without inter-node communication.

### Storage Layer

Redis handles all real-time state - heartbeats, server registry, and TTL-based liveness detection - with connections
managed through Jedis pooling. MongoDB serves as the persistent storage layer using the Reactive Streams async driver
for non-blocking I/O. Per-node configuration is managed through YAML files.

## Tech Stack

Java 21, Gradle with Shadow for fat JAR packaging, Redis via Jedis for real-time state, MongoDB Reactive Streams driver
for persistence, and Lucko's Helper for scheduling. The Bukkit module targets Paper 1.21 and uses ACF for commands. The
Velocity module targets Velocity 3.4.0.

## Building

```bash
./gradlew shadowJar
```

Artifacts are output per module. Dependencies are relocated under the project namespace to avoid classpath conflicts
with other plugins.
