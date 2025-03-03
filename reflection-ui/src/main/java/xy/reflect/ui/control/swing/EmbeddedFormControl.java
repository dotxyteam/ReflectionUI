
package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import xy.reflect.ui.control.ErrorHandlingFieldControlData;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
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
	protected Object subFormObject;
	protected Form subForm;
	protected ModificationStack subFormInitialModificationStack;
	protected IFieldControlInput input;
	protected IModificationListener refreshingModificationListener = new AbstractSimpleModificationListener() {
		@Override
		protected void handleAnyEvent(IModification modification) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					refreshUI(false);
				}
			});
		}
	};

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
		if (!ReflectionUIUtils.mayModificationsHaveImpact(
				ReflectionUIUtils.isValueImmutable(swingRenderer.getReflectionUI(), subFormObject),
				data.getValueReturnMode(), !data.isGetOnly())) {
			if (!Arrays.asList(subFormInitialModificationStack.getListeners())
					.contains(refreshingModificationListener)) {
				subFormInitialModificationStack.addListener(refreshingModificationListener);
			}
			subForm.setModificationStack(subFormInitialModificationStack);
		} else {
			Accessor<Boolean> childModifAcceptedGetter = Accessor.returning(Boolean.TRUE);
			Accessor<ValueReturnMode> childValueReturnModeGetter = new Accessor<ValueReturnMode>() {
				@Override
				public ValueReturnMode get() {
					return data.getValueReturnMode();
				}
			};
			Accessor<Boolean> childValueReplacedGetter = Accessor.returning(Boolean.FALSE);
			Accessor<Boolean> childValueTransactionExecutedGetter = Accessor.returning(false);
			Accessor<IModification> committingModifGetter = new Accessor<IModification>() {
				@Override
				public IModification get() {
					if (data.isGetOnly()) {
						return null;
					}
					return new FieldControlDataModification(data, subFormObject);
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
			Accessor<Boolean> masterModifFakeGetter = new Accessor<Boolean>() {
				@Override
				public Boolean get() {
					return data.isTransient();
				}
			};
			boolean exclusiveLinkWithParent = Boolean.TRUE.equals(input.getControlData().getSpecificProperties()
					.get(EncapsulatedObjectFactory.IS_ENCAPSULATION_FIELD_PROPERTY_KEY));
			Listener<Throwable> masterModificationExceptionListener = new Listener<Throwable>() {
				@Override
				public void handle(Throwable t) {
					swingRenderer.handleObjectException(EmbeddedFormControl.this, t);
				}
			};
			SlaveModificationStack slaveModficationStack = new SlaveModificationStack(subForm.getName(),
					childModifAcceptedGetter, childValueReturnModeGetter, childValueReplacedGetter,
					childValueTransactionExecutedGetter, committingModifGetter, undoModificationsReplacementGetter,
					childModifTitleGetter, masterModifStackGetter, masterModifFakeGetter, exclusiveLinkWithParent,
					ReflectionUIUtils.getDebugLogListener(swingRenderer.getReflectionUI()),
					ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()),
					masterModificationExceptionListener);
			for (IModificationListener listener : subFormInitialModificationStack.getListeners()) {
				if (listener == refreshingModificationListener) {
					continue;
				}
				slaveModficationStack.addSlaveListener(listener);
			}
			subForm.setModificationStack(slaveModficationStack);
		}
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		if (refreshStructure) {
			if (data.getCaption().length() > 0) {
				setBorder(BorderFactory.createTitledBorder(swingRenderer.prepareMessageToDisplay(data.getCaption())));
				if (data.getLabelForegroundColor() != null) {
					((TitledBorder) getBorder())
							.setTitleColor(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
				}
				if (data.getBorderColor() != null) {
					((TitledBorder) getBorder()).setBorder(
							BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
				}
				if (data.getLabelCustomFontResourcePath() != null) {
					((TitledBorder) getBorder())
							.setTitleFont(
									SwingRendererUtils
											.loadFontThroughCache(data.getLabelCustomFontResourcePath(),
													ReflectionUIUtils
															.getErrorLogListener(swingRenderer.getReflectionUI()))
											.deriveFont(((TitledBorder) getBorder()).getTitleFont().getStyle(),
													((TitledBorder) getBorder()).getTitleFont().getSize()));
				}
			} else {
				setBorder(null);
			}
		}
		if (subForm == null) {
			subFormObject = data.getValue();
			if (subFormObject == null) {
				throw new ReflectionUIError();
			}
			IInfoFilter filter = data.getFormControlFilter();
			subForm = swingRenderer.createForm(subFormObject, filter);
			subFormInitialModificationStack = subForm.getModificationStack();
			add(subForm, BorderLayout.CENTER);
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			final Object newSubFormObject = data.getValue();
			if (newSubFormObject == null) {
				return false;
			}
			if (newSubFormObject == subFormObject) {
				subForm.refresh(refreshStructure);
			} else {
				final ITypeInfo subFormObjectType = swingRenderer.getReflectionUI()
						.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(subFormObject));
				final ITypeInfo newSubFormObjectType = swingRenderer.getReflectionUI()
						.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(newSubFormObject));
				if (subFormObjectType.equals(newSubFormObjectType)) {
					swingRenderer.showBusyDialogWhile(this, new Runnable() {
						@Override
						public void run() {
							subFormObjectType.onFormVisibilityChange(subFormObject, false);
							if (newSubFormObject instanceof java.util.Date) {
								System.out.println("debug");
							}
							subForm.setObject(newSubFormObject);
							newSubFormObjectType.onFormVisibilityChange(newSubFormObject, true);
						}
					}, "Refreshing " + swingRenderer.getObjectTitle(newSubFormObject) + "...");
					subFormObject = newSubFormObject;
					subForm.refresh(refreshStructure);
				} else {
					return false;
				}
			}
		}
		if (refreshStructure) {
			handleSubFormModifications();
		}
		return true;
	}

	@Override
	public boolean isAutoManaged() {
		return true;
	}

	@Override
	public void validateSubForms() throws Exception {
		subForm.validateForm();
	}

	@Override
	public void addMenuContributions(MenuModel menuModel) {
		subForm.addMenuContributionTo(menuModel);
	}

	@Override
	public String toString() {
		return "EmbeddedFormControl [data=" + data + "]";
	}

}
