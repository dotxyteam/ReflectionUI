package xy.reflect.ui.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.IEnumerationTypeInfo;
import xy.reflect.ui.util.component.TabulatingLabel;

public class EnumerationControl extends JPanel implements
		ICanShowCaptionControl {
	protected static final long serialVersionUID = 1L;
	protected IEnumerationTypeInfo enumType;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;
	private JComboBox comboBox;

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
		if (field.isReadOnly()) {
			comboBox.setModel(new DefaultComboBoxModel(
					new Object[] { initialValue }));
		} else {
			comboBox.setModel(new DefaultComboBoxModel(enumType
					.getPossibleValues().toArray()));
		}

		comboBox.setBackground(UIManager.getColor("TextField.background"));

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
					String s = enumType.formatValue(value);
					label.setText( reflectionUI.translateUIString(s));
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
		comboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					Object selected = comboBox.getSelectedItem();
					field.setValue(object, selected);
				} catch (Throwable t) {
					reflectionUI.handleExceptionsFromDisplayedUI(
							EnumerationControl.this, t);
				}
			}
		});
	}

	@Override
	public void showCaption() {
		add(new TabulatingLabel(field.getCaption() + ": "), BorderLayout.WEST);
	}

}
