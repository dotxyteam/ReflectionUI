package xy.reflect.ui.info.type;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import xy.reflect.ui.info.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.InfoCategory;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.parameter.ParameterInfoProxy;
import xy.reflect.ui.info.type.IListTypeInfo.IListStructuralInfo;

public class TypeInfoProxy {

	public ITypeInfo get(final ITypeInfo type) {
		if (type instanceof IListTypeInfo) {
			return new ListTypeInfoProxy((IListTypeInfo) type);
		} else if (type instanceof IEnumerationTypeInfo) {
			return new EnumerationTypeInfoProxy((IEnumerationTypeInfo) type);
		} else if (type instanceof IBooleanTypeInfo) {
			return new BooleanTypeInfoProxy((IBooleanTypeInfo) type);
		} else if (type instanceof ITextualTypeInfo) {
			return new TextualTypeInfoProxy((ITextualTypeInfo) type);
		} else if (type instanceof FileTypeInfo) {
			return new FileTypeInfoProxy((FileTypeInfo) type);
		} else {
			return new BasicTypeInfoProxy(type);
		}
	}

	private IMethodInfo getMethodProxy(final IMethodInfo method,
			final ITypeInfo containingType) {
		return new MethodInfoProxy(method) {

			@Override
			public String getName() {
				return getMethodName(method, containingType);
			}

			@Override
			public String getCaption() {
				return getMethodCaption(method, containingType);
			}

			@Override
			public ITypeInfo getReturnValueType() {
				return getMethodReturnValueType(method, containingType);
			}

			@Override
			public List<IParameterInfo> getParameters() {
				return getMethodParameters(method, containingType);
			}

			@Override
			public Object invoke(Object object,
					Map<String, Object> valueByParameterName) {
				return invokeMethod(object, valueByParameterName, method,
						containingType);
			}

			@Override
			public boolean isReadOnly() {
				return isMethodReadOnly(method, containingType);
			}

			@Override
			public InfoCategory getCategory() {
				return getMethodCategory(method, containingType);
			}

		};
	}

	private IFieldInfo getFieldProxy(final IFieldInfo field,
			final ITypeInfo containingType) {
		return new FieldInfoProxy(field) {

			@Override
			public String getName() {
				return getFieldName(field, containingType);
			}

			@Override
			public String getCaption() {
				return getFieldCaption(field, containingType);
			}

			@Override
			public void setValue(Object object, Object value) {
				setFieldValue(object, value, field, containingType);
			}

			@Override
			public boolean isReadOnly() {
				return isFieldReadOnly(field, containingType);
			}

			@Override
			public boolean isNullable() {
				return isFieldNullable(field, containingType);
			}

			@Override
			public Object getValue(Object object) {
				return getFieldValue(object, field, containingType);
			}

			@Override
			public ITypeInfo getType() {
				return getFieldType(field, containingType);
			}

			@Override
			public InfoCategory getCategory() {
				return getFieldCategory(field, containingType);
			}

		};
	}

	private IParameterInfo getParameterProxy(final IParameterInfo param,
			final IMethodInfo method, final ITypeInfo containingType) {
		return new ParameterInfoProxy(param) {

			@Override
			public String getName() {
				return getParameterName(param, method, containingType);
			}

			@Override
			public String getCaption() {
				return getParameterCaption(param, method, containingType);
			}

			@Override
			public boolean isNullable() {
				return isParameterNullable(param, method, containingType);
			}

			@Override
			public ITypeInfo getType() {
				return getParameterType(param, method, containingType);
			}

			@Override
			public int getPosition() {
				return getParameterPosition(param, method, containingType);
			}

			@Override
			public Object getDefaultValue() {
				return getParameterDefaultValue(param, method, containingType);
			}

			@Override
			public int hashCode() {
				return param.hashCode();
			}

		};
	}

	protected Object getParameterDefaultValue(IParameterInfo param,
			IMethodInfo method, ITypeInfo containingType) {
		return param.getDefaultValue();
	}

