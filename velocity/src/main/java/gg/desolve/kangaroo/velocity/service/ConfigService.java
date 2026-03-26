package gg.desolve.kangaroo.velocity.service;

import gg.desolve.kangaroo.storage.ConfigStorage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigService {

    private final Path dataDirectory;
    private final Map<String, ConfigStorage> configs = new HashMap<>();

    public ConfigService(Path dataDirectory) {
        this.dataDirectory = dataDirectory;

        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ConfigStorage load(String fileName) {
        return configs.computeIfAbsent(fileName, name -> {
            try {
                InputStream defaults = getClass().getClassLoader().getResourceAsStream(name);
                return new ConfigStorage(dataDirectory.resolve(name).toFile(), defaults);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
