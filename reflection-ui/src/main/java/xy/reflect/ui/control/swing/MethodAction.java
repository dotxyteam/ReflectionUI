
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
import xy.reflect.ui.control.swing.builder.StandardEditorBuilder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.undo.AbstractModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.MethodControlDataModification;
import xy.reflect.ui.undo.ModificationStack;
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
	protected boolean returnValueObtained = false;
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

	public boolean wasReturnValueObtained() {
		return returnValueObtained;
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
			swingRenderer.handleException(activatorComponent, t);
		}

	}

	public void onInvocationRequest(Component activatorComponent) {
		InvocationData invocationData = prepare(activatorComponent);
		if (invocationData == null) {
			cancelled = true;
			return;
		}
		invokeAndObtainReturnValue(invocationData, activatorComponent);
		if (data.getExecutionSuccessMessage() != null) {
			openExecutionSuccessMessageDialog(activatorComponent);
		}
		if (shouldDisplayReturnValue()) {
			openMethodReturnValueWindow(activatorComponent);
		}
	}

	protected void openExecutionSuccessMessageDialog(Component activatorComponent) {
		swingRenderer.openInformationDialog(activatorComponent, data.getExecutionSuccessMessage(),
				ReflectionUIUtils.composeMessage(getTitle(), "Success"));
	}

	public InvocationData prepare(Component activatorComponent) {
		InvocationData result = null;
		if (!ReflectionUIUtils.requiresParameterValue(data.getParameters())) {
			result = data.createInvocationData();
		} else {
			result = openMethoExecutionSettingsDialog(activatorComponent);
		}
		if (result == null) {
			return null;
		}
		if (!askConfirmation(result, activatorComponent)) {
			return null;
		}
		return result;
	}

	public InvocationData openMethoExecutionSettingsDialog(final Component activatorComponent) {
		final InvocationData invocationData;
		if (swingRenderer.getLastInvocationDataByMethodSignature().containsKey(data.getMethodSignature())) {
			invocationData = swingRenderer.getLastInvocationDataByMethodSignature().get(data.getMethodSignature());
		} else {
			invocationData = data.createInvocationData();
		}
		StandardEditorBuilder editorBuilder = new StandardEditorBuilder(swingRenderer, activatorComponent,
				data.createParametersObject(invocationData, input.getContext().getIdentifier())) {
			@Override
			protected String getEditorWindowTitle() {
				return MethodAction.this.getTitle();
			}

			@Override
			protected boolean isDialogCancellable() {
				return true;
			}

			@Override
			protected String getOKCaption() {
				String invokeButtonText = data.getParametersValidationCustomCaption();
				if (invokeButtonText == null) {
					invokeButtonText = data.getCaption();
				}
				return invokeButtonText;
			}
		};
		editorBuilder.createAndShowDialog();
		if (editorBuilder.isCancelled()) {
			return null;
		}
		return invocationData;
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
			 * the current control data is used to undo/redo modifications. If it happens
			 * then the first displayed ancestor component will be used as the busy dialog
			 * owner. This is why the list of ancestor components is saved here.
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
				final Object[] result = new Object[1];
				swingRenderer.showBusyDialogWhile(getDisplayedActivatorComponent(), new Runnable() {
					public void run() {
						result[0] = data.invoke(invocationData);
					}
				}, ReflectionUIUtils.composeMessage(data.getCaption(), "Executing..."));
				return result[0];
			}

			@Override
			public Runnable getNextInvocationUndoJob(InvocationData invocationData) {
				Runnable result = super.getNextInvocationUndoJob(invocationData);
				if (result == null) {
					return null;
				}
				return new Runnable() {
					@Override
					public void run() {
						swingRenderer.showBusyDialogWhile(getDisplayedActivatorComponent(), new Runnable() {
							public void run() {
								result.run();
							}
						}, ReflectionUIUtils.composeMessage(AbstractModification.getUndoTitle(data.getCaption()),
								"Executing..."));
					}
				};
			}

			@Override
			public Runnable getPreviousInvocationCustomRedoJob(InvocationData invocationData) {
				Runnable result = super.getPreviousInvocationCustomRedoJob(invocationData);
				if (result == null) {
					return null;
				}
				return new Runnable() {
					@Override
					public void run() {
						swingRenderer.showBusyDialogWhile(getDisplayedActivatorComponent(), new Runnable() {
							public void run() {
								result.run();
							}
						}, ReflectionUIUtils.composeMessage(data.getCaption(), "Executing..."));
					}
				};
			}

		};
	}

	public void invokeAndObtainReturnValue(InvocationData invocationData, Component activatorComponent) {
		if (data.getParameters().size() > 0) {
			swingRenderer.getLastInvocationDataByMethodSignature().put(data.getMethodSignature(), invocationData);
		}
		returnValueObtained = false;
		returnValue = makeMethodModificationsUndoable(indicateWhenBusy(data, activatorComponent))
				.invoke(invocationData);
		returnValueObtained = true;
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
		return returnValueObtained && shouldDisplayReturnValueIfAny && (data.getReturnValueType() != null);
	}

	protected void openMethodReturnValueWindow(final Component activatorComponent) {
		AbstractEditorBuilder editorBuilder = createReturnValueEditorBuilder(activatorComponent);
		if (!data.isReturnValueDetached()) {
			editorBuilder.createAndShowDialog();
		} else {
			editorBuilder.createAndShowFrame();
		}
	}

	protected AbstractEditorBuilder createReturnValueEditorBuilder(Component activatorComponent) {
		return new ReturnValueEditorBuilder(swingRenderer, activatorComponent, input, returnValue);
	}

	protected static class ReturnValueEditorBuilder extends AbstractEditorBuilder {

		protected SwingRenderer swingRenderer;
		protected Component ownerComponent;
		protected IMethodControlInput input;
		protected IMethodControlData data;
		protected Object returnValue;

		public ReturnValueEditorBuilder(SwingRenderer swingRenderer, Component ownerComponent,
				IMethodControlInput input, Object returnValue) {
			this.swingRenderer = swingRenderer;
			this.ownerComponent = ownerComponent;
			this.input = input;
			this.data = input.getControlData();
			this.returnValue = returnValue;
		}

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

		@Override
		protected boolean isEncapsulatedValueValidityDetectionEnabled() {
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
		protected IModification createUndoModificationsReplacement() {
			return ReflectionUIUtils.createUndoModificationsReplacement(data);
		}

		@Override
		public SwingRenderer getSwingRenderer() {
			return swingRenderer;
		}

		@Override
		protected Runnable getParentControlRefreshJob() {
			return null;
		}

		@Override
		protected ValueReturnMode getReturnModeFromParent() {
			return data.getValueReturnMode();
		}

		@Override
		protected Component getOwnerComponent() {
			return ownerComponent;
		}

		@Override
		protected String getParentModificationTitle() {
			return MethodControlDataModification.getTitle(data.getCaption());
		}

		@Override
		protected boolean isParentModificationVolatile() {
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
				return input.getModificationStack();
			}
		}

	}

}
