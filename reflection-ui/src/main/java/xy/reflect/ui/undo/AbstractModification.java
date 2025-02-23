
package xy.reflect.ui.undo;

import xy.reflect.ui.util.ReflectionUIError;

/**
 * This class exists as convenience for creating {@link IModification} objects.
 * 
 * @author olitank
 *
 */
public abstract class AbstractModification implements IModification {

	protected Runnable doJob;
	protected Runnable undoJob;
	protected Runnable redoJob;
	protected static final String UNDO_TITLE_PREFIX = "(Revert) ";

	/**
	 * @return The task that performs the modification for the first time. Must not
	 *         be null.
	 */
	protected abstract Runnable createDoJob();

	/**
	 * @return The task that reverts the modification. Must not be null.
	 */
	protected abstract Runnable createUndoJob();

	/**
	 * @return The task that performs again the modification. Must not be null.
	 */
	protected abstract Runnable createRedoJob();

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public boolean isFake() {
		return false;
	}

	@Override
	public boolean isComposite() {
		return false;
	}

	@Override
	public IModification applyAndGetOpposite(ModificationStack modificationStack) {
		if (doJob == null) {
			doJob = createDoJob();
			if (doJob == null) {
				throw new ReflectionUIError();
			}
		}
		try {
			if (undoJob == null) {
				undoJob = createUndoJob();
				if (undoJob == null) {
					throw new IrreversibleModificationException();
				}
			}
		} finally {
			if (redoJob != null) {
				redoJob.run();
			} else {
				doJob.run();
			}
		}
		return new OppositeModification();
	}

	@Override
	public String toString() {
		return "Modification [title=" + getTitle() + "]";
	}

	public static String getUndoTitle(String title) {
		String result;
		if (title == null) {
			result = null;
		} else if (title.startsWith(AbstractModification.UNDO_TITLE_PREFIX)) {
			result = title.substring(AbstractModification.UNDO_TITLE_PREFIX.length());
		} else {
			result = AbstractModification.UNDO_TITLE_PREFIX + title;
		}
		return result;
	}

	public class OppositeModification implements IModification {

		@Override
		public boolean isNull() {
			return getSourceModification().isNull();
		}

		@Override
		public boolean isFake() {
			return getSourceModification().isFake();
		}

		@Override
		public boolean isComposite() {
			return getSourceModification().isComposite();
		}

		@Override
		public String getTitle() {
			return AbstractModification.getUndoTitle(AbstractModification.this.getTitle());
		}

		public AbstractModification getSourceModification() {
			return AbstractModification.this;
		}

