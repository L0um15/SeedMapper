package dev.xpple.seedmapper.util.config;

import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static dev.xpple.seedmapper.SeedMapper.MOD_ID;
import static dev.xpple.seedmapper.SeedMapper.MOD_PATH;

public class Config {

    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private static final Path configPath = Paths.get(MOD_PATH + File.separator + "config.json");

    private static final JsonParser parser = new JsonParser();
    private static JsonObject root = null;

    private static final Set<Block> ignoredBlocks = new HashSet<>();

    public static void init() {
        try {
            if (configPath.toFile().exists() && configPath.toFile().isFile()) {
                root = parser.parse(Files.newBufferedReader(configPath)).getAsJsonObject();
                for (JsonElement element : root.getAsJsonArray("ignoredBlocks")) {
                    ignoredBlocks.add(Registry.BLOCK.get(new Identifier(element.getAsString())));
                }
                toggle("automate", false);
            } else {
                String standardJson = "" +
                        "{\n" +
                        "  \"automate\": {\n" +
                        "    \"enabled\": false\n" +
                        "  },\n" +
                        "  \"seed\": null,\n" +
                        "  \"ignoredBlocks\": [\n" +
                        "\n" +
                        "  ]\n" +
                        "}\n";
                Files.write(configPath, standardJson.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            LOGGER.error("Could not load config file. Your client may crash due to this.");
        }
    }

    public static void save() {
        try {
            JsonArray jsonIgnoredBlocks = new JsonArray();
            ignoredBlocks.forEach(block -> jsonIgnoredBlocks.add(Registry.BLOCK.getId(block).getPath()));
            root.add("ignoredBlocks", jsonIgnoredBlocks);
            final BufferedWriter writer = Files.newBufferedWriter(configPath);
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(root));
            writer.close();
        } catch (IOException e) {
            LOGGER.error("Could not save config file. Your client may crash due to this.");
        }
    }

    public static JsonElement get(String key) {
        JsonElement element = root.get(key);
        if (element == null) {
            throw new JsonIOException("No mapping found for " + key);
        }
        return element;
    }

    public static boolean isEnabled(String key) {
        JsonObject object = get(key).getAsJsonObject();
        if (object.has("enabled")) {
            return object.get("enabled").getAsBoolean();
        }
        throw new JsonParseException("No member \"enabled\" found for " + key);
    }

    public static void toggle(String key, boolean bool) {
        JsonObject object = get(key).getAsJsonObject();
        if (object.has("enabled")) {
            object.addProperty("enabled", bool);
            save();
            return;
        }
        throw new JsonParseException("No member \"enabled\" found for " + key);
    }

    public static boolean addBlock(Block block) {
        if (ignoredBlocks.contains(block)) {
            return false;
        }
        ignoredBlocks.add(block);
        save();
        return true;
    }

    public static boolean removeBlock(Block block) {
        if (ignoredBlocks.contains(block)) {
            ignoredBlocks.remove(block);
            save();
            return true;
        }
        return false;
    }

    public static Set<Block> getIgnoredBlocks() {
        return ignoredBlocks;
    }

    public static void set(String key, String value) {
        root.addProperty(key, value);
        save();
    }

    public static void set(String key, boolean value) {
        root.addProperty(key, value);
        save();
    }

    public static void set(String key, Number value) {
        root.addProperty(key, value);
        save();
    }

    public static void set(String key, Character value) {
        root.addProperty(key, value);
        save();
    }
}