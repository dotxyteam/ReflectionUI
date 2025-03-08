
package xy.reflect.ui.control.swing.plugin;

import java.util.Collections;
import java.util.List;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.EmbeddedFormControl;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlSplitPane;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
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
				Class<?> javaType;
				try {
					javaType = ClassUtils.getCachedClassForName(fieldType.getName());
				} catch (ClassNotFoundException e) {
					return true;
				}
				if (!(fieldType instanceof IEnumerationTypeInfo)
						&& !ReflectionUIUtils.hasPolymorphicInstanceSubTypes(fieldType)
						&& !ClassUtils.isPrimitiveClassOrWrapperOrString(javaType)) {
					return true;
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
		public boolean refreshUI(boolean refreshStructure) {
			SplitFormConfiguration controlCustomization = (SplitFormConfiguration) loadControlCustomization(input);
			if (refreshStructure) {
				if (controlCustomization.orientation == Orientation.HORIZONTAL_SPLIT) {
					setOrientation(ControlSplitPane.HORIZONTAL_SPLIT);
				} else if (controlCustomization.orientation == Orientation.VERTICAL_SPLIT) {
					setOrientation(ControlSplitPane.VERTICAL_SPLIT);
				} else {
					throw new ReflectionUIError();
				}
				SwingRendererUtils.ensureDividerLocation(this, controlCustomization.defaultDividerLocation);
				SwingRendererUtils.showFieldCaptionOnBorder(data, this, swingRenderer);
				SwingRendererUtils.handleComponentSizeChange(this);
			}
			if ((subControl1 == null) || !subControl1.refreshUI(refreshStructure)) {
				setLeftComponent(subControl1 = createSubControl1());
				forwardRefresh(subControl1, subControl2);
				SwingRendererUtils.handleComponentSizeChange(this);
			}
			if ((subControl2 == null) || !subControl2.refreshUI(refreshStructure)) {
				setRightComponent(subControl2 = createSubControl2());
				forwardRefresh(subControl2, subControl1);
				SwingRendererUtils.handleComponentSizeChange(this);
			}
			return true;
		}

		protected void forwardRefresh(EmbeddedFormControl control, EmbeddedFormControl otherControl) {
			control.getSubForm().getRefreshListeners().add(new Form.IRefreshListener() {
				@Override
				public void onRefresh(boolean refreshStructure) {
					if (refreshListenersDisabled) {
						return;
					}
					if (otherControl != null) {
						refreshListenersDisabled = true;
						try {
							otherControl.refreshUI(refreshStructure);
						} finally {
							refreshListenersDisabled = false;
						}
					}
				}
			});
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
					List<IFieldInfo> result = super.getFields(type);
					SplitFormConfiguration controlCustomization = (SplitFormConfiguration) loadControlCustomization(
							input);
					if (result.size() < controlCustomization.firstLotFieldCount) {
						throw new ReflectionUIError("Invalid divider position for " + getControlTitle() + ": "
								+ controlCustomization.firstLotFieldCount);
					}
					result = result.subList(0, controlCustomization.firstLotFieldCount);
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

			}.wrapTypeInfo(data.getType().getSource().buildTypeInfo(swingRenderer.getReflectionUI()));
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
					List<IFieldInfo> result = super.getFields(type);
					SplitFormConfiguration controlCustomization = (SplitFormConfiguration) loadControlCustomization(
							input);
					if (result.size() < controlCustomization.firstLotFieldCount) {
						throw new ReflectionUIError("Invalid divider position for " + getControlTitle() + ": "
								+ controlCustomization.firstLotFieldCount);
					}
					result = result.subList(controlCustomization.firstLotFieldCount, result.size());
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

			}.wrapTypeInfo(data.getType().getSource().buildTypeInfo(swingRenderer.getReflectionUI()));
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
