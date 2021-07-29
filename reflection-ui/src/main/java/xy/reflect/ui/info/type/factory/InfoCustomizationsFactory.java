
package xy.reflect.ui.info.type.factory;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.ITransactionInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.custom.InfoCustomizations.ApplicationCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.CustomizationCategory;
import xy.reflect.ui.info.custom.InfoCustomizations.EnumerationCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.EnumerationItemCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.FormSizeCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.FormSizeUnit;
import xy.reflect.ui.info.custom.InfoCustomizations.ITypeInfoFinder;
import xy.reflect.ui.info.custom.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ListItemFieldShortcut;
import xy.reflect.ui.info.custom.InfoCustomizations.ListItemMethodShortcut;
import xy.reflect.ui.info.custom.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ParameterCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.TextualStorage;
import xy.reflect.ui.info.custom.InfoCustomizations.TransactionalRole;
import xy.reflect.ui.info.custom.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.VirtualFieldDeclaration;
import xy.reflect.ui.info.field.CapsuleFieldInfo;
import xy.reflect.ui.info.field.ChangedTypeFieldInfo;
import xy.reflect.ui.info.field.DelegatingFieldInfo;
import xy.reflect.ui.info.field.ExportedNullStatusFieldInfo;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.field.HiddenFieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.ImportedNullStatusFieldInfo;
import xy.reflect.ui.info.field.MethodReturnValueFieldInfo;
import xy.reflect.ui.info.field.NullReplacementFieldInfo;
import xy.reflect.ui.info.field.ParameterAsFieldInfo;
import xy.reflect.ui.info.field.SubFieldInfo;
import xy.reflect.ui.info.field.ValueAsListFieldInfo;
import xy.reflect.ui.info.field.VirtualFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.IMenuElementInfo;
import xy.reflect.ui.info.menu.IMenuElementPosition;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.menu.MethodActionMenuItemInfo;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.FieldAsGetterInfo;
import xy.reflect.ui.info.method.FieldAsSetterInfo;
import xy.reflect.ui.info.method.HiddenMethodInfoProxy;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.LoadFromFileMethod;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.method.ParameterizedFieldsMethodInfo;
import xy.reflect.ui.info.method.PresetInvocationDataMethodInfo;
import xy.reflect.ui.info.method.SaveToFileMethod;
import xy.reflect.ui.info.method.SubMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.parameter.ParameterInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.CategoriesStyle;
import xy.reflect.ui.info.type.ITypeInfo.FieldsLayout;
import xy.reflect.ui.info.type.ITypeInfo.MethodsLayout;
import xy.reflect.ui.info.type.enumeration.EnumerationItemInfoProxy;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo.InitialItemValueCreationOption;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.CustomizedListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.AbstractListProperty;
import xy.reflect.ui.info.type.iterable.util.IDynamicListAction;
import xy.reflect.ui.info.type.iterable.util.IDynamicListProperty;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.Filter;
import xy.reflect.ui.util.Mapper;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.Pair;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Factory that generates proxying and virtual type information objects
 * according to the given {@link InfoCustomizations} object.
 * 
 * @author olitank
 *
 */
public abstract class InfoCustomizationsFactory extends InfoProxyFactory {

	protected static final String ACTIVE_CUSTOMIZATIONS_KEY = InfoCustomizations.class.getName() + ".active";
	protected static final String ITEM_TYPE_CHANGED = InfoCustomizations.class.getName() + ".itemTypeChanged";
	protected static final String ORIGINAL_ITEM_TYPE = InfoCustomizations.class.getName() + ".originalItemType";

	protected CustomizedUI customizedUI;

	public abstract String getIdentifier();

	public abstract InfoCustomizations getInfoCustomizations();

	public InfoCustomizationsFactory(CustomizedUI customizedUI) {
		this.customizedUI = customizedUI;
	}

	protected void traceActiveCustomizations(Map<String, Object> specificProperties) {
		@SuppressWarnings("unchecked")
		List<InfoCustomizations> trace = (List<InfoCustomizations>) specificProperties.get(ACTIVE_CUSTOMIZATIONS_KEY);
		if (trace == null) {
			trace = new ArrayList<InfoCustomizations>();
			specificProperties.put(ACTIVE_CUSTOMIZATIONS_KEY, trace);
		}
		trace.add(getInfoCustomizations());
	}

	public static boolean areCustomizationsActive(InfoCustomizations infoCustomizations,
			Map<String, Object> specificProperties) {
		@SuppressWarnings("unchecked")
		List<InfoCustomizations> trace = (List<InfoCustomizations>) specificProperties.get(ACTIVE_CUSTOMIZATIONS_KEY);
		if (trace == null) {
			return false;
		}
		return trace.contains(infoCustomizations);
	}

	protected void traceListItemTypeChange(Map<String, Object> specificProperties, ITypeInfoFinder customItemTypeFinder,
			ITypeInfo itemType) {
		specificProperties.put(ITEM_TYPE_CHANGED + "." + getInfoCustomizations().toString(), Boolean.TRUE);
		specificProperties.put(ORIGINAL_ITEM_TYPE + "." + getInfoCustomizations(), itemType);
	}

	public static boolean isItemTypeChanged(InfoCustomizations infoCustomizations,
			Map<String, Object> specificProperties) {
		return Boolean.TRUE.equals(specificProperties.get(ITEM_TYPE_CHANGED + "." + infoCustomizations.toString()));
	}

	public static ITypeInfo getOriginalItemType(InfoCustomizations infoCustomizations,
			Map<String, Object> specificProperties) {
		return (ITypeInfo) specificProperties.get(ORIGINAL_ITEM_TYPE + "." + infoCustomizations);
	}

