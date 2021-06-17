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
package xy.reflect.ui.info.type.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Factory that generates virtual enumeration type information from the list of
 * descendants of the specified polymorphic type. The base polymorphic type
 * itself is added as an item to the resulting enumeration if it is a concrete
 * type. Note that it is then wrapped with a proxy that trigger a
 * {@link RecursivePolymorphismDetectionException} when it is reused as the base
 * polymorphic type with another {@link PolymorphicTypeOptionsFactory}. This is
 * intended to prevent an infinite recursive enumeration of type options.
 * 
 * @author olitank
 *
 */
public class PolymorphicTypeOptionsFactory extends GenericEnumerationFactory {

	protected static final String RECURSIVE_POLYMORPHISM_DETECTED_PROPERTY_KEY = PolymorphicTypeOptionsFactory.class
			.getName() + ".RECURSIVE_POLYMORPHISM_DETECTED";

	protected ITypeInfo polymorphicType;

	public PolymorphicTypeOptionsFactory(ReflectionUI reflectionUI, ITypeInfo polymorphicType) {
		super(reflectionUI, getTypeOptionsCollector(reflectionUI, polymorphicType),
				"SubTypesEnumeration [polymorphicType=" + polymorphicType.getName() + "]", "", false, false);
		this.polymorphicType = polymorphicType;
	}

	protected static Iterable<ITypeInfo> getTypeOptionsCollector(ReflectionUI reflectionUI,
			final ITypeInfo polymorphicType) {
		return new Iterable<ITypeInfo>() {

			@Override
			public Iterator<ITypeInfo> iterator() {
				List<ITypeInfo> result = new ArrayList<ITypeInfo>();
				if (polymorphicType.isConcrete()) {
					result.add(0, detectRecursivity(reflectionUI, polymorphicType));
				}
				result.addAll(listDescendantTypes(polymorphicType));
				return result.iterator();
			}

		};

	}

	protected static List<ITypeInfo> listDescendantTypes(ITypeInfo polymorphicType) {
		List<ITypeInfo> result = new ArrayList<ITypeInfo>();
		List<ITypeInfo> subTypes = polymorphicType.getPolymorphicInstanceSubTypes();
		if (subTypes != null) {
			for (ITypeInfo subType : subTypes) {
				result.add(subType);
				result.addAll(listDescendantTypes(subType));
			}
		}
		return result;
	}

	protected static ITypeInfo detectRecursivity(ReflectionUI reflectionUI, final ITypeInfo type) {
		if (isRecursivityDetected(type)) {
			throw new RecursivePolymorphismDetectionException();
		}
		final ITypeInfoSource typeSource = type.getSource();
		final ITypeInfo unwrappedType = typeSource.getTypeInfo();
		final ITypeInfo[] blockedPolymorphismType = new ITypeInfo[1];
		blockedPolymorphismType[0] = new InfoProxyFactory() {

			@Override
			protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
				Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(type));
				result.put(RECURSIVE_POLYMORPHISM_DETECTED_PROPERTY_KEY, Boolean.TRUE);
				return result;
			}

			@Override
			public String getIdentifier() {
				return "PolymorphicRecursionBlocker [polymorphicType=" + type.getName() + "]";
			}

			@Override
			protected ITypeInfoSource getSource(ITypeInfo type) {
				return new PrecomputedTypeInfoSource(blockedPolymorphismType[0],
						typeSource.getSpecificitiesIdentifier());
			}

		}.wrapTypeInfo(unwrappedType);
		ITypeInfo result = reflectionUI.getTypeInfo(blockedPolymorphismType[0].getSource());
		return result;
	}

	public static boolean isRecursivityDetected(ITypeInfo type) {
		return Boolean.TRUE.equals(type.getSpecificProperties().get(RECURSIVE_POLYMORPHISM_DETECTED_PROPERTY_KEY));
	}

	public List<ITypeInfo> getTypeOptions() {
		List<ITypeInfo> result = new ArrayList<ITypeInfo>();
		for (Object item : getOrLoadItems()) {
			result.add((ITypeInfo) item);
		}
		return result;
	}

	/**
	 * @param instance The instance to analyze.
	 * @return the type information among {@link #getTypeOptions()} that best fits
	 *         the given instance. Note that the base polymorphic type may be a
	 *         valid option (because it is a concrete type for instance). Descendant
	 *         types have precedence over their ancestors. The actual instance type
	 *         may also be a sub-type of one of the type options.
	 * @throws ReflectionUIError If any ambiguity or inconsistency is detected.
	 */
	public ITypeInfo guessSubType(Object instance) throws ReflectionUIError {
		List<ITypeInfo> options = new ArrayList<ITypeInfo>(getTypeOptions());
		ITypeInfo polymorphicTypeAsValidOption = null;
		ITypeInfo validSubType = null;
		for (ITypeInfo type : options) {
			if (type.supports(instance)) {
				if (type.getName().equals(polymorphicType.getName())) {
					polymorphicTypeAsValidOption = type;
				} else {
					if (validSubType == null) {
						validSubType = type;
						continue;
					}
					if (listDescendantTypes(validSubType).contains(type)) {
						validSubType = type;
						continue;
					}
					if (listDescendantTypes(type).contains(validSubType)) {
						continue;
					}
					throw new ReflectionUIError(
							"Failed to guess the polymorphic value type: Ambiguity detected: More than 1 valid types found:"
									+ "\n- " + validSubType.getName() + "\n- " + type.getName());
				}
			}
		}
		if (validSubType != null) {
			ITypeInfo actualType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(instance));
			if (actualType.getName().equals(polymorphicType.getName())) {
				throw new ReflectionUIError(
						"Polymorphism inconsistency detected: The base type instance is supported by a sub-type : "
								+ "\n- Base polymorphic type: " + polymorphicType.getName() + "\n- Detected sub-type: "
								+ validSubType.getName() + "\n- Actual type: " + actualType.getName());
			}
			return validSubType;
		}
		if (polymorphicTypeAsValidOption != null) {
			return polymorphicTypeAsValidOption;
		}
		return null;
	}

	@Override
	protected ResourcePath getItemIconImagePath(Object arrayItem) {
		ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
		return polyTypesItem.getIconImagePath();
	}

	@Override
	protected String getItemOnlineHelp(Object arrayItem) {
		ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
		return polyTypesItem.getOnlineHelp();
	}

	@Override
	protected String getItemName(Object arrayItem) {
		ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
		return polyTypesItem.getName();
	}

	@Override
	protected String getItemCaption(Object arrayItem) {
		ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
		String result = polyTypesItem.getCaption();
		if (polyTypesItem.getName().equals(polymorphicType.getName())) {
			return ReflectionUIUtils.composeMessage("Basic", result);
		}
		return result;
	}

	@Override
	public String toString() {
		return "PolymorphicTypeOptionsFactory [polymorphicType=" + polymorphicType + "]";
	}

	public static class RecursivePolymorphismDetectionException extends ReflectionUIError {

		private static final long serialVersionUID = 1L;

	}

}
