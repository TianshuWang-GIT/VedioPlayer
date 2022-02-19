package VideoPlayer;

import Audio.PlaySound;
import Audio.PlayWaveException;
import Forms.BoxUnit;

import Forms.BoxUnit;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sound.sampled.*;
import javax.swing.*;


public class VideoPlayer {
    // video property
    int width = 352;
    int height = 288;
    public String videoName, videoPath;


    // status
    int currentFrame = 0, loadedFrame = 0, maxFrame = 8999;
    boolean playing = false;
    boolean dragged = false;
    boolean destroyed = false;
    AtomicBoolean bufferlocked = new AtomicBoolean(false);

    // buffer
    BufferedImage[] bufferedFrames = new BufferedImage[100];
    int BufferSize = bufferedFrames.length;

    // loader and player thread
    Loader loader;
    Player player;
    AudioPlayer audioPlayer;

    //boxes
    public DefaultListModel<BoxUnit> boxes;

    public Thread loader_thread, player_thread;

    //audio parameter and audio part
    public PlaySound playSound;
    public FileInputStream adFile;
    public Thread audio_thread;

    // Loader keeps reading frames into buffer
    class Loader implements Runnable {

        // read in single frame and store it in corresponding location
        private void readImageRGB(int frameNo) {
            // frame # check before reading from disk
            if (frameNo > maxFrame) return;

            // initialize return variable
            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            // path construction
            String filename = videoPath + '/' + videoName + String.format("%04d", frameNo + 1) + ".rgb";

            // try to read file from disk
            try {
                int frameLength = width * height * 3;

                File file = new File(filename);
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                raf.seek(0);

                // changed from
                // long len = frameLength;
                // byte[] bytes = new byte[(int) len];
                byte[] bytes = new byte[frameLength];

                raf.read(bytes);
                raf.close();

                int ind = 0;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        byte a = 0;
                        byte r = bytes[ind];
                        byte g = bytes[ind + height * width];
                        byte b = bytes[ind + height * width * 2];

                        int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                        // int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                        result.setRGB(x, y, pix);
                        ind++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // add loaded frame to buffer
            bufferedFrames[frameNo % BufferSize] = result;

            // update video player status
            loadedFrame = frameNo;
        }

        // buffer check and reload
        private void loadBuffer() {
            long time = System.currentTimeMillis(); // for calculating loading time
            bufferlocked.set(true);
            if (dragged) loadedFrame = currentFrame;
            int total = currentFrame + BufferSize - loadedFrame; // for calculating number of frames loaded

            // load needed frames
            for (int i = loadedFrame; i < currentFrame + BufferSize; i++) {
                readImageRGB(i);
            }
            loadedFrame = currentFrame + BufferSize;
            bufferlocked.set(false);
            // print out diagnostic info
            if (total > 0) {
                time = System.currentTimeMillis() - time;
                //System.out.println("loaded " + total + " frames took " + time + " ms, current frame " + currentFrame + ", loaded till frame " + loadedFrame);
            }
        }


        @Override
        public void run() {
            while (true) {
                if (bufferlocked.get()) {
                    continue;
                }
                loadBuffer();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        }
    }

    // Player renders frame from buffer
    class Player implements Runnable {
        BufferedImage bufferedImage1;
        ActionListener VAL;


        // initialize with first frame image
        private void frameInit(BufferedImage img) {
//            bufferedImage1 = new ImageIcon(img);
            bufferedImage1 = img;
        }

        // refresh frame display with new frame
        private void refresh() {
//            bufferedImage1 =  new ImageIcon(bufferedFrames[currentFrame % BufferSize]);
            bufferedImage1 = bufferedFrames[currentFrame % BufferSize];
            VAL.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        }


        // monitoring loop running in thread
        @Override
        public void run() {
            long lastCheck = System.currentTimeMillis();

            // refresh at 33-67-100(0) ms
            int[] intervals = {33,34,33};
            int dynamicStage = 0;


            while (!destroyed) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastCheck >= intervals[dynamicStage]) {
                    lastCheck = currentTime;
                    dynamicStage = (dynamicStage + 1) % 3;
                    if (playing) {  // only refresh when status is playing
                        // load next frame unless it's the end frame
                        if (++currentFrame >= maxFrame) {
                            pause();
                        } else {
                            refresh();
                        }
                    }
                }
            }
        }

    }

    //audio_player
    class AudioPlayer implements Runnable {
        private InputStream waveStream;

        private final int EXTERNAL_BUFFER_SIZE = 47040 / 8; //524288 / 32; // 128Kb

