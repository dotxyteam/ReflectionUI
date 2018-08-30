package xy.reflect.ui.info.menu;

import xy.reflect.ui.control.DefaultMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.swing.MethodAction;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class MethodActionMenuItem extends AbstractActionMenuItem {

	protected IMethodInfo method;

	public MethodActionMenuItem(IMethodInfo method) {
		super(ReflectionUIUtils.formatMethodControlCaption(new DefaultMethodControlData(new Object(), method)),
				method.getIconImagePath());
		this.method = method;
	}

	public IMethodInfo getMethod() {
		return method;
	}

	public void setMethod(IMethodInfo method) {
		this.method = method;
	}

	@Override
	public void execute(Object genericForm, Object renderer) {
		SwingRenderer swingRenderer = (SwingRenderer) renderer;
		Form form = (Form) genericForm;
		IMethodControlInput input = form.createMethodControlPlaceHolder(method);
		MethodAction methodAction = swingRenderer.createMethodAction(input);
		methodAction.execute(form);
	}

	@Override
	public boolean isEnabled(Object object, Object renderer) {
		return true;
	}

	@Override
	public String getName(Object form, Object renderer) {
		return getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodActionMenuItem other = (MethodActionMenuItem) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ActionMenuItem [name=" + name + ", action=" + method + "]";
	}

}
