package com.dungeons.systems.CombatSystem;
/**
 * Represents the final outcome of a combat encounter.
 *
 * PLAYER_WIN  — Boss HP reached zero first.
 * PLAYER_LOSE — Player HP reached zero first.
 * ONGOING     — Combat is still in progress.
 */
public enum CombatResult {
    PLAYER_WIN,
    PLAYER_LOSE,
    ONGOING
}
