package arcana.ui;

public class InventoryOverlayState {
    private final int slotCount;
    private final int columns;
    private final int rows;
    private int selectedSlotIndex;
    private int swapSourceSlotIndex;

    public InventoryOverlayState(int slotCount, int columns) {
        this.slotCount = slotCount;
        this.columns = columns;
        this.rows = slotCount / columns;
        this.selectedSlotIndex = 0;
        this.swapSourceSlotIndex = -1;
    }

    public int getSelectedSlotIndex() {
        return selectedSlotIndex;
    }

    public void setSelectedSlotIndex(int slotIndex) {
        selectedSlotIndex = clampSlotIndex(slotIndex);
    }

    public int getSwapSourceSlotIndex() {
        return swapSourceSlotIndex;
    }

    public boolean isSwapSource(int slotIndex) {
        return swapSourceSlotIndex == slotIndex;
    }

    public void setSwapSourceToSelectedSlot() {
        swapSourceSlotIndex = selectedSlotIndex;
    }

    public void clearSwapSource() {
        swapSourceSlotIndex = -1;
    }

    public void clearSwapSourceIfMatchesSelectedSlot() {
        if (swapSourceSlotIndex == selectedSlotIndex) {
            swapSourceSlotIndex = -1;
        }
    }

    public void moveSelection(int columnDelta, int rowDelta) {
        int currentColumn = selectedSlotIndex % columns;
        int currentRow = selectedSlotIndex / columns;
        int nextColumn = Math.max(0, Math.min(columns - 1, currentColumn + columnDelta));
        int nextRow = Math.max(0, Math.min(rows - 1, currentRow + rowDelta));
        selectedSlotIndex = clampSlotIndex((nextRow * columns) + nextColumn);
    }

    private int clampSlotIndex(int slotIndex) {
        return Math.max(0, Math.min(slotCount - 1, slotIndex));
    }
}
