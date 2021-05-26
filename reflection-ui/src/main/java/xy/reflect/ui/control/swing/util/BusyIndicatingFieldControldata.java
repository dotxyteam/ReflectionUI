package xy.reflect.ui.control.swing.util;

import java.awt.Component;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;

public class BusyIndicatingFieldControldata extends FieldControlDataProxy {

	protected SwingRenderer swingRenderer;
	protected Component busyDialogOwner;

	public BusyIndicatingFieldControldata(IFieldControlData base, SwingRenderer swingRenderer,
			Component busyDialogOwner) {
		super(base);
		this.swingRenderer = swingRenderer;
		this.busyDialogOwner = busyDialogOwner;
	}

	@Override
	public Object getValue() {
		if (isBusyIndicationDisabled()) {
			return super.getValue();
		}
		return SwingRendererUtils.showBusyDialogWhileGettingFieldValue(busyDialogOwner, swingRenderer, base);
	}

	@Override
	public void setValue(final Object newValue) {
		if (isBusyIndicationDisabled()) {
			super.setValue(newValue);
			return;
		}
		SwingRendererUtils.showBusyDialogWhileSettingFieldValue(busyDialogOwner, swingRenderer, base, newValue);
	}

	protected boolean isBusyIndicationDisabled() {
		return false;
	}
}