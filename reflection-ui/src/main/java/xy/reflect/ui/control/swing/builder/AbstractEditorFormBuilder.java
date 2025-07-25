
package xy.reflect.ui.control.swing.builder;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;

import javax.swing.SwingUtilities;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.ErrorOccurrence;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.IValidationJob;
import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.SlaveModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.PrecomputedTypeInstanceWrapper;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This is a base class for form-based editor factories.
 * 
 * Each instance of this class handles a local value/object according to the
 * specifications provided through the implementation of the various methods.
 * 
 * Note that the local value/object is encapsulated in a virtual parent object
 * for practical reasons. The editor is thus a form representing this capsule.
 * 
 * This class also handles the complex relationship that may exists between the
 * local value/object and a potential parent object. The parent object form will
 * typically embed (real-time link) or maintain a detached parent-child
 * relationship (e.g.: child dialog) with the local value/object form. Note that
 * the local value/object modifications will be committed to the parent object
 * and forwarded so that they can be undone or redone through the parent
 * modification stack.
 * 
 * A real-time link with the parent object form can be exclusive, meaning that
 * the local value/object form is the only child of its parent object form. It
 * affects the 'undo' management.
 * 
 * @author olitank
 *
 */
public abstract class AbstractEditorFormBuilder {

	protected Object initialObjectValue;
	protected Object currentObjectValue;
	protected Object lastObjectValue;
	protected boolean initialized = false;
	protected Accessor<Object> encapsulatedObjectValueAccessor;

	/**
	 * @return the renderer used to create the local value/object form(s).
	 */
	public abstract SwingRenderer getSwingRenderer();

	/**
	 * @return the parent object modification stack or null (if there is no parent
	 *         object).
	 */
	protected abstract ModificationStack getParentModificationStack();

	/**
	 * @return the title (or the title prefix) of the modification(s) that will be
	 *         communicated to the parent object modification stack.
	 */
	protected abstract String getParentModificationTitle();

	/**
	 * @return whether the modifications that will be communicated to the parent
	 *         object modification stack are volatile (see
	 *         {@link IModification#isVolatile()}) (typically because the source
	 *         field is transient) or not.
	 */
	protected abstract boolean isParentModificationVolatile();

	/**
	 * @return whether modifications of the local value/object can be committed
	 *         (using the result of {@link #createCommittingModification(Object)})
	 *         to make them real for the parent object.
	 */
	protected abstract boolean canCommitToParent();

	/**
	 * @param newObjectValue
	 * @return a modification (may be null) that will be applied in order to make
	 *         any local value/object modification real for the parent object.
	 *         Typically primitive field values would need to be committed (set
	 *         back) to their parent object after modification.
	 */
	protected abstract IModification createCommittingModification(Object newObjectValue);

	/**
	 * @return a modification (may be null) that will be applied in order to revert
	 *         modifications of the parent object. If null is returned then the
	 *         default undo modifications will be used.
	 */
	protected abstract IModification createUndoModificationsReplacement();

	/**
	 * Processes exceptions thrown when the local value/object is committed to the
	 * parent object through a real-time link.
	 * 
	 * @param t The thrown exception.
	 */
	protected abstract void handleRealtimeLinkCommitException(Throwable t);

	/**
	 * @return the source of the type information that will be used to handle the
	 *         local value/object. If null is returned then this type information
	 *         source will be dynamically inferred from the local value/object.
	 */
	protected abstract ITypeInfoSource getEncapsulatedFieldDeclaredTypeSource();

	/**
	 * @return the return mode (from the parent object) of the local value/object.
	 */
	protected abstract ValueReturnMode getReturnModeFromParent();

	/**
	 * @return a {@link Runnable} instance (or null) that can be used to refresh an
	 *         eventual parent control.
	 */
	protected abstract Runnable getParentControlRefreshJob();

	/**
	 * @return true if and only if the null value can be set and then must be
	 *         distinctly displayed.
	 */
	protected abstract boolean isNullValueDistinct();

	/**
	 * @return the initial local object/value.
	 */
	protected abstract Object loadValue();