	protected int getParameterPosition(IParameterInfo param,
			IMethodInfo method, ITypeInfo containingType) {
		return param.getPosition();
	}

	protected ITypeInfo getParameterType(IParameterInfo param,
			IMethodInfo method, ITypeInfo containingType) {
		return param.getType();
	}

	protected boolean isParameterNullable(IParameterInfo param,
			IMethodInfo method, ITypeInfo containingType) {
		return param.isNullable();
	}

	protected String getParameterCaption(IParameterInfo param,
			IMethodInfo method, ITypeInfo containingType) {
		return param.getCaption();
	}

	protected String getParameterName(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		return param.getName();
	}

	protected InfoCategory getFieldCategory(IFieldInfo field,
			ITypeInfo containingType) {
		return field.getCategory();
	}

	protected ITypeInfo getFieldType(IFieldInfo field, ITypeInfo containingType) {
		return field.getType();
	}

	protected Object getFieldValue(Object object, IFieldInfo field,
			ITypeInfo containingType) {
		return field.getValue(object);
	}

	protected boolean isFieldNullable(IFieldInfo field, ITypeInfo containingType) {
		return field.isNullable();
	}

	protected boolean isFieldReadOnly(IFieldInfo field, ITypeInfo containingType) {
		return field.isReadOnly();
	}

	protected void setFieldValue(Object object, Object value, IFieldInfo field,
			ITypeInfo containingType) {
		field.setValue(object, value);
	}

	protected String getFieldCaption(IFieldInfo field, ITypeInfo containingType) {
		return field.getCaption();
	}

	protected String getFieldName(IFieldInfo field, ITypeInfo containingType) {
		return field.getName();
	}

	protected InfoCategory getMethodCategory(IMethodInfo method,
			ITypeInfo containingType) {
		return method.getCategory();
	}

	protected boolean isMethodReadOnly(IMethodInfo method,
			ITypeInfo containingType) {
		return method.isReadOnly();
	}

	protected Object invokeMethod(Object object,
			Map<String, Object> valueByParameterName, IMethodInfo method,
			ITypeInfo containingType) {
		return method.invoke(object, valueByParameterName);
	}

	protected List<IParameterInfo> getMethodParameters(IMethodInfo method,
			ITypeInfo containingType) {
		List<IParameterInfo> result = new ArrayList<IParameterInfo>();
		for (IParameterInfo param : method.getParameters()) {
			result.add(getParameterProxy(param, method, containingType));
		}
		return result;
	}

	protected ITypeInfo getMethodReturnValueType(IMethodInfo method,
			ITypeInfo containingType) {
		return method.getReturnValueType();
	}

	protected String getMethodCaption(IMethodInfo method,
			ITypeInfo containingType) {
		return method.getCaption();
	}

	protected String getMethodName(IMethodInfo method, ITypeInfo containingType) {
		return method.getName();
	}

	protected List<?> getEnumerationTypePossibleValues(IEnumerationTypeInfo type) {
		return type.getPossibleValues();
	}

	protected Object fromListTypeStandardList(IListTypeInfo type, List<?> list) {
		return type.fromStandardList(list);
	}

	protected ITypeInfo getListTypeItemType(IListTypeInfo type) {
		return type.getItemType();
	}

	protected IListStructuralInfo getListTypeStructuralInfo(IListTypeInfo type) {
		return type.getStructuralInfo();
	}

	protected boolean isListTypeOrdered(IListTypeInfo type) {
		return type.isOrdered();
	}

	protected List<?> toListTypeStandardList(IListTypeInfo type, Object value) {
		return type.toStandardList(value);
	}

	protected Component createTypeFieldControl(ITypeInfo type, Object object,
			IFieldInfo field) {
		return type.createFieldControl(object, field);
	}

