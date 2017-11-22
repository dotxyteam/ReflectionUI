import java.util.Date;

import javax.swing.JOptionPane;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.TypeInfoProxyFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;

public class Example {

	public static void main(String[] args) {

		/* Most basic use case: */
		Object myObject = new Date();
		SwingRenderer.getDefault().openObjectFrame(myObject);

		/* You can open a dialog instead of a frame: */
		SwingRenderer.getDefault().openObjectDialog(null, myObject);

		/* You can just create a form and then insert it in any container: */
		JOptionPane.showMessageDialog(null, SwingRenderer.getDefault().createForm(myObject));

		/*
		 * If you want to take control of the object discovery and interpretation
		 * process, then you must create custom ReflectionUI and SwingRenderer objects:
		 */
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new TypeInfoProxyFactory() {

					/*
					 * For instance you can uppercase all the field captions this way:
					 */
					@Override
					protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
						return super.getCaption(field, containingType).toUpperCase();
					}

					/*
					 * By default the generated UI will allow to set <null> on non-primitive values.
					 * However usually the developers would not allow to set <null> from their UI in
					 * spite of the fact that it is allowed by the language. The methods below allow
					 * to selectively disable the nullable facet of any value displayed in the
					 * generated UI.
					 */
					@Override
					protected boolean isNullValueDistinct(IParameterInfo param, IMethodInfo method,
							ITypeInfo containingType) {
						if (containingType.getName().equals("myClass") && method.getName().equals("myMethod")
								&& param.getName().equals("myNullableField")) {
							return true;
						} else {
							return false;
						}
					}

					@Override
					protected boolean isNullValueDistinct(IFieldInfo field, ITypeInfo containingType) {
						if (containingType.getName().equals("myClass") && field.getName().equals("myNullableField")) {
							return true;
						} else {
							return false;
						}
					}

					@Override
					protected boolean isNullReturnValueDistinct(IMethodInfo method, ITypeInfo containingType) {
						if (containingType.getName().equals("myClass")
								&& method.getName().equals("myNullableReturnValueMethod")) {
							return true;
						} else {
							return false;
						}
					}

					/*
					 * if your class "toString" method (used in some field controls) is not
					 * implemented as you want then:
					 */
					@Override
					public String toString(ITypeInfo type, Object object) {
						return "overriden: " + super.toString(type, object).toUpperCase();
					}

					/*
					 * copy/cut/paste: By default this functionality is enabled only for
					 * Serializable objects. If your class does not implement the Serializable
					 * interface then override the following methods:
					 */
					@Override
					public boolean canCopy(ITypeInfo type, Object object) {
						// TODO: replace with your code
						return super.canCopy(type, object);
					}

					@Override
					public Object copy(ITypeInfo type, Object object) {
						// TODO: replace with your code
						return super.copy(type, object);
					}

					/*
					 * Many more methods can be overriden. Explore the class TypeInfoProxyFactory to
					 * find out...
					 */

				}.wrapType(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "uppercase field captions", null, false, true);

	}
}
