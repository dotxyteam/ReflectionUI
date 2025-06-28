
package xy.reflect.ui.util;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.text.NumberFormatter;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.MethodControlDataProxy;
import xy.reflect.ui.control.plugin.IFieldControlPlugin;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.ITransaction;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.custom.InfoCustomizations.AbstractFileMenuItemCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.AbstractMenuItemCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ExitMenuItemCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.HelpMenuItemCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.IMenuElementCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.IMenuItemContainerCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.MenuCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.MenuItemCategoryCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.MenuModelCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.OpenMenuItemCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.RedoMenuItemCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.RenewMenuItemCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ResetMenuItemCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.SaveAsMenuItemCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.SaveMenuItemCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.UndoMenuItemCustomization;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.AbstractMenuItemInfo;
import xy.reflect.ui.info.menu.DefaultMenuElementPosition;
import xy.reflect.ui.info.menu.IMenuElementInfo;
import xy.reflect.ui.info.menu.IMenuElementPosition;
import xy.reflect.ui.info.menu.MenuElementKind;
import xy.reflect.ui.info.menu.MenuInfo;
import xy.reflect.ui.info.menu.MenuItemCategory;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.menu.StandradActionMenuItemInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.factory.PolymorphicTypeOptionsFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.undo.AbstractModification;
import xy.reflect.ui.undo.AbstractModificationProxy;
import xy.reflect.ui.undo.CancelledModificationException;
import xy.reflect.ui.undo.FieldControlDataModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IrreversibleModificationException;
import xy.reflect.ui.undo.MethodControlDataModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.SlaveModificationStack;
import xy.reflect.ui.undo.UndoOrder;

/**
 * Utilities for dealing with {@link ReflectionUI} instances.
 * 
 * @author olitank
 *
 */
public class ReflectionUIUtils {

	public static final String METHOD_SIGNATURE_REGEX = "([^ ].*) ([^ ]+)? ?\\(([^\\)]*)\\)";

	public static String buildMethodSignature(String returnTypeName, String methodName,
			List<String> parameterTypeNames) {
		StringBuilder result = new StringBuilder();
		result.append(returnTypeName);
		result.append(" " + methodName + "(");
		for (int i = 0; i < parameterTypeNames.size(); i++) {
			String paramTypeName = parameterTypeNames.get(i);
			if (i > 0) {
				result.append(", ");
			}
			result.append(paramTypeName);
		}
		result.append(")");
		return result.toString();
	}

	public static String buildMethodSignature(IMethodInfo method) {
		ITypeInfo returnType = method.getReturnValueType();
		String returnTypeName = (returnType == null) ? "void" : returnType.getName();
		String methodName = method.getName();
		List<String> parameterTypeNames = new ArrayList<String>();
		List<IParameterInfo> params = method.getParameters();
		for (int i = 0; i < params.size(); i++) {
			IParameterInfo param = params.get(i);
			parameterTypeNames.add(param.getType().getName());
		}
		return buildMethodSignature(returnTypeName, methodName, parameterTypeNames);
	}

	public static String buildMethodSignature(Method method) {
		Class<?> returnType = method.getReturnType();
		String returnTypeName = returnType.getName();
		String methodName = method.getName();
		List<String> parameterTypeNames = new ArrayList<String>();
		Class<?>[] paramTypes = method.getParameterTypes();
		for (int i = 0; i < paramTypes.length; i++) {
			Class<?> paramType = paramTypes[i];
			parameterTypeNames.add(paramType.getName());
		}
		return buildMethodSignature(returnTypeName, methodName, parameterTypeNames);
	}

	public static String extractMethodReturnTypeNameFromSignature(String methodSignature) {
		Pattern pattern = Pattern.compile(METHOD_SIGNATURE_REGEX);
		Matcher matcher = pattern.matcher(methodSignature);
		if (!matcher.matches()) {
			return null;
		}
		String result = matcher.group(1);
		if (result != null) {
			if (!result.equals(result.trim())) {
				throw new ReflectionUIError();
			}
		}
		return result;
	}

	public static String extractMethodNameFromSignature(String methodSignature) {
		Pattern pattern = Pattern.compile(METHOD_SIGNATURE_REGEX);
		Matcher matcher = pattern.matcher(methodSignature);
		if (!matcher.matches()) {
			return null;
		}
		String result = matcher.group(2);
		if (result == null) {
			result = "";
		}
		if (!result.equals(result.trim())) {
			throw new ReflectionUIError();
		}
		return result;
	}

	public static String[] extractMethodParameterTypeNamesFromSignature(String methodSignature) {
		Pattern pattern = Pattern.compile(METHOD_SIGNATURE_REGEX);
		Matcher matcher = pattern.matcher(methodSignature);
		if (!matcher.matches()) {
			return null;
		}
		String paramListString = matcher.group(3);
		if (!paramListString.equals(paramListString.trim())) {
			throw new ReflectionUIError();
		}
		List<String> result = new ArrayList<String>();
		if (paramListString.length() > 0) {
			int openBracketCount = 0;
			int parameterTypeNameStart = 0;
			for (int i = 0; i < paramListString.length(); i++) {
				if (paramListString.charAt(i) == '[') {
					openBracketCount++;
				} else if (paramListString.charAt(i) == ']') {
					openBracketCount--;
				} else if (paramListString.charAt(i) == ',') {
					if (openBracketCount == 0) {
						result.add(paramListString.substring(parameterTypeNameStart, i));
						if ((i + 2) >= paramListString.length()) {
							throw new ReflectionUIError();
						}
						if (paramListString.charAt(i + 1) != ' ') {
							throw new ReflectionUIError();
						}
						parameterTypeNameStart = i + 2;
						i++;
					}
				}
			}
			if (openBracketCount > 0) {
				throw new ReflectionUIError();
			}
			result.add(paramListString.substring(parameterTypeNameStart));
		}
		return result.toArray(new String[result.size()]);
	}

	public static String identifierToCaption(String identifier) {
		if (identifier.length() == 0) {
			return "";
		} else if (identifier.toUpperCase().equals(identifier)) {
			String[] words = identifier.split("_");
			return Arrays.stream(words).map(String::toLowerCase).map(ReflectionUIUtils::identifierToCaption)
					.collect(Collectors.joining(" "));
		} else {
			StringBuilder result = new StringBuilder();
			int i = 0;
			char lastC = 0;
			for (char c : identifier.toCharArray()) {
				if (i == 0) {
					result.append(Character.toUpperCase(c));
				} else if (Character.isUpperCase(c) && !Character.isUpperCase(lastC)) {
					result.append(" " + c);
				} else if (Character.isDigit(c) && !Character.isDigit(lastC)) {
					result.append(" " + c);
				} else if (!Character.isLetterOrDigit(c) && Character.isLetterOrDigit(lastC)) {
					result.append(" " + c);
				} else {
					result.append(c);
				}
				lastC = c;
				i++;
			}
			return result.toString();
		}
	}