	/**
	 * @return an object that will be used to uniquely name the capsule type (may be
	 *         null).
	 */
	protected abstract IContext getContext();

	/**
	 * @return an secondary object that will be used to uniquely name the capsule
	 *         type (may be null).
	 */
	protected abstract IContext getSubContext();

	/**
	 * Ensures that the initial local value/object has been acquired.
	 */
	protected void ensureIsInitialized() {
		if (initialized) {
			return;
		}
		encapsulatedObjectValueAccessor = new Accessor<Object>() {
			{
				currentObjectValue = ErrorOccurrence.tryCatch(new Accessor<Object>() {
					@Override
					public Object get() {
						lastObjectValue = (initialObjectValue = null);
						return lastObjectValue = (initialObjectValue = loadValue());
					}
				});
			}

			@Override
			public Object get() {
				return ErrorOccurrence.rethrow(currentObjectValue);
			}

			@Override
			public void set(Object value) {
				lastObjectValue = currentObjectValue;
				currentObjectValue = value;
			}

		};
		initialized = true;
	}

	/**
	 * @return whether the initial local value/object has been acquired or not.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * @return whether the local object/value reference has changed. Typically
	 *         immutable objects like primitive wrappers would be replaced on every
	 *         modification.
	 */
	public boolean isValueReplaced() {
		return getCurrentValue() != lastObjectValue;
	}

	/**
	 * @return the current local object/value.
	 */
	public Object getCurrentValue() {
		ensureIsInitialized();
		return encapsulatedObjectValueAccessor.get();
	}

	/**
	 * @return a new capsule holding the local value/object.
	 */
	public Object getNewCapsule() {
		ensureIsInitialized();
		return createEncapsulation().getInstance(encapsulatedObjectValueAccessor);
	}

	/**
	 * @return the name of the capsule type.
	 */
	protected String getCapsuleTypeName() {
		String contextDeclaraion;
		{
			IContext context = getContext();
			if (context == null) {
				contextDeclaraion = "";
			} else {
				contextDeclaraion = "context=" + context.getIdentifier() + ", ";
			}
		}
		String subContextDeclaraion;
		{
			IContext subContext = getSubContext();
			if (subContext == null) {
				subContextDeclaraion = "";
			} else {
				subContextDeclaraion = "subContext=" + subContext.getIdentifier() + ", ";
			}
		}
		return "Encapsulation [" + contextDeclaraion + subContextDeclaraion + "encapsulatedObjectType="
				+ getSwingRenderer().getReflectionUI().getTypeInfo(getEncapsulatedFieldTypeSource()).getName() + "]";
	}

	/**
	 * @return the local value/object capsule factory.
	 */
	public EncapsulatedObjectFactory createEncapsulation() {
		return new EditorEncapsulation();
	}

	/**
	 * @return a form filter that will used (in case the local value/object is
	 *         represented by a generic form control).
	 */
	protected abstract IInfoFilter getEncapsulatedFormFilter();

	/**
	 * @return whether the local object/value form is embedded in the editor control
	 *         or displayed in a child dialog. Note that this method has no impact
	 *         in case the local value/object is not represented by a generic form
	 *         control.
	 */
	protected abstract boolean isEncapsulatedFormEmbedded();

	/**
	 * @return whether validation errors should be checked for the local
	 *         object/value.
	 */
	protected abstract boolean isEncapsulatedisControlValueValiditionEnabled();

	/**
	 * @return true if the local value/object must be displayed as a generic form
	 *         (not a custom control).
	 */
	protected boolean isCustomEncapsulatedControlForbidden() {
		return false;
	}

	/**
	 * @return the name of the encapsulated virtual field that will return local
	 *         value/object.
	 */
	protected String getEncapsulatedFieldName() {
		return "";
	}

	/**
	 * @return the specific properties of the encapsulated field information.
	 */
	protected Map<String, Object> getEncapsulatedFieldSpecificProperties() {
		return Collections.emptyMap();
	}

