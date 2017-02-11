import java.util.Date;

import javax.swing.JOptionPane;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.util.TypeInfoProxyFactory;
import xy.reflect.ui.util.SystemProperties;

/*
 * Read carefully the comments below. 
 */
public class Example {

	public static void main(String[] args) {

		/* The Hello world: */
		Object myObject = new Date();
		SwingRenderer.getDefault().openObjectFrame(myObject);

		/* You can open a dialog instead of a frame: */
		SwingRenderer.getDefault().openObjectDialog(null, myObject);

		/* You can just create a form and then insert it in any container: */
		JOptionPane.showMessageDialog(null, SwingRenderer.getDefault().createForm(myObject));

		/*
		 * You can customize some aspects (field labels, hide some methods, ...)
		 * of the generated UI by using an integrated customizations editor. To
		 * enable this editor set the following JVM property:
		 * "-Dxy.reflect.ui.defaultCustomizationsActive=true". You can also do
		 * it programmatically before any call to SwingRenderer.getDefault():
		 */
		System.setProperty(SystemProperties.DEFAULT_INFO_CUSTOMIZATIONS_ACTIVE, "true");

		/*
		 * When you are done customizing the UI the customizations editor must
		 * be hidden by setting the following JVM property:
		 * "-Dxy.reflect.ui.infoCustomizationsToolsHidden=true". You can also do
		 * it programmatically before any call to SwingRenderer.getDefault():
		 */
		System.setProperty(SystemProperties.HIDE_INFO_CUSTOMIZATIONS_TOOLS, "true");

		/*
		 * SwingRenderer.getDefault() assumes that the Java coding standards are
		 * respected for the classes of the objects for which it generates UI.
		 * If you want to take control over the object discovery and
		 * interpretation process, then you must create custom ReflectionUI and
		 * SwingRenderer objects and override the ReflectionUI.getTypeInfo(...)
		 * method:
		 */
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new TypeInfoProxyFactory() {

					@Override
					public String toString() {
						return Example.class.getName() + TypeInfoProxyFactory.class.getSimpleName();
					}

					/*
					 * For instance you can uppercase all the field captions
					 * this way:
					 */
					@Override
					protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
						return super.getCaption(field, containingType).toUpperCase();
					}

					/*
					 * if your class "equals" method is not implemented as you
					 * want then:
					 */
					@Override
					public boolean equals(ITypeInfo type, Object value1, Object value2) {
						return super.equals(type, value1, value2);
					}

					/*
					 * if your class "toString" method is not implemented as you
					 * want then:
					 */
					@Override
					public String toString(ITypeInfo type, Object object) {
						return super.toString(type, object);
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

				}.get(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "uppercase field captions", null, false, true);

	}
}
