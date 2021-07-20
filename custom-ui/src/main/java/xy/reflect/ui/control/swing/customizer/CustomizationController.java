
package xy.reflect.ui.control.swing.customizer;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.builder.StandardEditorBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This class allows to display a window that monitors and controls the WYSIWYG
 * UI customizations. Customizations can be undone/re-done, saved or listed via
 * this window.
 * 
 * @author olitank
 *
 */
public class CustomizationController {

	protected SwingCustomizer swingCustomizer;
	protected Set<Form> activeForms = new HashSet<Form>();
	protected JFrame window;
	protected Rectangle lastWindowBounds;
	protected StandardEditorBuilder windowBuilder;

	public CustomizationController(SwingCustomizer swingCustomizer) {
		this.swingCustomizer = swingCustomizer;
	}

	protected synchronized void formAdded(Form form) {
		if (activeForms.size() == 0) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					openWindow();
				}
			});
		}
		activeForms.add(form);
	}

	protected synchronized void formRemoved(Form customizingComponent) {
		activeForms.remove(customizingComponent);
		if (activeForms.size() == 0) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					closeWindow();
				}
			});
		}

	}

	protected void openWindow() {
		windowBuilder = createWindowBuilder();
		window = windowBuilder.createFrame();
		refreshCustomizedControlsOnModification();
		if (lastWindowBounds != null) {
			window.setBounds(lastWindowBounds);
		}
		SwingRenderer customizationsToolsRenderer = swingCustomizer.getCustomizationTools().getToolsRenderer();
		customizationsToolsRenderer.showFrame(window);
	}

	protected void closeWindow() {
		lastWindowBounds = window.getBounds();
		window.dispose();
		window = null;
		windowBuilder = null;
	}

	protected StandardEditorBuilder createWindowBuilder() {
		SwingRenderer customizationsToolsRenderer = swingCustomizer.getCustomizationTools().getToolsRenderer();
		return new StandardEditorBuilder(customizationsToolsRenderer, null, this);
	}

	protected void refreshCustomizedControlsOnModification() {
		getModificationStack().addListener(new AbstractSimpleModificationListener() {
			@Override
			protected void handleAnyEvent(IModification modification) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						SwingRendererUtils.refreshAllDisplayedFormsAndMenus(swingCustomizer, true);
					}
				});

			}
		});
	}

	public ModificationStack getModificationStack() {
		if (windowBuilder == null) {
			return null;
		}
		return windowBuilder.getModificationStack();
	}

	public String getInfoCustomizationsOutputFilePath() {
		return swingCustomizer.getInfoCustomizationsOutputFilePath();
	}

	public void saveCustomizations() {
		File file = new File(swingCustomizer.getInfoCustomizationsOutputFilePath());
		try {
			swingCustomizer.getInfoCustomizations().saveToFile(file,
					ReflectionUIUtils.getDebugLogListener(swingCustomizer.getReflectionUI()),
					"Generated with Custom UI (http://javacollection.net/reflectionui/)");
		} catch (IOException e) {
			throw new ReflectionUIError(e);
		}
	}

	public boolean isInEditMode() {
		return swingCustomizer.getCustomizationOptions().isInEditMode();
	}

	public void setInEditMode(boolean inEditMode) {
		swingCustomizer.getCustomizationOptions().setInEditMode(inEditMode);
	}

	public InfoCustomizations getAllCustomizations() {
		return swingCustomizer.getInfoCustomizations();
	}

}
