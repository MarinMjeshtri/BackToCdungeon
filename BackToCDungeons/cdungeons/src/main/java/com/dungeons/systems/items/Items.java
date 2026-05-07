package com.dungeons.systems.items;

public class Items {
    public String name;
    public String desc;
    public String price;
    public String image; // will be derived from item ID
    public Stats stats;

    public static class Stats {
        public int atk;
        public int def;
        public int hp;
    }
}