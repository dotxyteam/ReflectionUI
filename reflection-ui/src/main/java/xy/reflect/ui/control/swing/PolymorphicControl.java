
package xy.reflect.ui.control.swing;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.border.Border;

import xy.reflect.ui.control.BufferedFieldControlData;
import xy.reflect.ui.control.CustomContext;
import xy.reflect.ui.control.ErrorHandlingFieldControlData;
import xy.reflect.ui.control.ErrorOccurrence;
import xy.reflect.ui.control.ErrorWithDefaultValue;
import xy.reflect.ui.control.FieldControlDataProxy;
import xy.reflect.ui.control.FieldControlInputProxy;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IContext;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.IFieldControlInput;
import xy.reflect.ui.control.RejectedFieldControlInputException;
import xy.reflect.ui.control.swing.builder.AbstractEditorFormBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.PolymorphicTypeOptionsFactory;
import xy.reflect.ui.info.type.factory.PolymorphicTypeOptionsFactory.RecursivePolymorphismDetectionException;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.undo.CancelledModificationException;
import xy.reflect.ui.undo.FieldControlDataModification;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.Mapper;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.Pair;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Field control that can display values of different types. It uses 2
 * sub-controls: an enumeration control to display possible types, and another
 * dynamic control to display the actual field value. Note that the constructor
 * throws a {@link RejectedFieldControlInputException} if it detects that it is
 * being recreated recursively inside the dynamic control.
 * 
 * @author olitank
 *
 */
public class PolymorphicControl extends ControlPanel implements IAdvancedFieldControl {

	protected static final long serialVersionUID = 1L;

	protected SwingRenderer swingRenderer;
	protected BufferedFieldControlData data;

	protected ITypeInfo polymorphicType;
	protected PolymorphicTypeOptionsFactory typeOptionsFactory;

	protected AbstractEditorFormBuilder typeEnumerationControlBuilder;
	protected AbstractEditorFormBuilder dynamicControlBuilder;
	protected Form dynamicControl;
	protected Form typeEnumerationControl;
	protected Map<ITypeInfo, Pair<AbstractEditorFormBuilder, Form>> dynamicControlCache = new HashMap<ITypeInfo, Pair<AbstractEditorFormBuilder, Form>>();

	protected ITypeInfo dynamicControlInstanceType;
	protected IFieldControlInput input;
	protected Map<ITypeInfo, Object> subTypeInstanceCache = new HashMap<ITypeInfo, Object>();
	protected Throwable currentError;

	public PolymorphicControl(final SwingRenderer swingRenderer, IFieldControlInput input) {
		try {
			this.swingRenderer = swingRenderer;
			input = new FieldControlInputProxy(input) {
				IFieldControlData errorHandlingFieldControlData = new ErrorHandlingFieldControlData(
						super.getControlData(), swingRenderer, null) {

					@Override
					protected void handleError(Throwable t) {
						currentError = t;
					}
				};
				BufferedFieldControlData bufferedFieldControlData = new BufferedFieldControlData(
						errorHandlingFieldControlData);

				@Override
				public IFieldControlData getControlData() {
					return bufferedFieldControlData;
				}
			};
			this.input = input;
			this.data = (BufferedFieldControlData) input.getControlData();
			this.polymorphicType = input.getControlData().getType();
			this.typeOptionsFactory = new PolymorphicTypeOptionsFactory(swingRenderer.getReflectionUI(),
					polymorphicType);
			setLayout(new BorderLayout());
			refreshUI(true);
		} catch (RecursivePolymorphismDetectionException e) {
			throw new RejectedFieldControlInputException();
		}
	}

	@Override
	public boolean refreshUI(boolean refreshStructure) {
		if (refreshStructure) {
			SwingRendererUtils.showFieldCaptionOnBorder(data, this, new Accessor<Border>() {
				@Override
				public Border get() {
					return new ControlPanel().getBorder();
				}
			}, swingRenderer);
			SwingRendererUtils.handleComponentSizeChange(this);
			subTypeInstanceCache.clear();
			dynamicControlCache.clear();
		}
		Object value = data.getValue();
		data.returningValue(value, new Runnable() {
			@Override
			public void run() {
				refreshTypeEnumerationControl(refreshStructure);
			}
		});
		data.returningValue(value, new Runnable() {
			@Override
			public void run() {
				refreshDynamicControl(refreshStructure);
			}
		});
		return true;
	}

