


package xy.reflect.ui.control.swing.renderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.SortedMap;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.control.AbstractMethodControlData;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.MethodContext;
import xy.reflect.ui.control.swing.MethodControl;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.ModificationStack;

/**
 * Instances of this class are method control containers.
 * 
 * They provide common method control features as undo management, busy
 * indication, etc.
 * 
 * They also generate the input that will be used by the
 * {@link #createMethodControl()} method and passed to the control constructor.
 * 
 * @author olitank
 *
 */
public class MethodControlPlaceHolder extends ControlPanel implements IMethodControlInput {

	protected static final long serialVersionUID = 1L;

	protected final SwingRenderer swingRenderer;
	protected Form form;
	protected Component methodControl;
	protected IMethodInfo method;
	protected IMethodControlData controlData;

	public MethodControlPlaceHolder(Form form, IMethodInfo method) {
		super();
		this.swingRenderer = form.getSwingRenderer();
		this.form = form;
		this.method = method;
		this.controlData = createControlData();
		setName("methodControlPlaceHolder [method=" + method.getName() + ", parent=" + form.getName() + "]");
		setLayout(new BorderLayout());
		manageVisibiltyChanges();
	}

	public void initializeUI() {
		refreshUI(true);
	};

	protected void manageVisibiltyChanges() {
		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				MethodControlPlaceHolder.this.swingRenderer.showBusyDialogWhile(MethodControlPlaceHolder.this,
						new Runnable() {
							@Override
							public void run() {
								MethodControlPlaceHolder.this.method.onControlVisibilityChange(getObject(), true);
							}
						}, MethodControlPlaceHolder.this.swingRenderer.getObjectTitle(getObject()) + " - "
								+ MethodControlPlaceHolder.this.method.getCaption() + " - Setting up...");
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				MethodControlPlaceHolder.this.swingRenderer.showBusyDialogWhile(MethodControlPlaceHolder.this,
						new Runnable() {
							@Override
							public void run() {
								MethodControlPlaceHolder.this.method.onControlVisibilityChange(getObject(), false);
							}
						}, MethodControlPlaceHolder.this.swingRenderer.getObjectTitle(getObject()) + " - "
								+ MethodControlPlaceHolder.this.method.getCaption() + " - Cleaning up...");
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

		});
	}

	public Form getForm() {
		return form;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (result == null) {
			return null;
		}
		int maxMethodControlWidth = 0;
		SortedMap<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory = form
				.getMethodControlPlaceHoldersByCategory();
		for (InfoCategory category : methodControlPlaceHoldersByCategory.keySet()) {
			for (MethodControlPlaceHolder methodControlPlaceHolder : methodControlPlaceHoldersByCategory
					.get(category)) {
				Component methodControl = methodControlPlaceHolder.getMethodControl();
				maxMethodControlWidth = Math.max(maxMethodControlWidth, methodControl.getPreferredSize().width);
			}
		}
		maxMethodControlWidth = maxMethodControlWidth - (maxMethodControlWidth % getIndentWidth()) + getIndentWidth();
		result.width = maxMethodControlWidth;
		return result;
	}

	public int getIndentWidth() {
		return SwingRendererUtils.getStandardCharacterWidth(form) * 10;
	}

	public Component getMethodControl() {
		return methodControl;
	}

	public Object getObject() {
		return form.getObject();
	}

	@Override
	public IMethodControlData getControlData() {
		return controlData;
	}

	@Override
	public ModificationStack getModificationStack() {
		return form.getModificationStack();
	}

	@Override
	public IContext getContext() {
		ITypeInfo objectType = this.swingRenderer.reflectionUI
				.buildTypeInfo(this.swingRenderer.reflectionUI.getTypeInfoSource(getObject()));
		return new MethodContext(objectType, method);
	}

	public IMethodInfo getMethod() {
		return method;
	}

	public Component createMethodControl() {
		return new MethodControl(this.swingRenderer, this);
	}

	public void refreshUI(boolean refreshStructure) {
		if (refreshStructure && (methodControl != null)) {
			remove(methodControl);
			methodControl = null;
		}
		if (methodControl == null) {
			methodControl = createMethodControl();
			methodControl.setName("methodControl [method=" + method.getName() + ", parent=" + form.getName() + "]");
			add(methodControl, BorderLayout.CENTER);
			SwingRendererUtils.handleComponentSizeChange(this);
		}
		methodControl.setEnabled(controlData.isEnabled());
	}

	public IMethodControlData createControlData() {
		IMethodControlData result = new MethodControlData(method);
		return result;
	}

	@Override
	public String toString() {
		return "MethodControlPlaceHolder [method=" + method + ", form=" + form + "]";
	}

	protected class MethodControlData extends AbstractMethodControlData {

		protected IMethodInfo finalMethod;

		public MethodControlData(IMethodInfo finalMethod) {
			super(swingRenderer.getReflectionUI());
			this.finalMethod = finalMethod;
		}

		@Override
		public Object getObject() {
			return form.getObject();
		}

		@Override
		protected IMethodInfo getMethod() {
			return finalMethod;
		}

		private Object getEnclosingInstance() {
			return MethodControlPlaceHolder.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + super.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MethodControlData other = (MethodControlData) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (!super.equals(other))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "InitialControlData [of=" + MethodControlPlaceHolder.this + ", finalMethod=" + getMethod() + "]";
		}

	}

}
