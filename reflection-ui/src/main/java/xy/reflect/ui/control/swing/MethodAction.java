package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import xy.reflect.ui.info.DesktopSpecificProperty;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.EncapsulatedObjectFactory;
import xy.reflect.ui.info.type.util.MethodSetupObjectFactory;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.InvokeMethodModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIUtils;

public class MethodAction extends AbstractAction {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected Object object;
	protected IMethodInfo method;
	protected Object returnValue;
	protected boolean shouldDisplayReturnValueIfAny = false;
	protected boolean retunValueWindowDetached;
	protected ModificationStack modificationStack;

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
			final boolean displayReturnValue = shouldDisplayReturnValueIfAny && (method.getReturnValueType() != null);
			final boolean[] exceptionThrownHoler = new boolean[] { false };
			returnValue = method.invoke(object, new InvocationData());
			if (displayReturnValue && !exceptionThrownHoler[0]) {
				openMethodReturnValueWindow(activatorComponent);
			}
		}
		return true;
	}

	protected boolean openMethoExecutionSettingDialog(final Component activatorComponent) {
		final DialogBuilder dialogBuilder = new DialogBuilder(swingRenderer, activatorComponent);

		final boolean displayReturnValue = shouldDisplayReturnValueIfAny && (method.getReturnValueType() != null);
		final boolean[] exceptionThrownHolder = new boolean[] { false };
		final InvocationData invocationData;
		if (swingRenderer.getLastInvocationDataByMethod().containsKey(method)) {
			invocationData = swingRenderer.getLastInvocationDataByMethod().get(method);
		} else {
			invocationData = new InvocationData();
		}
		JPanel methodForm = swingRenderer
				.createForm(new MethodSetupObjectFactory(swingRenderer.getReflectionUI(), object, method)
						.getInstance(object, invocationData));
		final boolean[] invokedStatusHolder = new boolean[] { false };
		List<Component> toolbarControls = new ArrayList<Component>();
		String doc = method.getOnlineHelp();
		if ((doc != null) && (doc.trim().length() > 0)) {
			toolbarControls.add(swingRenderer.createOnlineHelpControl(doc));
		}
		final JButton invokeButton = new JButton(method.getCaption());
		{
			invokeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					swingRenderer.getLastInvocationDataByMethod().put(method, invocationData);
					try {
						returnValue = method.invoke(object, invocationData);
					} catch (Throwable t) {
						swingRenderer.handleExceptionsFromDisplayedUI(invokeButton, t);
					}
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
		dialogBuilder.setTitle(ReflectionUIUtils.composeMessage(method.getCaption(), "Execution"));
		dialogBuilder.setToolbarComponents(toolbarControls);

		swingRenderer.showDialog(dialogBuilder.build(), true);
		if (displayReturnValue) {
			return true;
		} else {
			return invokedStatusHolder[0];
		}
	}

	protected void openMethodReturnValueWindow(Component activatorComponent) {
		String windowTitle = ReflectionUIUtils.composeMessage(method.getCaption(), "Result");
		if (returnValue == null) {
			EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(swingRenderer.getReflectionUI(),
					swingRenderer.getReflectionUI().getTypeInfo(new JavaTypeInfoSource(Object.class)));
			encapsulation.setTypeCaption(ReflectionUIUtils.composeMessage(windowTitle, "<null>"));
			encapsulation.setFieldCaption("");
			encapsulation.setFieldGetOnly(true);
			encapsulation.setFieldNullValueLabel(method.getNullReturnValueLabel());
			Object nullEncapsulated = encapsulation.getInstance(Accessor.returning(null));
			ObjectDialogBuilder dialogBuilder = new ObjectDialogBuilder(swingRenderer, activatorComponent,
					nullEncapsulated);
			dialogBuilder.setCancellable(false);
			dialogBuilder.setTitle(windowTitle);
			swingRenderer.showDialog(dialogBuilder.build(), true);
		} else {
			if (retunValueWindowDetached) {
				swingRenderer.openObjectFrame(returnValue);
			} else {
				EncapsulatedObjectFactory encapsulation = new EncapsulatedObjectFactory(swingRenderer.getReflectionUI(),
						method.getReturnValueType());
				encapsulation.setTypeCaption(windowTitle);
				encapsulation.setFieldCaption("");
				encapsulation.setFieldGetOnly(true);
				encapsulation.setFieldNullable(false);
				encapsulation.setFieldValueReturnMode(method.getValueReturnMode());
				Map<String, Object> properties = new HashMap<String, Object>();
				{
					DesktopSpecificProperty.setSubFormExpanded(properties, true);
					encapsulation.setFieldSpecificProperties(properties);
				}
				Object encapsulated = encapsulation.getInstance(Accessor.returning(returnValue));
								
				ObjectDialogBuilder dialogBuilder = new ObjectDialogBuilder(swingRenderer, activatorComponent,
						encapsulated);
				swingRenderer.showDialog(dialogBuilder.build(), true);

				if (modificationStack != null) {
					ModificationStack childModifStack = dialogBuilder.getModificationStack();
					String childModifTitle = InvokeMethodModification.getTitle(method);
					IInfo childModifTarget = method;
					IModification commitModif = null;
					boolean childModifAccepted = (!dialogBuilder.isCancellable()) || dialogBuilder.wasOkPressed();
					ValueReturnMode childValueReturnMode = method.getValueReturnMode();
					boolean childValueNew = dialogBuilder.isValueNew();
					ReflectionUIUtils.integrateSubModifications(swingRenderer.getReflectionUI(), modificationStack,
							childModifStack, childModifAccepted, childValueReturnMode, childValueNew, commitModif,
							childModifTarget, childModifTitle);
				}
			}
		}
	}

}
