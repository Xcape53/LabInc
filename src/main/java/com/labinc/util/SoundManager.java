package com.labinc.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.SourceDataLine;

import com.labinc.model.GameSettings;

/**
 * Manager dźwięków obsługujący WAV (SFX) i MP3 (Music Loop)
 * Z pełną kontrolą głośności.
 */
public class SoundManager {
    private static SoundManager instance;
    private final Map<String, Clip> soundClips = new HashMap<>();

    // Music Loop variables
    private SourceDataLine musicLine;
    private Thread musicThread;
    private volatile boolean isMusicPlaying = false;
    private volatile boolean stopMusicFlag = false;

    private SoundManager() {
        // Load settings
        GameSettings settings = GameSettings.getInstance();

        // Load default sounds
        loadSound("click1", "/sounds/click.wav");
        loadSound("click2", "/sounds/click2.wav");
        loadSound("achievement", "/sounds/achievement.wav");

        // Start music if enabled
        if (settings.isMusicEnabled()) {
            startMusic();
        }
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void loadSound(String name, String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("[Sound] Sound not found: " + path);
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            soundClips.put(name, clip);
        } catch (Exception e) {
            System.err.println("[Sound] Error loading " + name + ": " + e.getMessage());
        }
    }

    public void play(String name) {
        boolean soundEnabled = GameSettings.getInstance().isSoundEnabled();
        if (!soundEnabled)
            return;

        Clip clip = soundClips.get(name);
        if (clip != null) {
            if (clip.isRunning())
                clip.stop();
            clip.setFramePosition(0);
            int volume = GameSettings.getInstance().getSoundVolume();
            setVolume(clip, volume);
            clip.start();
        } else {
            System.err.println("[Sound] Clip not found: " + name);
        }
    }

    // --- Volume Control Helper ---

    private void setVolume(Line line, int volumePercent) {
        if (line == null || !line.isOpen())
            return;
        try {
            if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);

                // Convert percent to dB (logarithmic)
                // 100% = 0dB, 50% = -6dB, 0% = -80dB (mute)

                // Better Log Scale
                float db;
                if (volumePercent <= 0) {
                    db = gainControl.getMinimum();
                } else {
                    db = (float) (Math.log(volumePercent / 100.0) / Math.log(10.0) * 20.0);
                }

                // Clamp
                db = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), db));
                gainControl.setValue(db);
            }
        } catch (Exception e) {
            // Ignore volume error
        }
    }

    public void setMusicVolume(int volume) {
        if (musicLine != null) {
            setVolume(musicLine, volume);
        }
    }

    // --- Music Player (Streaming) ---

    public synchronized void startMusic() {
        if (!GameSettings.getInstance().isMusicEnabled() || isMusicPlaying)
            return;

        stopMusicFlag = false;
        isMusicPlaying = true;

        musicThread = new Thread(() -> {
            while (isMusicPlaying && !stopMusicFlag) {
                try (InputStream is = getClass().getResourceAsStream("/sounds/loop.mp3")) {
                    if (is == null) {
                        System.err.println("Music file not found.");
                        break;
                    }
                    InputStream bufferedIn = new BufferedInputStream(is);
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedIn);

                    // Get format and decoded format (PCM)
                    AudioFormat baseFormat = audioIn.getFormat();
                    AudioFormat decodedFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            baseFormat.getSampleRate(),
                            16,
                            baseFormat.getChannels(),
                            baseFormat.getChannels() * 2,
                            baseFormat.getSampleRate(),
                            false);

                    AudioInputStream decodedAudioIn = AudioSystem.getAudioInputStream(decodedFormat, audioIn);

                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
                    musicLine = (SourceDataLine) AudioSystem.getLine(info);

                    musicLine.open(decodedFormat);
                    // Set initial volume
                    setVolume(musicLine, GameSettings.getInstance().getMusicVolume());

                    musicLine.start();

                    byte[] buffer = new byte[4096];
                    int nBytesRead;
                    while (isMusicPlaying && !stopMusicFlag
                            && (nBytesRead = decodedAudioIn.read(buffer, 0, buffer.length)) != -1) {
                        musicLine.write(buffer, 0, nBytesRead);
                        // Real-time volume update check not needed as we use setVolume on the line
                        // object directly from outside
                    }

                    musicLine.drain();
                    musicLine.stop();
                    musicLine.close();
                    decodedAudioIn.close();

                } catch (Exception e) {
                    System.err.println("Error playing music: " + e.getMessage());
                    // e.printStackTrace();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                    } // Prevent tight loop on error
                }
            }
            isMusicPlaying = false;
        });
        musicThread.setDaemon(true);
        musicThread.start();
    }

    public synchronized void stopMusic() {
        stopMusicFlag = true;
        isMusicPlaying = false;
        if (musicLine != null) {
            musicLine.stop();
            musicLine.close();
        }
        if (musicThread != null) {
            musicThread.interrupt();
        }
    }

    public void updateMusicState() {
        if (GameSettings.getInstance().isMusicEnabled()) {
            if (!isMusicPlaying)
                startMusic();
        } else {
            if (isMusicPlaying)
                stopMusic();
        }
    }

    // --- Shortcuts ---
    public void playClick1() {
        play("click1");
    }

    public void playClick2() {
        play("click2");
    }

    public void playAchievement() {
        play("achievement");
    }
}