        public final int audio_speed = 47040;
        public int count = 0;
        public int offset;
        public boolean flag = false;
        public int audioSize = 52900000; //52.9MB 需要一次性读入
        AudioInputStream audioInputStream;
        InputStream bufferedIn;
        SourceDataLine dataLine;
        AudioFormat audioFormat;
        DataLine.Info info;
        int readBytes = 0;
        byte[] audioBuffer;

        public AudioPlayer(String AudioPath, String AudioName) {
            String audioPath = AudioPath + "/" + AudioName + ".wav";
            try {
                this.waveStream = new FileInputStream(audioPath);
            }catch (IOException e1){
                e1.printStackTrace();
            }
            audioInputStream =null;
            try {
                bufferedIn = new BufferedInputStream(this.waveStream); // new
                audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
               //System.out.println(audioInputStream.getFrameLength());
            } catch (UnsupportedAudioFileException e1) {
                try {
                    throw new PlayWaveException(e1);
                } catch (PlayWaveException e) {
                    e.printStackTrace();
                }
            } catch (IOException e1) {
                try {
                    throw new PlayWaveException(e1);
                } catch (PlayWaveException e) {
                    e.printStackTrace();
                }
            }

            // Obtain the information about the AudioInputStream
            audioFormat = audioInputStream.getFormat();
            info = new DataLine.Info(SourceDataLine.class, audioFormat);
            // opens the audio channel
            dataLine = null;
            try {
                dataLine = (SourceDataLine) AudioSystem.getLine(info);
                dataLine.open(audioFormat, this.EXTERNAL_BUFFER_SIZE);
            } catch (LineUnavailableException e1) {
                try {
                    throw new PlayWaveException(e1);
                } catch (PlayWaveException e) {
                    e.printStackTrace();
                }
            }
            dataLine.start();
            audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];
        }

        @Override
        public void run() {
            try {
                // arrive at offset
                audioInputStream.skip(offset);

                // feed dataLine
                while ((playing) && readBytes != -1) {
                    readBytes = audioInputStream.read(audioBuffer, 0, this.EXTERNAL_BUFFER_SIZE);

                    if (readBytes >= 0) {
                        dataLine.write(audioBuffer, 0, this.EXTERNAL_BUFFER_SIZE);
                    }
                }
            } catch (IOException e1) {
                try {
                    throw new PlayWaveException(e1);
                } catch (PlayWaveException e) {
                    e.printStackTrace();
                }
            } finally {
                // plays what's left and closes the audioChannel
                dataLine.drain();
                dataLine.close();
            }
        }


        public  void pause() {
            playing = false;

        }

        public void play(int position){
            offset=audio_speed*position/8;
        }

    }

    public VideoPlayer(String videoPath, int startingFrame) {

        // path and name parsing
        this.videoPath = videoPath;
        videoName = videoPath.substring(videoPath.lastIndexOf('/') + 1);

        // set starting frame as current frame
        currentFrame = startingFrame;
        //System.out.println("the videoName is "+videoName);
        //System.out.println("the videoPath  is "+ videoPath);
        // initialize loader thread
        loader = new Loader();
        loader.readImageRGB(currentFrame);
        loader.loadBuffer();
        loader_thread = new Thread(loader);

        loader_thread.start();

        // initialize player thread
        player = new Player();
        player_thread = new Thread(player);
        player.frameInit(bufferedFrames[currentFrame % 100]);
        player_thread.start();

    }

    public void play() {
        if (dragged) {
            while (bufferlocked.get()) {
                //todo: (Tony) program often stuck here.
            }
            loader.loadBuffer();
            dragged = false;
        }
        //playing == false
        playing = true;
        //audio_player
        audioPlayer = new AudioPlayer(videoPath, videoName);
        audio_thread = new Thread(audioPlayer);
        audioPlayer.play(currentFrame);
        audio_thread.start();
    }

    public void pause() {
        if (audioPlayer != null) this.audioPlayer.flag = true;
//      audioPlayer.dataLine.stop();
//      audioPlayer.dataLine.close();
        playing = false;
    }


    public void destroy() {
        this.pause();
        destroyed = true;
    }

    public void gotoFrame(int frameNo) {
        dragged = true;
        currentFrame = frameNo;
        loader.readImageRGB(frameNo);
        player.refresh();

    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public int getMaxFrame() {
        return maxFrame;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setAL(ActionListener al) {
        player.VAL = al;
    }

    public BufferedImage getBufferedImage() {
        return player.bufferedImage1;
    }


}
