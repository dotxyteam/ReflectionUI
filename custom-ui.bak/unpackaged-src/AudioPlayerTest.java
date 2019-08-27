
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;

import javafx.scene.media.MediaPlayer;
import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoCustomizationsFactory;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.SystemProperties;

@SuppressWarnings("unused")
public class AudioPlayerTest {

	public static class Test {

		
		public void play(File fileToPlay) throws Exception{
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
		CustomizedUI reflectionUI = new CustomizedUI(infoCustomizations) {
			@Override
			protected ITypeInfo getTypeInfoBeforeCustomizations(ITypeInfo type) {
				ITypeInfo result = type;
				return new InfoProxyFactory() {

					
				}.wrapTypeInfo(result);
			}
		};
		SwingCustomizer renderer = new SwingCustomizer(reflectionUI, "unpackaged-src/default.icu");
		renderer.openObjectFrame(new Test(), null, null);
	}
}
