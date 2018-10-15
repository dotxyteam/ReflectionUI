import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.app.ApplicationInfoProxy;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.enumeration.IEnumerationItemInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.SwingRendererUtils;

public class Tutorial {

	public static void main(String[] args) {
		/*
		 * Each of the following methods demonstrates a feature of the library.
		 */

		openObjectDialog();
		justCreateObjectForm();
		controlReflection();
		allowToSetNull();
		hideSomeFieldsAndMethods();
		addVirtualFieldsAndMethods();
		customizeCopyCutPasteFeature();
		customizeColors();
	}

	private static void openObjectDialog() {
		/*
		 * Most basic use case: opening an object dialog
		 */
		Object myObject = new HelloWorld();
		SwingRenderer.getDefault().openObjectDialog(null, myObject);
	}

	private static void justCreateObjectForm() {
		/*
		 * create JPanel-based form in order to include it in a GUI as a sub-component.
		 */
		Object myObject = new HelloWorld();
		JOptionPane.showMessageDialog(null, SwingRenderer.getDefault().createForm(myObject), "As a form",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private static void controlReflection() {
		/*
		 * If you want to take control of the object discovery and interpretation
		 * process, then you must create custom ReflectionUI and SwingRenderer objects:
		 */
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyFactory() {

					/*
					 * For instance you can all the displayed labels this way (many more options are
					 * available. Explore the proxy factory class to find out):
					 */

					@Override
					protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
						return super.getCaption(field, containingType).toUpperCase();
					}

					@Override
					protected String getCaption(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
						return super.getCaption(param, method, containingType).toUpperCase();
					}

					@Override
					protected String getCaption(IMethodInfo method, ITypeInfo containingType) {
						return super.getCaption(method, containingType).toUpperCase();
					}

					@Override
					protected String getCaption(ITypeInfo type) {
						return super.getCaption(type).toUpperCase();
					}

					@Override
					protected String getCaption(IApplicationInfo appInfo) {
						return super.getCaption(appInfo).toUpperCase();
					}

					@Override
					protected String getCaption(IEnumerationItemInfo info, ITypeInfo parentEnumType) {
						return super.getCaption(info, parentEnumType).toUpperCase();
					}

				}.wrapTypeInfo(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Labels => uppercase", null, false, true);
	}

	private static void allowToSetNull() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyFactory() {

					/*
					 * By default, the generated user interface does not allow to assign <null>
					 * values. Indeed, the developers do not generally allow the assignment of
					 * <null> values from their graphical interface despite the fact that it is
					 * authorized by the language. The following methods allow to selectively enable
					 * or disable the nullable facet of any value displayed in the generated GUI.
					 */

					@Override
					protected boolean isNullValueDistinct(IParameterInfo param, IMethodInfo method,
							ITypeInfo containingType) {
						if (containingType.getName().equals(HelloWorld.class.getName())) {
							return !param.getType().isPrimitive();
						} else {
							return super.isNullValueDistinct(param, method, containingType);
						}
					}

					@Override
					protected boolean isNullValueDistinct(IFieldInfo field, ITypeInfo containingType) {
						if (containingType.getName().equals(HelloWorld.class.getName())) {
							return !field.getType().isPrimitive();
						} else {
							return super.isNullValueDistinct(field, containingType);
						}
					}

					@Override
					protected boolean isNullReturnValueDistinct(IMethodInfo method, ITypeInfo containingType) {
						if (containingType.getName().equals(HelloWorld.class.getName())) {
							return !method.getReturnValueType().isPrimitive();
						} else {
							return super.isNullReturnValueDistinct(method, containingType);
						}
					}

				}.wrapTypeInfo(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Non-primitive fields => nullable", null, false, true);
	}

	private static void hideSomeFieldsAndMethods() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyFactory() {

					/*
					 * This is how you can hide fields and methods:
					 */

					@Override
					protected boolean isHidden(IFieldInfo field, ITypeInfo containingType) {
						if (field.getType() instanceof IListTypeInfo) {
							return true;
						} else {
							return super.isHidden(field, containingType);
						}
					}

					@Override
					protected boolean isHidden(IMethodInfo method, ITypeInfo containingType) {
						return super.isHidden(method, containingType);
					}

					@Override
					protected boolean isHidden(IParameterInfo param, IMethodInfo method, ITypeInfo containingType) {
						return super.isHidden(param, method, containingType);
					}

				}.wrapTypeInfo(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "List fields => hidden", null, false, true);
	}

	private static void addVirtualFieldsAndMethods() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyFactory() {

					/*
					 * This is how you can add virtual fields and methods that would generally be
					 * calculated from the existing ones:
					 */

					@Override
					protected List<IFieldInfo> getFields(ITypeInfo type) {
						if (type.getName().equals(HelloWorld.class.getName())) {
							final List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
							result.add(new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {

								@Override
								public String getName() {
									return "numberOfFields";
								}

								@Override
								public String getCaption() {
									return "(Virtual) Number Of Fields";
								}

								@Override
								public Object getValue(Object object) {
									return result.size();
								}

							});
							return result;
						} else {
							return super.getFields(type);
						}
					}

					@Override
					protected List<IMethodInfo> getMethods(ITypeInfo type) {
						if (type.getName().equals(HelloWorld.class.getName())) {
							List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(type));
							result.add(new MethodInfoProxy(IMethodInfo.NULL_METHOD_INFO) {

								@Override
								public String getName() {
									return "resetFields";
								}

								@Override
								public String getCaption() {
									return "(Virtual) Reset Fields";
								}

								@Override
								public Object invoke(Object object, InvocationData invocationData) {
									HelloWorld newObject = new HelloWorld();
									for (IFieldInfo field : ReflectionUI.getDefault()
											.getTypeInfo(new JavaTypeInfoSource(HelloWorld.class, null)).getFields()) {
										if (field.isGetOnly()) {
											continue;
										}
										field.setValue(object, field.getValue(newObject));
									}
									return null;
								}

							});
							return result;
						} else {
							return super.getMethods(type);
						}
					}

				}.wrapTypeInfo(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Added virtual members (numberOfFields + resetFields())", null,
				false, true);
	}

	private static void customizeCopyCutPasteFeature() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyFactory() {

					/*
					 * copy/cut/paste: By default this feature is enabled on collections/arrays but
					 * only for Serializable items. If your item class does not implement the
					 * Serializable interface yet you want to allow to copy its instances (or if you
					 * want to customize the copy process) then override the following methods. Here
					 * actually we will simply disable the feature on all lists:
					 */

					@Override
					public boolean canCopy(ITypeInfo type, Object object) {
						return false;
					}

					@Override
					public Object copy(ITypeInfo type, Object object) {
						throw new AssertionError();
					}

				}.wrapTypeInfo(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Copy/Cut/Paste from lists => disabled", null, false, true);
	}

	private static void customizeColors() {
		Object myObject = new HelloWorld();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public IApplicationInfo getApplicationInfo() {
				return new ApplicationInfoProxy(super.getApplicationInfo()) {

					@Override
					public boolean isSystemIntegrationCrossPlatform() {
						return true;
					}

					@Override
					public ColorSpecification getTitleBackgroundColor() {
						return SwingRendererUtils.getColorSpecification(Color.RED.darker());
					}

					@Override
					public ColorSpecification getTitleForegroundColor() {
						return SwingRendererUtils.getColorSpecification(Color.WHITE);
					}

					@Override
					public ColorSpecification getMainBackgroundColor() {
						return SwingRendererUtils.getColorSpecification(Color.BLACK);
					}

					@Override
					public ColorSpecification getMainForegroundColor() {
						return SwingRendererUtils.getColorSpecification(Color.WHITE);
					}

					@Override
					public ColorSpecification getButtonBackgroundColor() {
						return SwingRendererUtils.getColorSpecification(Color.CYAN);
					}

					@Override
					public ColorSpecification getButtonForegroundColor() {
						return SwingRendererUtils.getColorSpecification(Color.BLACK);
					}
				};
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Custom colors", null, false, true);
	}

	/*
	 * Fields
	 */

	private String text = "a text";
	private int number = 10;
	private boolean checked = true;
	private Date complexValue = new Date();
	private double readOnly = Math.PI;
	private Enumerated enumerated = Enumerated.ENUM_VALUE1;
	private List<String> list = new ArrayList<String>(Arrays.asList("item1", "item2", "item3", "item4", "item5"));

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public Date getComplexValue() {
		return complexValue;
	}

	public void setComplexValue(Date complexValue) {
		this.complexValue = complexValue;
	}

	public double getReadOnly() {
		return readOnly;
	}

	public Enumerated getEnumerated() {
		return enumerated;
	}

	public void setEnumerated(Enumerated enumerated) {
		this.enumerated = enumerated;
	}

	public List<String> getList() {
		return list;
	}

	public void setList(List<String> list) {
		this.list = list;
	}

	/*
	 * Example methods
	 */

	public String invokeMethod(int param1, int param2) {
		return "return value";
	}

	/*
	 * Example enumeration
	 */
	public enum Enumerated {
		ENUM_VALUE1, ENUM_VALUE2, ENUM_VALUE3
	}

}