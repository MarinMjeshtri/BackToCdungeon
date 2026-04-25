//Klasat per combatin e bossit dhe zgjedja qe ben me RNG.
//*******DUHET ME BO PERSHTATJEN ME JSON -- nvm I did it cuz im a pro */
package com.dungeons.systems.CombatSystem;

import java.util.Random;


public class BossLoader extends Combatant {

    private String id;
    private String title;          
    private final Random rng = new Random();

    public BossLoader() {}

    /**
     *
     * @return the chosen Move
     * @throws IllegalStateException if the boss has no moves loaded 
     */
    @Override
    public Move chooseMove() {
        if (moves == null || moves.isEmpty()) {
            throw new IllegalStateException("Boss '" + name + "' has no moves. Check stats.json.");
        }
        int index = rng.nextInt(moves.size());
        return moves.get(index);
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @Override
    public String toString() {
        return String.format("%s — %s | HP: %d/%d | ATK: %d | DEF: %d",
                name, title, currentHp, maxHp, attack, defense);
    }
}
