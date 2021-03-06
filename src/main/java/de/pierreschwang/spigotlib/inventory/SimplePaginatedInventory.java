package de.pierreschwang.spigotlib.inventory;

import de.pierreschwang.spigotlib.inventory.exceptions.PaginatedInventoryException;
import de.pierreschwang.spigotlib.item.ItemFactory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimplePaginatedInventory extends SimpleInventory {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private ItemStack PREVIOUS_PAGE_ITEM = ItemFactory.skull()
            .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==")
            .name("§6<-")
            .apply();
    private ItemStack NEXT_PAGE_ITEM = ItemFactory.skull()
            .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19")
            .name("§6->")
            .apply();
    private int pageSwitcherBackSlot = -1;
    private int pageSwitcherForwardSlot = -1;
    private final LinkedHashMap<Integer, ItemStack> inventoryContents;
    private final List<Integer> dynamicSlots;
    private int currentPage;

    public SimplePaginatedInventory(int size, String title, int... dynamicSlots) {
        super(size, title);
        this.currentPage = 1;
        this.inventoryContents = new LinkedHashMap<>();
        this.dynamicSlots = Arrays.stream(dynamicSlots).boxed().collect(Collectors.toList());
        Collections.sort(this.dynamicSlots);
        if(this.dynamicSlots.size() <= 0) {
            return;
        }
        if(this.dynamicSlots.get(0) < 0) {
            throw new PaginatedInventoryException("Dynamic slot out of range: value for a dynamic slot can not be less than 0");
        }
        if(this.dynamicSlots.get(this.dynamicSlots.size() - 1) >= getInventory().getSize()) {
            throw new PaginatedInventoryException("Dynamic slot out of range: value for dynamic slot can not be greater than the size of the inventory");
        }
    }

    public LinkedHashMap<Integer, ItemStack> getInventoryContents() {
        return inventoryContents;
    }

    public List<Integer> getDynamicSlots() {
        return dynamicSlots;
    }

    public void addDynamicSlots(int... slots) {
        getDynamicSlots().addAll(Arrays.stream(slots).boxed().collect(Collectors.toList()));
    }

    public void removeDynamicSlots(int... slots) {
        getDynamicSlots().removeAll(Arrays.stream(slots).boxed().collect(Collectors.toList()));
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getMaxPage() {
        return getPageForIndex(getMaxItemIndex());
    }

    public int getNextPage() {
        return Math.min(getCurrentPage() + 1, getMaxPage());
    }

    public int getPreviousPage() {
        return Math.max(getCurrentPage() - 1, 1);
    }

    public int getPageSwitcherBackSlot() {
        return pageSwitcherBackSlot;
    }

    public SimplePaginatedInventory setPageSwitcherBackSlot(int pageSwitcherBackSlot) {
        if(getPageSwitcherBackSlot() != -1) {
            getInventory().setItem(getPageSwitcherBackSlot(), ItemFactory.create(Material.AIR).apply());
        }
        this.pageSwitcherBackSlot = pageSwitcherBackSlot;
        if(pageSwitcherBackSlot != -1) {
            getInventory().setItem(pageSwitcherBackSlot, PREVIOUS_PAGE_ITEM);
        }
        return this;
    }

    public int getPageSwitcherForwardSlot() {
        return pageSwitcherForwardSlot;
    }

    public SimplePaginatedInventory setPageSwitcherForwardSlot(int pageSwitcherForwardSlot) {
        if(getPageSwitcherForwardSlot() != -1) {
            getInventory().setItem(getPageSwitcherForwardSlot(), ItemFactory.create(Material.AIR).apply());
        }
        this.pageSwitcherForwardSlot = pageSwitcherForwardSlot;
        if(pageSwitcherForwardSlot != -1) {
            getInventory().setItem(pageSwitcherForwardSlot, NEXT_PAGE_ITEM);
        }
        return this;
    }

    public SimplePaginatedInventory setPageSwitcherBackItem(ItemStack itemStack) {
        if(getPageSwitcherBackSlot() != -1) {
            this.PREVIOUS_PAGE_ITEM = itemStack;
            getInventory().setItem(getPageSwitcherBackSlot(), PREVIOUS_PAGE_ITEM);
        }
        return this;
    }

    public SimplePaginatedInventory setPageSwitcherForwardItem(ItemStack itemStack) {
        if(getPageSwitcherForwardSlot() != -1) { 
            this.NEXT_PAGE_ITEM = itemStack;
            getInventory().setItem(getPageSwitcherForwardSlot(), NEXT_PAGE_ITEM);
        }
        return this;
    }

    public int getPageForIndex(int index) {
        if(index < 0) {
            return 1;
        }
        return (int) Math.ceil((index + 1) / (float) getDynamicSlots().size());
    }

    public int getMaxItemIndex() {
        OptionalInt optionalHighestItemSlot = getInventoryContents().keySet().stream().mapToInt(Integer::intValue).max();
        if(!optionalHighestItemSlot.isPresent())
            return -1;
        return optionalHighestItemSlot.getAsInt();
    }

    public int getOffsetForPage(int page) {
        return (page - 1) * getInventory().getSize();
    }

    public SimplePaginatedInventory setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        return this;
    }

    public SimplePaginatedInventory setItem(int page, int slot, ItemStack item, Consumer<InventoryClickEvent> eventConsumer) {
        if(page < 1) {
            throw new PaginatedInventoryException("Page number can not be less than 1");
        }
        if(!getDynamicSlots().contains(slot)) {
            throw new PaginatedInventoryException("You can not set an item on a non dynamic slot");
        }
        int targetSlot = slot + getOffsetForPage(page);
        this.inventoryContents.put(targetSlot, item);
        super.getClickHandlers().put(targetSlot, eventConsumer);
        return this;
    }

    @Override
    public SimplePaginatedInventory setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> eventConsumer) {
        return this.setItem(getPageForIndex(slot), slot, item, eventConsumer);
    }

    @Override
    public SimplePaginatedInventory setItem(int slot, ItemFactory<?> item, Consumer<InventoryClickEvent> eventConsumer) {
        return this.setItem(getPageForIndex(slot), slot, item.apply(), eventConsumer);
    }

    @Override
    public SimplePaginatedInventory setItem(int slot, ItemStack item) {
        return this.setItem(getPageForIndex(slot), slot, item, event -> {});
    }

    @Override
    public SimplePaginatedInventory setItem(int slot, ItemFactory<?> item) {
        return this.setItem(getPageForIndex(slot), slot, item.apply(), event -> {});
    }

    @Override
    public SimplePaginatedInventory fill(int beginning, int end, ItemStack item) {
        for (int i = beginning; i <= end; i++) {
            getInventory().setItem(i, item);
        }
        return this;
    }

    @Override
    public SimplePaginatedInventory fill(int beginning, int end, ItemFactory<?> item) {
        return this.fill(beginning, end, item.apply());
    }

    public SimplePaginatedInventory addItem(ItemStack item, Consumer<InventoryClickEvent> eventConsumer) {
        int[] inventoryPages = IntStream.range(1, getMaxPage() + 1).toArray();
        for (int inventoryPage : inventoryPages) {
            int offset = getOffsetForPage(inventoryPage);
            for (int dynamicSlot : dynamicSlots) {
                int targetSlot = dynamicSlot + offset;
                if(getInventoryContents().get(targetSlot) == null) {
                    return this.setItem(inventoryPage, dynamicSlot, item, eventConsumer);
                }
            }
        }
        int nextPage = getMaxPage() + 1;
        return this.setItem(nextPage, dynamicSlots.get(0), item, eventConsumer);
    }

    public SimplePaginatedInventory addItem(ItemFactory<?> item, Consumer<InventoryClickEvent> eventConsumer) {
        return this.addItem(item.apply(), eventConsumer);
    }

    public SimplePaginatedInventory addItem(ItemStack item) {
        return this.addItem(item, event -> {});
    }

    public SimplePaginatedInventory addItem(ItemFactory<?> item) {
        return this.addItem(item.apply(), event -> {});
    }

    public void refresh(int newPage, Player player) {
        if(newPage > getMaxPage() || newPage < 1) {
            return;
        }
        int offset = setCurrentPage(newPage).getOffsetForPage(newPage);
        getDynamicSlots().forEach(dynamicSlot -> getInventory().setItem(dynamicSlot, getInventoryContents().get(dynamicSlot + offset)));
        player.updateInventory();

        if(getInventory().getTitle().contains("%page%") || getInventory().getTitle().contains("%maxpage%")) {
            setTitle(getInventory().getTitle()
                    .replace("%page%", String.valueOf(getCurrentPage()))
                    .replace("%maxpage%", String.valueOf(getMaxPage())), player);
        }
    }

    public void open(int page, Player player) {
        if(page < 1) {
            throw new PaginatedInventoryException("Page number (" + page +  ") can not be less than 1");
        }
        if(page > getMaxPage()) {
            throw new PaginatedInventoryException("Page number (" + page + ") can not be greater than the maximum amount of pages (currently " + getMaxPage() + ")");
        }
        player.openInventory(getInventory());
        if(getInventory().getTitle().contains("%page%") || getInventory().getTitle().contains("%maxpage%")) {
            setTitle(getInventory().getTitle()
                    .replace("%page%", String.valueOf(getCurrentPage()))
                    .replace("%maxpage%", String.valueOf(getMaxPage())), player);
        }
        EXECUTOR_SERVICE.submit(() -> {
            int offset = setCurrentPage(page).getOffsetForPage(page);
            getDynamicSlots().forEach(dynamicSlot -> getInventory().setItem(dynamicSlot, getInventoryContents().get(dynamicSlot + offset)));
            player.updateInventory();
        });
    }

    @Override
    public void open(Player... players) {
        if(players.length > 1) {
            throw new PaginatedInventoryException("A paginated inventory can not be opened for more than one player");
        }
        if(players[0] != null) {
            this.open(1, players[0]);
        }
    }
}