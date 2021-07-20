
package xy.reflect.ui.control.swing.plugin;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JColorChooser;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleFieldControlPlugin;
import xy.reflect.ui.control.swing.DialogAccessControl;
import xy.reflect.ui.control.swing.TextControl;
import xy.reflect.ui.control.swing.builder.DialogBuilder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.method.AbstractConstructorInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.ClassUtils;

/**
 * Field control plugin that allows to display and update adequately
 * {@link Color} values.
 * 
 * @author olitank
 *
 */
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
	public IFieldControlData filterDistinctNullValueControlData(final Object renderer, IFieldControlData controlData) {
		return new FieldControlDataProxy(controlData) {

			@Override
			public ITypeInfo getType() {
				return new ColorTypeInfoProxyFactory(((SwingRenderer) renderer).getReflectionUI())
						.wrapTypeInfo(super.getType());
			}

		};
	}

	protected static class ColorTypeInfoProxyFactory extends InfoProxyFactory {

		protected ReflectionUI reflectionUI;

		public ColorTypeInfoProxyFactory(ReflectionUI reflectionUI) {
			this.reflectionUI = reflectionUI;
		}

		@Override
		protected List<IMethodInfo> getConstructors(ITypeInfo type) {
			if (ColorConstructor.isCompatibleWith(type)) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>();
				result.add(new ColorConstructor(reflectionUI, type));
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

		protected ReflectionUI reflectionUI;
		protected ITypeInfo type;
		protected ITypeInfo returnType;

		public ColorConstructor(ReflectionUI reflectionUI, ITypeInfo type) {
			this.reflectionUI = reflectionUI;
			this.type = type;
		}

		@Override
		public ITypeInfo getReturnValueType() {
			if (returnType == null) {
				returnType = reflectionUI.buildTypeInfo(new TypeInfoSourceProxy(type.getSource()) {
					@Override
					public SpecificitiesIdentifier getSpecificitiesIdentifier() {
						return null;
					}

					@Override
					protected String getTypeInfoProxyFactoryIdentifier() {
						return "ContructorReturnValueTypeInfoProxyFactory [of=" + getClass().getName() + "]";
					}
				});
			}
			return returnType;
		}

		@Override
		public List<IParameterInfo> getParameters() {
			return Collections.emptyList();
		}

		@Override
		public Object invoke(Object ignore, InvocationData invocationData) {
			return Color.GRAY;
		}

		public static boolean isCompatibleWith(ITypeInfo type) {
			Class<?> colorClass;
			try {
				colorClass = ClassUtils.getCachedClassforName(type.getName());
			} catch (ClassNotFoundException e) {
				return false;
			}
			return Color.class.isAssignableFrom(colorClass);
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
		protected boolean isNullSupported() {
			return true;
		}

		@Override
		public boolean isAutoManaged() {
			return false;
		}

		@Override
		protected Component createStatusControl(IFieldControlInput input) {
			return new TextControl(swingRenderer, new FieldControlInputProxy(input) {

				@Override
				public IFieldControlData getControlData() {
					return new FieldControlDataProxy(super.getControlData()) {

						@Override
						public Object getValue() {
							return "";
						}

						@Override
						public boolean isGetOnly() {
							return false;
						}

						@Override
						public ITypeInfo getType() {
							return new DefaultTypeInfo(
									new JavaTypeInfoSource(swingRenderer.getReflectionUI(), String.class, null));
						}

					};
				}
			}) {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean refreshUI(boolean refreshStructure) {
					super.refreshUI(refreshStructure);
					Color color = (Color) ColorControl.this.data.getValue();
					textComponent.setBackground(color);
					textComponent.setOpaque(color != null);
					textComponent.setEditable(false);
					setForeground(color);
					return true;
				}
			};
		}

		@Override
		protected Component createActionControl() {
			Component result = super.createActionControl();
			if (data.isGetOnly()) {
				result.setEnabled(false);
			}
			return result;
		}

		@Override
		protected void openDialog(Component owner) {
			final DialogBuilder dialogBuilder = swingRenderer.createDialogBuilder(owner);
			dialogBuilder.setTitle("Choose a color");
			Color initialColor = statusControl.getForeground();
			JColorChooser colorChooser = new JColorChooser(initialColor != null ? initialColor : Color.white);
			dialogBuilder.setContentComponent(colorChooser);
			dialogBuilder.setButtonBarControls(
					new ArrayList<Component>(dialogBuilder.createStandardOKCancelDialogButtons(null, null)));
			swingRenderer.showDialog(dialogBuilder.createDialog(), true);
			if (!dialogBuilder.getCreatedDialog().wasOkPressed()) {
				return;
			}
			Color newColor = colorChooser.getColor();
			data.setValue(newColor);
			((TextControl) statusControl).refreshUI(false);
		}

		@Override
		public String toString() {
			return "ColorControl [data=" + data + "]";
		}

	}

}