	protected Form createTypeEnumerationControl() {
		Accessor<ITypeInfo> currentSubTypeAccessor = new Accessor<ITypeInfo>() {
			@Override
			public ITypeInfo get() {
				return getSubType(data.getValue());
			}
		};
		Listener<Object> dynamicControlUpdater = new Listener<Object>() {
			@Override
			public void handle(Object instance) {
				data.returningValue(instance, new Runnable() {
					@Override
					public void run() {
						refreshDynamicControl(false);
					}
				});
			}
		};
		Mapper<ITypeInfo, Object> instantiator = new Mapper<ITypeInfo, Object>() {
			@Override
			public Object get(ITypeInfo selectedSubType) {
				return swingRenderer.onTypeInstantiationRequest(PolymorphicControl.this, selectedSubType);
			}
		};
		Listener<Throwable> commitExceptionHandler = new Listener<Throwable>() {
			@Override
			public void handle(Throwable t) {
				swingRenderer.handleException(PolymorphicControl.this, t);
				refreshTypeEnumerationControl(false);
			}
		};
		typeEnumerationControlBuilder = new TypeEnumerationControlBuilder(swingRenderer, this, input,
				typeOptionsFactory, currentSubTypeAccessor, dynamicControlUpdater, subTypeInstanceCache, instantiator,
				commitExceptionHandler);
		return typeEnumerationControlBuilder.createEditorForm(true, false);
	}

	protected ITypeInfo getSubType(Object instance) {
		if (instance == null) {
			return null;
		}
		ITypeInfo result = MiscUtils.getFirstKeyFromValue(subTypeInstanceCache, instance);
		if (result == null) {
			result = typeOptionsFactory.guessSubType(instance);
			if (result == null) {
				throw new ReflectionUIError("Failed to find a compatible sub-type for '" + instance
						+ "'. Sub-type options: " + typeOptionsFactory.getTypeOptions());
			}
			subTypeInstanceCache.put(result, instance);
		}
		return result;
	}

	protected void refreshTypeEnumerationControl(boolean refreshStructure) {
		if (typeEnumerationControl != null) {
			typeEnumerationControlBuilder.reloadValue(typeEnumerationControl, refreshStructure);
		} else {
			add(typeEnumerationControl = createTypeEnumerationControl(), BorderLayout.NORTH);
			SwingRendererUtils.handleComponentSizeChange(this);
		}
	}

	protected String getEnumerationValueCaption(ITypeInfo actualFieldValueType) {
		return actualFieldValueType.getCaption();
	}

	protected Form createDynamicControl(final ITypeInfo instanceType) {
		Listener<Throwable> commitExceptionHandler = new Listener<Throwable>() {
			@Override
			public void handle(Throwable t) {
				throw new ReflectionUIError(t);
			}
		};
		dynamicControlBuilder = new DynamicControlBuilder(swingRenderer, this, input, PolymorphicTypeOptionsFactory
				.preventPolymorphismRecursivity(swingRenderer.getReflectionUI(), polymorphicType),
				commitExceptionHandler);
		return dynamicControlBuilder.createEditorForm(true, false);
	}

	protected void refreshDynamicControl(boolean refreshStructure) {
		Object instance = data.getValue();
		ITypeInfo instanceType = getSubType(instance);
		if ((dynamicControlInstanceType == null) && (instanceType == null)) {
			// no dynamic control
			return;
		} else if ((dynamicControlInstanceType != null) && (instanceType == null)) {
			// hide dynamic control
			remove(dynamicControl);
			dynamicControl = null;
			dynamicControlInstanceType = null;
			SwingRendererUtils.handleComponentSizeChange(this);
		} else if ((instanceType != null) && instanceType.equals(dynamicControlInstanceType)) {
			// refresh dynamic control
			if (currentError != null) {
				// display the current error (over the last valid instance value)
				instance = new ErrorOccurrence(new ErrorWithDefaultValue(currentError, instance));
			}
			data.returningValue(instance, new Runnable() {
				@Override
				public void run() {
					dynamicControlBuilder.reloadValue(dynamicControl, refreshStructure);
				}
			});
		} else {
			// show/replace dynamic control
			if (dynamicControlInstanceType != null) {
				remove(dynamicControl);
			}
			Pair<AbstractEditorFormBuilder, Form> dynamicControlAndBuilder = dynamicControlCache.get(instanceType);
			if (dynamicControlAndBuilder != null) {
				dynamicControlBuilder = dynamicControlAndBuilder.getFirst();
				dynamicControl = dynamicControlAndBuilder.getSecond();
				if (currentError != null) {
					// display the current error (over the last valid instance value)
					instance = new ErrorOccurrence(new ErrorWithDefaultValue(currentError, instance));
				}
				data.returningValue(instance, new Runnable() {
					@Override
					public void run() {
						dynamicControlBuilder.reloadValue(dynamicControl, refreshStructure);
					}
				});
			} else {
				data.returningValue(instance, new Runnable() {
					@Override
					public void run() {
						dynamicControl = createDynamicControl(instanceType);
					}
				});
				dynamicControlCache.put(instanceType,
						new Pair<AbstractEditorFormBuilder, Form>(dynamicControlBuilder, dynamicControl));
				if (currentError != null) {
					// display the current error (over the last valid instance value)
					data.returningValue(new ErrorOccurrence(currentError), new Runnable() {
						@Override
						public void run() {
							dynamicControlBuilder.reloadValue(dynamicControl, refreshStructure);
						}
					});
				}
			}
			dynamicControlInstanceType = instanceType;
			add(dynamicControl, BorderLayout.CENTER);
			SwingRendererUtils.handleComponentSizeChange(this);
		}
	}

