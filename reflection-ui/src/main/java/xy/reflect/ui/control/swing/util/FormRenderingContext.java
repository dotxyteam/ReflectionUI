package xy.reflect.ui.control.swing.util;

import xy.reflect.ui.control.RenderingContext;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.info.type.ITypeInfo;

public class FormRenderingContext extends RenderingContext {

	protected Form form;

	public FormRenderingContext(Form form) {
		super(form.getSwingRenderer().getReflectionUI().getRenderingContextThreadLocal().get());
		this.form = form;
	}

	public Form getForm() {
		return form;
	}

	@Override
	protected Object findObjectLocally(ITypeInfo type) {
		Form resultForm = SwingRendererUtils.findCurrentFormOfType(type, form, form.getSwingRenderer());
		if (resultForm == null) {
			return null;
		}
		return resultForm.getObject();
	}

}
