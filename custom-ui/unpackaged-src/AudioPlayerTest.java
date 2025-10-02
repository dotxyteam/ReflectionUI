
import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.info.custom.InfoCustomizations;

public class AudioPlayerTest {

	public static class Test {

		public void play(File fileToPlay) throws Exception {
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(fileToPlay);
			// Get a sound clip resource.
			Clip clip = AudioSystem.getClip();
			// Open audio clip and load samples from the audio input stream.
			clip.open(audioIn);
			clip.start();
		}
	}

	public static void main(String[] args) {
		InfoCustomizations infoCustomizations = new InfoCustomizations();
		CustomizedUI reflectionUI = new CustomizedUI(infoCustomizations);
		final SwingCustomizer renderer = new SwingCustomizer(reflectionUI, "unpackaged-src/default.icu");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				renderer.openObjectFrame(new Test(), null, null);
			}
		});
	}
}