	/**
	 * @return the encapsulated field value return mode.
	 *         {@link ValueReturnMode.#DIRECT} ({@link ValueReturnMode.#CALCULATED}
	 *         for primitives) is returned by default, since this encapsulated field
	 *         value is hosted by the {@link #encapsulatedObjectValueAccessor} and
	 *         accessed directly.
	 */
	protected ValueReturnMode getEncapsulatedFieldValueReturnMode() {
		return getSwingRenderer().getReflectionUI().getTypeInfo(getEncapsulatedFieldTypeSource()).isPrimitive()
				? ValueReturnMode.CALCULATED
				: ValueReturnMode.DIRECT;
	}

	/**
	 * @return whether the encapsulated field value has options. If true then
	 *         {@link #getEncapsulatedFieldValueOptions()} must not return null.
	 */
	protected boolean hasEncapsulatedFieldValueOptions() {
		return false;
	}

	/**
	 * @return the encapsulated field value options. If
	 *         {@link #hasEncapsulatedFieldValueOptions()} returns true then this
	 *         method must not return null.
	 */
	protected Object[] getEncapsulatedFieldValueOptions() {
		return null;
	}

	/**
	 * @return whether the encapsulated field is get-only. Note that an encapsulated
	 *         get-only field does not prevent all modifications. The encapsulated
	 *         field value may be modified and these modifications may be volatile
	 *         (for calculated values, copies, ..) or persistent even if the new
	 *         encapsulated field value is not set.
	 */
	protected boolean isEncapsulatedFieldGetOnly() {
		return isInReadOnlyMode() || (hasParentObject() && !canCommitToParent());
	}

	/**
	 * @return the caption of the encapsulated virtual field that will return local
	 *         value/object.
	 */
	protected String getEncapsulatedFieldCaption() {
		return "";
	}

	/**
	 * @return the source of the type information that is used to qualify the
	 *         encapsulated virtual field that returns the local value/object.
	 */
	protected ITypeInfoSource getEncapsulatedFieldTypeSource() {
		ITypeInfoSource result = getEncapsulatedFieldDeclaredTypeSource();
		if (result != null) {
			return result;
		}
		ensureIsInitialized();
		if (initialObjectValue != null) {
			return getSwingRenderer().getReflectionUI().getTypeInfoSource(initialObjectValue);
		}
		return new JavaTypeInfoSource(Object.class, null);
	}

	/**
	 * @return whether the editor value is reloaded and its control refreshed every
	 *         time a modification of the local value/object is detected. It
	 *         typically allows to keep a calculated read-only local value/object
	 *         coherent by resetting it whenever it is modified.
	 */
	protected boolean isValueReloadedOnModification() {
		return isInReadOnlyMode();
	}

	/**
	 * @return true if the local value/object modifications does not impact the
	 *         parent object. If there is no parent object then false should be
	 *         returned.
	 */
	protected boolean isInReadOnlyMode() {
		return hasParentObject() ? !mayModifyParentObject() : false;
	}

