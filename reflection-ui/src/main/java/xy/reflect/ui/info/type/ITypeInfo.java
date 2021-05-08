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
package xy.reflect.ui.info.type;

import java.awt.Dimension;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;

/**
 * This interface allows to specify UI-oriented type information of objects.
 * This information is typically inferred from object classes.
 * 
 * @author olitank
 *
 */
public interface ITypeInfo extends IInfo {

	/**
	 * @return the source object from which this type information was created.
	 */
	ITypeInfoSource getSource();

	/**
	 * @return true if and only if objects of this type can be persisted via
	 *         {@link #save(Object, OutputStream)} and
	 *         {@link #load(Object, InputStream)} methods.
	 */
	boolean canPersist();

	/**
	 * Saves the given object state to the output stream.
	 * 
	 * @param object An object of the current type.
	 * @param out    The output stream.
	 */
	void save(Object object, OutputStream out);

	/**
	 * Loads the given object state from the input stream.
	 * 
	 * @param object An object of the current type.
	 * @param in     The input stream.
	 */
	void load(Object object, InputStream in);

	/**
	 * @return true if and only if objects of the current type should be considered
	 *         as immutable.
	 */
	boolean isImmutable();

	/**
	 * @return true if and only if objects of the current type are primitive.
	 */
	boolean isPrimitive();

	/**
	 * @return true if and only if the current type is concrete. Otherwise the type
	 *         is considered as abstract and thus instances cannot be created from
	 *         it.
	 */
	boolean isConcrete();

	/**
	 * @return the list of constructors of this type.
	 */
	List<IMethodInfo> getConstructors();

	/**
	 * @return the list of fields of this type.
	 */
	List<IFieldInfo> getFields();

	/**
	 * @return the list of methods of this type.
	 */
	List<IMethodInfo> getMethods();

	/**
	 * @param object Any object.
	 * @return whether the current type is compatible with the given object.
	 */
	boolean supportsInstance(Object object);

	/**
	 * @return the list of known types derived from the current one.
	 */
	List<ITypeInfo> getPolymorphicInstanceSubTypes();

	/**
	 * @param object Any object of the current type.
	 * @return the textual representation of the given object. This method allows to
	 *         provide an alternative to the {@link Object#toString()} method of the
	 *         given object for generated UIs.
	 */
	String toString(Object object);

	/**
	 * Validates the state of the given object. An exception should be thrown if the
	 * object state is not valid. Otherwise the object is considered as valid.
	 * 
	 * @param object Any object of the current type.
	 * @throws Exception If the state of the given object is not valid.
	 */
	void validate(Object object) throws Exception;

	/**
	 * @param object Any object of the current type.
	 * @return true if and only if the given object can be duplicated. Otherwise the
	 *         {@link #copy(Object)} method should not be called.
	 */
	boolean canCopy(Object object);

	/**
	 * @param object Any object of the current type.
	 * @return a copy of the given object.
	 */
	Object copy(Object object);

	/**
	 * @return true if and only if the undo/redo/etc features should be made
	 *         available typically when an object of the current type is the root
	 *         one displayed in a window.
	 */
	boolean isModificationStackAccessible();

	/**
	 * @return the location of an icon image resource that should represent objects
	 *         of the current type or null.
	 */
	ResourcePath getIconImagePath();

	/**
	 * @return the layout strategy to be used when displaying the fields of objects
	 *         of the current type (or null if the default layout strategy should be
	 *         used).
	 */
	ITypeInfo.FieldsLayout getFieldsLayout();

	/**
	 * @return the layout strategy to be used when displaying the methods of objects
	 *         of the current type (or null if the default layout strategy should be
	 *         used).
	 */
	ITypeInfo.MethodsLayout getMethodsLayout();

	/**
	 * @return the descriptor of menu bar elements provided for objects of the
	 *         current type.
	 */
	MenuModel getMenuModel();

	/**
	 * This method is called by the renderer when the visibility of the form
	 * generated for the given object changes.
	 * 
	 * @param object  Any object of the current type.
	 * @param visible Is true when the form becomes visible, false when it becomes
	 *                invisible.
	 * @return whether an update of the form is required.
	 */
	boolean onFormVisibilityChange(Object object, boolean visible);

	/**
	 * @return the preferred size (in pixels) of forms generated from this type.
	 */
	Dimension getFormPreferredSize();

	/**
	 * @return background color of forms generated from this type or null if the
	 *         default background color should be used.
	 */
	ColorSpecification getFormBackgroundColor();

	/**
	 * @return text color of forms generated from this type or null if the default
	 *         text color should be used.
	 */
	ColorSpecification getFormForegroundColor();

	/**
	 * @return border color used by forms generated from this type or null if the
	 *         default borders should be used.
	 */
	ColorSpecification getFormBorderColor();

	/**
	 * @return the resource location of a background image displayed on forms
	 *         generated from this type or null if no background image should be
	 *         used.
	 */
	ResourcePath getFormBackgroundImagePath();

	/**
	 * @return the display style of the member categories control.
	 */
	CategoriesStyle getCategoriesStyle();

	/**
	 * @return the background color of the categories control generated from this
	 *         type or null if the default background color should be used.
	 */
	ColorSpecification getCategoriesBackgroundColor();

	/**
	 * @return the text color of the categories control generated from this type or
	 *         null if the default text color should be used.
	 */
	ColorSpecification getCategoriesForegroundColor();

	/**
	 * @return the editable text color of controls generated from this type or null
	 *         if the default text color should be used.
	 */
	ColorSpecification getFormEditorsForegroundColor();

	/**
	 * @return the editable background color of controls generated from this type or
	 *         null if the default background color should be used.
	 */
	ColorSpecification getFormEditorsBackgroundColor();

	/**
	 * @return the background color of the buttons generated from this type or null
	 *         if the default background color should be used.
	 */
	ColorSpecification getFormButtonBackgroundColor();

	/**
	 * @return the text color of the buttons generated from this type or null if the
	 *         default text color should be used.
	 */
	ColorSpecification getFormButtonForegroundColor();

	/**
	 * @return the resource path to a background image for the buttons generated
	 *         from this type or null if no background image should be used.
	 */
	ResourcePath getFormButtonBackgroundImagePath();

	/**
	 * @return the border color of the buttons generated from this type or null if
	 *         the default border should be used.
	 */
	ColorSpecification getFormButtonBorderColor();

	/**
	 * Fields layout strategy class.
	 * 
	 * @author olitank
	 *
	 */
	public enum FieldsLayout {
		HORIZONTAL_FLOW, VERTICAL_FLOW
	}

	/**
	 * Methods layout strategy class.
	 * 
	 * @author olitank
	 *
	 */
	public enum MethodsLayout {
		HORIZONTAL_FLOW, VERTICAL_FLOW
	}

	/**
	 * Display style of member categories control, subject to interpretation
	 * according to the renderer.
	 * 
	 * @author olitank
	 *
	 */
	public enum CategoriesStyle {
		MODERN, MODERN_VERTICAL;

		public static CategoriesStyle getDefault() {
			return MODERN;
		}
	}

}
