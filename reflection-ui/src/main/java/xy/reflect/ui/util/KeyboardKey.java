package xy.reflect.ui.util;

import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KeyboardKey implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String keyName;

	public KeyboardKey(int keyCode) {
		this(getKeyNameOptions().stream().map(KeyboardKey::new)
				.filter(keyboardKey -> keyboardKey.getKeyCode() == keyCode).map(KeyboardKey::getKeyName).findFirst()
				.get());
	}

	public KeyboardKey(String keyName) {
		this.keyName = keyName;
	}

	public KeyboardKey() {
		this(getKeyNameOptions().get(0));
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public static List<String> getKeyNameOptions() {
		return Arrays.stream(KeyEvent.class.getFields()).filter(field -> field.getName().startsWith("VK_"))
				.map(field -> field.getName().substring("VK_".length())).sorted().collect(Collectors.toList());
	}

	public int getKeyCode() {
		try {
			return KeyEvent.class.getField("VK_" + keyName).getInt(null);
		} catch (Exception e) {
			throw new ReflectionUIError(e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((keyName == null) ? 0 : keyName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyboardKey other = (KeyboardKey) obj;
		if (keyName == null) {
			if (other.keyName != null)
				return false;
		} else if (!keyName.equals(other.keyName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Mnemonic [key=" + keyName + "]";
	}

}