	/**
	 * @return whether the editor control is empty or not. It can be checked before
	 *         actually creating the editor control.
	 */
	public boolean isFormEmpty() {
		Object capsule = getNewCapsule();
		ITypeInfo capsuleType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(capsule));
		IFieldInfo encapsulatedField = capsuleType.getFields().get(0);
		if (encapsulatedField.isNullValueDistinct()) {
			return false;
		}
		Object object = getCurrentValue();
		if (object == null) {
			return false;
		}
		ITypeInfo actualObjectType = getSwingRenderer().getReflectionUI()
				.getTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(object));
		if (!ReflectionUIUtils.isTypeEmpty(actualObjectType, getEncapsulatedFormFilter())) {
			return false;
		}
		return true;
	}

	/**
	 * Creates and return the editor control.
	 * 
	 * @param realTimeLinkWithParent  Whether a real-time link should be maintained
	 *                                with the parent object.
	 * @param exclusiveLinkWithParent Whether the real-time link with the parent
	 *                                object (if existing) should be exclusive or
	 *                                not.
	 * @return the created editor control.
	 */
	public Form createEditorForm(boolean realTimeLinkWithParent, boolean exclusiveLinkWithParent) {
		Object capsule = getNewCapsule();
		Form result = getSwingRenderer().createForm(capsule);
		setupLinkWithParent(result, realTimeLinkWithParent, exclusiveLinkWithParent);
		return result;
	}

	/**
	 * Reloads the local object/value and refreshes the editor control.
	 * 
	 * @param editorForm       The created editor control.
	 * @param refreshStructure Whether the editor control should update its
	 *                         structure to reflect the recent meta-data changes
	 *                         (mainly used in design mode).
	 */
	public void reloadValue(Form editorForm, boolean refreshStructure) {
		if (refreshStructure) {
			initialized = false;
			ensureIsInitialized();
			editorForm.setObject(getNewCapsule());
		} else {
			ensureIsInitialized();
			Object oldValue = ErrorOccurrence.tryCatch(new Accessor<Object>() {
				@Override
				public Object get() {
					return encapsulatedObjectValueAccessor.get();
				}
			});
			Object newValue = ErrorOccurrence.tryCatch(new Accessor<Object>() {
				@Override
				public Object get() {
					return loadValue();
				}
			});
			if (oldValue != newValue) {
				encapsulatedObjectValueAccessor.set(newValue);
				refreshStructure = true;
				ReflectionUI reflectionUI = getSwingRenderer().getReflectionUI();
				ITypeInfo oldValueType = (oldValue == null) ? null
						: reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(oldValue));
				ITypeInfo newValueType = (newValue == null) ? null
						: reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(newValue));
				if (MiscUtils.equalsOrBothNull(oldValueType, newValueType)) {
					EncapsulatedObjectFactory oldEncapsulation = ((EncapsulatedObjectFactory.TypeInfo) ((PrecomputedTypeInstanceWrapper) editorForm
							.getObject()).getPrecomputedType()).getFactory();
					EncapsulatedObjectFactory newEncapsulation = ((EncapsulatedObjectFactory.TypeInfo) ((PrecomputedTypeInstanceWrapper) getNewCapsule())
							.getPrecomputedType()).getFactory();
					if (oldEncapsulation.equals(newEncapsulation)) {
						refreshStructure = false;
					}
				}
				if (refreshStructure) {
					editorForm.setObject(getNewCapsule());
				}
			}
		}
		editorForm.refresh(refreshStructure);
	}

	/**
	 * Installs the link between the local value/object editor control and its
	 * parent object form.
	 * 
	 * @param editorForm              The created local value/object editor control.
	 * @param realTimeLinkWithParent  Whether a real-time link should be maintained
	 *                                with the parent object.
	 * @param exclusiveLinkWithParent Whether the real-time link with the parent
	 *                                object (if existing) should be exclusive or
	 *                                not.
	 */
	protected void setupLinkWithParent(Form editorForm, boolean realTimeLinkWithParent,
			boolean exclusiveLinkWithParent) {
		if (realTimeLinkWithParent) {
			if (mayModifyParentObject()) {
				forwardEditorFormModificationsToParentObject(editorForm, exclusiveLinkWithParent);
			}
			if (isValueReloadedOnModification()) {
				reloadValueOnModification(editorForm);
			}
		}
	}

	/**
	 * @return whether the local value/object has a parent object or not.
	 */
	protected boolean hasParentObject() {
		return getParentModificationStack() != null;
	}

	/**
	 * @return whether modifications of the local value/object may impact the parent
	 *         object.
	 */
	public boolean mayModifyParentObject() {
		if (!hasParentObject()) {
			return false;
		}
		ensureIsInitialized();
		return ReflectionUIUtils.mayModificationsHaveImpact(isValueKnownAsImmutable(), getReturnModeFromParent(),
				canCommitToParent());
	}

	/**
	 * @return whether the local value/object can be modified or not. If there is a
	 *         doubt then false should be returned.
	 */
	protected boolean isValueKnownAsImmutable() {
		return ReflectionUIUtils.isValueImmutable(getSwingRenderer().getReflectionUI(), initialObjectValue);
	}

	/**
	 * Installs a listener that will trigger the editor value reloading and control
	 * refreshing whenever a modification of the local value/object is detected.
	 * 
	 * @param editorForm The created editor control.
	 */
	protected void reloadValueOnModification(final Form editorForm) {
		ModificationStack childModificationStack = editorForm.getModificationStack();
		childModificationStack.addListener(new AbstractSimpleModificationListener() {
			@Override
			protected void handleAnyEvent(IModification modification) {
				reloadValue(editorForm, false);
			}
		});
	}

	/**
	 * @param value The new local value.
	 * @return whether the new local value passed as argument should be integrated
	 *         in the parent object or not, typically because it was accepted or
	 *         rejected by a user.
	 */
	protected boolean shouldIntegrateNewObjectValue(Object value) {
		return true;
	}

	/**
	 * Installs on the editor control a listener that will forward the local
	 * value/object modifications to the parent object modification stack.
	 * 
	 * @param editorForm              The created editor control.
	 * @param exclusiveLinkWithParent Whether the real-time link with the parent
	 *                                object (if existing) is exclusive or not.
	 */
	protected void forwardEditorFormModificationsToParentObject(final Form editorForm,
			boolean exclusiveLinkWithParent) {
		Accessor<Boolean> childModifAcceptedGetter = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return shouldIntegrateNewObjectValue(getCurrentValue());
			}
		};
		Accessor<ValueReturnMode> childValueReturnModeGetter = new Accessor<ValueReturnMode>() {
			@Override
			public ValueReturnMode get() {
				return getReturnModeFromParent();
			}
		};
		Accessor<Boolean> childValueReplacedGetter = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return isValueReplaced();
			}
		};
		Accessor<Boolean> childValueTransactionExecutedGetter = Accessor.returning(false);
		Accessor<IModification> committingModifGetter = new Accessor<IModification>() {
			@Override
			public IModification get() {
				if (!canCommitToParent()) {
					return null;
				}
				return createCommittingModification(getCurrentValue());
			}
		};
		Accessor<IModification> undoModificationsReplacementGetter = new Accessor<IModification>() {
			@Override
			public IModification get() {
				return createUndoModificationsReplacement();
			}
		};
		Accessor<String> masterModifTitleGetter = new Accessor<String>() {
			@Override
			public String get() {
				return getParentModificationTitle();
			}
		};
		Accessor<ModificationStack> masterModifStackGetter = new Accessor<ModificationStack>() {
			@Override
			public ModificationStack get() {
				ModificationStack result = getParentModificationStack();
				if (result == null) {
					throw new ReflectionUIError();
				}
				return result;
			}
		};
		Accessor<Boolean> masterModifVolatileGetter = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return isParentModificationVolatile();
			}
		};
		Accessor<Runnable> parentControlRefreshJobGetter = new Accessor<Runnable>() {
			@Override
			public Runnable get() {
				return getParentControlRefreshJob();
			}
		};
		Listener<Throwable> masterModificationExceptionListener = new Listener<Throwable>() {
			@Override
			public void handle(Throwable t) {
				handleRealtimeLinkCommitException(t);
			}
		};
		SlaveModificationStack slaveModificationStack = new SlaveModificationStack(editorForm.toString(),
				childModifAcceptedGetter, childValueReturnModeGetter, childValueReplacedGetter,
				childValueTransactionExecutedGetter, committingModifGetter, undoModificationsReplacementGetter,
				masterModifTitleGetter, masterModifStackGetter, masterModifVolatileGetter,
				parentControlRefreshJobGetter, exclusiveLinkWithParent,
				ReflectionUIUtils.getDebugLogListener(getSwingRenderer().getReflectionUI()),
				ReflectionUIUtils.getErrorLogListener(getSwingRenderer().getReflectionUI()),
				masterModificationExceptionListener);
		if (editorForm.getModificationStack() != null) {
			for (IModificationListener listener : editorForm.getModificationStack().getListeners()) {
				slaveModificationStack.addSlaveListener(listener);
			}
		}
		editorForm.setModificationStack(slaveModificationStack);
	}

	/**
	 * Validates the local value/object. The validation is performed by the job
	 * returned by {@link #getValueAbstractFormValidationJob()} if not null.
	 * Otherwise the validation is done by using either the
	 * {@link ITypeInfo#validate(Object, ValidationSession)} method associated with
	 * the current local value/object when there is no specific control-based
	 * validation, or by using a temporary concrete form that will orchestrate the
	 * validation.
	 * 
	 * @param session If the state of the underlying object is not valid.
	 * @throws Exception If the state of the underlying object is not valid.
	 */
	public void performHeadlessFormValidation(ValidationSession session) throws Exception {
		SwingRenderer swingRenderer = getSwingRenderer();
		IValidationJob validationJob;
		IValidationJob abstractFormValidationJob = getValueAbstractFormValidationJob();
		if (abstractFormValidationJob == null) {
			ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
			final ITypeInfo valueType;
			if (getCurrentValue() == null) {
				valueType = reflectionUI.getTypeInfo(getEncapsulatedFieldTypeSource());
			} else {
				valueType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(getCurrentValue()));
			}
			if (!valueType.getFields().stream().anyMatch(IFieldInfo::isControlValueValiditionEnabled)
					&& !valueType.getMethods().stream().anyMatch(IMethodInfo::isControlReturnValueValiditionEnabled)) {
				abstractFormValidationJob = (sessionArg) -> valueType.validate(getCurrentValue(), sessionArg);
			}
		}
		if (abstractFormValidationJob != null) {
			/*
			 * Manage validation error attribution since it will not be managed
			 * automatically through a concrete form validation.
			 */
			validationJob = swingRenderer.getReflectionUI().getValidationErrorRegistry().attributing(getCurrentValue(),
					abstractFormValidationJob);
		} else {
			Form[] form = new Form[1];
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						form[0] = createEditorForm(false, false);
					}
				});
			} catch (InvocationTargetException e) {
				throw new ReflectionUIError(e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			validationJob = (sessionArg) -> form[0].validateForm(sessionArg);
		}
		validationJob.validate(session);
	}

	/**
	 * Behaves like {@link IFieldControlData#getValueAbstractFormValidationJob()}.
	 * 
	 * @return the value corresponding to the behavior described above. Note that
	 *         null is returned unless this method is overriden
	 */
	protected IValidationJob getValueAbstractFormValidationJob() {
		return null;
	}

	/**
	 * Factory used to encapsulate the local value/object for practical reasons.
	 * 
	 * @author olitank
	 *
	 */
	public class EditorEncapsulation extends EncapsulatedObjectFactory {

		public EditorEncapsulation() {
			super(getSwingRenderer().getReflectionUI(), getCapsuleTypeName(),
					getSwingRenderer().getReflectionUI().getTypeInfo(getEncapsulatedFieldTypeSource()));
			setFieldName(getEncapsulatedFieldName());
			setFieldCaption(getEncapsulatedFieldCaption());
			setFieldGetOnly(isEncapsulatedFieldGetOnly());
			setFieldNullValueDistinct(isNullValueDistinct());
			setFieldValueReturnMode(getEncapsulatedFieldValueReturnMode());
			setFieldFormControlEmbedded(isEncapsulatedFormEmbedded());
			setFieldValueValidityDetectionEnabled(isEncapsulatedisControlValueValiditionEnabled());
			setFieldFormControlFilter(getEncapsulatedFormFilter());
			setFieldFormControlMandatory(isCustomEncapsulatedControlForbidden());
			setFieldSpecificProperties(getEncapsulatedFieldSpecificProperties());
		}

		public AbstractEditorFormBuilder getBuilder() {
			return AbstractEditorFormBuilder.this;
		}

		@Override
		protected boolean hasFieldValueOptions() {
			return hasEncapsulatedFieldValueOptions();
		}

		@Override
		protected Object[] getFieldValueOptions() {
			return getEncapsulatedFieldValueOptions();
		}

	}

}
