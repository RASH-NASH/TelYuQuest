package arcana.model.map;

import java.awt.Rectangle;

public class InteractionZone {
    private InteractionType interactionType;
    private Rectangle bounds;
    private String label;
    private String prompt;

    public InteractionZone(InteractionType interactionType, String label, String prompt, int x, int y, int width, int height) {
        this.interactionType = interactionType;
        this.label = label;
        this.prompt = prompt;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public boolean contains(Rectangle actorBounds) {
        return bounds.intersects(actorBounds);
    }

    public InteractionType getInteractionType() {
        return interactionType;
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    public String getLabel() {
        return label;
    }

    public String getPrompt() {
        return prompt;
    }
}
