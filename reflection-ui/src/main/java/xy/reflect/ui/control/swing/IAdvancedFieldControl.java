package xy.reflect.ui.control.swing;

import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.info.type.ITypeInfo;

public interface IAdvancedFieldControl {
	boolean displayError(String msg);

	boolean showCaption();

	boolean refreshUI();

	boolean handlesModificationStackUpdate();

	Object getFocusDetails();
	
	void requestDetailedFocus(Object value);
	
	void validateSubForm() throws Exception;

	void setPalceHolder(FieldControlPlaceHolder fieldControlPlaceHolder);
	
	ITypeInfo getDynamicObjectType();
}
