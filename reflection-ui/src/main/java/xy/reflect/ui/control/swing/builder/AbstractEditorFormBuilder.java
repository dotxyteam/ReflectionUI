
package xy.reflect.ui.control.swing.builder;

import java.util.Collections;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.ErrorOccurrence;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.EncapsulatedObjectFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.SlaveModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.MiscUtils;
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
	 *         object modification stack are fake (typically because the source
	 *         field is transient) or not.
	 */
	protected abstract boolean isParentModificationFake();

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

			Object object = ErrorOccurrence.tryCatch(new Accessor<Object>() {
				@Override
				public Object get() {
					initialObjectValue = null;
					return initialObjectValue = loadValue();
				}
			});

			@Override
			public Object get() {
				return ErrorOccurrence.rethrow(object);
			}

			@Override
			public void set(Object o) {
				object = o;
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
	 * @return whether the local object/value reference (or primitive value) has
	 *         changed. Typically immutable objects like primitive wrappers would be
	 *         replaced on every modification.
	 */
	public boolean isValueReplaced() {
		return getCurrentValue() != initialObjectValue;
	}

	/**
	 * @return the current local object/value.
	 */
	public Object getCurrentValue() {
		ensureIsInitialized();
		return encapsulatedObjectValueAccessor.get();
	}

	/**
	 * @return the capsule holding the local value/object.
	 */
	public Object getCapsule() {
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
				+ getSwingRenderer().getReflectionUI().buildTypeInfo(getEncapsulatedFieldTypeSource()).getName() + "]";
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
	 * @return the encapsulated field value return mode (equals to
	 *         {@link #getReturnModeFromParent()} is there is a parent object).
	 */
	protected ValueReturnMode getEncapsulatedFieldValueReturnMode() {
		return hasParentObject() ? getReturnModeFromParent() : ValueReturnMode.DIRECT_OR_PROXY;
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
		return new JavaTypeInfoSource(getSwingRenderer().getReflectionUI(), Object.class, null);
	}

	/**
	 * @return whether the editor control is refreshed every time a modification of
	 *         the local value/object is detected. It typically allows to keep a
	 *         calculated read-only local value/object coherent by resetting it
	 *         whenever it is modified.
	 */
	protected boolean isEditorFormRefreshedOnModification() {
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
		Object capsule = getCapsule();
		ITypeInfo capsuleType = getSwingRenderer().getReflectionUI()
				.buildTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(capsule));
		IFieldInfo encapsulatedField = capsuleType.getFields().get(0);
		if (encapsulatedField.isNullValueDistinct()) {
			return false;
		}
		Object object = getCurrentValue();
		if (object == null) {
			return false;
		}
		ITypeInfo actualObjectType = getSwingRenderer().getReflectionUI()
				.buildTypeInfo(getSwingRenderer().getReflectionUI().getTypeInfoSource(object));
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
		Object capsule = getCapsule();
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
	public void refreshEditorForm(Form editorForm, boolean refreshStructure) {
		if (refreshStructure) {
			initialized = false;
			ensureIsInitialized();
			editorForm.setObject(getCapsule());
		} else {
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
				ReflectionUI reflectionUI = getSwingRenderer().getReflectionUI();
				ITypeInfo oldValueType = (oldValue == null) ? null
						: reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(oldValue));
				ITypeInfo newValueType = (newValue == null) ? null
						: reflectionUI.buildTypeInfo(reflectionUI.getTypeInfoSource(newValue));
				if (!MiscUtils.equalsOrBothNull(oldValueType, newValueType)) {
					editorForm.setObject(getCapsule());
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
			if (isEditorFormRefreshedOnModification()) {
				refreshEditorFormOnModification(editorForm);
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
		return ReflectionUIUtils.mayModificationsHaveImpact(
				ReflectionUIUtils.isValueImmutable(getSwingRenderer().getReflectionUI(), initialObjectValue),
				getReturnModeFromParent(), canCommitToParent());
	}

	/**
	 * Installs a listener that will trigger the editor control refreshing whenever
	 * a modification of the local value/object is detected.
	 * 
	 * @param editorForm The created editor control.
	 */
	protected void refreshEditorFormOnModification(final Form editorForm) {
		ModificationStack childModificationStack = editorForm.getModificationStack();
		childModificationStack.addListener(new AbstractSimpleModificationListener() {
			@Override
			protected void handleAnyEvent(IModification modification) {
				refreshEditorForm(editorForm, false);
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
		Accessor<Boolean> masterModifFakeGetter = new Accessor<Boolean>() {
			@Override
			public Boolean get() {
				return isParentModificationFake();
			}
		};
		Listener<Throwable> masterModificationExceptionListener = new Listener<Throwable>() {
			@Override
			public void handle(Throwable t) {
				handleRealtimeLinkCommitException(t);
			}
		};
		editorForm.setModificationStack(new SlaveModificationStack(editorForm.toString(), childModifAcceptedGetter,
				childValueReturnModeGetter, childValueReplacedGetter, childValueTransactionExecutedGetter,
				committingModifGetter, masterModifTitleGetter, masterModifStackGetter, masterModifFakeGetter,
				exclusiveLinkWithParent, ReflectionUIUtils.getDebugLogListener(getSwingRenderer().getReflectionUI()),
				ReflectionUIUtils.getErrorLogListener(getSwingRenderer().getReflectionUI()),
				masterModificationExceptionListener));
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
					getSwingRenderer().getReflectionUI().buildTypeInfo(getEncapsulatedFieldTypeSource()));
			setFieldName(getEncapsulatedFieldName());
			setFieldCaption(getEncapsulatedFieldCaption());
			setFieldGetOnly(isEncapsulatedFieldGetOnly());
			setFieldNullValueDistinct(isNullValueDistinct());
			setFieldValueReturnMode(getEncapsulatedFieldValueReturnMode());
			setFieldFormControlEmbedded(isEncapsulatedFormEmbedded());
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
