//package com.dungeons.MusicandSoundsCode;
//
//import javafx.scene.media.Media;
//import javafx.scene.media.MediaPlayer;
//
//import javax.sound.sampled.*;
//import java.net.URL;
//import java.io.IOException;
//
//public class AudioManager {
//
//    public static final String MUSIC_EXPLORATION = "Walkingthroughthegame";
//    public static final String MUSIC_BOSS        = "FightingtheBosses";
//    public static final String MUSIC_FINAL_BOSS  = "Fightingthefinalboss";
//    public static final String MUSIC_LABORATORY  = "Emotionalmomentattheend";
//
//    public static final String SFX_WALKING      = "WalkingSound";
//    public static final String SFX_HIT          = "HittingSound";
//    public static final String SFX_SWORD        = "SwordFight";
//    public static final String SFX_LIGHTNING    = "LightningStrikeSound";
//    public static final String SFX_MAGIC_SPELL  = "MagicSpellSound";
//    public static final String SFX_CLONE        = "CloneSound";
//    public static final String SFX_SPAWN_WALL   = "SpawnWallSound";
//    public static final String SFX_SPAWN_TURRET = "SpawnTurret";
//    public static final String SFX_PICKUP       = "PickupItemSound";
//    public static final String SFX_LEVEL_UP     = "LevelUp";
//    public static final String SFX_GAME_OVER    = "GameOverSound";
//
//    private static final String MUSIC_PATH  = "/MusicForTheGame/";
//    private static final String SOUNDS_PATH = "/Sounds/";
//
//    private static MediaPlayer currentMusic     = null;
//    private static String      currentMusicName = "";
//    private static double      musicVolume      = 0.8;
//    private static double      sfxVolume        = 1.0;
//    private static boolean     musicEnabled     = true;
//    private static boolean     sfxEnabled       = true;
//
//    private AudioManager() {}
//
//    public static void playMusic(String trackName) {
//        if (!musicEnabled) return;
//        if (trackName.equals(currentMusicName) && currentMusic != null
//                && currentMusic.getStatus() == MediaPlayer.Status.PLAYING) return;
//
//        stopMusic();
//
//        String path = findResource(MUSIC_PATH, trackName, "mp3", "wav");
//        if (path == null) {
//            System.err.println("[AudioManager] Music not found: " + trackName);
//            return;
//        }
//        try {
//            URL url = AudioManager.class.getResource(path);
//            Media media = new Media(url.toExternalForm());
//            currentMusic = new MediaPlayer(media);
//            currentMusic.setVolume(musicVolume);
//            currentMusic.setCycleCount(MediaPlayer.INDEFINITE);
//            currentMusic.play();
//            currentMusicName = trackName;
//        } catch (Exception e) {
//            System.err.println("[AudioManager] Error playing music: " + e.getMessage());
//        }
//    }
//
//    public static void playMusicOnce(String trackName) {
//        if (!musicEnabled) return;
//        stopMusic();
//
//        String path = findResource(MUSIC_PATH, trackName, "mp3", "wav");
//        if (path == null) {
//            System.err.println("[AudioManager] Music not found: " + trackName);
//            return;
//        }
//        try {
//            URL url = AudioManager.class.getResource(path);
//            Media media = new Media(url.toExternalForm());
//            currentMusic = new MediaPlayer(media);
//            currentMusic.setVolume(musicVolume);
//            currentMusic.setCycleCount(1);
//            currentMusic.play();
//            currentMusicName = trackName;
//        } catch (Exception e) {
//            System.err.println("[AudioManager] Error playing music once: " + e.getMessage());
//        }
//    }
//
//    public static void stopMusic() {
//        if (currentMusic != null) {
//            currentMusic.stop();
//            currentMusic.dispose();
//            currentMusic = null;
//            currentMusicName = "";
//        }
//    }
//
//    public static void pauseMusic() {
//        if (currentMusic != null && currentMusic.getStatus() == MediaPlayer.Status.PLAYING)
//            currentMusic.pause();
//    }
//
//    public static void resumeMusic() {
//        if (currentMusic != null && currentMusic.getStatus() == MediaPlayer.Status.PAUSED)
//            currentMusic.play();
//    }
//
//    public static void setMusicVolume(double volume) {
//        musicVolume = Math.max(0.0, Math.min(1.0, volume));
//        if (currentMusic != null) currentMusic.setVolume(musicVolume);
//    }
//
//    public static void toggleMusic() {
//        musicEnabled = !musicEnabled;
//        if (!musicEnabled) stopMusic();
//    }
//
//    public static void playSound(String soundName) {
//        if (!sfxEnabled) return;
//
//        String path = findResource(SOUNDS_PATH, soundName, "wav", "ogg");
//        if (path == null) {
//            System.err.println("[AudioManager] Sound not found: " + soundName);
//            return;
//        }
//        try {
//            URL url = AudioManager.class.getResource(path);
//            AudioInputStream stream = AudioSystem.getAudioInputStream(url);
//            Clip clip = AudioSystem.getClip();
//            clip.open(stream);
//            applyVolume(clip, (float) sfxVolume);
//            clip.addLineListener(e -> {
//                if (e.getType() == LineEvent.Type.STOP) clip.close();
//            });
//            clip.start();
//        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
//            System.err.println("[AudioManager] Error playing sound '" + soundName + "': " + e.getMessage());
//        }
//    }
//
//    public static void setSfxVolume(double volume) {
//        sfxVolume = Math.max(0.0, Math.min(1.0, volume));
//    }
//
//    public static void toggleSfx() {
//        sfxEnabled = !sfxEnabled;
//    }
//
//    private static String findResource(String folder, String name, String... extensions) {
//        for (String ext : extensions) {
//            String path = folder + name + "." + ext;
//            if (AudioManager.class.getResource(path) != null) return path;
//        }
//        return null;
//    }
//
//    private static void applyVolume(Clip clip, float volume) {
//        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
//            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
//            float dB = (float) (Math.log10(Math.max(volume, 0.0001)) * 20);
//            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
//        }
//    }
//}
