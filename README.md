# Kangaroo

Real-time monitoring and management for distributed Minecraft networks. Live server/proxy health in an in-game GUI,
cross-proxy player tracking, remote command dispatch — backed by Redis for low-latency state and MongoDB for
persistence.

## Highlights

- **Heartbeat-based discovery** — every node writes a Redis key every 5s with a 15s TTL. No registry service, no
  inter-node chatter; TTL expiration *is* the failure detector.
- **Cross-proxy player tracking** — Velocity owns player state over pub/sub with a local `PlayerCache` for O(1) lookups.
  Pre-login rejects duplicate sessions across live proxies.
- **Remote RPC & redirects** — route players or dispatch commands to any server over Redis pub/sub (`/send`, `/sendall`,
  `/execute`).
- **Sentinel election** — one proxy holds a `SET NX EX` lease as the sole coordinator for network-wide tasks. Automatic
  failover on lease expiry.
- **Lifecycle events** — `CONNECTED` / `LOADED` / `DISCONNECTED` broadcasts, with synthesised `DIED` events for unclean
  exits, surfaced as in-game notifications.

## Modules

- **core** — shared domain models, heartbeat protocol, lifecycle bus, RPC envelopes, Redis/MongoDB/YAML clients.
- **bukkit** — Paper 1.21 plugin: monitoring GUI, heartbeat publishing, remote command execution.
- **velocity** — Velocity 3.4 plugin: player tracking, redirects, sentinel election, admin commands.
- **deployment** — Gradle plugin adding a `publishPlugin` task that uploads shadowed jars over SFTP.

## Tech

Java 21 · Gradle (Shadow) · Jedis · MongoDB · Paper 1.21 · Velocity 3.4

## Build

```bash
./gradlew shadowJar
```

## Deployment

Uploads shadow jars over SFTP. Copy `servers.example.json` → `servers.json` (gitignored) and fill in targets keyed by
group. Each target requires `name`, `host`, `user`, and one of `password` / `privateKey`. `port` defaults to `2022`,
`remotePath` to `/plugins`.

```bash
./gradlew :bukkit:publishPlugin     # → every 'bukkit' target
./gradlew :velocity:publishPlugin   # → every 'proxy' target
./gradlew publishPlugins            # → aggregate; every module that applies the plugin
```

The `publishPlugins` aggregator is registered automatically by the deployment plugin — any module that applies
`gg.desolve.kangaroo.deployment` is wired in with no changes to the root build.

### Reusing across repos

The deployment plugin is a standalone Gradle `includeBuild`, so another project can consume it directly. In the
consuming repo's `settings.gradle`:

```groovy
includeBuild '/absolute/or/relative/path/to/Kangaroo'
```

Then in any module's `build.gradle`:

```groovy
plugins {
    id 'com.gradleup.shadow'
    id 'gg.desolve.kangaroo.deployment'
}

deployment {
    groupKey = 'my-group'                                              // new key in servers.json
    configFile = rootProject.file('../Kangaroo/servers.json')          // optional; share Kangaroo's config
}
```

`configFile` is optional — omit it and the plugin falls back to `<rootDir>/servers.json` in the consuming repo. The
consumer gets its own `publishPlugins` aggregator scoped to its own modules.
