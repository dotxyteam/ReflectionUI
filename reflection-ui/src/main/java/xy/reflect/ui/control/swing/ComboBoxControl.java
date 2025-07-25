
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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control that displays a combo box. Compatible with
 * {@link IEnumerationTypeInfo}.
 * 
 * @author olitank
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ComboBoxControl extends ControlPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;

	protected static final String INVALID_VALUE_SUFFIX = "...";

	protected IEnumerationTypeInfo enumType;
	protected List<Object> possibleValues;
	protected SwingRenderer swingRenderer;
	protected IFieldControlInput input;
	protected IFieldControlData data;
	protected JComboBox comboBox;
	protected boolean listenerDisabled = false;

	public ComboBoxControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		this.enumType = (IEnumerationTypeInfo) data.getType();
		initialize();
	}

	protected List<Object> collectPossibleValues() {
		List<Object> result = new ArrayList<Object>(Arrays.asList(enumType.getValues()));
		if (data.isNullValueDistinct()) {
			result.add(0, null);
		}
		return result;
	}

	protected void initialize() {
		setLayout(new BorderLayout());
		comboBox = new JComboBox();
		add(comboBox, BorderLayout.CENTER);
		comboBox.setRenderer(createRenderer());
		comboBox.addActionListener(createActionListener());
		refreshUI(true);

	}

	protected ActionListener createActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (listenerDisabled) {
					return;
				}
				try {
					Object selected = comboBox.getSelectedItem();
					data.setValue(selected);
				} catch (Throwable t) {
					swingRenderer.handleException(ComboBoxControl.this, t);
				}
			}
		};
	}

	protected ListCellRenderer createRenderer() {
		return new BasicComboBoxRenderer() {

			protected static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				String text = getValueText(value);
				if ((text == null) || (text.length() == 0)) {
					label.setText("");
					label.setToolTipText(null);
				} else {
					label.setText(text.replaceAll(MiscUtils.getNewLineRegex(), " "));
					label.setToolTipText(SwingRendererUtils.adaptToolTipTextToMultiline(text));
				}
				label.setIcon(getValueIcon(value));
				if ((value != null) && !possibleValues.contains(value)) {
					label.setBorder(swingRenderer.getErrorBorder());
				} else {
					label.setBorder(null);
				}
				return label;
			}
		};
	}

	protected String getValueText(Object value) {
		if (value == null) {
			String nullValueLabel = data.getNullValueLabel();
			if (nullValueLabel == null) {
				return "";
			} else {
				return enumType.isDynamicEnumeration() ? nullValueLabel
						: swingRenderer.prepareMessageToDisplay(nullValueLabel);
			}
		} else {
			IEnumerationItemInfo itemInfo = enumType.getValueInfo(value);
			String s;
			if (itemInfo == null) {
				s = "";
			} else {
				s = enumType.isDynamicEnumeration() ? itemInfo.getCaption()
						: swingRenderer.prepareMessageToDisplay(itemInfo.getCaption());
			}
			return s;
		}
	}

	protected String getValueTooltipText(Object value) {
		if (value != null) {
			IEnumerationItemInfo itemInfo = enumType.getValueInfo(value);
			if (itemInfo != null) {
				String onlineHelp = itemInfo.getOnlineHelp();
				if (onlineHelp != null) {
					return swingRenderer.prepareMessageToDisplay(onlineHelp);
				}
			}
		}
		return getValueText(value);
	}

	protected Icon getValueIcon(Object value) {
		if (value == null) {
			return null;
		} else {
			IEnumerationItemInfo itemInfo = enumType.getValueInfo(value);
			if (itemInfo == null) {
				return null;
			} else {
				Image image = swingRenderer.getEnumerationItemIconImage(itemInfo);
				if (image == null) {
					return null;
				}
				return SwingRendererUtils.getIcon(image);
			}
		}
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		if (enumType.isDynamicEnumeration() || refreshStructure) {
			possibleValues = collectPossibleValues();
		}
		List<Object> extendedPossibleValues = new ArrayList<Object>(possibleValues);
		Object currentValue = data.getValue();
		if (!possibleValues.contains(currentValue)) {
			extendedPossibleValues.add(currentValue);
		}
		comboBox.setModel(new DefaultComboBoxModel(extendedPossibleValues.toArray()));
		listenerDisabled = true;
		try {
			comboBox.setSelectedItem(currentValue);
			String tooltipText = getValueTooltipText(currentValue);
			if ((tooltipText == null) || (tooltipText.length() == 0)) {
				comboBox.setToolTipText(null);
			} else {
				comboBox.setToolTipText(SwingRendererUtils.adaptToolTipTextToMultiline(tooltipText));
			}
		} finally {
			listenerDisabled = false;
		}
		if (refreshStructure) {
			((JComponent) comboBox.getEditor().getEditorComponent()).setBorder(BorderFactory.createEmptyBorder());
			if (data.getBorderColor() != null) {
				comboBox.setBorder(BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
			} else {
				comboBox.setBorder(new JComboBox().getBorder());
			}
			if (data.isGetOnly()) {
				comboBox.setEnabled(false);
				comboBox.setBackground(new JComboBox().getBackground());
				comboBox.setForeground(new JComboBox().getForeground());
			} else {
				comboBox.setEnabled(true);
				if (data.getEditorBackgroundColor() != null) {
					comboBox.setBackground(SwingRendererUtils.getColor(data.getEditorBackgroundColor()));
				} else {
					comboBox.setBackground(new JComboBox().getBackground());
				}
				if (data.getEditorForegroundColor() != null) {
					comboBox.setForeground(SwingRendererUtils.getColor(data.getEditorForegroundColor()));
				} else {
					comboBox.setForeground(new JComboBox().getForeground());
				}
			}
			if (data.getEditorCustomFontResourcePath() != null) {
				comboBox.setFont(SwingRendererUtils
						.loadFontThroughCache(data.getEditorCustomFontResourcePath(),
								ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()))
						.deriveFont(comboBox.getFont().getStyle(), comboBox.getFont().getSize()));
			} else {
				comboBox.setFont(new JComboBox().getFont());
			}
		}
		return true;
	}

	@Override
	public boolean isModificationStackManaged() {
		return false;
	}

	@Override
	public boolean areValueAccessErrorsManaged() {
		return false;
	}

	@Override
	public boolean displayError(Throwable error) {
		SwingRendererUtils.displayErrorOnBorderAndTooltip(this, comboBox, error, swingRenderer);
		return true;
	}

	@Override
	public boolean showsCaption() {
		return false;
	}

	@Override
	public boolean requestCustomFocus() {
		if (data.isGetOnly()) {
			return false;
		}
		return SwingRendererUtils.requestAnyComponentFocus(comboBox, swingRenderer);
	}

	@Override
	public void validateControlData(ValidationSession session) throws Exception {
	}

	@Override
	public void addMenuContributions(MenuModel menuModel) {
	}

}
