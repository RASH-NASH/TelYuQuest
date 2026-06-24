package arcana.model.shop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arcana.model.character.Character;
import arcana.model.item.Fish;
import arcana.model.item.FishingRod;
import arcana.model.item.Grimoire;
import arcana.model.item.Item;
import arcana.model.exception.NotEnoughGoldException;

public class Shop {
    private final ArrayList<Item> availableItems;

    public Shop() {
        availableItems = new ArrayList<Item>();
        seedDefaultItems();
    }

    private void seedDefaultItems() {
        availableItems.add(new FishingRod("Fishing Rod", 30));
        availableItems.add(new Fish("Silver Fish", 15));
        availableItems.add(new Fish("Golden Koi", 40));
        availableItems.add(new Grimoire("Grimoire of Ember", 75, "Fireball"));
        availableItems.add(new Grimoire("Grimoire of Tide", 75, "Water Surge"));
    }

    public List<Item> getAvailableItems() {
        return Collections.unmodifiableList(availableItems);
    }

    public void buyItem(Character character, Item item) throws NotEnoughGoldException {
        if (character == null || item == null) {
            throw new IllegalArgumentException("Character dan item tidak boleh null.");
        }

        if (character.getGold() < item.getPrice()) {
            throw new NotEnoughGoldException(
                "Gold tidak cukup untuk membeli " + item.getName() + "."
            );
        }

        character.spendGold(item.getPrice());
        character.addItem(createPurchasedItem(item));
    }

    private Item createPurchasedItem(Item item) {
        if (item instanceof Grimoire) {
            Grimoire grimoire = (Grimoire) item;
            return new Grimoire(grimoire.getName(), grimoire.getPrice(), grimoire.getRelatedSkillName());
        }

        if (item instanceof Fish) {
            return new Fish(item.getName(), item.getPrice());
        }

        if (item instanceof FishingRod) {
            return new FishingRod(item.getName(), item.getPrice());
        }

        return new Item(item.getName(), item.getPrice());
    }
}
