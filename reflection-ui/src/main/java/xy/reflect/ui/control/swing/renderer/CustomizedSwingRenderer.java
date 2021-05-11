/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.control.swing.renderer;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.SystemProperties;
import xy.reflect.ui.util.ReflectionUIError;

/**
 * A sub-class of {@link SwingRenderer} supporting customizations by default.
 * 
 * @author olitank
 *
 */
public class CustomizedSwingRenderer extends SwingRenderer {

	public static void main(String[] args) throws Exception {
		Class<?> clazz = Object.class;
		String usageText = "Expected arguments: [ <className> | --help ]"
				+ "\n  => <className>: Fully qualified name of a class to instanciate and display in a window"
				+ "\n  => --help: Displays this help message" + "\n"
				+ "\nAdditionally, the following JVM properties can be set:" + "\n" + SystemProperties.describe();
		if (args.length == 0) {
			clazz = Object.class;
		} else if (args.length == 1) {
			if (args[0].equals("--help")) {
				System.out.println(usageText);
				return;
			} else {
				clazz = Class.forName(args[0]);
			}
		} else {
			throw new IllegalArgumentException(usageText);
		}
		ReflectionUI reflectionUI = CustomizedSwingRenderer.getDefault().getReflectionUI();
		Object object = CustomizedSwingRenderer.getDefault().onTypeInstanciationRequest(null,
				reflectionUI.getTypeInfo(new JavaTypeInfoSource(reflectionUI, clazz, null)), null);
		if (object == null) {
			return;
		}
		CustomizedSwingRenderer.getDefault().openObjectFrame(object);
	}

	protected static CustomizedSwingRenderer defaultInstance;

	/**
	 * @return the default instance of this class. This instance is constructed with
	 *         the {@link CustomizedUI#getDefault()} return value.
	 */
	public static CustomizedSwingRenderer getDefault() {
		if (defaultInstance == null) {
			Class<?> customClass = SystemProperties.getAlternateDefaultCustomizedSwingRendererClass();
			if (customClass != null) {
				try {
					defaultInstance = (CustomizedSwingRenderer) customClass.getMethod("getDefault").invoke(null);
				} catch (Exception e) {
					throw new ReflectionUIError(e);
				}
			} else {
				defaultInstance = new CustomizedSwingRenderer(CustomizedUI.getDefault());
			}
		}
		return defaultInstance;
	}

	public CustomizedSwingRenderer(CustomizedUI customizedUI) {
		super(customizedUI);
	}

	public CustomizedSwingRenderer() {
		this(CustomizedUI.getDefault());
	}

	public CustomizedUI getCustomizedUI() {
		return (CustomizedUI) getReflectionUI();
	}

	public InfoCustomizations getInfoCustomizations() {
		return getCustomizedUI().getInfoCustomizations();
	}

}
