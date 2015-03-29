package xy.reflect.ui.info.type;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.IListTypeInfo.IItemPosition;
import xy.reflect.ui.util.ReflectionUIError;

public class DefaultListStructuralInfo extends
		AbstractTreeDetectionListStructuralInfo {

	private IFieldInfo treeField = new IFieldInfo() {

		@Override
		public String getName() {
			return "";
		}

		@Override
		public String getDocumentation() {
			return null;
		}

		@Override
		public String getCaption() {
			return "";
		}

		@Override
		public void setValue(Object object, Object value) {

		}

		@Override
		public boolean isReadOnly() {
			return true;
		}

		@Override
		public boolean isNullable() {
			return false;
		}

		@Override
		public Object getValue(Object object) {
			return reflectionUI.toString(object);
		}

		@Override
		public ITypeInfo getType() {
			return new DefaultTypeInfo(reflectionUI, String.class);
		}

		@Override
		public InfoCategory getCategory() {
			return null;
		}
	};

	public DefaultListStructuralInfo(ReflectionUI reflectionUI,
			ITypeInfo rootItemType) {
		super(reflectionUI, rootItemType);
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public String getColumnCaption(int columnIndex) {
		return treeField.getCaption();
	}

	@Override
	public String getCellValue(IItemPosition itemPosition, int columnIndex) {
		if (columnIndex != 0) {
			throw new ReflectionUIError();
		}
		return (String) treeField.getValue(itemPosition.getItem());
	}

	@Override
	protected boolean isFieldBased() {
		return false;
	}

	@Override
	protected boolean autoDetectTreeStructure() {
		return true;
	}
	
	

}
