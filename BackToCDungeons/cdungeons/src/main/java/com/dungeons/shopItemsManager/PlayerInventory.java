package com.dungeons.shopItemsManager;


public class PlayerInventory {

    private static PlayerInventory instance = new PlayerInventory();
    public static PlayerInventory getInstance() {
        return instance;
    }
    private PlayerInventory() {}

    private Shop[] slots = new Shop[4];

    public boolean addItem(Shop item) {
        for(int i=0;i<slots.length;i++) {
            if(slots[i] == null){
                slots[i] = item;
                return true;
            }
        }
        return false;
    }

    public Shop getSlot(int index) {
        return slots[index];
    }

    public boolean isFull() {
        for(Shop slot : slots) {
            if (slot == null) {
                return false;
            }
        }
        return true;
    }

    public void clearSlot(int index){
        slots[index] = null;
    }

}
