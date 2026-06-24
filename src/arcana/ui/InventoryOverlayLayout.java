package arcana.ui;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class InventoryOverlayLayout {
    private final int assetWidth;
    private final int assetHeight;
    private final int slotStartX;
    private final int slotStartY;
    private final int slotSize;
    private final int slotGapX;
    private final int slotGapY;
    private final int columns;

    public InventoryOverlayLayout(int assetWidth, int assetHeight, int slotStartX, int slotStartY, int slotSize,
            int slotGapX, int slotGapY, int columns) {
        this.assetWidth = assetWidth;
        this.assetHeight = assetHeight;
        this.slotStartX = slotStartX;
        this.slotStartY = slotStartY;
        this.slotSize = slotSize;
        this.slotGapX = slotGapX;
        this.slotGapY = slotGapY;
        this.columns = columns;
    }

    public Rectangle getPanelBounds(int viewportWidth, int viewportHeight, BufferedImage panelImage) {
        int baseWidth = panelImage == null ? assetWidth : panelImage.getWidth();
        int baseHeight = panelImage == null ? assetHeight : panelImage.getHeight();
        double widthScale = (viewportWidth - 80) / (double) baseWidth;
        double heightScale = (viewportHeight - 120) / (double) baseHeight;
        double scale = Math.min(Math.min(widthScale, heightScale), 1.35);
        int panelWidth = Math.max(baseWidth, (int) Math.round(baseWidth * scale));
        int panelHeight = Math.max(baseHeight, (int) Math.round(baseHeight * scale));
        int panelX = (viewportWidth - panelWidth) / 2;
        int panelY = Math.max(48, (viewportHeight - panelHeight) / 2);
        return new Rectangle(panelX, panelY, panelWidth, panelHeight);
    }

    public Rectangle getSlotBounds(int slotIndex, Rectangle panelBounds) {
        int column = slotIndex % columns;
        int row = slotIndex / columns;
        int assetX = slotStartX + column * (slotSize + slotGapX);
        int assetY = slotStartY + row * (slotSize + slotGapY);
        return new Rectangle(
                panelBounds.x + scaleAssetX(panelBounds, assetX),
                panelBounds.y + scaleAssetY(panelBounds, assetY),
                Math.max(1, scaleAssetWidth(panelBounds, slotSize)),
                Math.max(1, scaleAssetHeight(panelBounds, slotSize)));
    }

    public int scaleAssetX(Rectangle panelBounds, int assetX) {
        return (int) Math.round(assetX * (panelBounds.width / (double) assetWidth));
    }

    public int scaleAssetY(Rectangle panelBounds, int assetY) {
        return (int) Math.round(assetY * (panelBounds.height / (double) assetHeight));
    }

    public int scaleAssetWidth(Rectangle panelBounds, int assetWidthValue) {
        return (int) Math.round(assetWidthValue * (panelBounds.width / (double) assetWidth));
    }

    public int scaleAssetHeight(Rectangle panelBounds, int assetHeightValue) {
        return (int) Math.round(assetHeightValue * (panelBounds.height / (double) assetHeight));
    }
}
