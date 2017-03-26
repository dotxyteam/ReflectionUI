package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import xy.reflect.ui.control.input.IMethodControlData;
import xy.reflect.ui.control.input.IMethodControlInput;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.util.MethodSetupObjectFactory;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.InvokeMethodModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIUtils;

public class MethodAction extends AbstractAction {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IMethodControlInput input;
	protected IMethodControlData data;
	protected Object returnValue;
	protected boolean shouldDisplayReturnValueIfAny = false;
	protected boolean retunValueWindowDetached;
	protected ModificationStack modificationStack;

	public MethodAction(SwingRenderer swingRenderer, IMethodControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		this.retunValueWindowDetached = data.getValueReturnMode() == ValueReturnMode.CALCULATED;
	}

	public SwingRenderer getSwingRenderer() {
		return swingRenderer;
	}

	public Object getReturnValue() {
		return returnValue;
	}

	public void setShouldDisplayReturnValueIfAny(boolean shouldDisplayReturnValueIfAny) {
		this.shouldDisplayReturnValueIfAny = shouldDisplayReturnValueIfAny;
	}

	public boolean getShouldDisplayReturnValueIfAny() {
		return shouldDisplayReturnValueIfAny;
	}

	public boolean isRetunValueWindowDetached() {
		return retunValueWindowDetached;
	}

	public void setRetunValueWindowDetached(boolean retunValueWindowDetached) {
		this.retunValueWindowDetached = retunValueWindowDetached;
	}

	public ModificationStack getModificationStack() {
		return modificationStack;
	}

	public void setModificationStack(ModificationStack modificationStack) {
		this.modificationStack = modificationStack;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Component activatorComponent = (Component) e.getSource();
		try {
			execute(activatorComponent);
		} catch (Throwable t) {
			swingRenderer.handleExceptionsFromDisplayedUI(activatorComponent, t);
		}

	}

	public void execute(Component activatorComponent) {
		onMethodInvocationRequest(activatorComponent);
	}

	protected boolean onMethodInvocationRequest(Component activatorComponent) {
		if (data.getParameters().size() > 0) {
			return openMethoExecutionSettingDialog(activatorComponent);
		} else {
			final boolean displayReturnValue = shouldDisplayReturnValueIfAny && (data.getReturnValueType() != null);
			final boolean[] exceptionThrownHoler = new boolean[] { false };
			returnValue = data.invoke(new InvocationData());
			if (displayReturnValue && !exceptionThrownHoler[0]) {
				openMethodReturnValueWindow(activatorComponent);
			}
		}
		return true;
	}

	protected boolean openMethoExecutionSettingDialog(final Component activatorComponent) {
		final DialogBuilder dialogBuilder = new DialogBuilder(swingRenderer, activatorComponent);
		final boolean displayReturnValue = shouldDisplayReturnValueIfAny && (data.getReturnValueType() != null);
		final boolean[] exceptionThrownHolder = new boolean[] { false };
		final InvocationData invocationData;
		if (swingRenderer.getLastInvocationDataByMethodSignature().containsKey(data)) {
			invocationData = swingRenderer.getLastInvocationDataByMethodSignature().get(data);
		} else {
			invocationData = new InvocationData();
		}
		JPanel methodForm = swingRenderer.createForm(
				new MethodSetupObjectFactory(swingRenderer.getReflectionUI(), data).getInstance(invocationData));
		final boolean[] invokedStatusHolder = new boolean[] { false };
		List<Component> toolbarControls = new ArrayList<Component>();
		String doc = data.getOnlineHelp();
		if ((doc != null) && (doc.trim().length() > 0)) {
			toolbarControls.add(swingRenderer.createOnlineHelpControl(doc));
		}
		final JButton invokeButton = new JButton(data.getCaption());
		{
			invokeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					swingRenderer.getLastInvocationDataByMethodSignature().put(data.getMethodSignature(),
							invocationData);
					try {
						returnValue = data.invoke(invocationData);
					} catch (Throwable t) {
						swingRenderer.handleExceptionsFromDisplayedUI(invokeButton, t);
						exceptionThrownHolder[0] = true;
					}
					if (displayReturnValue) {
						if (!exceptionThrownHolder[0]) {
							openMethodReturnValueWindow(activatorComponent);
						}
					} else {
						dialogBuilder.getCreatedDialog().dispose();
					}
				}
			});
			toolbarControls.add(invokeButton);
		}
		JButton closeButton = new JButton(displayReturnValue ? "Close" : "Cancel");
		{
			closeButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					dialogBuilder.getCreatedDialog().dispose();
				}

			});
			toolbarControls.add(closeButton);
		}

		dialogBuilder.setContentComponent(methodForm);
		dialogBuilder.setTitle(ReflectionUIUtils.composeMessage(data.getCaption(), "Execution"));
		dialogBuilder.setToolbarComponents(toolbarControls);

		swingRenderer.showDialog(dialogBuilder.createDialog(), true);
		if (displayReturnValue) {
			return true;
		} else {
			return invokedStatusHolder[0];
		}
	}

	protected void openMethodReturnValueWindow(final Component activatorComponent) {
		final String windowTitle = ReflectionUIUtils.composeMessage(data.getCaption(), "Result");
		if (retunValueWindowDetached) {
			swingRenderer.openObjectFrame(returnValue);
		} else {
			new AbstractEditorDialogBuilder() {

				@Override
				public Object getInitialObjectValue() {
					return returnValue;
				}

				@Override
				public boolean isObjectNullable() {
					return true;
				}

				@Override
				public boolean isObjectFormExpanded() {
					return true;
				}

				@Override
				public boolean canCommit() {
					return false;
				}

				@Override
				public IModification createCommitModification(Object newObjectValue) {
					return null;
				}

				@Override
				public SwingRenderer getSwingRenderer() {
					return swingRenderer;
				}

				@Override
				public ValueReturnMode getObjectValueReturnMode() {
					return data.getValueReturnMode();
				}

				@Override
				public String getEditorTitle() {
					return windowTitle;
				}

				@Override
				public Component getOwnerComponent() {
					return activatorComponent;
				}

				@Override
				public String getCumulatedModificationsTitle() {
					return InvokeMethodModification.getTitle(data);
				}

				@Override
				public IInfo getCumulatedModificationsTarget() {
					return input.getModificationsTarget();
				}

				@Override
				public IInfoFilter getObjectFormFilter() {
					return IInfoFilter.NO_FILTER;
				}

				@Override
				public ITypeInfo getObjectDeclaredType() {
					return data.getReturnValueType();
				}

				@Override
				public ModificationStack getParentModificationStack() {
					return modificationStack;
				}

			}.showDialog();
		}
	}

	@Override
	public String toString() {
		return "MethodAction [data=" + data + "]";
	}

}