	@Override
	protected boolean isFactoryTracedFor(ITypeInfo base) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), base.getName());
		if (t != null) {
			if (t.getBaseTypeName() != null) {
				return false;
			}
		}
		return super.isFactoryTracedFor(base);
	}

	@Override
	public ITypeInfo wrapTypeInfo(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getBaseTypeName() != null) {
				type = new ChangedTypeNameFactory(t.getTypeName(), t.getBaseTypeName()).wrapTypeInfo(type);
				type = super.wrapTypeInfo(type);
				type = new ChangedTypeNameFactory(t.getBaseTypeName(), t.getTypeName()).wrapTypeInfo(type);
			}
		}
		return super.wrapTypeInfo(type);
	}

	@Override
	public ITypeInfo unwrapTypeInfo(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getBaseTypeName() != null) {
				type = ((ChangedTypeNameFactory) ((GeneratedBasicTypeInfoProxy) type).factory).unwrapTypeInfo(type);
				type = super.unwrapTypeInfo(type);
				type = ((ChangedTypeNameFactory) ((GeneratedBasicTypeInfoProxy) type).factory).unwrapTypeInfo(type);
			}
		}
		return super.unwrapTypeInfo(type);
	}

	@Override
	protected FieldsLayout getFieldsLayout(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getFieldsLayout() != null) {
				return t.getFieldsLayout();
			}
		}
		return super.getFieldsLayout(type);
	}

	@Override
	protected CategoriesStyle getCategoriesStyle(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getCategoriesStyle() != null) {
				return t.getCategoriesStyle();
			}
		}
		return super.getCategoriesStyle(type);
	}

	@Override
	protected MethodsLayout getMethodsLayout(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getMethodsLayout() != null) {
				return t.getMethodsLayout();
			}
		}
		return super.getMethodsLayout(type);
	}

	@Override
	protected boolean isItemNullValueSupported(IListTypeInfo listType) {
		ITypeInfo itemType = listType.getItemType();
		final ListCustomization l = InfoCustomizations.getListCustomization(this.getInfoCustomizations(),
				listType.getName(), (itemType == null) ? null : itemType.getName());
		if (l != null) {
			if (l.isItemNullValueAllowed()) {
				return true;
			}
		}
		return super.isItemNullValueSupported(listType);
	}

	@Override
	protected InitialItemValueCreationOption getInitialItemValueCreationOption(IListTypeInfo listType) {
		ITypeInfo itemType = listType.getItemType();
		final ListCustomization l = InfoCustomizations.getListCustomization(this.getInfoCustomizations(),
				listType.getName(), (itemType == null) ? null : itemType.getName());
		if (l != null) {
			if (l.getInitialItemValueCreationOption() != null) {
				return l.getInitialItemValueCreationOption();
			}
		}
		return super.getInitialItemValueCreationOption(listType);
	}

	@Override
	protected boolean isConcrete(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.isAbstractForced()) {
				return false;
			}
		}
		return super.isConcrete(type);
	}

	@Override
	protected boolean isImmutable(ITypeInfo type) {
		final TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(),
				type.getName());
		if (t != null) {
			if (t.isImmutableForced()) {
				return true;
			}
		}
		return super.isImmutable(type);
	}

	@Override
	protected boolean canPersist(ITypeInfo type) {
		final TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(),
				type.getName());
		if (t != null) {
			if ((t.getSavingMethodName() != null) && (t.getLoadingMethodName() != null)) {
				return true;
			}
		}
		return super.canPersist(type);
	}

	@Override
	protected void save(ITypeInfo type, Object object, OutputStream out) {
		final TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(),
				type.getName());
		if (t != null) {
			if (t.getSavingMethodName() != null) {
				Class<?> javaType;
				try {
					javaType = ClassUtils.getCachedClassforName(type.getName());
					Method method = javaType.getMethod(t.getSavingMethodName(), OutputStream.class);
					method.invoke(object, out);
					return;
				} catch (Exception e) {
					throw new ReflectionUIError(e);
				}
			}
		}
		super.save(type, object, out);
	}

	@Override
	protected void load(ITypeInfo type, Object object, InputStream in) {
		final TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(),
				type.getName());
		if (t != null) {
			if (t.getLoadingMethodName() != null) {
				Class<?> javaType;
				try {
					javaType = ClassUtils.getCachedClassforName(type.getName());
					Method method = javaType.getMethod(t.getLoadingMethodName(), InputStream.class);
					method.invoke(object, in);
					return;
				} catch (Exception e) {
					throw new ReflectionUIError(e);
				}
			}
		}
		super.load(type, object, in);
	}

	@Override
	protected boolean canCopy(ITypeInfo type, Object object) {
		final TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(),
				type.getName());
		if (t != null) {
			if (t.isCopyForbidden()) {
				return false;
			}
			if (super.canCopy(type, object)) {
				return true;
			}
			// if possible use specified persistence methods to implement copy/cut/paste
			if ((t.getSavingMethodName() != null) && (t.getLoadingMethodName() != null)) {
				Class<?> javaType;
				try {
					javaType = ClassUtils.getCachedClassforName(type.getName());
				} catch (Exception e) {
					throw new ReflectionUIError(e);
				}
				if (object.getClass().equals(javaType)) {
					if (ReflectionUIUtils.canCreateDefaultInstance(wrapTypeInfo(type), false)) {
						return true;
					}
				}
			}
		}
		return super.canCopy(type, object);
	}

	@Override
	protected Object copy(ITypeInfo type, Object object) {
		final TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(),
				type.getName());
		if (t != null) {
			if (t.isCopyForbidden()) {
				throw new ReflectionUIError();
			}
			if (super.canCopy(type, object)) {
				return super.copy(type, object);
			}
			// if possible use specified persistence methods to implement copy/cut/paste
			if ((t.getSavingMethodName() != null) && (t.getLoadingMethodName() != null)) {
				Class<?> javaType;
				try {
					javaType = ClassUtils.getCachedClassforName(type.getName());
				} catch (Exception e) {
					throw new ReflectionUIError(e);
				}
				if (object.getClass().equals(javaType)) {
					if (ReflectionUIUtils.canCreateDefaultInstance(wrapTypeInfo(type), false)) {
						ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
						try {
							Method method = javaType.getMethod(t.getSavingMethodName(), OutputStream.class);
							method.invoke(object, outputBuffer);
						} catch (Exception e) {
							throw new ReflectionUIError(e);
						}
						Object newInstance = ReflectionUIUtils.createDefaultInstance(wrapTypeInfo(type), false);
						ByteArrayInputStream inputBuffer = new ByteArrayInputStream(outputBuffer.toByteArray());
						try {
							Method method = javaType.getMethod(t.getLoadingMethodName(), InputStream.class);
							method.invoke(newInstance, inputBuffer);
						} catch (Exception e) {
							throw new ReflectionUIError(e);
						}
						return newInstance;
					}
				}
			}
		}
		return super.copy(type, object);
	}

	@Override
	protected Object[] toArray(IListTypeInfo listType, Object listValue) {
		ITypeInfo itemType = listType.getItemType();
		final ListCustomization l = InfoCustomizations.getListCustomization(this.getInfoCustomizations(),
				listType.getName(), (itemType == null) ? null : itemType.getName());
		if (l != null) {
			if (l.isListSorted()) {
				Object[] result = super.toArray(listType, listValue);
				Arrays.sort(result);
				return result;
			}
		}
		return super.toArray(listType, listValue);
	}

	@Override
	protected Object[] getValues(IEnumerationTypeInfo enumType) {
		EnumerationCustomization e = InfoCustomizations.getEnumerationCustomization(this.getInfoCustomizations(),
				enumType.getName());
		if (e != null) {
			List<Object> result = new ArrayList<Object>();
			List<IEnumerationItemInfo> valueInfos = new ArrayList<IEnumerationItemInfo>();
			for (Object value : super.getValues(enumType)) {
				IEnumerationItemInfo valueInfo = getValueInfo(enumType, value);
				valueInfos.add(valueInfo);
				EnumerationItemCustomization i = InfoCustomizations.getEnumerationItemCustomization(e,
						valueInfo.getName());
				if (i != null) {
					if (i.isHidden()) {
						continue;
					}
				}
				result.add(value);
			}

			final List<String> customOrder = e.getItemsCustomOrder();
			if (customOrder != null) {
				final Comparator<IEnumerationItemInfo> valueInfoComparator = ReflectionUIUtils
						.getInfosComparator(customOrder, valueInfos);
				Collections.sort(result, new Comparator<Object>() {
					@Override
					public int compare(Object value1, Object value2) {
						return valueInfoComparator.compare(getValueInfo(enumType, value1),
								getValueInfo(enumType, value2));
					}
				});
			}

			return result.toArray();
		}
		return super.getValues(enumType);
	}

	@Override
	protected boolean isDynamicEnumeration(IEnumerationTypeInfo enumType) {
		EnumerationCustomization e = InfoCustomizations.getEnumerationCustomization(this.getInfoCustomizations(),
				enumType.getName());
		if (e != null) {
			if (e.isDynamicEnumerationForced()) {
				return true;
			}
		}
		return super.isDynamicEnumeration(enumType);
	}

	@Override
	protected IEnumerationItemInfo getValueInfo(IEnumerationTypeInfo enumType, Object object) {
		IEnumerationItemInfo result = super.getValueInfo(enumType, object);
		EnumerationCustomization e = InfoCustomizations.getEnumerationCustomization(this.getInfoCustomizations(),
				enumType.getName());
		if (e != null) {
			final EnumerationItemCustomization i = InfoCustomizations.getEnumerationItemCustomization(e,
					result.getName());
			if (i != null) {
				return new EnumerationItemInfoProxy(result) {

					@Override
					public String getCaption() {
						if (i.getCustomCaption() != null) {
							return i.getCustomCaption();
						}
						return super.getCaption();
					}

					@Override
					public ResourcePath getIconImagePath() {
						if (i.getIconImagePath() != null) {
							return i.getIconImagePath();
						}
						return super.getIconImagePath();
					}

				};
			}
		}
		return result;
	}

	@Override
	protected List<IDynamicListProperty> getDynamicProperties(IListTypeInfo listType,
			List<? extends ItemPosition> selection,
			final Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
		ITypeInfo itemType = listType.getItemType();
		final ListCustomization l = InfoCustomizations.getListCustomization(this.getInfoCustomizations(),
				listType.getName(), (itemType == null) ? null : itemType.getName());
		if (l != null) {
			List<IDynamicListProperty> result = super.getDynamicProperties(listType, selection,
					listModificationFactoryAccessor);
			result = new ArrayList<IDynamicListProperty>(result);
			for (final ListItemFieldShortcut shortcut : l.getAllowedItemFieldShortcuts()) {
				boolean fieldFound = false;
				if (selection.size() == 1) {
					final ItemPosition itemPosition = selection.get(0);
					final Object item = itemPosition.getItem();
					if (item != null) {
						final ITypeInfo actualItemType = customizedUI
								.buildTypeInfo(customizedUI.getTypeInfoSource(item));
						for (final IFieldInfo itemField : actualItemType.getFields()) {
							if (itemField.getName().equals(shortcut.getFieldName())) {
								AbstractListProperty property = new AbstractListProperty() {

									IFieldInfo itemPositionAsField = new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {

										ListModificationFactory listModificationFactory = listModificationFactoryAccessor
												.get(itemPosition);

										@Override
										public Object getValue(Object object) {
											return item;
										}

										@Override
										public String getCaption() {
											return itemPosition.getContainingListTitle();
										}

										@Override
										public void setValue(Object object, Object value) {
											listModificationFactory.set(itemPosition.getIndex(), item)
													.applyAndGetOpposite();
										}

										@Override
										public boolean isGetOnly() {
											return !listModificationFactory.canSet(itemPosition.getIndex());
										}

									};
									SubFieldInfo delegate = new SubFieldInfo(customizedUI, itemPositionAsField,
											itemField, actualItemType);

									@Override
									public boolean isEnabled() {
										return true;
									}

									@Override
									public String getName() {
										return shortcut.getFieldName();
									}

									@Override
									public String getCaption() {
										if (shortcut.getCustomFieldCaption() != null) {
											return shortcut.getCustomFieldCaption();
										} else {
											return delegate.getCaption();
										}
									}

									public ITypeInfo getType() {
										return delegate.getType();
									}

									public List<IMethodInfo> getAlternativeConstructors(Object object) {
										return delegate.getAlternativeConstructors(object);
									}

									public List<IMethodInfo> getAlternativeListItemConstructors(Object object) {
										return delegate.getAlternativeListItemConstructors(object);
									}

									public Object getValue(Object object) {
										return delegate.getValue(object);
									}

									@Override
									public boolean hasValueOptions(Object object) {
										return delegate.hasValueOptions(object);
									}

									public Object[] getValueOptions(Object object) {
										return delegate.getValueOptions(object);
									}

									public boolean isGetOnly() {
										return delegate.isGetOnly();
									}

									public boolean isTransient() {
										return delegate.isTransient();
									}

									public void setValue(Object object, Object subFieldValue) {
										delegate.setValue(object, subFieldValue);
									}

									public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
										return delegate.getNextUpdateCustomUndoJob(object, value);
									}

									public boolean isNullValueDistinct() {
										return delegate.isNullValueDistinct();
									}

									public String getNullValueLabel() {
										return delegate.getNullValueLabel();
									}

									public ValueReturnMode getValueReturnMode() {
										return delegate.getValueReturnMode();
									}

									public InfoCategory getCategory() {
										return delegate.getCategory();
									}

									public String getOnlineHelp() {
										return delegate.getOnlineHelp();
									}

									public Map<String, Object> getSpecificProperties() {
										return delegate.getSpecificProperties();
									}

									public boolean isFormControlMandatory() {
										return delegate.isFormControlMandatory();
									}

									public boolean isFormControlEmbedded() {
										return delegate.isFormControlEmbedded();
									}

									public IInfoFilter getFormControlFilter() {
										return delegate.getFormControlFilter();
									}

								};
								result.add(property);
								fieldFound = true;
								break;
							}
						}
					}
				}
				if ((!fieldFound) && shortcut.isAlwaysShown()) {
					AbstractListProperty property = new AbstractListProperty() {

						@Override
						public boolean isEnabled() {
							return false;
						}

						@Override
						public String getName() {
							return shortcut.getFieldName();
						}

						@Override
						public String getCaption() {
							if (shortcut.getCustomFieldCaption() != null) {
								return shortcut.getCustomFieldCaption();
							} else {
								return ReflectionUIUtils.identifierToCaption(shortcut.getFieldName());
							}
						}

						@Override
						public void setValue(Object object, Object value) {
							throw new UnsupportedOperationException();
						}

						@Override
						public boolean isNullValueDistinct() {
							throw new UnsupportedOperationException();
						}

						@Override
						public boolean isGetOnly() {
							throw new UnsupportedOperationException();
						}

						@Override
						public boolean isTransient() {
							throw new UnsupportedOperationException();
						}

						@Override
						public ValueReturnMode getValueReturnMode() {
							throw new UnsupportedOperationException();
						}

						@Override
						public Object getValue(Object object) {
							throw new UnsupportedOperationException();
						}

						@Override
						public ITypeInfo getType() {
							throw new UnsupportedOperationException();
						}
					};
					result.add(property);

				}
			}
			return result;
		}
		return super.getDynamicProperties(listType, selection, listModificationFactoryAccessor);
	}

	@Override
	protected List<IDynamicListAction> getDynamicActions(IListTypeInfo listType, List<? extends ItemPosition> selection,
			final Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
		ITypeInfo itemType = listType.getItemType();
		final ListCustomization l = InfoCustomizations.getListCustomization(this.getInfoCustomizations(),
				listType.getName(), (itemType == null) ? null : itemType.getName());
		if (l != null) {
			List<IDynamicListAction> result = super.getDynamicActions(listType, selection,
					listModificationFactoryAccessor);
			result = new ArrayList<IDynamicListAction>(result);

			for (final ListItemMethodShortcut shortcut : l.getAllowedItemMethodShortcuts()) {
				final String methodName = ReflectionUIUtils
						.extractMethodNameFromSignature(shortcut.getMethodSignature());
				boolean methodFound = false;
				if (selection.size() == 1) {
					final ItemPosition itemPosition = selection.get(0);
					final Object item = itemPosition.getItem();
					if (item != null) {
						final ITypeInfo actualItemType = customizedUI
								.buildTypeInfo(customizedUI.getTypeInfoSource(item));
						for (final IMethodInfo itemMethod : actualItemType.getMethods()) {
							if (itemMethod.getSignature().equals(shortcut.getMethodSignature())) {
								AbstractListAction action = new AbstractListAction() {

									IFieldInfo itemPositionAsField = new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {

										ListModificationFactory listModificationFactory = listModificationFactoryAccessor
												.get(itemPosition);

										@Override
										public Object getValue(Object object) {
											return item;
										}

										@Override
										public String getCaption() {
											return itemPosition.getContainingListTitle();
										}

										@Override
										public void setValue(Object object, Object value) {
											listModificationFactory.set(itemPosition.getIndex(), item)
													.applyAndGetOpposite();
										}

										@Override
										public boolean isGetOnly() {
											return !listModificationFactory.canSet(itemPosition.getIndex());
										}

									};
									SubMethodInfo delegate = new SubMethodInfo(customizedUI, itemPositionAsField,
											itemMethod, actualItemType);

									boolean returnValueVoid = false;
									ITypeInfo returnValueType;

									@Override
									public String getName() {
										return methodName;
									}

									@Override
									public boolean isEnabled(Object object) {
										return delegate.isEnabled(object);
									}

									@Override
									public String getCaption() {
										if (shortcut.getCustomMethodCaption() != null) {
											return shortcut.getCustomMethodCaption();
										} else {
											return delegate.getCaption();
										}
									}

									public ResourcePath getIconImagePath() {
										if (shortcut.getCustomIconImagePath() != null) {
											return shortcut.getCustomIconImagePath();
										} else {
											return delegate.getIconImagePath();
										}
									}

									public ITypeInfo getReturnValueType() {
										if (returnValueVoid) {
											return null;
										}
										if (returnValueType == null) {
											if (delegate.getReturnValueType() == null) {
												returnValueVoid = true;
											} else {
												returnValueType = customizedUI.buildTypeInfo(new TypeInfoSourceProxy(
														delegate.getReturnValueType().getSource()) {
													@Override
													public SpecificitiesIdentifier getSpecificitiesIdentifier() {
														return null;
													}

													@Override
													protected String getTypeInfoProxyFactoryIdentifier() {
														return "DynamicActionReturnValueTypeInfoProxyFactory [of="
																+ getClass().getName() + ", listType="
																+ listType.getName() + ", method="
																+ shortcut.getMethodSignature() + "]";
													}
												});
											}
										}
										return returnValueType;
									}

									public Object invoke(Object object, InvocationData invocationData) {
										return delegate.invoke(object, invocationData);
									}

									public boolean isReadOnly() {
										return delegate.isReadOnly();
									}

									public Runnable getNextInvocationUndoJob(Object object,
											InvocationData invocationData) {
										return delegate.getNextInvocationUndoJob(object, invocationData);
									}

									public boolean isNullReturnValueDistinct() {
										return delegate.isNullReturnValueDistinct();
									}

									public String getNullReturnValueLabel() {
										return delegate.getNullReturnValueLabel();
									}

									public ValueReturnMode getValueReturnMode() {
										return delegate.getValueReturnMode();
									}

									public InfoCategory getCategory() {
										return delegate.getCategory();
									}

									public String getOnlineHelp() {
										return delegate.getOnlineHelp();
									}

									public Map<String, Object> getSpecificProperties() {
										return delegate.getSpecificProperties();
									}

									public List<IParameterInfo> getParameters() {
										return delegate.getParameters();
									}

									public void validateParameters(Object object, InvocationData invocationData)
											throws Exception {
										delegate.validateParameters(object, invocationData);
									}

									public boolean isReturnValueDetached() {
										return delegate.isReturnValueDetached();
									}

									public String getSignature() {
										return delegate.getSignature();
									}

									public String getConfirmationMessage(Object object, InvocationData invocationData) {
										return delegate.getConfirmationMessage(object, invocationData);
									}

									public boolean isReturnValueIgnored() {
										return delegate.isReturnValueIgnored();
									}

								};
								result.add(action);
								methodFound = true;
								break;
							}
						}
					}
				}
				if ((!methodFound) && shortcut.isAlwaysShown()) {
					result.add(new AbstractListAction() {

						@Override
						public boolean isNullReturnValueDistinct() {
							return false;
						}

						@Override
						public String getName() {
							return methodName;
						}

						@Override
						public String getCaption() {
							if (shortcut.getCustomMethodCaption() != null) {
								return shortcut.getCustomMethodCaption();
							} else {
								return ReflectionUIUtils.identifierToCaption(methodName);
							}
						}

						public ResourcePath getIconImagePath() {
							if (shortcut.getCustomIconImagePath() != null) {
								return shortcut.getCustomIconImagePath();
							} else {
								return null;
							}
						}

						@Override
						public boolean isEnabled(Object object) {
							return false;
						}

						@Override
						public Object invoke(Object object, InvocationData invocationData) {
							throw new UnsupportedOperationException();
						}

						@Override
						public ValueReturnMode getValueReturnMode() {
							throw new UnsupportedOperationException();
						}

					});
				}
			}
			return result;
		}
		return super.getDynamicActions(listType, selection, listModificationFactoryAccessor);
	}

	@Override
	protected IListItemDetailsAccessMode getDetailsAccessMode(IListTypeInfo listType) {
		ITypeInfo itemType = listType.getItemType();
		ListCustomization l = InfoCustomizations.getListCustomization(this.getInfoCustomizations(), listType.getName(),
				(itemType == null) ? null : itemType.getName());
		if (l != null) {
			if (l.getCustomDetailsAccessMode() != null) {
				return l.getCustomDetailsAccessMode();
			}
		}
		return super.getDetailsAccessMode(listType);
	}

	@Override
	protected Object fromArray(IListTypeInfo listType, Object[] array) {
		ITypeInfo itemType = listType.getItemType();
		ListCustomization l = InfoCustomizations.getListCustomization(this.getInfoCustomizations(), listType.getName(),
				(itemType == null) ? null : itemType.getName());
		if (l != null) {
			if (l.getEditOptions() != null) {
				if (l.getEditOptions().getListInstanciationOption() != null) {
					Object newListInstance;
					if (l.getEditOptions().getListInstanciationOption().getCustomInstanceTypeFinder() != null) {
						ITypeInfo customInstanceType = l.getEditOptions().getListInstanciationOption()
								.getCustomInstanceTypeFinder().find(customizedUI, null);
						newListInstance = ReflectionUIUtils.createDefaultInstance(customInstanceType);
					} else {
						newListInstance = ReflectionUIUtils.createDefaultInstance(listType);
					}
					super.replaceContent(listType, newListInstance, array);
					return newListInstance;
				}
			}
		}
		return super.fromArray(listType, array);
	}

	@Override
	protected boolean canInstanciateFromArray(IListTypeInfo listType) {
		ITypeInfo itemType = listType.getItemType();
		ListCustomization l = InfoCustomizations.getListCustomization(this.getInfoCustomizations(), listType.getName(),
				(itemType == null) ? null : itemType.getName());
		if (l != null) {
			if (l.getEditOptions() == null) {
				return false;
			}
			if (l.getEditOptions().getListInstanciationOption() != null) {
				return true;
			}
		}
		return super.canInstanciateFromArray(listType);
	}

	@Override
	protected boolean canReplaceContent(IListTypeInfo listType) {
		ITypeInfo itemType = listType.getItemType();
		ListCustomization l = InfoCustomizations.getListCustomization(this.getInfoCustomizations(), listType.getName(),
				(itemType == null) ? null : itemType.getName());
		if (l != null) {
			if (l.getEditOptions() == null) {
				return false;
			}
			if (l.getEditOptions().getListInstanciationOption() != null) {
				return false;
			}
		}
		return super.canReplaceContent(listType);
	}

	@Override
	protected boolean isInsertionAllowed(IListTypeInfo listType) {
		ITypeInfo itemType = listType.getItemType();
		ListCustomization l = InfoCustomizations.getListCustomization(this.getInfoCustomizations(), listType.getName(),
				(itemType == null) ? null : itemType.getName());
		if (l != null) {
			if ((l.getEditOptions() == null) || !l.getEditOptions().isItemCreationEnabled()) {
				return false;
			}
		}
		return super.isInsertionAllowed(listType);
	}

	@Override
	protected boolean isRemovalAllowed(IListTypeInfo listType) {
		ITypeInfo itemType = listType.getItemType();
		ListCustomization l = InfoCustomizations.getListCustomization(this.getInfoCustomizations(), listType.getName(),
				(itemType == null) ? null : itemType.getName());
		if (l != null) {
			if ((l.getEditOptions() == null) || !l.getEditOptions().isItemDeletionEnabled()) {
				return false;
			}
		}
		return super.isRemovalAllowed(listType);
	}

	@Override
	protected boolean isOrdered(IListTypeInfo listType) {
		ITypeInfo itemType = listType.getItemType();
		ListCustomization l = InfoCustomizations.getListCustomization(this.getInfoCustomizations(), listType.getName(),
				(itemType == null) ? null : itemType.getName());
		if (l != null) {
			if ((l.getEditOptions() == null) || !l.getEditOptions().isItemMoveEnabled()) {
				return false;
			}
			if (l.isListSorted()) {
				return false;
			}
		}
		return super.isOrdered(listType);
	}

	@Override
	protected boolean canViewItemDetails(IListTypeInfo listType) {
		ITypeInfo itemType = listType.getItemType();
		ListCustomization l = InfoCustomizations.getListCustomization(this.getInfoCustomizations(), listType.getName(),
				(itemType == null) ? null : itemType.getName());
		if (l != null) {
			if (l.isItemDetailsViewDisabled()) {
				return false;
			}
		}
		return super.canViewItemDetails(listType);
	}

	@Override
	protected boolean isModificationStackAccessible(ITypeInfo type) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (tc != null) {
			if (tc.isUndoManagementHidden()) {
				return false;
			}
		}
		return super.isModificationStackAccessible(type);
	}

	@Override
	protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (tc != null) {
			if (tc.getPolymorphicSubTypeFinders() != null) {
				List<ITypeInfo> result = new ArrayList<ITypeInfo>(super.getPolymorphicInstanceSubTypes(type));
				for (ITypeInfoFinder finder : tc.getPolymorphicSubTypeFinders()) {
					ITypeInfo subType = finder.find(customizedUI, null);
					result.add(subType);
				}
				return result;
			}
		}
		return super.getPolymorphicInstanceSubTypes(type);
	}

	@Override
	protected IListStructuralInfo getStructuralInfo(IListTypeInfo listType) {
		ITypeInfo itemType = listType.getItemType();
		ListCustomization l = InfoCustomizations.getListCustomization(this.getInfoCustomizations(), listType.getName(),
				(itemType == null) ? null : itemType.getName());
		if (l != null) {
			IListStructuralInfo base = super.getStructuralInfo(listType);
			return new CustomizedListStructuralInfo(customizedUI, base, listType, l);
		}
		return super.getStructuralInfo(listType);
	}

	@Override
	protected ITypeInfo getItemType(IListTypeInfo listType) {
		ITypeInfo itemType = listType.getItemType();
		ListCustomization l = InfoCustomizations.getListCustomization(this.getInfoCustomizations(), listType.getName(),
				(itemType == null) ? null : itemType.getName());
		if (l != null) {
			if (l.getCustomItemTypeFinder() != null) {
				return l.getCustomItemTypeFinder().find(customizedUI, null);
			}
		}
		return super.getItemType(listType);
	}

	@Override
	protected ResourcePath getIconImagePath(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			ResourcePath result = t.getIconImagePath();
			if (result != null) {
				if (result.getSpecification().length() > 0) {
					return result;
				}
			}
		}
		return super.getIconImagePath(type);
	}

	@Override
	protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
		Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(type));
		traceActiveCustomizations(result);
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getSpecificProperties() != null) {
				if (t.getSpecificProperties().size() > 0) {
					result.putAll(t.getSpecificProperties());
				}
			}
		}
		if (type instanceof IListTypeInfo) {
			IListTypeInfo listType = (IListTypeInfo) type;
			ITypeInfo itemType = listType.getItemType();
			ListCustomization l = InfoCustomizations.getListCustomization(this.getInfoCustomizations(),
					listType.getName(), (itemType == null) ? null : itemType.getName());
			if (l != null) {
				if (l.getCustomItemTypeFinder() != null) {
					traceListItemTypeChange(result, l.getCustomItemTypeFinder(), itemType);
				}
			}
		}
		return result;
	}

	@Override
	protected Map<String, Object> getSpecificProperties(IApplicationInfo appInfo) {
		Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(appInfo));
		traceActiveCustomizations(result);
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization != null) {
			if (appCustomization.getSpecificProperties() != null) {
				if (appCustomization.getSpecificProperties().size() > 0) {
					result.putAll(appCustomization.getSpecificProperties());
				}
			}
		}
		return result;
	}

	@Override
	protected Map<String, Object> getSpecificProperties(IEnumerationItemInfo info, ITypeInfo parentEnumType) {
		Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(info, parentEnumType));
		traceActiveCustomizations(result);
		EnumerationCustomization e = InfoCustomizations.getEnumerationCustomization(this.getInfoCustomizations(),
				parentEnumType.getName());
		if (e != null) {
			final EnumerationItemCustomization i = InfoCustomizations.getEnumerationItemCustomization(e,
					info.getName());
			if (i != null) {
				if (i.getSpecificProperties() != null) {
					if (i.getSpecificProperties().size() > 0) {
						result.putAll(i.getSpecificProperties());
					}
				}
			}
		}
		return result;
	}

	@Override
	protected List<IMethodInfo> getConstructors(ITypeInfo containingType) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>(getMembers(containingType).getOutputConstructors());
		result = sortMethods(result, containingType);
		return result;
	}

	@Override
	protected List<IFieldInfo> getFields(final ITypeInfo containingType) {
		List<IFieldInfo> result = new ArrayList<IFieldInfo>(getMembers(containingType).getOutputFields());
		result = sortFields(result, containingType);
		return result;
	}

	@Override
	protected List<IMethodInfo> getMethods(ITypeInfo containingType) {
		List<IMethodInfo> result = new ArrayList<IMethodInfo>(getMembers(containingType).getOutputMethods());
		result = sortMethods(result, containingType);
		return result;
	}

	@Override
	protected MenuModel getMenuModel(ITypeInfo type) {
		return getMembers(type).getMenuModel();
	}

	protected List<IFieldInfo> sortFields(List<IFieldInfo> fields, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(),
				containingType.getName());
		if (t != null) {
			if (t != null) {
				List<IFieldInfo> result = new ArrayList<IFieldInfo>(fields);
				if (t.getCustomFieldsOrder() != null) {
					Collections.sort(result, ReflectionUIUtils.getInfosComparator(t.getCustomFieldsOrder(), result));
				}
				return result;
			}
		}
		return fields;
	}

	protected List<IMethodInfo> sortMethods(List<IMethodInfo> methods, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(),
				containingType.getName());
		if (t != null) {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>(methods);
			if (t.getCustomMethodsOrder() != null) {
				Collections.sort(result, ReflectionUIUtils.getInfosComparator(t.getCustomMethodsOrder(), result));
			}
			return result;
		}
		return methods;
	}

	@Override
	protected String getCaption(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getCustomTypeCaption() != null) {
				return t.getCustomTypeCaption();
			}
		}
		return super.getCaption(type);
	}

	@Override
	protected ResourcePath getFormBackgroundImagePath(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getFormBackgroundImagePath() != null) {
				return t.getFormBackgroundImagePath();
			}
		}
		return super.getFormBackgroundImagePath(type);
	}

	@Override
	protected ColorSpecification getFormBackgroundColor(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getFormBackgroundColor() != null) {
				return t.getFormBackgroundColor();
			}
		}
		return super.getFormBackgroundColor(type);
	}

	@Override
	protected ColorSpecification getFormForegroundColor(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getFormForegroundColor() != null) {
				return t.getFormForegroundColor();
			}
		}
		return super.getFormForegroundColor(type);
	}

	@Override
	protected ColorSpecification getFormBorderColor(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getFormBorderColor() != null) {
				return t.getFormBorderColor();
			}
		}
		return super.getFormBorderColor(type);
	}

	@Override
	protected ColorSpecification getFormEditorsForegroundColor(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getFormEditorsForegroundColor() != null) {
				return t.getFormEditorsForegroundColor();
			}
		}
		return super.getFormEditorsForegroundColor(type);
	}

	@Override
	protected ColorSpecification getFormEditorsBackgroundColor(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getFormEditorsBackgroundColor() != null) {
				return t.getFormEditorsBackgroundColor();
			}
		}
		return super.getFormEditorsBackgroundColor(type);
	}

	@Override
	protected ColorSpecification getFormButtonBackgroundColor(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getFormButtonBackgroundColor() != null) {
				return t.getFormButtonBackgroundColor();
			}
		}
		return super.getFormButtonBackgroundColor(type);
	}

	@Override
	protected ColorSpecification getFormButtonForegroundColor(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getFormButtonForegroundColor() != null) {
				return t.getFormButtonForegroundColor();
			}
		}
		return super.getFormButtonForegroundColor(type);
	}

	@Override
	protected ColorSpecification getFormButtonBorderColor(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getFormButtonBorderColor() != null) {
				return t.getFormButtonBorderColor();
			}
		}
		return super.getFormButtonBorderColor(type);
	}

	@Override
	protected ResourcePath getFormButtonBackgroundImagePath(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getFormButtonBackgroundImagePath() != null) {
				return t.getFormButtonBackgroundImagePath();
			}
		}
		return super.getFormButtonBackgroundImagePath(type);
	}

	@Override
	protected ColorSpecification getCategoriesBackgroundColor(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getCategoriesBackgroundColor() != null) {
				return t.getCategoriesBackgroundColor();
			}
		}
		return super.getCategoriesBackgroundColor(type);
	}

	@Override
	protected ColorSpecification getCategoriesForegroundColor(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getCategoriesForegroundColor() != null) {
				return t.getCategoriesForegroundColor();
			}
		}
		return super.getCategoriesForegroundColor(type);
	}

	@Override
	protected String getName(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.getApplicationName() != null) {
			return appCustomization.getApplicationName();
		}
		return super.getName(appInfo);
	}

	@Override
	protected String getCaption(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.getCustomApplicationCaption() != null) {
			return appCustomization.getCustomApplicationCaption();
		}
		return super.getCaption(appInfo);
	}

	@Override
	protected ResourcePath getIconImagePath(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.getIconImagePath() != null) {
			return appCustomization.getIconImagePath();
		}
		return super.getIconImagePath(appInfo);
	}

	@Override
	protected String getOnlineHelp(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.getOnlineHelp() != null) {
			return appCustomization.getOnlineHelp();
		}
		return super.getOnlineHelp(appInfo);
	}

	@Override
	protected boolean isSystemIntegrationCrossPlatform(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.isSystemIntegrationCrossPlatform()) {
			return true;
		}
		return super.isSystemIntegrationCrossPlatform(appInfo);
	}

	@Override
	protected ColorSpecification getMainBackgroundColor(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.getMainBackgroundColor() != null) {
			return appCustomization.getMainBackgroundColor();
		}
		return super.getMainBackgroundColor(appInfo);
	}

	@Override
	protected ColorSpecification getMainForegroundColor(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.getMainForegroundColor() != null) {
			return appCustomization.getMainForegroundColor();
		}
		return super.getMainForegroundColor(appInfo);
	}

	@Override
	protected ColorSpecification getMainBorderColor(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.getMainBorderColor() != null) {
			return appCustomization.getMainBorderColor();
		}
		return super.getMainBorderColor(appInfo);
	}

	@Override
	protected ColorSpecification getMainEditorBackgroundColor(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.getMainEditorBackgroundColor() != null) {
			return appCustomization.getMainEditorBackgroundColor();
		}
		return super.getMainEditorBackgroundColor(appInfo);
	}

	@Override
	protected ColorSpecification getMainEditorForegroundColor(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.getMainEditorForegroundColor() != null) {
			return appCustomization.getMainEditorForegroundColor();
		}
		return super.getMainEditorForegroundColor(appInfo);
	}

	@Override
	protected ResourcePath getMainBackgroundImagePath(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.getMainBackgroundImagePath() != null) {
			return appCustomization.getMainBackgroundImagePath();
		}
		return super.getMainBackgroundImagePath(appInfo);
	}

	@Override
	protected ColorSpecification getMainButtonBackgroundColor(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.getMainButtonBackgroundColor() != null) {
			return appCustomization.getMainButtonBackgroundColor();
		}
		return super.getMainButtonBackgroundColor(appInfo);
	}

	@Override
	protected ColorSpecification getMainButtonBorderColor(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.getMainButtonBorderColor() != null) {
			return appCustomization.getMainButtonBorderColor();
		}
		return super.getMainButtonBorderColor(appInfo);
	}

	@Override
	protected ResourcePath getMainButtonBackgroundImagePath(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.getMainButtonBackgroundImagePath() != null) {
			return appCustomization.getMainButtonBackgroundImagePath();
		}
		return super.getMainButtonBackgroundImagePath(appInfo);
	}

	@Override
	protected ColorSpecification getMainButtonForegroundColor(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.getMainButtonForegroundColor() != null) {
			return appCustomization.getMainButtonForegroundColor();
		}
		return super.getMainButtonForegroundColor(appInfo);
	}

	@Override
	protected ColorSpecification getTitleBackgroundColor(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.getTitleBackgroundColor() != null) {
			return appCustomization.getTitleBackgroundColor();
		}
		return super.getTitleBackgroundColor(appInfo);
	}

	@Override
	protected ColorSpecification getTitleForegroundColor(IApplicationInfo appInfo) {
		ApplicationCustomization appCustomization = this.getInfoCustomizations().getAppplicationCustomization();
		if (appCustomization.getTitleForegroundColor() != null) {
			return appCustomization.getTitleForegroundColor();
		}
		return super.getTitleForegroundColor(appInfo);
	}

	@Override
	protected Dimension getFormPreferredSize(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if ((t.getFormWidth() != null) || (t.getFormHeight() != null)) {
				Dimension result = super.getFormPreferredSize(type);
				if (result == null) {
					result = new Dimension(-1, -1);
				}
				FormSizeCustomization width = t.getFormWidth();
				if (width != null) {
					if (width.getUnit() == FormSizeUnit.PIXELS) {
						result.width = width.getValue();
					} else if (width.getUnit() == FormSizeUnit.SCREEN_PERCENT) {
						Dimension screenSize = MiscUtils.getDefaultScreenSize();
						result.width = Math.round((width.getValue() / 100f) * screenSize.width);
					} else {
						throw new ReflectionUIError();
					}
				}
				FormSizeCustomization height = t.getFormHeight();
				if (height != null) {
					if (height.getUnit() == FormSizeUnit.PIXELS) {
						result.height = height.getValue();
					} else if (height.getUnit() == FormSizeUnit.SCREEN_PERCENT) {
						Dimension screenSize = MiscUtils.getDefaultScreenSize();
						result.height = Math.round((height.getValue() / 100f) * screenSize.height);
					} else {
						throw new ReflectionUIError();
					}
				}
				return result;
			}
		}
		return super.getFormPreferredSize(type);
	}

	@Override
	protected int getFormSpacing(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getFormSpacing() != null) {
				return t.getFormSpacing();
			}
		}
		return super.getFormSpacing(type);
	}

	@Override
	protected void validate(ITypeInfo type, Object object) throws Exception {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			for (IMethodInfo method : getMethods(type)) {
				MethodCustomization m = InfoCustomizations.getMethodCustomization(t, method.getSignature(), false);
				if (m != null) {
					if (m.isValidating()) {
						if (ReflectionUIUtils.requiresParameterValue(method)) {
							throw new ReflectionUIError(
									"Invalid validating method: Parameter value(s) required: " + method.getSignature());
						}
						method.invoke(object, new InvocationData(object, method));
					}
				}
			}
		}
		super.validate(type, object);
	}

	@Override
	protected boolean onFormVisibilityChange(ITypeInfo type, Object object, boolean visible) {
		boolean formUpdateNeeded = false;
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			for (IMethodInfo method : getMethods(type)) {
				MethodCustomization m = InfoCustomizations.getMethodCustomization(t, method.getSignature(), false);
				if (m != null) {
					if ((m.isRunWhenObjectShown() && visible) || (m.isRunWhenObjectHidden() && !visible)) {
						if (ReflectionUIUtils.requiresParameterValue(method)) {
							throw new ReflectionUIError(
									"Cannot call method on object visibilty change: Parameter value(s) required: "
											+ method.getSignature());
						}
						method.invoke(object, new InvocationData(object, method));
						formUpdateNeeded = formUpdateNeeded || !method.isReadOnly();
					}
				}
			}
		}
		return super.onFormVisibilityChange(type, object, visible) || formUpdateNeeded;
	}

	@Override
	protected ITransactionInfo getTransaction(ITypeInfo type, final Object object) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			final Map<TransactionalRole, List<IMethodInfo>> transactionMethodsByRole = new HashMap<InfoCustomizations.TransactionalRole, List<IMethodInfo>>();
			transactionMethodsByRole.put(TransactionalRole.BEGIN, new ArrayList<IMethodInfo>());
			transactionMethodsByRole.put(TransactionalRole.COMMIT, new ArrayList<IMethodInfo>());
			transactionMethodsByRole.put(TransactionalRole.ROLLBACK, new ArrayList<IMethodInfo>());
			for (IMethodInfo method : getMethods(type)) {
				MethodCustomization m = InfoCustomizations.getMethodCustomization(t, method.getSignature(), false);
				if (m != null) {
					if (m.getTransactionalRole() != null) {
						if (ReflectionUIUtils.requiresParameterValue(method)) {
							throw new ReflectionUIError("Invalid transactional method: Parameter value(s) required: "
									+ method.getSignature());
						}
						transactionMethodsByRole.get(m.getTransactionalRole()).add(method);
					}
				}
			}
			if ((transactionMethodsByRole.get(TransactionalRole.BEGIN).size() > 0)
					|| (transactionMethodsByRole.get(TransactionalRole.COMMIT).size() > 0)
					|| (transactionMethodsByRole.get(TransactionalRole.ROLLBACK).size() > 0)) {
				return new ITransactionInfo() {

					@Override
					public void rollback() {
						for (IMethodInfo method : transactionMethodsByRole.get(TransactionalRole.ROLLBACK)) {
							method.invoke(object, new InvocationData(object, method));
						}
					}

					@Override
					public void commit() {
						for (IMethodInfo method : transactionMethodsByRole.get(TransactionalRole.COMMIT)) {
							method.invoke(object, new InvocationData(object, method));
						}
					}

					@Override
					public void begin() {
						for (IMethodInfo method : transactionMethodsByRole.get(TransactionalRole.BEGIN)) {
							method.invoke(object, new InvocationData(object, method));
						}
					}
				};
			}
		}
		return super.getTransaction(type, object);
	}

	@Override
	protected String getOnlineHelp(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.getInfoCustomizations(), type.getName());
		if (t != null) {
			if (t.getOnlineHelp() != null) {
				return t.getOnlineHelp();
			}
		}
		return super.getOnlineHelp(type);
	}

	protected MembersCustomizationsFactory getMembers(ITypeInfo type) {
		return new MembersCustomizationsFactory(type);
	}

	/**
	 * Helper class that customizes information about the fields and methods of a
	 * specific type according to the related {@link TypeCustomization}.
	 * 
	 * The customization is split into multiple transformations handled by the
	 * sub-classes of {@link AbstractFieldTransformer} and
	 * {@link AbstractMethodTransformer}.
	 * 
	 * Note that the order in which these transformations are executed is important
	 * since they generate proxies that are stacked and impact one another.
	 * 
	 * @author olitank
	 *
	 */
	protected class MembersCustomizationsFactory {

		protected List<CapsuleFieldInfo> capsuleFields = new ArrayList<CapsuleFieldInfo>();
		protected List<IFieldInfo> inputFields = new ArrayList<IFieldInfo>();
		protected List<IMethodInfo> inputMethods = new ArrayList<IMethodInfo>();
		protected List<IMethodInfo> inputConstructors = new ArrayList<IMethodInfo>();
		protected List<IFieldInfo> outputFields = new ArrayList<IFieldInfo>();
		protected List<IMethodInfo> outputMethods = new ArrayList<IMethodInfo>();
		protected List<IMethodInfo> outputConstructors = new ArrayList<IMethodInfo>();
		protected MenuModel menuModel = new MenuModel();
		protected ITypeInfo containingType;
		protected TypeCustomization containingTypeCustomization;

		protected Map<Pair<MethodCustomization, ParameterCustomization>, ParameterAsFieldInfo> methodParameterAsFields = new HashMap<Pair<MethodCustomization, ParameterCustomization>, ParameterAsFieldInfo>();

		public MembersCustomizationsFactory(ITypeInfo containingType) {
			this.containingType = containingType;
			this.containingTypeCustomization = InfoCustomizations.getTypeCustomization(getInfoCustomizations(),
					containingType.getName());
			if (containingTypeCustomization != null) {
				inheritMembers(inputFields, inputMethods, inputConstructors, menuModel);
				addDeclaredMembers(inputFields, inputMethods, inputConstructors, menuModel);
				evolveMembers();
			} else {
				inheritMembers(outputFields, outputMethods, outputConstructors, menuModel);
			}
		}

		protected void addDeclaredMembers(List<IFieldInfo> inputFields, List<IMethodInfo> inputMethods,
				List<IMethodInfo> inputConstructors, MenuModel menuModel) {
			if (containingTypeCustomization.isAnyDefaultObjectMemberIncluded()) {
				addDefaultObjectMembers(inputFields, inputMethods, inputConstructors);
			}
			if (containingTypeCustomization.isAnyPersistenceMemberIncluded()) {
				addPersistenceMembers(inputFields, inputMethods, inputConstructors);
			}
			for (VirtualFieldDeclaration virtualFieldDeclaration : containingTypeCustomization
					.getVirtualFieldDeclarations()) {
				IFieldInfo newField = createVirtualField(virtualFieldDeclaration);
				newField = customizedUI.getInfoCustomizationsSetupFactory().wrapFieldInfo(newField, containingType);
				inputFields.add(newField);
			}
			menuModel.importContributions(
					ReflectionUIUtils.createMenuModel(containingTypeCustomization.getMenuModelCustomization()));
		}

		protected IFieldInfo createVirtualField(VirtualFieldDeclaration virtualFieldDeclaration) {
			try {
				ITypeInfo fieldType = virtualFieldDeclaration.getFieldTypeFinder().find(customizedUI,
						new SpecificitiesIdentifier(containingType.getName(), virtualFieldDeclaration.getFieldName()));
				return new VirtualFieldInfo(virtualFieldDeclaration.getFieldName(), fieldType);
			} catch (Throwable t) {
				throw new ReflectionUIError("Type '" + containingType.getName() + "': Failed to create virtual field '"
						+ virtualFieldDeclaration.getFieldName() + "': " + t.toString(), t);
			}
		}

		protected void inheritMembers(List<IFieldInfo> fields, List<IMethodInfo> methods,
				List<IMethodInfo> constructors, MenuModel menuModel) {
			fields.addAll(InfoCustomizationsFactory.super.getFields(containingType));
			methods.addAll(InfoCustomizationsFactory.super.getMethods(containingType));
			constructors.addAll(InfoCustomizationsFactory.super.getConstructors(containingType));
			menuModel.importContributions(InfoCustomizationsFactory.super.getMenuModel(containingType));
		}

		protected void addDefaultObjectMembers(List<IFieldInfo> fields, List<IMethodInfo> methods,
				List<IMethodInfo> constructors) {
			for (Method objectMethod : Object.class.getMethods()) {
				if (GetterFieldInfo.GETTER_PATTERN.matcher(objectMethod.getName()).matches()) {
					IFieldInfo newField = new GetterFieldInfo(customizedUI, objectMethod, Object.class);
					newField = customizedUI.getInfoCustomizationsSetupFactory().wrapFieldInfo(newField, containingType);
					fields.add(newField);
				} else {
					IMethodInfo newMethod = new DefaultMethodInfo(customizedUI, objectMethod);
					if (newMethod.getName().equals("toString")) {
						newMethod = new MethodInfoProxy(newMethod) {
							@Override
							public Object invoke(Object object, InvocationData invocationData) {
								return ReflectionUIUtils.toString(customizedUI, object);
							}
						};
					}
					newMethod = customizedUI.getInfoCustomizationsSetupFactory().wrapMethodInfo(newMethod,
							containingType);
					methods.add(newMethod);
				}
			}
		}

		protected void addPersistenceMembers(List<IFieldInfo> fields, List<IMethodInfo> methods,
				List<IMethodInfo> constructors) {
			methods.add(new SaveToFileMethod(customizedUI, containingType));
			methods.add(new LoadFromFileMethod(customizedUI, containingType));
		}

		protected void evolveMembers() {

			List<IFieldInfo> newFields = new ArrayList<IFieldInfo>();
			List<IMethodInfo> newMethods = new ArrayList<IMethodInfo>();
			List<IMethodInfo> newConstructors = new ArrayList<IMethodInfo>();

			List<CapsuleFieldInfo> newCapsuleFields = encapsulateMembers(inputFields, inputMethods);
			newCapsuleFields = mergeOrAddCapsuleFields(capsuleFields, newCapsuleFields);
			newFields.addAll(newCapsuleFields);

			try {
				transformFields(inputFields, outputFields, newFields, newMethods);
				transformMethods(inputMethods, outputMethods, newFields, newMethods);
				transformMethods(inputConstructors, outputConstructors, newFields, newConstructors);
			} catch (final Throwable t) {
				customizedUI.logError(t);
				outputFields.add(createErrorField(new ReflectionUIError(t)));
				return;
			}

			checkDuplicates(outputFields, outputMethods, outputConstructors);

			for (int i = 0; i < newFields.size(); i++) {
				newFields.set(i, customizedUI.getInfoCustomizationsSetupFactory().wrapFieldInfo(newFields.get(i),
						containingType));
			}
			for (int i = 0; i < newMethods.size(); i++) {
				newMethods.set(i, customizedUI.getInfoCustomizationsSetupFactory().wrapMethodInfo(newMethods.get(i),
						containingType));
			}
			for (int i = 0; i < newConstructors.size(); i++) {
				newConstructors.set(i, customizedUI.getInfoCustomizationsSetupFactory()
						.wrapMethodInfo(newConstructors.get(i), containingType));
			}

			inputFields = newFields;
			inputMethods = newMethods;
			inputConstructors = newConstructors;

			if ((inputFields.size() > 0) || (inputMethods.size() > 0) || (inputConstructors.size() > 0)) {
				evolveMembers();
			}
		}

		protected IFieldInfo createErrorField(final ReflectionUIError error) {
			return new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {

				@Override
				public Object getValue(Object object) {
					throw error;

				}

				@Override
				public String getName() {
					return MembersCustomizationsFactory.class.getName() + ".customizationError";
				}

				@Override
				public String getCaption() {
					return "";
				}

				@Override
				public ITypeInfo getType() {
					return customizedUI.buildTypeInfo(new JavaTypeInfoSource(customizedUI, Object.class,
							new SpecificitiesIdentifier(containingType.getName(), "customizationError")));
				}

			};
		}

		protected void transformFields(List<IFieldInfo> baseFields, List<IFieldInfo> modifiedFields,
				List<IFieldInfo> newFields, List<IMethodInfo> newMethods) {
			for (IFieldInfo field : new ArrayList<IFieldInfo>(baseFields)) {
				try {
					FieldCustomization f = InfoCustomizations.getFieldCustomization(containingTypeCustomization,
							field.getName());
					if (f != null) {
						for (AbstractFieldTransformer transformer : getFieldTransformers()) {
							field = transformer.process(field, f, newFields, newMethods);
						}
					}
				} catch (Throwable t) {
					throw new ReflectionUIError("Type '" + containingType.getName() + "': Field '" + field.getName()
							+ "' customization error: " + t.toString(), t);
				}
				modifiedFields.add(field);
			}
		}

		protected void transformMethods(List<IMethodInfo> baseMethods, List<IMethodInfo> modifiedMethods,
				List<IFieldInfo> newFields, List<IMethodInfo> newMethods) {
			for (IMethodInfo method : new ArrayList<IMethodInfo>(baseMethods)) {
				try {
					MethodCustomization mc = InfoCustomizations.getMethodCustomization(containingTypeCustomization,
							method.getSignature());
					if (mc != null) {
						for (AbstractMethodTransformer transformer : getMethodTransformers()) {
							method = transformer.process(method, mc, newFields, newMethods);
						}
					}
				} catch (Throwable t) {
					throw new ReflectionUIError("Type '" + containingType.getName() + "': Method '"
							+ method.getSignature() + "' customization error: " + t.toString(), t);
				}
				modifiedMethods.add(method);
			}
		}

		protected List<AbstractFieldTransformer> getFieldTransformers() {
			List<AbstractFieldTransformer> result = new ArrayList<AbstractFieldTransformer>();
			result.add(new FieldDuplicateGeneratingTransformer());
			result.add(new FieldGetterGeneratingTransformer());
			result.add(new FieldSetterGeneratingTransformer());
			result.add(new FieldNullStatusExportTransformer());
			result.add(new FieldNullStatusImportTransformer());
			result.add(new FieldCustomSetterTransformer());
			result.add(new FieldNullReplacementTransformer());
			result.add(new FieldTypeConversionTransformer());
			result.add(new FieldValueAsListTransformer());
			result.add(new FieldCommonOptionsTransformer());
			return result;
		}

		protected List<AbstractMethodTransformer> getMethodTransformers() {
			List<AbstractMethodTransformer> result = new ArrayList<AbstractMethodTransformer>();
			result.add(new MethodDuplicateGeneratingTransformer());
			result.add(new MethodHiddenParametersAndDefaultValuesTransformer());
			result.add(new MethodPresetsGeneratingTransformer());
			result.add(new MethodReturnValueFieldGeneratingTransformer());
			result.add(new MethodExportedParametersGeneratingTransformer());
			result.add(new MethodImportedParametersTransformer());
			result.add(new MethodParameterPropertiesTransformer());
			result.add(new MethodCommonOptionsTransformer());
			result.add(new MethodMenuItemGeneratingTransformer());
			return result;
		}

		protected List<CapsuleFieldInfo> encapsulateMembers(List<IFieldInfo> fields, List<IMethodInfo> methods) {
			Map<String, Pair<List<IFieldInfo>, List<IMethodInfo>>> encapsulatedMembersByCapsuleFieldName = new HashMap<String, Pair<List<IFieldInfo>, List<IMethodInfo>>>();
			for (IFieldInfo field : new ArrayList<IFieldInfo>(fields)) {
				if (!field.isHidden()) {
					FieldCustomization fc = InfoCustomizations.getFieldCustomization(containingTypeCustomization,
							field.getName());
					if (fc != null) {
						if (!fc.isHidden()) {
							if (fc.getEncapsulationFieldName() != null) {
								Pair<List<IFieldInfo>, List<IMethodInfo>> encapsulatedMembers = encapsulatedMembersByCapsuleFieldName
										.get(fc.getEncapsulationFieldName());
								if (encapsulatedMembers == null) {
									encapsulatedMembers = new Pair<List<IFieldInfo>, List<IMethodInfo>>(
											new ArrayList<IFieldInfo>(), new ArrayList<IMethodInfo>());
									encapsulatedMembersByCapsuleFieldName.put(fc.getEncapsulationFieldName(),
											encapsulatedMembers);
								}
								encapsulatedMembers.getFirst().add(field);
								MiscUtils.replaceItem(fields, field, new HiddenFieldInfoProxy(field));
							}
						}
					}
				}
			}

			for (IMethodInfo method : new ArrayList<IMethodInfo>(methods)) {
				MethodCustomization mc = InfoCustomizations.getMethodCustomization(containingTypeCustomization,
						method.getSignature());
				if (!method.isHidden()) {
					if (mc != null) {
						if (!mc.isHidden()) {
							if (mc.getEncapsulationFieldName() != null) {
								Pair<List<IFieldInfo>, List<IMethodInfo>> encapsulatedMembers = encapsulatedMembersByCapsuleFieldName
										.get(mc.getEncapsulationFieldName());
								if (encapsulatedMembers == null) {
									encapsulatedMembers = new Pair<List<IFieldInfo>, List<IMethodInfo>>(
											new ArrayList<IFieldInfo>(), new ArrayList<IMethodInfo>());
									encapsulatedMembersByCapsuleFieldName.put(mc.getEncapsulationFieldName(),
											encapsulatedMembers);
								}
								encapsulatedMembers.getSecond().add(method);
								MiscUtils.replaceItem(methods, method, new HiddenMethodInfoProxy(method));
							}
						}
					}
				}
			}
			if (encapsulatedMembersByCapsuleFieldName.size() == 0) {
				return Collections.emptyList();
			}
			List<CapsuleFieldInfo> result = new ArrayList<CapsuleFieldInfo>();
			for (String capsuleFieldName : encapsulatedMembersByCapsuleFieldName.keySet()) {
				Pair<List<IFieldInfo>, List<IMethodInfo>> encapsulatedMembers = encapsulatedMembersByCapsuleFieldName
						.get(capsuleFieldName);
				List<IFieldInfo> encapsulatedFields = encapsulatedMembers.getFirst();
				List<IMethodInfo> encapsulatedMethods = encapsulatedMembers.getSecond();
				CapsuleFieldInfo capsuleField = new CapsuleFieldInfo(customizedUI, capsuleFieldName, encapsulatedFields,
						encapsulatedMethods, containingType);
				result.add(capsuleField);
			}
			return result;
		}

		protected List<CapsuleFieldInfo> mergeOrAddCapsuleFields(List<CapsuleFieldInfo> capsuleFields,
				List<CapsuleFieldInfo> toMerge) {
			List<CapsuleFieldInfo> notMerged = new ArrayList<CapsuleFieldInfo>();
			for (CapsuleFieldInfo newField : toMerge) {
				boolean merged = false;
				for (CapsuleFieldInfo oldField : capsuleFields) {
					if (newField.getName().equals(oldField.getName())) {
						oldField.getEncapsulatedFields().addAll(newField.getEncapsulatedFields());
						oldField.getEncapsulatedMethods().addAll(newField.getEncapsulatedMethods());
						merged = true;
					}
				}
				if (!merged) {
					capsuleFields.add(newField);
					notMerged.add(newField);
				}
			}
			return notMerged;
		}

		protected void checkDuplicates(List<IFieldInfo> outputFields, List<IMethodInfo> outputMethods,
				List<IMethodInfo> outputConstructors) {
			for (int i = 0; i < outputFields.size(); i++) {
				for (int j = i + 1; j < outputFields.size(); j++) {
					IFieldInfo field1 = outputFields.get(i);
					IFieldInfo field2 = outputFields.get(j);
					if (field1.getName().equals(field2.getName())) {
						IFieldInfo errorField = new FieldInfoProxy(field2) {

							@Override
							public Object getValue(Object object) {
								throw new ReflectionUIError("Duplicate field name detected: '" + super.getName()
										+ "' in type '" + containingType.getName() + "'");

							}

							@Override
							public String getName() {
								return super.getName() + ".duplicateError";
							}

							@Override
							public ITypeInfo getType() {
								return customizedUI.buildTypeInfo(new JavaTypeInfoSource(customizedUI, Object.class,
										new SpecificitiesIdentifier(containingType.getName(), getName())));
							}

						};
						outputFields.set(outputFields.indexOf(field2), errorField);
					}
				}
			}
			for (int i = 0; i < outputMethods.size(); i++) {
				for (int j = i + 1; j < outputMethods.size(); j++) {
					IMethodInfo method1 = outputMethods.get(i);
					IMethodInfo method2 = outputMethods.get(j);
					if (method1.getSignature().equals(method2.getSignature())) {
						IMethodInfo errorMethod = new MethodInfoProxy(method2) {

							@Override
							public String getCaption() {
								return "(Duplicate) " + super.getCaption();
							}

							@Override
							public Object invoke(Object object, InvocationData invocationData) {
								throw new ReflectionUIError("Duplicate method name detected: '" + super.getName()
										+ "' in type '" + containingType.getName() + "'");
							}

							@Override
							public String getName() {
								return super.getName() + ".duplicateError";
							}

							@Override
							public String getSignature() {
								return ReflectionUIUtils.buildMethodSignature(this);
							}

						};
						outputMethods.set(outputMethods.indexOf(method2), errorMethod);
					}
				}
			}
			for (int i = 0; i < outputConstructors.size(); i++) {
				for (int j = i + 1; j < outputConstructors.size(); j++) {
					IMethodInfo constructor1 = outputConstructors.get(i);
					IMethodInfo constructor2 = outputConstructors.get(j);
					if (constructor1.getSignature().equals(constructor2.getSignature())) {
						IMethodInfo errorConstructor = new MethodInfoProxy(constructor2) {

							@Override
							public String getCaption() {
								return "(Duplicate) " + super.getCaption();
							}

							@Override
							public Object invoke(Object object, InvocationData invocationData) {
								throw new ReflectionUIError("Duplicate constructor name detected: '" + super.getName()
										+ "' in type '" + containingType.getName() + "'");
							}

							@Override
							public String getName() {
								return super.getName() + ".duplicateError";
							}

							@Override
							public String getSignature() {
								return ReflectionUIUtils.buildMethodSignature(this);
							}

						};
						outputConstructors.set(outputConstructors.indexOf(constructor2), errorConstructor);
					}
				}
			}
		}

		public List<IFieldInfo> getOutputFields() {
			return outputFields;
		}

		public List<IMethodInfo> getOutputMethods() {
			return outputMethods;
		}

		public List<IMethodInfo> getOutputConstructors() {
			return outputConstructors;
		}

		public MenuModel getMenuModel() {
			return menuModel;
		}

		protected class MethodParameterPropertiesTransformer extends AbstractMethodTransformer {

			@Override
			public IMethodInfo process(IMethodInfo method, final MethodCustomization mc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				method = new MethodInfoProxy(method) {

					@Override
					public List<IParameterInfo> getParameters() {
						List<IParameterInfo> result = new ArrayList<IParameterInfo>();
						for (IParameterInfo param : super.getParameters()) {
							final ParameterCustomization pc = InfoCustomizations.getParameterCustomization(mc,
									param.getName());
							if (pc != null) {
								param = new ParameterInfoProxy(param) {

									@Override
									public boolean isNullValueDistinct() {
										if (pc.isNullValueDistinctForced()) {
											return true;
										}
										return super.isNullValueDistinct();
									}

									@Override
									public Map<String, Object> getSpecificProperties() {
										Map<String, Object> result = new HashMap<String, Object>(
												super.getSpecificProperties());
										traceActiveCustomizations(result);
										result.putAll(pc.getSpecificProperties());
										return result;
									}

									@Override
									public String getCaption() {
										if (pc.getCustomParameterCaption() != null) {
											return pc.getCustomParameterCaption();
										}
										return super.getCaption();
									}

									@Override
									public String getOnlineHelp() {
										if (pc.getOnlineHelp() != null) {
											return pc.getOnlineHelp();
										}
										return super.getOnlineHelp();
									}

								};
							}
							result.add(param);
						}
						return result;
					}

				};
				return method;
			}

		}

		protected class MethodHiddenParametersAndDefaultValuesTransformer extends AbstractMethodTransformer {

			@Override
			public IMethodInfo process(IMethodInfo method, final MethodCustomization mc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				method = new MethodInfoProxy(method) {

					@Override
					public List<IParameterInfo> getParameters() {
						List<IParameterInfo> result = new ArrayList<IParameterInfo>();
						for (IParameterInfo param : super.getParameters()) {
							final ParameterCustomization pc = InfoCustomizations.getParameterCustomization(mc,
									param.getName());
							if (pc != null) {
								param = new ParameterInfoProxy(param) {

									@Override
									public boolean isHidden() {
										if (pc.isHidden() || pc.isDisplayedAsField()) {
											return true;
										}
										return super.isHidden();
									}

									@Override
									public Object getDefaultValue(Object object) {
										ParameterAsFieldInfo methodParameterAsField = MembersCustomizationsFactory.this.methodParameterAsFields
												.get(new Pair<MethodCustomization, ParameterCustomization>(mc, pc));
										if (methodParameterAsField != null) {
											if (methodParameterAsField.isInitialized(object)) {
												return methodParameterAsField.getValue(object);
											}
										}
										Object defaultValue = pc.getDefaultValue().load();
										if (defaultValue != null) {
											return defaultValue;
										}
										return super.getDefaultValue(object);
									}

								};
							}
							result.add(param);
						}
						return result;
					}

				};
				return method;
			}

		}

		protected class MethodCommonOptionsTransformer extends AbstractMethodTransformer {

			@Override
			public IMethodInfo process(IMethodInfo method, final MethodCustomization mc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				method = new MethodInfoProxy(method) {

					@Override
					public boolean isEnabled(Object object) {
						if (mc.getEnablementStatusFieldName() != null) {
							IFieldInfo field = ReflectionUIUtils.findInfoByName(outputFields,
									mc.getEnablementStatusFieldName());
							if (field == null) {
								throw new ReflectionUIError("Enablement status field not found: '"
										+ mc.getEnablementStatusFieldName() + "'");
							}
							Object enablementStatus = field.getValue(object);
							if (!(enablementStatus instanceof Boolean)) {
								throw new ReflectionUIError(
										"Invalid enablement status field value (boolean expected): '" + enablementStatus
												+ "'");
							}
							return (boolean) enablementStatus;
						}
						return super.isEnabled(object);
					}

					@Override
					public boolean isHidden() {
						if (mc.isHidden()) {
							return true;
						}
						return super.isHidden();
					}

					@Override
					public String getConfirmationMessage(Object object, InvocationData invocationData) {
						if (mc.getConfirmationMessage() != null) {
							return mc.getConfirmationMessage();
						}
						return super.getConfirmationMessage(object, invocationData);
					}

					@Override
					public String getParametersValidationCustomCaption() {
						if (mc.getParametersValidationCustomCaption() != null) {
							return mc.getParametersValidationCustomCaption();
						}
						return super.getParametersValidationCustomCaption();
					}

					@Override
					public boolean isReturnValueIgnored() {
						if (mc.isIgnoredReturnValueForced()) {
							return true;
						}
						return super.isReturnValueIgnored();
					}

					@Override
					public boolean isReturnValueDetached() {
						if (mc.isDetachedReturnValueForced()) {
							return true;
						}
						return super.isReturnValueDetached();
					}

					@Override
					public String getNullReturnValueLabel() {
						if (mc.getNullReturnValueLabel() != null) {
							return mc.getNullReturnValueLabel();
						}
						return super.getNullReturnValueLabel();
					}

					@Override
					public ValueReturnMode getValueReturnMode() {
						if (mc.getCustomValueReturnMode() != null) {
							return mc.getCustomValueReturnMode();
						}
						return super.getValueReturnMode();
					}

					@Override
					public ResourcePath getIconImagePath() {
						ResourcePath result = mc.getIconImagePath();
						if (result != null) {
							if (result.getSpecification().length() > 0) {
								return result;
							}
						}
						return super.getIconImagePath();
					}

					@Override
					public Map<String, Object> getSpecificProperties() {
						Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties());
						traceActiveCustomizations(result);
						result.putAll(mc.getSpecificProperties());
						return result;
					}

					@Override
					public boolean isReadOnly() {
						if (mc.isReadOnlyForced()) {
							return true;
						}
						return super.isReadOnly();
					}

					@Override
					public String getCaption() {
						if (mc.getCustomMethodCaption() != null) {
							return mc.getCustomMethodCaption();
						}
						return super.getCaption();
					}

					@Override
					public InfoCategory getCategory() {
						String categoryName = mc.getCategoryCaption();
						List<CustomizationCategory> categories = containingTypeCustomization.getMemberCategories();
						int categoryPosition = -1;
						int i = 0;
						for (CustomizationCategory c : categories) {
							if (c.getCaption().equals(categoryName)) {
								categoryPosition = i;
								break;
							}
							i++;
						}
						if (categoryPosition != -1) {
							CustomizationCategory category = categories.get(categoryPosition);
							return new InfoCategory(category.getCaption(), categoryPosition,
									(category.getIconImagePath() == null) ? null
											: new ResourcePath(category.getIconImagePath().getSpecification()));
						}
						return super.getCategory();
					}

					@Override
					public String getOnlineHelp() {
						if (mc.getOnlineHelp() != null) {
							return mc.getOnlineHelp();
						}
						return super.getOnlineHelp();
					}

				};
				return method;
			}

		}

		protected class MethodMenuItemGeneratingTransformer extends AbstractMethodTransformer {

			@Override
			public IMethodInfo process(IMethodInfo method, MethodCustomization mc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				if (mc.getMenuLocation() != null) {
					IMenuElementPosition menuItemContainerPosition = ReflectionUIUtils.getMenuElementPosition(
							containingTypeCustomization.getMenuModelCustomization(), mc.getMenuLocation());
					if (menuItemContainerPosition != null) {
						IMenuElementInfo actionMenuItem = new MethodActionMenuItemInfo(customizedUI,
								wrapMethodInfo(method, containingType));
						menuModel.importContribution(menuItemContainerPosition, actionMenuItem);
					}
				}
				return method;
			}

		}

		protected class MethodDuplicateGeneratingTransformer extends AbstractMethodTransformer {

			@Override
			public IMethodInfo process(IMethodInfo method, MethodCustomization mc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				if (mc.isDuplicateGenerated()) {
					newMethods.add(new MethodInfoProxy(method) {

						@Override
						public String getSignature() {
							return ReflectionUIUtils.buildMethodSignature(this);
						}

						@Override
						public String getName() {
							return super.getName() + ".duplicate";
						}

						@Override
						public String getCaption() {
							return ReflectionUIUtils.composeMessage(super.getCaption(), "Duplicate");
						}

						@Override
						public boolean isHidden() {
							return false;
						}
					});
				}
				return method;
			}

		}

		protected class MethodReturnValueFieldGeneratingTransformer extends AbstractMethodTransformer {

			@Override
			public IMethodInfo process(IMethodInfo method, MethodCustomization mc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				if (mc.isReturnValueFieldGenerated()) {
					newFields.add(new MethodReturnValueFieldInfo(customizedUI, method, containingType) {

						@Override
						public boolean isHidden() {
							return false;
						}
					});
				}
				return method;
			}

		}

		protected class MethodImportedParametersTransformer extends AbstractMethodTransformer {

			@Override
			public IMethodInfo process(IMethodInfo method, MethodCustomization mc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				if (mc.getParameterizedFieldNames().size() > 0) {
					List<IFieldInfo> parameterizedFields = new ArrayList<IFieldInfo>();
					for (final String fieldName : mc.getParameterizedFieldNames()) {
						IFieldInfo field = new DelegatingFieldInfo() {

							@Override
							protected IFieldInfo getDelegate() {
								IFieldInfo result = ReflectionUIUtils.findInfoByName(outputFields, fieldName);
								if (result == null) {
									return createErrorField(new ReflectionUIError(
											"Parameterized field not found: '" + fieldName + "'"));
								}
								return result;
							}

							@Override
							protected Object getDelegateId() {
								return fieldName;
							}
						};
						parameterizedFields.add(field);
					}
					method = new ParameterizedFieldsMethodInfo(customizedUI, method, parameterizedFields,
							containingType) {
						/*
						 * The method signature is not modified since it identifies this customization.
						 * Otherwise it will not be possible to disable the customization.
						 */
						@Override
						public String getSignature() {
							return base.getSignature();
						}
					};
				}
				return method;
			}

		}

		protected class MethodExportedParametersGeneratingTransformer extends AbstractMethodTransformer {

			@Override
			public IMethodInfo process(IMethodInfo method, MethodCustomization mc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				for (final IParameterInfo param : method.getParameters()) {
					final ParameterCustomization pc = InfoCustomizations.getParameterCustomization(mc, param.getName());
					if (pc != null) {
						if (pc.isDisplayedAsField()) {
							ParameterAsFieldInfo methodParameterAsField = new ParameterAsFieldInfo(customizedUI, method,
									param, containingType) {

								@Override
								public String getCaption() {
									return ReflectionUIUtils.composeMessage(method.getCaption(), param.getCaption());
								}

								@Override
								public boolean isHidden() {
									return false;
								}
							};
							MembersCustomizationsFactory.this.methodParameterAsFields.put(
									new Pair<MethodCustomization, ParameterCustomization>(mc, pc),
									methodParameterAsField);
							newFields.add(methodParameterAsField);
						}
					}
				}
				return method;
			}

		}

		protected class MethodPresetsGeneratingTransformer extends AbstractMethodTransformer {

			@Override
			public IMethodInfo process(IMethodInfo method, MethodCustomization mc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				for (int i = 0; i < mc.getSerializedInvocationDatas().size(); i++) {
					final TextualStorage invocationDataStorage = mc.getSerializedInvocationDatas().get(i);
					final int finalI = i;
					newMethods.add(new PresetInvocationDataMethodInfo(method, (TextualStorage) ReflectionUIUtils
							.copy(ReflectionUIUtils.STANDARD_REFLECTION, invocationDataStorage)) {

						@Override
						public String getName() {
							return buildPresetMethodName(base.getSignature(), finalI);
						}

						@Override
						public String getSignature() {
							return ReflectionUIUtils.buildMethodSignature(this);
						}

						@Override
						public String getCaption() {
							return ReflectionUIUtils.composeMessage(base.getCaption(), "Preset " + (finalI + 1));
						}

						@Override
						public boolean isHidden() {
							return false;
						}
					});
				}
				return method;
			}

		}

		protected class FieldCommonOptionsTransformer extends AbstractFieldTransformer {

			@Override
			public IFieldInfo process(IFieldInfo field, final FieldCustomization fc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				field = new FieldInfoProxy(field) {

					@Override
					public boolean isHidden() {
						if (fc.isHidden()) {
							return true;
						}
						return super.isHidden();
					}

					@Override
					public double getDisplayAreaHorizontalWeight() {
						if (fc.getDisplayAreaHorizontalWeight() != null) {
							return fc.getDisplayAreaHorizontalWeight();
						}
						return super.getDisplayAreaHorizontalWeight();
					}

					@Override
					public double getDisplayAreaVerticalWeight() {
						if (fc.getDisplayAreaVerticalWeight() != null) {
							return fc.getDisplayAreaVerticalWeight();
						}
						return super.getDisplayAreaVerticalWeight();
					}

					@Override
					public boolean isNullValueDistinct() {
						if (fc.isNullValueDistinctForced()) {
							return true;
						}
						return super.isNullValueDistinct();
					}

					@Override
					public boolean isGetOnly() {
						if (fc.isGetOnlyForced()) {
							return true;
						}
						return super.isGetOnly();
					}

					@Override
					public boolean isTransient() {
						if (fc.isTransientForced()) {
							return true;
						}
						return super.isTransient();
					}

					@Override
					public ValueReturnMode getValueReturnMode() {
						if (fc.getCustomValueReturnMode() != null) {
							return fc.getCustomValueReturnMode();
						}
						return super.getValueReturnMode();
					}

					@Override
					public String getNullValueLabel() {
						if (fc.getNullValueLabel() != null) {
							return fc.getNullValueLabel();
						}
						return super.getNullValueLabel();
					}

					@Override
					public boolean hasValueOptions(Object object) {
						if (fc.getValueOptionsFieldName() != null) {
							return true;
						}
						return super.hasValueOptions(object);
					}

					@Override
					public Object[] getValueOptions(Object object) {
						if (fc.getValueOptionsFieldName() != null) {
							IFieldInfo valueOptionsfield = ReflectionUIUtils.findInfoByName(outputFields,
									fc.getValueOptionsFieldName());
							if (valueOptionsfield == null) {
								throw new ReflectionUIError(
										"Value options field not found: '" + fc.getValueOptionsFieldName() + "'");
							}
							if (!(valueOptionsfield.getType() instanceof IListTypeInfo)) {
								throw new ReflectionUIError("Value options field '" + fc.getValueOptionsFieldName()
										+ "' type is not a list type: '" + valueOptionsfield.getType().getName() + "'");
							}
							IListTypeInfo valueOptionsfieldType = (IListTypeInfo) valueOptionsfield.getType();
							Object options = valueOptionsfield.getValue(object);
							if (options == null) {
								throw new ReflectionUIError("Value options field '" + fc.getValueOptionsFieldName()
										+ "': returned <null>!");
							}
							return valueOptionsfieldType.toArray(options);
						}
						return super.getValueOptions(object);
					}

					@Override
					public boolean isFormControlMandatory() {
						if (fc.isFormControlCreationForced()) {
							return true;
						}
						return super.isFormControlMandatory();
					}

					@Override
					public boolean isFormControlEmbedded() {
						if (fc.isFormControlEmbeddingForced()) {
							return true;
						}
						return super.isFormControlEmbedded();
					}

					@Override
					public long getAutoUpdatePeriodMilliseconds() {
						if (fc.getAutoUpdatePeriodMilliseconds() != null) {
							return fc.getAutoUpdatePeriodMilliseconds();
						}
						return super.getAutoUpdatePeriodMilliseconds();
					}

					@Override
					public Map<String, Object> getSpecificProperties() {
						Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties());
						traceActiveCustomizations(result);
						result.putAll(fc.getSpecificProperties());
						return result;
					}

					@Override
					public String getCaption() {
						if (fc.getCustomFieldCaption() != null) {
							return fc.getCustomFieldCaption();
						}
						return super.getCaption();
					}

					@Override
					public InfoCategory getCategory() {
						String categoryName = fc.getCategoryCaption();
						List<CustomizationCategory> categories = containingTypeCustomization.getMemberCategories();
						int categoryPosition = -1;
						int i = 0;
						for (CustomizationCategory c : categories) {
							if (c.getCaption().equals(categoryName)) {
								categoryPosition = i;
								break;
							}
							i++;
						}
						if (categoryPosition != -1) {
							CustomizationCategory category = categories.get(categoryPosition);
							return new InfoCategory(category.getCaption(), categoryPosition,
									(category.getIconImagePath() == null) ? null
											: new ResourcePath(category.getIconImagePath().getSpecification()));
						}
						return super.getCategory();
					}

					@Override
					public String getOnlineHelp() {
						if (fc.getOnlineHelp() != null) {
							return fc.getOnlineHelp();
						}
						return super.getOnlineHelp();
					}

				};
				return field;
			}

		}

		protected class FieldValueAsListTransformer extends AbstractFieldTransformer {

			@Override
			public IFieldInfo process(IFieldInfo field, FieldCustomization f, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				if (f.isDisplayedAsSingletonList()) {
					field = new ValueAsListFieldInfo(customizedUI, field, containingType);
				}
				return field;
			}

		}

		protected class FieldNullStatusExportTransformer extends AbstractFieldTransformer {

			@Override
			public IFieldInfo process(IFieldInfo field, FieldCustomization f, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				if (f.isNullStatusFieldExported()) {
					newFields.add(new ExportedNullStatusFieldInfo(customizedUI, field, containingType) {

						@Override
						public boolean isHidden() {
							return false;
						}
					});
				}
				return field;
			}

		}

		protected class FieldNullStatusImportTransformer extends AbstractFieldTransformer {

			@Override
			public IFieldInfo process(IFieldInfo field, final FieldCustomization f, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				if (f.getImportedNullStatusFieldName() != null) {
					IFieldInfo nullStatusField = new DelegatingFieldInfo() {
						@Override
						protected IFieldInfo getDelegate() {
							IFieldInfo result = ReflectionUIUtils.findInfoByName(outputFields,
									f.getImportedNullStatusFieldName());
							if (result == null) {
								return createErrorField(new ReflectionUIError(
										"Null status field not found: '" + f.getImportedNullStatusFieldName() + "'"));
							}
							return result;
						}

						@Override
						protected Object getDelegateId() {
							return f.getImportedNullStatusFieldName();
						}
					};
					field = new ImportedNullStatusFieldInfo(customizedUI, field, nullStatusField, containingType);
				}
				return field;
			}

		}

		protected class FieldSetterGeneratingTransformer extends AbstractFieldTransformer {

			@Override
			public IFieldInfo process(IFieldInfo field, FieldCustomization f, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				if (f.isSetterGenerated()) {
					newMethods.add(new FieldAsSetterInfo(customizedUI, field, containingType) {

						@Override
						public boolean isHidden() {
							return false;
						}
					});
				}
				return field;
			}

		}

		protected class FieldDuplicateGeneratingTransformer extends AbstractFieldTransformer {

			@Override
			public IFieldInfo process(IFieldInfo field, FieldCustomization fc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				if (fc.isDuplicateGenerated()) {
					IFieldInfo duplicateField = new FieldInfoProxy(field) {

						ITypeInfo type;

						@Override
						public String getName() {
							return super.getName() + ".duplicate";
						}

						@Override
						public String getCaption() {
							return ReflectionUIUtils.composeMessage(super.getCaption(), "Duplicate");
						}

						@Override
						public boolean isHidden() {
							return false;
						}

						@Override
						public ITypeInfo getType() {
							if (type == null) {
								type = customizedUI.buildTypeInfo(new TypeInfoSourceProxy(super.getType().getSource()) {
									@Override
									public SpecificitiesIdentifier getSpecificitiesIdentifier() {
										return new SpecificitiesIdentifier(containingType.getName(), getName());
									}

									@Override
									protected String getTypeInfoProxyFactoryIdentifier() {
										return "FieldValueTypeInfoProxyFactory [of=" + getClass().getName() + ", field="
												+ getName() + ", containingType=" + containingType.getName() + "]";
									}
								});
							}
							return type;
						}
					};
					newFields.add(duplicateField);
				}
				return field;
			}

		}

		protected class FieldCustomSetterTransformer extends AbstractFieldTransformer {

			@Override
			public IFieldInfo process(IFieldInfo field, final FieldCustomization f, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				if (f.getCustomSetterSignature() != null) {
					field = new FieldInfoProxy(field) {

						protected IMethodInfo getCustomSetter() {
							IMethodInfo result = ReflectionUIUtils.findMethodBySignature(outputMethods,
									f.getCustomSetterSignature());
							if (result == null) {
								throw new ReflectionUIError("Field '" + f.getFieldName()
										+ "': Custom setter not found: '" + f.getCustomSetterSignature() + "'");
							}
							return result;
						}

						@Override
						public boolean isGetOnly() {
							return false;
						}

						@Override
						public void setValue(Object object, Object value) {
							IMethodInfo customSetter = getCustomSetter();
							customSetter.invoke(object, new InvocationData(object, customSetter, value));
						}

						@Override
						public Runnable getNextUpdateCustomUndoJob(Object object, Object value) {
							IMethodInfo customSetter = getCustomSetter();
							return customSetter.getNextInvocationUndoJob(object,
									new InvocationData(object, customSetter, value));
						}

					};
				}
				return field;
			}

		}

		protected class FieldGetterGeneratingTransformer extends AbstractFieldTransformer {

			@Override
			public IFieldInfo process(IFieldInfo field, FieldCustomization f, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				if (f.isGetterGenerated()) {
					newMethods.add(new FieldAsGetterInfo(customizedUI, field, containingType) {

						@Override
						public boolean isHidden() {
							return false;
						}
					});
				}
				return field;
			}

		}

		protected class FieldTypeConversionTransformer extends AbstractFieldTransformer {

			@Override
			public IFieldInfo process(IFieldInfo field, FieldCustomization f, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				if (f.getTypeConversion() != null) {
					ITypeInfo newType = f.getTypeConversion().findNewType(customizedUI,
							new SpecificitiesIdentifier(containingType.getName(), field.getName()));
					Filter<Object> conversionMethod = f.getTypeConversion().buildOverallConversionMethod();
					Filter<Object> reverseConversionMethod = f.getTypeConversion()
							.buildOverallReverseConversionMethod();
					boolean nullValueConverted = f.getTypeConversion().isNullValueConverted();
					field = new ChangedTypeFieldInfo(field, newType, conversionMethod, reverseConversionMethod,
							nullValueConverted);
				}
				return field;
			}

		}

		/**
		 * Base class for factories that generate proxies or new members from field
		 * information according to the specified {@link FieldCustomization}.
		 * 
		 * @author olitank
		 *
		 */
		protected class FieldNullReplacementTransformer extends AbstractFieldTransformer {

			@Override
			public IFieldInfo process(IFieldInfo field, FieldCustomization fc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				if (fc.getNullReplacement().getData() != null) {
					field = new NullReplacementFieldInfo(field, (TextualStorage) ReflectionUIUtils
							.copy(ReflectionUIUtils.STANDARD_REFLECTION, fc.getNullReplacement()));
				}
				return field;
			}

		}

		/**
		 * Base class for factories that generate proxies or new members from field
		 * information according to the specified {@link FieldCustomization}.
		 * 
		 * @author olitank
		 *
		 */
		protected abstract class AbstractFieldTransformer {

			/**
			 * @param field      The field that needs to be transformed.
			 * @param fc         The transformation settings.
			 * @param newFields  The new fields generated during the transformation. Note
			 *                   that they must have unique names compared to the other
			 *                   fields of the same type.
			 * @param newMethods The new methods generated during the transformation. Note
			 *                   that they must have unique signatures compared to the other
			 *                   methods of the same type.
			 * @return the input field or a proxy to this input field. Note that in case of
			 *         a proxy the name of the field must not change.
			 */
			public abstract IFieldInfo process(IFieldInfo field, FieldCustomization fc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods);

		}

		/**
		 * Base class for factories that generate proxies or new members from method
		 * information according to the specified {@link MethodCustomization}.
		 * 
		 * @author olitank
		 *
		 */
		protected abstract class AbstractMethodTransformer {

			/**
			 * @param method     The method that needs to be transformed.
			 * @param mc         The transformation settings.
			 * @param newFields  The new fields generated during the transformation. Note
			 *                   that they must have unique names compared to the other
			 *                   fields of the same type.
			 * @param newMethods The new methods generated during the transformation. Note
			 *                   that they must have unique signatures compared to the other
			 *                   methods of the same type.
			 * @return the input method or a proxy to this input method. Note that in case
			 *         of a proxy the signature of the method must not change.
			 */
			public abstract IMethodInfo process(IMethodInfo method, MethodCustomization mc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods);

		}

	}

	protected class ChangedTypeNameFactory extends InfoProxyFactory {

		protected String sourceTypeName;
		protected String targetTypeName;

		public ChangedTypeNameFactory(String sourceTypeName, String targetTypeName) {
			this.sourceTypeName = sourceTypeName;
			this.targetTypeName = targetTypeName;
		}

		@Override
		public String getIdentifier() {
			return "ChangedTypeNameFactory [sourceTypeName=" + sourceTypeName + ", targetTypeName=" + targetTypeName
					+ ", parent=" + InfoCustomizationsFactory.this.getIdentifier() + "]";
		}

		@Override
		protected String getName(ITypeInfo type) {
			if (type.getName().equals(sourceTypeName)) {
				return targetTypeName;
			}
			return type.getName();
		}

	}

}
