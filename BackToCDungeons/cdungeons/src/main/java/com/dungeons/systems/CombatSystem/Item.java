//Logjika per items t lojtarit, merren nga stats.json 

package com.dungeons.systems.CombatSystem;


public class Item {

    private String id;
    private String name;
    private int healAmount;
    private int quantity;
    private String description;

    public Item() {}

    public Item(String id, String name, int healAmount, int quantity, String description) {
        this.id = id;
        this.name = name;
        this.healAmount = healAmount;
        this.quantity = quantity;
        this.description = description;
    }

   //Shef nese lojtari ka item 
    public boolean isAvailable() {
        return quantity > 0;
    }

    public void consume() {
        if (quantity <= 0) {
            throw new IllegalStateException("Item '" + name + "' has no uses left.");
        }
        quantity--;
    }

   
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getHealAmount() { return healAmount; }
    public void setHealAmount(int healAmount) { this.healAmount = healAmount; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return String.format("[%s] %s - +%d HP | x%d remaining | %s",
                id, name, healAmount, quantity, description);
    }
}
