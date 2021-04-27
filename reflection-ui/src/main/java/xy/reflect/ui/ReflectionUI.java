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
package xy.reflect.ui;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.google.common.cache.CacheBuilder;

import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.app.ApplicationInfoProxy;
import xy.reflect.ui.info.app.DefaultApplicationInfo;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SystemProperties;

/**
 * This class reads and interprets the metadata (usually the class) of objects
 * in order to propose an abstract UI model (ITypeInfo) that a renderer can use
 * to generate a working UI.
 * 
 * @author olitank
 *
 */
public class ReflectionUI {

	protected static ReflectionUI defaultInstance;

	protected Map<Object, ITypeInfo> precomputedTypeInfoByObject = CacheBuilder.newBuilder().weakKeys()
			.<Object, ITypeInfo>build().asMap();

	/**
	 * Constructs an instance of this class.
	 */
	public ReflectionUI() {
	}

	/**
	 * @return the default instance of this class.
	 */
	public static ReflectionUI getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new ReflectionUI() {

				@Override
				public IApplicationInfo getApplicationInfo() {
					return new ApplicationInfoProxy(super.getApplicationInfo()) {

						@Override
						public boolean isSystemIntegrationCrossPlatform() {
							return true;
						}

						@Override
						public ColorSpecification getTitleBackgroundColor() {
							return SwingRendererUtils.getColorSpecification(Color.LIGHT_GRAY);
						}

						@Override
						public ColorSpecification getTitleForegroundColor() {
							return SwingRendererUtils.getColorSpecification(Color.DARK_GRAY);
						}

					};
				}

			};
		}
		return defaultInstance;
	}

	/**
	 * Allows to associate an object with a predefined {@link ITypeInfo} instance.
	 * 
	 * @param object The object to associate with the type information.
	 * @param type   The ITypeInfo instance to associate with the object.
	 */
	public void registerPrecomputedTypeInfoObject(Object object, ITypeInfo type) {
		precomputedTypeInfoByObject.put(object, type);
	}

	/**
	 * Allows to break the association between an object and a predefined
	 * {@link ITypeInfo} instance.
	 * 
	 * @param object The object that was associated with the type information.
	 */
	public void unregisterPrecomputedTypeInfoObject(Object object) {
		precomputedTypeInfoByObject.remove(object);
	}

	/**
	 * @param object Any object from which a UI needs to be generated.
	 * @return a metadata object from which a type information will be extracted.
	 */
	public ITypeInfoSource getTypeInfoSource(Object object) {
		ITypeInfo precomputedType = precomputedTypeInfoByObject.get(object);
		if (precomputedType != null) {
			return new PrecomputedTypeInfoSource(precomputedType, null);
		} else {
			return new JavaTypeInfoSource(object.getClass(), null);
		}
	}

	/**
	 * @param typeInfoSource The data object needed to generate the UI-oriented type
	 *                       information.
	 * @return an object containing the UI-oriented type information extracted from
	 *         the given source. Note that the calling {@link ITypeInfo#getSource()}
	 *         on the result should return an object equals to the given source.
	 */
	public ITypeInfo getTypeInfo(ITypeInfoSource typeInfoSource) {
		return typeInfoSource.getTypeInfo(this);
	}

	/**
	 * @return the UI-oriented application (global) information.
	 */
	public IApplicationInfo getApplicationInfo() {
		return new DefaultApplicationInfo();
	}

	protected String formatLogMessage(String msg) {
		return SimpleDateFormat.getDateTimeInstance().format(new Date()) + " [" + ReflectionUI.class.getSimpleName()
				+ "] " + msg;
	}

	public void logDebug(String msg) {
		if (!SystemProperties.isDebugModeActive()) {
			return;
		}
		System.out.println(formatLogMessage("DEBUG - " + msg));
	}

	public void logDebug(Throwable t) {
		logDebug(ReflectionUIUtils.getPrintedStackTrace(t));
	}

	public void logError(String msg) {
		System.err.println(formatLogMessage("ERROR - " + msg));
	}

	public void logError(Throwable t) {
		logError(ReflectionUIUtils.getPrintedStackTrace(t));
	}

	@Override
	public String toString() {
		if (this == defaultInstance) {
			return "ReflectionUI.DEFAULT";
		} else {
			return super.toString();
		}
	}

}
