package xy.reflect.ui.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.IEnumerationTypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class EnumerationControl extends JPanel {
	protected static final long serialVersionUID = 1L;
	protected IEnumerationTypeInfo enumType;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;
	protected JComboBox comboBox;

	@SuppressWarnings({})
	public EnumerationControl(final ReflectionUI reflectionUI,
			final Object object, final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		this.enumType = (IEnumerationTypeInfo) field.getType();

		initialize();
	}

	protected void initialize() {
		setLayout(new BorderLayout());

		comboBox = new JComboBox();
		add(comboBox, BorderLayout.CENTER);

		Object initialValue = field.getValue(object);
		comboBox.setModel(new DefaultComboBoxModel(enumType.getPossibleValues()
				.toArray()));
		if (field.isReadOnly()) {
			comboBox.setEnabled(false);
		} else {
			comboBox.setBackground(ReflectionUIUtils
					.fixSeveralColorRenderingIssues(ReflectionUIUtils
							.getTextBackgroundColor()));
		}

		comboBox.setRenderer(new BasicComboBoxRenderer() {

			protected static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(
						list, value, index, isSelected, cellHasFocus);
				if (value == null) {
					label.setText("");
				} else {
					String s = enumType.formatEnumerationItem(value);
					label.setText(reflectionUI.prepareUIString(s));
				}

				Image imageIcon = reflectionUI.getObjectIconImage(value);
				if (imageIcon == null) {
					label.setIcon(null);
				} else {
					label.setIcon(new ImageIcon(imageIcon));
				}

				return label;
			}
		});
		comboBox.setSelectedItem(initialValue);
		comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					try {
						Object selected = e.getItem();
						field.setValue(object, selected);
					} catch (Throwable t) {
						reflectionUI.handleExceptionsFromDisplayedUI(
								EnumerationControl.this, t);
					}
				}
			}
		});
	}
}
