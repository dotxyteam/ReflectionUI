package xy.reflect.ui.info.type;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import xy.reflect.ui.info.field.FieldInfoProxy;
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
		} else if (type instanceof IMapEntryTypeInfo) {
			return new MapEntryTypeInfoProxy((IMapEntryTypeInfo) type);
		} else {
			return new BasicTypeInfoProxy(type);
		}
	}

	private IMethodInfo getMethodProxy(final IMethodInfo method,
			final ITypeInfo containingType) {
		return new MethodInfoProxy(method) {

			@Override
			public String getName() {
				return TypeInfoProxy.this.getName(method, containingType);
			}

			@Override
			public String getCaption() {
				return TypeInfoProxy.this.getCaption(method, containingType);
			}

			@Override
			public ITypeInfo getReturnValueType() {
				return TypeInfoProxy.this.getReturnValueType(method, containingType);
			}

			@Override
			public List<IParameterInfo> getParameters() {
				return TypeInfoProxy.this.getParameters(method, containingType);
			}

			@Override
			public Object invoke(Object object,
					Map<String, Object> valueByParameterName) {
				return TypeInfoProxy.this.invoke(object, valueByParameterName, method,
						containingType);
			}

			@Override
			public boolean isReadOnly() {
				return TypeInfoProxy.this.isReadOnly(method, containingType);
			}

			@Override
			public InfoCategory getCategory() {
				return TypeInfoProxy.this.getCategory(method, containingType);
			}

		};
	}

	private IFieldInfo getFieldProxy(final IFieldInfo field,
			final ITypeInfo containingType) {
		return new FieldInfoProxy(field) {

			@Override
			public String getName() {
				return TypeInfoProxy.this.getName(field, containingType);
			}

			@Override
			public String getCaption() {
				return TypeInfoProxy.this.getCaption(field, containingType);
			}

			@Override
			public void setValue(Object object, Object value) {
				TypeInfoProxy.this.setValue(object, value, field, containingType);
			}

			@Override
			public boolean isReadOnly() {
				return TypeInfoProxy.this.isReadOnly(field, containingType);
			}

			@Override
			public boolean isNullable() {
				return TypeInfoProxy.this.isNullable(field, containingType);
			}

			@Override
			public Object getValue(Object object) {
				return TypeInfoProxy.this.getValue(object, field, containingType);
			}

			@Override
			public ITypeInfo getType() {
				return TypeInfoProxy.this.getType(field, containingType);
			}

			@Override
			public InfoCategory getCategory() {
				return TypeInfoProxy.this.getCategory(field, containingType);
			}

		};
	}

	private IParameterInfo getParameterProxy(final IParameterInfo param,
			final IMethodInfo method, final ITypeInfo containingType) {
		return new ParameterInfoProxy(param) {

			@Override
			public String getName() {
				return TypeInfoProxy.this.getName(param, method, containingType);
			}

			@Override
			public String getCaption() {
				return TypeInfoProxy.this.getCaption(param, method, containingType);
			}

			@Override
			public boolean isNullable() {
				return TypeInfoProxy.this.isNullable(param, method, containingType);
			}

			@Override
			public ITypeInfo getType() {
				return TypeInfoProxy.this.getType(param, method, containingType);
			}

			@Override
			public int getPosition() {
				return TypeInfoProxy.this.getPosition(param, method, containingType);
			}

			@Override
			public Object getDefaultValue() {
				return TypeInfoProxy.this.getDefaultValue(param, method, containingType);
			}

			@Override
			public int hashCode() {
				return param.hashCode();
			}

		};
	}

	protected Object getDefaultValue(IParameterInfo param,
			IMethodInfo method, ITypeInfo containingType) {
		return param.getDefaultValue();
	}

	protected int getPosition(IParameterInfo param,
			IMethodInfo method, ITypeInfo containingType) {
		return param.getPosition();
	}

	protected ITypeInfo getType(IParameterInfo param,
			IMethodInfo method, ITypeInfo containingType) {
		return param.getType();
	}

	protected boolean isNullable(IParameterInfo param,
			IMethodInfo method, ITypeInfo containingType) {
		return param.isNullable();
	}

	protected String getCaption(IParameterInfo param,
			IMethodInfo method, ITypeInfo containingType) {
		return param.getCaption();
	}

	protected String getName(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		return param.getName();
	}

	protected InfoCategory getCategory(IFieldInfo field,
			ITypeInfo containingType) {
		return field.getCategory();
	}

	protected ITypeInfo getType(IFieldInfo field, ITypeInfo containingType) {
		return field.getType();
	}

	protected Object getValue(Object object, IFieldInfo field,
			ITypeInfo containingType) {
		return field.getValue(object);
	}

	protected boolean isNullable(IFieldInfo field, ITypeInfo containingType) {
		return field.isNullable();
	}

	protected boolean isReadOnly(IFieldInfo field, ITypeInfo containingType) {
		return field.isReadOnly();
	}

	protected void setValue(Object object, Object value, IFieldInfo field,
			ITypeInfo containingType) {
		field.setValue(object, value);
	}

	protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
		return field.getCaption();
	}

	protected String getName(IFieldInfo field, ITypeInfo containingType) {
		return field.getName();
	}

	protected InfoCategory getCategory(IMethodInfo method,
			ITypeInfo containingType) {
		return method.getCategory();
	}

	protected boolean isReadOnly(IMethodInfo method,
			ITypeInfo containingType) {
		return method.isReadOnly();
	}

	protected Object invoke(Object object,
			Map<String, Object> valueByParameterName, IMethodInfo method,
			ITypeInfo containingType) {
		return method.invoke(object, valueByParameterName);
	}

	protected List<IParameterInfo> getParameters(IMethodInfo method,
			ITypeInfo containingType) {
		List<IParameterInfo> result = new ArrayList<IParameterInfo>();
		for (IParameterInfo param : method.getParameters()) {
			result.add(getParameterProxy(param, method, containingType));
		}
		return result;
	}

	protected ITypeInfo getReturnValueType(IMethodInfo method,
			ITypeInfo containingType) {
		return method.getReturnValueType();
	}

	protected String getCaption(IMethodInfo method,
			ITypeInfo containingType) {
		return method.getCaption();
	}

	protected String getName(IMethodInfo method, ITypeInfo containingType) {
		return method.getName();
	}

	protected List<?> getPossibleValues(IEnumerationTypeInfo type) {
		return type.getPossibleValues();
	}

	protected Object fromStandardList(IListTypeInfo type, List<?> list) {
		return type.fromStandardList(list);
	}

	protected ITypeInfo getItemType(IListTypeInfo type) {
		return type.getItemType();
	}

	protected IListStructuralInfo getStructuralInfo(IListTypeInfo type) {
		return type.getStructuralInfo();
	}

	protected boolean isOrdered(IListTypeInfo type) {
		return type.isOrdered();
	}

	protected List<?> toStandardList(IListTypeInfo type, Object value) {
		return type.toStandardList(value);
	}

	protected Component createFieldControl(ITypeInfo type, Object object,
			IFieldInfo field) {
		return type.createFieldControl(object, field);
	}

	protected List<IMethodInfo> getConstructors(ITypeInfo type) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (IMethodInfo constructor : type.getConstructors()) {
			result.add(getMethodProxy(constructor, type));
		}
		return result;
	}

	protected List<IFieldInfo> getFields(ITypeInfo type) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>();
		for (IFieldInfo field : type.getFields()) {
			result.add(getFieldProxy(field, type));
		}
		return result;
	}

	protected List<IMethodInfo> getMethods(ITypeInfo type) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>();
		for (IMethodInfo method : type.getMethods()) {
			result.add(getMethodProxy(method, type));
		}
		return result;
	}

	protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
		return type.getPolymorphicInstanceSubTypes();
	}

	protected boolean hasCustomFieldControl(ITypeInfo type) {
		return type.hasCustomFieldControl();
	}

	protected boolean isConcrete(ITypeInfo type) {
		return type.isConcrete();
	}

	protected boolean isImmutable(ITypeInfo type) {
		return type.isImmutable();
	}

	protected boolean supportsValue(ITypeInfo type, Object value) {
		return type.supportsValue(value);
	}

	protected String getCaption(ITypeInfo type) {
		return type.getCaption();
	}

	protected String getName(ITypeInfo type) {
		return type.getName();
	}

	protected Boolean toBoolean(Object value,
			IBooleanTypeInfo type) {
		return type.toBoolean(value);
	}

	protected Object fromBoolean(Boolean b,
			IBooleanTypeInfo type) {
		return type.fromBoolean(b);
	}

	protected String toText(Object value,
			ITextualTypeInfo type) {
		return type.toText(value);
	}

	protected Object fromText(String text,
			ITextualTypeInfo type) {
		return type.fromText(text);
	}

	protected File getDefaultFile(FileTypeInfo type) {
		return type.getDefaultFile();
	}

	protected Component createNonNullFieldValueControl(Object object,
			IFieldInfo field, FileTypeInfo type) {
		return type.createNonNullFieldValueControl(object, field);
	}

	protected String getDialogTitle(FileTypeInfo type) {
		return type.getDialogTitle();
	}

	protected IFieldInfo getKeyField(IMapEntryTypeInfo type) {
		return type.getKeyField();
	}

	protected IFieldInfo getValueField(IMapEntryTypeInfo type) {
		return type.getValueField();
	}

	protected void configureFileChooser(JFileChooser fileChooser,
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
			return TypeInfoProxy.this.getName(type);
		}

		@Override
		public String getCaption() {
			return TypeInfoProxy.this.getCaption(type);
		}

		@Override
		public boolean supportsValue(Object value) {
			return TypeInfoProxy.this.supportsValue(type, value);
		}

		@Override
		public boolean isImmutable() {
			return TypeInfoProxy.this.isImmutable(type);
		}

		@Override
		public boolean isConcrete() {
			return TypeInfoProxy.this.isConcrete(type);
		}

		@Override
		public boolean hasCustomFieldControl() {
			return TypeInfoProxy.this.hasCustomFieldControl(type);
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return TypeInfoProxy.this.getPolymorphicInstanceSubTypes(type);
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return TypeInfoProxy.this.getMethods(type);
		}

		@Override
		public List<IFieldInfo> getFields() {
			return TypeInfoProxy.this.getFields(type);
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return TypeInfoProxy.this.getConstructors(type);
		}

		@Override
		public Component createFieldControl(Object object, IFieldInfo field) {
			return TypeInfoProxy.this.createFieldControl(type, object, field);
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
			return TypeInfoProxy.this.toStandardList((IListTypeInfo) type, value);
		}

		@Override
		public boolean isOrdered() {
			return TypeInfoProxy.this.isOrdered((IListTypeInfo) type);
		}

		@Override
		public IListStructuralInfo getStructuralInfo() {
			return TypeInfoProxy.this.getStructuralInfo((IListTypeInfo) type);
		}

		@Override
		public ITypeInfo getItemType() {
			return TypeInfoProxy.this.getItemType((IListTypeInfo) type);
		}

		@Override
		public Object fromStandardList(List<?> list) {
			return TypeInfoProxy.this.fromStandardList((IListTypeInfo) type, list);
		}

	}

	private class EnumerationTypeInfoProxy extends BasicTypeInfoProxy implements
			IEnumerationTypeInfo {

		public EnumerationTypeInfoProxy(IEnumerationTypeInfo type) {
			super(type);
		}

		@Override
		public List<?> getPossibleValues() {
			return TypeInfoProxy.this.getPossibleValues((IEnumerationTypeInfo) type);
		}

	}

	private class BooleanTypeInfoProxy extends BasicTypeInfoProxy implements
			IBooleanTypeInfo {

		public BooleanTypeInfoProxy(IBooleanTypeInfo type) {
			super(type);
		}

		@Override
		public Boolean toBoolean(Object value) {
			return TypeInfoProxy.this.toBoolean(value, (IBooleanTypeInfo) type);
		}

		@Override
		public Object fromBoolean(Boolean b) {
			return TypeInfoProxy.this.fromBoolean(b, (IBooleanTypeInfo) type);
		}

	}

	private class TextualTypeInfoProxy extends BasicTypeInfoProxy implements
			ITextualTypeInfo {

		public TextualTypeInfoProxy(ITextualTypeInfo type) {
			super(type);
		}

		@Override
		public String toText(Object value) {
			return TypeInfoProxy.this.toText(value, (ITextualTypeInfo) type);
		}

		@Override
		public Object fromText(String text) {
			return TypeInfoProxy.this.fromText(text, (ITextualTypeInfo) type);
		}
	}

	private class MapEntryTypeInfoProxy extends BasicTypeInfoProxy implements
			IMapEntryTypeInfo {

		public MapEntryTypeInfoProxy(IMapEntryTypeInfo type) {
			super(type);
		}

		@Override
		public IFieldInfo getKeyField() {
			return TypeInfoProxy.this.getKeyField((IMapEntryTypeInfo) type);
		}

		@Override
		public IFieldInfo getValueField() {
			return TypeInfoProxy.this.getValueField((IMapEntryTypeInfo) type);
		}

	}

	private class FileTypeInfoProxy extends FileTypeInfo {

		private FileTypeInfo type;

		public FileTypeInfoProxy(FileTypeInfo type) {
			super(null);
			this.type = type;
		}

		@Override
		public File getDefaultFile() {
			return TypeInfoProxy.this.getDefaultFile(type);
		}

		@Override
		public void configureFileChooser(JFileChooser fileChooser,
				File currentFile) {
			TypeInfoProxy.this.configureFileChooser(fileChooser, currentFile, type);
		}

		@Override
		public String getDialogTitle() {
			return TypeInfoProxy.this.getDialogTitle(type);
		}

		@Override
		public Component createNonNullFieldValueControl(Object object,
				IFieldInfo field) {
			return TypeInfoProxy.this.createNonNullFieldValueControl(object, field, type);
		}

		@Override
		public String getName() {
			return TypeInfoProxy.this.getName(type);
		}

		@Override
		public String getCaption() {
			return TypeInfoProxy.this.getCaption(type);
		}

		@Override
		public boolean supportsValue(Object value) {
			return TypeInfoProxy.this.supportsValue(type, value);
		}

		@Override
		public boolean isImmutable() {
			return TypeInfoProxy.this.isImmutable(type);
		}

		@Override
		public boolean isConcrete() {
			return TypeInfoProxy.this.isConcrete(type);
		}

		@Override
		public boolean hasCustomFieldControl() {
			return TypeInfoProxy.this.hasCustomFieldControl(type);
		}

		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return TypeInfoProxy.this.getPolymorphicInstanceSubTypes(type);
		}

		@Override
		public List<IMethodInfo> getMethods() {
			return TypeInfoProxy.this.getMethods(type);
		}

		@Override
		public List<IFieldInfo> getFields() {
			return TypeInfoProxy.this.getFields(type);
		}

		@Override
		public List<IMethodInfo> getConstructors() {
			return TypeInfoProxy.this.getConstructors(type);
		}

		@Override
		public Component createFieldControl(Object object, IFieldInfo field) {
			return TypeInfoProxy.this.createFieldControl(type, object, field);
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
