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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import xy.reflect.ui.control.input.IFieldControlData;
import xy.reflect.ui.control.input.IFieldControlInput;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
public class EnumerationControl extends JPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;

	protected static final String INVALID_VALUE_SUFFIX = "... (Invalid value. Please select another value)";

	protected IEnumerationTypeInfo enumType;
	protected List<Object> possibleValues;
	protected SwingRenderer swingRenderer;
	protected IFieldControlInput input;
	protected IFieldControlData data;
	protected JComboBox comboBox;
	protected boolean listenerDisabled = false;
	protected Throwable error;

	@SuppressWarnings({})
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
		if (data.isNullable()) {
			result.add(0, null);
		}
		return result;
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(""));
		comboBox = new JComboBox();
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
					if (error != null) {
						if (selected == error) {
							throw error;
						}
						try {
							data.setValue(selected);
						} finally {
							input.getModificationStack().invalidate();
						}
					} else {
						ReflectionUIUtils.setValueThroughModificationStack(data, selected, input.getModificationStack(),
								input.getModificationsTarget());
					}
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
			comboBox.setBackground(SwingRendererUtils.getTextBackgroundColor());
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
		} else if (value == error) {
			return ReflectionUIUtils.truncateNicely(ReflectionUIUtils.getPrettyErrorMessage(error), 50);
		} else {
			IEnumerationItemInfo itemInfo = enumType.getValueInfo(value);
			String s;
			if (itemInfo == null) {
				s = "";
			} else {
				s = itemInfo.getCaption();
			}
			if (!possibleValues.contains(value)) {
				s += INVALID_VALUE_SUFFIX;
			}
			return s;
		}

	}

	protected Icon getValueIcon(Object value) {
		if (value == null) {
			return null;
		} else if (value == error) {
			return null;
		} else {
			IEnumerationItemInfo itemInfo = enumType.getValueInfo(value);
			String s;
			if (itemInfo == null) {
				s = "";
			} else {
				s = itemInfo.getCaption();
			}
			Image iconImage = SwingRendererUtils.findIconImage(swingRenderer, itemInfo.getSpecificProperties());
			if (iconImage == null) {
				return null;
			} else {
				return SwingRendererUtils.getSmallIcon(iconImage);
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
		if(enumType.isDynamicEnumeration()){
			possibleValues =  collectPossibleValues();
		}
		List<Object> extendedPossibleValues = new ArrayList<Object>(possibleValues);
		Object currentValue;
		try {
			currentValue = data.getValue();
			error = null;
			if (!possibleValues.contains(currentValue)) {
				extendedPossibleValues.add(currentValue);
			}
		} catch (final Throwable t) {
			currentValue = error = t;
			extendedPossibleValues.add(error);
		}
		comboBox.setModel(new DefaultComboBoxModel(extendedPossibleValues.toArray()));
		listenerDisabled = true;
		try {
			comboBox.setSelectedItem(currentValue);
		} finally {
			listenerDisabled = false;
		}
		final Object finalCurrentValue = currentValue;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (error != null) {
					swingRenderer.getReflectionUI().logError(error);
					displayError(ReflectionUIUtils.getPrettyErrorMessage(error));
				} else if (!possibleValues.contains(finalCurrentValue)) {
					StringBuilder invalidValueMessage = new StringBuilder();
					{
						invalidValueMessage.append("Invalid enumeration value found:" + "\n- Enumeration Type: "
								+ enumType + "\n- Value: " + "\n\t. '" + finalCurrentValue
								+ "'\n- Expected Valid Values (" + possibleValues.size() + " item(s)):");
						for (Object value : possibleValues) {
							invalidValueMessage.append("\n\t. " + ((value == null) ? "<null>" : ("'" + value + "'")));
						}
					}
					swingRenderer.getReflectionUI().logError(new ReflectionUIError(invalidValueMessage.toString()));
					displayError("");
				} else {
					displayError(null);
				}
			}

		});
		return true;
	}

	@Override
	public boolean handlesModificationStackUpdate() {
		return true;
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
	public String toString() {
		return "EnumerationControl [data=" + data + "]";
	}

}
