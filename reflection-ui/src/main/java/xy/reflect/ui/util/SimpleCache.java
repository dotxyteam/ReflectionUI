package xy.reflect.ui.util;

import java.util.*;

/**
 * Here is a simple cache implementation that implements the Map<K,V> interface
 * with:
 * 
 * - maximum size (LRU eviction);
 * 
 * - entry expiration after a TTL (checked in get / containsKey);
 * 
 * - key comparison via equals() (standard Map behavior);
 * 
 * - no external dependencies.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class SimpleCache<K, V> implements Map<K, V> {

	private final long expirationDelayMilliseconds;
	private final int maxSize;
	private final LinkedHashMap<K, TimestampedValue<V>> map;

	public SimpleCache(int maxSize, long expirationDelayMilliseconds) {
		if (maxSize <= 0)
			throw new IllegalArgumentException("maxSize > 0");
		if (expirationDelayMilliseconds < 0)
			throw new IllegalArgumentException("expirationDelayMilliseconds >= 0");

		this.maxSize = maxSize;
		this.expirationDelayMilliseconds = expirationDelayMilliseconds;

		this.map = new LinkedHashMap<K, TimestampedValue<V>>(16, 0.75f, true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<K, TimestampedValue<V>> eldest) {
				return size() > SimpleCache.this.maxSize;
			}
		};
	}

	@Override
	public int size() {
		cleanupExpired();
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		cleanupExpired();
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		V v = get(key);
		return v != null || map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		cleanupExpired();
		for (TimestampedValue<V> e : map.values()) {
			if (Objects.equals(e.value, value))
				return true;
		}
		return false;
	}

	@Override
	public V get(Object key) {
		TimestampedValue<V> e = map.get(key);
		if (e == null)
			return null;
		if (isExpired(e)) {
			map.remove(key);
			return null;
		}
		return e.value;
	}

	@Override
	public V put(K key, V value) {
		TimestampedValue<V> old = map.put(key, new TimestampedValue<>(value, System.currentTimeMillis()));
		return old == null ? null : old.value;
	}

	@Override
	public V remove(Object key) {
		TimestampedValue<V> old = map.remove(key);
		return old == null ? null : old.value;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		m.forEach(this::put);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set<K> keySet() {
		cleanupExpired();
		return new HashSet<>(map.keySet());
	}

	@Override
	public Collection<V> values() {
		cleanupExpired();
		List<V> values = new ArrayList<>();
		map.values().forEach(e -> values.add(e.value));
		return values;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		cleanupExpired();
		Set<java.util.Map.Entry<K, V>> entries = new HashSet<>();
		for (java.util.Map.Entry<K, TimestampedValue<V>> e : map.entrySet()) {
			entries.add(new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().value));
		}
		return entries;
	}

	private boolean isExpired(TimestampedValue<V> e) {
		return expirationDelayMilliseconds > 0
				&& System.currentTimeMillis() - e.timestamp > expirationDelayMilliseconds;
	}

	private void cleanupExpired() {
		Iterator<java.util.Map.Entry<K, TimestampedValue<V>>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			java.util.Map.Entry<K, TimestampedValue<V>> e = it.next();
			if (isExpired(e.getValue()))
				it.remove();
		}
	}

	private static class TimestampedValue<V> {
		final V value;
		final long timestamp;

		TimestampedValue(V value, long timestamp) {
			this.value = value;
			this.timestamp = timestamp;
		}
	}
}
