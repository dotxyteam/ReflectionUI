package xy.reflect.ui.info.type.iterable.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class StandardMapAsListTypeInfo extends StandardCollectionTypeInfo {

	protected Class<?> keyJavaType;
	protected Class<?> valueJavaType;

	public StandardMapAsListTypeInfo(ReflectionUI reflectionUI, Class<?> javaType, Class<?> keyJavaType,
			Class<?> valueJavaType) {
		super(reflectionUI, javaType,
				reflectionUI.getTypeInfo(new JavaTypeInfoSource(StandardMapEntry.class, keyJavaType, valueJavaType)));
		this.keyJavaType = keyJavaType;
		this.valueJavaType = valueJavaType;
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		if (Map.class.isAssignableFrom(javaType)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean canReplaceContent() {
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void replaceContent(Object listValue, Object[] array) {
		Map tmpMap = new HashMap();
		for (Object item : array) {
			StandardMapEntry entry = (StandardMapEntry) item;
			if (tmpMap.containsKey(entry.getKey())) {
				throw new ReflectionUIError(
						"Duplicate key: '" + ReflectionUIUtils.toString(reflectionUI, entry.getKey()) + "'");
			}
			tmpMap.put(entry.getKey(), entry.getValue());
		}
		Map map = (Map) listValue;
		map.clear();
		map.putAll(tmpMap);
	}

	@Override
	public boolean canInstanciateFromArray() {
		return true;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object fromArray(Object[] array) {
		IMethodInfo constructor = ReflectionUIUtils.getZeroParameterMethod(getConstructors());
		Map result = (Map) constructor.invoke(null, new InvocationData());
		replaceContent(result, array);
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object[] toArray(Object listValue) {
		List<StandardMapEntry> result = new ArrayList<StandardMapEntry>();
		for (Object obj : ((Map) listValue).entrySet()) {
			Map.Entry entry = (Entry) obj;
			StandardMapEntry standardMapEntry = new StandardMapEntry(entry.getKey(), entry.getValue());
			reflectionUI.registerPrecomputedTypeInfoObject(standardMapEntry,
					new StandardMapEntryTypeInfo(reflectionUI, keyJavaType, valueJavaType));
			result.add(standardMapEntry);
		}
		return result.toArray();
	}

	@Override
	public boolean isOrdered() {
		if (Map.class.equals(javaType)) {
			return false;
		}
		if (HashMap.class.equals(javaType)) {
			return false;
		}
		if (SortedMap.class.isAssignableFrom(javaType)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "StandardMapAsListTypeInfo [mapType=" + javaType + ", entryType=" + itemType + "]";
	}

}
