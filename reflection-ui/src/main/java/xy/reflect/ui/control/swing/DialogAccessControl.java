package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.SwingRendererUtils;

public class DialogAccessControl extends JPanel {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;

	protected Component statusControl;
	protected Component iconControl;
	protected Component button;

	public DialogAccessControl(final ReflectionUI reflectionUI, final Object object, final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		setLayout(new BorderLayout());

		statusControl = createStatusControl();
		button = createButton();
		iconControl = createIconControl();

		add(SwingRendererUtils.flowInLayout(button, FlowLayout.CENTER), BorderLayout.WEST);
		JPanel centerPanel = new JPanel();
		{
			add(centerPanel, BorderLayout.CENTER);
			centerPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			centerPanel.add(statusControl, c);
		}
		add(iconControl, BorderLayout.EAST);

		updateControls();
	}

	@Override
	public void requestFocus() {
		statusControl.requestFocus();
	}

	protected Component createIconControl() {
		return new JLabel() {

			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result != null) {
					if (statusControl != null) {
						Dimension textControlPreferredSize = statusControl.getPreferredSize();
						if (textControlPreferredSize != null) {
							result.height = textControlPreferredSize.height;
						}
					}
				}
				return result;
			}

		};
	}

	protected Component createButton() {
		final JButton result = new JButton("...") {

			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result != null) {
					result.width = result.height;
				}
				return result;
			}

		};
		result.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					openDialog();
				} catch (Throwable t) {
					reflectionUI.getSwingRenderer().handleExceptionsFromDisplayedUI(result, t);
				}
			}
		});
		return result;
	}

	protected Component createStatusControl() {
		return new TextControl(reflectionUI, object, new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {

			@Override
			public boolean isNullable() {
				return false;
			}

			@Override
			public Object getValue(Object object) {
				Object fieldValue = field.getValue(object);
				return reflectionUI.toString(fieldValue);
			}

			@Override
			public ITypeInfo getType() {
				return new TextualTypeInfo(reflectionUI, String.class);
			}

		});
	}

	protected void openDialog() {
		final Accessor<Object> valueAccessor = new Accessor<Object>() {

			@Override
			public Object get() {
				return field.getValue(object);
			}

			@Override
			public void set(Object t) {
				if (!field.isGetOnly()) {
					field.setValue(object, t);
				}
			}

		};
		ModificationStack parentStack = SwingRendererUtils.findModificationStack(DialogAccessControl.this,
				reflectionUI);
		String title = reflectionUI.getFieldTitle(object, field);
		Image iconImage = reflectionUI.getSwingRenderer().getIconImage(valueAccessor.get());
		boolean[] changeDetectedHolder = new boolean[] { false };
		boolean[] okPressedHolder = field.isGetOnly() ? null : new boolean[1];
		reflectionUI.getSwingRenderer().openObjectDialog(this, valueAccessor, title, iconImage, true, IInfoCollectionSettings.DEFAULT,
				parentStack, true, okPressedHolder, changeDetectedHolder, null);
		if (changeDetectedHolder[0]) {
			updateControls();
		}

	}

	protected void updateControls() {
		updateStatusControl();
		updateIconControl();
	}

	protected void updateIconControl() {
		Object fieldValue = field.getValue(object);
		Image iconImage = reflectionUI.getSwingRenderer().getIconImage(fieldValue);
		if (iconImage != null) {
			((JLabel) iconControl).setIcon(new ImageIcon(iconImage));
			iconControl.setVisible(true);
		} else {
			((JLabel) iconControl).setIcon(null);
			iconControl.setVisible(false);
		}
	}

	protected void updateStatusControl() {
		((TextControl) statusControl).refreshUI();
	}

}
