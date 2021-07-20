


package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import xy.reflect.ui.control.CustomContext;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.MethodControlDataProxy;
import xy.reflect.ui.control.swing.builder.AbstractEditorBuilder;
import xy.reflect.ui.control.swing.builder.DialogBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.MethodControlDataModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Action allowing orchestrate a method execution (retrieve parameter values,
 * invoke the method and display the return value).
 * 
 * @author olitank
 *
 */
public class MethodAction extends AbstractAction {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IMethodControlInput input;
	protected IMethodControlData data;
	protected boolean shouldDisplayReturnValueIfAny;
	protected ModificationStack modificationStack;

	protected Object returnValue;
	protected boolean returnValueSet = false;
	protected boolean cancelled = false;

	public MethodAction(SwingRenderer swingRenderer, IMethodControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		this.shouldDisplayReturnValueIfAny = !data.isReturnValueIgnored();
		this.modificationStack = input.getModificationStack();
	}

	public SwingRenderer getSwingRenderer() {
		return swingRenderer;
	}

	public Object getReturnValue() {
		return returnValue;
	}

	public boolean isReturnValueSet() {
		return returnValueSet;
	}

	public boolean wasCancelled() {
		return cancelled;
	}

	public void setShouldDisplayReturnValueIfAny(boolean shouldDisplayReturnValueIfAny) {
		this.shouldDisplayReturnValueIfAny = shouldDisplayReturnValueIfAny;
	}

	public boolean getShouldDisplayReturnValueIfAny() {
		return shouldDisplayReturnValueIfAny;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Component activatorComponent = (Component) e.getSource();
		try {
			onInvocationRequest(activatorComponent);
		} catch (Throwable t) {
			swingRenderer.handleObjectException(activatorComponent, t);
		}

	}

	public void onInvocationRequest(Component activatorComponent) {
		InvocationData invocationData = prepare(activatorComponent);
		if (invocationData == null) {
			cancelled = true;
			return;
		}
		invokeAndSetReturnValue(invocationData, activatorComponent);
		if (shouldDisplayReturnValue()) {
			openMethodReturnValueWindow(activatorComponent);
		}
	}

	public InvocationData prepare(Component activatorComponent) {
		InvocationData result = null;
		if (!ReflectionUIUtils.requiresParameterValue(data.getParameters())) {
			result = data.createInvocationData();
		} else {
			result = openMethoExecutionSettingDialog(activatorComponent);
		}
		if (result == null) {
			return null;
		}
		if (!askConfirmation(result, activatorComponent)) {
			return null;
		}
		return result;
	}

	public InvocationData openMethoExecutionSettingDialog(final Component activatorComponent) {
		final DialogBuilder dialogBuilder = swingRenderer.createDialogBuilder(activatorComponent);
		final InvocationData invocationData;
		if (swingRenderer.getLastInvocationDataByMethodSignature().containsKey(data.getMethodSignature())) {
			invocationData = swingRenderer.getLastInvocationDataByMethodSignature().get(data.getMethodSignature());
		} else {
			invocationData = data.createInvocationData();
		}
		final Form methodForm = swingRenderer
				.createForm(data.createParametersObject(invocationData, input.getContext().getIdentifier()));

		List<Component> buttonBarControls = new ArrayList<Component>();
		{
			buttonBarControls.addAll(methodForm.createButtonBarControls());
			String invokeButtonText = data.getParametersValidationCustomCaption();
			if (invokeButtonText == null) {
				invokeButtonText = data.getCaption();
			}
			buttonBarControls.addAll(dialogBuilder.createStandardOKCancelDialogButtons(invokeButtonText, null));
			dialogBuilder.setButtonBarControls(buttonBarControls);
		}

		dialogBuilder.setContentComponent(methodForm);
		dialogBuilder.setTitle(getTitle());

		swingRenderer.showDialog(dialogBuilder.createDialog(), true);
		if (dialogBuilder.getCreatedDialog().wasOkPressed()) {
			return invocationData;
		} else {
			return null;
		}
	}

	public String getTitle() {
		return ReflectionUIUtils.composeMessage(data.getCaption(), "Execution");
	}

	protected IMethodControlData makeMethodModificationsUndoable(final IMethodControlData data) {
		return new MethodControlDataProxy(data) {

			@Override
			public Object invoke(InvocationData invocationData) {
				return ReflectionUIUtils.invokeMethodThroughModificationStack(data, invocationData, modificationStack,
						ReflectionUIUtils.getDebugLogListener(swingRenderer.getReflectionUI()));
			}

		};
	}

