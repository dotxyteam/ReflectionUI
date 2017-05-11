package xy.reflect.ui.info.type.factory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.custom.InfoCustomizations.CustomizationCategory;
import xy.reflect.ui.info.custom.InfoCustomizations.EnumerationCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.EnumerationItemCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.FieldCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ITypeInfoFinder;
import xy.reflect.ui.info.custom.InfoCustomizations.ListCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ListItemFieldShortcut;
import xy.reflect.ui.info.custom.InfoCustomizations.ListItemMethodShortcut;
import xy.reflect.ui.info.custom.InfoCustomizations.MethodCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.ParameterCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.TextualStorage;
import xy.reflect.ui.info.custom.InfoCustomizations.TypeConversion;
import xy.reflect.ui.info.custom.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.info.custom.InfoCustomizations.VirtualFieldDeclaration;
import xy.reflect.ui.info.field.AllMethodParametersAsFieldInfo;
import xy.reflect.ui.info.field.CapsuleFieldInfo;
import xy.reflect.ui.info.field.ChangedTypeFieldInfo;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.GetterFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MethodAsFieldInfo;
import xy.reflect.ui.info.field.MethodParameterAsFieldInfo;
import xy.reflect.ui.info.field.NerverNullFieldInfo;
import xy.reflect.ui.info.field.NullStatusFieldInfo;
import xy.reflect.ui.info.field.SubFieldInfo;
import xy.reflect.ui.info.field.ValueAsListFieldInfo;
import xy.reflect.ui.info.field.VirtualFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.DefaultMenuElementPosition;
import xy.reflect.ui.info.menu.IMenuItemContainer;
import xy.reflect.ui.info.menu.MenuElementKind;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.menu.MethodActionMenuItem;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.FieldAsGetterInfo;
import xy.reflect.ui.info.method.FieldAsSetterInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.method.PresetInvocationDataMethodInfo;
import xy.reflect.ui.info.method.SubMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.parameter.ParameterInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.FieldsLayout;
import xy.reflect.ui.info.type.enumeration.EnumerationItemInfoProxy;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.factory.MethodInvocationDataAsObjectFactory.Instance;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.structure.CustomizedStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.AbstractListProperty;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.util.Mapper;
import xy.reflect.ui.util.Pair;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

@SuppressWarnings("unused")
public class InfoCustomizationsFactory extends TypeInfoProxyFactory {

	protected ReflectionUI reflectionUI;
	protected final InfoCustomizations infoCustomizations;

	public InfoCustomizationsFactory(ReflectionUI reflectionUI, InfoCustomizations infoCustomizations) {
		this.reflectionUI = reflectionUI;
		this.infoCustomizations = infoCustomizations;
	}

	public InfoCustomizations getInfoCustomizations() {
		return infoCustomizations;
	}

