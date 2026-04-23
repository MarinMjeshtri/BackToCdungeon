//Klasat per combatin e lojtarit dhe zgjedja qe ben. 
package com.dungeons.systems.CombatSystem;

import java.util.List;


public class Player extends Combatant {

    private List<Item> items;

    public Player() {}



    /**
     * Uses an item by id.
     *
      @param itemId 
      @return
     */
    public int useItem(String itemId) {
        for (Item item : items) {
            if (item.getId().equals(itemId) && item.isAvailable()) {
                item.consume();
                return heal(item.getHealAmount());
            }
        }
        return -1; 
    }

   
    public boolean hasItems() {
        return items != null && items.stream().anyMatch(Item::isAvailable);
    }

    
    @Override
    public Move chooseMove() {
        throw new UnsupportedOperationException(
            "Player move selection is handled by CombatUI. " +
            "Use CombatEngine.executePlayerTurn() instead."
        );
    }

   
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
}
