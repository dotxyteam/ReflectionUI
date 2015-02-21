import java.awt.Window;
import java.util.Date;

import javax.swing.JOptionPane;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoProxyConfiguration;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfoSource;

public class GettingStarted {

	public static void main(String[] args) {
		/* The Hello world: */
		Object myObject = new Date();
		ReflectionUI reflectionUI = new ReflectionUI();
		reflectionUI.openObjectFrame(myObject, "Hello world", null);

		/* You can open a dialog instead of a frame: */
		reflectionUI.openObjectDialog(null, myObject, "dialog instead", null,
				true);

		/* You can just create a form and then insert it in any container: */
		JOptionPane.showMessageDialog(null,
				reflectionUI.createObjectForm(myObject));

		/*
		 * In order to customize the UI behavior, just override the ReflectionUI
		 * object. For instance let set any window size to 300x300:
		 */
		reflectionUI = new ReflectionUI() {
			@Override
			public void adjustWindowBounds(Window window) {
				super.adjustWindowBounds(window);
				window.setSize(300, 300);
			}
		};
		reflectionUI.openObjectDialog(null, myObject, "300x300 window", null,
				true);

		/*
		 * If you specifically want to take control over the object discovery
		 * and interpretation process, then you must override the getTypeInfo()
		 * method. For instance we can uppercase all the field captions
		 */
		reflectionUI = new ReflectionUI() {
			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyConfiguration() {
					@Override
					protected String getCaption(IFieldInfo field,
							ITypeInfo containingType) {
						return super.getCaption(field, containingType)
								.toUpperCase();
					}
				}.get(super.getTypeInfo(typeSource));
			}

		};
		reflectionUI.openObjectDialog(null, myObject,
				"uppercase field captions", null, true);

	}

}
