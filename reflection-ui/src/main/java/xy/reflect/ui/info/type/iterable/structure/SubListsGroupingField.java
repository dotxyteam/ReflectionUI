package xy.reflect.ui.info.type.iterable.structure;

import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldsAsListField;
import xy.reflect.ui.info.type.ITypeInfo;

public class SubListsGroupingField extends MultipleFieldsAsListField {

	public SubListsGroupingField(ReflectionUI reflectionUI, List<IFieldInfo> fields) {
		super(reflectionUI, fields);
	}
	
	@Override
	protected ValueListItem getListItem(Object object, IFieldInfo listFieldInfo) {
		return new SubListGroup(object, listFieldInfo);
	}

	@Override
	protected ITypeInfo getListItemTypeInfo(final IFieldInfo field) {
		return new SubListGroupTypeInfo(field);
	}
	
	public class SubListGroupTypeInfo extends ValueListItemTypeInfo{

		public SubListGroupTypeInfo(IFieldInfo field) {
			super(field);
		}
		
	}

	public class SubListGroup extends ValueListItem{

		public SubListGroup(Object object, IFieldInfo field) {
			super(object, field);
		}
		
	}

}
