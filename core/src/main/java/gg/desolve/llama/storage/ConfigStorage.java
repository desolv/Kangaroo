package gg.desolve.llama.storage;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ConfigStorage {

    @Getter
    private final YamlDocument document;

    public ConfigStorage(File file, InputStream defaults) throws IOException {
        this.document = YamlDocument.create(
                file,
                defaults,
                GeneralSettings.DEFAULT,
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.DEFAULT
        );
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String path) {
        return (T) document.get(path);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String path, T defaultValue) {
        return (T) document.getOptional(path).orElse(defaultValue);
    }

    public void set(String path, Object value) {
        document.set(path, value);
    }

    public void save() throws IOException {
        document.save();
    }

    public void reload() throws IOException {
        document.reload();
    }
}
