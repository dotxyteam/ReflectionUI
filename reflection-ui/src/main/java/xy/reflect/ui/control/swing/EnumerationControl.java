package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.util.SwingRendererUtils;

public class EnumerationControl extends JPanel {
	protected static final long serialVersionUID = 1L;
	protected IEnumerationTypeInfo enumType;
	protected SwingRenderer swingRenderer;
	protected Object object;
	protected IFieldInfo field;
	protected JComboBox comboBox;

	@SuppressWarnings({})
	public EnumerationControl(final SwingRenderer swingRenderer, final Object object, final IFieldInfo field) {
		this.swingRenderer = swingRenderer;
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
		List<Object> possibleValues = new ArrayList<Object>(Arrays.asList(enumType.getPossibleValues()));
		if (field.isNullable()) {
			possibleValues.add(0, null);
		}
		comboBox.setModel(new DefaultComboBoxModel(possibleValues.toArray()));
		if (field.isGetOnly()) {
			comboBox.setEnabled(false);
		} else {
			comboBox.setBackground(
					SwingRendererUtils.fixSeveralColorRenderingIssues(SwingRendererUtils.getTextBackgroundColor()));
		}

		comboBox.setRenderer(new BasicComboBoxRenderer() {

			protected static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				if (value == null) {
					label.setText("");
					label.setIcon(null);
				} else {
					IEnumerationItemInfo itemInfo = enumType.getValueInfo(value);
					String s;
					if (itemInfo == null) {
						s = "";
					} else {
						s = itemInfo.getCaption();
					}
					label.setText(swingRenderer.getReflectionUI().prepareStringToDisplay(s));
					Image imageIcon = swingRenderer.getObjectIconImage(value);
					if (imageIcon == null) {
						label.setIcon(null);
					} else {
						label.setIcon(new ImageIcon(imageIcon));
					}
				}

				return label;
			}
		});
		if (possibleValues.contains(initialValue)) {
			comboBox.setSelectedItem(initialValue);
		} else {
			comboBox.setSelectedIndex(-1);
		}
		comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					try {
						Object selected = e.getItem();
						field.setValue(object, selected);
					} catch (Throwable t) {
						swingRenderer.handleExceptionsFromDisplayedUI(EnumerationControl.this, t);
					}
				}
			}
		});
	}
}
