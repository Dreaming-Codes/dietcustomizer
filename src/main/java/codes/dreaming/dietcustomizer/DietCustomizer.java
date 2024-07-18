package codes.dreaming.dietcustomizer;

import com.typesafe.config.ConfigFactory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import com.typesafe.config.Config;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DietCustomizer implements ModInitializer {
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("dietcustomizer.conf").toFile();

    public static Config CONFIG;

    {
        loadConfig();
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> loadConfig());
    }

    private void loadConfig() {
        writeDefaultConfig();
        ConfigFactory.invalidateCaches();
        CONFIG = ConfigFactory.parseFile(CONFIG_FILE);
    }

    private void writeDefaultConfig() {
        if (CONFIG_FILE.exists()) {
            return;
        }

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("default-config.conf")) {
            if (inputStream != null) {
                Files.copy(inputStream, CONFIG_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                throw new IOException("Default configuration file not found in resources.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
