package com.dungeons.MusicandSoundsCode;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioManager {

    public static final String MUSIC_EXPLORATION   = "Walkingthroughthe";
    public static final String MUSIC_BOSS          = "FightingtheBosses";
    public static final String MUSIC_FINAL_BOSS    = "Fightingthefinalboss";
    public static final String MUSIC_LABORATORY    = "EmotionalMomenta";

    public static final String SFX_WALKING         = "WalkingSound";
    public static final String SFX_HIT             = "HittingSound";
    public static final String SFX_SWORD           = "SwordFight";
    public static final String SFX_LIGHTNING       = "LightningStrikeSound";
    public static final String SFX_MAGIC_SPELL     = "MagicSpellSound";
    public static final String SFX_CLONE           = "CloneSound";
    public static final String SFX_SPAWN_WALL      = "SpawnWallSound";
    public static final String SFX_SPAWN_TURRET    = "SpawnTurret";
    public static final String SFX_PICKUP          = "PickupItemSound";
    public static final String SFX_LEVEL_UP        = "LevelUp";
    public static final String SFX_GAME_OVER       = "GameOverSound";

    private static Clip currentMusic = null;
    private static String currentMusicName = "";
    private static float musicVolume = 0.8f;   // 0.0 to 1.0
    private static float sfxVolume   = 1.0f;   // 0.0 to 1.0
    private static boolean musicEnabled = true;
    private static boolean sfxEnabled   = true;

    private static final String MUSIC_PATH  = "res/music/";
    private static final String SOUNDS_PATH = "res/sounds/";

    public static void playMusic(String trackName) {
        if (!musicEnabled) return;
        if (trackName.equals(currentMusicName) && currentMusic != null && currentMusic.isRunning()) return;

        stopMusic();

        try {
            File file = new File(MUSIC_PATH + trackName + ".wav");
            if (!file.exists()) {
                System.out.println("[AudioManager] Music file not found: " + file.getPath());
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            currentMusic = AudioSystem.getClip();
            currentMusic.open(audioStream);
            setClipVolume(currentMusic, musicVolume);
            currentMusic.loop(Clip.LOOP_CONTINUOUSLY);
            currentMusic.start();
            currentMusicName = trackName;

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("[AudioManager] Error playing music: " + e.getMessage());
        }
    }

    public static void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.close();
            currentMusic = null;
            currentMusicName = "";
        }
    }

    public static void pauseMusic() {
        if (currentMusic != null && currentMusic.isRunning()) {
            currentMusic.stop();
        }
    }

    public static void resumeMusic() {
        if (currentMusic != null && !currentMusic.isRunning()) {
            currentMusic.start();
        }
    }

    public static void playSound(String soundName) {
        if (!sfxEnabled) return;

        try {
            File file = new File(SOUNDS_PATH + soundName + ".wav");
            if (!file.exists()) {
                System.out.println("[AudioManager] Sound file not found: " + file.getPath());
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            setClipVolume(clip, sfxVolume);

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

            clip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("[AudioManager] Error playing sound: " + e.getMessage());
        }
    }

    public static void setMusicVolume(float volume) {
        musicVolume = Math.max(0f, Math.min(1f, volume));
        if (currentMusic != null) {
            setClipVolume(currentMusic, musicVolume);
        }
    }

    public static void setSfxVolume(float volume) {
        sfxVolume = Math.max(0f, Math.min(1f, volume));
    }

    public static void toggleMusic() {
        musicEnabled = !musicEnabled;
        if (!musicEnabled) stopMusic();
    }

    public static void toggleSfx() {
        sfxEnabled = !sfxEnabled;
    }

    private static void setClipVolume(Clip clip, float volume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            float dB = (float) (Math.log10(Math.max(volume, 0.0001)) * 20);
            dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
            gainControl.setValue(dB);
        }
    }
}