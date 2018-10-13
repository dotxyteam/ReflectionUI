package xy.reflect.ui.info.type.source;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.ITypeInfo;

public interface ITypeInfoSource {

	SpecificitiesIdentifier getSpecificitiesIdentifier();

	ITypeInfo getTypeInfo(ReflectionUI reflectionUI);

}
