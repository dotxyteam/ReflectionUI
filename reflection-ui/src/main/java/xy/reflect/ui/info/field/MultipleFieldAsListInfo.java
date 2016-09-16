package xy.reflect.ui.info.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;

public class MultipleFieldAsListInfo implements IFieldInfo {

	protected List<IFieldInfo> listFieldInfos;
	protected ReflectionUI reflectionUI;

	public MultipleFieldAsListInfo(ReflectionUI reflectionUI, List<IFieldInfo> listFieldInfos) {
		this.reflectionUI = reflectionUI;
		this.listFieldInfos = listFieldInfos;
	}

	@Override
	public ITypeInfo getType() {
		return new StandardCollectionTypeInfo(reflectionUI, List.class, MultipleFieldAsListItem.class){

			@Override
			public boolean canAdd() {
				return false;
			}

			@Override
			public boolean canRemove() {
				return false;
			}
			
		};
	}

	@Override
	public String getCaption() {
		StringBuilder result = new StringBuilder(MultipleFieldAsListInfo.class.getSimpleName());
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
		List<MultipleFieldAsListItem> result = new ArrayList<MultipleFieldAsListItem>();
		for (IFieldInfo listFieldInfo : listFieldInfos) {
			MultipleFieldAsListItem item = new MultipleFieldAsListItem(object, listFieldInfo);
			result.add(item);
		}
		return result;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public boolean isGetOnly() {
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
		StringBuilder result = new StringBuilder(MultipleFieldAsListInfo.class.getSimpleName());
		result.append(MultipleFieldAsListInfo.class.getSimpleName() + "(");
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
		return listFieldInfos.equals(((MultipleFieldAsListInfo) obj).listFieldInfos);
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	public static class MultipleFieldAsListItem {

		protected Object object;
		protected IFieldInfo wrappedFieldInfo;

		public MultipleFieldAsListItem(Object object, IFieldInfo wrappedListFieldInfo) {
			this.object = object;
			this.wrappedFieldInfo = wrappedListFieldInfo;
		}

		@Override
		public String toString() {
			return "(" + wrappedFieldInfo.getCaption() + ")";
		}

		public Object getValue() {
			return wrappedFieldInfo.getValue(object);			
		}

		public void setValue(Object listValue) {
			wrappedFieldInfo.setValue(object, listValue);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((object == null) ? 0 : object.hashCode());
			result = prime * result + ((wrappedFieldInfo == null) ? 0 : wrappedFieldInfo.hashCode());
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
			MultipleFieldAsListItem other = (MultipleFieldAsListItem) obj;
			if (object == null) {
				if (other.object != null)
					return false;
			} else if (!object.equals(other.object))
				return false;
			if (wrappedFieldInfo == null) {
				if (other.wrappedFieldInfo != null)
					return false;
			} else if (!wrappedFieldInfo.equals(other.wrappedFieldInfo))
				return false;
			return true;
		}
		
		

	}

	public static class MultipleFieldAsListItemTypeInfo extends DefaultTypeInfo {

		protected MultipleFieldAsListItem item;

		public MultipleFieldAsListItemTypeInfo(ReflectionUI reflectionUI, MultipleFieldAsListItem item) {
			super(reflectionUI, MultipleFieldAsListItem.class);
			this.item = item;
		}

		@Override
		public String getCaption() {
			return item.toString();
		}
		
		public IFieldInfo getValueField(){
			return new FieldInfoProxy(super.getFields().get(0)) {
				@Override
				public ITypeInfo getType() {
					return item.wrappedFieldInfo.getType();
				}
			};
		}

		@Override
		public List<IFieldInfo> getFields() {
			return Collections.<IFieldInfo> singletonList(getValueField());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((item == null) ? 0 : item.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			MultipleFieldAsListItemTypeInfo other = (MultipleFieldAsListItemTypeInfo) obj;
			if (item == null) {
				if (other.item != null)
					return false;
			} else if (!item.equals(other.item))
				return false;
			return true;
		}
		

	}

}
