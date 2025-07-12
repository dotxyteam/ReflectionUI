package xy.reflect.ui.util;

import java.awt.event.InputEvent;

public class KeyboardShortcut extends KeyboardKey {

	private static final long serialVersionUID = 1L;

	protected boolean shiftDown;
	protected boolean ctrlDown;
	protected boolean metaDown;
	protected boolean altDown;
	protected boolean altGrDown;

	public KeyboardShortcut(String keyName, boolean shiftDown, boolean ctrlDown, boolean metaDown, boolean altDown,
			boolean altGrDown) {
		super(keyName);
		this.shiftDown = shiftDown;
		this.ctrlDown = ctrlDown;
		this.metaDown = metaDown;
		this.altDown = altDown;
		this.altGrDown = altGrDown;
	}

	public KeyboardShortcut(int keyCode, boolean shiftDown, boolean ctrlDown, boolean metaDown, boolean altDown,
			boolean altGrDown) {
		super(keyCode);
		this.shiftDown = shiftDown;
		this.ctrlDown = ctrlDown;
		this.metaDown = metaDown;
		this.altDown = altDown;
		this.altGrDown = altGrDown;
	}

	public KeyboardShortcut() {
		this(getKeyNameOptions().get(0), false, false, false, false, false);
	}

	public boolean isShiftDown() {
		return shiftDown;
	}

	public void setShiftDown(boolean shiftDown) {
		this.shiftDown = shiftDown;
	}

	public boolean isCtrlDown() {
		return ctrlDown;
	}

	public void setCtrlDown(boolean ctrlDown) {
		this.ctrlDown = ctrlDown;
	}

	public boolean isMetaDown() {
		return metaDown;
	}

	public void setMetaDown(boolean metaDown) {
		this.metaDown = metaDown;
	}

	public boolean isAltDown() {
		return altDown;
	}

	public void setAltDown(boolean altDown) {
		this.altDown = altDown;
	}

	public boolean isAltGrDown() {
		return altGrDown;
	}

	public void setAltGrDown(boolean altGrDown) {
		this.altGrDown = altGrDown;
	}

	public int getModifiers() {
		int result = 0;
		if (isShiftDown()) {
			result |= InputEvent.SHIFT_DOWN_MASK;
		}
		if (isCtrlDown()) {
			result |= InputEvent.CTRL_DOWN_MASK;
		}
		if (isMetaDown()) {
			result |= InputEvent.META_DOWN_MASK;
		}
		if (isAltDown()) {
			result |= InputEvent.ALT_DOWN_MASK;
		}
		if (isAltGrDown()) {
			result |= InputEvent.ALT_GRAPH_DOWN_MASK;
		}
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (altDown ? 1231 : 1237);
		result = prime * result + (altGrDown ? 1231 : 1237);
		result = prime * result + (ctrlDown ? 1231 : 1237);
		result = prime * result + (metaDown ? 1231 : 1237);
		result = prime * result + (shiftDown ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyboardShortcut other = (KeyboardShortcut) obj;
		if (altDown != other.altDown)
			return false;
		if (altGrDown != other.altGrDown)
			return false;
		if (ctrlDown != other.ctrlDown)
			return false;
		if (metaDown != other.metaDown)
			return false;
		if (shiftDown != other.shiftDown)
			return false;
		return true;
	}

	@Override
	public String toString() {
		String result = keyName;
		if (metaDown) {
			result = "Meta+" + result;
		}
		if (shiftDown) {
			result = "Shift+" + result;
		}
		if (altGrDown) {
			result = "AltGr+" + result;
		}
		if (altDown) {
			result = "Alt+" + result;
		}
		if (ctrlDown) {
			result = "Ctrl+" + result;
		}
		return result;
	}

}