import javax.sound.sampled.*;
import java.io.*;

public class GenerateMenuMusic {
    public static void main(String[] args) throws Exception {
        // Audio parameters
        int sampleRate = 44100;
        int duration = 4; // 4 seconds for the melody loop
        byte[] audioData = new byte[sampleRate * duration * 2]; // 16-bit audio
        
        // Simple uplifting melody (8 notes repeated)
        int[] melody = {
            262, 330, 392, 440,  // C, E, G, A (ascending)
            494, 440, 392, 330   // B, A, G, E (descending)
        };
        int noteDuration = sampleRate / 2; // 0.5 seconds per note
        
        int sampleIndex = 0;
        for (int i = 0; i < 4 && sampleIndex < audioData.length - 1; i++) { // Loop through melody 4 times
            for (int frequency : melody) {
                for (int j = 0; j < noteDuration && sampleIndex < audioData.length - 1; j++) {
                    double sample = Math.sin(2.0 * Math.PI * frequency * j / sampleRate);
                    sample *= 0.3; // Reduce volume to 30%
                    
                    // Add envelope (fade in/out)
                    double envelope = 1.0;
                    if (j < 2205) envelope = j / 2205.0; // Fade in (50ms)
                    if (j > noteDuration - 2205) envelope = (noteDuration - j) / 2205.0; // Fade out
                    
                    sample *= envelope;
                    
                    short sampleValue = (short) (sample * 32767);
                    audioData[sampleIndex++] = (byte) (sampleValue & 0xFF);
                    audioData[sampleIndex++] = (byte) ((sampleValue >> 8) & 0xFF);
                }
            }
        }
        
        // Create WAV file
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream ais = new AudioInputStream(bais, format, audioData.length / 2);
        
        File outputFile = new File("menu-music.wav");
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile);
        System.out.println("Menu music created: " + outputFile.getAbsolutePath());
    }
}
