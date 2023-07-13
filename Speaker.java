import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

// Adapted from http://www.wolinlabs.com/blog/java.sine.wave.html
public class Speaker implements Runnable {
    final static int SAMPLING_RATE = 44100;            // Audio sampling rate
    final static int SAMPLE_SIZE = 2;                  // Audio sample size in bytes
    final static public double BUFFER_DURATION = 0.100;      //About a 100ms buffer
    final static public int SINE_PACKET_SIZE = (int) (BUFFER_DURATION * SAMPLING_RATE * SAMPLE_SIZE); 

    SourceDataLine line;
    double freq = 440;                         // Frequency of sine wave in hz
    float gain = 2f;

    //Position through the sine wave as a percentage (i.e. 0 to 1 is 0 to 2*PI)
    double cyclePosition = 0;        

    AudioFormat format;
    DataLine.Info info;

    boolean exitThread = false;

    public Speaker() {
        try {
            // Open up audio output, using 44100hz sampling rate, 16 bit samples, mono, and big 
            // endian byte ordering
            this.format = new AudioFormat(SAMPLING_RATE, 16, 1, true, true);
            this.info = new DataLine.Info(SourceDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line matching " + info + " is not supported.");
                throw new LineUnavailableException();
            }

            this.line = (SourceDataLine) AudioSystem.getLine(info);
        }
        catch (LineUnavailableException c){
            System.out.println("Speaker line unavailable");
        }
    }

    @Override
    public void run() {
        try {
            this.line.open(this.format);

            FloatControl volume = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
            volume.setValue(this.gain); // increases volume

            this.line.start();

            // Make our buffer size match audio system's buffer
            ByteBuffer audioBuffer = ByteBuffer.allocate(line.getBufferSize());

            // On each pass main loop fills the available free space in the audio buffer
            // Main loop creates audio samples for sine wave, runs until we tell the thread to exit
            // Each sample is spaced 1/SAMPLING_RATE apart in time
            while (this.exitThread == false) {
                double cycleIncrement = freq / SAMPLING_RATE;  // Fraction of cycle between samples

                audioBuffer.clear();                           // Toss out samples from previous pass

                // Generate SINE_PACKET_SIZE samples based on the current cycleIncrement from freq
                for (int i = 0; i < SINE_PACKET_SIZE / SAMPLE_SIZE; i++) {
                    audioBuffer.putShort((short) (Short.MAX_VALUE * Math.sin(2 * Math.PI * cyclePosition)));

                    cyclePosition += cycleIncrement;
                    if (cyclePosition > 1)
                        cyclePosition -= 1;
                }

                // Write sine samples to the line buffer
                // If the audio buffer is full, this would block until there is enough room,
                // but we are not writing unless we know there is enough space.
                this.line.write(audioBuffer.array(), 0, audioBuffer.position());    

                // Removed original code that waited until there were less than 
                // SINE_PACKET_SIZE samples in the buffer
            }

            // Done playing the whole waveform, now wait until the queued samples finish 
            // playing, then clean up and exit
            this.line.drain();                                         
            this.line.close();
        }
        catch (LineUnavailableException e) {}
    }

    public void setFrequency(int inFreq) {
        this.freq = inFreq;
    }

    public void exit() {
        this.exitThread = true;
    }

    public static void testFreqChange() {
        try {
            Speaker s = new Speaker();
            Thread t = new Thread(s);
            t.start();

            int freq = 27;
            while (freq < 800) {
                s.setFrequency(freq);
                Thread.sleep(100);
                freq += 10;
            }

            s.exit();
            t.interrupt();
        }
        catch (InterruptedException e) {}
    }

    public static void main(String[] args) {
        Speaker.testFreqChange();
    }
}