	protected IMethodControlData indicateWhenBusy(final IMethodControlData data, final Component activatorComponent) {
		return new MethodControlDataProxy(data) {

			/*
			 * The initial activator component may not be displayed anymore typically when
			 * the current control data is used to undo/redo modifications. If it happen
			 * then the first displayed ancestor component will be used as the busy dialog
			 * owner. TGhis is why the list of ancestor components is saved here.
			 */

			List<Component> initialComponentHierachy = getComponentHierachy(activatorComponent);

			List<Component> getComponentHierachy(Component c) {
				List<Component> result = new ArrayList<Component>();
				if (c != null) {
					result.add(c);
					if (c instanceof Window) {
						Window ownerWindow = ((Window) c).getOwner();
						if (ownerWindow != null) {
							result.addAll(getComponentHierachy(ownerWindow));
						}
					} else {
						Window window = SwingUtilities.getWindowAncestor(c);
						if (window != null) {
							result.addAll(getComponentHierachy(window));
						}
					}
				}
				return result;
			}

			Component getDisplayedActivatorComponent() {
				Component result = null;
				for (Component c : initialComponentHierachy) {
					if (!c.isVisible()) {
						continue;
					}
					if (!c.isDisplayable()) {
						continue;
					}
					result = c;
					break;
				}
				return result;
			}

			@Override
			public Object invoke(final InvocationData invocationData) {
				return SwingRendererUtils.showBusyDialogWhileInvokingMethod(getDisplayedActivatorComponent(),
						swingRenderer, data, invocationData);
			}

		};
	}

	public void invokeAndSetReturnValue(InvocationData invocationData, Component activatorComponent) {
		if (data.getParameters().size() > 0) {
			swingRenderer.getLastInvocationDataByMethodSignature().put(data.getMethodSignature(), invocationData);
		}
		returnValueSet = false;
		returnValue = makeMethodModificationsUndoable(indicateWhenBusy(data, activatorComponent))
				.invoke(invocationData);
		returnValueSet = true;

	}

	public boolean askConfirmation(InvocationData invocationData, Component activatorComponent) {
		String confirmationMessage = data.getConfirmationMessage(invocationData);
		if (confirmationMessage != null) {
			if (!swingRenderer.openQuestionDialog(activatorComponent, confirmationMessage, getTitle(), "OK",
					"Cancel")) {
				return false;
			}
		}
		return true;
	}

	protected boolean shouldDisplayReturnValue() {
		return returnValueSet && shouldDisplayReturnValueIfAny && (data.getReturnValueType() != null);
	}

	protected void openMethodReturnValueWindow(final Component activatorComponent) {
		AbstractEditorBuilder editorBuilder = new AbstractEditorBuilder() {

			@Override
			protected IContext getContext() {
				return input.getContext();
			}

			@Override
			protected IContext getSubContext() {
				return new CustomContext("MethodResult");
			}

			@Override
			protected Object loadValue() {
				return returnValue;
			}

			@Override
			protected boolean isNullValueDistinct() {
				return data.isNullReturnValueDistinct();
			}

			@Override
			protected boolean isEncapsulatedFormEmbedded() {
				return true;
			}

			protected boolean canCommitToParent() {
				return false;
			}

			@Override
			protected void handleRealtimeLinkCommitException(Throwable t) {
				throw new ReflectionUIError();
			}

			@Override
			protected IModification createCommittingModification(Object newObjectValue) {
				return null;
			}

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			protected ValueReturnMode getReturnModeFromParent() {
				return data.getValueReturnMode();
			}

			@Override
			protected Component getOwnerComponent() {
				return activatorComponent;
			}

			@Override
			protected String getParentModificationTitle() {
				return MethodControlDataModification.getTitle(data.getCaption());
			}

			@Override
			protected boolean isParentModificationFake() {
				return false;
			}

			@Override
			protected IInfoFilter getEncapsulatedFormFilter() {
				return IInfoFilter.DEFAULT;
			}

			@Override
			protected ITypeInfoSource getEncapsulatedFieldDeclaredTypeSource() {
				return data.getReturnValueType().getSource();
			}

			@Override
			protected ModificationStack getParentModificationStack() {
				if (data.isReturnValueDetached()) {
					return null;
				} else {
					return MethodAction.this.modificationStack;
				}
			}

		};
		if (!data.isReturnValueDetached()) {
			editorBuilder.createAndShowDialog();
		} else {
			if (returnValue == null) {
				EncapsulatedObjectFactory nullEncapsulation = new EncapsulatedObjectFactory(
						swingRenderer.getReflectionUI(), "Encapsulation [context=" + input.getContext().getIdentifier()
								+ ", subContext=NullReturnValue]",
						data.getReturnValueType());
				swingRenderer.openObjectFrame(nullEncapsulation.getInstance(Accessor.returning(null)));
			} else {
				swingRenderer.openObjectFrame(returnValue);
			}
		}
	}

	@Override
	public String toString() {
		return "MethodAction [data=" + data + "]";
	}

}
