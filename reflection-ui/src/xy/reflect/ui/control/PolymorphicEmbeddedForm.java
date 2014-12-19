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
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.InfoCategory;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.IEnumerationTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.ReflectionUIUtils;

public class PolymorphicEmbeddedForm extends JPanel implements
		IRefreshableControl, ICanShowCaptionControl {

	protected static final long serialVersionUID = 1L;
	protected ReflectionUI reflectionUI;
	protected Object object;
	protected IFieldInfo field;
	protected List<ITypeInfo> polyTypes;
	protected Map<String, Object> instanceByEnumerationValueCaptionCache = new HashMap<String, Object>();
	protected Component dynamicControl;
	protected EnumerationControl typeEnumerationControl;
	protected DefaultTypeInfo defaultTypeInfo;

	public PolymorphicEmbeddedForm(final ReflectionUI reflectionUI,
			final Object object, final IFieldInfo field,
			DefaultTypeInfo defaultObjectTypeInfo) {
		this.reflectionUI = reflectionUI;
		this.object = object;
		this.field = field;
		this.polyTypes = field.getType().getPolymorphicInstanceTypes();
		this.defaultTypeInfo = defaultObjectTypeInfo;

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
					return getEnumerationNullCaption();
				} else {
					ITypeInfo actualFieldValueType = reflectionUI
							.getTypeInfo(reflectionUI
									.getTypeInfoSource(instance));
					return getEnumerationValueCaption(actualFieldValueType);
				}
			}

			@Override
			public void setValue(Object object, Object value) {
				try {
					if (getEnumerationNullCaption().equals(value)) {
						field.setValue(object, null);
					} else {
						ITypeInfo actualFieldValueType = null;
						for (ITypeInfo polyType : polyTypes) {
							if (value
									.equals(getEnumerationValueCaption(polyType))) {
								actualFieldValueType = polyType;
								break;
							}
						}
						String enumValueCaption = getEnumerationValueCaption(actualFieldValueType);
						Object instance = instanceByEnumerationValueCaptionCache
								.get(enumValueCaption);
						if (instance == null) {
							instance = reflectionUI.onTypeInstanciationRequest(
									actualFieldValueType,
									PolymorphicEmbeddedForm.this, true);
							if (instance == null) {
								return;
							}
							instanceByEnumerationValueCaptionCache.put(
									enumValueCaption, instance);
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
					public List<ITypeInfo> getPolymorphicInstanceTypes() {
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
						throw new AssertionError();
					}

					@Override
					public List<?> getPossibleValues() {
						List<String> result = new ArrayList<String>();
						result.add(getEnumerationNullCaption());
						for (ITypeInfo type : polyTypes) {
							result.add(getEnumerationValueCaption(type));
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

	protected String getEnumerationNullCaption() {
		return "<Choose an option>";
	}

	protected Component createDynamicControl() {
		return defaultTypeInfo.createNonNullFieldValueControl(object, field);
	}

	protected void updateDynamicControl() {
		if (dynamicControl != null) {
			remove(dynamicControl);
			dynamicControl = null;
		}
		if (field.getValue(object) != null) {
			dynamicControl = createDynamicControl();
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
