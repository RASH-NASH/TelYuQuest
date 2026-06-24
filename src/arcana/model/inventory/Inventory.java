package arcana.model.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arcana.model.item.Item;

public class Inventory {
    private static final int MAX_SLOT_COUNT = 40;
    private final ArrayList<Item> items;

    public Inventory() {
        items = new ArrayList<Item>(Collections.nCopies(MAX_SLOT_COUNT, null));
    }

    public boolean addItem(Item item) {
        if (item == null) {
            return false;
        }
        int emptySlotIndex = findFirstEmptySlotIndex();
        if (emptySlotIndex < 0) {
            return false;
        }
        items.set(emptySlotIndex, item);
        return true;
    }

    public boolean removeItem(Item item) {
        if (item == null) {
            return false;
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == item) {
                items.set(i, null);
                return true;
            }
        }
        return false;
    }

    public boolean hasItem(String itemName) {
        return findItemByName(itemName) != null;
    }

    public Item findItemByName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return null;
        }

        for (Item item : items) {
            if (item != null && item.getName().equalsIgnoreCase(itemName)) {
                return item;
            }
        }
        return null;
    }

    public boolean removeItem(String itemName) {
        Item item = findItemByName(itemName);
        if (item == null) {
            return false;
        }
        return items.remove(item);
    }

    public Item getItemAt(int index) {
        if (index < 0 || index >= items.size()) {
            return null;
        }
        return items.get(index);
    }

    public boolean setItemAt(int index, Item item) {
        if (index < 0 || index >= items.size()) {
            return false;
        }
        items.set(index, item);
        return true;
    }

    public boolean removeItemAt(int index) {
        if (index < 0 || index >= items.size() || items.get(index) == null) {
            return false;
        }
        items.set(index, null);
        return true;
    }

    public boolean swapItems(int firstIndex, int secondIndex) {
        if (firstIndex < 0 || firstIndex >= items.size() || secondIndex < 0 || secondIndex >= items.size()) {
            return false;
        }

        Item firstItem = items.get(firstIndex);
        items.set(firstIndex, items.get(secondIndex));
        items.set(secondIndex, firstItem);
        return true;
    }

    public int getItemCount() {
        int itemCount = 0;
        for (Item item : items) {
            if (item != null) {
                itemCount++;
            }
        }
        return itemCount;
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public boolean isFull() {
        return findFirstEmptySlotIndex() < 0;
    }

    public int getMaxSlotCount() {
        return MAX_SLOT_COUNT;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    private int findFirstEmptySlotIndex() {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == null) {
                return i;
            }
        }
        return -1;
    }
}
