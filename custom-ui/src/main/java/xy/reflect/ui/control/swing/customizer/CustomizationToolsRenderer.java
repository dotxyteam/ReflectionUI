
package xy.reflect.ui.control.swing.customizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

import org.jdesktop.swingx.StackLayout;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.AlternativeWindowDecorationsPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.control.swing.util.WindowManager;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.custom.InfoCustomizations.AbstractCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.TextualStorage;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.MoreSystemProperties;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This is a sub-class of {@link SwingRenderer} that generates the customization
 * tools UIs.
 * 
 * @author olitank
 *
 */
public class CustomizationToolsRenderer extends SwingCustomizer {

	public CustomizationToolsRenderer(CustomizationToolsUI toolsUI) {
		super(toolsUI, MoreSystemProperties.getInfoCustomizationToolsCustomizationsFilePath());
	}

	public CustomizationToolsUI getCustomizationToolsUI() {
		return (CustomizationToolsUI) getCustomizedUI();
	}

	@Override
	public boolean isCustomizationsEditorEnabled() {
		return getCustomizationToolsUI().getSwingCustomizer().isMetaCustomizationAllowed();
	}

	@Override
	public boolean isMetaCustomizationAllowed() {
		return false;
	}

	@Override
	public CustomizationTools createCustomizationTools() {
		if (!getCustomizationToolsUI().getSwingCustomizer().isMetaCustomizationAllowed()) {
			return null;
		}
		return new CustomizationTools(this);
	}

	protected Color getToolsForegroundColor() {
		IApplicationInfo toolsAppInfo = getCustomizationToolsUI().getApplicationInfo();
		if (toolsAppInfo.getTitleForegroundColor() != null) {
			return SwingRendererUtils.getColor(toolsAppInfo.getTitleForegroundColor());
		}
		return new Color(0, 255, 255);
	}

	protected Color getToolsBackgroundColor() {
		IApplicationInfo toolsAppInfo = getCustomizationToolsUI().getApplicationInfo();
		if (toolsAppInfo.getTitleBackgroundColor() != null) {
			return SwingRendererUtils.getColor(toolsAppInfo.getTitleBackgroundColor());
		}
		return new Color(0, 0, 0);
	}

	@Override
	public WindowManager createWindowManager(Window window) {
		return new WindowManager(this, window) {

			@Override
			protected void layoutContentPane(Container contentPane) {
				alternativeDecorationsPanel = createAlternativeWindowDecorationsPanel(window, contentPane);
				rootPane.add(alternativeDecorationsPanel, StackLayout.TOP);
			}

			protected AlternativeWindowDecorationsPanel createAlternativeWindowDecorationsPanel(final Window window,
					final Component windowContent) {
				String title = SwingRendererUtils.getWindowTitle(window);
				Image iconImage = window.getIconImages().get(0);
				ImageIcon icon;
				if (SwingRendererUtils.isNullImage(iconImage)) {
					icon = null;
				} else {
					icon = SwingRendererUtils.getSmallIcon(new ImageIcon(iconImage));
				}
				return new CustomWindowDecorationsPanel(title, icon, window, windowContent) {

					private static final long serialVersionUID = 1L;

					@Override
					protected void configureWindow(Window window) {
						super.configureWindow(window);
						if (window instanceof JFrame) {
							getCloseButton().setVisible(false);
							getMaximizeButton().setVisible(false);
						}
					}

				};
			}

			@Override
			public void windowClosing(WindowEvent e) {
				if (window instanceof JFrame) {
					return;
				}
				super.windowClosing(e);
			}

		};
	}

	@Override
	public CustomizingForm createForm(Object object, IInfoFilter infoFilter) {
		/*
		 * Add a button to allow to change textual storage data field control.
		 */
		return new CustomizingForm(this, object, infoFilter) {

			private static final long serialVersionUID = 1L;

			@Override
			public CustomizingFieldControlPlaceHolder createFieldControlPlaceHolder(IFieldInfo field) {
				return new CustomizingFieldControlPlaceHolder(this, field) {

					private static final long serialVersionUID = 1L;
					Component textualStorageFieldControlPluginSelectionComponent;

					@Override
					public void refreshInfoCustomizationsControl() {
						if (form.getObject() instanceof TextualStorage) {
							if (textualStorageFieldControlPluginSelectionComponent == null) {
								textualStorageFieldControlPluginSelectionComponent = new CustomizationTools(
										(SwingCustomizer) swingRenderer) {

									@Override
									public void changeCustomizationFieldValue(AbstractCustomization customization,
											String fieldName, Object fieldValue) {
										ITypeInfo customizationType = ReflectionUI.getDefault().getTypeInfo(
												ReflectionUI.getDefault().getTypeInfoSource(customization));
										IFieldInfo customizationField = ReflectionUIUtils
												.findInfoByName(customizationType.getFields(), fieldName);
										customizationField.setValue(customization, fieldValue);
										form.refresh(true);
									}

									@Override
									protected JButton makeButton() {
										JButton result = new JButton(this.swingCustomizer.getCustomizationsIcon());
										result.setPreferredSize(new Dimension(result.getPreferredSize().height,
												result.getPreferredSize().height));
										result.setFocusable(false);
										return result;
									}
								}.makeButtonForTextualStorageDataField(this);
								add(textualStorageFieldControlPluginSelectionComponent, BorderLayout.EAST);
								SwingRendererUtils.handleComponentSizeChange(this);
							}
						}
						super.refreshInfoCustomizationsControl();
					}
				};
			}
		};
	}

}
