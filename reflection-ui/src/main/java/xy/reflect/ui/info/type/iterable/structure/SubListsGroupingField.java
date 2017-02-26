package xy.reflect.ui.info.type.iterable.structure;

import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldsAsOne;
import xy.reflect.ui.info.type.ITypeInfo;

public class SubListsGroupingField extends MultipleFieldsAsOne {

	public SubListsGroupingField(ReflectionUI reflectionUI, List<IFieldInfo> fields) {
		super(reflectionUI, fields);
	}
	
	@Override
	public String getItemTitle(IFieldInfo field) {
		return "(" + super.getItemTitle(field) + ")";
	}
	
	@Override
	protected ListItem getListItem(Object object, IFieldInfo listFieldInfo) {
		return new SubListGroup(object, listFieldInfo);
	}

	@Override
	protected ITypeInfo getListItemTypeInfo(final IFieldInfo field) {
		return new SubListGroupTypeInfo(field);
	}
	
	public class SubListGroupTypeInfo extends ListItemTypeInfo{

		public SubListGroupTypeInfo(IFieldInfo field) {
			super(field);
		}
		
	}

	public class SubListGroup extends ListItem{

		public SubListGroup(Object object, IFieldInfo field) {
			super(object, field);
		}
		
	}

}
