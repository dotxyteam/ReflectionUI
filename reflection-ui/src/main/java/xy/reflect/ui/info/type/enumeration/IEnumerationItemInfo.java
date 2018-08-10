package xy.reflect.ui.info.type.enumeration;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ResourcePath;

public interface IEnumerationItemInfo extends IInfo{
	
	ResourcePath getIconImagePath();

	Object getItem();

}
