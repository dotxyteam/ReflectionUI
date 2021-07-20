


package xy.reflect.ui.info.type.iterable.map;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import xy.reflect.ui.info.type.iterable.StandardCollectionTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.PrecomputedTypeInstanceWrapper;
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

	protected JavaTypeInfoSource entryTypeSource;

	public StandardMapAsListTypeInfo(JavaTypeInfoSource source, Class<?> keyJavaType, Class<?> valueJavaType) {
		super(source, null);
		this.keyJavaType = keyJavaType;
		this.valueJavaType = valueJavaType;
		this.entryTypeSource = new JavaTypeInfoSource(reflectionUI, StandardMapEntry.class,
				new Class[] { keyJavaType, valueJavaType }, null);
		this.itemType = reflectionUI
				.buildTypeInfo(new PrecomputedTypeInstanceWrapper.TypeInfoSource(entryTypeSource.getTypeInfo()));
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
			StandardMapEntry entry = (StandardMapEntry) ((PrecomputedTypeInstanceWrapper) item).unwrap();
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
			StandardMapEntry standardMapEntry = new StandardMapEntry(entry.getKey(), entry.getValue());
			result.add(new PrecomputedTypeInstanceWrapper(standardMapEntry, entryTypeSource.getTypeInfo()));
		}
		return result.toArray();
	}

	@Override
	public boolean isOrdered() {
		if (LinkedHashMap.class.isAssignableFrom(getJavaType())) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "StandardMapAsListTypeInfo [source=" + source + ", entryType=" + itemType + "]";
	}

}