		@Override
		public IModification applyAndGetOpposite(ModificationStack modificationStack) {
			if (getSourceModification().toString().equals(
					"FieldControlDataModification [data=FieldControlDataProxy [base=InitialFieldControlData [of=FieldControlPlaceHolder [field=GeneratedFieldInfoProxy [name=fieldValueMode, factory=FilteredTypeFactory [infoFilter=InfoFilterProxy [base=xy.reflect.ui.info.filter.IInfoFilter.DEFAULT]], base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=com.otk.jesb.GUI$JESBReflectionUI$2], base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=SpecificitiesFactory [of=com.otk.jesb.GUI$JESBReflectionUI@456d6c1e, specificitiesIdentifier=SpecificitiesIdentifier [objectTypeName=com.otk.jesb.instantiation.FieldInitializerFacade, fieldName=valueGroup]]], base=FieldInfoProxy [name=fieldValueMode, base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=CustomizationsFactory [of=com.otk.jesb.GUI$JESBReflectionUI@456d6c1e]], base=FieldInfoProxy [name=fieldValueMode, base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=com.otk.jesb.GUI$JESBReflectionUI$1], base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=CustomizationsSetupFactory [of=com.otk.jesb.GUI$JESBReflectionUI@456d6c1e]], base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=PrecomputedTypeInstanceWrapping [type=CapsuleFieldType [context=EncapsulationContext [objectType=com.otk.jesb.instantiation.FieldInitializerFacade], fieldName=valueGroup]]], base=FieldInfoProxy [name=fieldValueMode, base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=com.otk.jesb.GUI$JESBReflectionUI$1], base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=CustomizationsSetupFactory [of=com.otk.jesb.GUI$JESBReflectionUI@456d6c1e]], base=GetterFieldInfo [javaGetterMethod=public com.otk.jesb.instantiation.ValueMode com.otk.jesb.instantiation.FieldInitializerFacade.getFieldValueMode()]]]]]]]]]]]]], form=Form [id=839950301, object=PrecomputedTypeInstanceWrapper [instance=CapsuleFieldFieldInstance [object=stringValue], precomputedType=ValueTypeInfo [of=CapsuleField [fieldName=valueGroup, objectType=GeneratedBasicTypeInfoProxy [name=com.otk.jesb.instantiation.FieldInitializerFacade, factory=TypeInfoProxyFactory [id=com.otk.jesb.GUI$JESBReflectionUI$1], base=GeneratedBasicTypeInfoProxy [name=com.otk.jesb.instantiation.FieldInitializerFacade, factory=TypeInfoProxyFactory [id=CustomizationsSetupFactory [of=com.otk.jesb.GUI$JESBReflectionUI@456d6c1e]], base=DefaultTypeInfo [source=JavaTypeInfoSource [javaType=class com.otk.jesb.instantiation.FieldInitializerFacade, declaringMember=null, declaringInvokableParameterPosition=0, genericTypeParameters=null, specificitiesIdentifier=null]]]], fields=[GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=com.otk.jesb.GUI$JESBReflectionUI$1], base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=CustomizationsSetupFactory [of=com.otk.jesb.GUI$JESBReflectionUI@456d6c1e]], base=GetterFieldInfo [javaGetterMethod=public com.otk.jesb.instantiation.ValueMode com.otk.jesb.instantiation.FieldInitializerFacade.getFieldValueMode()]]], GeneratedFieldInfoProxy [name=fieldValue, factory=TypeInfoProxyFactory [id=com.otk.jesb.GUI$JESBReflectionUI$1], base=GeneratedFieldInfoProxy [name=fieldValue, factory=TypeInfoProxyFactory [id=CustomizationsSetupFactory [of=com.otk.jesb.GUI$JESBReflectionUI@456d6c1e]], base=GetterFieldInfo [javaGetterMethod=public java.lang.Object com.otk.jesb.instantiation.FieldInitializerFacade.getFieldValue()]]]], methods=[]]]]]], finalField=GeneratedFieldInfoProxy [name=fieldValueMode, factory=FilteredTypeFactory [infoFilter=InfoFilterProxy [base=xy.reflect.ui.info.filter.IInfoFilter.DEFAULT]], base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=com.otk.jesb.GUI$JESBReflectionUI$2], base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=SpecificitiesFactory [of=com.otk.jesb.GUI$JESBReflectionUI@456d6c1e, specificitiesIdentifier=SpecificitiesIdentifier [objectTypeName=com.otk.jesb.instantiation.FieldInitializerFacade, fieldName=valueGroup]]], base=FieldInfoProxy [name=fieldValueMode, base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=CustomizationsFactory [of=com.otk.jesb.GUI$JESBReflectionUI@456d6c1e]], base=FieldInfoProxy [name=fieldValueMode, base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=com.otk.jesb.GUI$JESBReflectionUI$1], base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=CustomizationsSetupFactory [of=com.otk.jesb.GUI$JESBReflectionUI@456d6c1e]], base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=PrecomputedTypeInstanceWrapping [type=CapsuleFieldType [context=EncapsulationContext [objectType=com.otk.jesb.instantiation.FieldInitializerFacade], fieldName=valueGroup]]], base=FieldInfoProxy [name=fieldValueMode, base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=com.otk.jesb.GUI$JESBReflectionUI$1], base=GeneratedFieldInfoProxy [name=fieldValueMode, factory=TypeInfoProxyFactory [id=CustomizationsSetupFactory [of=com.otk.jesb.GUI$JESBReflectionUI@456d6c1e]], base=GetterFieldInfo [javaGetterMethod=public com.otk.jesb.instantiation.ValueMode com.otk.jesb.instantiation.FieldInitializerFacade.getFieldValueMode()]]]]]]]]]]]]]]], newValue=PLAIN]")) {
				System.out.println("debug");
			}
			if (redoJob == null) {
				redoJob = createRedoJob();
				if (redoJob == null) {
					throw new ReflectionUIError();
				}
			}
			undoJob.run();
			return getSourceModification();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getSourceModification().hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OppositeModification other = (OppositeModification) obj;
			if (!getSourceModification().equals(other.getSourceModification()))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "OppositeModification [soure=" + getSourceModification() + "]";
		}

	}
}
