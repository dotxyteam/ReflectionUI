package xy.reflect.ui.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.HiddenNullableFacetFieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.InfoCategory;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

public class PolymorphicEmbeddedForm extends JPanel implements
		IRefreshableControl, ICanShowCaptionControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;
	protected List<ITypeInfo> polyTypes;
	protected Map<ITypeInfo, Object> instanceByEnumerationValueCache = new HashMap<ITypeInfo, Object>();
	protected Component dynamicControl;
	protected EnumerationControl typeEnumerationControl;
	protected ITypeInfo type;

	protected final ITypeInfo NULL_POLY_TYPE = new DefaultTypeInfo(reflectionUI, Object.class){

		@Override
		public String getCaption() {
			return "<Choose an option>";
		}
		
	};
	
	public PolymorphicEmbeddedForm(final ReflectionUI reflectionUI,
			final Object object, final IFieldInfo field) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		this.type = field.getType();
		this.polyTypes = type.getPolymorphicInstanceSubTypes();

		setLayout(new BorderLayout());

		updateTypeEnumerationControl();
		updateDynamicControl();
	}

	protected EnumerationControl createTypeEnumerationControl() {
		return new EnumerationControl(reflectionUI, object, new IFieldInfo() {

			@Override
			public String getName() {
				return "";
			}

			@Override
			public String getCaption() {
				return null;
			}

			@Override
			public Object getValue(Object object) {
				Object instance = field.getValue(object);
				if (instance == null) {
					return NULL_POLY_TYPE;
				} else {
					ITypeInfo actualFieldValueType = reflectionUI
							.getTypeInfo(reflectionUI
									.getTypeInfoSource(instance));
					return actualFieldValueType;
				}
			}

			@Override
			public void setValue(Object object, Object value) {
				try {
					if (value == NULL_POLY_TYPE) {
						field.setValue(object, null);
					} else {
						ITypeInfo selectedPolyType = null;
						for (ITypeInfo polyType : polyTypes) {
							if (value
									.equals(polyType)) {
								selectedPolyType = polyType;
								break;
							}
						}
						Object instance = instanceByEnumerationValueCache
								.get(selectedPolyType);
						if (instance == null) {
							instance = reflectionUI.onTypeInstanciationRequest(
									PolymorphicEmbeddedForm.this,
									selectedPolyType, true, false);
							if (instance == null) {
								return;
							}
							instanceByEnumerationValueCache.put(
									selectedPolyType, instance);
						}
						field.setValue(object, instance);
					}
				} finally {
					refreshUI();
				}
			}

			@Override
			public boolean isReadOnly() {
				return false;
			}

			@Override
			public boolean isNullable() {
				return false;
			}

			@Override
			public InfoCategory getCategory() {
				return null;
			}

			@Override
			public ITypeInfo getType() {
				return new IEnumerationTypeInfo() {

					@Override
					public String getName() {
						return "";
					}

					@Override
					public String getCaption() {
						return "";
					}

					@Override
					public boolean supportsValue(Object value) {
						return value instanceof String;
					}

					@Override
					public boolean isConcrete() {
						return true;
					}

					@Override
					public List<ITypeInfo> getPolymorphicInstanceSubTypes() {
						return null;
					}

					@Override
					public List<IMethodInfo> getMethods() {
						return Collections.emptyList();
					}

					@Override
					public List<IFieldInfo> getFields() {
						return Collections.emptyList();
					}

					@Override
					public List<IMethodInfo> getConstructors() {
						return Collections.emptyList();
					}

					@Override
					public Component createFieldControl(Object object,
							IFieldInfo field) {
						throw new ReflectionUIError();
					}

					@Override
					public List<?> getPossibleValues() {
						List<ITypeInfo> result = new ArrayList<ITypeInfo>();
						if (PolymorphicEmbeddedForm.this.field.isNullable()) {
							result.add(NULL_POLY_TYPE);
						}
						for (ITypeInfo type : polyTypes) {
							result.add(type);
						}
						return result;
					}

					@Override
					public boolean isImmutable() {
						return true;
					}

					@Override
					public boolean hasCustomFieldControl() {
						return true;
					}

				};
			}
		});
	}

	protected String getEnumerationValueCaption(ITypeInfo actualFieldValueType) {
		return actualFieldValueType.getCaption();
	}

	protected Component createDynamicControl(Object instance) {
		final ITypeInfo actualFieldValueType = reflectionUI
				.getTypeInfo(reflectionUI.getTypeInfoSource(instance));
		return actualFieldValueType.createFieldControl(object,
				new HiddenNullableFacetFieldInfoProxy(reflectionUI,
						new FieldInfoProxy(field) {

							@Override
							public ITypeInfo getType() {
								return actualFieldValueType;
							}

						}));
	}

	protected void updateDynamicControl() {
		if (dynamicControl != null) {
			remove(dynamicControl);
			dynamicControl = null;
		}
		Object instance = field.getValue(object);
		if (instance != null) {
			dynamicControl = createDynamicControl(instance);
			add(dynamicControl, BorderLayout.CENTER);
		}

		ReflectionUIUtils.updateLayout(this);
	}

	protected void updateTypeEnumerationControl() {
		if (typeEnumerationControl != null) {
			remove(typeEnumerationControl);
		}
		add(typeEnumerationControl = createTypeEnumerationControl(),
				BorderLayout.NORTH);
		validate();
	}

	@Override
	public void refreshUI() {
		updateTypeEnumerationControl();
		updateDynamicControl();
	}

	@Override
	public void showCaption() {
		setBorder(BorderFactory.createTitledBorder(field.getCaption()));
	}

}
