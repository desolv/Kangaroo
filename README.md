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

The bundled `deployment` Gradle plugin uploads shadow jars over SFTP. Copy `servers.example.json` to `servers.json` and
fill in your targets, keyed by group (`bukkit` or `proxy`):

```json
{
  "bukkit": [
    {
      "name": "lobby-1",
      "host": "node.example.com",
      "user": "abcdef01.1",
      "privateKey": "~/.ssh/id_ed25519"
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

Each target needs `name`, `host`, `user`, plus one of `password` or `privateKey`. `port` defaults to `2022`,`remotePath`
to `/plugins`. `servers.json` is gitignored.

```bash
./gradlew :bukkit:publishPlugin     # → every 'bukkit' target
./gradlew :velocity:publishPlugin   # → every 'proxy' target
```
