
package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control that displays a check box. Compatible with booleans.
 * 
 * @author olitank
 *
 */
public class CheckBoxControl extends JCheckBox implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;

	protected SwingRenderer swingRenderer;
	protected IFieldControlInput input;
	protected IFieldControlData data;

	public CheckBoxControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					data.setValue(isSelected());
				} catch (Throwable t) {
					swingRenderer.handleException(CheckBoxControl.this, t);
				}
			}
		});
		refreshUI(true);
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	@Override
	public boolean displayError(Throwable error) {
		return false;
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		if (refreshStructure) {
			setText(swingRenderer.prepareMessageToDisplay(data.getCaption()));
			setOpaque(false);
			setForeground(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
			if (data.getLabelCustomFontResourcePath() != null) {
				setFont(SwingRendererUtils
						.loadFontThroughCache(data.getLabelCustomFontResourcePath(),
								ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()))
						.deriveFont(getFont().getStyle(), getFont().getSize()));
			} else {
				setFont(new JCheckBox().getFont());
			}
			setEnabled(!data.isGetOnly());
			SwingRendererUtils.handleComponentSizeChange(this);
		}
		setSelected(Boolean.TRUE.equals(data.getValue()));
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
	public boolean requestCustomFocus() {
		if (data.isGetOnly()) {
			return false;
		}
		for (Component c : getComponents()) {
			if (SwingRendererUtils.requestAnyComponentFocus(c, swingRenderer)) {
				return true;
			}
		}
		return false;

	}

	@Override
	public void validateControlData(ValidationSession session) throws Exception {
	}

	@Override
	public void addMenuContributions(MenuModel menuModel) {
	}

}
