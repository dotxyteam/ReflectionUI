package xy.reflect.ui.info.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValueAccessMode;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.TypeInfoProxyFactory;
import xy.reflect.ui.undo.UpdateListValueModification;
import xy.reflect.ui.util.ReflectionUIError;

@SuppressWarnings("unused")
public class MultipleFieldAsOne implements IFieldInfo {

	protected List<IFieldInfo> fields;
	protected ReflectionUI reflectionUI;

	public MultipleFieldAsOne(ReflectionUI reflectionUI, List<IFieldInfo> fields) {
		this.reflectionUI = reflectionUI;
		this.fields = fields;
	}

	@Override
	public ITypeInfo getType() {
		return new TypeInfoProxyFactory() {

			@Override
			protected boolean canAdd(IListTypeInfo type) {
				return false;
			}

			@Override
			protected boolean canRemove(IListTypeInfo type) {
				return false;
			}

			@Override
			protected boolean canInstanciateFromArray(IListTypeInfo type) {
				return false;
			}

			@Override
			protected boolean canReplaceContent(IListTypeInfo type) {
				return true;
			}

			@Override
			protected boolean canViewItemDetails(IListTypeInfo type) {
				return false;
			}

		}.get(reflectionUI.getTypeInfo(new JavaTypeInfoSource(List.class)));
	}

	@Override
	public String getCaption() {
		StringBuilder result = new StringBuilder(MultipleFieldAsOne.class.getSimpleName());
		result.append("(");
		int i = 0;
		for (IFieldInfo field : fields) {
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
		List<ListItem> result = new ArrayList<ListItem>();
		for (IFieldInfo listFieldInfo : fields) {
			ListItem item = new ListItem(object, listFieldInfo);
			reflectionUI.registerPrecomputedTypeInfoObject(item, new ListItemTypeInfo(reflectionUI, item));
			result.add(item);
		}
		return result;
	}

	@Override
	public Runnable getCustomUndoUpdateJob(Object object, Object value) {
		return null;
	}

	@Override
	public Object[] getValueOptions(Object object) {
		return null;
	}

	@Override
	public boolean isGetOnly() {
		return isGetOnlyAndCompatibilityFixedWithUpdateListValueModification();
	}

	private boolean isGetOnlyAndCompatibilityFixedWithUpdateListValueModification() {
		return false;
	}

	@Override
	public ValueAccessMode getValueAccessMode() {
		return ValueAccessMode.PROXY;
	}

	@Override
	public void setValue(Object object, Object value) {
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
		StringBuilder result = new StringBuilder(MultipleFieldAsOne.class.getSimpleName());
		result.append(MultipleFieldAsOne.class.getSimpleName() + "(");
		int i = 0;
		for (IFieldInfo field : fields) {
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
		return fields.hashCode();
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
		return fields.equals(((MultipleFieldAsOne) obj).fields);
	}

	@Override
	public String getOnlineHelp() {
		return null;
	}

	@Override
	public Map<String, Object> getSpecificProperties() {
		return Collections.emptyMap();
	}

	public static class ListItem {

		protected Object object;
		protected IFieldInfo wrappedFieldInfo;

		public ListItem(Object object, IFieldInfo wrappedListFieldInfo) {
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
			ListItem other = (ListItem) obj;
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

	public static class ListItemTypeInfo extends DefaultTypeInfo {

		protected ListItem item;

		public ListItemTypeInfo(ReflectionUI reflectionUI, ListItem item) {
			super(reflectionUI, ListItem.class);
			this.item = item;
		}

		@Override
		public String getCaption() {
			return item.toString();
		}

		public IFieldInfo getValueField() {
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
			ListItemTypeInfo other = (ListItemTypeInfo) obj;
			if (item == null) {
				if (other.item != null)
					return false;
			} else if (!item.equals(other.item))
				return false;
			return true;
		}

	}

}
