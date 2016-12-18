package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.lang.reflect.Constructor;

import xy.reflect.ui.control.data.IControlData;
import xy.reflect.ui.util.ReflectionUIError;

public class ControlBuilder {

	protected SwingRenderer swingRenderer;
	protected Class<? extends Component> controlClass;
	protected IControlData data;

	public ControlBuilder(SwingRenderer swingRenderer, Class<? extends Component> controlClass, IControlData data) {
		super();
		this.swingRenderer = swingRenderer;
		this.controlClass = controlClass;
		this.data = data;
	}

	public SwingRenderer getSwingRenderer() {
		return swingRenderer;
	}

	public Class<? extends Component> getControlClass() {
		return controlClass;
	}

	public IControlData getData() {
		return data;
	}

	public Component build() {
		try {
			Constructor<? extends Component> ctor = controlClass.getConstructor(SwingRenderer.class,
					IControlData.class);
			return ctor.newInstance(swingRenderer, data);
		} catch (Throwable e) {
			throw new ReflectionUIError(e);
		}
	}

}
