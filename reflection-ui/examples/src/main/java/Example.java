import java.util.Date;

import javax.swing.JOptionPane;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.TypeInfoProxyFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;

public class Example {

	public static void main(String[] args) {

		/* Most basic use case: */
		Object myObject = new Date();
		SwingRenderer.getDefault().openObjectFrame(myObject);
		/*
		 * You can customize a lot of aspects of the generated UI by using the
		 * integrated customizations editor (displayed by default). When you are
		 * done customizing, the customizations editor must be hidden by setting
		 * the following JVM property:
		 * "-Dxy.reflect.ui.infoCustomizationsToolsHidden=true".
		 */

		/* You can open a dialog instead of a frame: */
		SwingRenderer.getDefault().openObjectDialog(null, myObject);

		/* You can just create a form and then insert it in any container: */
		JOptionPane.showMessageDialog(null, SwingRenderer.getDefault().createForm(myObject));

		/*
		 * If you want to take control of the object discovery and
		 * interpretation process, then you must create custom ReflectionUI and
		 * SwingRenderer objects:
		 */
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new TypeInfoProxyFactory() {

					/*
					 * For instance you can uppercase all the field captions
					 * this way:
					 */
					@Override
					protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
						return super.getCaption(field, containingType).toUpperCase();
					}

					/*
					 * if your class "toString" method (used in some field
					 * controls) is not implemented as you want then:
					 */
					@Override
					public String toString(ITypeInfo type, Object object) {
						return "overriden: " + super.toString(type, object).toUpperCase();
					}

					/*
					 * copy/cut/paste: By default this functionality is enabled
					 * only for Serializable objects. If your class does not
					 * implement the Serializable interface then override the
					 * following methods:
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
					 * Many more methods can be overriden. Explore the class
					 * TypeInfoProxyFactory to find out...
					 */

				}.get(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "uppercase field captions", null, false, true);

	}
}
