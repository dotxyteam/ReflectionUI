package xy.reflect.ui.control.swing.renderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.swing.JPanel;

import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.MethodControlDataProxy;
import xy.reflect.ui.control.swing.MethodControl;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.ITypeInfoProxyFactory;
import xy.reflect.ui.undo.AbstractModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;

public class MethodControlPlaceHolder extends JPanel implements IMethodControlInput {

	protected static final long serialVersionUID = 1L;

	protected final SwingRenderer swingRenderer;
	protected JPanel form;
	protected Component methodControl;
	protected IMethodInfo method;
	protected IMethodControlData controlData;

	public MethodControlPlaceHolder(SwingRenderer swingRenderer, JPanel form, IMethodInfo method) {
		super();
		this.swingRenderer = swingRenderer;
		this.form = form;
		this.method = method;
		setLayout(new BorderLayout());
		refreshUI(false);
	}

	public JPanel getForm() {
		return form;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (result == null) {
			return null;
		}
		int maxMethodControlWidth = 0;
		SortedMap<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory = swingRenderer
				.getMethodControlPlaceHoldersByCategoryByForm().get(form);
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

	protected int getIndentWidth() {
		return SwingRendererUtils.getStandardCharacterWidth(form) * 10;
	}

	public IMethodControlData makeMethodModificationsUndoable(final IMethodControlData data) {
		return new MethodControlDataProxy(data) {

			@Override
			public Object invoke(InvocationData invocationData) {
				return ReflectionUIUtils.invokeMethodThroughModificationStack(data, invocationData,
						getModificationStack(), getModificationsTarget());
			}

		};
	}

	public IMethodControlData indicateWhenBusy(final IMethodControlData data) {
		return new MethodControlDataProxy(data) {

			@Override
			public Object invoke(final InvocationData invocationData) {
				return SwingRendererUtils.showBusyDialogWhileInvokingMethod(MethodControlPlaceHolder.this,
						swingRenderer, data, invocationData);
			}

			@Override
			public Runnable getUndoJob(InvocationData invocationData) {
				final Runnable result = data.getUndoJob(invocationData);
				if (result == null) {
					return null;
				}
				return new Runnable() {
					@Override
					public void run() {
						MethodControlPlaceHolder.this.swingRenderer.showBusyDialogWhile(MethodControlPlaceHolder.this,
								new Runnable() {
									public void run() {
										result.run();
									}
								}, AbstractModification.getUndoTitle(
										ReflectionUIUtils.composeMessage(data.getCaption(), "Execution")));
					}
				};
			}

		};
	}

	public Component getMethodControl() {
		return methodControl;
	}

	public Object getObject() {
		return this.swingRenderer.getObjectByForm().get(form);
	}

	@Override
	public IMethodControlData getControlData() {
		return controlData;
	}

	@Override
	public IInfo getModificationsTarget() {
		return method;
	}

	@Override
	public ModificationStack getModificationStack() {
		return this.swingRenderer.getModificationStackByForm().get(form);
	}

	@Override
	public String getContextIdentifier() {
		ITypeInfo objectType = this.swingRenderer.reflectionUI
				.getTypeInfo(this.swingRenderer.reflectionUI.getTypeInfoSource(getObject()));
		return "MethodContext [methodSignature=" + method.getSignature() + ", containingType=" + objectType.getName()
				+ "]";
	}

	public IMethodInfo getMethod() {
		return method;
	}

	public Component createMethodControl() {
		Component result = this.swingRenderer.createCustomMethodControl(this);
		if (result != null) {
			return result;
		}
		return new MethodControl(this.swingRenderer, this);
	}

	public void refreshUI(boolean recreate) {
		if (recreate) {
			if (methodControl != null) {
				remove(methodControl);
				methodControl = null;
			}
		}
		if (methodControl == null) {
			controlData = getInitialControlData();
			methodControl = createMethodControl();
			add(methodControl, BorderLayout.CENTER);
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			remove(methodControl);
			methodControl = null;
			refreshUI(false);
		}
	}

	public IMethodControlData getInitialControlData() {
		IMethodInfo method = MethodControlPlaceHolder.this.method;
		final ITypeInfoProxyFactory typeSpecificities = method.getReturnValueTypeSpecificities();
		if (method.getReturnValueType() != null) {
			if (typeSpecificities != null) {
				method = new MethodInfoProxy(method) {
					@Override
					public ITypeInfo getReturnValueType() {
						return typeSpecificities.get(super.getReturnValueType());
					}
				};
			}
		}
		final IMethodInfo finalMethod = method;
		IMethodControlData result = new InitialMethodControlData(finalMethod);

		result = indicateWhenBusy(result);
		result = makeMethodModificationsUndoable(result);
		return result;
	}

	@Override
	public String toString() {
		return "MethodControlPlaceHolder [form=" + form + ", method=" + method + "]";
	}

	protected class InitialMethodControlData implements IMethodControlData {
		protected IMethodInfo finalMethod;

		public InitialMethodControlData(IMethodInfo finalMethod) {
			this.finalMethod = finalMethod;
		}

		@Override
		public String getConfirmationMessage(InvocationData invocationData) {
			return finalMethod.getConfirmationMessage(getObject(), invocationData);
		}

		@Override
		public boolean isReturnValueNullable() {
			return finalMethod.isReturnValueNullable();
		}

		@Override
		public boolean isReturnValueDetached() {
			return finalMethod.isReturnValueDetached();
		}

		public boolean isReturnValueIgnored() {
			return finalMethod.isReturnValueIgnored();
		}

		@Override
		public void validateParameters(InvocationData invocationData) throws Exception {
			finalMethod.validateParameters(getObject(), invocationData);
		}

		@Override
		public boolean isReadOnly() {
			return finalMethod.isReadOnly();
		}

		@Override
		public Object invoke(InvocationData invocationData) {
			return finalMethod.invoke(getObject(), invocationData);
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return finalMethod.getValueReturnMode();
		}

		@Override
		public Runnable getUndoJob(InvocationData invocationData) {
			return finalMethod.getUndoJob(getObject(), invocationData);
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return finalMethod.getReturnValueType();
		}

		@Override
		public List<IParameterInfo> getParameters() {
			return finalMethod.getParameters();
		}

		@Override
		public String getNullReturnValueLabel() {
			return finalMethod.getNullReturnValueLabel();
		}

		@Override
		public String getOnlineHelp() {
			return finalMethod.getOnlineHelp();
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return finalMethod.getSpecificProperties();
		}

		@Override
		public String getCaption() {
			return finalMethod.getCaption();
		}

		@Override
		public String getMethodSignature() {
			return finalMethod.getSignature();
		}

		@Override
		public ResourcePath getIconImagePath() {
			return finalMethod.getIconImagePath();
		}

		private Object getOuterType() {
			return MethodControlPlaceHolder.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((finalMethod == null) ? 0 : finalMethod.hashCode());
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
			InitialMethodControlData other = (InitialMethodControlData) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (finalMethod == null) {
				if (other.finalMethod != null)
					return false;
			} else if (!finalMethod.equals(other.finalMethod))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "InitialControlData [of=" + MethodControlPlaceHolder.this + "]";
		}

	};

}