package com.dungeons.MusicandSoundsCode;

public class GameMusicManager {

    public static final String FINAL_BOSS_ID = "SuperCoolSigma";

    public enum MusicState { NONE, OPENING, GAMEPLAY, COMBAT, FINAL_BOSS, ENDING }

    private static MusicState currentState = MusicState.NONE;
    private static int        walkTimer    = 0;

    private GameMusicManager() {}

    public static void playOpening() {
        transition(MusicState.OPENING, AudioManager.MUSIC_EXPLORATION);
    }

    public static void playGameplay() {
        transition(MusicState.GAMEPLAY, AudioManager.MUSIC_EXPLORATION);
    }

    public static void playCombat() {
        transition(MusicState.COMBAT, AudioManager.MUSIC_BOSS);
    }

    public static void playFinalBoss() {
        transition(MusicState.FINAL_BOSS, AudioManager.MUSIC_FINAL_BOSS);
    }

    public static void playEnding() {
        if (currentState == MusicState.ENDING) return;
        currentState = MusicState.ENDING;
        AudioManager.stopMusic();
        AudioManager.playMusicOnce(AudioManager.MUSIC_LABORATORY);
    }

    public static void pauseMusic() {
        AudioManager.pauseMusic();
    }

    public static void resumeMusic() {
        AudioManager.resumeMusic();
    }

    public static void stopMusic() {
        AudioManager.stopMusic();
        currentState = MusicState.NONE;
    }

    public static void setMusicVolume(double volume) { AudioManager.setMusicVolume(volume); }
    public static MusicState getCurrentMusicState()   { return currentState; }

    public static void tickWalkSound(boolean isMoving) {
        if (isMoving) {
            walkTimer++;
            if (walkTimer >= 20) {
                AudioManager.playSound(AudioManager.SFX_WALKING);
                walkTimer = 0;
            }
        } else {
            walkTimer = 0;
        }
    }

    public static void playHitSound() {
        AudioManager.playSound(AudioManager.SFX_HIT);
    }

    public static void playMoveSound(String moveName) {
        if (moveName == null) return;
        String lower = moveName.toLowerCase();
        if      (lower.contains("clone"))                                        AudioManager.playSound(AudioManager.SFX_CLONE);
        else if (lower.contains("wall"))                                         AudioManager.playSound(AudioManager.SFX_SPAWN_WALL);
        else if (lower.contains("turret"))                                       AudioManager.playSound(AudioManager.SFX_SPAWN_TURRET);
        else if (lower.contains("lightning"))                                    AudioManager.playSound(AudioManager.SFX_LIGHTNING);
        else if (lower.contains("spell") || lower.contains("magic")
              || lower.contains("literature") || lower.contains("lore"))         AudioManager.playSound(AudioManager.SFX_MAGIC_SPELL);
        else                                                                     AudioManager.playSound(AudioManager.SFX_SWORD);
    }

    public static void playPickupSound() {
        AudioManager.playSound(AudioManager.SFX_PICKUP);
    }

    public static void playGameOverSound() {
        AudioManager.playSound(AudioManager.SFX_GAME_OVER);
    }

    public static void setSfxVolume(double volume) { AudioManager.setSfxVolume(volume); }

    private static void transition(MusicState newState, String track) {
        if (currentState == newState) return;
        AudioManager.stopMusic();
        currentState = newState;
        AudioManager.playMusic(track);
        System.out.println("[GameMusicManager] -> " + newState + " (" + track + ")");
    }


}
