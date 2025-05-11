package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;

import javax.swing.SwingUtilities;

import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Field control intended to be displayed when a standard field control creation
 * fails.
 * 
 * @author olitank
 *
 */
public class ErrorDisplayControl extends ControlPanel implements IAdvancedFieldControl {

	private static final long serialVersionUID = 1L;

	protected IFieldControlInput input;
	protected IFieldControlData data;
	protected Throwable error;
	protected SwingRenderer swingRenderer;

	public ErrorDisplayControl(final SwingRenderer swingRenderer, IFieldControlInput input, final Throwable error) {
		this.swingRenderer = swingRenderer;
		this.input = input;
		this.data = input.getControlData();
		this.error = error;
		setLayout(new BorderLayout());
		add(swingRenderer.createForm(data.isGetOnly() ? new GetOnlyErrorDisplay() : new ErrorDisplay()),
				BorderLayout.CENTER);
		setBorder(SwingRendererUtils.getErrorBorder());
	}

	@Override
	public boolean showsCaption() {
		return false;
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		return false;
	}

	@Override
	public void validateSubForms(ValidationSession session) throws Exception {
		throw new ReflectionUIError(error);
	}

	@Override
	public void addMenuContributions(MenuModel menuModel) {
	}

	@Override
	public boolean requestCustomFocus() {
		return false;
	}

	@Override
	public boolean isAutoManaged() {
		return false;
	}

	@Override
	public boolean displayError(Throwable error) {
		return false;
	}

	public class GetOnlyErrorDisplay {

		public String get() {
			return swingRenderer.prepareMessageToDisplay("An error occured: " + MiscUtils.getPrettyErrorMessage(error));
		}

		public void showErrorDetails() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					swingRenderer.openErrorDetailsDialog(ErrorDisplayControl.this, error);
				}
			});
		}

	}

	public class ErrorDisplay extends GetOnlyErrorDisplay {

		public void reset() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					Object newValue = swingRenderer.onTypeInstantiationRequest(ErrorDisplayControl.this,
							data.getType());
					if (newValue == null) {
						return;
					}
					data.setValue(newValue);
				}
			});
		}
	}

}