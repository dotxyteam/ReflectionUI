package xy.reflect.ui.example;

import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;

/**
 * Input Settings GUI generated using only the XML declarative customizations.
 * 
 * @author olitank
 *
 */
public class InputSettings {

	public static void main(String[] args) {
		CustomizedUI reflectionUI = new CustomizedUI();
		final SwingCustomizer renderer = new SwingCustomizer(reflectionUI,
				System.getProperty("custom-reflection-ui-examples.project.directory", "./") + "inputSettings.icu");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				renderer.openObjectDialog(null, new InputSettings());
			}
		});
	}

	private MouseSettings mouseSettings = new MouseSettings();
	private TouchpadSettings touchpadSettings = new TouchpadSettings();
	private KeyboardSettings keyboardSettings = new KeyboardSettings();

	public MouseSettings getMouseSettings() {
		return mouseSettings;
	}

	public void setMouseSettings(MouseSettings mouseSettings) {
		this.mouseSettings = mouseSettings;
	}

	public TouchpadSettings getTouchpadSettings() {
		return touchpadSettings;
	}

	public void setTouchpadSettings(TouchpadSettings touchpadSettings) {
		this.touchpadSettings = touchpadSettings;
	}

	public KeyboardSettings getKeyboardSettings() {
		return keyboardSettings;
	}

	public void setKeyboardSettings(KeyboardSettings keyboardSettings) {
		this.keyboardSettings = keyboardSettings;
	}

	public static class MouseSettings {

		private boolean leftHanded;
		private boolean pointerShownWhenCtrlKeyPressed;
		private boolean middleClickEmulated;
		private int dragAndDropThresholdPixels;
		private int pointerSize;
		private int pointerAcceleration;
		private int pointerSensitivity;
		private int doubleClickTimeoutMilliseconds;

		public boolean isLeftHanded() {
			return leftHanded;
		}

		public void setLeftHanded(boolean leftHanded) {
			this.leftHanded = leftHanded;
		}

		public boolean isPointerShownWhenCtrlKeyPressed() {
			return pointerShownWhenCtrlKeyPressed;
		}

		public void setPointerShownWhenCtrlKeyPressed(boolean pointerShownWhenCtrlKeyPressed) {
			this.pointerShownWhenCtrlKeyPressed = pointerShownWhenCtrlKeyPressed;
		}

		public boolean isMiddleClickEmulated() {
			return middleClickEmulated;
		}

		public void setMiddleClickEmulated(boolean middleClickEmulated) {
			this.middleClickEmulated = middleClickEmulated;
		}

		public int getDragAndDropThresholdPixels() {
			return dragAndDropThresholdPixels;
		}

		public void setDragAndDropThresholdPixels(int dragAndDropThresholdPixels) {
			this.dragAndDropThresholdPixels = dragAndDropThresholdPixels;
		}

		public int getPointerSize() {
			return pointerSize;
		}

		public void setPointerSize(int pointerSize) {
			this.pointerSize = pointerSize;
		}

		public int getPointerAcceleration() {
			return pointerAcceleration;
		}

		public void setPointerAcceleration(int pointerAcceleration) {
			this.pointerAcceleration = pointerAcceleration;
		}

		public int getPointerSensitivity() {
			return pointerSensitivity;
		}

		public void setPointerSensitivity(int pointerSensitivity) {
			this.pointerSensitivity = pointerSensitivity;
		}

		public int getDoubleClickTimeoutMilliseconds() {
			return doubleClickTimeoutMilliseconds;
		}

		public void setDoubleClickTimeoutMilliseconds(int doubleClickTimeoutMilliseconds) {
			this.doubleClickTimeoutMilliseconds = doubleClickTimeoutMilliseconds;
		}
	}

	public static class TouchpadSettings {

		private boolean enabled;
		private boolean clickEventFiredWhenTapping;
		private boolean disabledWHenTyping;
		private MouseButton twoFingersClickEmulation;
		private MouseButton threeFingersClickEmulation;
		private boolean scrollingDirectionReversed;
		private boolean verticalEdgeScrolling;
		private boolean horizontalEdgeScrolling;
		private boolean verticalTwoFingersScrolling;
		private boolean horizontalTwoFingersScrolling;
		private int pointerAcceleration;
		private int pointerSensitivity;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public boolean isClickEventFiredWhenTapping() {
			return clickEventFiredWhenTapping;
		}

		public void setClickEventFiredWhenTapping(boolean clickEventFiredWhenTapping) {
			this.clickEventFiredWhenTapping = clickEventFiredWhenTapping;
		}

		public boolean isDisabledWHenTyping() {
			return disabledWHenTyping;
		}

		public void setDisabledWHenTyping(boolean disabledWHenTyping) {
			this.disabledWHenTyping = disabledWHenTyping;
		}

		public MouseButton getTwoFingersClickEmulation() {
			return twoFingersClickEmulation;
		}

		public void setTwoFingersClickEmulation(MouseButton twoFingersClickEmulation) {
			this.twoFingersClickEmulation = twoFingersClickEmulation;
		}

		public MouseButton getThreeFingersClickEmulation() {
			return threeFingersClickEmulation;
		}

		public void setThreeFingersClickEmulation(MouseButton threeFingersClickEmulation) {
			this.threeFingersClickEmulation = threeFingersClickEmulation;
		}

		public boolean isScrollingDirectionReversed() {
			return scrollingDirectionReversed;
		}

		public void setScrollingDirectionReversed(boolean scrollingDirectionReversed) {
			this.scrollingDirectionReversed = scrollingDirectionReversed;
		}

		public boolean isVerticalEdgeScrolling() {
			return verticalEdgeScrolling;
		}

		public void setVerticalEdgeScrolling(boolean verticalEdgeScrolling) {
			this.verticalEdgeScrolling = verticalEdgeScrolling;
		}

		public boolean isHorizontalEdgeScrolling() {
			return horizontalEdgeScrolling;
		}

		public void setHorizontalEdgeScrolling(boolean horizontalEdgeScrolling) {
			this.horizontalEdgeScrolling = horizontalEdgeScrolling;
		}

		public boolean isVerticalTwoFingersScrolling() {
			return verticalTwoFingersScrolling;
		}

		public void setVerticalTwoFingersScrolling(boolean verticalTwoFingersScrolling) {
			this.verticalTwoFingersScrolling = verticalTwoFingersScrolling;
		}

		public boolean isHorizontalTwoFingersScrolling() {
			return horizontalTwoFingersScrolling;
		}

		public void setHorizontalTwoFingersScrolling(boolean horizontalTwoFingersScrolling) {
			this.horizontalTwoFingersScrolling = horizontalTwoFingersScrolling;
		}

		public int getPointerAcceleration() {
			return pointerAcceleration;
		}

		public void setPointerAcceleration(int pointerAcceleration) {
			this.pointerAcceleration = pointerAcceleration;
		}

		public int getPointerSensitivity() {
			return pointerSensitivity;
		}

		public void setPointerSensitivity(int pointerSensitivity) {
			this.pointerSensitivity = pointerSensitivity;
		}
	}

	public static class KeyboardSettings {
		private int repeatDelayMilliseconds;
		private int repeatIntervalMilliseconds;
		private int cursorBlinkingIntervalMilliseconds;

		public int getRepeatDelayMilliseconds() {
			return repeatDelayMilliseconds;
		}

		public void setRepeatDelayMilliseconds(int repeatDelayMilliseconds) {
			this.repeatDelayMilliseconds = repeatDelayMilliseconds;
		}

		public int getRepeatIntervalMilliseconds() {
			return repeatIntervalMilliseconds;
		}

		public void setRepeatIntervalMilliseconds(int repeatIntervalMilliseconds) {
			this.repeatIntervalMilliseconds = repeatIntervalMilliseconds;
		}

		public int getCursorBlinkingIntervalMilliseconds() {
			return cursorBlinkingIntervalMilliseconds;
		}

		public void setCursorBlinkingIntervalMilliseconds(int cursorBlinkingIntervalMilliseconds) {
			this.cursorBlinkingIntervalMilliseconds = cursorBlinkingIntervalMilliseconds;
		}
	}

	public enum MouseButton {
		RightButton, LeftButton, MiddleButton
	}
}