	public static IMethodInfo getNParametersMethod(List<IMethodInfo> methods, int n) {
		for (IMethodInfo c : methods) {
			if (c.getParameters().size() == n) {
				return c;
			}
		}
		return null;
	}

	public static IMethodInfo getZeroParameterMethod(List<IMethodInfo> methods) {
		return getNParametersMethod(methods, 0);
	}

	public static <T extends IMethodInfo> T findMethodBySignature(List<T> methods, String signature) {
		for (T method : methods) {
			String candidateMethodSignature = method.getSignature();
			if (candidateMethodSignature.equals(signature)) {
				return method;
			}
		}
		return null;
	}

	public static <T extends IInfo> T findInfoByName(List<T> infos, String name) {
		for (T info : infos) {
			if (info.getName().equals(name)) {
				return info;
			}
		}
		return null;
	}

	public static <T extends IInfo> T findInfoByCaption(List<T> infos, String caption) {
		for (T info : infos) {
			if (info.getCaption().equals(caption)) {
				return info;
			}
		}
		return null;
	}

	public static String formatRequiredParameterList(List<IParameterInfo> parameters) {
		StringBuilder result = new StringBuilder();
		int iRequiredParam = 0;
		for (IParameterInfo param : parameters) {
			if (param.isHidden()) {
				continue;
			}
			if (iRequiredParam > 0) {
				if (iRequiredParam == parameters.size() - 1) {
					result.append(" AND ");
				} else {
					result.append(", ");
				}
			}
			String paramCaption = param.getCaption();
			if (paramCaption.length() > 0) {
				result.append(paramCaption);
			} else {
				result.append(param.getType().getCaption());
			}
			iRequiredParam++;
		}
		return result.toString();
	}

	public static <M extends Member> M findJavaMemberByName(M[] members, String memberName) {
		for (M member : members) {
			if (member.getName().equals(memberName)) {
				return member;
			}
		}
		return null;
	}

	public static void sortFields(Field[] fields) {
		Arrays.sort(fields, new Comparator<Field>() {
			@Override
			public int compare(Field f1, Field f2) {
				int result;

				result = f1.getType().getName().toUpperCase().compareTo(f2.getType().getName().toUpperCase());
				if (result != 0) {
					return result;
				}

				result = f1.getName().compareTo(f2.getName());
				if (result != 0) {
					return result;
				}

				return 0;
			}
		});
	}

	public static void sortMethods(Method[] methods) {
		Arrays.sort(methods, new Comparator<Method>() {
			@Override
			public int compare(Method m1, Method m2) {
				int result;

				List<String> parameterTypeNames1 = new ArrayList<String>();
				for (Parameter param : m1.getParameters()) {
					parameterTypeNames1.add(param.getType().getName());
				}
				Collections.sort(parameterTypeNames1);
				List<String> parameterTypeNames2 = new ArrayList<String>();
				for (Parameter param : m2.getParameters()) {
					parameterTypeNames2.add(param.getType().getName());
				}
				Collections.sort(parameterTypeNames2);
				result = MiscUtils.stringJoin(parameterTypeNames1, "\n")
						.compareTo(MiscUtils.stringJoin(parameterTypeNames2, "\n"));
				if (result != 0) {
					return result;
				}

				result = m1.getReturnType().getName().compareTo(m2.getReturnType().getName());
				if (result != 0) {
					return result;
				}

				result = m1.getName().compareTo(m2.getName());
				if (result != 0) {
					return result;
				}

				return 0;
			}
		});
	}

	public static String composeMessage(String contextMessage, String localMessage) {
		if ((contextMessage == null) || (contextMessage.length() == 0)) {
			return localMessage;
		}
		return contextMessage + " - " + localMessage;
	}

	public static Object createDefaultInstance(ITypeInfo type) {
		return createDefaultInstance(type, true);
	}

