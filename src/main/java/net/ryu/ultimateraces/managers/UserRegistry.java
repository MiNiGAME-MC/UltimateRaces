package net.ryu.ultimateraces.managers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.ryu.ultimateraces.UltimateRacesPlugin;
import net.ryu.ultimateraces.api.UltimateRacesAPI;
import net.ryu.ultimateraces.utils.message.Txt;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

public class UserRegistry implements UltimateRacesAPI {
    private UltimateRacesPlugin plugin;
    private Path usersPath;
    private Map<UUID, UsersData> userCache = new ConcurrentHashMap<>();

    public UserRegistry(UltimateRacesPlugin plugin) {
        this.plugin = plugin;
        this.usersPath = plugin.getDataFolder().toPath().resolve("users");
    }

    public void init() {
        if (!Files.exists(this.usersPath)) {
            try {
                Files.createDirectories(this.usersPath);
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to create user directory, disabling...", e);
                this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            }
        }

        this.plugin.getServer().getOnlinePlayers().forEach(player -> this.getUser(player.getUniqueId(), data -> {}));
    }

    @Override
    public void getUser(UUID uuid, Consumer<UsersData> action) {
        UsersData cached = this.userCache.get(uuid);
        if (cached != null) {
            action.accept(cached);
            return;
        }

        if (this.plugin.isEnabled()) {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                UsersData data = this.userCache.compute(uuid, (k, v) -> {
                    if (v != null) {
                        return v;
                    }

                    Path userPath = this.getUserPath(uuid);

                    if (!Files.exists(userPath)) {
                        return new UsersData(uuid);
                    }

                    UsersData dat = null;

                    try (Reader reader = Files.newBufferedReader(userPath)) {
                        JsonObject obj = Txt.GSON.fromJson(reader, JsonObject.class);
                        dat = new UsersData(uuid);
                        JsonElement race = obj.get("race");
                        if (race != null) {
                            dat.setRace(race.getAsString());
                        }
                        JsonElement chat = obj.get("chat");
                        if (chat != null) {
                            dat.setChat(chat.getAsBoolean());
                        }
                    } catch (IOException e) {
                        this.plugin.getLogger().log(Level.SEVERE, "Failed to read user file: " + uuid, e);
                    }

                    return dat;
                });

                if (data != null) {
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> action.accept(data));
                }
            });
        }
    }

    public UsersData getUser(UUID uuid) {
        return this.userCache.get(uuid);
    }

    @Override
    public Map<UUID, UsersData> getUsers() {
        return Collections.unmodifiableMap(this.userCache);
    }

    public void saveUser(UUID uuid) {
        UsersData cached = this.userCache.get(uuid);

        if (cached == null) {
            return;
        }

        Path userPath = this.getUserPath(uuid);

        try (Writer writer = Files.newBufferedWriter(userPath)) {
            JsonObject obj = new JsonObject();
            obj.addProperty("uuid", cached.getUuid().toString());
            obj.addProperty("race", cached.getRace());
            obj.addProperty("chat", cached.isChat());
            Txt.GSON.toJson(obj, writer);
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save user data, dumping...", e);
            this.plugin.getLogger().severe("Data dump: " + cached.toString());
        }
    }

    private Path getUserPath(UUID uuid) {
        return this.usersPath.resolve(uuid.toString() + ".json");
    }
}
