package xy.reflect.ui.info.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.type.DefaultListStructuralInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.StandardCollectionTypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

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
				VirtualItem.class) {

			@Override
			public ITypeInfo getItemType() {
				return super.getItemType();
			}

			@Override
			public IListStructuralInfo getStructuralInfo() {
				return new DefaultListStructuralInfo(reflectionUI,
						getItemType()) {

					@Override
					public IFieldInfo getItemSubListField(
							IItemPosition itemPosition) {
						return ((VirtualItem) itemPosition.getItem())
								.getListField();
					}

					@Override
					public List<IFieldInfo> getItemSubListFieldsToExcludeFromDetailsView(
							IItemPosition itemPosition) {
						ITypeInfo virtualItemtypeInfo = new DefaultTypeInfo(
								reflectionUI, VirtualItem.class);
						IFieldInfo objectFieldInfo = ReflectionUIUtils
								.findInfoByName(
										virtualItemtypeInfo.getFields(),
										"object");
						IFieldInfo listFieldFieldInfo = ReflectionUIUtils
								.findInfoByName(
										virtualItemtypeInfo.getFields(),
										"listField");
						return Arrays.asList(objectFieldInfo, listFieldFieldInfo);
					}
				};
			}

		};
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
		List<VirtualItem> result = new ArrayList<VirtualItem>();
		for (IFieldInfo listFieldInfo : listFieldInfos) {
			result.add(new VirtualItem(object, listFieldInfo));
		}
		return result;
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

	public static class VirtualItem {

		protected Object object;
		protected IFieldInfo wrappedListFieldInfo;

		public VirtualItem(Object object, IFieldInfo wrappedListFieldInfo) {
			this.object = object;
			this.wrappedListFieldInfo = wrappedListFieldInfo;
		}

		@Override
		public String toString() {
			return "(" + wrappedListFieldInfo.getCaption() + ")";
		}

		public Object getObject() {
			return object;
		}

		public IFieldInfo getListField() {
			return new IFieldInfo() {

				@Override
				public void setValue(Object object, Object value) {
					wrappedListFieldInfo.setValue(VirtualItem.this.object,
							value);
				}

				@Override
				public boolean isNullable() {
					return wrappedListFieldInfo.isNullable();
				}

				@Override
				public Object getValue(Object object) {
					return wrappedListFieldInfo
							.getValue(VirtualItem.this.object);
				}

				@Override
				public ITypeInfo getType() {
					return wrappedListFieldInfo.getType();
				}

				@Override
				public String getCaption() {
					return "Actual List";
				}

				@Override
				public boolean isReadOnly() {
					return false;
				}

				@Override
				public String getName() {
					return "";
				}

				@Override
				public InfoCategory getCategory() {
					return null;
				}
			};
		}

	}

	@Override
	public String toString() {
		return getCaption();
	}

	@Override
	public String getName() {
		return "";
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

}
