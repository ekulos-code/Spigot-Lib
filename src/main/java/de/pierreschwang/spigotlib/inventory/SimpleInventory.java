package de.pierreschwang.spigotlib.inventory;

import de.pierreschwang.spigotlib.item.ItemFactory;
import de.pierreschwang.spigotlib.nms.NmsHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SimpleInventory {

    private final Inventory inventory;
    private final Map<Integer, Consumer<InventoryClickEvent>> clickHandlers = new HashMap<>();
    private Consumer<Player> inventoryCloseListener = (player) -> {
    };

    public SimpleInventory(int size, String title) {
        inventory = Bukkit.createInventory(null, size, title);
    }

    public SimpleInventory(int size) {
        inventory = Bukkit.createInventory(null, size);
    }

    public SimpleInventory(InventoryType type) {
        inventory = Bukkit.createInventory(null, type);
    }

    public SimpleInventory(InventoryType type, String title) {
        inventory = Bukkit.createInventory(null, type, title);
    }

    public SimpleInventory setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> eventConsumer) {
        this.inventory.setItem(slot, item);
        this.clickHandlers.put(slot, eventConsumer);
        return this;
    }

    public SimpleInventory setItem(int slot, ItemFactory<?> item, Consumer<InventoryClickEvent> eventConsumer) {
        return setItem(slot, item.apply(), eventConsumer);
    }

    public void setTitle(String title, Player... players) {
        try {
            ItemFactory.wrap(new ItemStack(Material.SADDLE)).name("Test").apply();
            Class<?> chatMessageClass = NmsHelper.getNmsClass("ChatMessage");
            if (chatMessageClass == null)
                return;
            Object chatMessage = chatMessageClass.getDeclaredConstructor(String.class, Object[].class).newInstance(title, new Object[0]);
            for (Player player : players) {
                Class<?> packetPlayOutOpenWindowPacket = NmsHelper.getNmsClass("PacketPlayOutOpenWindow");
                if (packetPlayOutOpenWindowPacket == null)
                    return;

                Object playerHandle = NmsHelper.playerHandle(player);
                if (playerHandle == null)
                    return;

                Object activeContainer = playerHandle.getClass().getField("activeContainer").get(playerHandle);
                int windowId = activeContainer.getClass().getField("windowId").getInt(activeContainer);

                Object packet = packetPlayOutOpenWindowPacket
                        .getDeclaredConstructor(int.class, String.class, NmsHelper.getNmsClass("IChatBaseComponent"), int.class)
                        .newInstance(
                                windowId,
                                "minecraft:" + getInventory().getType().name().toLowerCase(),
                                chatMessage,
                                getInventory().getSize());

                NmsHelper.sendPacket(player, packet);
                player.updateInventory();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public SimpleInventory setItem(int slot, ItemStack item) {
        return setItem(slot, item, (ev) -> {
        });
    }

    public SimpleInventory setItem(int slot, ItemFactory<?> item) {
        return setItem(slot, item, (ev) -> {
        });
    }

    public SimpleInventory fill(int beginning, int end, ItemStack item) {
        for (int i = beginning; i <= end; i++) {
            setItem(i, item);
        }
        return this;
    }

    public SimpleInventory fill(int beginning, int end, ItemFactory<?> item) {
        return fill(beginning, end, item.apply());
    }

    public SimpleInventory setCloseListener(Consumer<Player> eventConsumer) {
        this.inventoryCloseListener = eventConsumer;
        return this;
    }

    public void open(Player... players) {
        for (Player player : players) {
            player.openInventory(inventory);
        }
    }

    Inventory getInventory() {
        return inventory;
    }

    Map<Integer, Consumer<InventoryClickEvent>> getClickHandlers() {
        return clickHandlers;
    }

    Consumer<Player> getInventoryCloseListener() {
        return inventoryCloseListener;
    }

}