package Audio;//package org.wikijava.sound.playWave;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class PlayWaveFile {

    public static void main(String[] args) {

	// get the command line parameters
	if (args.length < 1) {
	    System.err.println("usage: java -jar PlayWaveFile.jar [filename]");
	    return;
	}
	String filename = args[0];

	// opens the inputStream
	FileInputStream inputStream;
	try {
	    inputStream = new FileInputStream(filename);
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    return;
	}

	// initializes the playSound Object
	PlaySound playSound = new PlaySound(inputStream);

	// plays the sound
	try {
	    playSound.play();
	} catch (PlayWaveException e) {
	    e.printStackTrace();
	    return;
	}
    }

}
