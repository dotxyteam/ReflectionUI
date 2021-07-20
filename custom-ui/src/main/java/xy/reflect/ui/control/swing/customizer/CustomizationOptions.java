
package xy.reflect.ui.control.swing.customizer;

/**
 * This class holds global editing options for WYSIWYG UI customizations.
 * 
 * @author olitank
 *
 */
public class CustomizationOptions {

	protected final SwingCustomizer swingCustomizer;

	protected boolean inEditMode = true;
	protected boolean fieldSharedTypeOptionsDisplayed = true;

	public CustomizationOptions(SwingCustomizer swingCustomizer) {
		this.swingCustomizer = swingCustomizer;
	}

	public boolean isInEditMode() {
		return inEditMode;
	}

	public void setInEditMode(boolean inEditMode) {
		this.inEditMode = inEditMode;
	}

	public boolean areFieldSharedTypeOptionsDisplayed() {
		return fieldSharedTypeOptionsDisplayed;
	}

	public void setFieldSharedTypeOptionsDisplayed(boolean fieldSharedTypeOptionsDisplayed) {
		this.fieldSharedTypeOptionsDisplayed = fieldSharedTypeOptionsDisplayed;
	}

}
