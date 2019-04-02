package xy.reflect.ui.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.util.MoreSystemProperties;

/**
 * Audio Player GUI generated using only the javax.sound.* API and the XML
 * declarative customizations.
 * 
 * @author olitank
 *
 */
public class AudioPlayer {

	public static void main(String[] args) throws IOException {
		System.out.println("Set the following system property to disable the design mode:\n-D"
				+ MoreSystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS + "=true");

		AudioPlayer player = new AudioPlayer();

		CustomizedUI reflectionUI = new CustomizedUI();
		SwingCustomizer renderer = new SwingCustomizer(reflectionUI, "audioPlayer.icu");
		renderer.openObjectFrame(player);
	}

	private List<Track> playList = new ArrayList<Track>();
	private Clip clip;

	public AudioPlayer() {
		try {
			clip = AudioSystem.getClip();
		} catch (LineUnavailableException e) {
			throw new AssertionError(e);
		}
	}

	public List<Track> getPlayList() {
		return playList;
	}

	public void setPlayList(List<Track> playList) {
		this.playList = playList;
	}

	public void importDirectory(File directory) {
		if(Thread.currentThread().isInterrupted()){
			return;
		}
		System.out.println("Importing directory '" + directory + "'...");
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				importDirectory(file);
			} else {
				try {
					if (AudioSystem.getAudioInputStream(file) != null) {
						playList.add(new Track(file));
					}
				} catch (UnsupportedAudioFileException e) {
					continue;
				} catch (IOException e) {
					continue;
				}
			}
		}
	}

	public class Track {
		private File file;

		public Track(File file) {
			super();
			this.file = file;
		}

		public void play() throws Exception {
			stop();
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
			clip.open(audioIn);
			clip.start();
		}
		
		public void stop() throws Exception {
			clip.stop();
			clip.close();
		}
		
		
		
		public String getFileName(){
			return file.getName();
		}
		
		public String getDirectoryPath(){
			return file.getAbsoluteFile().getParent();
		}
	}

}
