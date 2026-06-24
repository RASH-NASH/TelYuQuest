package arcana.ui;

import java.awt.Rectangle;
import java.util.List;

import arcana.model.item.Item;

public class ShopOverlayState {
    private static final int PANEL_WIDTH = 640;
    private static final int PANEL_HEIGHT = 360;
    private static final int SLOT_COLUMNS = 6;
    private static final int SLOT_WIDTH = 41;
    private static final int SLOT_HEIGHT = 38;
    private static final int SLOT_GAP_X = 6;
    private static final int SLOT_GAP_Y = 6;
    private static final int GRID_OFFSET_X = 293;
    private static final int GRID_OFFSET_Y = 82;

    private int selectedItemIndex;

    public ShopOverlayState() {
        selectedItemIndex = 0;
    }

    public void resetSelection() {
        selectedItemIndex = 0;
    }

    public int getSelectedItemIndex() {
        return selectedItemIndex;
    }

    public void moveSelection(int step, int itemCount) {
        if (itemCount <= 0) {
            selectedItemIndex = 0;
            return;
        }
        selectedItemIndex = (selectedItemIndex + step + itemCount) % itemCount;
    }

    public void setSelectedItemIndex(int selectedItemIndex, int itemCount) {
        if (itemCount <= 0) {
            this.selectedItemIndex = 0;
            return;
        }
        this.selectedItemIndex = Math.max(0, Math.min(selectedItemIndex, itemCount - 1));
    }

    public Item getSelectedItem(List<Item> items) {
        if (items == null || items.isEmpty() || selectedItemIndex >= items.size()) {
            return null;
        }
        return items.get(selectedItemIndex);
    }

    public Rectangle getPanelBounds(int width, int height) {
        return new Rectangle((width - PANEL_WIDTH) / 2, (height - PANEL_HEIGHT) / 2, PANEL_WIDTH, PANEL_HEIGHT);
    }

    public Rectangle getItemSlotBounds(int itemIndex, int width, int height) {
        Rectangle panelBounds = getPanelBounds(width, height);
        int col = itemIndex % SLOT_COLUMNS;
        int row = itemIndex / SLOT_COLUMNS;
        int slotX = panelBounds.x + GRID_OFFSET_X + (col * (SLOT_WIDTH + SLOT_GAP_X));
        int slotY = panelBounds.y + GRID_OFFSET_Y + (row * (SLOT_HEIGHT + SLOT_GAP_Y));
        return new Rectangle(slotX, slotY, SLOT_WIDTH, SLOT_HEIGHT);
    }

    public Rectangle getBuyButtonBounds(int width, int height) {
        Rectangle panelBounds = getPanelBounds(width, height);
        return new Rectangle(panelBounds.x + 272, panelBounds.y + 286, 98, 22);
    }

    public Rectangle getExitButtonBounds(int width, int height) {
        Rectangle panelBounds = getPanelBounds(width, height);
        return new Rectangle(panelBounds.x + 472, panelBounds.y + 286, 98, 22);
    }

    public int findClickedItemIndex(int mouseX, int mouseY, int width, int height, int itemCount) {
        for (int i = 0; i < itemCount; i++) {
            if (getItemSlotBounds(i, width, height).contains(mouseX, mouseY)) {
                return i;
            }
        }
        return -1;
    }
}
