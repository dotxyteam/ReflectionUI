package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.SwingRenderer;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.info.type.util.InfoCustomizations.SpecificFieldCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.SpecificMethodCustomization;
import xy.reflect.ui.info.type.util.InfoCustomizations.SpecificTypeCustomization;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class InfoCustomizationsControls {

	protected ReflectionUI infoCustomizationsUI = new ReflectionUI() {

		@Override
		protected SwingRenderer createSwingRenderer() {
			return new SwingRenderer(this) {
				@Override
				public boolean areInfoCustomizationsControlsEnabled() {
					return false;
				}
			};
		}
	};
	protected InfoCustomizations infoCustomizations;
	protected JFrame infoCustomizationsWindow;

	public InfoCustomizationsControls(InfoCustomizations infoCustomizations) {
		this.infoCustomizations = infoCustomizations;
		infoCustomizationsWindow = infoCustomizationsUI.getSwingRenderer().createObjectFrame(infoCustomizations,
				infoCustomizationsUI.getObjectTitle(infoCustomizations), getImageIcon().getImage());
		infoCustomizationsWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	public Component createSpecificTypeCustomizationsSettingsControl(final ReflectionUI reflectionUI,
			final ITypeInfo type) {
		final JButton result = new JButton(
				infoCustomizationsUI.prepareStringToDisplay("Type Customizations: " + type + "..."), getImageIcon());
		result.setContentAreaFilled(false);
		result.setFocusable(false);
		final SpecificTypeCustomization t = infoCustomizations.getSpecificTypeCustomization(type, true);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (infoCustomizationsUI.getSwingRenderer().openObjectDialogAndGetConfirmation(result, t,
						infoCustomizationsUI.getObjectTitle(t), getImageIcon().getImage(), true)) {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							update(type, reflectionUI);
						}
					});
				}
			}
		});
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				infoCustomizationsWindow.setVisible(true);
			}
		});
		return result;
	}

	public Component createFieldInfoCustomizationsControl(final ReflectionUI reflectionUI, final ITypeInfo type,
			final IFieldInfo field) {
		final JButton result = new JButton(getImageIcon());
		result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
		result.setContentAreaFilled(false);
		result.setFocusable(false);
		SwingRendererUtils.setMultilineToolTipText(result,
				infoCustomizationsUI.prepareStringToDisplay("Customize this field display"));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				IFieldInfo nonCustomizedField = ReflectionUIUtils.findInfoByName(type.getFields(), field.getName());
				SpecificFieldCustomization fc = infoCustomizations.getSpecificFieldCustomization(type,
						nonCustomizedField, true);
				if (infoCustomizationsUI.getSwingRenderer().openObjectDialogAndGetConfirmation(result, fc,
						infoCustomizationsUI.getObjectTitle(fc), getImageIcon().getImage(), true)) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							update(type, reflectionUI);
						}
					});
				}
			}
		});
		return result;
	}
	
	
	public Component createMethodInfoCustomizationsControl(final ReflectionUI reflectionUI, final ITypeInfo type,
			final IMethodInfo method) {
		final JButton result = new JButton(getImageIcon());
		result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().height));
		result.setContentAreaFilled(false);
		result.setFocusable(false);
		SwingRendererUtils.setMultilineToolTipText(result,
				infoCustomizationsUI.prepareStringToDisplay("Customize this method display"));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				IMethodInfo nonCustomizedMethod = ReflectionUIUtils.findInfoByName(type.getMethods(), method.getName());
				SpecificMethodCustomization mc = infoCustomizations.getSpecificMethodCustomization(type,
						nonCustomizedMethod, true);
				if (infoCustomizationsUI.getSwingRenderer().openObjectDialogAndGetConfirmation(result, mc,
						infoCustomizationsUI.getObjectTitle(mc), getImageIcon().getImage(), true)) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							update(type, reflectionUI);
						}
					});
				}
			}
		});
		return result;
	}

	protected ImageIcon getImageIcon() {
		return SwingRendererUtils.CUSTOM_ICON;
	}

	protected void update(ITypeInfo type, ReflectionUI reflectionUI) {
		for (Object object : ReflectionUIUtils.getKnownInstances(type, reflectionUI)) {
			for (JPanel form : reflectionUI.getSwingRenderer().getForms(object)) {
				reflectionUI.getSwingRenderer().recreateFormContent(form);
			}
		}
		SpecificTypeCustomization t = infoCustomizations.getSpecificTypeCustomization(type);
		for (JPanel form : infoCustomizationsUI.getSwingRenderer().getForms(t)) {
			infoCustomizationsUI.getSwingRenderer().refreshAllFieldControls(form, false);
		}
		for (JPanel form : infoCustomizationsUI.getSwingRenderer().getForms(infoCustomizations)) {
			infoCustomizationsUI.getSwingRenderer().refreshAllFieldControls(form, false);
		}
	}

}
