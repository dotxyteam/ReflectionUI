package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;

import javax.swing.SwingUtilities;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.ChangedTypeNameFactory;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.PrecomputedTypeInstanceWrapper;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * Field control that displays an error. Note that it is intended to be
 * displayed typically when a standard field control creation fails.
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
		add(swingRenderer.createForm(buildDisplayObject()), BorderLayout.CENTER);
		setBorder(swingRenderer.getErrorBorder());
	}

	protected Object buildDisplayObject() {
		ErrorDisplay errorDisplay = data.isGetOnly() ? new ErrorDisplay() : new RecoverableErrorDisplay();
		Class<? extends ErrorDisplay> errorDisplayClass = errorDisplay.getClass();
		JavaTypeInfoSource typeSource = new JavaTypeInfoSource(errorDisplayClass, null);
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = typeSource.buildTypeInfo(reflectionUI);
		InfoProxyFactory proxyFactory = new ChangedTypeNameFactory(swingRenderer.getReflectionUI(),
				errorDisplayClass.getName(), "ErrorDisplayType [context=" + input.getContext().getIdentifier() + "]") {

			@Override
			public String getIdentifier() {
				return "ErrorDisplayWrappingFactory [context=" + input.getContext().getIdentifier() + "]";
			}

			@Override
			protected boolean isReadOnly(IMethodInfo method, ITypeInfo objectType) {
				return true;
			}

		};
		type = proxyFactory.wrapTypeInfo(type);
		return new PrecomputedTypeInstanceWrapper(errorDisplay, type);
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
	public void validateControlData(ValidationSession session) throws Exception {
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
	public boolean isModificationStackManaged() {
		return false;
	}

	@Override
	public boolean areValueAccessErrorsManaged() {
		return false;
	}

	@Override
	public boolean displayError(Throwable error) {
		return false;
	}

	public class ErrorDisplay {

		public String get() {
			return swingRenderer.prepareMessageToDisplay(MiscUtils.getPrettyErrorMessage(error));
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

	public class RecoverableErrorDisplay extends ErrorDisplay {

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