	@Override
	protected FieldsLayout getFieldsLayout(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations, type.getName());
		if (t != null) {
			if (t.getFieldsLayout() != null) {
				return t.getFieldsLayout();
			}
		}
		return super.getFieldsLayout(type);
	}

	@Override
	public String getIdentifier() {
		return this.infoCustomizations.toString();
	}

	@Override
	protected boolean isItemConstructorSelectable(IListTypeInfo listType) {
		ITypeInfo itemType = listType.getItemType();
		final ListCustomization l = InfoCustomizations.getListCustomization(this.infoCustomizations, listType.getName(),
				(itemType == null) ? null : itemType.getName());
		if (l != null) {
			if (l.isItemContructorSelectableforced()) {
				return true;
			}
		}
		return super.isItemConstructorSelectable(listType);
	}

	@Override
	protected boolean isConcrete(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations, type.getName());
		if (t != null) {
			if (t.isAbstractForced()) {
				return false;
			}
		}
		return super.isConcrete(type);
	}

	@Override
	protected boolean isImmutable(ITypeInfo type) {
		final TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations, type.getName());
		if (t != null) {
			if (t.isImmutableForced()) {
				return true;
			}
		}
		return super.isImmutable(type);
	}

	@Override
	protected Object[] toArray(IListTypeInfo listType, Object listValue) {
		ITypeInfo itemType = listType.getItemType();
		final ListCustomization l = InfoCustomizations.getListCustomization(this.infoCustomizations, listType.getName(),
				(itemType == null) ? null : itemType.getName());
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
	protected Object[] getPossibleValues(IEnumerationTypeInfo enumType) {
		EnumerationCustomization e = InfoCustomizations.getEnumerationCustomization(this.infoCustomizations,
				enumType.getName());
		if (e != null) {
			List<Object> result = new ArrayList<Object>();
			for (Object value : super.getPossibleValues(enumType)) {
				IEnumerationItemInfo valueInfo = getValueInfo(enumType, value);
				EnumerationItemCustomization i = InfoCustomizations.getEnumerationItemCustomization(e,
						valueInfo.getName());
				if (i != null) {
					if (i.isHidden()) {
						continue;
					}
				}
				result.add(value);
			}
			return result.toArray();
		}
		return super.getPossibleValues(enumType);
	}

	@Override
	protected boolean isDynamicEnumeration(IEnumerationTypeInfo enumType) {
		EnumerationCustomization e = InfoCustomizations.getEnumerationCustomization(this.infoCustomizations,
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
		EnumerationCustomization e = InfoCustomizations.getEnumerationCustomization(this.infoCustomizations,
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

				};
			}
		}
		return result;
	}

	@Override
	protected List<AbstractListProperty> getDynamicProperties(IListTypeInfo listType,
			ItemPosition anyRootListItemPosition, List<? extends ItemPosition> selection) {
		ITypeInfo itemType = listType.getItemType();
		final ListCustomization l = InfoCustomizations.getListCustomization(this.infoCustomizations, listType.getName(),
				(itemType == null) ? null : itemType.getName());
		if (l != null) {
			List<AbstractListProperty> result = super.getDynamicProperties(listType, anyRootListItemPosition,
					selection);
			result = new ArrayList<AbstractListProperty>(result);
			for (final ListItemFieldShortcut shortcut : l.getAllowedItemFieldShortcuts()) {
				final String fieldCaption;
				if (shortcut.getCustomFieldCaption() != null) {
					fieldCaption = shortcut.getCustomFieldCaption();
				} else {
					fieldCaption = ReflectionUIUtils.identifierToCaption(shortcut.getFieldName());
				}
				boolean fieldFound = false;
				if (selection.size() == 1) {
					final ItemPosition itemPosition = selection.get(0);
					final Object item = itemPosition.getItem();
					if (item != null) {
						ITypeInfo actualItemType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(item));
						for (final IFieldInfo itemField : actualItemType.getFields()) {
							if (itemField.getName().equals(shortcut.getFieldName())) {
								AbstractListProperty property = new AbstractListProperty() {

									AbstractListProperty thisProperty = this;
									IFieldInfo itemPositionAsField = new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {

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
											new ListModificationFactory(itemPosition, thisProperty)
													.set(itemPosition.getIndex(), item);
										}

										@Override
										public boolean isGetOnly() {
											return !new ListModificationFactory(itemPosition, this)
													.canSet(itemPosition.getIndex());
										}

									};
									SubFieldInfo delegate = new SubFieldInfo(itemPositionAsField, itemField);

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
										return fieldCaption;
									}

									public ITypeInfo getType() {
										return delegate.getType();
									}

									public ITypeInfoProxyFactory getTypeSpecificities() {
										return delegate.getTypeSpecificities();
									}

									public Object getValue(Object object) {
										return delegate.getValue(object);
									}

									public Object[] getValueOptions(Object object) {
										return delegate.getValueOptions(object);
									}

									public boolean isGetOnly() {
										return delegate.isGetOnly();
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
							return fieldCaption;
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

						@Override
						public ITypeInfoProxyFactory getTypeSpecificities() {
							return null;
						}

					};
					result.add(property);

				}
			}
			return result;
		}
		return super.getDynamicProperties(listType, anyRootListItemPosition, selection);
	}

	@Override
	protected List<AbstractListAction> getDynamicActions(IListTypeInfo listType, ItemPosition anyRootListItemPosition,
			List<? extends ItemPosition> selection) {
		ITypeInfo itemType = listType.getItemType();
		final ListCustomization l = InfoCustomizations.getListCustomization(this.infoCustomizations, listType.getName(),
				(itemType == null) ? null : itemType.getName());
		if (l != null) {
			List<AbstractListAction> result = super.getDynamicActions(listType, anyRootListItemPosition, selection);
			result = new ArrayList<AbstractListAction>(result);

			for (final ListItemMethodShortcut shortcut : l.getAllowedItemMethodShortcuts()) {
				final String methodName = ReflectionUIUtils
						.extractMethodNameFromSignature(shortcut.getMethodSignature());
				final String methodCaption;
				if (shortcut.getCustomMethodCaption() != null) {
					methodCaption = shortcut.getCustomMethodCaption();
				} else {
					methodCaption = ReflectionUIUtils.identifierToCaption(methodName);
				}
				boolean methodFound = false;
				if (selection.size() == 1) {
					final ItemPosition itemPosition = selection.get(0);
					final Object item = itemPosition.getItem();
					if (item != null) {
						ITypeInfo actualItemType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(item));
						for (final IMethodInfo itemMethod : actualItemType.getMethods()) {
							if (itemMethod.getSignature().equals(shortcut.getMethodSignature())) {
								AbstractListAction action = new AbstractListAction() {

									AbstractListAction thisAction = this;
									IFieldInfo itemPositionAsField = new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {

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
											new ListModificationFactory(itemPosition, thisAction)
													.set(itemPosition.getIndex(), item);
										}

										@Override
										public boolean isGetOnly() {
											return !new ListModificationFactory(itemPosition, this)
													.canSet(itemPosition.getIndex());
										}

									};
									SubMethodInfo delegate = new SubMethodInfo(itemPositionAsField, itemMethod);

									@Override
									public String getName() {
										return methodName;
									}

									@Override
									public String getCaption() {
										return methodCaption;
									}

									@Override
									public boolean isEnabled() {
										return true;
									}

									public ITypeInfo getReturnValueType() {
										return delegate.getReturnValueType();
									}

									public ITypeInfoProxyFactory getReturnValueTypeSpecificities() {
										return delegate.getReturnValueTypeSpecificities();
									}

									public Object invoke(Object object, InvocationData invocationData) {
										return delegate.invoke(object, invocationData);
									}

									public boolean isReadOnly() {
										return delegate.isReadOnly();
									}

									public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
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

									public ResourcePath getIconImagePath() {
										return delegate.getIconImagePath();
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
							return methodCaption;
						}

						@Override
						public boolean isEnabled() {
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
		return super.getDynamicActions(listType, anyRootListItemPosition, selection);
	}

	@Override
	protected IListItemDetailsAccessMode getDetailsAccessMode(IListTypeInfo listType) {
		ITypeInfo itemType = listType.getItemType();
		final ListCustomization l = InfoCustomizations.getListCustomization(this.infoCustomizations, listType.getName(),
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
		final ListCustomization l = InfoCustomizations.getListCustomization(this.infoCustomizations, listType.getName(),
				(itemType == null) ? null : itemType.getName());
		if (l != null) {
			if (l.getEditOptions() != null) {
				if (l.getEditOptions().getListInstanciationOption() != null) {
					Object newListInstance;
					if (l.getEditOptions().getListInstanciationOption().getCustomInstanceTypeFinder() != null) {
						ITypeInfo customInstanceType = l.getEditOptions().getListInstanciationOption()
								.getCustomInstanceTypeFinder().find(reflectionUI);
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
		final ListCustomization l = InfoCustomizations.getListCustomization(this.infoCustomizations, listType.getName(),
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
		final ListCustomization l = InfoCustomizations.getListCustomization(this.infoCustomizations, listType.getName(),
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
		final ListCustomization l = InfoCustomizations.getListCustomization(this.infoCustomizations, listType.getName(),
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
		final ListCustomization l = InfoCustomizations.getListCustomization(this.infoCustomizations, listType.getName(),
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
		final ListCustomization l = InfoCustomizations.getListCustomization(this.infoCustomizations, listType.getName(),
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
		final ListCustomization l = InfoCustomizations.getListCustomization(this.infoCustomizations, listType.getName(),
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
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.infoCustomizations, type.getName());
		if (tc != null) {
			if (tc.isUndoManagementHidden()) {
				return false;
			}
		}
		return super.isModificationStackAccessible(type);
	}

	@Override
	protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.infoCustomizations, type.getName());
		if (tc != null) {
			if (tc.getPolymorphicSubTypeFinders() != null) {
				List<ITypeInfo> result = new ArrayList<ITypeInfo>(super.getPolymorphicInstanceSubTypes(type));
				for (ITypeInfoFinder finder : tc.getPolymorphicSubTypeFinders()) {
					ITypeInfo subType = finder.find(reflectionUI);
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
		final ListCustomization l = InfoCustomizations.getListCustomization(this.infoCustomizations, listType.getName(),
				(itemType == null) ? null : itemType.getName());
		if (l != null) {
			final IListStructuralInfo base = super.getStructuralInfo(listType);
			return new CustomizedStructuralInfo(reflectionUI, base, listType, l);
		}
		return super.getStructuralInfo(listType);
	}

	@Override
	protected ResourcePath getIconImagePath(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations, type.getName());
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
		result.put(InfoCustomizations.CURRENT_PROXY_SOURCE_PROPERTY_KEY, this.infoCustomizations);
		final TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations, type.getName());
		if (t != null) {
			if (t.getSpecificProperties() != null) {
				if (t.getSpecificProperties().entrySet().size() > 0) {
					result.putAll(t.getSpecificProperties());
				}
			}
		}
		return result;
	}

	@Override
	protected List<IMethodInfo> getConstructors(ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getConstructors(containingType));
			for (Iterator<IMethodInfo> it = result.iterator(); it.hasNext();) {
				IMethodInfo ctor = it.next();
				MethodCustomization m = InfoCustomizations.getMethodCustomization(t, ctor.getSignature());
				if (m != null) {
					if (m.isHidden()) {
						it.remove();
					}
				}
			}
			if (t.getCustomMethodsOrder() != null) {
				Collections.sort(result, ReflectionUIUtils.getInfosComparator(t.getCustomMethodsOrder(), result));
			}
			return result;
		}
		return super.getConstructors(containingType);
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
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
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
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
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
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations, type.getName());
		if (t != null) {
			if (t.getCustomTypeCaption() != null) {
				return t.getCustomTypeCaption();
			}
		}
		return super.getCaption(type);
	}

	@Override
	protected void validate(ITypeInfo type, Object object) throws Exception {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations, type.getName());
		if (t != null) {
			for (MethodCustomization m : t.getMethodsCustomizations()) {
				if (m.isValidating()) {
					IMethodInfo method = ReflectionUIUtils.findMethodBySignature(type.getMethods(),
							m.getMethodSignature());
					if (method != null) {
						if (method.getParameters().size() > 0) {
							throw new ReflectionUIError(
									"Invalid validating method: Number of parameters > 0: " + method.getSignature());
						}
						method.invoke(object, new InvocationData());
					}
				}
			}
		}
		super.validate(type, object);
	}

	@Override
	protected String getOnlineHelp(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations, type.getName());
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

	protected class MembersCustomizationsFactory {

		protected List<IFieldInfo> inputFields = new ArrayList<IFieldInfo>();
		protected List<IMethodInfo> inputMethods = new ArrayList<IMethodInfo>();
		protected List<IFieldInfo> outputFields = new ArrayList<IFieldInfo>();
		protected List<IMethodInfo> outputMethods = new ArrayList<IMethodInfo>();
		protected MenuModel menuModel = new MenuModel();
		protected ITypeInfo containingType;
		protected TypeCustomization containingTypeCustomization;

		public MembersCustomizationsFactory(ITypeInfo containingType) {
			this.containingType = containingType;
			this.containingTypeCustomization = InfoCustomizations.getTypeCustomization(infoCustomizations,
					containingType.getName());
			if (containingTypeCustomization != null) {
				inheritMembers(inputFields, inputMethods, menuModel);
				addDeclaredMembers(inputFields, inputMethods, menuModel);
				evolveMembers();
				outputFields = removeHiddenFields(outputFields);
				outputMethods = removeHiddenMethods(outputMethods);
			} else {
				inheritMembers(outputFields, outputMethods, menuModel);
			}
		}

		protected void addDeclaredMembers(List<IFieldInfo> inputFields, List<IMethodInfo> inputMethods,
				MenuModel menuModel) {
			if (containingTypeCustomization.isAnyDefaultObjectMemberIncluded()) {
				addDefaultObjectMembers(inputFields, inputMethods);
			}
			for (VirtualFieldDeclaration virtualFieldDeclaration : containingTypeCustomization
					.getVirtualFieldDeclarations()) {
				inputFields.add(createVirtualField(virtualFieldDeclaration));
			}
			menuModel.importContributions(containingTypeCustomization.getMenuModel());
		}

		protected IFieldInfo createVirtualField(VirtualFieldDeclaration virtualFieldDeclaration) {
			try {
				ITypeInfo fieldType = virtualFieldDeclaration.getFieldTypeFinder().find(reflectionUI);
				return new VirtualFieldInfo(virtualFieldDeclaration.getFieldName(), fieldType);
			} catch (Throwable t) {
				throw new ReflectionUIError("Type '" + containingType.getName() + "': Failed to create virtual field '"
						+ virtualFieldDeclaration.getFieldName() + "': " + t.toString(), t);
			}
		}

		protected void inheritMembers(List<IFieldInfo> fields, List<IMethodInfo> methods, MenuModel menuModel) {
			fields.addAll(InfoCustomizationsFactory.super.getFields(containingType));
			methods.addAll(InfoCustomizationsFactory.super.getMethods(containingType));
			menuModel.importContributions(InfoCustomizationsFactory.super.getMenuModel(containingType));
		}

		protected void addDefaultObjectMembers(List<IFieldInfo> fields, List<IMethodInfo> methods) {
			for (Method objectMethod : Object.class.getMethods()) {
				if (GetterFieldInfo.GETTER_PATTERN.matcher(objectMethod.getName()).matches()) {
					fields.add(new GetterFieldInfo(reflectionUI, objectMethod, Object.class));
				} else {
					methods.add(new DefaultMethodInfo(reflectionUI, objectMethod));
				}
			}
		}

		protected void evolveMembers() {

			List<IFieldInfo> newFields = new ArrayList<IFieldInfo>();
			List<IMethodInfo> newMetods = new ArrayList<IMethodInfo>();

			encapsulateMembers(inputFields, inputMethods, newFields);
			transformFields(inputFields, outputFields, newFields, newMetods);
			transformMethods(inputMethods, outputMethods, newFields, newMetods);

			checkDuplicates(outputFields, outputMethods);

			inputFields = newFields;
			inputMethods = newMetods;

			if ((inputFields.size() > 0) || (inputMethods.size() > 0)) {
				evolveMembers();
			}
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
			result.add(new FieldTypeConversionTransformer());
			result.add(new FieldCommonOptionsFieldTransformer());
			result.add(new FieldNullReplacementTransformer());
			result.add(new FieldValueAsListTransformer());
			result.add(new FieldCustomSetterTransformer());
			result.add(new FieldNullStatusGeneratingTransformer());
			result.add(new FieldGetterGeneratingTransformer());
			result.add(new FieldSetterGeneratingTransformer());
			result.add(new FieldDuplicateGeneratingTransformer());
			return result;
		}

		protected List<AbstractMethodTransformer> getMethodTransformers() {
			List<AbstractMethodTransformer> result = new ArrayList<AbstractMethodTransformer>();
			result.add(new MethodCommonOptionsTransformer());
			result.add(new MethodParameterDefaultValueSettingTransformer());
			result.add(new MethodParametersAsSubFormTransformer());
			result.add(new MethodParameterAsFieldTransformer());
			result.add(new MethodReturnValueFieldGeneratingTransformer());
			result.add(new MethodPresetsGeneratingTransformer());
			result.add(new MethodMenuItemGeneratingTransformer());
			result.add(new MethodDuplicateGeneratingTransformer());
			return result;
		}

		protected void encapsulateMembers(List<IFieldInfo> fields, List<IMethodInfo> methods,
				List<IFieldInfo> newFields) {
			Map<String, Pair<List<IFieldInfo>, List<IMethodInfo>>> encapsulatedMembersByCapsuleFieldName = new HashMap<String, Pair<List<IFieldInfo>, List<IMethodInfo>>>();
			for (IFieldInfo field : new ArrayList<IFieldInfo>(fields)) {
				FieldCustomization fc = InfoCustomizations.getFieldCustomization(containingTypeCustomization,
						field.getName());
				if (fc != null) {
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
						fields.remove(field);
					}
				}
			}

			for (IMethodInfo method : new ArrayList<IMethodInfo>(methods)) {
				MethodCustomization mc = InfoCustomizations.getMethodCustomization(containingTypeCustomization,
						method.getSignature());
				if (mc != null) {
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
						methods.remove(method);
					}
				}
			}

			if (encapsulatedMembersByCapsuleFieldName.size() == 0) {
				return;
			}
			for (String capsuleFieldName : encapsulatedMembersByCapsuleFieldName.keySet()) {
				Pair<List<IFieldInfo>, List<IMethodInfo>> encapsulatedMembers = encapsulatedMembersByCapsuleFieldName
						.get(capsuleFieldName);
				List<IFieldInfo> encapsulatedFields = encapsulatedMembers.getFirst();
				List<IMethodInfo> encapsulatedMethods = encapsulatedMembers.getSecond();
				IFieldInfo duplicateField = ReflectionUIUtils.findInfoByName(fields, capsuleFieldName);
				String contextId = "EncapsulationContext [containingType=" + containingType.getName() + "]";
				if (duplicateField != null) {
					CapsuleFieldInfo duplicateFieldAsTranslatedCapsuleField = CapsuleFieldInfo
							.translateProxy(duplicateField);
					if (duplicateFieldAsTranslatedCapsuleField != null) {
						fields.remove(duplicateField);
						encapsulatedFields.addAll(0, duplicateFieldAsTranslatedCapsuleField.getEncapsulatedFields());
						encapsulatedMethods.addAll(0, duplicateFieldAsTranslatedCapsuleField.getEncapsulatedMethods());
						contextId = duplicateFieldAsTranslatedCapsuleField.getContextId();
					} else {
						throw new ReflectionUIError("Failed to generate capsule field: Duplicate field name detected: '"
								+ capsuleFieldName + "'");
					}
				}
				CapsuleFieldInfo capsuleField = new CapsuleFieldInfo(reflectionUI, capsuleFieldName, encapsulatedFields,
						encapsulatedMethods, contextId);
				initializeEncapsulatedMemberCustomizations(capsuleField, containingType);
				newFields.add(capsuleField);
			}
		}

		protected void initializeEncapsulatedMemberCustomizations(CapsuleFieldInfo capsuleField,
				ITypeInfo containingType) {
			ITypeInfo capsuleFieldType = capsuleField.getType();
			TypeCustomization capsuleTc = InfoCustomizations.getTypeCustomization(infoCustomizations,
					capsuleFieldType.getName(), true);
			for (IFieldInfo field : capsuleFieldType.getFields()) {
				FieldCustomization fc = InfoCustomizations.getFieldCustomization(capsuleTc, field.getName(), true);
				if (fc.isInitial()) {
					FieldCustomization baseFc = InfoCustomizations.getFieldCustomization(containingTypeCustomization,
							field.getName(), true);
					if (!baseFc.isInitial()) {
						fc.setCustomFieldCaption(baseFc.getCustomFieldCaption());
						fc.setCustomValueReturnMode(baseFc.getCustomValueReturnMode());
						fc.setDisplayedAsSingletonList(baseFc.isDisplayedAsSingletonList());
						fc.setFormControlCreationForced(baseFc.isFormControlCreationForced());
						fc.setFormControlEmbeddingForced(baseFc.isFormControlEmbeddingForced());
						fc.setGetOnlyForced(baseFc.isGetOnlyForced());
						fc.setNullValueDistinctForced(baseFc.isNullValueDistinctForced());
						fc.setNullValueLabel(baseFc.getNullValueLabel());
						fc.setOnlineHelp(baseFc.getOnlineHelp());
						fc.setTypeConversion((TypeConversion) ReflectionUIUtils
								.copyThroughSerialization(baseFc.getTypeConversion()));

					}
				}
			}
			for (IMethodInfo method : capsuleFieldType.getMethods()) {
				MethodCustomization mc = InfoCustomizations.getMethodCustomization(capsuleTc, method.getSignature(),
						true);
				if (mc.isInitial()) {
					MethodCustomization baseMc = InfoCustomizations.getMethodCustomization(containingTypeCustomization,
							method.getSignature(), true);
					if (!baseMc.isInitial()) {
						mc.setCustomMethodCaption(baseMc.getCustomMethodCaption());
						mc.setCustomValueReturnMode(baseMc.getCustomValueReturnMode());
						mc.setDetachedReturnValueForced(baseMc.isDetachedReturnValueForced());
						mc.setIconImagePath(baseMc.getIconImagePath());
						mc.setNullReturnValueLabel(baseMc.getNullReturnValueLabel());
						mc.setOnlineHelp(baseMc.getOnlineHelp());
						mc.setReadOnlyForced(baseMc.isReadOnlyForced());
						mc.setIgnoredReturnValueForced(baseMc.isIgnoredReturnValueForced());
					}
				}
			}
		}

		protected void checkDuplicates(List<IFieldInfo> outputFields, List<IMethodInfo> outputMethods) {
			for (int i = 0; i < outputFields.size(); i++) {
				for (int j = i + 1; j < outputFields.size(); j++) {
					IFieldInfo field1 = outputFields.get(i);
					IFieldInfo field2 = outputFields.get(j);
					if (field1.getName().equals(field2.getName())) {
						throw new ReflectionUIError("Duplicate field '" + field1.getName() + "' detected in type '"
								+ containingType.getName() + "'");
					}
				}
			}
			for (int i = 0; i < outputMethods.size(); i++) {
				for (int j = i + 1; j < outputMethods.size(); j++) {
					IMethodInfo method1 = outputMethods.get(i);
					IMethodInfo method2 = outputMethods.get(j);
					if (method1.getSignature().equals(method2.getSignature())) {
						throw new ReflectionUIError("Duplicate method '" + method1.getSignature()
								+ "' detected in type '" + containingType.getName() + "'");
					}
				}
			}
		}

		protected List<IFieldInfo> removeHiddenFields(List<IFieldInfo> fields) {
			List<IFieldInfo> result = new ArrayList<IFieldInfo>();
			for (IFieldInfo field : fields) {
				FieldCustomization f = InfoCustomizations.getFieldCustomization(containingTypeCustomization,
						field.getName());
				if (f != null) {
					if (f.isHidden()) {
						continue;
					}
				}
				result.add(field);
			}
			return result;
		}

		protected List<IMethodInfo> removeHiddenMethods(List<IMethodInfo> methods) {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>();
			for (IMethodInfo method : methods) {
				MethodCustomization m = InfoCustomizations.getMethodCustomization(containingTypeCustomization,
						method.getSignature());
				if (m != null) {
					if (m.isHidden()) {
						continue;
					}
				}
				result.add(method);
			}
			return result;
		}

		public List<IFieldInfo> getOutputFields() {
			return outputFields;
		}

		public List<IMethodInfo> getOutputMethods() {
			return outputMethods;
		}

		public MenuModel getMenuModel() {
			return menuModel;
		}

		protected class MethodCommonOptionsTransformer extends AbstractMethodTransformer {

			@Override
			public IMethodInfo process(IMethodInfo method, final MethodCustomization mc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				method = new MethodInfoProxy(method) {

					@Override
					public String getConfirmationMessage(Object object, InvocationData invocationData) {
						if (mc.getConfirmationMessage() != null) {
							return mc.getConfirmationMessage();
						}
						return super.getConfirmationMessage(object, invocationData);
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
					public ITypeInfoProxyFactory getReturnValueTypeSpecificities() {
						ITypeInfoProxyFactory result = new InfoCustomizationsFactory(reflectionUI,
								mc.getSpecificReturnValueTypeCustomizations()) {
							@Override
							public String getIdentifier() {
								return "MethodReturnValueTypeSpecificities [containingTypeName="
										+ containingType.getName() + ", methodSignature=" + base.getSignature() + "]";
							}
						};
						ITypeInfoProxyFactory baseTypeSpecificities = base.getReturnValueTypeSpecificities();
						if (baseTypeSpecificities != null) {
							result = new TypeInfoProxyFactorChain(baseTypeSpecificities, result);
						}
						return result;
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
						result.put(InfoCustomizations.CURRENT_PROXY_SOURCE_PROPERTY_KEY, infoCustomizations);
						if (mc.getSpecificProperties() != null) {
							if (mc.getSpecificProperties().entrySet().size() > 0) {
								result.putAll(mc.getSpecificProperties());
							}
						}
						return result;
					}

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
										result.put(InfoCustomizations.CURRENT_PROXY_SOURCE_PROPERTY_KEY,
												infoCustomizations);
										if (pc.getSpecificProperties() != null) {
											if (pc.getSpecificProperties().entrySet().size() > 0) {
												result.putAll(pc.getSpecificProperties());
											}
										}
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
						result = removeHiddenParameters(result);
						return result;
					}

					protected List<IParameterInfo> removeHiddenParameters(List<IParameterInfo> params) {
						List<IParameterInfo> result = new ArrayList<IParameterInfo>();
						for (IParameterInfo param : params) {
							ParameterCustomization p = InfoCustomizations.getParameterCustomization(mc,
									param.getName());
							if (p != null) {
								if (p.isHidden()) {
									continue;
								}
							}
							result.add(param);
						}
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

					public InfoCategory getCategory() {
						CustomizationCategory category = mc.getCategory();
						List<CustomizationCategory> categories = containingTypeCustomization.getMemberCategories();
						int categoryPosition = categories.indexOf(category);
						if (categoryPosition != -1) {
							return new InfoCategory(category.getCaption(), categoryPosition);
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
					List<IMenuItemContainer> ancestors = InfoCustomizations
							.getMenuElementAncestors(containingTypeCustomization, mc.getMenuLocation());
					if (ancestors != null) {
						ancestors = new ArrayList<IMenuItemContainer>(ancestors);
						ancestors.add(0, mc.getMenuLocation());
						DefaultMenuElementPosition menuPosition = new DefaultMenuElementPosition(method.getCaption(),
								MenuElementKind.ITEM, ancestors);
						menuModel.importContribution(menuPosition,
								new MethodActionMenuItem(new GeneratedMethodInfoProxy(method, containingType)));
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
					newFields.add(new MethodAsFieldInfo(method));
				}
				return method;
			}

		}

		protected class MethodParametersAsSubFormTransformer extends AbstractMethodTransformer {

			@Override
			public IMethodInfo process(IMethodInfo method, MethodCustomization mc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				if (mc.isParametersFormDisplayed()) {
					final AllMethodParametersAsFieldInfo methodParametersAsField = new AllMethodParametersAsFieldInfo(
							reflectionUI, method, method.getName() + ".parameters") {

						@Override
						public String getCaption() {
							return ReflectionUIUtils.composeMessage(method.getCaption(), "Settings");
						}

					};
					newFields.add(methodParametersAsField);
					method = new MethodInfoProxy(method) {

						@Override
						public List<IParameterInfo> getParameters() {
							return Collections.emptyList();
						}

						@Override
						public void validateParameters(Object object, InvocationData invocationData) throws Exception {
						}

						@Override
						public Object invoke(Object object, InvocationData invocationData) {
							MethodInvocationDataAsObjectFactory.Instance instance = (Instance) methodParametersAsField
									.getValue(object);
							invocationData = instance.getInvocationData();
							try {
								super.validateParameters(object, invocationData);
							} catch (Exception e) {
								throw new ReflectionUIError(e);
							}
							return super.invoke(object, invocationData);
						}

					};
				}
				return method;
			}

		}

		protected class MethodParameterAsFieldTransformer extends AbstractMethodTransformer {

			@Override
			public IMethodInfo process(IMethodInfo method, MethodCustomization mc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				for (final IParameterInfo param : method.getParameters()) {
					final ParameterCustomization pc = InfoCustomizations.getParameterCustomization(mc, param.getName());
					if (pc != null) {
						if (pc.isDisplayedAsField()) {
							final IMethodInfo finalMethod = method;
							final MethodParameterAsFieldInfo methodParameterAsField = new MethodParameterAsFieldInfo(
									method, param) {

								@Override
								public String getName() {
									return finalMethod.getName() + "." + param.getName();
								}

								@Override
								public String getCaption() {
									return ReflectionUIUtils.composeMessage(finalMethod.getCaption(),
											param.getCaption());
								}

							};
							newFields.add(methodParameterAsField);
							method = new MethodInfoProxy(method) {

								@Override
								public List<IParameterInfo> getParameters() {
									List<IParameterInfo> result = new ArrayList<IParameterInfo>();
									for (IParameterInfo param : super.getParameters()) {
										if (pc.getParameterName().equals(param.getName())) {
											continue;
										}
										result.add(param);
									}
									return result;
								}

								@Override
								public Object invoke(Object object, InvocationData invocationData) {
									Object paramValue = methodParameterAsField.getValue(object);
									invocationData.setParameterValue(param, paramValue);
									try {
										super.validateParameters(object, invocationData);
									} catch (Exception e) {
										throw new ReflectionUIError(e);
									}
									return super.invoke(object, invocationData);
								}
							};
						}
					}
				}
				return method;
			}

		}

		protected class MethodParameterDefaultValueSettingTransformer extends AbstractMethodTransformer {

			@Override
			public IMethodInfo process(IMethodInfo method, MethodCustomization mc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				for (final IParameterInfo param : method.getParameters()) {
					final ParameterCustomization pc = InfoCustomizations.getParameterCustomization(mc, param.getName());
					if (pc != null) {
						final Object defaultValue = pc.getDefaultValue().load();
						if (defaultValue != null) {
							method = new MethodInfoProxy(method) {

								@Override
								public List<IParameterInfo> getParameters() {
									List<IParameterInfo> result = new ArrayList<IParameterInfo>();
									for (IParameterInfo param : super.getParameters()) {
										if (pc.getParameterName().equals(param.getName())) {
											param = new ParameterInfoProxy(param) {

												@Override
												public Object getDefaultValue() {
													return defaultValue;
												}

											};
										}
										result.add(param);
									}
									return result;
								}
							};
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
					newMethods.add(
							new PresetInvocationDataMethodInfo(method, (InvocationData) invocationDataStorage.load()) {
								@Override
								public String getName() {
									return super.getName() + ".savedInvocation" + finalI;
								}

								@Override
								public String getCaption() {
									return ReflectionUIUtils.composeMessage(super.getCaption(),
											"Preset " + (finalI + 1));
								}

							});
				}
				return method;
			}

		}

		protected class FieldCommonOptionsFieldTransformer extends AbstractFieldTransformer {

			@Override
			public IFieldInfo process(IFieldInfo field, final FieldCustomization f, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				field = new FieldInfoProxy(field) {

					@Override
					public boolean isNullValueDistinct() {
						if (f.isNullValueDistinctForced()) {
							return true;
						}
						return super.isNullValueDistinct();
					}

					@Override
					public boolean isGetOnly() {
						if (f.isGetOnlyForced()) {
							return true;
						}
						return super.isGetOnly();
					}

					@Override
					public ValueReturnMode getValueReturnMode() {
						if (f.getCustomValueReturnMode() != null) {
							return f.getCustomValueReturnMode();
						}
						return super.getValueReturnMode();
					}

					@Override
					public ITypeInfoProxyFactory getTypeSpecificities() {
						ITypeInfoProxyFactory result = new InfoCustomizationsFactory(reflectionUI,
								f.getSpecificTypeCustomizations()) {
							@Override
							public String getIdentifier() {
								return "FieldTypeSpecificities [containingTypeName=" + containingType.getName()
										+ ", fieldName=" + base.getName() + "]";
							}
						};
						ITypeInfoProxyFactory baseTypeSpecificities = super.getTypeSpecificities();
						if (baseTypeSpecificities != null) {
							return new TypeInfoProxyFactorChain(baseTypeSpecificities, result);
						}
						return result;
					}

					@Override
					public String getNullValueLabel() {
						if (f.getNullValueLabel() != null) {
							return f.getNullValueLabel();
						}
						return super.getNullValueLabel();
					}

					@Override
					public Object[] getValueOptions(Object object) {
						if (f.getValueOptionsFieldName() != null) {
							IFieldInfo valueOptionsfield = ReflectionUIUtils.findInfoByName(containingType.getFields(),
									f.getValueOptionsFieldName());
							if (valueOptionsfield == null) {
								throw new ReflectionUIError(
										"Value options field not found: '" + f.getValueOptionsFieldName() + "'");
							}
							IListTypeInfo valueOptionsfieldType = (IListTypeInfo) valueOptionsfield.getType();
							Object options = valueOptionsfield.getValue(object);
							if (options == null) {
								return null;
							}
							return valueOptionsfieldType.toArray(options);
						}
						return super.getValueOptions(object);
					}

					@Override
					public boolean isFormControlMandatory() {
						if (f.isFormControlCreationForced()) {
							return true;
						}
						return super.isFormControlMandatory();
					}

					@Override
					public boolean isFormControlEmbedded() {
						if (f.isFormControlEmbeddingForced()) {
							return true;
						}
						return super.isFormControlEmbedded();
					}

					@Override
					public Map<String, Object> getSpecificProperties() {
						Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties());
						result.put(InfoCustomizations.CURRENT_PROXY_SOURCE_PROPERTY_KEY, infoCustomizations);
						if (f.getSpecificProperties() != null) {
							result.putAll(f.getSpecificProperties());
						}
						return result;
					}

					@Override
					public String getCaption() {
						if (f.getCustomFieldCaption() != null) {
							return f.getCustomFieldCaption();
						}
						return super.getCaption();
					}

					@Override
					public InfoCategory getCategory() {
						CustomizationCategory category = f.getCategory();
						List<CustomizationCategory> categories = containingTypeCustomization.getMemberCategories();
						int categoryPosition = categories.indexOf(category);
						if (categoryPosition != -1) {
							return new InfoCategory(category.getCaption(), categoryPosition);
						}
						return super.getCategory();
					}

					@Override
					public String getOnlineHelp() {
						if (f.getOnlineHelp() != null) {
							return f.getOnlineHelp();
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
					field = new ValueAsListFieldInfo(reflectionUI, field);
				}
				return field;
			}

		}

		protected class FieldNullStatusGeneratingTransformer extends AbstractFieldTransformer {

			@Override
			public IFieldInfo process(IFieldInfo field, FieldCustomization f, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				if (f.isNullStatusFieldDisplayed()) {
					newFields.add(new NullStatusFieldInfo(reflectionUI, field) {

						@Override
						public String getCaption() {
							return "Set " + base.getCaption();
						}

						@Override
						public String getName() {
							return super.getName() + ".nullStatus";
						}

					});
				}
				return field;
			}

		}

		protected class FieldSetterGeneratingTransformer extends AbstractFieldTransformer {

			@Override
			public IFieldInfo process(IFieldInfo field, FieldCustomization f, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				if (f.isSetterGenerated()) {
					newMethods.add(new FieldAsSetterInfo(field) {

						@Override
						public String getCaption() {
							return "Set " + field.getCaption();
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
						@Override
						public String getName() {
							return super.getName() + ".duplicate";
						}

						@Override
						public String getCaption() {
							return ReflectionUIUtils.composeMessage(super.getCaption(), "Duplicate");
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

						@Override
						public boolean isGetOnly() {
							return false;
						}

						@Override
						public void setValue(Object object, Object value) {
							IMethodInfo customMethod = ReflectionUIUtils
									.findMethodBySignature(containingType.getMethods(), f.getCustomSetterSignature());
							if (customMethod == null) {
								throw new ReflectionUIError("Field '" + f.getFieldName()
										+ "': Custom setter not found: '" + f.getCustomSetterSignature() + "'");
							}
							customMethod.invoke(object, new InvocationData(value));
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
					newMethods.add(new FieldAsGetterInfo(field) {

						@Override
						public String getCaption() {
							return "Show " + field.getCaption();
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
					ITypeInfo newType = f.getTypeConversion().findNewType(reflectionUI);
					Mapper<Object> conversionMethod = f.getTypeConversion().buildOverallConversionMethod();
					Mapper<Object> reverseConversionMethod = f.getTypeConversion()
							.buildOverallReverseConversionMethod();
					field = new ChangedTypeFieldInfo(field, newType, conversionMethod, reverseConversionMethod);
				}
				return field;
			}

		}

		protected class FieldNullReplacementTransformer extends AbstractFieldTransformer {

			@Override
			public IFieldInfo process(IFieldInfo field, FieldCustomization f, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods) {
				final Object nullReplacement = f.getNullReplacement().load();
				if (nullReplacement != null) {
					field = new FieldInfoProxy(field) {

						@Override
						public Object getValue(Object object) {
							Object result = super.getValue(object);
							if (result == null) {
								result = nullReplacement;
							}
							return result;
						}

					};
				}
				return field;
			}

		}

		protected abstract class AbstractFieldTransformer {

			public abstract IFieldInfo process(IFieldInfo field, FieldCustomization f, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods);

		}

		protected abstract class AbstractMethodTransformer {

			public abstract IMethodInfo process(IMethodInfo method, MethodCustomization mc, List<IFieldInfo> newFields,
					List<IMethodInfo> newMethods);

		}

	}

}