	protected List<IMethodInfo> getTypeConstructors(ITypeInfo type) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (IMethodInfo constructor : type.getConstructors()) {
			result.add(getMethodProxy(constructor, type));
		}
		return result;
	}

	protected List<IFieldInfo> getTypeFields(ITypeInfo type) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		for (IFieldInfo field : type.getFields()) {
			result.add(getFieldProxy(field, type));
		}
		return result;
	}

	protected List<IMethodInfo> getTypeMethods(ITypeInfo type) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (IMethodInfo method : type.getMethods()) {
			result.add(getMethodProxy(method, type));
		}
		return result;
	}

	protected List<ITypeInfo> getTypePolymorphicInstanceTypes(ITypeInfo type) {
		return type.getPolymorphicInstanceTypes();
	}

	protected boolean typeHasCustomFieldControl(ITypeInfo type) {
		return type.hasCustomFieldControl();
	}

	protected boolean isTypeConcrete(ITypeInfo type) {
		return type.isConcrete();
	}

	protected boolean isTypeImmutable(ITypeInfo type) {
		return type.isImmutable();
	}

	protected boolean typeSupportsValue(ITypeInfo type, Object value) {
		return type.supportsValue(value);
	}

	protected String getTypeCaption(ITypeInfo type) {
		return type.getCaption();
	}

	protected String getTypeName(ITypeInfo type) {
		return type.getName();
	}

	protected Boolean toBooleanTypeInstanceValue(Object value,
			IBooleanTypeInfo type) {
		return type.toBoolean(value);
	}

	protected Object fromBooleanTypeInstanceValue(Boolean b,
			IBooleanTypeInfo type) {
		return type.fromBoolean(b);
	}

	protected String toTextualtypeInstanceValue(Object value,
			ITextualTypeInfo type) {
		return type.toText(value);
	}

	protected Object fromTextualtypeInstanceValue(String text,
			ITextualTypeInfo type) {
		return type.fromText(text);
	}
	
	protected File getFileTypeDefaultValue(FileTypeInfo type) {
		return type.getDefaultFile();
	}

	protected Component createFileTypeNonNullFieldValueControl(Object object,
			IFieldInfo field, FileTypeInfo type) {
		return type.createNonNullFieldValueControl(object, field);
	}

	protected String getFileTypeDialogTitle(FileTypeInfo type) {
		return type.getDialogTitle();
	}

	protected void configureFileTypeChooser(JFileChooser fileChooser,
			File currentFile, FileTypeInfo type) {
		type.configureFileChooser(fileChooser, currentFile);
	}

	private class BasicTypeInfoProxy implements ITypeInfo {

		protected ITypeInfo type;

		public BasicTypeInfoProxy(ITypeInfo type) {
			this.type = type;
		}

		@Override
		public String getName() {
			return getTypeName(type);
		}

		@Override
		public String getCaption() {
			return getTypeCaption(type);
		}

		@Override
		public boolean supportsValue(Object value) {
			return typeSupportsValue(type, value);
		}

		@Override
		public boolean isImmutable() {
			return isTypeImmutable(type);
		}

		@Override
		public boolean isConcrete() {
			return isTypeConcrete(type);
		}

		@Override
		public boolean hasCustomFieldControl() {
			return typeHasCustomFieldControl(type);
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceTypes() {
			return getTypePolymorphicInstanceTypes(type);
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return getTypeMethods(type);
		}

		@Override
		public List<IFieldInfo> getFields() {
			return getTypeFields(type);
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return getTypeConstructors(type);
		}

		@Override
		public Component createFieldControl(Object object, IFieldInfo field) {
			return createTypeFieldControl(type, object, field);
		}

		@Override
		public int hashCode() {
			return type.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!getClass().equals(obj.getClass())) {
				return false;
			}
			if (!type.equals(((BasicTypeInfoProxy) obj).type)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return type.toString();
		}

	}

	private class ListTypeInfoProxy extends BasicTypeInfoProxy implements
			IListTypeInfo {

		public ListTypeInfoProxy(IListTypeInfo type) {
			super(type);
		}

		@Override
		public List<?> toStandardList(Object value) {
			return toListTypeStandardList((IListTypeInfo) type, value);
		}

		@Override
		public boolean isOrdered() {
			return isListTypeOrdered((IListTypeInfo) type);
		}

		@Override
		public IListStructuralInfo getStructuralInfo() {
			return getListTypeStructuralInfo((IListTypeInfo) type);
		}

		@Override
		public ITypeInfo getItemType() {
			return getListTypeItemType((IListTypeInfo) type);
		}

		@Override
		public Object fromStandardList(List<?> list) {
			return fromListTypeStandardList((IListTypeInfo) type, list);
		}

	}

	private class EnumerationTypeInfoProxy extends BasicTypeInfoProxy implements
			IEnumerationTypeInfo {

		public EnumerationTypeInfoProxy(IEnumerationTypeInfo type) {
			super(type);
		}

		@Override
		public List<?> getPossibleValues() {
			return getEnumerationTypePossibleValues((IEnumerationTypeInfo) type);
		}

	}

	private class BooleanTypeInfoProxy extends BasicTypeInfoProxy implements
			IBooleanTypeInfo {

		public BooleanTypeInfoProxy(IBooleanTypeInfo type) {
			super(type);
		}

		@Override
		public Boolean toBoolean(Object value) {
			return toBooleanTypeInstanceValue(value, (IBooleanTypeInfo) type);
		}

		@Override
		public Object fromBoolean(Boolean b) {
			return fromBooleanTypeInstanceValue(b, (IBooleanTypeInfo) type);
		}

	}

	private class TextualTypeInfoProxy extends BasicTypeInfoProxy implements
			ITextualTypeInfo {

		public TextualTypeInfoProxy(ITextualTypeInfo type) {
			super(type);
		}

		@Override
		public String toText(Object value) {
			return toTextualtypeInstanceValue(value, (ITextualTypeInfo) type);
		}

		@Override
		public Object fromText(String text) {
			return fromTextualtypeInstanceValue(text, (ITextualTypeInfo) type);
		}
	}

	private class FileTypeInfoProxy extends FileTypeInfo{

		private FileTypeInfo type;

		public FileTypeInfoProxy(FileTypeInfo type) {
			super(null);
			this.type = type;
		}

		@Override
		public File getDefaultFile() {
			return getFileTypeDefaultValue(type);
		}

		@Override
		public void configureFileChooser(JFileChooser fileChooser,
				File currentFile) {
			configureFileTypeChooser(fileChooser, currentFile, type);
		}

		@Override
		public String getDialogTitle() {
			return getFileTypeDialogTitle(type);
		}

		@Override
		public Component createNonNullFieldValueControl(Object object,
				IFieldInfo field) {
			return createFileTypeNonNullFieldValueControl(object, field, type);
		}

		@Override
		public String getName() {
			return getTypeName(type);
		}

		@Override
		public String getCaption() {
			return getTypeCaption(type);
		}

		@Override
		public boolean supportsValue(Object value) {
			return typeSupportsValue(type, value);
		}

		@Override
		public boolean isImmutable() {
			return isTypeImmutable(type);
		}

		@Override
		public boolean isConcrete() {
			return isTypeConcrete(type);
		}

		@Override
		public boolean hasCustomFieldControl() {
			return typeHasCustomFieldControl(type);
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceTypes() {
			return getTypePolymorphicInstanceTypes(type);
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return getTypeMethods(type);
		}

		@Override
		public List<IFieldInfo> getFields() {
			return getTypeFields(type);
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return getTypeConstructors(type);
		}

		@Override
		public Component createFieldControl(Object object, IFieldInfo field) {
			return createTypeFieldControl(type, object, field);
		}

		@Override
		public int hashCode() {
			return type.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!getClass().equals(obj.getClass())) {
				return false;
			}
			if (!type.equals(((BasicTypeInfoProxy) obj).type)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return type.toString();
		}

		
		
	}

	

}