	public static Object createDefaultInstance(ITypeInfo type, boolean subTypeInstanceAllowed) {
		try {
			if (!type.isConcrete()) {
				if (subTypeInstanceAllowed) {
					if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(type)) {
						List<ITypeInfo> subTypes = type.getPolymorphicInstanceSubTypes();
						for (ITypeInfo subType : subTypes) {
							try {
								return createDefaultInstance(subType, true);
							} catch (Throwable ignore) {
							}
						}
					}
				}
				throw new ReflectionUIError(
						"Cannot instantiate abstract (or " + Object.class.getSimpleName() + ") type");

			}

			IMethodInfo zeroParamConstructor = getZeroParameterMethod(type.getConstructors());
			if (zeroParamConstructor != null) {
				return zeroParamConstructor.invoke(null, new InvocationData(null, zeroParamConstructor));
			}
			for (IMethodInfo constructor : type.getConstructors()) {
				InvocationData invocationData = new InvocationData(null, constructor);
				if (!ReflectionUIUtils.requiresParameterValue(constructor)) {
					return constructor.invoke(null, invocationData);
				}
			}
			throw new ReflectionUIError("Default constructor not found");
		} catch (Throwable t) {
			throw new ReflectionUIError(
					"Failed to create a default instance of type '" + type.getName() + "': " + t.toString(), t);

		}
	}

	public static boolean canCreateDefaultInstance(ITypeInfo type, boolean subTypeInstanceAllowed) {
		if (!type.isConcrete()) {
			if (subTypeInstanceAllowed) {
				if (ReflectionUIUtils.hasPolymorphicInstanceSubTypes(type)) {
					List<ITypeInfo> subTypes = type.getPolymorphicInstanceSubTypes();
					for (ITypeInfo subType : subTypes) {
						return canCreateDefaultInstance(subType, true);
					}
				}
			}
			return false;
		}

		IMethodInfo zeroParamConstructor = getZeroParameterMethod(type.getConstructors());
		if (zeroParamConstructor != null) {
			return true;
		}
		for (IMethodInfo constructor : type.getConstructors()) {
			if (!ReflectionUIUtils.requiresParameterValue(constructor)) {
				return true;
			}
		}
		return false;
	}

	public static <T extends IInfo> Comparator<T> getInfosComparator(final List<String> expectedOrderSpecification,
			List<T> initialOrder) {
		final List<T> initialOrderCopy = new ArrayList<T>(initialOrder);
		return new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				if (expectedOrderSpecification.contains(o1.getName())
						&& expectedOrderSpecification.contains(o2.getName())) {
					Integer index1 = new Integer(expectedOrderSpecification.indexOf(o1.getName()));
					Integer index2 = new Integer(expectedOrderSpecification.indexOf(o2.getName()));
					return index1.compareTo(index2);
				}
				if (expectedOrderSpecification.contains(o1.getName())) {
					return 1;
				}
				if (expectedOrderSpecification.contains(o2.getName())) {
					return -1;
				}
				Integer index1 = new Integer(initialOrderCopy.indexOf(o1));
				Integer index2 = new Integer(initialOrderCopy.indexOf(o2));
				return index1.compareTo(index2);
			}
		};
	}

	public static boolean hasPolymorphicInstanceSubTypes(ITypeInfo type) {
		if (PolymorphicTypeOptionsFactory.isPolymorphismRecursivityDetected(type)) {
			return false;
		}
		List<ITypeInfo> polyTypes = type.getPolymorphicInstanceSubTypes();
		return polyTypes.size() > 0;
	}

	public static String toString(ReflectionUI reflectionUI, Object object) {
		if (object == null) {
			return "";
		}
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		return type.toString(object);
	}

	public static ResourcePath getIconImagePath(ReflectionUI reflectionUI, Object object) {
		if (object == null) {
			return null;
		}
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		return type.getIconImagePath(object);
	}

	public static boolean canCopy(ReflectionUI reflectionUI, Object object) {
		if (object == null) {
			return false;
		}
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		return type.canCopy(object);
	}

	public static Object copy(ReflectionUI reflectionUI, Object object) {
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		return type.copy(object);
	}

	public static void copyFieldValuesAccordingInfos(ReflectionUI reflectionUI, Object src, Object dst,
			boolean deeply) {
		copyFieldValuesAccordingInfos(reflectionUI, src, dst, deeply, new ArrayList<Pair<Object, Object>>());
	}

	public static void copyFieldValuesAccordingInfos(ReflectionUI reflectionUI, Object src, Object dst, boolean deeply,
			List<Pair<Object, Object>> alreadyCopied) {
		alreadyCopied.add(new Pair<Object, Object>(src, dst));
		ITypeInfo srcType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(src));
		ITypeInfo dstType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(dst));
		for (IFieldInfo dstField : dstType.getFields()) {
			if (dstField.isGetOnly()) {
				continue;
			}
			IFieldInfo srcField = ReflectionUIUtils.findInfoByName(srcType.getFields(), dstField.getName());
			if (srcField == null) {
				continue;
			}
			Object srcFieldValue = srcField.getValue(src);
			if (srcFieldValue == null) {
				dstField.setValue(dst, null);
			} else {
				ITypeInfo fieldValueType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(srcFieldValue));
				if (deeply && !fieldValueType.isImmutable()) {
					Object dstFieldValue = null;
					for (Pair<Object, Object> pair : alreadyCopied) {
						if (pair.getFirst() == srcFieldValue) {
							dstFieldValue = pair.getSecond();
							break;
						}
					}
					if (dstFieldValue == null) {
						if (canCopy(reflectionUI, srcFieldValue)) {
							dstFieldValue = copy(reflectionUI, srcFieldValue);
						} else {
							dstFieldValue = ReflectionUIUtils.createDefaultInstance(fieldValueType, false);
							copyFieldValuesAccordingInfos(reflectionUI, srcFieldValue, dstFieldValue, true,
									alreadyCopied);
						}
					}
					dstField.setValue(dst, dstFieldValue);
				} else {
					dstField.setValue(dst, srcFieldValue);
				}
			}
		}
	}

	public static void checkInstance(ITypeInfo type, Object object) {
		if (!type.supports(object)) {
			throw new ReflectionUIError();
		}
	}

	public static void finalizeModifications(final ModificationStack parentModificationStack,
			final ModificationStack currentModificationsStack, boolean currentModificationsAccepted,
			final ValueReturnMode valueReturnMode, final boolean valueReplaced, boolean valueTransactionExecuted,
			final IModification committingModification, final IModification undoModificationsReplacement,
			String parentModificationTitle, boolean volatileParentModification, Runnable parentControlRefreshJob,
			final Listener<String> debugLogListener, Listener<String> errorLogListener) {

		if (currentModificationsStack == null) {
			throw new ReflectionUIError();
		}

		if (!mayModificationsHaveImpact(false, valueReturnMode, (committingModification != null))) {
			if (!currentModificationsStack.isInitial()) {
				if (parentControlRefreshJob != null) {
					parentControlRefreshJob.run();
				}
			}
			return;
		}

		if (currentModificationsStack.isInitial()) {
			if (!currentModificationsStack.isExhaustive()) {
				if (parentModificationStack != null) {
					parentModificationStack.push(IModification.VOLATILE_MODIFICATION);
				}
				return;
			}
			return;
		}

		if (currentModificationsAccepted) {
			if (parentModificationStack != null) {
				parentModificationStack.insideComposite(parentModificationTitle, UndoOrder.FIFO,
						new Accessor<Boolean>() {
							@Override
							public Boolean get() {
								if (undoModificationsReplacement == null) {
									if (valueReturnMode != ValueReturnMode.CALCULATED) {
										/*
										 * If the modifications were applied on calculated (then temporary) data, it
										 * would be useless to revert or replay them, and any invalidation would have no
										 * impact.
										 */
										if (currentModificationsStack.wasInvalidated()) {
											if (debugLogListener != null) {
												debugLogListener.handle(
														"Sub-modification stack invalidated => Invalidating parent modification stack: "
																+ parentModificationStack);
											}
											parentModificationStack.invalidate();
										} else {
											parentModificationStack.push(ModificationStack.createCompositeModification(
													null, UndoOrder.getNormal(),
													currentModificationsStack.getUndoModifications()));
										}
									}
								}
								if (!ValueReturnMode.isDirectOrProxy(valueReturnMode) || valueReplaced) {
									/*
									 * If the modifications were applied directly or through a proxy, it would be
									 * useless to commit them since we are sure that the actual data (not a copy or
									 * something else) was altered. However, it is necessary to commit the
									 * modifications, even if the editor has direct access to the value, when the
									 * resulting value replaces the old value (with a new identity).
									 */
									if (committingModification != null) {
										if (debugLogListener != null) {
											debugLogListener.handle("Executing " + committingModification);
										}
										parentModificationStack
												.apply((undoModificationsReplacement == null) ? committingModification
														: new AbstractModificationProxy(committingModification) {

															@Override
															public IModification applyAndGetOpposite(
																	ModificationStack modificationStack)
																	throws IrreversibleModificationException,
																	CancelledModificationException {
																/*
																 * to prevent the undo modification from being pushed on
																 * the stack.
																 */
																return null;
															}
														});
									}
								}
								if (undoModificationsReplacement != null) {
									parentModificationStack.push(undoModificationsReplacement);
								}
								return true;
							}
						}, volatileParentModification);
			}
		} else {
			if (valueReturnMode != ValueReturnMode.CALCULATED) {
				if (valueTransactionExecuted) {
					/*
					 * The transaction has been rolled back then the value has recovered its initial
					 * state => no need to undo the value modifications anymore.
					 */
					if (parentModificationStack != null) {
						/*
						 * We then need to make sure that the parentModificationStack will fire an event
						 * allowing the parent object UI to refresh and then take into account the
						 * changes caused by the transaction.
						 */
						parentModificationStack.push(IModification.VOLATILE_MODIFICATION);
					}
				} else {
					if (debugLogListener != null) {
						debugLogListener.handle("Undoing sub-modification stack: " + currentModificationsStack);
					}
					currentModificationsStack.undoAll();
					if (!currentModificationsStack.wasInvalidated()) {
						if (parentControlRefreshJob != null) {
							parentControlRefreshJob.run();
						}
					} else {
						if (errorLogListener != null) {
							errorLogListener.handle(
									"Detected invalidated sub-modification stack: " + currentModificationsStack);
						}
						if (parentModificationStack != null) {
							if (debugLogListener != null) {
								debugLogListener
										.handle("Invalidating parent modification stack: " + parentModificationStack);
							}
							parentModificationStack.invalidate();
						}
					}
				}
			}
		}
	}

	public static boolean mayModificationsHaveImpact(boolean valueKnownAsImmutable, ValueReturnMode valueReturnMode,
			boolean canCommit) {
		if ((valueReturnMode != ValueReturnMode.CALCULATED) && !valueKnownAsImmutable) {
			return true;
		}
		if (canCommit) {
			return true;
		}
		return false;
	}

	public static boolean isValueImmutable(ReflectionUI reflectionUI, Object value) {
		if (value == null) {
			return true;
		}
		ITypeInfo valueType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(value));
		return valueType.isImmutable();
	}

	public static void setFieldValueThroughModificationStack(IFieldControlData data, Object newValue,
			ModificationStack modifStack, Listener<String> debugLogListener) {
		if (data.isTransient()) {
			try {
				data.setValue(newValue);
			} finally {
				if (debugLogListener != null) {
					debugLogListener.handle("Sending volatile modification to: " + modifStack);
				}
				modifStack.apply(IModification.VOLATILE_MODIFICATION);
			}
		} else {
			FieldControlDataModification modif = new FieldControlDataModification(data, newValue);
			try {
				if (debugLogListener != null) {
					debugLogListener.handle("Executing " + modif);
				}
				modifStack.apply(modif);
			} catch (Throwable t) {
				if (debugLogListener != null) {
					debugLogListener.handle("Invalidating modification stack: " + modifStack);
				}
				try {
					modifStack.invalidate();
				} catch (Throwable ignore) {
				}
				throw new ReflectionUIError(t);
			}
		}
	}

	public static Object invokeMethodThroughModificationStack(IMethodControlData data, InvocationData invocationData,
			ModificationStack modifStack, Listener<String> debugLogListener) {
		if (data.isReadOnly()) {
			try {
				return data.invoke(invocationData);
			} finally {
				if (debugLogListener != null) {
					debugLogListener.handle("Sending volatile modification to: " + modifStack);
				}
				modifStack.apply(IModification.VOLATILE_MODIFICATION);
			}
		} else {
			final Object[] resultHolder = new Object[1];
			data = new MethodControlDataProxy(data) {
				@Override
				public Object invoke(InvocationData invocationData) {
					return resultHolder[0] = super.invoke(invocationData);
				}
			};
			MethodControlDataModification modif = new MethodControlDataModification(data, invocationData);
			try {
				if (debugLogListener != null) {
					debugLogListener.handle("Executing " + modif);
				}
				modifStack.apply(modif);
			} catch (Throwable t) {
				if (debugLogListener != null) {
					debugLogListener.handle("Invalidating modification stack: " + modifStack);
				}
				modifStack.invalidate();
				throw new ReflectionUIError(t);
			}
			return resultHolder[0];
		}
	}

	public static String getDefaultListTypeCaption(IListTypeInfo listType) {
		ITypeInfo itemType = listType.getItemType();
		if (itemType == null) {
			return "List";
		} else {
			return "List Of " + itemType.getCaption() + " Elements";
		}
	}

	public static Listener<String> getDebugLogListener(final ReflectionUI reflectionUI) {
		return new Listener<String>() {
			@Override
			public void handle(String event) {
				reflectionUI.logDebug(event);
			}
		};
	}

	public static Listener<String> getErrorLogListener(final ReflectionUI reflectionUI) {
		return new Listener<String>() {
			@Override
			public void handle(String event) {
				reflectionUI.logError(event);
			}
		};
	}

	public static String getContructorDescription(IMethodInfo ctor) {
		StringBuilder result = new StringBuilder(ctor.getCaption());
		if (!ReflectionUIUtils.requiresParameterValue(ctor)) {
			result.append(" - by default");
		} else {
			result.append(" - specify ");
			result.append(formatRequiredParameterList(ctor.getParameters()));
		}
		return result.toString();
	}

	public static MenuElementKind getMenuElementKind(IMenuElementInfo element) {
		if (element instanceof MenuInfo) {
			return MenuElementKind.MENU;
		} else if (element instanceof MenuItemCategory) {
			return MenuElementKind.ITEM_CATEGORY;
		} else if (element instanceof AbstractMenuItemInfo) {
			return MenuElementKind.ITEM;
		} else {
			throw new ReflectionUIError();
		}
	}

	public static boolean equalsAccordingInfos(Object o1, Object o2, ReflectionUI reflectionUI,
			IInfoFilter infoFilter) {
		return equalsAccordingInfos(o1, o2, reflectionUI, infoFilter, new ArrayList<Pair<Object, Object>>());
	}

	public static boolean equalsAccordingInfos(Object o1, Object o2, ReflectionUI reflectionUI, IInfoFilter infoFilter,
			List<Pair<Object, Object>> alreadyCompared) {
		if (o1 == o2) {
			return true;
		}
		if ((o1 == null) || (o2 == null)) {
			return false;
		}
		if (ClassUtils.isPrimitiveClassOrWrapperOrString(o1.getClass())) {
			return o1.equals(o2);
		}
		for (Pair<Object, Object> pair : alreadyCompared) {
			if ((pair.getFirst() == o1) && (pair.getSecond() == o2)) {
				return true;
			}
		}
		alreadyCompared.add(new Pair<Object, Object>(o1, o2));
		ITypeInfo type1 = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(o1));
		ITypeInfo type2 = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(o2));
		if (!type1.equals(type2)) {
			return false;
		}
		if (type1.isPrimitive()) {
			return o1.equals(o2);
		}
		for (IFieldInfo field : type1.getFields()) {
			field = infoFilter.apply(field);
			if (field == null) {
				continue;
			}
			Object value1 = field.getValue(o1);
			Object value2 = field.getValue(o2);
			if (!equalsAccordingInfos(value1, value2, reflectionUI, infoFilter, alreadyCompared)) {
				return false;
			}
		}
		if (type1 instanceof IListTypeInfo) {
			IListTypeInfo listType = (IListTypeInfo) type1;
			Object[] rawList1 = listType.toArray(o1);
			Object[] rawList2 = listType.toArray(o2);
			if (rawList1.length != rawList2.length) {
				return false;
			}
			for (int i = 0; i < rawList1.length; i++) {
				Object item1 = rawList1[i];
				Object item2 = rawList2[i];
				if (!equalsAccordingInfos(item1, item2, reflectionUI, infoFilter, alreadyCompared)) {
					return false;
				}
			}
		}
		if (type1 instanceof IEnumerationTypeInfo) {
			IEnumerationTypeInfo enumType = (IEnumerationTypeInfo) type1;
			IEnumerationItemInfo valueInfo1 = enumType.getValueInfo(o1);
			IEnumerationItemInfo valueInfo2 = enumType.getValueInfo(o2);
			if (!valueInfo1.getName().equals(valueInfo2.getName())) {
				return false;
			}
		}
		return true;
	}

	public static String formatMethodControlCaption(String methodCaption, List<IParameterInfo> methodParameters) {
		if (methodCaption.length() > 0) {
			if (requiresParameterValue(methodParameters)) {
				methodCaption += "...";
			}
		}
		return methodCaption;
	}

	public static String formatMethodControlTooltipText(String methodCaption, String methodOnlineHelp,
			List<IParameterInfo> methodParameters) {
		if (methodOnlineHelp != null) {
			return methodOnlineHelp;
		} else {
			if (ReflectionUIUtils.requiresParameterValue(methodParameters)) {
				String toolTipText = formatMethodControlCaption(methodCaption, methodParameters);
				if (toolTipText.length() > 0) {
					toolTipText += "\n";
				}
				toolTipText += "Parameter(s): " + ReflectionUIUtils.formatRequiredParameterList(methodParameters);
				return toolTipText;
			} else {
				return null;
			}
		}
	}

	public static List<IMenuElementPosition> getAncestors(IMenuElementPosition elementPosition) {
		List<IMenuElementPosition> result = new ArrayList<IMenuElementPosition>();
		while (elementPosition.getParent() != null) {
			result.add(elementPosition.getParent());
			elementPosition = elementPosition.getParent();
		}
		return result;
	}

	public static Runnable getNextUpdateCustomOrDefaultUndoJob(final IFieldControlData data, Object newValue) {
		Runnable result = data.getNextUpdateCustomUndoJob(newValue);
		if (result == null) {
			result = createNextUpdateDefaultUndoJob(data);
		}
		return result;
	}

	public static Runnable getNextUpdateCustomOrDefaultUndoJob(Object object, IFieldInfo field, Object newValue) {
		Runnable result = field.getNextUpdateCustomUndoJob(object, newValue);
		if (result == null) {
			result = createNextUpdateDefaultUndoJob(object, field);
		}
		return result;
	}

	public static Runnable createNextUpdateDefaultUndoJob(final IFieldControlData data) {
		final Object oldValue = data.getValue();
		return new Runnable() {
			@Override
			public void run() {
				data.setValue(oldValue);
			}
		};
	}

	public static Runnable createNextUpdateDefaultUndoJob(final Object object, final IFieldInfo field) {
		final Object oldValue = field.getValue(object);
		return new Runnable() {
			@Override
			public void run() {
				field.setValue(object, oldValue);
			}
		};
	}

	public static Runnable getPreviousUpdateCustomOrDefaultRedoJob(final IFieldControlData data, Object newValue) {
		Runnable result = data.getPreviousUpdateCustomRedoJob(newValue);
		if (result == null) {
			result = createUpdateJob(data, newValue);
		}
		return result;
	}

	public static Runnable getPreviousUpdateCustomOrDefaultRedoJob(Object object, IFieldInfo field, Object newValue) {
		Runnable result = field.getPreviousUpdateCustomRedoJob(object, newValue);
		if (result == null) {
			result = createUpdateJob(object, field, newValue);
		}
		return result;
	}

	public static Runnable createUpdateJob(final IFieldControlData data, final Object newValue) {
		return new Runnable() {
			@Override
			public void run() {
				data.setValue(newValue);
			}
		};
	}

	public static Runnable createUpdateJob(final Object object, final IFieldInfo field, final Object newValue) {
		return new Runnable() {
			@Override
			public void run() {
				field.setValue(object, newValue);
			}
		};
	}

	public static Runnable getPreviousInvocationCustomOrDefaultRedoJob(final IMethodControlData data,
			InvocationData invocationData) {
		Runnable result = data.getPreviousInvocationCustomRedoJob(invocationData);
		if (result == null) {
			result = createInvocationJob(data, invocationData);
		}
		return result;
	}

	public static Runnable getPreviousInvocationCustomOrDefaultRedoJob(final Object object, final IMethodInfo method,
			InvocationData invocationData) {
		Runnable result = method.getPreviousInvocationCustomRedoJob(object, invocationData);
		if (result == null) {
			result = createInvocationJob(object, method, invocationData);
		}
		return result;
	}

	public static Runnable createInvocationJob(final IMethodControlData data, final InvocationData invocationData) {
		return new Runnable() {
			@Override
			public void run() {
				data.invoke(invocationData);
			}
		};
	}

	public static Runnable createInvocationJob(final Object object, final IMethodInfo method,
			final InvocationData invocationData) {
		return new Runnable() {
			@Override
			public void run() {
				method.invoke(object, invocationData);
			}
		};
	}

	public static Runnable createRollbackJob(ITransaction transaction) {
		if (transaction == null) {
			throw new AssertionError();
		}
		return new Runnable() {
			@Override
			public void run() {
				transaction.rollback();
			}
		};
	}

	public static boolean requiresParameterValue(IMethodInfo method) {
		return requiresParameterValue(method.getParameters());
	}

	public static boolean requiresParameterValue(List<IParameterInfo> parameters) {
		for (IParameterInfo param : parameters) {
			if (!param.isHidden()) {
				return true;
			}
		}
		return false;
	}

	public static void setFieldControlPluginManagementDisabled(Map<String, Object> specificProperties, boolean b) {
		if (b) {
			specificProperties.put(IFieldControlPlugin.MANAGEMENT_DISABLED_PROPERTY_KEY, Boolean.TRUE);
		} else {
			specificProperties.remove(IFieldControlPlugin.MANAGEMENT_DISABLED_PROPERTY_KEY);
		}
	}

	public static boolean isFieldControlPluginManagementDisabled(Map<String, Object> specificProperties) {
		return Boolean.TRUE.equals(specificProperties.get(IFieldControlPlugin.MANAGEMENT_DISABLED_PROPERTY_KEY));
	}

	public static void setFieldControlPluginIdentifier(Map<String, Object> specificProperties, String identifier) {
		if (identifier == null) {
			specificProperties.remove(IFieldControlPlugin.CHOSEN_PROPERTY_KEY);
		} else {
			specificProperties.put(IFieldControlPlugin.CHOSEN_PROPERTY_KEY, identifier);
		}
	}

	public static String getFieldControlPluginIdentifier(Map<String, Object> specificProperties) {
		return (String) specificProperties.get(IFieldControlPlugin.CHOSEN_PROPERTY_KEY);
	}

	public static void setFieldControlPluginConfiguration(Map<String, Object> specificProperties, String identifier,
			Serializable controlConfiguration) {
		if (controlConfiguration == null) {
			specificProperties.remove(identifier);
		} else {
			specificProperties.put(identifier, IOUtils.serializeToHexaText(controlConfiguration));
		}
	}

	public static Serializable getFieldControlPluginConfiguration(Map<String, Object> specificProperties,
			String identifier) {
		String text = (String) specificProperties.get(identifier);
		if (text == null) {
			return null;
		}
		return (Serializable) IOUtils.deserializeFromHexaText(text);
	}

	public static InfoCategory getCategory(IInfo info) {
		if (info instanceof IFieldInfo) {
			return ((IFieldInfo) info).getCategory();
		} else if (info instanceof IMethodInfo) {
			return ((IMethodInfo) info).getCategory();
		} else {
			return null;
		}
	}

	public static boolean isTypeEmpty(ITypeInfo type, IInfoFilter infoFilter) {
		List<IFieldInfo> fields = type.getFields();
		List<IMethodInfo> methods = type.getMethods();

		fields = new ArrayList<IFieldInfo>(fields);
		for (Iterator<IFieldInfo> it = fields.iterator(); it.hasNext();) {
			IFieldInfo field = it.next();
			field = infoFilter.apply(field);
			if ((field == null) || field.isHidden()) {
				it.remove();
			}
		}

		methods = new ArrayList<IMethodInfo>(methods);
		for (Iterator<IMethodInfo> it = methods.iterator(); it.hasNext();) {
			IMethodInfo method = it.next();
			method = infoFilter.apply(method);
			if ((method == null) || method.isHidden()) {
				it.remove();
			}
		}

		return (fields.size() + methods.size()) == 0;
	}

	public static boolean isConstructor(IMethodInfo method) {
		return method.getName().length() == 0;
	}

	public static String buildNameFromMethodSignature(String baseMethodSignature) {
		String baseMethodReturnTypeName = ReflectionUIUtils
				.extractMethodReturnTypeNameFromSignature(baseMethodSignature);
		String baseMethodName = ReflectionUIUtils.extractMethodNameFromSignature(baseMethodSignature);
		String[] baseMethodParameterTypeNames = ReflectionUIUtils
				.extractMethodParameterTypeNamesFromSignature(baseMethodSignature);
		String result = baseMethodReturnTypeName + "-"
				+ ((baseMethodName.length() == 0) ? "<constructor>" : baseMethodName)
				+ ((baseMethodParameterTypeNames.length == 0) ? ""
						: ("-" + MiscUtils.stringJoin(Arrays.asList(baseMethodParameterTypeNames), "-")));
		result = result.replace(" ", "_");
		return result;
	}

	public static DefaultMenuElementPosition getMenuElementPosition(MenuModelCustomization menuModelCustomization,
			IMenuItemContainerCustomization menuItemContainerCustomization) {
		for (MenuCustomization menuCustomization : menuModelCustomization.getMenuCustomizations()) {
			DefaultMenuElementPosition result = getMenuElementPosition(menuCustomization,
					menuItemContainerCustomization);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public static DefaultMenuElementPosition getMenuElementPosition(IMenuItemContainerCustomization fromContainer,
			IMenuItemContainerCustomization elementContainer) {
		String elementName = fromContainer.getName();
		MenuElementKind elementKind = getMenuElementKind(fromContainer);
		DefaultMenuElementPosition rootPosition = new DefaultMenuElementPosition(elementName, elementKind, null);
		if (fromContainer == elementContainer) {
			return rootPosition;
		}
		for (AbstractMenuItemCustomization menuItemCustomization : fromContainer.getItemCustomizations()) {
			if (menuItemCustomization instanceof IMenuItemContainerCustomization) {
				DefaultMenuElementPosition result = getMenuElementPosition(
						(IMenuItemContainerCustomization) menuItemCustomization, elementContainer);
				if (result != null) {
					((DefaultMenuElementPosition) result).getRoot().setParent(rootPosition);
					return result;
				}
			}
		}
		if (fromContainer instanceof MenuCustomization) {
			for (MenuItemCategoryCustomization menuItemCategoryCustomization : ((MenuCustomization) fromContainer)
					.getItemCategoryCustomizations()) {
				DefaultMenuElementPosition result = getMenuElementPosition(menuItemCategoryCustomization,
						elementContainer);
				if (result != null) {
					((DefaultMenuElementPosition) result).getRoot().setParent(rootPosition);
					return result;
				}
			}
		}
		return null;
	}

	public static DefaultMenuElementPosition getMenuElementPosition(InfoCustomizations infoCustomizations,
			IMenuItemContainerCustomization menuItemContainerCustomization) {
		for (TypeCustomization tc : infoCustomizations.getTypeCustomizations()) {
			DefaultMenuElementPosition result = getMenuElementPosition(tc.getMenuModelCustomization(),
					menuItemContainerCustomization);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public static MenuElementKind getMenuElementKind(IMenuElementCustomization elementCustomization) {
		return ReflectionUIUtils.getMenuElementKind(createMenuElementInfo(elementCustomization));
	}

	public static MenuModel createMenuModel(MenuModelCustomization menuModelCustomization) {
		MenuModel result = new MenuModel();
		for (MenuCustomization menuCustomization : menuModelCustomization.getMenuCustomizations()) {
			result.getMenus().add((MenuInfo) createMenuElementInfo(menuCustomization));
		}
		return result;
	}

	public static IMenuElementInfo createMenuElementInfo(IMenuElementCustomization menuElementCustomization) {
		if (menuElementCustomization instanceof MenuCustomization) {
			MenuInfo result = new MenuInfo();
			result.setCaption(menuElementCustomization.getName());
			for (MenuItemCategoryCustomization menuItemCategoryCustomization : ((MenuCustomization) menuElementCustomization)
					.getItemCategoryCustomizations()) {
				result.getItemCategories().add((MenuItemCategory) createMenuElementInfo(menuItemCategoryCustomization));
			}
			for (AbstractMenuItemCustomization menuItemCustomization : ((MenuCustomization) menuElementCustomization)
					.getItemCustomizations()) {
				result.getItems().add((AbstractMenuItemInfo) createMenuElementInfo(menuItemCustomization));
			}
			return result;
		} else if (menuElementCustomization instanceof MenuItemCategoryCustomization) {
			MenuItemCategory result = new MenuItemCategory();
			result.setCaption(menuElementCustomization.getName());
			for (AbstractMenuItemCustomization menuItemCustomization : ((MenuItemCategoryCustomization) menuElementCustomization)
					.getItemCustomizations()) {
				result.getItems().add((AbstractMenuItemInfo) createMenuElementInfo(menuItemCustomization));
			}
			return result;
		} else if (menuElementCustomization instanceof RenewMenuItemCustomization) {
			return new StandradActionMenuItemInfo(menuElementCustomization.getName(),
					((AbstractMenuItemCustomization) menuElementCustomization).getIconImagePath(),
					StandradActionMenuItemInfo.Type.NEW);
		} else if (menuElementCustomization instanceof OpenMenuItemCustomization) {
			return new StandradActionMenuItemInfo(menuElementCustomization.getName(),
					((AbstractMenuItemCustomization) menuElementCustomization).getIconImagePath(),
					StandradActionMenuItemInfo.Type.OPEN,
					((AbstractFileMenuItemCustomization) menuElementCustomization).getFileBrowserConfiguration());
		} else if (menuElementCustomization instanceof SaveAsMenuItemCustomization) {
			return new StandradActionMenuItemInfo(menuElementCustomization.getName(),
					((AbstractMenuItemCustomization) menuElementCustomization).getIconImagePath(),
					StandradActionMenuItemInfo.Type.SAVE_AS,
					((AbstractFileMenuItemCustomization) menuElementCustomization).getFileBrowserConfiguration());
		} else if (menuElementCustomization instanceof SaveMenuItemCustomization) {
			return new StandradActionMenuItemInfo(menuElementCustomization.getName(),
					((AbstractMenuItemCustomization) menuElementCustomization).getIconImagePath(),
					StandradActionMenuItemInfo.Type.SAVE,
					((AbstractFileMenuItemCustomization) menuElementCustomization).getFileBrowserConfiguration());
		} else if (menuElementCustomization instanceof ExitMenuItemCustomization) {
			return new StandradActionMenuItemInfo(menuElementCustomization.getName(),
					((AbstractMenuItemCustomization) menuElementCustomization).getIconImagePath(),
					StandradActionMenuItemInfo.Type.EXIT);
		} else if (menuElementCustomization instanceof HelpMenuItemCustomization) {
			return new StandradActionMenuItemInfo(menuElementCustomization.getName(),
					((AbstractMenuItemCustomization) menuElementCustomization).getIconImagePath(),
					StandradActionMenuItemInfo.Type.HELP);

		} else if (menuElementCustomization instanceof RedoMenuItemCustomization) {
			return new StandradActionMenuItemInfo(menuElementCustomization.getName(),
					((AbstractMenuItemCustomization) menuElementCustomization).getIconImagePath(),
					StandradActionMenuItemInfo.Type.REDO);
		} else if (menuElementCustomization instanceof UndoMenuItemCustomization) {
			return new StandradActionMenuItemInfo(menuElementCustomization.getName(),
					((AbstractMenuItemCustomization) menuElementCustomization).getIconImagePath(),
					StandradActionMenuItemInfo.Type.UNDO);
		} else if (menuElementCustomization instanceof ResetMenuItemCustomization) {
			return new StandradActionMenuItemInfo(menuElementCustomization.getName(),
					((AbstractMenuItemCustomization) menuElementCustomization).getIconImagePath(),
					StandradActionMenuItemInfo.Type.RESET);
		} else {
			throw new ReflectionUIError();
		}
	}

	public static String primitiveToString(Object object) {
		Class<?> javaType = object.getClass();
		if (!ClassUtils.isPrimitiveClassOrWrapper(javaType)) {
			throw new RuntimeException("Invalid primitive type: '" + javaType.getName() + "'");
		}
		return object.toString();
	}

	public static Object primitiveFromString(String text, Class<?> javaType) {
		if (javaType.isPrimitive()) {
			javaType = ClassUtils.primitiveToWrapperClass(javaType);
		}
		if (javaType == Character.class) {
			if (text.length() != 1) {
				throw new RuntimeException("Invalid value: '" + text + "'. 1 character is expected");
			}
			return text.charAt(0);
		} else if (javaType == Boolean.class) {
			if (Boolean.TRUE.toString().equals(text)) {
				return true;
			}
			if (Boolean.FALSE.toString().equals(text)) {
				return false;
			}
			throw new RuntimeException("Invalid value: '" + text + "'. Expected '" + Boolean.TRUE.toString() + "' or '"
					+ Boolean.FALSE.toString() + "'");
		} else {
			try {
				try {
					return javaType.getConstructor(new Class[] { String.class }).newInstance(text);
				} catch (IllegalArgumentException e) {
					throw new ReflectionUIError(e);
				} catch (SecurityException e) {
					throw new ReflectionUIError(e);
				} catch (InstantiationException e) {
					throw new ReflectionUIError(e);
				} catch (IllegalAccessException e) {
					throw new ReflectionUIError(e);
				} catch (InvocationTargetException e) {
					throw new ReflectionUIError(e.getTargetException());
				} catch (NoSuchMethodException e) {
					throw new ReflectionUIError(e);
				}
			} catch (Throwable t) {
				throw new ReflectionUIError(javaType.getSimpleName() + " Inupt Error: " + t.toString(), t);
			}
		}
	}

	public static NumberFormatter getNumberFormatter(Class<?> javaType, NumberFormat numberFormat) {
		NumberFormatter result = new NumberFormatter(new StrictNumberFormat(numberFormat));
		result.setValueClass(javaType);
		return result;
	}

	public static NumberFormat getNativeNumberFormat(final Class<?> javaType) {
		return new NumberFormat() {

			private static final long serialVersionUID = 1L;

			@Override
			public Number parse(String source, ParsePosition parsePosition) {
				Number result = (Number) primitiveFromString(source, javaType);
				parsePosition.setIndex(source.length());
				return result;
			}

			@Override
			public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
				return toAppendTo.append(primitiveToString(number));
			}

			@Override
			public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
				return toAppendTo.append(primitiveToString(number));
			}
		};
	}

	public static String secureNameContent(String s) {
		return s.replaceAll("[^a-zA-Z0-9 ]", "_");
	}

	public static List<Object> collectItemAncestors(ItemPosition itemPosition) {
		List<Object> result = new ArrayList<Object>();
		for (ItemPosition ancestorPosition : itemPosition.getAncestors()) {
			result.add(ancestorPosition.getItem());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T extends ItemPosition> List<T> actualizeItemPositions(List<T> oldItemPositions, List<Object> items,
			List<List<Object>> itemAncestorLists) {
		if (oldItemPositions.size() != items.size()) {
			throw new ReflectionUIError();
		}
		if (items.size() != itemAncestorLists.size()) {
			throw new ReflectionUIError();
		}
		List<T> result = new ArrayList<T>();
		for (int i = 0; i < oldItemPositions.size(); i++) {
			T itemPosition = oldItemPositions.get(i);
			if (itemPosition.isStable()) {
				if (itemPosition.isValid()) {
					result.add(itemPosition);
				}
			} else {
				Object[] containingListRawValue;
				if (itemPosition.isRoot()) {
					containingListRawValue = itemPosition.retrieveContainingListRawValue();
				} else {
					List<T> list = actualizeItemPositions(
							Collections.singletonList((T) itemPosition.getParentItemPosition()),
							Collections.singletonList(itemAncestorLists.get(i).get(0)), Collections.singletonList(
									itemAncestorLists.get(i).subList(1, itemAncestorLists.get(i).size())));
					if (list.isEmpty()) {
						containingListRawValue = null;
					} else {
						T parentItemPosition = list.get(0);
						itemPosition = (T) parentItemPosition.getSubItemPosition(itemPosition.getIndex());
						containingListRawValue = parentItemPosition.retrieveSubListRawValue();
					}
				}
				if (containingListRawValue != null) {
					if (itemPosition.getContainingListType().areItemsAutomaticallyPositioned()) {
						int index;
						if (itemPosition.getItemReturnMode() == ValueReturnMode.DIRECT) {
							index = -1;
							for (int containingListItemIndex = 0; containingListItemIndex < containingListRawValue.length; containingListItemIndex++) {
								if (containingListRawValue[containingListItemIndex] == items.get(i)) {
									index = containingListItemIndex;
									break;
								}
							}
						} else {
							index = Arrays.asList(containingListRawValue).indexOf(items.get(i));
						}
						if (index != -1) {
							itemPosition = (T) itemPosition.getSibling(index);
							result.add(itemPosition);
						}
					} else {
						if (itemPosition.isValid()) {
							result.add(itemPosition);
						}
					}
				}
			}
		}
		return result;
	}

	public static List<ITypeInfo> listDescendantTypes(ITypeInfo type) {
		List<ITypeInfo> result = new ArrayList<ITypeInfo>();
		List<ITypeInfo> subTypes = type.getPolymorphicInstanceSubTypes();
		for (ITypeInfo subType : subTypes) {
			result.add(subType);
			result.addAll(listDescendantTypes(subType));
		}
		return result;
	}

	public static boolean isTransitivelySlave(ModificationStack slaveModificationStack,
			ModificationStack masterModificationStack) {
		if (!(slaveModificationStack instanceof SlaveModificationStack)) {
			return false;
		}
		ModificationStack currentMaster = ((SlaveModificationStack) slaveModificationStack)
				.getMasterModificationStackGetter().get();
		if (currentMaster != masterModificationStack) {
			return isTransitivelySlave(currentMaster, masterModificationStack);
		}
		return true;
	}

	public static IModification createUndoModificationsReplacement(final IFieldControlData data) {
		return createUndoModificationsReplacement(new Accessor<Runnable>() {
			@Override
			public Runnable get() {
				return data.getLastFormRefreshStateRestorationJob();
			}
		});
	}

	public static IModification createUndoModificationsReplacement(final IMethodControlData data) {
		return createUndoModificationsReplacement(new Accessor<Runnable>() {
			@Override
			public Runnable get() {
				return data.getLastFormRefreshStateRestorationJob();
			}
		});
	}

	public static IModification createUndoModificationsReplacement(final Accessor<Runnable> stateRestorationJobGetter) {
		final Runnable stateRestorationJob = stateRestorationJobGetter.get();
		if (stateRestorationJob == null) {
			return null;
		}
		return new AbstractModification() {

			FutureActionBuilder undoJobBuilder;

			@Override
			public String getTitle() {
				return "Restore Previous State";
			}

			@Override
			protected Runnable createDoJob() {
				return new Runnable() {
					@Override
					public void run() {
						undoJobBuilder.setOption("runnable", stateRestorationJobGetter.get());
						undoJobBuilder.build();
						stateRestorationJob.run();
					}
				};
			}

			@Override
			protected Runnable createUndoJob() {
				return (undoJobBuilder = new FutureActionBuilder()).will(new FutureActionBuilder.FuturePerformance() {
					@Override
					public void perform(Map<String, Object> options) {
						((Runnable) options.get("runnable")).run();
					}
				});
			}

			@Override
			protected Runnable createRedoJob() {
				return stateRestorationJob;
			}

		};
	}

	public static Exception unwrapValidationError(Exception e) {
		if (e instanceof ReflectionUIError) {
			if (e.getCause() instanceof Exception) {
				if (((ReflectionUIError) e).getBaseMessage() != null) {
					if (((ReflectionUIError) e).getBaseMessage().contains(e.getCause().toString())) {
						return unwrapValidationError((Exception) e.getCause());
					}
				}
			}
		}
		return e;
	}

}
