package xy.reflect.ui.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract class that updates an external list with minimal
 * insertions/removals. The list starts empty and is only updated via add/remove
 * methods. The set of keys is stable, and shouldBePresent(key) may evolve.
 */
public abstract class MinimalListUpdater<K> {

	/**
	 * Represents the currently present elements in the external list, in the same
	 * order as keys from getKeys().
	 */
	protected final List<K> presentKeys = new ArrayList<>();

	/** Returns the full stable list of keys (always in the same order). */
	protected abstract List<K> collectKeys();

	/** Indicates whether the given key should be present in the external list. */
	protected abstract boolean shouldBePresent(K key);

	/** Adds the given key at the specified index in the external list. */
	protected abstract void add(int index, K key);

	/** Removes the key at the specified index from the external list. */
	protected abstract void remove(int index);

	/**
	 * @return the currently present elements in the external list, in the same
	 *         order as keys from getKeys().
	 */
	public List<K> getPresentKeys() {
		return Collections.unmodifiableList(presentKeys);
	}

	/**
	 * Updates the external list by inserting or removing only what is necessary,
	 * maintaining the order of getKeys(), and minimizing disruptions.
	 */
	public void update() {
		List<K> allKeys = collectKeys();
		// Step 1: Build the desired list of keys to be present after update
		List<K> desiredKeys = new ArrayList<>();
		for (K key : allKeys) {
			if (shouldBePresent(key)) {
				desiredKeys.add(key);
			}
		}
		int presentIndex = 0; // Index in presentKeys
		int desiredIndex = 0; // Index in desiredKeys
		// Step 2: Walk through both present and desired lists, updating as needed
		while (presentIndex < presentKeys.size() || desiredIndex < desiredKeys.size()) {
			if (presentIndex >= presentKeys.size()) {
				// Append remaining desired elements
				K keyToAdd = desiredKeys.get(desiredIndex);
				add(presentIndex, keyToAdd);
				presentKeys.add(presentIndex, keyToAdd);
				presentIndex++;
				desiredIndex++;
			} else if (desiredIndex >= desiredKeys.size()) {
				// Remove any leftover elements in presentKeys
				remove(presentIndex);
				presentKeys.remove(presentIndex);
			} else {
				K current = presentKeys.get(presentIndex);
				K expected = desiredKeys.get(desiredIndex);
				if (current.equals(expected)) {
					// Element is already in the correct place
					presentIndex++;
					desiredIndex++;
				} else {
					// Check if expected key exists later in presentKeys
					int offset = presentKeys.subList(presentIndex, presentKeys.size()).indexOf(expected);
					if (offset >= 0) {
						// Remove all keys before the expected one
						for (int skipped = 0; skipped < offset; skipped++) {
							remove(presentIndex);
							presentKeys.remove(presentIndex);
						}
					} else {
						// Insert missing key
						add(presentIndex, expected);
						presentKeys.add(presentIndex, expected);
						presentIndex++;
						desiredIndex++;
					}
				}
			}
		}
	}
}
