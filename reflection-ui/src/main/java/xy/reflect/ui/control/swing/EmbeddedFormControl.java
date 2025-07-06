
package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.border.Border;

import xy.reflect.ui.control.ErrorHandlingFieldControlData;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory;
import xy.reflect.ui.undo.FieldControlDataModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.SlaveModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control that displays an embedded form allowing to edit the field
 * value.
 * 
 * @author olitank
 *
 */
public class EmbeddedFormControl extends ControlPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IFieldControlData data;

	protected Component textControl;
	protected Component iconControl;
	protected IFieldControlInput input;
	protected Form subForm;
	protected Object lastSubFormObject;

	public EmbeddedFormControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		input = new FieldControlInputProxy(input) {
			IFieldControlData errorHandlingFieldControlData = new ErrorHandlingFieldControlData(super.getControlData(),
					swingRenderer, EmbeddedFormControl.this);

			@Override
			public IFieldControlData getControlData() {
				return errorHandlingFieldControlData;
			}
		};
		this.input = input;
		this.data = input.getControlData();
		setLayout(new BorderLayout());
		refreshUI(true);
	}

	public Form getSubForm() {
		return subForm;
	}

	@Override
	public boolean requestCustomFocus() {
		return SwingRendererUtils.requestAnyComponentFocus(subForm, swingRenderer);
	}

	protected void handleSubFormModifications() {
		Accessor<Boolean> childModifAcceptedGetter = Accessor.returning(Boolean.TRUE);
		Accessor<ValueReturnMode> childValueReturnModeGetter = new Accessor<ValueReturnMode>() {
			@Override
			public ValueReturnMode get() {
				return data.getValueReturnMode();
			}
		};
		Accessor<Boolean> childValueReplacedGetter = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return lastSubFormObject != subForm.getObject();
			}
		};
		Accessor<Boolean> childValueTransactionExecutedGetter = Accessor.returning(false);
		Accessor<IModification> committingModifGetter = new Accessor<IModification>() {
			@Override
			public IModification get() {
				if (data.isGetOnly()) {
					return null;
				}
				return new FieldControlDataModification(data, subForm.getObject());
			}
		};
		Accessor<IModification> undoModificationsReplacementGetter = new Accessor<IModification>() {
			@Override
			public IModification get() {
				return ReflectionUIUtils.createUndoModificationsReplacement(data);
			}
		};
		Accessor<String> childModifTitleGetter = new Accessor<String>() {
			@Override
			public String get() {
				return FieldControlDataModification.getTitle(data.getCaption());
			}
		};
		Accessor<ModificationStack> masterModifStackGetter = new Accessor<ModificationStack>() {
			@Override
			public ModificationStack get() {
				return input.getModificationStack();
			}
		};
		Accessor<Boolean> masterModifVolatileGetter = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return data.isTransient();
			}
		};
		Accessor<Runnable> parentControlRefreshJobGetter = new Accessor<Runnable>() {
			@Override
			public Runnable get() {
				return new Runnable() {
					@Override
					public void run() {
						EmbeddedFormControl.this.refreshUI(false);
					}
				};
			}
		};
		boolean exclusiveLinkWithParent = Boolean.TRUE.equals(input.getControlData().getSpecificProperties()
				.get(EncapsulatedObjectFactory.ENCAPSULATION_STATUS_PROPERTY_KEY));
		Listener<Throwable> masterModificationExceptionListener = new Listener<Throwable>() {
			@Override
			public void handle(Throwable t) {
				swingRenderer.handleException(EmbeddedFormControl.this, t);
			}
		};
		SlaveModificationStack slaveModficationStack = new SlaveModificationStack(subForm.getName(),
				childModifAcceptedGetter, childValueReturnModeGetter, childValueReplacedGetter,
				childValueTransactionExecutedGetter, committingModifGetter, undoModificationsReplacementGetter,
				childModifTitleGetter, masterModifStackGetter, masterModifVolatileGetter, parentControlRefreshJobGetter,
				exclusiveLinkWithParent, ReflectionUIUtils.getDebugLogListener(swingRenderer.getReflectionUI()),
				ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()),
				masterModificationExceptionListener);
		for (IModificationListener listener : subForm.getModificationStack().getListeners()) {
			slaveModficationStack.addSlaveListener(listener);
		}
		subForm.setModificationStack(slaveModficationStack);
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	@Override
	public boolean displayError(Throwable error) {
		return false;
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		if (refreshStructure) {
			SwingRendererUtils.showFieldCaptionOnBorder(data, this, new Accessor<Border>() {
				@Override
				public Border get() {
					return new ControlPanel().getBorder();
				}
			}, swingRenderer);
			SwingRendererUtils.handleComponentSizeChange(this);
		}
		if (subForm == null) {
			lastSubFormObject = data.getValue();
			if (lastSubFormObject == null) {
				throw new ReflectionUIError();
			}
			subForm = swingRenderer.createForm(lastSubFormObject, data.getFormControlFilter());
			handleSubFormModifications();
			add(subForm, BorderLayout.CENTER);
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			if (refreshStructure) {
				subForm.setInfoFilter(data.getFormControlFilter());
			}
			final Object newSubFormObject = data.getValue();
			if (newSubFormObject == null) {
				return false;
			}
			if (newSubFormObject == subForm.getObject()) {
				subForm.refresh(refreshStructure);
			} else {
				final ITypeInfo subFormObjectType = swingRenderer.getReflectionUI()
						.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(subForm.getObject()));
				final ITypeInfo newSubFormObjectType = swingRenderer.getReflectionUI()
						.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(newSubFormObject));
				if (subFormObjectType.equals(newSubFormObjectType)) {
					swingRenderer.showBusyDialogWhile(this, new Runnable() {
						@Override
						public void run() {
							subFormObjectType.onFormVisibilityChange(subForm.getObject(), false);
							subForm.setObject(newSubFormObject);
							newSubFormObjectType.onFormVisibilityChange(newSubFormObject, true);
						}
					}, "Refreshing " + swingRenderer.getObjectTitle(newSubFormObject) + "...");
					subForm.refresh(refreshStructure);
				} else {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean isAutoManaged() {
		return true;
	}

	@Override
	public void validateControl(ValidationSession session) throws Exception {
		subForm.validateForm(session);
	}

	@Override
	public void addMenuContributions(MenuModel menuModel) {
		subForm.addMenuContributionTo(menuModel);
	}

}
