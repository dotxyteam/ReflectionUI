package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.util.MethodSetupObjectFactory;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.InvokeMethodModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import javax.swing.AbstractAction;

public class MethodAction extends AbstractAction {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected Object object;
	protected IMethodInfo method;
	protected Object returnValue;
	protected boolean shouldDisplayReturnValue = true;

	public MethodAction(SwingRenderer swingRenderer, Object object, IMethodInfo method) {
		this.swingRenderer = swingRenderer;
		this.object = object;
		this.method = method;

	}

	public SwingRenderer getSwingRenderer() {
		return swingRenderer;
	}

	public Object getObject() {
		return object;
	}

	public Object getReturnValue() {
		return returnValue;
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

	public IMethodInfo getMethod() {
		return method;
	}

	public void execute(Component activatorComponent) {
		onMethodInvocationRequest(activatorComponent);
	}

	protected boolean onMethodInvocationRequest(Component activatorComponent) {
		if (method.getParameters().size() > 0) {
			return openMethoExecutionSettingDialog(activatorComponent);
		} else {
			final boolean displayReturnValue = shouldDisplayReturnValue && (method.getReturnValueType() != null);
			final boolean[] exceptionThrownHoler = new boolean[] { false };
			swingRenderer.showBusyDialogWhile(activatorComponent, new Runnable() {
				@Override
				public void run() {
					try {
						returnValue = method.invoke(object, new InvocationData());
					} catch (Throwable t) {
						exceptionThrownHoler[0] = true;
						throw new ReflectionUIError(t);
					}
				}
			}, ReflectionUIUtils.composeTitle(method.getCaption(), "Execution"));
			if (displayReturnValue && !exceptionThrownHoler[0]) {
				openMethodReturnValueWindow(activatorComponent);
			}
		}
		return true;
	}

	protected void openMethodReturnValueWindow(Component activatorComponent) {
		if (returnValue == null) {
			String msg = "No data returned!";
			swingRenderer.openMessageDialog(activatorComponent, msg, "Result", null);
		} else {
			if (method.getValueReturnMode() == ValueReturnMode.COPY) {
				swingRenderer.openObjectFrame(returnValue);
			} else {
				ObjectDialogBuilder dialogBuilder = new ObjectDialogBuilder(swingRenderer, activatorComponent,
						returnValue);
				dialogBuilder.setGetOnly(true);
				boolean cancellable = true;
				{
					if (!dialogBuilder.getDisplayValueType().isModificationStackAccessible()) {
						cancellable = false;
					}
					if (method.getValueReturnMode() == ValueReturnMode.COPY) {
						cancellable = false;
					}
				}
				dialogBuilder.setCancellable(cancellable);
				swingRenderer.showDialog(dialogBuilder.build(), true);

				if (activatorComponent != null) {
					ModificationStack parentModifStack = SwingRendererUtils
							.findParentFormModificationStack(activatorComponent, swingRenderer);
					if (parentModifStack != null) {
						ModificationStack childModifStack = dialogBuilder.getModificationStack();
						String childModifTitle = InvokeMethodModification.getTitle(method);
						IInfo childModifTarget = method;
						IModification commitModif = null;
						boolean childModifAccepted = (!dialogBuilder.isCancellable()) || dialogBuilder.isOkPressed();
						ValueReturnMode childValueReturnMode = method.getValueReturnMode();
						boolean childValueNew = dialogBuilder.isValueNew();
						ReflectionUIUtils.integrateSubModifications(parentModifStack, childModifStack,
								childModifAccepted, childValueReturnMode, childValueNew, commitModif, childModifTarget,
								childModifTitle);
					}
				}
			}
		}
	}

	protected boolean openMethoExecutionSettingDialog(final Component activatorComponent) {
		final DialogBuilder dialogBuilder = new DialogBuilder(swingRenderer, activatorComponent);

		final boolean displayReturnValue = shouldDisplayReturnValue && (method.getReturnValueType() != null);
		final boolean[] exceptionThrownHolder = new boolean[] { false };
		final InvocationData invocationData;
		if (swingRenderer.getLastInvocationDataByMethod().containsKey(method)) {
			invocationData = swingRenderer.getLastInvocationDataByMethod().get(method);
		} else {
			invocationData = new InvocationData();
		}
		JPanel methodForm = swingRenderer
				.createObjectForm(new MethodSetupObjectFactory(swingRenderer.getReflectionUI(), method)
						.getInstance(object, invocationData));
		final boolean[] invokedStatusHolder = new boolean[] { false };
		List<Component> toolbarControls = new ArrayList<Component>();
		String doc = method.getOnlineHelp();
		if ((doc != null) && (doc.trim().length() > 0)) {
			toolbarControls.add(swingRenderer.createOnlineHelpControl(doc));
		}
		JButton invokeButton = new JButton(method.getCaption());
		{
			invokeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					swingRenderer.getLastInvocationDataByMethod().put(method, invocationData);
					swingRenderer.showBusyDialogWhile(activatorComponent, new Runnable() {
						@Override
						public void run() {
							try {
								returnValue = method.invoke(object, invocationData);
							} catch (Throwable t) {
								exceptionThrownHolder[0] = true;
								throw new ReflectionUIError(t);
							}
						}
					}, ReflectionUIUtils.composeTitle(method.getCaption(), "Execution"));
					if (displayReturnValue) {
						if (!exceptionThrownHolder[0]) {
							openMethodReturnValueWindow(activatorComponent);
						}
					} else {
						dialogBuilder.getBuiltDialog().dispose();
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
					dialogBuilder.getBuiltDialog().dispose();
				}

			});
			toolbarControls.add(closeButton);
		}

		dialogBuilder.setContentComponent(methodForm);
		dialogBuilder.setTitle(ReflectionUIUtils.composeTitle(method.getCaption(), "Setup"));
		dialogBuilder.setToolbarComponents(toolbarControls);

		swingRenderer.showDialog(dialogBuilder.build(), true);
		if (displayReturnValue) {
			return true;
		} else {
			return invokedStatusHolder[0];
		}
	}

	public void setShouldDisplayReturnValue(boolean shouldDisplayReturnValue) {
		this.shouldDisplayReturnValue = shouldDisplayReturnValue;
	}

	public boolean getShouldDisplayReturnValue() {
		return shouldDisplayReturnValue;
	}
	
	
}
