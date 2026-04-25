package com.dungeons.MusicandSoundsCode;


public class GameMusicManager {

    private static final String TRACK_OPENING    = AudioManager.MUSIC_EXPLORATION; 
    private static final String TRACK_GAMEPLAY   = AudioManager.MUSIC_EXPLORATION; 
    private static final String TRACK_COMBAT     = AudioManager.MUSIC_BOSS;        
    private static final String TRACK_FINAL_BOSS = AudioManager.MUSIC_FINAL_BOSS;  
    private static final String TRACK_ENDING     = AudioManager.MUSIC_LABORATORY;  

    public static final String FINAL_BOSS_ID = "final_boss";

    public enum MusicState {
        NONE, OPENING, GAMEPLAY, COMBAT, FINAL_BOSS, ENDING
    }

    private static MusicState currentState = MusicState.NONE;

    private GameMusicManager() {}

    public static void playOpening() {
        transitionMusic(MusicState.OPENING, TRACK_OPENING);
    }

    public static void playGameplay() {
        transitionMusic(MusicState.GAMEPLAY, TRACK_GAMEPLAY);
    }

    public static void playCombat() {
        transitionMusic(MusicState.COMBAT, TRACK_COMBAT);
    }

    public static void playFinalBoss() {
        transitionMusic(MusicState.FINAL_BOSS, TRACK_FINAL_BOSS);
    }

    public static void playEnding() {
        if (currentState == MusicState.ENDING) return;
        currentState = MusicState.ENDING;
        AudioManager.stopMusic();
        AudioManager.playMusic(TRACK_ENDING);
        System.out.println("[GameMusicManager] → ENDING");
        printEndingMessage();
    }

    public static void pauseMusic() {
        AudioManager.pauseMusic();
        System.out.println("[GameMusicManager] Music paused.");
    }

    public static void resumeMusic() {
        AudioManager.resumeMusic();
        System.out.println("[GameMusicManager] Music resumed.");
    }

    public static void stopMusic() {
        AudioManager.stopMusic();
        currentState = MusicState.NONE;
    }

    public static void setMusicVolume(float volume) {
        AudioManager.setMusicVolume(volume);
    }

    public static MusicState getCurrentMusicState() {
        return currentState;
    }


    public static void playWalkingSound() {
        AudioManager.playSound(AudioManager.SFX_WALKING);
    }

    public static void playHitSound() {
        AudioManager.playSound(AudioManager.SFX_HIT);
    }


    public static void playSwordSound() {
        AudioManager.playSound(AudioManager.SFX_SWORD);
    }


    public static void playLightningSound() {
        AudioManager.playSound(AudioManager.SFX_LIGHTNING);
    }

    public static void playMagicSpellSound() {
        AudioManager.playSound(AudioManager.SFX_MAGIC_SPELL);
    }

    public static void playCloneSound() {
        AudioManager.playSound(AudioManager.SFX_CLONE);
    }

    public static void playSpawnWallSound() {
        AudioManager.playSound(AudioManager.SFX_SPAWN_WALL);
    }

    public static void playSpawnTurretSound() {
        AudioManager.playSound(AudioManager.SFX_SPAWN_TURRET);
    }

    public static void playPickupSound() {
        AudioManager.playSound(AudioManager.SFX_PICKUP);
    }

    public static void playLevelUpSound() {
        AudioManager.playSound(AudioManager.SFX_LEVEL_UP);
    }

    public static void playGameOverSound() {
        AudioManager.playSound(AudioManager.SFX_GAME_OVER);
    }

    public static void setSfxVolume(float volume) {
        AudioManager.setSfxVolume(volume);
    }

    private static void transitionMusic(MusicState newState, String track) {
        if (currentState == newState) return;
        currentState = newState;
        AudioManager.playMusic(track);
        System.out.println("[GameMusicManager] → " + newState + " (" + track + ")");
    }

    private static void printEndingMessage() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║                                              ║");
        System.out.println("║   Back to CDungeons                          ║");
        System.out.println("║                                              ║");
        System.out.println("║   Thank you for playing!                     ║");
        System.out.println("║   The dungeon has been conquered.            ║");
        System.out.println("║   Your legend will echo through these halls. ║");
        System.out.println("║                                              ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();
    }
}
