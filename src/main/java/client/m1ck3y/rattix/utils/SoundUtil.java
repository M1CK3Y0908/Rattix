package client.m1ck3y.rattix.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class SoundUtil {
    private static volatile boolean playedStartupSound = false;

    public static boolean hasPlayedStartupSound() {
        return playedStartupSound;
    }

    public static synchronized void playStartupSound() {
        if (playedStartupSound) return;
        playedStartupSound = true;
        playWavAsync("welcome_to_rattix.wav");
    }

    public static void playWavAsync(String soundName) {
        Thread thread = new Thread(() -> {
            InputStream in = null;
            try {
                // 1. Try classpath resource (assets/rattix/sounds/...)
                in = SoundUtil.class.getResourceAsStream("/assets/rattix/sounds/" + soundName);

                // 2. Try rattix/assets/audio/ directory
                if (in == null) {
                    File file = new File(FileManager.AUDIO_DIR, soundName);
                    if (file.exists()) {
                        in = new FileInputStream(file);
                    }
                }

                // 3. Try current working directory
                if (in == null) {
                    File file = new File(soundName);
                    if (file.exists()) {
                        in = new FileInputStream(file);
                    }
                }

                // 4. Try absolute project path fallback
                if (in == null) {
                    File file = new File("F:\\Projects\\Rattix", soundName);
                    if (file.exists()) {
                        in = new FileInputStream(file);
                    }
                }

                if (in == null) {
                    System.err.println("[Rattix] Could not find sound file: " + soundName);
                    return;
                }

                try (BufferedInputStream bis = new BufferedInputStream(in);
                     AudioInputStream ais = AudioSystem.getAudioInputStream(bis)) {

                    AudioFormat baseFormat = ais.getFormat();
                    AudioFormat decodedFormat = baseFormat;

                    if (baseFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                        decodedFormat = new AudioFormat(
                                AudioFormat.Encoding.PCM_SIGNED,
                                baseFormat.getSampleRate(),
                                16,
                                baseFormat.getChannels(),
                                baseFormat.getChannels() * 2,
                                baseFormat.getSampleRate(),
                                false
                        );
                    }

                    try (AudioInputStream dais = (decodedFormat != baseFormat) ? AudioSystem.getAudioInputStream(decodedFormat, ais) : ais) {
                        Clip clip = AudioSystem.getClip();
                        clip.addLineListener(event -> {
                            if (event.getType() == LineEvent.Type.STOP) {
                                clip.close();
                            }
                        });

                        clip.open(dais);

                        // Respect Minecraft Master Volume
                        Minecraft mc = Minecraft.getMinecraft();
                        if (mc != null && mc.gameSettings != null) {
                            float masterVolume = mc.gameSettings.getSoundLevel(SoundCategory.MASTER);
                            if (masterVolume <= 0.001f) {
                                clip.close();
                                return;
                            }
                            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                                float dB = (float) (Math.log10(Math.max(0.0001f, masterVolume)) * 20.0);
                                dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
                                gainControl.setValue(dB);
                            }
                        }

                        clip.start();
                    }
                }
            } catch (Exception e) {
                System.err.println("[Rattix] Failed to play sound: " + soundName);
                e.printStackTrace();
            }
        }, "Rattix-Audio-Player");

        thread.setDaemon(true);
        thread.start();
    }
}
