package xy.reflect.ui.control.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import xy.reflect.ui.control.CustomContext;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.swing.editor.AbstractEditorBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.InvokeMethodModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.component.AbstractControlButton;

public class MethodAction extends AbstractAction {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IMethodControlInput input;
	protected IMethodControlData data;
	protected boolean shouldDisplayReturnValueIfAny;
	protected ModificationStack modificationStack;

	protected Object returnValue;
	protected boolean exceptionThrown = false;
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

	public boolean wasExceptionThrown() {
		return exceptionThrown;
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
			execute(activatorComponent);
		} catch (Throwable t) {
			swingRenderer.handleExceptionsFromDisplayedUI(activatorComponent, t);
		}

	}

	public void execute(Component activatorComponent) {
		onMethodInvocationRequest(activatorComponent);
	}

	protected void onMethodInvocationRequest(Component activatorComponent) {
		if (data.getParameters().size() > 0) {
			openMethoExecutionSettingDialog(activatorComponent);
		} else {
			invoke(data.createInvocationData(), activatorComponent);
		}
	}

	protected void openMethoExecutionSettingDialog(final Component activatorComponent) {
		final DialogBuilder dialogBuilder = swingRenderer.getDialogBuilder(activatorComponent);
		final InvocationData invocationData;
		if (swingRenderer.getLastInvocationDataByMethodSignature().containsKey(data.getMethodSignature())) {
			invocationData = swingRenderer.getLastInvocationDataByMethodSignature().get(data.getMethodSignature());
		} else {
			invocationData = data.createInvocationData();
		}
		final Form methodForm = swingRenderer
				.createForm(data.createParametersObject(invocationData, input.getContext().getIdentifier()));
		Accessor<List<Component>> toolbarControlsAccessor = new Accessor<List<Component>>() {

			@Override
			public List<Component> get() {
				List<Component> toolbarControls = new ArrayList<Component>();
				toolbarControls.addAll(methodForm.createToolbarControls());
				String invokeButtonText = data.getParametersValidationCustomCaption();
				if (invokeButtonText == null) {
					invokeButtonText = data.getCaption();
				}
				final JButton invokeButton = createTool(invokeButtonText);
				{
					invokeButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							swingRenderer.getLastInvocationDataByMethodSignature().put(data.getMethodSignature(),
									invocationData);
							invoke(invocationData, invokeButton);
							dialogBuilder.getCreatedDialog().dispose();
						}
					});
					toolbarControls.add(invokeButton);
				}
				JButton cancelButton = createTool("Cancel");
				{
					cancelButton.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							cancelled = true;
							dialogBuilder.getCreatedDialog().dispose();
						}

					});
					toolbarControls.add(cancelButton);
				}
				return toolbarControls;
			}
		};

		dialogBuilder.setContentComponent(methodForm);
		dialogBuilder.setTitle(getTitle());
		dialogBuilder.setToolbarComponentsAccessor(toolbarControlsAccessor);

		swingRenderer.showDialog(dialogBuilder.createDialog(), true);
	}

	protected JButton createTool(final String caption) {
		return new AbstractControlButton() {
			private static final long serialVersionUID = 1L;

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			protected boolean isApplicationStyleButtonSpecific() {
				return true;
			}

			@Override
			public String retrieveCaption() {
				return caption;
			}

		};
	}

	public String getTitle() {
		return ReflectionUIUtils.composeMessage(data.getCaption(), "Execution");
	}

	protected void invoke(InvocationData invocationData, Component activatorComponent) {
		String confirmationMessage = data.getConfirmationMessage(invocationData);
		if (confirmationMessage != null) {
			if (!swingRenderer.openQuestionDialog(activatorComponent, confirmationMessage, getTitle(), "OK",
					"Cancel")) {
				cancelled = true;
				return;
			}
		}
		try {
			returnValue = data.invoke(invocationData);
			exceptionThrown = false;
		} catch (Throwable t) {
			swingRenderer.handleExceptionsFromDisplayedUI(activatorComponent, t);
			exceptionThrown = true;
		}
		if (shouldDisplayReturnValue() && !exceptionThrown) {
			openMethodReturnValueWindow(activatorComponent);
		}
	}

	protected boolean shouldDisplayReturnValue() {
		return shouldDisplayReturnValueIfAny && (data.getReturnValueType() != null);
	}

	protected void openMethodReturnValueWindow(final Component activatorComponent) {
		AbstractEditorBuilder editorBuilder = new AbstractEditorBuilder() {

			@Override
			public IContext getContext() {
				return input.getContext();
			}

			@Override
			public IContext getSubContext() {
				return new CustomContext("MethodResult");
			}

			@Override
			public Object getInitialObjectValue() {
				return returnValue;
			}

			@Override
			public boolean isObjectNullValueDistinct() {
				return data.isNullReturnValueDistinct();
			}

			@Override
			public boolean isObjectFormExpanded() {
				return true;
			}

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
			public Component getOwnerComponent() {
				return activatorComponent;
			}

			@Override
			public String getCumulatedModificationsTitle() {
				return InvokeMethodModification.getTitle(data.getCaption());
			}

			@Override
			public IInfoFilter getObjectFormFilter() {
				return IInfoFilter.DEFAULT;
			}

			@Override
			public ITypeInfoSource getObjectDeclaredNonSpecificTypeInfoSource() {
				return data.getReturnValueType().getSource();
			}

			@Override
			public ModificationStack getParentObjectModificationStack() {
				if (data.isReturnValueDetached()) {
					return null;
				} else {
					return modificationStack;
				}
			}

		};
		if (!data.isReturnValueDetached() || (returnValue == null)) {
			editorBuilder.showDialog();
		} else {
			editorBuilder.createAndShowFrame();
		}
	}

	@Override
	public String toString() {
		return "MethodAction [data=" + data + "]";
	}

}
