package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.SwingRenderer;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.info.type.util.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

@SuppressWarnings("unused")
public class InfoCustomizationsControls {

	protected ReflectionUI customizationsUI = new ReflectionUI() {

		@Override
		protected SwingRenderer createSwingRenderer() {
			return new SwingRenderer(this) {

				File customizationsFile;

				@Override
				protected boolean areInfoCustomizationsControlsEnabled() {
					return false;
				}

				@Override
				protected String getIInfoCustomizationsFilePath() {
					if (customizationsFile == null) {
						URL url = ReflectionUI.class.getResource("resource/info-customizations-types.icu");
						try {
							customizationsFile = ReflectionUIUtils.getStreamAsFile(url.openStream());
						} catch (IOException e) {
							throw new ReflectionUIError(e);
						}
					}
					return customizationsFile.getPath();
				}

			};
		}
	};
	protected InfoCustomizations infoCustomizations;
	private ReflectionUI reflectionUI;

	public InfoCustomizationsControls(ReflectionUI reflectionUI, InfoCustomizations infoCustomizations) {
		this.reflectionUI = reflectionUI;
		this.infoCustomizations = infoCustomizations;
	}

	public void openInfoCustomizationsWindow(InfoCustomizations infoCustomizations) {
		customizationsUI.getSwingRenderer().openObjectFrame(infoCustomizations,
				customizationsUI.getObjectTitle(infoCustomizations), getMainImageIcon().getImage());
	}

	public JButton createSaveControl(final File file) {
		final JButton result = new JButton(SwingRendererUtils.SAVE_ICON);
		result.setContentAreaFilled(false);
		result.setFocusable(false);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					infoCustomizations.saveToFile(file);
				} catch (IOException e1) {
					customizationsUI.getSwingRenderer().handleExceptionsFromDisplayedUI(result, e1);
				}
			}
		});
		return result;
	}

	public Component createTypeInfoCustomizer(final ITypeInfo type) {
		final JButton result = new JButton(customizationsUI.prepareStringToDisplay("Customizations..."),
				getMainImageIcon());
		result.setContentAreaFilled(false);
		result.setFocusable(false);
		final TypeCustomization t = infoCustomizations.getTypeCustomization(type, true);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (customizationsUI.getSwingRenderer().openObjectDialogAndGetConfirmation(result, t,
						customizationsUI.getObjectTitle(t), getMainImageIcon().getImage(), true)) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							update(type);
						}
					});
				}
			}
		});
		return result;
	}

	public Component createFieldInfoCustomizer(final ITypeInfo type, final IFieldInfo field) {
		final JButton result = new JButton(getMainImageIcon());
		result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
		result.setContentAreaFilled(false);
		result.setFocusable(false);
		SwingRendererUtils.setMultilineToolTipText(result,
				customizationsUI.prepareStringToDisplay("Customize this field display"));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popupMenu = new JPopupMenu();
				popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move Up")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveField(type, field, -1);
					}
				});
				popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move Down")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveField(type, field, 1);
					}
				});
				popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move To Top")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveField(type, field, Short.MIN_VALUE);
					}
				});
				popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move To Bottom")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveField(type, field, Short.MAX_VALUE);
					}
				});
				popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Other options...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openFieldCutomizationDialog(result, type, field);
					}
				});
				popupMenu.show(result, result.getWidth(), result.getHeight());
			}
		});
		return result;
	}

	protected void moveField(ITypeInfo type, IFieldInfo field, int offset) {
		TypeCustomization tc = infoCustomizations.getTypeCustomization(type, true);
		tc.moveField(infoCustomizations, type, field, offset);
		update(type);
	}

	protected void moveMethod(ITypeInfo type, IMethodInfo method, int offset) {
		TypeCustomization tc = infoCustomizations.getTypeCustomization(type, true);
		tc.moveMethod(infoCustomizations, type, method, offset);
		update(type);
	}

	protected void openFieldCutomizationDialog(Component activatorComponent, final ITypeInfo type, IFieldInfo field) {
		FieldCustomization fc = infoCustomizations.getFieldCustomization(type, field, true);
		if (customizationsUI.getSwingRenderer().openObjectDialogAndGetConfirmation(activatorComponent, fc,
				customizationsUI.getObjectTitle(fc), getMainImageIcon().getImage(), true)) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					update(type);
				}
			});
		}
	}

	protected void openMethodCutomizationDialog(Component activatorComponent, final ITypeInfo type,
			IMethodInfo method) {
		MethodCustomization mc = infoCustomizations.getMethodCustomization(type, method, true);
		if (customizationsUI.getSwingRenderer().openObjectDialogAndGetConfirmation(activatorComponent, mc,
				customizationsUI.getObjectTitle(mc), getMainImageIcon().getImage(), true)) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					update(type);
				}
			});
		}
	}

	public Component createMethodInfoCustomizer(final ITypeInfo type, final IMethodInfo method) {
		final JButton result = new JButton(getMainImageIcon());
		result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
		result.setContentAreaFilled(false);
		result.setFocusable(false);
		SwingRendererUtils.setMultilineToolTipText(result,
				customizationsUI.prepareStringToDisplay("Customize this method display"));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popupMenu = new JPopupMenu();
				popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move Left")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveMethod(type, method, -1);
					}
				});
				popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Move Right")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						moveMethod(type, method, 1);
					}
				});
				popupMenu.add(new AbstractAction(reflectionUI.prepareStringToDisplay("Other options...")) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						openMethodCutomizationDialog(result, type, method);
					}
				});
				popupMenu.show(result, result.getWidth(), result.getHeight());
			}
		});
		return result;
	}

	protected ImageIcon getMainImageIcon() {
		return SwingRendererUtils.CUSTOMIZATION_ICON;
	}

	protected void update(ITypeInfo type) {
		for (Object object : ReflectionUIUtils.getActiveInstances(type, reflectionUI)) {
			for (JPanel form : reflectionUI.getSwingRenderer().getForms(object)) {
				reflectionUI.getSwingRenderer().recreateFormContent(form);
			}
		}
		TypeCustomization t = infoCustomizations.getTypeCustomization(type);
		for (JPanel form : customizationsUI.getSwingRenderer().getForms(t)) {
			customizationsUI.getSwingRenderer().refreshAllFieldControls(form, false);
		}
		for (JPanel form : customizationsUI.getSwingRenderer().getForms(infoCustomizations)) {
			customizationsUI.getSwingRenderer().refreshAllFieldControls(form, false);
		}
	}
}
