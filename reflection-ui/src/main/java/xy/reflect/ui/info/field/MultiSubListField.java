package xy.reflect.ui.info.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.list.IListTypeInfo;
import xy.reflect.ui.info.type.list.StandardCollectionTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class MultiSubListField implements IFieldInfo {

	protected List<IFieldInfo> listFieldInfos;
	protected ReflectionUI reflectionUI;

	public MultiSubListField(ReflectionUI reflectionUI,
			List<IFieldInfo> listFieldInfos) {
		this.reflectionUI = reflectionUI;
		this.listFieldInfos = listFieldInfos;
	}

	@Override
	public ITypeInfo getType() {
		return new StandardCollectionTypeInfo(reflectionUI, List.class,
				MultiSubListVirtualParent.class);
	}

	@Override
	public String getCaption() {
		StringBuilder result = new StringBuilder(
				MultiSubListField.class.getSimpleName());
		result.append("(");
		int i = 0;
		for (IFieldInfo field : listFieldInfos) {
			if (i > 0) {
				result.append(", ");
			}
			result.append(field.getCaption());
			i++;
		}
		result.append(")");
		return result.toString();
	}

	@Override
	public Object getValue(Object object) {
		List<MultiSubListVirtualParent> result = new ArrayList<MultiSubListVirtualParent>();
		for (IFieldInfo listFieldInfo : listFieldInfos) {
			result.add(new MultiSubListVirtualParent(object, listFieldInfo));
		}
		return result;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public void setValue(Object object, Object value) {
		throw new ReflectionUIError();
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public InfoCategory getCategory() {
		return null;
	}

	@Override
	public String toString() {
		return getCaption();
	}

	@Override
	public String getName() {
		StringBuilder result = new StringBuilder(
				MultiSubListField.class.getSimpleName());
		result.append(MultiSubListField.class.getSimpleName() + "(");
		int i = 0;
		for (IFieldInfo field : listFieldInfos) {
			if (i > 0) {
				result.append(", ");
			}
			result.append(field.getName());
			i++;
		}
		result.append(")");
		return result.toString();
	}

	@Override
	public int hashCode() {
		return listFieldInfos.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		return listFieldInfos.equals(((MultiSubListField) obj).listFieldInfos);
	}

	@Override
	public String getDocumentation() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	public static class MultiSubListVirtualParent {

		protected Object object;
		protected IFieldInfo wrappedListFieldInfo;

		public MultiSubListVirtualParent(Object object,
				IFieldInfo wrappedListFieldInfo) {
			this.object = object;
			this.wrappedListFieldInfo = wrappedListFieldInfo;
		}

		@Override
		public String toString() {
			return "(" + wrappedListFieldInfo.getCaption() + ")";
		}

		public Object[] getSubListValue() {
			IListTypeInfo listType = (IListTypeInfo) wrappedListFieldInfo
					.getType();
			Object list = wrappedListFieldInfo.getValue(object);
			return listType.toListValue(list);
		}

		public void setSubListValue(Object[] listValue) {
			IListTypeInfo listType = (IListTypeInfo) wrappedListFieldInfo
					.getType();
			Object list = listType.fromListValue(listValue);
			wrappedListFieldInfo.setValue(object, list);
		}

	}

	public static class MultiSubListVirtualParentType extends DefaultTypeInfo {

		private MultiSubListVirtualParent virtualParent;

		public MultiSubListVirtualParentType(ReflectionUI reflectionUI,
				MultiSubListVirtualParent virtualParent) {
			super(reflectionUI, MultiSubListVirtualParent.class);
			this.virtualParent = virtualParent;
		}

		@Override
		public String getCaption() {
			return virtualParent.toString();
		}

		@Override
		public List<IFieldInfo> getFields() {
			return Collections.<IFieldInfo> singletonList(new FieldInfoProxy(
					super.getFields().get(0)) {
				@Override
				public IListTypeInfo getType() {
					return (IListTypeInfo) virtualParent.wrappedListFieldInfo
							.getType();
				}

				@Override
				public Object getValue(Object object) {
					Object[] listValue = (Object[]) super.getValue(object);
					return getType().fromListValue(listValue);
				}

				@Override
				public void setValue(Object object, Object value) {
					Object[] listValue = getType().toListValue(value);
					super.setValue(object, listValue);
				}

			});
		}

	}

}
