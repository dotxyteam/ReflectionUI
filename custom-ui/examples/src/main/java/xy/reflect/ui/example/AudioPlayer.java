package xy.reflect.ui.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

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
	private ExecutorService playAllExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread result = new Thread(r);
			result.setDaemon(true);
			return result;
		}
	});
	private Future<?> playAllTask;
	private String currently;

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

	public String getCurrently() {
		return currently;
	}

	public void importDirectory(File directory) {
		if (Thread.currentThread().isInterrupted()) {
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

	public void playAll() throws Exception {
		stop();
		playAllTask = playAllExecutor.submit(new Runnable() {
			@Override
			public void run() {
				int currentTrackIndex = 0;
				while (true) {
					try {
						if (currentTrackIndex >= playList.size()) {
							return;
						}
						clip.stop();
						clip.close();
						Track currentTrack = playList.get(currentTrackIndex);
						currently = (currentTrackIndex + 1) + " - " + currentTrack.getFileName();
						AudioInputStream audioIn = AudioSystem.getAudioInputStream(currentTrack.file);
						clip.open(audioIn);
						clip.start();
						Thread.sleep(1000);
						while (clip.isRunning()) {
							Thread.sleep(1000);
						}
						if (Thread.currentThread().isInterrupted()) {
							break;
						}
						currentTrackIndex++;
					} catch (Exception e) {
						throw new AssertionError(e);
					}
				}
			}
		});
	}

	public void stop() throws Exception {
		if (playAllTask != null) {
			playAllTask.cancel(true);
		}
		clip.stop();
		clip.close();
	}

	public class Track {
		private File file;

		public Track(File file) {
			super();
			this.file = file;
		}

		public void play() throws Exception {
			currently = getFileName();
			stop();
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
			clip.open(audioIn);
			clip.start();
		}

		public String getFileName() {
			return file.getName();
		}

		public String getDirectoryPath() {
			return file.getAbsoluteFile().getParent();
		}
	}

}
