package xy.reflect.ui.control;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.UIManager;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.IEnumerationTypeInfo;

public class EnumerationControl extends JComboBox {
	protected static final long serialVersionUID = 1L;
	protected IEnumerationTypeInfo enumType;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;

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
		Object initialValue = field.getValue(object);
		if (field.isReadOnly()) {
			setModel(new DefaultComboBoxModel(new Object[] { initialValue }));
		} else {
			setModel(new DefaultComboBoxModel(enumType.getPossibleValues()
					.toArray()));
		}

		setBackground(UIManager.getColor("TextField.background"));

		setRenderer(new DefaultListCellRenderer() {

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
					String s = reflectionUI.toString(value);
					s = reflectionUI.translateUIString(s);
					label.setText(s);
				}
				label.setOpaque(true);
				label.setBackground(UIManager.getColor("TextField.background"));
				Image imageIcon = reflectionUI.getObjectIconImage(value);
				if (imageIcon == null) {
					label.setIcon(null);
				} else {
					label.setIcon(new ImageIcon(imageIcon));
				}

				return label;
			}
		});
		setSelectedItem(initialValue);
		addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					Object selected = getSelectedItem();
					field.setValue(object, selected);
				} catch (Throwable t) {
					reflectionUI.handleDisplayedUIExceptions(
							EnumerationControl.this, t);
				}
			}
		});
	}

}
