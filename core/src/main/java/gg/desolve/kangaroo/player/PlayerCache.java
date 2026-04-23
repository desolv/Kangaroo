package gg.desolve.kangaroo.player;

import gg.desolve.kangaroo.scheduler.KangarooScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PlayerCache {

    private final PlayerService playerService;
    private final KangarooScheduler scheduler;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<UUID, KangarooPlayer> players = new HashMap<>();
    private KangarooScheduler.ScheduledTask task;

    public PlayerCache(PlayerService playerService, KangarooScheduler scheduler) {
        this.playerService = playerService;
        this.scheduler = scheduler;
    }

    public void start() {
        refresh();
        this.task = scheduler.scheduleRepeating(this::refresh, 30, 30);
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    private void refresh() {
        try {
            List<KangarooPlayer> all = playerService.getAll();
            Map<UUID, KangarooPlayer> updated = new HashMap<>();
            for (KangarooPlayer player : all) {
                updated.put(player.getUuid(), player);
            }

            lock.writeLock().lock();
            try {
                this.players = updated;
            } finally {
                lock.writeLock().unlock();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<KangarooPlayer> getAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(players.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    public KangarooPlayer getByUuid(UUID uuid) {
        lock.readLock().lock();
        try {
            return players.get(uuid);
        } finally {
            lock.readLock().unlock();
        }
    }

    public KangarooPlayer getByName(String name) {
        lock.readLock().lock();
        try {
            return players.values().stream()
                    .filter(player -> player.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getCount() {
        lock.readLock().lock();
        try {
            return players.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void handleLogin(KangarooPlayer player) {
        lock.writeLock().lock();
        try {
            players.put(player.getUuid(), player);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void handleLogout(UUID uuid) {
        lock.writeLock().lock();
        try {
            players.remove(uuid);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void handleSwitch(UUID uuid, String newServer, String lastServer, long switchTime) {
        lock.writeLock().lock();
        try {
            KangarooPlayer player = players.get(uuid);
            if (player != null) {
                player.setServer(newServer);
                player.setLastServer(lastServer);
                player.setLastSwitchTime(switchTime);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
