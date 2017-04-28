package xy.reflect.ui.control.swing.customizer;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.component.AlternativeWindowDecorationsPanel;

public class CustomizationController {

	protected SwingCustomizer swingCustomizer;
	protected Set<Component> activeCustomizingComponents = new HashSet<Component>();
	protected JFrame window;

	protected CustomizationController(SwingCustomizer swingCustomizer) {
		this.swingCustomizer = swingCustomizer;
	}

	protected synchronized void customizingComponentAdded(Component customizingComponent) {
		if (activeCustomizingComponents.size() == 0) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					openWindow();
				}
			});
		}
		activeCustomizingComponents.add(customizingComponent);
	}

	protected synchronized void customizingComponentRemoved(Component customizingComponent) {
		activeCustomizingComponents.remove(customizingComponent);
		if (activeCustomizingComponents.size() == 0) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					closeWindow();
				}
			});
		}

	}

	protected void openWindow() {
		window = new JFrame();
		SwingRenderer customizationsToolsRenderer = swingCustomizer.getCustomizationTools().getToolsRenderer();
		JPanel form = customizationsToolsRenderer.createForm(this);
		customizationsToolsRenderer.setupWindow(window, form, null, customizationsToolsRenderer.getObjectTitle(this),
				customizationsToolsRenderer.getObjectIconImage(this));
		AlternativeWindowDecorationsPanel decorations = (AlternativeWindowDecorationsPanel) window.getContentPane();
		decorations.getCloseButton().setVisible(false);
		decorations.getMaximizeButton().setVisible(false);
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}

	protected void closeWindow() {
		window.dispose();
		window = null;
	}

	public void saveCustomizations() {
		File file = new File(
				swingCustomizer.getCustomizationTools().swingCustomizer.getInfoCustomizationsOutputFilePath());
		try {
			swingCustomizer.getCustomizationTools().swingCustomizer.getInfoCustomizations().saveToFile(file);
		} catch (IOException e) {
			throw new ReflectionUIError(e);
		}
	}

	public CustomizationOptions getOptions() {
		return swingCustomizer.getCustomizationOptions();
	}

	public InfoCustomizations getAllCustomizations() {
		return swingCustomizer.getInfoCustomizations();
	}

}
