package xy.reflect.ui.control.swing.plugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JCheckBox;

import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.control.swing.IAdvancedFieldControl;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.util.ClassUtils;

public class BooleanControlPlugin implements IFieldControlPlugin {

	protected static File lastDirectory = new File(".").getAbsoluteFile();

	@Override
	public boolean handles(IFieldControlInput input) {
		if (input.getControlData().isValueNullable()) {
			return false;
		}
		final Class<?> javaType;
		try {
			javaType = ClassUtils.getCachedClassforName(input.getControlData().getType().getName());
		} catch (ClassNotFoundException e) {
			return false;
		}
		if (!javaType.equals(boolean.class) && !javaType.equals(Boolean.class)) {
			return false;
		}
		return true;
	}

	@Override
	public Component createControl(Object renderer, IFieldControlInput input) {
		return new CheckBoxControl((SwingRenderer) renderer, input);
	}

	protected class CheckBoxControl extends JCheckBox implements IAdvancedFieldControl {

		protected static final long serialVersionUID = 1L;
		protected SwingRenderer swingRenderer;
		protected IFieldControlData data;

		public CheckBoxControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
			this.swingRenderer = swingRenderer;
			this.data = input.getControlData();

			if (data.isGetOnly()) {
				setEnabled(false);
			}

			setSelected((Boolean) data.getValue());
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					data.setValue(isSelected());
				}
			});
			setText(swingRenderer.prepareStringToDisplay(data.getCaption()));
		}

		@Override
		public boolean showsCaption() {
			return true;
		}

		@Override
		public boolean displayError(String msg) {
			return false;
		}

		@Override
		public boolean refreshUI() {
			return false;
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
			return false;
		}

		@Override
		public void validateSubForm() throws Exception {
		}

		@Override
		public String toString() {
			return "CheckBoxControl [data=" + data + "]";
		}

	}

}
