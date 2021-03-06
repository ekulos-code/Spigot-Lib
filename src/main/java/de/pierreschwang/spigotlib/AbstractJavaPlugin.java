package de.pierreschwang.spigotlib;

import de.pierreschwang.spigotlib.command.CommandRegistry;
import de.pierreschwang.spigotlib.database.ConnectionProvider;
import de.pierreschwang.spigotlib.database.StandardConnectionProvider;
import de.pierreschwang.spigotlib.internal.PlayerListener;
import de.pierreschwang.spigotlib.internal.interceptors.PacketPlayInSettingsInterceptor;
import de.pierreschwang.spigotlib.inventory.InventoryListener;
import de.pierreschwang.spigotlib.lang.LanguageHandler;
import de.pierreschwang.spigotlib.nms.intercept.PacketInterceptorRegistry;
import de.pierreschwang.spigotlib.scoreboard.AbstractScoreboard;
import de.pierreschwang.spigotlib.scoreboard.PlayerRenderer;
import de.pierreschwang.spigotlib.user.User;
import de.pierreschwang.spigotlib.user.UserFactory;
import de.pierreschwang.spigotlib.user.UserRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Function;

public abstract class AbstractJavaPlugin<T extends User> extends JavaPlugin {

    private LanguageHandler languageHandler;
    private CommandRegistry commandRegistry;
    private UserRepository<T> userRepository;
    private AbstractScoreboard scoreboard;
    private PacketInterceptorRegistry packetInterceptorRegistry;

    @Override
    public void onEnable() {
        commandRegistry = new CommandRegistry(this);
        languageHandler = new LanguageHandler(this);
        userRepository = new UserRepository<>(this);
        scoreboard = new AbstractScoreboard(this);
        packetInterceptorRegistry = new PacketInterceptorRegistry();
        packetInterceptorRegistry.registerInterceptor("PacketPlayInSettings", new PacketPlayInSettingsInterceptor(this));
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);

        onAbstractEnable();
    }

    @Override
    public void onDisable() {
        onAbstractDisable();
    }

    /**
     * Called when the plugin is enabled
     */
    public abstract void onAbstractEnable();

    /**
     * Called when the plugin disables
     */
    public abstract void onAbstractDisable();

    public UserRepository<T> getUserRepository() {
        return userRepository;
    }

    public LanguageHandler getLanguageHandler() {
        return languageHandler;
    }

    /**
     * Should return your custom UserFactory.
     *
     * @return The user factory for building user objects.
     */
    public abstract UserFactory<T> getUserFactory();

    /**
     * The player renderer for scoreboard related views.
     *
     * @return Your custom renderer.
     */
    public PlayerRenderer getPlayerRenderer() {
        return new PlayerRenderer() {
            @Override
            public Function<User, String> getPrefix() {
                return p -> "";
            }

            @Override
            public Function<User, String> getSuffix() {
                return p -> "";
            }

            @Override
            public Function<User, String> getSidebarTitle() {
                return p -> "";
            }

            @Override
            public Function<User, String[]> getLines() {
                return p -> new String[0];
            }
        };
    }

    public Class<? extends ConnectionProvider> getConnectionProvider() {
        return StandardConnectionProvider.class;
    }

    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public T getUser(Player player) {
        return userRepository.getUser(player);
    }

    public AbstractScoreboard getScoreboard() {
        return scoreboard;
    }

    public PacketInterceptorRegistry getPacketInterceptorRegistry() {
        return packetInterceptorRegistry;
    }
}
