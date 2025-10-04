
package xy.reflect.ui.control.swing.plugin;

import java.awt.Component;
import java.awt.Dimension;
import java.io.Serializable;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.plugin.AbstractSimpleCustomizableFieldControlPlugin;
import xy.reflect.ui.control.swing.EmbeddedFormControl;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlScrollPane;
import xy.reflect.ui.control.swing.util.ControlSplitPane;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.filter.InfoFilterProxy;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;

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
	public boolean handles(ReflectionUI reflectionUI, IFieldControlInput input) {
		IFieldControlData data = input.getControlData();
		if (!data.isNullValueDistinct()) {
			ITypeInfo fieldType = data.getType();
			if (fieldType.getFields().size() >= 2) {
				if (data.isFormControlEmbedded()) {
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
		public ControlDimensionSpecification width;
		public ControlDimensionSpecification height;
	}

	public enum Orientation {
		HORIZONTAL_SPLIT, VERTICAL_SPLIT
	}

	public enum ControlSizeUnit {
		PIXELS, SCREEN_PERCENT
	}

	public static class ControlDimensionSpecification implements Serializable {

		private static final long serialVersionUID = 1L;

		public int value = 500;
		public ControlSizeUnit unit = ControlSizeUnit.PIXELS;

	}

	public class SplitForm extends ControlSplitPane implements IAdvancedFieldControl {
		private static final long serialVersionUID = 1L;

		protected SwingRenderer swingRenderer;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected EmbeddedFormControl subControl1;
		protected EmbeddedFormControl subControl2;
		protected boolean refreshListenersDisabled = false;
		protected SplitFormConfiguration controlConfiguration;

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
			}
			if (controlConfiguration.width != null) {
				if (controlConfiguration.width.unit == ControlSizeUnit.PIXELS) {
					result.width = controlConfiguration.width.value;
				} else if (controlConfiguration.width.unit == ControlSizeUnit.SCREEN_PERCENT) {
					Dimension screenSize = MiscUtils.getDefaultScreenSize();
					result.width = Math.round((controlConfiguration.width.value / 100f) * screenSize.width);
				} else {
					throw new ReflectionUIError();
				}
			}
			if (controlConfiguration.height != null) {
				if (controlConfiguration.height.unit == ControlSizeUnit.PIXELS) {
					result.height = controlConfiguration.height.value;
				} else if (controlConfiguration.height.unit == ControlSizeUnit.SCREEN_PERCENT) {
					Dimension screenSize = MiscUtils.getDefaultScreenSize();
					result.height = Math.round((controlConfiguration.height.value / 100f) * screenSize.height);
				} else {
					throw new ReflectionUIError();
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
			if (refreshStructure) {
				updateControlConfiguration();
				SwingRendererUtils.showFieldCaptionOnBorder(data, this, new Accessor<Border>() {
					@Override
					public Border get() {
						if (data.getBorderColor() != null) {
							return BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor()));
						} else {
							return new ControlSplitPane().getBorder();
						}
					}
				}, swingRenderer);
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

		protected void updateControlConfiguration() {
			controlConfiguration = (SplitFormConfiguration) loadControlCustomization(input);
		}

		protected void layoutSubControls() {
			if (controlConfiguration.orientation == Orientation.HORIZONTAL_SPLIT) {
				setOrientation(ControlSplitPane.HORIZONTAL_SPLIT);
				setTopComponent(null);
				setBottomComponent(null);
				setLeftComponent(createSubControlScrollPane(subControl1));
				setRightComponent(createSubControlScrollPane(subControl2));
			} else if (controlConfiguration.orientation == Orientation.VERTICAL_SPLIT) {
				setOrientation(ControlSplitPane.VERTICAL_SPLIT);
				setLeftComponent(null);
				setRightComponent(null);
				setTopComponent(createSubControlScrollPane(subControl1));
				setBottomComponent(createSubControlScrollPane(subControl2));
			} else {
				throw new ReflectionUIError();
			}
			validate();
			SwingRendererUtils.ensureDividerLocation(this, controlConfiguration.defaultDividerLocation);
			setResizeWeight(controlConfiguration.defaultDividerLocation);
			SwingRendererUtils.handleComponentSizeChange(this);
		}

		protected JScrollPane createSubControlScrollPane(Component content) {
			ControlScrollPane result = new ControlScrollPane(content);
			SwingRendererUtils.removeScrollPaneBorder(result);
			return result;
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
						public IInfoFilter getFormControlFilter() {
							return new InfoFilterProxy(super.getFormControlFilter()) {

								@Override
								public IFieldInfo apply(IFieldInfo field) {
									if (getType().getFields().stream()
											.filter(f -> !(f.isHidden() || (super.apply(f) == null)))
											.skip(controlConfiguration.firstLotFieldCount)
											.anyMatch(f -> f.getName().equals(field.getName()))) {
										return null;
									}
									return field;
								}

								@Override
								public IMethodInfo apply(IMethodInfo method) {
									return null;
								}
							};
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
						public IInfoFilter getFormControlFilter() {
							return new InfoFilterProxy(super.getFormControlFilter()) {

								@Override
								public IFieldInfo apply(IFieldInfo field) {
									if (getType().getFields().stream()
											.filter(f -> !(f.isHidden() || (super.apply(f) == null)))
											.limit(controlConfiguration.firstLotFieldCount)
											.anyMatch(f -> f.getName().equals(field.getName()))) {
										return null;
									}
									return field;
								}

								@Override
								public IMethodInfo apply(IMethodInfo method) {
									return method;
								}
							};
						}

					};
				}
			});
		}

		@Override
		public boolean displayError(Throwable error) {
			return false;
		}

		@Override
		public boolean showsCaption() {
			return true;
		}

		@Override
		public boolean isModificationStackManaged() {
			return true;
		}

		@Override
		public boolean areValueAccessErrorsManaged() {
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
		public void validateControlData(ValidationSession session) throws Exception {
			subControl1.validateControlData(session);
			subControl2.validateControlData(session);
		}

		@Override
		public void addMenuContributions(MenuModel menuModel) {
			subControl1.addMenuContributions(menuModel);
		}

	}

}
