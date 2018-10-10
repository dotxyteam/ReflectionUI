package xy.reflect.ui.control.swing.plugin;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleFieldControlPlugin;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.DialogBuilder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ClassUtils;

public class ColorPickerPlugin extends AbstractSimpleFieldControlPlugin {

	protected static File lastDirectory = new File(".").getAbsoluteFile();

	@Override
	public String getControlTitle() {
		return "Color Picker";
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		return javaType.equals(Color.class);
	}

	@Override
	public boolean canDisplayDistinctNullValue() {
		return false;
	}

	@Override
	public ColorControl createControl(Object renderer, IFieldControlInput input) {
		return new ColorControl((SwingRenderer) renderer, input);
	}

	@Override
	public IFieldControlData filterDistinctNullValueControlData(IFieldControlData controlData) {
		return new FieldControlDataProxy(controlData) {

			@Override
			public ITypeInfo getType() {
				return new ColorTypeInfoProxyFactory().wrapTypeInfo(super.getType());
			}

		};
	}

	protected static class ColorTypeInfoProxyFactory extends InfoProxyFactory {

		@Override
		protected List<IMethodInfo> getConstructors(ITypeInfo type) {
			if (ColorConstructor.isCompatibleWith(type)) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>();
				result.add(new ColorConstructor(type));
				return result;
			}
			return super.getConstructors(type);
		}

		@Override
		protected boolean isConcrete(ITypeInfo type) {
			if (ColorConstructor.isCompatibleWith(type)) {
				return true;
			}
			return super.isConcrete(type);
		}

	}

	protected static class ColorConstructor extends AbstractConstructorInfo {

		private ITypeInfo type;

		public ColorConstructor(ITypeInfo type) {
			this.type = type;
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return type;
		}

		@Override
		public List<IParameterInfo> getParameters() {
			return Collections.emptyList();
		}

		@Override
		public Object invoke(Object parentObject, InvocationData invocationData) {
			return Color.GRAY;
		}

		public static boolean isCompatibleWith(ITypeInfo type) {
			Class<?> fileClass;
			try {
				fileClass = ClassUtils.getCachedClassforName(type.getName());
			} catch (ClassNotFoundException e) {
				return false;
			}
			return Color.class.isAssignableFrom(fileClass);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((type == null) ? 0 : type.hashCode());
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
			ColorConstructor other = (ColorConstructor) obj;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ColorConstructor [type=" + type + "]";
		}

	}

	public class ColorControl extends DialogAccessControl {
		protected static final long serialVersionUID = 1L;

		public ColorControl(SwingRenderer swingRenderer, IFieldControlInput input) {
			super(swingRenderer, input);
		}

		@Override
		public boolean refreshUI(boolean refreshStructure) {
			if (refreshStructure) {
				actionControl.setEnabled(!data.isGetOnly());
			}
			return super.refreshUI(refreshStructure);
		}

		@Override
		public boolean handlesModificationStackAndStress() {
			return false;
		}

		@Override
		protected JLabel createStatusControl(IFieldControlInput input) {
			JLabel result = new JLabel(" ");
			result.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
			return result;
		}

		@Override
		protected void updateStatusControl(boolean refreshStructure) {
			Color newColor = (Color) data.getValue();
			((JLabel) statusControl).setOpaque(true);
			((JLabel) statusControl).setBackground(newColor);
		}

		@Override
		protected void openDialog(Component owner) {
			final DialogBuilder dialogBuilder = swingRenderer.getDialogBuilder(owner);
			dialogBuilder.setTitle("Choose a color");
			Color initialColor = statusControl.getBackground();
			JColorChooser colorChooser = new JColorChooser(initialColor != null ? initialColor : Color.white);
			dialogBuilder.setContentComponent(colorChooser);
			dialogBuilder.setToolbarComponentsAccessor(new Accessor<List<Component>>() {
				@Override
				public List<Component> get() {
					return new ArrayList<Component>(dialogBuilder.createStandardOKCancelDialogButtons(null, null));
				}
			});
			swingRenderer.showDialog(dialogBuilder.createDialog(), true);
			if (!dialogBuilder.wasOkPressed()) {
				return;
			}
			Color newColor = colorChooser.getColor();
			data.setValue(newColor);
			refreshUI(false);
		}

		@Override
		public String toString() {
			return "ColorControl [data=" + data + "]";
		}

	}

}
