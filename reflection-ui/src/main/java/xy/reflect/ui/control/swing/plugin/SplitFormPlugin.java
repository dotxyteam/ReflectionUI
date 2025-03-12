
package xy.reflect.ui.control.swing.plugin;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.EmbeddedFormControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlSplitPane;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.info.type.source.TypeInfoSourceProxy;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.PrecomputedTypeInstanceWrapper;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control plugin that allows to have a split form display with a movable
 * divider.
 * 
 * @author olitank
 *
 */
public class SplitFormPlugin extends AbstractSimpleCustomizableFieldControlPlugin {

	@Override
	public String getControlTitle() {
		return "Split Form";
	}

	@Override
	public boolean handles(IFieldControlInput input) {
		IFieldControlData data = input.getControlData();
		if (!data.isNullValueDistinct()) {
			ITypeInfo fieldType = data.getType();
			if (fieldType.getFields().size() >= 2) {
				if (data.isFormControlMandatory()) {
					return true;
				}
				if (!(fieldType instanceof IEnumerationTypeInfo)
						&& !ReflectionUIUtils.hasPolymorphicInstanceSubTypes(fieldType)) {
					Class<?> javaType;
					try {
						javaType = ClassUtils.getCachedClassForName(fieldType.getName());
					} catch (ClassNotFoundException e) {
						return true;
					}
					if (!ClassUtils.isPrimitiveClassOrWrapperOrString(javaType)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	protected boolean handles(Class<?> javaType) {
		throw new ReflectionUIError();
	}

	@Override
	public AbstractConfiguration getDefaultControlCustomization() {
		return new SplitFormConfiguration();
	}

	@Override
	public SplitForm createControl(Object renderer, IFieldControlInput input) {
		return new SplitForm((SwingRenderer) renderer, input);
	}

	@Override
	public boolean canDisplayDistinctNullValue() {
		return false;
	}

	public static class SplitFormConfiguration extends AbstractConfiguration {
		private static final long serialVersionUID = 1L;

		public Orientation orientation = Orientation.HORIZONTAL_SPLIT;
		public int firstLotFieldCount = 1;
		public double defaultDividerLocation = 0.5;
	}

	public enum Orientation {
		HORIZONTAL_SPLIT, VERTICAL_SPLIT
	}

	public class SplitForm extends ControlSplitPane implements IAdvancedFieldControl {
		private static final long serialVersionUID = 1L;

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected EmbeddedFormControl subControl1;
		protected EmbeddedFormControl subControl2;
		protected boolean refreshListenersDisabled = false;

		public SplitForm(SwingRenderer swingRenderer, IFieldControlInput input) {
			this.swingRenderer = swingRenderer;
			this.input = input;
			this.data = input.getControlData();
			refreshUI(true);
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension result = super.getPreferredSize();
			if (result == null) {
				result = new Dimension(100, 100);
			} else {
				int screenWidth = SwingRendererUtils.getScreenBounds(this).width;
				if (result.width > screenWidth) {
					result.width = screenWidth;
				}
			}
			ITypeInfo objectType = input.getControlData().getType();
			if (objectType != null) {
				Dimension configuredSize = new Dimension(objectType.getFormPreferredWidth(),
						objectType.getFormPreferredHeight());
				if (configuredSize.width > 0) {
					result.width = configuredSize.width;
				}
				if (configuredSize.height > 0) {
					result.height = configuredSize.height;
				}
			}
			return result;
		}

		@Override
		public Dimension getMinimumSize() {
			Dimension result = super.getMinimumSize();
			if (result != null) {
				Dimension preferredSize = getPreferredSize();
				if (preferredSize != null) {
					result.width = Math.min(result.width, preferredSize.width);
					result.height = Math.min(result.height, preferredSize.height);
				}
			}
			return result;

		}

		@Override
		public Dimension getMaximumSize() {
			Dimension result = super.getMaximumSize();
			if (result != null) {
				Dimension preferredSize = getPreferredSize();
				if (preferredSize != null) {
					result.width = Math.max(result.width, preferredSize.width);
					result.height = Math.max(result.height, preferredSize.height);
				}
			}
			return result;
		}

		@Override
		public boolean refreshUI(boolean refreshStructure) {
			SplitFormConfiguration controlCustomization = (SplitFormConfiguration) loadControlCustomization(input);
			if (refreshStructure) {
				SwingRendererUtils.ensureDividerLocation(this, controlCustomization.defaultDividerLocation);
				SwingRendererUtils.showFieldCaptionOnBorder(data, this, swingRenderer);
				SwingRendererUtils.handleComponentSizeChange(this);
			}
			boolean layoutUpdateNeeded = false;
			if ((subControl1 == null) || !subControl1.refreshUI(refreshStructure)) {
				subControl1 = createSubControl1();
				layoutUpdateNeeded = true;
			}
			if ((subControl2 == null) || !subControl2.refreshUI(refreshStructure)) {
				subControl2 = createSubControl2();
				layoutUpdateNeeded = true;
			}
			if (layoutUpdateNeeded || refreshStructure) {
				layoutSubControls();
			}
			return true;
		}

		protected void layoutSubControls() {
			SplitFormConfiguration controlCustomization = (SplitFormConfiguration) loadControlCustomization(input);
			if (controlCustomization.orientation == Orientation.HORIZONTAL_SPLIT) {
				setOrientation(ControlSplitPane.HORIZONTAL_SPLIT);
				setTopComponent(null);
				setBottomComponent(null);
				setLeftComponent(subControl1);
				setRightComponent(subControl2);
			} else if (controlCustomization.orientation == Orientation.VERTICAL_SPLIT) {
				setOrientation(ControlSplitPane.VERTICAL_SPLIT);
				setLeftComponent(null);
				setRightComponent(null);
				setTopComponent(subControl1);
				setBottomComponent(subControl2);
			} else {
				throw new ReflectionUIError();
			}
			SwingRendererUtils.handleComponentSizeChange(this);
		}

		protected EmbeddedFormControl createSubControl1() {
			return new EmbeddedFormControl(swingRenderer, new FieldControlInputProxy(input) {
				@Override
				public IFieldControlData getControlData() {
					return new FieldControlDataProxy(super.getControlData()) {

						@Override
						public String getCaption() {
							return "";
						}

						@Override
						public Object getValue() {
							return new PrecomputedTypeInstanceWrapper(super.getValue(), getSubObject1Type());
						}

						@Override
						public void setValue(Object value) {
							super.setValue(((PrecomputedTypeInstanceWrapper) value).getInstance());
						}

						@Override
						public Runnable getNextUpdateCustomUndoJob(Object newValue) {
							return super.getNextUpdateCustomUndoJob(
									((PrecomputedTypeInstanceWrapper) newValue).getInstance());
						}

						@Override
						public Runnable getPreviousUpdateCustomRedoJob(Object newValue) {
							return super.getPreviousUpdateCustomRedoJob(
									((PrecomputedTypeInstanceWrapper) newValue).getInstance());
						}

						@Override
						public ITypeInfo getType() {
							return swingRenderer.getReflectionUI().getTypeInfo(
									new PrecomputedTypeInstanceWrapper.TypeInfoSource(getSubObject1Type()));
						}

					};
				}
			});
		}

		protected EmbeddedFormControl createSubControl2() {
			return new EmbeddedFormControl(swingRenderer, new FieldControlInputProxy(input) {
				@Override
				public IFieldControlData getControlData() {
					return new FieldControlDataProxy(super.getControlData()) {

						@Override
						public String getCaption() {
							return "";
						}

						@Override
						public Object getValue() {
							return new PrecomputedTypeInstanceWrapper(super.getValue(), getSubObject2Type());
						}

						@Override
						public void setValue(Object value) {
							super.setValue(((PrecomputedTypeInstanceWrapper) value).getInstance());
						}

						@Override
						public Runnable getNextUpdateCustomUndoJob(Object newValue) {
							return super.getNextUpdateCustomUndoJob(
									((PrecomputedTypeInstanceWrapper) newValue).getInstance());
						}

						@Override
						public Runnable getPreviousUpdateCustomRedoJob(Object newValue) {
							return super.getPreviousUpdateCustomRedoJob(
									((PrecomputedTypeInstanceWrapper) newValue).getInstance());
						}

						@Override
						public ITypeInfo getType() {
							return swingRenderer.getReflectionUI().getTypeInfo(
									new PrecomputedTypeInstanceWrapper.TypeInfoSource(getSubObject2Type()));
						}

					};
				}
			});
		}

		protected ITypeInfo getSubObject1Type() {
			return new InfoProxyFactory() {

				@Override
				protected String getName(ITypeInfo type) {
					return "SplitFormType1 [base=" + super.getName(type) + "]";
				}

				@Override
				protected MenuModel getMenuModel(ITypeInfo type) {
					return new MenuModel();
				}

				@Override
				protected List<IFieldInfo> getFields(ITypeInfo type) {
					final SplitFormConfiguration controlCustomization = (SplitFormConfiguration) loadControlCustomization(
							input);
					List<IFieldInfo> result = new ArrayList<IFieldInfo>();
					int visibleFieldsCount = 0;
					for (IFieldInfo field : super.getFields(type)) {
						if (!field.isHidden()) {
							visibleFieldsCount++;
						}
						final int visibleFieldsCountAtPosition = visibleFieldsCount;
						result.add(new FieldInfoProxy(field) {
							@Override
							public boolean isHidden() {
								return super.isHidden()
										|| (visibleFieldsCountAtPosition > controlCustomization.firstLotFieldCount);
							}
						});
					}
					return result;
				}

				@Override
				protected List<IMethodInfo> getMethods(ITypeInfo type) {
					return Collections.emptyList();
				}

				@Override
				protected boolean onFormVisibilityChange(ITypeInfo type, Object object, boolean visible) {
					return false;
				}

				@Override
				protected void onFormRefresh(ITypeInfo type, Object object) {
				}

				@Override
				protected Runnable getLastFormRefreshStateRestorationJob(ITypeInfo type, Object object) {
					return null;
				}

				@Override
				protected boolean isFactoryTracedFor(ITypeInfo base) {
					return false;
				}

				@Override
				protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
					Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(type));
					result.put(ACTIVE_FACTORIES_KEY, null);
					return result;
				}

				@Override
				protected ITypeInfo getType(IFieldInfo field, ITypeInfo objectType) {
					return swingRenderer.getReflectionUI()
							.getTypeInfo(new TypeInfoSourceProxy(super.getType(field, objectType).getSource()) {

								@Override
								public SpecificitiesIdentifier getSpecificitiesIdentifier() {
									return new SpecificitiesIdentifier(getName(objectType), field.getName());
								}

								@Override
								protected String getTypeInfoProxyFactoryIdentifier() {
									return "TypeInfoSourceSpecificitiesIdentifierChangeFactory [of=" + getIdentifier()
											+ ", newSpecificitiesIdentifier=" + getSpecificitiesIdentifier() + "]";
								}

							});
				}

			}.wrapTypeInfo(data.getType());
		}

		protected ITypeInfo getSubObject2Type() {
			return new InfoProxyFactory() {

				@Override
				protected String getName(ITypeInfo type) {
					return "SplitFormType2 [base=" + super.getName(type) + "]";
				}

				@Override
				protected MenuModel getMenuModel(ITypeInfo type) {
					return super.getMenuModel(type);
				}

				@Override
				protected List<IFieldInfo> getFields(ITypeInfo type) {
					final SplitFormConfiguration controlCustomization = (SplitFormConfiguration) loadControlCustomization(
							input);
					List<IFieldInfo> result = new ArrayList<IFieldInfo>();
					int visibleFieldsCount = 0;
					for (IFieldInfo field : super.getFields(type)) {
						if (!field.isHidden()) {
							visibleFieldsCount++;
						}
						final int visibleFieldsCountAtPosition = visibleFieldsCount;
						result.add(new FieldInfoProxy(field) {
							@Override
							public boolean isHidden() {
								return super.isHidden()
										|| (visibleFieldsCountAtPosition <= controlCustomization.firstLotFieldCount);
							}
						});
					}
					return result;
				}

				@Override
				protected List<IMethodInfo> getMethods(ITypeInfo type) {
					return super.getMethods(type);
				}

				@Override
				protected boolean onFormVisibilityChange(ITypeInfo type, Object object, boolean visible) {
					return super.onFormVisibilityChange(type, object, visible);
				}

				@Override
				protected void onFormRefresh(ITypeInfo type, Object object) {
					super.onFormRefresh(type, object);
				}

				@Override
				protected Runnable getLastFormRefreshStateRestorationJob(ITypeInfo type, Object object) {
					return super.getLastFormRefreshStateRestorationJob(type, object);
				}

				@Override
				protected boolean isFactoryTracedFor(ITypeInfo base) {
					return false;
				}

				@Override
				protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
					Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(type));
					result.put(ACTIVE_FACTORIES_KEY, null);
					return result;
				}

				@Override
				protected ITypeInfo getType(IFieldInfo field, ITypeInfo objectType) {
					return swingRenderer.getReflectionUI()
							.getTypeInfo(new TypeInfoSourceProxy(super.getType(field, objectType).getSource()) {

								@Override
								public SpecificitiesIdentifier getSpecificitiesIdentifier() {
									return new SpecificitiesIdentifier(getName(objectType), field.getName());
								}

								@Override
								protected String getTypeInfoProxyFactoryIdentifier() {
									return "TypeInfoSourceSpecificitiesIdentifierChangeFactory [of=" + getIdentifier()
											+ ", newSpecificitiesIdentifier=" + getSpecificitiesIdentifier() + "]";
								}

							});
				}

			}.wrapTypeInfo(data.getType());
		}

		@Override
		public boolean displayError(String msg) {
			return false;
		}

		@Override
		public boolean showsCaption() {
			return true;
		}

		@Override
		public boolean isAutoManaged() {
			return true;
		}

		@Override
		public boolean requestCustomFocus() {
			if (SwingRendererUtils.requestAnyComponentFocus(subControl1, swingRenderer)) {
				return true;
			}
			if (SwingRendererUtils.requestAnyComponentFocus(subControl2, swingRenderer)) {
				return true;
			}
			return false;
		}

		@Override
		public void validateSubForms() throws Exception {
			subControl1.validateSubForms();
			subControl2.validateSubForms();
		}

		@Override
		public void addMenuContributions(MenuModel menuModel) {
			subControl1.addMenuContributions(menuModel);
			subControl2.addMenuContributions(menuModel);
		}

		@Override
		public String toString() {
			return "SplitForm [data=" + data + "]";
		}
	}

}
