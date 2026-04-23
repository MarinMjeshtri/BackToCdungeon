//Ka klasa qe behen exteend nga lojtari dhe bosat. Menaxhim per HP. stats vijn nga stats.json
package com.dungeons.systems.CombatSystem;

import java.util.List;


public abstract class Combatant {

    protected String name;
    protected int maxHp;
    protected int currentHp;
    protected int attack;   
    protected int defense;  
    protected List<Move> moves;

  
    /**
     
      @param rawDamage damage value before defense reduction
      @return actual damage dealt
     */

    public int takeDamage(int rawDamage) {
        int effective = Math.max(1, rawDamage - defense);
        currentHp = Math.max(0, currentHp - effective);
        return effective;
    }

    /**
     * Restores HP, capped at maxHp.
     *
     * @param amount HP to restore
     * @return actual HP restored
     */
    public int heal(int amount) {
        int actual = Math.min(amount, maxHp - currentHp);
        currentHp += actual;
        return actual;
    }

    
    public boolean isDefeated() {
        return currentHp <= 0;
    }

    /**
     * Kthen hP si perqindje -  Ur welcome Marin
     */
    public double getHpPercent() {
        return (double) currentHp / maxHp;
    }

    //Zgjedhja e moves 

    //User e zgjedh, Bosi RNG per tani
    public abstract Move chooseMove();



    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    public List<Move> getMoves() { return moves; }
    public void setMoves(List<Move> moves) { this.moves = moves; }

    @Override
    public String toString() {
        return String.format("%s | HP: %d/%d | ATK: %d | DEF: %d",
                name, currentHp, maxHp, attack, defense);
    }
}
