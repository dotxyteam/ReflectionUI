package xy.reflect.ui.info.type.iterable.structure;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MultipleFieldsAsOneListField.ListItem;

public class ListFieldNamesNode extends ListItem {

	public ListFieldNamesNode(Object object, IFieldInfo wrappedListFieldInfo) {
		super(object, wrappedListFieldInfo);
	}

	@Override
	public String getTitle() {
		return "(" + super.getTitle() + ")";
	}
}