	@Override
	public boolean showsCaption() {
		return true;
	}

	@Override
	public boolean displayError(Throwable error) {
		return false;
	}

	@Override
	public boolean isAutoManaged() {
		return true;
	}

	@Override
	public boolean requestCustomFocus() {
		if (SwingRendererUtils.requestAnyComponentFocus(typeEnumerationControl, swingRenderer)) {
			return true;
		}
		if (dynamicControl != null) {
			if (SwingRendererUtils.requestAnyComponentFocus(dynamicControl, swingRenderer)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void validateControlData(ValidationSession session) throws Exception {
		if (dynamicControl != null) {
			dynamicControl.validateForm(session);
		}
	}

	@Override
	public void addMenuContributions(MenuModel menuModel) {
		if (dynamicControl != null) {
			dynamicControl.addMenuContributionTo(menuModel);
		}
	}

	protected static class TypeEnumerationControlBuilder extends AbstractEditorFormBuilder {

		protected SwingRenderer swingRenderer;
		protected PolymorphicControl ownerComponent;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected PolymorphicTypeOptionsFactory typeOptionsFactory;
		protected Listener<Object> dynamicControlUpdater;
		protected Map<ITypeInfo, Object> subTypeInstanceCache;
		protected Mapper<ITypeInfo, Object> instantiator;
		protected Listener<Throwable> commitExceptionHandler;

		public TypeEnumerationControlBuilder(SwingRenderer swingRenderer, PolymorphicControl ownerComponent,
				IFieldControlInput input, PolymorphicTypeOptionsFactory typeOptionsFactory,
				Accessor<ITypeInfo> currentSubTypeAccessor, Listener<Object> dynamicControlUpdater,
				Map<ITypeInfo, Object> subTypeInstanceCache, Mapper<ITypeInfo, Object> instantiator,
				Listener<Throwable> commitExceptionHandler) {
			this.swingRenderer = swingRenderer;
			this.ownerComponent = ownerComponent;
			input = new FieldControlInputProxy(input) {
				IFieldControlData fieldControlDataProxy = new FieldControlDataProxy(super.getControlData()) {

					@Override
					public Object getValue() {
						return typeOptionsFactory.getItemInstance(currentSubTypeAccessor.get());
					}

					@Override
					public void setValue(Object newValue) {
						ITypeInfo selectedSubType = (newValue == null) ? null
								: (ITypeInfo) typeOptionsFactory.getInstanceItem(newValue);
						Object instance;
						if (selectedSubType == null) {
							instance = null;
						} else {
							instance = subTypeInstanceCache.get(selectedSubType);
							if (instance == null) {
								instance = instantiator.get(selectedSubType);
								if (instance == null) {
									throw new CancelledModificationException();
								}
							}
						}
						try {
							dynamicControlUpdater.handle(instance);
						} catch (Throwable t) {
							commitExceptionHandler.handle(t);
							throw new CancelledModificationException();
						}
						super.setValue(instance);
					}

					@Override
					public ITypeInfo getType() {
						return swingRenderer.getReflectionUI()
								.getTypeInfo(typeOptionsFactory.getInstanceTypeInfoSource(null));
					}

				};

				@Override
				public IFieldControlData getControlData() {
					return fieldControlDataProxy;
				}
			};
			this.input = input;
			this.data = input.getControlData();
			this.typeOptionsFactory = typeOptionsFactory;
			this.dynamicControlUpdater = dynamicControlUpdater;
			this.subTypeInstanceCache = subTypeInstanceCache;
			this.instantiator = instantiator;
			this.commitExceptionHandler = commitExceptionHandler;
		}

		@Override
		protected IContext getContext() {
			return input.getContext();
		}

		@Override
		protected IContext getSubContext() {
			return null;
		}

		@Override
		protected boolean isEncapsulatedFormEmbedded() {
			return false;
		}

		@Override
		protected boolean isEncapsulatedisControlValueValiditionEnabled() {
			return false;
		}

		@Override
		protected boolean isNullValueDistinct() {
			return data.isNullValueDistinct();
		}

		@Override
		protected Object loadValue() {
			return data.getValue();
		}

		@Override
		protected ITypeInfoSource getEncapsulatedFieldDeclaredTypeSource() {
			return typeOptionsFactory.getInstanceTypeInfoSource(null);
		}

		@Override
		protected ValueReturnMode getReturnModeFromParent() {
			return ValueReturnMode.CALCULATED;
		}

		@Override
		protected boolean canCommitToParent() {
			return !data.isGetOnly();
		}

		@Override
		protected boolean shouldIntegrateNewObjectValue(Object value) {
			return true;
		}

		@Override
		protected IModification createCommittingModification(Object value) {
			return new FieldControlDataModification(data, value);
		}

		@Override
		protected IModification createUndoModificationsReplacement() {
			return ReflectionUIUtils.createUndoModificationsReplacement(data);
		}

		/**
		 * Should never be called since the {@link FieldControlInputProxy} above catches
		 * the exceptions.
		 */
		@Override
		protected void handleRealtimeLinkCommitException(Throwable t) {
			commitExceptionHandler.handle(new ReflectionUIError("Unexpected: " + t, t));
		}

		@Override
		public SwingRenderer getSwingRenderer() {
			return swingRenderer;
		}

		@Override
		protected Runnable getParentControlRefreshJob() {
			return new Runnable() {
				@Override
				public void run() {
					ownerComponent.refreshUI(false);
				}
			};
		}

		@Override
		protected String getParentModificationTitle() {
			return FieldControlDataModification.getTitle(data.getCaption());
		}

		@Override
		protected boolean isParentModificationVolatile() {
			return data.isTransient();
		}

		@Override
		protected IInfoFilter getEncapsulatedFormFilter() {
			return IInfoFilter.DEFAULT;
		}

		@Override
		protected ModificationStack getParentModificationStack() {
			return input.getModificationStack();
		}

	}

	protected static class DynamicControlBuilder extends AbstractEditorFormBuilder {

		protected SwingRenderer swingRenderer;
		protected PolymorphicControl ownerComponent;
		protected IFieldControlInput input;
		protected IFieldControlData data;
		protected ITypeInfo instanceType;
		protected Listener<Throwable> commitExceptionHandler;

		public DynamicControlBuilder(SwingRenderer swingRenderer, PolymorphicControl ownerComponent,
				IFieldControlInput input, ITypeInfo instanceType, Listener<Throwable> commitExceptionHandler) {
			this.swingRenderer = swingRenderer;
			this.ownerComponent = ownerComponent;
			this.input = input;
			this.data = input.getControlData();
			this.instanceType = instanceType;
			this.commitExceptionHandler = commitExceptionHandler;
		}

		@Override
		protected IContext getContext() {
			return input.getContext();
		}

		@Override
		protected IContext getSubContext() {
			return new CustomContext("PolymorphicInstance");
		}

		@Override
		protected boolean isEncapsulatedFormEmbedded() {
			return data.isFormControlEmbedded();
		}

		@Override
		protected boolean isEncapsulatedisControlValueValiditionEnabled() {
			return data.isControlValueValiditionEnabled();
		}

		@Override
		protected boolean isNullValueDistinct() {
			return false;
		}

		@Override
		protected boolean canCommitToParent() {
			return !data.isGetOnly();
		}

		@Override
		protected IModification createCommittingModification(Object newObjectValue) {
			return new FieldControlDataModification(data, newObjectValue);
		}

		@Override
		protected IModification createUndoModificationsReplacement() {
			return ReflectionUIUtils.createUndoModificationsReplacement(data);
		}

		@Override
		protected void handleRealtimeLinkCommitException(Throwable t) {
			commitExceptionHandler.handle(t);
		}

		@Override
		public SwingRenderer getSwingRenderer() {
			return swingRenderer;
		}

		@Override
		protected Runnable getParentControlRefreshJob() {
			return new Runnable() {
				@Override
				public void run() {
					ownerComponent.refreshUI(false);
				}
			};
		}

		@Override
		protected ValueReturnMode getReturnModeFromParent() {
			return data.getValueReturnMode();
		}

		@Override
		protected String getParentModificationTitle() {
			return FieldControlDataModification.getTitle(data.getCaption());
		}

		@Override
		protected boolean isParentModificationVolatile() {
			return data.isTransient();
		}

		@Override
		protected IInfoFilter getEncapsulatedFormFilter() {
			IInfoFilter result = data.getFormControlFilter();
			if (result == null) {
				result = IInfoFilter.DEFAULT;
			}
			return result;
		}

		@Override
		protected ITypeInfoSource getEncapsulatedFieldDeclaredTypeSource() {
			return instanceType.getSource();
		}

		@Override
		protected ModificationStack getParentModificationStack() {
			return input.getModificationStack();
		}

		@Override
		protected Object loadValue() {
			return data.getValue();
		}
	}

}
