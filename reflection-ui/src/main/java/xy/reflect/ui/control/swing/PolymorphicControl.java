package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import xy.reflect.ui.control.CustomContext;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.swing.editor.AbstractEditorFormBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.PolymorphicTypeOptionsFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.undo.ControlDataValueModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationProxy;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.ControlPanel;

public class PolymorphicControl extends ControlPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;
	protected SwingRenderer swingRenderer;
	protected IFieldControlData data;

	protected ITypeInfo polymorphicType;
	protected PolymorphicTypeOptionsFactory typeOptionsFactory;

	protected AbstractEditorFormBuilder typeEnumerationControlBuilder;
	protected AbstractEditorFormBuilder dynamicControlBuilder;
	protected Form dynamicControl;
	protected Form typeEnumerationControl;

	protected ITypeInfo lastInstanceType;
	protected IFieldControlInput input;

	public PolymorphicControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		this.swingRenderer = swingRenderer;
		this.input = new FieldControlInputProxy(input) {
			@Override
			public IFieldControlData getControlData() {
				IFieldControlData result = super.getControlData();
				result = SwingRendererUtils.handleErrors(swingRenderer, result, PolymorphicControl.this);
				return result;
			}
		};
		this.data = input.getControlData();
		this.polymorphicType = input.getControlData().getType();
		this.typeOptionsFactory = new PolymorphicTypeOptionsFactory(swingRenderer.getReflectionUI(), polymorphicType);

		setLayout(new BorderLayout());
		refreshUI(true);
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		if (refreshStructure) {
			if (data.getCaption().length() > 0) {
				setBorder(BorderFactory.createTitledBorder(swingRenderer.prepareStringToDisplay(data.getCaption())));
				if (data.getForegroundColor() != null) {
					((TitledBorder) getBorder()).setTitleColor(SwingRendererUtils.getColor(data.getForegroundColor()));
				}
				if (data.getBorderColor() != null) {
					((TitledBorder) getBorder()).setBorder(
							BorderFactory.createLineBorder(SwingRendererUtils.getColor(data.getBorderColor())));
				}
			} else {
				setBorder(null);
			}
		}
		refreshTypeEnumerationControl(refreshStructure);
		refreshDynamicControl(refreshStructure);
		return true;
	}

	protected Form createTypeEnumerationControl() {
		typeEnumerationControlBuilder = new AbstractEditorFormBuilder() {

			Map<ITypeInfo, Object> instanceByEnumerationValueCache = new HashMap<ITypeInfo, Object>();

			@Override
			public IContext getContext() {
				return input.getContext();
			}

			@Override
			public IContext getSubContext() {
				return null;
			}

			@Override
			public boolean isObjectFormExpanded() {
				return false;
			}

			@Override
			public boolean isObjectNullValueDistinct() {
				return data.isNullValueDistinct();
			}

			@Override
			public Object getInitialObjectValue() {
				Object instance = data.getValue();
				if (instance == null) {
					return null;
				}
				ITypeInfo selectedType = ReflectionUIUtils.getFirstKeyFromValue(instanceByEnumerationValueCache,
						instance);
				if (selectedType == null) {
					selectedType = typeOptionsFactory.guessSubType(instance);
					instanceByEnumerationValueCache.put(selectedType, instance);
				}
				return typeOptionsFactory.getInstance(selectedType);
			}

			@Override
			public ITypeInfoSource getObjectDeclaredNonSpecificTypeInfoSource() {
				return typeOptionsFactory.getInstanceTypeInfoSource(null);
			}

			@Override
			public ValueReturnMode getObjectValueReturnMode() {
				return ValueReturnMode.CALCULATED;
			}

			@Override
			public boolean canCommit() {
				return !data.isGetOnly();
			}

			@Override
			protected boolean shouldAcceptNewObjectValue(Object value) {
				Object instance;
				if (value != null) {
					ITypeInfo selectedSubType = (ITypeInfo) typeOptionsFactory.unwrapInstance(value);
					instance = instanceByEnumerationValueCache.get(selectedSubType);
					if (instance == null) {
						try {
							instance = data.createValue(selectedSubType, true);
						} catch (Throwable t) {
							instance = null;
						}
						if (instance == null) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									refreshTypeEnumerationControl(false);
								}
							});
							return false;
						}
						instanceByEnumerationValueCache.put(selectedSubType, instance);
					}
				}
				return true;
			}

			@Override
			public IModification createCommitModification(final Object value) {

				return new ModificationProxy(IModification.NULL_MODIFICATION) {

					@Override
					public String toString() {
						return "CommitModification [editor=PolymorphicControlEnumeration, data=" + data + "]";
					}

					@Override
					public IModification applyAndGetOpposite() {
						Object instance;
						if (value == null) {
							instance = null;
							instanceByEnumerationValueCache.clear();
						} else {
							ITypeInfo selectedSubType = (ITypeInfo) typeOptionsFactory.unwrapInstance(value);
							instance = instanceByEnumerationValueCache.get(selectedSubType);
							if (instance == null) {
								throw new ReflectionUIError();
							}
						}
						return new ControlDataValueModification(new FieldControlDataProxy(data) {
							@Override
							public void setValue(Object value) {
								try {
									super.setValue(value);
								} finally {
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											refreshDynamicControl(false);
										}
									});
								}
							}
						}, instance).applyAndGetOpposite();
					}
				};
			}

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			public String getCumulatedModificationsTitle() {
				return ControlDataValueModification.getTitle(data.getCaption());
			}

			@Override
			public IInfoFilter getObjectFormFilter() {
				return IInfoFilter.DEFAULT;
			}

			@Override
			public ModificationStack getParentObjectModificationStack() {
				return input.getModificationStack();
			}

		};
		return typeEnumerationControlBuilder.createForm(true, false);
	}

	protected void refreshTypeEnumerationControl(boolean refreshStructure) {
		if (typeEnumerationControl != null) {
			typeEnumerationControlBuilder.refreshEditorForm(typeEnumerationControl, refreshStructure);
		} else {
			add(typeEnumerationControl = createTypeEnumerationControl(), BorderLayout.NORTH);
			SwingRendererUtils.handleComponentSizeChange(this);
		}
	}

	protected String getEnumerationValueCaption(ITypeInfo actualFieldValueType) {
		return actualFieldValueType.getCaption();
	}

	protected Form createDynamicControl(final ITypeInfo instanceType) {
		dynamicControlBuilder = new AbstractEditorFormBuilder() {

			@Override
			public IContext getContext() {
				return input.getContext();
			}

			@Override
			public IContext getSubContext() {
				return new CustomContext("PolymorphicInstance");
			}

			@Override
			public boolean isObjectFormExpanded() {
				return false;
			}

			@Override
			public boolean isObjectNullValueDistinct() {
				return false;
			}

			@Override
			public boolean canCommit() {
				return !data.isGetOnly();
			}

			@Override
			public IModification createCommitModification(Object newObjectValue) {
				return new ControlDataValueModification(data, newObjectValue);
			}

			@Override
			public SwingRenderer getSwingRenderer() {
				return swingRenderer;
			}

			@Override
			public ValueReturnMode getObjectValueReturnMode() {
				return data.getValueReturnMode();
			}

			@Override
			public String getCumulatedModificationsTitle() {
				return ControlDataValueModification.getTitle(data.getCaption());
			}

			@Override
			public IInfoFilter getObjectFormFilter() {
				IInfoFilter result = data.getFormControlFilter();
				if (result == null) {
					result = IInfoFilter.DEFAULT;
				}
				return result;
			}

			@Override
			public ITypeInfoSource getObjectDeclaredNonSpecificTypeInfoSource() {
				return instanceType.getSource();
			}

			@Override
			public ModificationStack getParentObjectModificationStack() {
				return input.getModificationStack();
			}

			@Override
			public Object getInitialObjectValue() {
				return data.getValue();
			}

		};
		return dynamicControlBuilder.createForm(true, false);
	}

	protected void refreshDynamicControl(boolean refreshStructure) {
		ITypeInfo instanceType = (ITypeInfo) typeOptionsFactory
				.unwrapInstance(typeEnumerationControlBuilder.getCurrentObjectValue());
		if ((lastInstanceType == null) && (instanceType == null)) {
			return;
		} else if ((lastInstanceType != null) && (instanceType == null)) {
			remove(dynamicControl);
			dynamicControl = null;
			SwingRendererUtils.handleComponentSizeChange(this);
		} else if ((lastInstanceType == null) && (instanceType != null)) {
			dynamicControl = createDynamicControl(instanceType);
			add(dynamicControl, BorderLayout.CENTER);
			SwingRendererUtils.handleComponentSizeChange(this);
		} else {
			if (lastInstanceType.equals(instanceType)) {
				dynamicControlBuilder.refreshEditorForm(dynamicControl, refreshStructure);
			} else {
				remove(dynamicControl);
				dynamicControl = null;
				dynamicControl = createDynamicControl(instanceType);
				add(dynamicControl, BorderLayout.CENTER);
				SwingRendererUtils.handleComponentSizeChange(this);
			}
		}
		lastInstanceType = instanceType;
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	@Override
	public boolean displayError(String msg) {
		return false;
	}

	@Override
	public boolean isAutoManaged() {
		return true;
	}

	@Override
	public boolean requestCustomFocus() {
		return SwingRendererUtils.requestAnyComponentFocus(typeEnumerationControl, swingRenderer);
	}

	@Override
	public void validateSubForm() throws Exception {
		if (dynamicControl != null) {
			dynamicControl.validateForm();
		}
	}

	@Override
	public void addMenuContribution(MenuModel menuModel) {
		if (dynamicControl != null) {
			dynamicControl.addMenuContribution(menuModel);
		}
	}

	@Override
	public long getDataUpdateDelayMilliseconds() {
		return 0;
	}

	@Override
	public String toString() {
		return "PolymorphicControl [data=" + data + "]";
	}

}
