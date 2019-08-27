package xy.reflect.ui.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.sound.sampled.AudioFormat;
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
	private ExecutorService playExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread result = new Thread(r);
			result.setDaemon(true);
			return result;
		}
	});
	private Future<?> playTask;
	private int currentTrackIndex = 0;

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

	public Track getCurrentTrack() {
		if (currentTrackIndex == -1) {
			return null;
		}
		if (currentTrackIndex >= playList.size()) {
			return null;
		}
		return playList.get(currentTrackIndex);

	}

	public String getCurrentTrackDescription() {
		Track currentTrack = getCurrentTrack();
		if (currentTrack == null) {
			return "";
		}
		return (currentTrackIndex + 1) + " - " + currentTrack.getFileName() + " (" + currentTrack.getFormattedDuration()
				+ ")";
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

	public void play() throws Exception {
		stop();
		playTask = playExecutor.submit(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						if (playList.size() == 0) {
							return;
						}
						if (currentTrackIndex == -1) {
							currentTrackIndex = playList.size() - 1;
						}
						if (currentTrackIndex >= playList.size()) {
							currentTrackIndex = 0;
						}
						if (clip.isRunning()) {
							clip.stop();
						}
						if (clip.isOpen()) {
							clip.close();
						}
						Track currentTrack = playList.get(currentTrackIndex);
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

	public void next() throws Exception {
		currentTrackIndex++;
		play();
	}

	public void previous() throws Exception {
		currentTrackIndex--;
		play();
	}

	public void stop() throws Exception {
		if (playTask != null) {
			playTask.cancel(true);
		}
		if (clip.isRunning()) {
			clip.stop();
		}
		if (clip.isOpen()) {
			clip.close();
		}
	}

	public class Track {
		private File file;

		public Track(File file) {
			super();
			this.file = file;
		}

		public String getFileName() {
			return file.getName();
		}

		public String getDirectoryPath() {
			return file.getAbsoluteFile().getParent();
		}

		public int getDurationSeconds() {
			try {
				AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
				AudioFormat format = audioStream.getFormat();
				if (!AudioFormat.Encoding.PCM_SIGNED.equals(format.getEncoding())) {
					format = new AudioFormat(format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels(),
							true, format.isBigEndian());
					audioStream = AudioSystem.getAudioInputStream(format, audioStream);
				}
				return Math.round(file.length() / (format.getFrameSize() * format.getFrameRate()));
			} catch (Exception e) {
				return -1;
			}
		}

		public String getFormattedDuration() {
			int durationSeconds = getDurationSeconds();
			if (durationSeconds == -1) {
				return "";
			}
			int minutes = durationSeconds / 60;
			int remainingSeconds = durationSeconds % 60;
			return minutes + ":" + remainingSeconds;
		}

		public String getStatusIndicator() {
			if (getCurrentTrack() == this) {
				return ">";
			} else {
				return "";
			}
		}

	}

}
