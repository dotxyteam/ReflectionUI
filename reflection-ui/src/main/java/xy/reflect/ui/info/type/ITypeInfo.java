package xy.reflect.ui.info.type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public interface ITypeInfo extends IInfo {

	ITypeInfo NULL_TYPE_INFO = new ITypeInfo() {
		
		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}
		
		@Override
		public String getOnlineHelp() {
			return null;
		}
		
		@Override
		public String getName() {
			return "NULL_TYPE_INFO";
		}
		
		@Override
		public String getCaption() {
			return "NULL_TYPE_INFO";
		}
		
		@Override
		public void validate(Object object) throws Exception {
		}
		
		@Override
		public String toString(Object object) {
			return getCaption();
		}
		
		@Override
		public boolean supportsInstance(Object object) {
			return true;
		}
		
		@Override
		public boolean isModificationStackAccessible() {
			return true;
		}
		
		@Override
		public boolean isConcrete() {
			return false;
		}
		
		@Override
		public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
			return null;
		}
		
		@Override
		public List<IMethodInfo> getMethods() {
			return Collections.emptyList();
		}
		
		@Override
		public List<IFieldInfo> getFields() {
			return Collections.emptyList();
		}
		
		@Override
		public List<IMethodInfo> getConstructors() {
			return Collections.emptyList();
		}
		
		@Override
		public boolean equals(Object value1, Object value2) {
			return ReflectionUIUtils.equalsOrBothNull(value1, value2);
		}
		
		@Override
		public Object copy(Object object) {
			throw new ReflectionUIError();
		}
		
		@Override
		public boolean canCopy(Object object) {
			return false;
		}
	};

	boolean isConcrete();

	List<IMethodInfo> getConstructors();

	List<IFieldInfo> getFields();

	List<IMethodInfo> getMethods();

	boolean supportsInstance(Object object);

	List<ITypeInfo> getPolymorphicInstanceSubTypes();

	String toString(Object object);

	void validate(Object object) throws Exception;

	boolean canCopy(Object object);

	Object copy(Object object);

	boolean equals(Object value1, Object value2);

	boolean isModificationStackAccessible();
}
