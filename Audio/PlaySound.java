package Audio;//package org.wikijava.sound.playWave;

import VideoPlayer.VideoPlayer;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;

import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;

public class PlaySound {

    private InputStream waveStream;

    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb

	public int playMode=0;
    public int playspeed=176400;
	public final int audioSize=423428096; //52.9MB 需要一次性读入
    /**
     * CONSTRUCTOR
     */
    public PlaySound(InputStream waveStream) {
		this.waveStream = waveStream;
    }

    public void play() throws PlayWaveException {

		AudioInputStream audioInputStream = null;
		try {
			InputStream bufferedIn = new BufferedInputStream(this.waveStream); // new
		    audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
		} catch (UnsupportedAudioFileException e1) {
		    throw new PlayWaveException(e1);
		} catch (IOException e1) {
		    throw new PlayWaveException(e1);
		}

		// Obtain the information about the AudioInputStream
		AudioFormat audioFormat = audioInputStream.getFormat();
		Info info = new Info(SourceDataLine.class, audioFormat);

		// opens the audio channel
		SourceDataLine dataLine = null;
		try {
		    dataLine = (SourceDataLine) AudioSystem.getLine(info);
		    dataLine.open(audioFormat, this.audioSize);
			System.out.println(dataLine.getBufferSize());
		} catch (LineUnavailableException e1) {
		    throw new PlayWaveException(e1);
		}

		// Starts the music :P
		dataLine.start();

		int readBytes = 0;
		byte[] audioBuffer = new byte[this.audioSize];

		try {
			while (readBytes != -1) {
				if (playMode==1) {
					System.out.println("the audio is start");
					readBytes = audioInputStream.read(audioBuffer, 0,
							this.audioSize);
					System.out.println(readBytes);
					if (readBytes >= 0) {
						System.out.println("the audio is write");
						dataLine.write(audioBuffer,  10000000, this.audioSize-10000000);
					}
				} else if(playMode==0) {
					Thread.sleep(1);
				}else if(playMode==-1){
					System.exit(0);
				}
			}
		}catch (IOException e1) {
		    throw new PlayWaveException(e1);
		}catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
		    // plays what's left and and closes the audioChannel
		    dataLine.drain();
		    dataLine.close();
		}

    }

	public void PlayAudio(int currentFrame) throws PlayWaveException{
		AudioInputStream audioInputStream = null;
		try {
			InputStream bufferedIn = new BufferedInputStream(this.waveStream); // new
			audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
		} catch (UnsupportedAudioFileException e1) {
			throw new PlayWaveException(e1);
		} catch (IOException e1) {
			throw new PlayWaveException(e1);
		}
		// Obtain the information about the AudioInputStream
		AudioFormat audioFormat = audioInputStream.getFormat();
		Info info = new Info(SourceDataLine.class, audioFormat);

		// opens the audio channel
		SourceDataLine dataLine = null;
		try {
			dataLine = (SourceDataLine) AudioSystem.getLine(info);
			dataLine.open(audioFormat,this.audioSize);
		} catch (LineUnavailableException e1) {
			throw new PlayWaveException(e1);
		}

		// Starts the music :P
		dataLine.start();

		int readBytes = 0;
		byte[] audioBuffer = new byte[this.EXTERNAL_BUFFER_SIZE];

		try {
			while (readBytes != -1) {
				if (playMode==1) {
					System.out.println("the audio is start");
					readBytes = audioInputStream.read(audioBuffer, 0,
							audioBuffer.length);
					if (readBytes >= 0) {
						dataLine.write(audioBuffer, 0, readBytes);
					}
				} else if(playMode==0) {
					Thread.sleep(1);
				}else if(playMode==-1){
					System.exit(0);
				}
			}
		}catch (IOException e1) {
			throw new PlayWaveException(e1);
		}catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			// plays what's left and and closes the audioChannel
			dataLine.drain();
			dataLine.close();
		}

	}

}
