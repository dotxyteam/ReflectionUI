import java.util.Date;

import javax.swing.JOptionPane;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.util.InfoProxyGenerator;

/*
 * Read carefully the comments below. 
 */
public class Example {

	public static void main(String[] args) {

		/* The Hello world: */
		Object myObject = new Date();
		SwingRenderer.DEFAULT.openObjectFrame(myObject);

		/* You can open a dialog instead of a frame: */
		SwingRenderer.DEFAULT.openObjectDialog(null, myObject, true);

		/* You can just create a form and then insert it in any container: */
		JOptionPane.showMessageDialog(null, SwingRenderer.DEFAULT.createObjectForm(myObject));

		/*
		 * You can customize some aspects (field labels, hide some methods, ...)
		 * of the generated UI by using an integrated customizations editor. To
		 * enable this editor set the following JVM properties:
		 */
		// -Dxy.reflect.ui.defaultCustomizationsActive=true
		// -Dxy.reflect.ui.defaultCustomizationsEditable=true

		/*
		 * The SwingRenderer.DEFAULT assumes that the Java coding standards are
		 * respected for the classes of the objects for which it generates UI.
		 * Otherwise adjustments can be done by overriding some methods:
		 */
		ReflectionUI reflectionUI = new ReflectionUI() {

			/* if your class "equals" method is not well implemented then: */
			@Override
			public boolean equals(Object value1, Object value2) {
				return super.equals(value1, value2);
			}

			/* if your class "toString" method is not well implemented then: */
			@Override
			public String toString(Object object) {
				return super.toString(object);
			}

			/*
			 * copy/cut/paste: By default this functionality is enabled only for
			 * Serializable objects. If your class does not implement the
			 * Serializable interface then override the following methods of the
			 * ReflectionUI object:
			 */
			@Override
			public boolean canCopy(Object object) {
				// TODO: replace with your code
				return super.canCopy(object);
			}

			@Override
			public Object copy(Object object) {
				// TODO: replace with your code
				return super.copy(object);
			}

			/*
			 * If you specifically want to take control over the object
			 * discovery and interpretation process, then you must override the
			 * getTypeInfo() method. For instance you can uppercase all the
			 * field captions this way:
			 */
			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyGenerator() {
					@Override
					protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
						return super.getCaption(field, containingType).toUpperCase();
					}
				}.get(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, true);

		swingRenderer.openObjectDialog(null, myObject, "uppercase field captions", null, true);

	}
}
