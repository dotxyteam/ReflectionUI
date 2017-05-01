package xy.reflect.ui.info.type.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.InfoCategory;
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
import xy.reflect.ui.info.custom.InfoCustomizations.TypeCustomization;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MethodAsField;
import xy.reflect.ui.info.field.AllMethodParametersAsField;
import xy.reflect.ui.info.field.CapsuleField;
import xy.reflect.ui.info.field.NerverNullField;
import xy.reflect.ui.info.field.NullStatusField;
import xy.reflect.ui.info.field.MethodParameterAsField;
import xy.reflect.ui.info.field.SubFieldInfo;
import xy.reflect.ui.info.field.ValueAsListField;
import xy.reflect.ui.info.menu.DefaultMenuElementPosition;
import xy.reflect.ui.info.menu.IMenuItemContainer;
import xy.reflect.ui.info.menu.MenuElementKind;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.menu.MethodActionMenuItem;
import xy.reflect.ui.info.method.FieldAsGetter;
import xy.reflect.ui.info.method.FieldAsSetter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.method.SubMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
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
import xy.reflect.ui.util.Pair;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class InfoCustomizationsFactory extends HiddenNullableFacetsTypeInfoProxyFactory {

	protected final InfoCustomizations infoCustomizations;

	public InfoCustomizationsFactory(ReflectionUI reflectionUI, InfoCustomizations infoCustomizations) {
		super(reflectionUI);
		this.infoCustomizations = infoCustomizations;
	}

	public InfoCustomizations getInfoCustomizations() {
		return infoCustomizations;
	}

	@Override
	protected MenuModel getMenuModel(ITypeInfo type) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.infoCustomizations, type.getName());
		if (tc != null) {
			MenuModel menuModel = new MenuModel();
			menuModel.importContributions(super.getMenuModel(type));
			menuModel.importContributions(tc.getMenuModel());

			List<IFieldInfo> inputFields = new ArrayList<IFieldInfo>(super.getFields(type));
			List<IMethodInfo> inputMethods = new ArrayList<IMethodInfo>(super.getMethods(type));
			List<IFieldInfo> outputFields = new ArrayList<IFieldInfo>();
			List<IMethodInfo> outputMethods = new ArrayList<IMethodInfo>();
			evolveMembers(inputFields, inputMethods, outputFields, outputMethods, menuModel, type);

			return menuModel;
		}
		return super.getMenuModel(type);
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
	protected ITypeInfoProxyFactory getTypeSpecificities(final IFieldInfo field, final ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			FieldCustomization f = InfoCustomizations.getFieldCustomization(t, field.getName());
			if (f != null) {
				ITypeInfoProxyFactory result = new InfoCustomizationsFactory(reflectionUI,
						f.getSpecificTypeCustomizations()) {
					@Override
					public String getIdentifier() {
						return "FieldTypeSpecificities [containingTypeName=" + containingType.getName() + ", fieldName="
								+ field.getName() + "]";
					}
				};
				ITypeInfoProxyFactory baseTypeSpecificities = field.getTypeSpecificities();
				if (baseTypeSpecificities != null) {
					return new TypeInfoProxyFactorChain(baseTypeSpecificities, result);
				}
				return result;
			}
		}
		return super.getTypeSpecificities(field, containingType);
	}

	@Override
	protected boolean isReturnValueDetached(IMethodInfo method, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
					method.getSignature());
			if (m != null) {
				if (m.isDetachedReturnValueForced()) {
					return true;
				}
			}
		}
		return super.isReturnValueDetached(method, containingType);
	}

	@Override
	protected ITypeInfoProxyFactory getReturnValueTypeSpecificities(final IMethodInfo method,
			final ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
					method.getSignature());
			if (m != null) {
				ITypeInfoProxyFactory result = new InfoCustomizationsFactory(reflectionUI,
						m.getSpecificReturnValueTypeCustomizations()) {
					@Override
					public String getIdentifier() {
						return "MethodReturnValueTypeSpecificities [containingTypeName=" + containingType.getName()
								+ ", methodSignature=" + method.getSignature() + "]";
					}
				};
				ITypeInfoProxyFactory baseTypeSpecificities = method.getReturnValueTypeSpecificities();
				if (baseTypeSpecificities != null) {
					result = new TypeInfoProxyFactorChain(baseTypeSpecificities, result);
				}
				return result;
			}
		}
		return super.getReturnValueTypeSpecificities(method, containingType);
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
	protected String getNullValueLabel(IFieldInfo field, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			FieldCustomization f = InfoCustomizations.getFieldCustomization(t, field.getName());
			if (f != null) {
				if (f.getNullValueLabel() != null) {
					return f.getNullValueLabel();
				}
			}
		}
		return super.getNullValueLabel(field, containingType);
	}

	@Override
	protected String getNullReturnValueLabel(IMethodInfo method, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
					method.getSignature());
			if (m != null) {
				if (m.getNullReturnValueLabel() != null) {
					return m.getNullReturnValueLabel();
				}
			}
		}
		return super.getNullReturnValueLabel(method, containingType);
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
	protected ValueReturnMode getValueReturnMode(IFieldInfo field, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			FieldCustomization f = InfoCustomizations.getFieldCustomization(t, field.getName());
			if (f != null) {
				if (f.getCustomValueReturnMode() != null) {
					return f.getCustomValueReturnMode();
				}
			}
		}
		return super.getValueReturnMode(field, containingType);
	}

	@Override
	protected Object[] getValueOptions(Object object, IFieldInfo field, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			FieldCustomization f = InfoCustomizations.getFieldCustomization(t, field.getName());
			if (f != null) {
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
			}
		}
		return super.getValueOptions(object, field, containingType);
	}

	@Override
	protected ValueReturnMode getValueReturnMode(IMethodInfo method, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
					method.getSignature());
			if (m != null) {
				if (m.getCustomValueReturnMode() != null) {
					return m.getCustomValueReturnMode();
				}
			}
		}
		return super.getValueReturnMode(method, containingType);
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
			for (final ListItemFieldShortcut s : l.getAllowedItemFieldShortcuts()) {
				final String fieldCaption;
				if (s.getCustomFieldCaption() != null) {
					fieldCaption = s.getCustomFieldCaption();
				} else {
					fieldCaption = ReflectionUIUtils.identifierToCaption(s.getFieldName());
				}
				boolean fieldFound = false;
				if (selection.size() == 1) {
					final ItemPosition itemPosition = selection.get(0);
					final Object item = itemPosition.getItem();
					if (item != null) {
						ITypeInfo actualItemType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(item));
						for (final IFieldInfo itemField : actualItemType.getFields()) {
							if (itemField.getName().equals(s.getFieldName())) {
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
										return s.getFieldName();
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

									public Runnable getCustomUndoUpdateJob(Object object, Object value) {
										return delegate.getCustomUndoUpdateJob(object, value);
									}

									public boolean isValueNullable() {
										return delegate.isValueNullable();
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

								};
								result.add(property);
								fieldFound = true;
								break;
							}
						}
					}
				}
				if ((!fieldFound) && s.isAlwaysShown()) {
					AbstractListProperty property = new AbstractListProperty() {

						@Override
						public boolean isEnabled() {
							return false;
						}

						@Override
						public String getName() {
							return s.getFieldName();
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
						public boolean isValueNullable() {
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

			for (final ListItemMethodShortcut s : l.getAllowedItemMethodShortcuts()) {
				final String methodName = ReflectionUIUtils.extractMethodNameFromSignature(s.getMethodSignature());
				final String methodCaption;
				if (s.getCustomMethodCaption() != null) {
					methodCaption = s.getCustomMethodCaption();
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
							if (itemMethod.getSignature().equals(s.getMethodSignature())) {
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

									public Runnable getUndoJob(Object object, InvocationData invocationData) {
										return delegate.getUndoJob(object, invocationData);
									}

									public boolean isReturnValueNullable() {
										return delegate.isReturnValueNullable();
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

								};
								result.add(action);
								methodFound = true;
								break;
							}
						}
					}
				}
				if ((!methodFound) && s.isAlwaysShown()) {
					result.add(new AbstractListAction() {

						@Override
						public boolean isReturnValueNullable() {
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
	protected String getIconImagePath(ITypeInfo type) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations, type.getName());
		if (t != null) {
			if (t.getIconImagePath() != null) {
				String result = t.getIconImagePath().getSpecification();
				if (result.length() > 0) {
					return result;
				}
			}
		}
		return super.getIconImagePath(type);
	}

	@Override
	protected String getIconImagePath(IMethodInfo method, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
					method.getSignature());
			if (m != null) {
				if (m.getIconImagePath() != null) {
					String result = m.getIconImagePath().getSpecification();
					if (result.length() > 0) {
						return result;
					}
				}
			}
		}
		return super.getIconImagePath(method, containingType);
	}

	@Override
	protected boolean isFormControlMandatory(IFieldInfo field, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			FieldCustomization f = InfoCustomizations.getFieldCustomization(t, field.getName());
			if (f != null) {
				if (f.isFormControlCreationForced()) {
					return true;
				}
			}
		}
		return super.isFormControlMandatory(field, containingType);
	}

	@Override
	protected boolean isFormControlEmbedded(IFieldInfo field, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			FieldCustomization f = InfoCustomizations.getFieldCustomization(t, field.getName());
			if (f != null) {
				if (f.isFormControlEmbeddingForced()) {
					return true;
				}
			}
		}
		return super.isFormControlEmbedded(field, containingType);
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
	protected Map<String, Object> getSpecificProperties(IFieldInfo field, ITypeInfo containingType) {
		Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(field, containingType));
		result.put(InfoCustomizations.CURRENT_PROXY_SOURCE_PROPERTY_KEY, this.infoCustomizations);
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			FieldCustomization f = InfoCustomizations.getFieldCustomization(t, field.getName());
			if (f != null) {
				if (f.getSpecificProperties() != null) {
					result.putAll(f.getSpecificProperties());
				}
			}
		}
		return result;
	}

	@Override
	protected Map<String, Object> getSpecificProperties(IMethodInfo method, ITypeInfo containingType) {
		Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(method, containingType));
		result.put(InfoCustomizations.CURRENT_PROXY_SOURCE_PROPERTY_KEY, this.infoCustomizations);
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
					method.getSignature());
			if (m != null) {
				if (m.getSpecificProperties() != null) {
					if (m.getSpecificProperties().entrySet().size() > 0) {
						result.putAll(m.getSpecificProperties());
					}
				}
			}
		}
		return result;
	}

	@Override
	protected Map<String, Object> getSpecificProperties(IParameterInfo param, IMethodInfo method,
			ITypeInfo containingType) {
		Map<String, Object> result = new HashMap<String, Object>(
				super.getSpecificProperties(param, method, containingType));
		result.put(InfoCustomizations.CURRENT_PROXY_SOURCE_PROPERTY_KEY, this.infoCustomizations);
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
					method.getSignature());
			if (m != null) {
				ParameterCustomization p = InfoCustomizations.getParameterCustomization(m, param.getName());
				if (p != null) {
					if (p.getSpecificProperties() != null) {
						if (p.getSpecificProperties().entrySet().size() > 0) {
							result.putAll(p.getSpecificProperties());
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	protected boolean isValueNullable(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
					method.getSignature());
			if (m != null) {
				ParameterCustomization p = InfoCustomizations.getParameterCustomization(m, param.getName());
				if (p != null) {
					if (p.isNullableFacetHidden()) {
						return false;
					}
				}
			}
		}
		return param.isValueNullable();
	}

	@Override
	protected String getCaption(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
					method.getSignature());
			if (m != null) {
				ParameterCustomization p = InfoCustomizations.getParameterCustomization(m, param.getName());
				if (p != null) {
					if (p.getCustomParameterCaption() != null) {
						return p.getCustomParameterCaption();
					}
				}
			}
		}
		return super.getCaption(param, method, containingType);
	}

	@Override
	protected boolean isValueNullable(IFieldInfo field, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			FieldCustomization f = InfoCustomizations.getFieldCustomization(t, field.getName());
			if (f != null) {
				if (f.isNullableFacetHidden()) {
					return false;
				}
			}
		}
		return field.isValueNullable();
	}

	@Override
	protected boolean isGetOnly(IFieldInfo field, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			FieldCustomization f = InfoCustomizations.getFieldCustomization(t, field.getName());
			if (f != null) {
				if (f.isGetOnlyForced()) {
					return true;
				}
				if (f.getCustomSetterSignature() != null) {
					return false;
				}
			}
		}
		return super.isGetOnly(field, containingType);
	}

	@Override
	protected void setValue(Object object, Object value, IFieldInfo field, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			FieldCustomization f = InfoCustomizations.getFieldCustomization(t, field.getName());
			if (f != null) {
				if (f.getCustomSetterSignature() != null) {
					IMethodInfo customMethod = ReflectionUIUtils.findMethodBySignature(containingType.getMethods(),
							f.getCustomSetterSignature());
					if (customMethod == null) {
						throw new ReflectionUIError("Custom setter not found: '" + f.getCustomSetterSignature() + "'");
					}
					customMethod.invoke(object, new InvocationData(value));
				}
			}
		}
		super.setValue(object, value, field, containingType);
	}

	@Override
	protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			FieldCustomization f = InfoCustomizations.getFieldCustomization(t, field.getName());
			if (f != null) {
				if (f.getCustomFieldCaption() != null) {
					return f.getCustomFieldCaption();
				}
			}
		}
		return super.getCaption(field, containingType);
	}

	@Override
	protected boolean isReadOnly(IMethodInfo method, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
					method.getSignature());
			if (m != null) {
				if (m.isReadOnlyForced()) {
					return true;
				}
			}
		}
		return super.isReadOnly(method, containingType);
	}

	@Override
	protected List<IParameterInfo> getParameters(IMethodInfo method, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
					method.getSignature());
			if (m != null) {
				List<IParameterInfo> result = new ArrayList<IParameterInfo>(
						super.getParameters(method, containingType));
				result = removeHiddenParameters(result, m);
				return result;
			}
		}
		return super.getParameters(method, containingType);
	}

	protected List<IParameterInfo> removeHiddenParameters(List<IParameterInfo> params, MethodCustomization m) {
		List<IParameterInfo> result = new ArrayList<IParameterInfo>();
		for (IParameterInfo param : params) {
			ParameterCustomization p = InfoCustomizations.getParameterCustomization(m, param.getName());
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
	protected String getCaption(IMethodInfo method, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
					method.getSignature());
			if (m != null) {
				if (m.getCustomMethodCaption() != null) {
					return m.getCustomMethodCaption();
				}
			}
		}
		return super.getCaption(method, containingType);
	}

	@Override
	protected List<IMethodInfo> getConstructors(ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getConstructors(containingType));
			for (Iterator<IMethodInfo> it = result.iterator(); it.hasNext();) {
				IMethodInfo ctor = it.next();
				MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
						ctor.getSignature());
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
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {

			List<IFieldInfo> inputFields = new ArrayList<IFieldInfo>(super.getFields(containingType));
			List<IMethodInfo> inputMethods = new ArrayList<IMethodInfo>(super.getMethods(containingType));
			List<IFieldInfo> outputFields = new ArrayList<IFieldInfo>();
			List<IMethodInfo> outputMethods = new ArrayList<IMethodInfo>();
			MenuModel menuModel = new MenuModel();
			evolveMembers(inputFields, inputMethods, outputFields, outputMethods, menuModel, containingType);

			outputFields = removeHiddenFields(outputFields, containingType);
			outputFields = sortFields(outputFields, containingType);
			return outputFields;

		}
		return super.getFields(containingType);
	}

	protected void evolveMembers(List<IFieldInfo> inputFields, List<IMethodInfo> inputMethods,
			List<IFieldInfo> outputFields, List<IMethodInfo> outputMethods, MenuModel menuModel,
			ITypeInfo containingType) {

		transformInputMembers(inputFields, inputMethods, outputFields, outputMethods, menuModel, containingType);
		encapsulateOutputMembers(inputFields, inputMethods, outputFields, outputMethods, containingType);
		checkDuplicates(new Pair<List<IFieldInfo>, List<IMethodInfo>>(outputFields, outputMethods));

		if ((inputFields.size() > 0) || (inputMethods.size() > 0)) {
			evolveMembers(inputFields, inputMethods, outputFields, outputMethods, menuModel, containingType);
		}
	}

	protected void checkDuplicates(Pair<List<IFieldInfo>, List<IMethodInfo>> members) {
		List<IFieldInfo> fields = members.getFirst();
		for (int i = 0; i < fields.size(); i++) {
			for (int j = i + 1; j < fields.size(); j++) {
				IFieldInfo field1 = fields.get(i);
				IFieldInfo field2 = fields.get(j);
				if (field1.getName().equals(field2.getName())) {
					throw new ReflectionUIError("Duplicate field detected: '" + field1.getName() + "'");
				}
			}
		}
		List<IMethodInfo> methods = members.getSecond();
		for (int i = 0; i < methods.size(); i++) {
			for (int j = i + 1; j < methods.size(); j++) {
				IMethodInfo method1 = methods.get(i);
				IMethodInfo method2 = methods.get(j);
				if (method1.getSignature().equals(method2.getSignature())) {
					throw new ReflectionUIError("Duplicate method detected: '" + method1.getSignature() + "'");
				}
			}
		}
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

	protected void transformInputMembers(List<IFieldInfo> inputFields, List<IMethodInfo> inputMethods,
			List<IFieldInfo> outputFields, List<IMethodInfo> outputMethods, MenuModel menuModel,
			ITypeInfo containingType) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (tc != null) {
			for (IFieldInfo field : new ArrayList<IFieldInfo>(inputFields)) {
				inputFields.remove(field);
				FieldCustomization f = InfoCustomizations.getFieldCustomization(tc, field.getName());
				if (f != null) {
					if (f.isGetterGenerated()) {
						inputMethods.add(new FieldAsGetter(field, field.getName() + ".get") {

							@Override
							public String getCaption() {
								return "Show " + field.getCaption();
							}

						});
					}
					if (f.isSetterGenerated()) {
						inputMethods.add(new FieldAsSetter(field, field.getName() + ".set") {

							@Override
							public String getCaption() {
								return "Set " + field.getCaption();
							}

						});
					}
					if (f.isNullStatusFieldDisplayed()) {
						inputFields.add(new NullStatusField(reflectionUI, field) {

							@Override
							public String getCaption() {
								return "Set " + base.getCaption();
							}

							@Override
							public String getName() {
								return super.getName() + ".nullStatus";
							}

						});
						field = new NerverNullField(reflectionUI, field);
					}
					if (f.isDisplayedAsSingletonList()) {
						field = new ValueAsListField(reflectionUI, field);
					}
				}
				outputFields.add(field);
			}

			for (IMethodInfo method : new ArrayList<IMethodInfo>(inputMethods)) {
				inputMethods.remove(method);
				MethodCustomization mc = InfoCustomizations.getMethodCustomization(tc,
						method.getSignature());
				if (mc != null) {
					if (mc.isReturnValueFieldGenerated()) {
						inputFields.add(new MethodAsField(method, method.getName() + ".result") {

							@Override
							public String getCaption() {
								return method.getCaption() + " Result";
							}

						});
					}
					for (final IParameterInfo param : method.getParameters()) {
						final ParameterCustomization pc = InfoCustomizations.getParameterCustomization(mc,
								param.getName());
						if (pc != null) {
							if (pc.isDisplayedAsField()) {
								final IMethodInfo finalMethod = method;
								final MethodParameterAsField methodParameterAsField = new MethodParameterAsField(method,
										param) {

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
								inputFields.add(methodParameterAsField);
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
					if (mc.isParametersFormDisplayed()) {
						final AllMethodParametersAsField methodParametersAsField = new AllMethodParametersAsField(
								reflectionUI, method, method.getName() + ".parameters") {

							@Override
							public String getCaption() {
								return ReflectionUIUtils.composeMessage(method.getCaption(), "Settings");
							}

						};
						inputFields.add(methodParametersAsField);
						method = new MethodInfoProxy(method) {

							@Override
							public List<IParameterInfo> getParameters() {
								return Collections.emptyList();
							}

							@Override
							public void validateParameters(Object object, InvocationData invocationData)
									throws Exception {
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
					if (mc.getMenuLocation() != null) {
						List<IMenuItemContainer> ancestors = InfoCustomizations.getMenuElementAncestors(tc,
								mc.getMenuLocation());
						if (ancestors != null) {
							ancestors = new ArrayList<IMenuItemContainer>(ancestors);
							ancestors.add(0, mc.getMenuLocation());
							DefaultMenuElementPosition menuPosition = new DefaultMenuElementPosition(
									method.getCaption(), MenuElementKind.ITEM, ancestors);
							menuModel.importContribution(menuPosition,
									new MethodActionMenuItem(new GeneratedMethodInfoProxy(method, containingType)));
						}
					}
				}
				outputMethods.add(method);
			}
		}
	}

	protected void encapsulateOutputMembers(List<IFieldInfo> inputFields, List<IMethodInfo> inputMethods,
			List<IFieldInfo> outputFields, List<IMethodInfo> outputMethods, ITypeInfo containingType) {
		TypeCustomization tc = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (tc != null) {
			Map<String, Pair<List<IFieldInfo>, List<IMethodInfo>>> encapsulatedMembersByCapsuleFieldName = new HashMap<String, Pair<List<IFieldInfo>, List<IMethodInfo>>>();
			for (IFieldInfo field : new ArrayList<IFieldInfo>(outputFields)) {
				FieldCustomization fc = InfoCustomizations.getFieldCustomization(tc, field.getName());
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
						outputFields.remove(field);
					}
				}
			}

			for (IMethodInfo method : new ArrayList<IMethodInfo>(outputMethods)) {
				MethodCustomization mc = InfoCustomizations.getMethodCustomization(tc,
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
						outputMethods.remove(method);
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
				IFieldInfo duplicateField = ReflectionUIUtils.findInfoByName(outputFields, capsuleFieldName);
				String contextId = "EncapsulationContext [containingType=" + containingType.getName() + "]";
				if (duplicateField != null) {
					CapsuleField duplicateFieldAsTranslatedCapsuleField = CapsuleField.translateProxy(duplicateField);
					if (duplicateFieldAsTranslatedCapsuleField != null) {
						outputFields.remove(duplicateField);
						encapsulatedFields.addAll(0, duplicateFieldAsTranslatedCapsuleField.getEncapsulatedFields());
						encapsulatedMethods.addAll(0, duplicateFieldAsTranslatedCapsuleField.getEncapsulatedMethods());
						contextId = duplicateFieldAsTranslatedCapsuleField.getContextId();
					} else {
						throw new ReflectionUIError("Failed to generate capsule field: Duplicate field name detected: '"
								+ capsuleFieldName + "'");
					}
				}
				CapsuleField capsuleField = new CapsuleField(reflectionUI, capsuleFieldName, encapsulatedFields,
						encapsulatedMethods, contextId);
				initializeEncapsulatedMemberCustomizations(capsuleField, containingType);
				inputFields.add(capsuleField);
			}
		}
	}

	protected void initializeEncapsulatedMemberCustomizations(CapsuleField capsuleField, ITypeInfo containingType) {
		TypeCustomization containingTc = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		ITypeInfo capsuleFieldType = capsuleField.getType();
		TypeCustomization capsuleTc = InfoCustomizations.getTypeCustomization(infoCustomizations,
				capsuleFieldType.getName(), true);
		for (IFieldInfo field : capsuleFieldType.getFields()) {
			if (InfoCustomizations.getFieldCustomization(capsuleTc, field.getName()) == null) {
				FieldCustomization initialFc = InfoCustomizations.getFieldCustomization(capsuleTc, field.getName(),
						true);
				FieldCustomization baseFc = InfoCustomizations.getFieldCustomization(containingTc, field.getName());
				initialFc.setCustomFieldCaption(baseFc.getCustomFieldCaption());
				initialFc.setCustomValueReturnMode(baseFc.getCustomValueReturnMode());
				initialFc.setDisplayedAsSingletonList(baseFc.isDisplayedAsSingletonList());
				initialFc.setFormControlCreationForced(baseFc.isFormControlCreationForced());
				initialFc.setFormControlEmbeddingForced(baseFc.isFormControlEmbeddingForced());
				initialFc.setGetOnlyForced(baseFc.isGetOnlyForced());
				initialFc.setNullableFacetHidden(baseFc.isNullableFacetHidden());
				initialFc.setNullValueLabel(baseFc.getNullValueLabel());
				initialFc.setOnlineHelp(baseFc.getOnlineHelp());
			}
		}
		for (IMethodInfo method : capsuleFieldType.getMethods()) {
			if (InfoCustomizations.getMethodCustomization(capsuleTc,
					method.getSignature()) == null) {
				MethodCustomization initialMc = InfoCustomizations.getMethodCustomization(capsuleTc,
						method.getSignature(), true);
				MethodCustomization baseMc = InfoCustomizations.getMethodCustomization(containingTc,
						method.getSignature());
				initialMc.setCustomMethodCaption(baseMc.getCustomMethodCaption());
				initialMc.setCustomValueReturnMode(baseMc.getCustomValueReturnMode());
				initialMc.setDetachedReturnValueForced(baseMc.isDetachedReturnValueForced());
				initialMc.setIconImagePath(baseMc.getIconImagePath());
				initialMc.setNullReturnValueLabel(baseMc.getNullReturnValueLabel());
				initialMc.setOnlineHelp(baseMc.getOnlineHelp());
				initialMc.setReadOnlyForced(baseMc.isReadOnlyForced());
			}
		}
	}

	protected List<IFieldInfo> removeHiddenFields(List<IFieldInfo> fields, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			List<IFieldInfo> result = new ArrayList<IFieldInfo>();
			for (IFieldInfo field : fields) {
				FieldCustomization f = InfoCustomizations.getFieldCustomization(t, field.getName());
				if (f != null) {
					if (f.isHidden()) {
						continue;
					}
				}
				result.add(field);
			}
			return result;
		}
		return fields;
	}

	@Override
	protected List<IMethodInfo> getMethods(ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {

			List<IFieldInfo> inputFields = new ArrayList<IFieldInfo>(super.getFields(containingType));
			List<IMethodInfo> inputMethods = new ArrayList<IMethodInfo>(super.getMethods(containingType));
			List<IFieldInfo> outputFields = new ArrayList<IFieldInfo>();
			List<IMethodInfo> outputMethods = new ArrayList<IMethodInfo>();
			MenuModel menuModel = new MenuModel();
			evolveMembers(inputFields, inputMethods, outputFields, outputMethods, menuModel, containingType);

			outputMethods = removeHiddenMethods(outputMethods, containingType);
			outputMethods = sortMethods(outputMethods, containingType);
			return outputMethods;
		}
		return super.getMethods(containingType);
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

	protected List<IMethodInfo> removeHiddenMethods(List<IMethodInfo> methods, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			List<IMethodInfo> result = new ArrayList<IMethodInfo>();
			for (IMethodInfo method : methods) {
				MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
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
							throw new ReflectionUIError("Invalid validating method: Number of parameters > 0: "
									+ method.getSignature());
						}
						method.invoke(object, new InvocationData());
					}
				}
			}
		}
		super.validate(type, object);
	}

	@Override
	protected InfoCategory getCategory(IFieldInfo field, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			FieldCustomization f = InfoCustomizations.getFieldCustomization(t, field.getName());
			if (f != null) {
				CustomizationCategory category = f.getCategory();
				List<CustomizationCategory> categories = t.getMemberCategories();
				int categoryPosition = categories.indexOf(category);
				if (categoryPosition != -1) {
					return new InfoCategory(category.getCaption(), categoryPosition);
				}
			}
		}
		return super.getCategory(field, containingType);
	}

	@Override
	protected InfoCategory getCategory(IMethodInfo method, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
					method.getSignature());
			if (m != null) {
				CustomizationCategory category = m.getCategory();
				List<CustomizationCategory> categories = t.getMemberCategories();
				int categoryPosition = categories.indexOf(category);
				if (categoryPosition != -1) {
					return new InfoCategory(category.getCaption(), categoryPosition);
				}
			}
		}
		return super.getCategory(method, containingType);
	}

	@Override
	protected String getOnlineHelp(IFieldInfo field, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			FieldCustomization f = InfoCustomizations.getFieldCustomization(t, field.getName());
			if (f != null) {
				if (f.getOnlineHelp() != null) {
					return f.getOnlineHelp();
				}
			}
		}
		return super.getOnlineHelp(field, containingType);
	}

	@Override
	protected String getOnlineHelp(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
					method.getSignature());
			if (m != null) {
				ParameterCustomization p = InfoCustomizations.getParameterCustomization(m, param.getName());
				if (p != null) {
					if (p.getOnlineHelp() != null) {
						return p.getOnlineHelp();
					}
				}
			}
		}
		return super.getOnlineHelp(param, method, containingType);
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

	@Override
	protected String getOnlineHelp(IMethodInfo method, ITypeInfo containingType) {
		TypeCustomization t = InfoCustomizations.getTypeCustomization(this.infoCustomizations,
				containingType.getName());
		if (t != null) {
			MethodCustomization m = InfoCustomizations.getMethodCustomization(t,
					method.getSignature());
			if (m != null) {
				if (m.getOnlineHelp() != null) {
					return m.getOnlineHelp();
				}
			}
		}
		return super.getOnlineHelp(method, containingType);
	}

}