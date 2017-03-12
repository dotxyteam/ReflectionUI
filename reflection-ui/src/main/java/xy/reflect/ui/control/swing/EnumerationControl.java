package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import xy.reflect.ui.control.input.IControlData;
import xy.reflect.ui.control.input.IControlInput;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.util.SwingRendererUtils;

@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
public class EnumerationControl extends JPanel {
	protected static final long serialVersionUID = 1L;
	protected IEnumerationTypeInfo enumType;
	protected SwingRenderer swingRenderer;
	protected IControlData data;
	protected JComboBox comboBox;

	@SuppressWarnings({})
	public EnumerationControl(final SwingRenderer swingRenderer, IControlInput input) {
		this.swingRenderer = swingRenderer;
		this.data = input.getControlData();
		this.enumType = (IEnumerationTypeInfo) data.getType();

		initialize();
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(""));

		comboBox = new JComboBox();
		add(comboBox, BorderLayout.CENTER);

		Object initialValue = data.getValue();
		List<Object> possibleValues = new ArrayList<Object>(Arrays.asList(enumType.getPossibleValues()));
		if (data.isNullable()) {
			possibleValues.add(0, null);
		}
		comboBox.setModel(new DefaultComboBoxModel(possibleValues.toArray()));
		if (data.isGetOnly()) {
			comboBox.setEnabled(false);
		} else {
			comboBox.setBackground(SwingRendererUtils.getTextBackgroundColor());
		}

		comboBox.setRenderer(new BasicComboBoxRenderer() {

			protected static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				if (value == null) {
					String nullValueLabel = data.getNullValueLabel();
					if (nullValueLabel == null) {
						label.setText("");
					} else {
						label.setText(nullValueLabel);
					}
					label.setIcon(null);
				} else {
					IEnumerationItemInfo itemInfo = enumType.getValueInfo(value);
					String s;
					if (itemInfo == null) {
						s = "";
					} else {
						s = itemInfo.getCaption();
					}
					label.setText(swingRenderer.prepareStringToDisplay(s));
					Image iconImage = SwingRendererUtils.getCachedIconImage(swingRenderer,
							itemInfo.getSpecificProperties());
					if (iconImage == null) {
						label.setIcon(null);
					} else {
						label.setIcon(SwingRendererUtils.getSmallIcon(iconImage));
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
		comboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Object selected = comboBox.getSelectedItem();
					data.setValue(selected);
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(EnumerationControl.this, t);
				}
			}
		});
	}
}
