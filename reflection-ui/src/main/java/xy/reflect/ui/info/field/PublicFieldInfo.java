
package xy.reflect.ui.info.field;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.AbstractInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field information extracted from a Java field.
 * 
 * Note that a unique suffix may be added to the field name to avoid collisions
 * when there are multiple fields with the same name accessible from the same
 * class.
 * 
 * @author olitank
 *
 */
public class PublicFieldInfo extends AbstractInfo implements IFieldInfo {

	protected Field javaField;
	protected ReflectionUI reflectionUI;
	protected Class<?> objectJavaClass;
	protected int duplicateNameIndex = -1;
	protected String name;
	protected String caption;

	public PublicFieldInfo(ReflectionUI reflectionUI, Field field, Class<?> objectJavaClass) {
		this.reflectionUI = reflectionUI;
		this.javaField = field;
		this.objectJavaClass = objectJavaClass;
		resolveJavaReflectionModelAccessProblems();
	}

	protected void resolveJavaReflectionModelAccessProblems() {
		try {
			javaField.setAccessible(true);
		} catch (Throwable t) {
			reflectionUI.logDebug(t);
		}
	}

	public Field getJavaField() {
		return javaField;
	}

	@Override
	public String getName() {
		if (name == null) {
			name = javaField.getName();
			int index = getDuplicateNameIndex(javaField);
			if (index > 0) {
				name += "." + Integer.toString(index);
			}
		}
		return name;
	}

	protected int getDuplicateNameIndex(Field javaField) {
		if (duplicateNameIndex == -1) {
			duplicateNameIndex = 0;
			for (Field otherField : javaField.getDeclaringClass().getFields()) {
				if (otherField.getName().equals(javaField.getName())) {
					if (!otherField.equals(javaField)) {
						// other field with same name forcibly declared in base class
						duplicateNameIndex += 1;
					}
				}
			}
		}
		return duplicateNameIndex;
	}

	@Override
	public String getCaption() {
		if (caption == null) {
			caption = ReflectionUIUtils.identifierToCaption(javaField.getName());
			int index = getDuplicateNameIndex(javaField);
			if (index > 0) {
				caption += " (" + (index + 1) + ")";
			}
		}
		return caption;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public double getDisplayAreaHorizontalWeight() {
		return 1.0;
	}

	@Override
	public double getDisplayAreaVerticalWeight() {
		return 0.0;
	}

	@Override
	public boolean isDisplayAreaHorizontallyFilled() {
		return true;
	}

	@Override
	public boolean isDisplayAreaVerticallyFilled() {
		return false;
	}

	@Override
	public void onControlVisibilityChange(Object object, boolean visible) {
	}

	@Override
	public void setValue(Object object, Object value) {
		try {
			javaField.set(object, value);
		} catch (IllegalArgumentException e) {
			throw new ReflectionUIError(e);
		} catch (IllegalAccessException e) {
			throw new ReflectionUIError(e);
		}
	}

	@Override
	public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
		return null;
	}

	@Override
	public Runnable getPreviousUpdateCustomRedoJob(Object object, Object newValue) {
		return null;
	}

	@Override
	public Object getValue(Object object) {
		try {
			return javaField.get(object);
		} catch (IllegalArgumentException e) {
			throw new ReflectionUIError(e);
		} catch (IllegalAccessException e) {
			throw new ReflectionUIError(e);
		}
	}

	@Override
	public boolean hasValueOptions(Object object) {
		return false;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public ITypeInfo getType() {
		return reflectionUI.getTypeInfo(new JavaTypeInfoSource(javaField.getType(), javaField, -1,
				new SpecificitiesIdentifier(
						reflectionUI.getTypeInfo(new JavaTypeInfoSource(objectJavaClass, null)).getName(),
						javaField.getName())));
	}

	@Override
	public List<IMethodInfo> getAlternativeConstructors(Object object) {
		return null;
	}

	@Override
	public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
		return null;
	}

	@Override
	public String getNullValueLabel() {
		return null;
	}

	@Override
	public boolean isNullValueDistinct() {
		return false;
	}

	@Override
	public boolean isGetOnly() {
		return Modifier.isFinal(javaField.getModifiers());
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	@Override
	public ValueReturnMode getValueReturnMode() {
		return ValueReturnMode.DIRECT_OR_PROXY;
	}

	public static boolean isCompatibleWith(Field field) {
		return true;
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public boolean isFormControlMandatory() {
		return false;
	}

	@Override
	public boolean isFormControlEmbedded() {
		return false;
	}

	@Override
	public IInfoFilter getFormControlFilter() {
		return IInfoFilter.DEFAULT;
	}

	@Override
	public long getAutoUpdatePeriodMilliseconds() {
		return -1;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((javaField == null) ? 0 : javaField.hashCode());
		result = prime * result + ((objectJavaClass == null) ? 0 : objectJavaClass.hashCode());
		result = prime * result + ((reflectionUI == null) ? 0 : reflectionUI.hashCode());
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
		PublicFieldInfo other = (PublicFieldInfo) obj;
		if (javaField == null) {
			if (other.javaField != null)
				return false;
		} else if (!javaField.equals(other.javaField))
			return false;
		if (objectJavaClass == null) {
			if (other.objectJavaClass != null)
				return false;
		} else if (!objectJavaClass.equals(other.objectJavaClass))
			return false;
		if (reflectionUI == null) {
			if (other.reflectionUI != null)
				return false;
		} else if (!reflectionUI.equals(other.reflectionUI))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PublicFieldInfo [javaField=" + javaField + ", objectJavaClass=" + objectJavaClass + "]";
	}

};
