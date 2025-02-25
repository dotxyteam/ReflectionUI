
package xy.reflect.ui.info.type.iterable.map;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Type information extracted from the Java standard map type (assignable to
 * {@link Map}) encapsulated in the given type information source.
 * 
 * @author olitank
 *
 */
public class StandardMapAsListTypeInfo extends StandardCollectionTypeInfo {

	protected Class<?> keyJavaType;
	protected Class<?> valueJavaType;

	public StandardMapAsListTypeInfo(ReflectionUI reflectionUI, JavaTypeInfoSource source, Class<?> keyJavaType, Class<?> valueJavaType) {
		super(reflectionUI, source, null);
		this.keyJavaType = keyJavaType;
		this.valueJavaType = valueJavaType;
		this.itemType = reflectionUI.getTypeInfo(new JavaTypeInfoSource(StandardMapEntry.class,
				new Class[] { keyJavaType, valueJavaType }, null));
	}

	public Class<?> getKeyJavaType() {
		return keyJavaType;
	}

	public Class<?> getValueJavaType() {
		return valueJavaType;
	}

	public static boolean isCompatibleWith(Class<?> javaType) {
		if (Map.class.isAssignableFrom(javaType)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isItemNullValueSupported() {
		return false;
	}

	@Override
	public boolean canReplaceContent() {
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void replaceContent(Object listValue, Object[] array) {
		Map tmpMap = new LinkedHashMap();
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

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object[] toArray(Object listValue) {
		List<Object> result = new ArrayList<Object>();
		for (Object obj : ((Map) listValue).entrySet()) {
			Map.Entry entry = (Entry) obj;
			StandardMapEntry standardMapEntry = new StandardMapEntry(entry.getKey(), entry.getValue(),
					new Class[] { keyJavaType, valueJavaType });
			result.add(standardMapEntry);
		}
		return result.toArray();
	}

	@Override
	public boolean areItemsAutomaticallyPositioned() {
		if (LinkedHashMap.class.isAssignableFrom(getJavaType())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "StandardMapAsListTypeInfo [source=" + source + ", entryType=" + itemType + "]";
	}

}
