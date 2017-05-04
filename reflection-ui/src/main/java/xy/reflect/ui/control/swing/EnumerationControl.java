package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class EnumerationControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;

	protected static final String INVALID_VALUE_SUFFIX = "...";

	protected IEnumerationTypeInfo enumType;
	protected List<Object> possibleValues;
	protected SwingRenderer swingRenderer;
	protected IFieldControlInput input;
	protected IFieldControlData data;
	protected JComboBox comboBox;
	protected boolean listenerDisabled = false;

	public EnumerationControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		this.enumType = (IEnumerationTypeInfo) data.getType();
		this.possibleValues = collectPossibleValues();
		initialize();
	}

	protected List<Object> collectPossibleValues() {
		List<Object> result = new ArrayList<Object>(Arrays.asList(enumType.getPossibleValues()));
		if (data.isValueNullable()) {
			result.add(0, null);
		}
		return result;
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		comboBox = new JComboBox() {
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getMinimumSize() {
				Dimension result = super.getMinimumSize();
				if (result == null) {
					return null;
				}
				result.width = Math.max(20, result.width);
				return result;
			}

		};
		add(comboBox, BorderLayout.CENTER);
		comboBox.setRenderer(new BasicComboBoxRenderer() {

			protected static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				label.setText(swingRenderer.prepareStringToDisplay(getValueText(value)));
				label.setIcon(getValueIcon(value));
				if (!possibleValues.contains(value)) {
					SwingRendererUtils.setErrorBorder(label);
				} else {
					label.setBorder(null);
				}
				return label;
			}
		});
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (listenerDisabled) {
					return;
				}
				try {
					Object selected = comboBox.getSelectedItem();
					data.setValue(selected);
					refreshUI();
				} catch (Throwable t) {
					swingRenderer.handleExceptionsFromDisplayedUI(EnumerationControl.this, t);
				}
			}
		});
		if (data.isGetOnly())

		{
			comboBox.setEnabled(false);
		} else {
			comboBox.setBackground(SwingRendererUtils.getEditableTextBackgroundColor());
		}

		refreshUI();

	}

	protected String getValueText(Object value) {
		if (value == null) {
			String nullValueLabel = data.getNullValueLabel();
			if (nullValueLabel == null) {
				return "";
			} else {
				return nullValueLabel;
			}
		} else {
			IEnumerationItemInfo itemInfo = enumType.getValueInfo(value);
			String s;
			if (itemInfo == null) {
				s = "";
			} else {
				s = itemInfo.getCaption();
			}
			return s;
		}

	}

	protected Icon getValueIcon(Object value) {
		if (value == null) {
			return null;
		} else {
			IEnumerationItemInfo itemInfo = enumType.getValueInfo(value);
			ResourcePath imagePath = itemInfo.getIconImagePath();
			if (imagePath == null) {
				return null;
			} else {
				Image iconImage = SwingRendererUtils.loadImageThroughcache(imagePath,
						ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()));
				if (iconImage == null) {
					return null;
				} else {
					return SwingRendererUtils.getSmallIcon(iconImage);
				}
			}
		}
	}

	@Override
	public boolean displayError(String msg) {
		SwingRendererUtils.displayErrorOnBorderAndTooltip(this, comboBox, msg, swingRenderer);
		return true;
	}

	@Override
	public boolean showsCaption() {
		return false;
	}

	@Override
	public boolean refreshUI() {
		if (enumType.isDynamicEnumeration()) {
			possibleValues = collectPossibleValues();
		}
		List<Object> extendedPossibleValues = new ArrayList<Object>(possibleValues);
		Object currentValue;
		currentValue = data.getValue();
		if (!possibleValues.contains(currentValue)) {
			extendedPossibleValues.add(currentValue);
		}
		comboBox.setModel(new DefaultComboBoxModel(extendedPossibleValues.toArray()));
		listenerDisabled = true;
		try {
			comboBox.setSelectedItem(currentValue);
		} finally {
			listenerDisabled = false;
		}
		return true;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return false;
	}

	@Override
	public Object getFocusDetails() {
		return null;
	}

	@Override
	public boolean requestDetailedFocus(Object focusDetails) {
		return SwingRendererUtils.requestAnyComponentFocus(comboBox, null, swingRenderer);
	}

	@Override
	public void validateSubForm() throws Exception {
	}

	@Override
	public void addMenuContribution(MenuModel menuModel) {
	}

	@Override
	public String toString() {
		return "EnumerationControl [data=" + data + "]";
	}

}
