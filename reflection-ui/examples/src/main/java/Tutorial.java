import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;

public class Tutorial {

	public static void main(String[] args) {
		/*
		 * Each of the following methods demonstrates a feature of the library.
		 */
		openObjectDialog();
		justCreateObjectForm();
		controlReflectionInterpretation();
		preventFromSettingNull();
		hideSomeFieldsAndMethods();
		addVirtualFieldsAndMethods();
		overrideToStringMethod();
		customizeCopyCutPasteFeature();
	}

	private static void openObjectDialog() {
		/*
		 * Most basic use case: opening an object dialog
		 */
		Object myObject = new Tutorial();
		SwingRenderer.getDefault().openObjectDialog(null, myObject);
	}

	private static void justCreateObjectForm() {
		/*
		 * create JPanel-based form in order to include it in a GUI as a sub-component.
		 */
		Object myObject = new Tutorial();
		JOptionPane.showMessageDialog(null, SwingRenderer.getDefault().createForm(myObject), "As a form",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private static void controlReflectionInterpretation() {
		/*
		 * If you want to take control of the object discovery and interpretation
		 * process, then you must create custom ReflectionUI and SwingRenderer objects:
		 */
		Object myObject = new Tutorial();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyFactory() {

					/*
					 * For instance you can uppercase all the field captions this way:
					 */
					@Override
					protected String getCaption(IFieldInfo field, ITypeInfo containingType) {
						return super.getCaption(field, containingType).toUpperCase();
					}

					/*
					 * Many more methods can be overriden. Explore the class to find out...
					 */

				}.wrapTypeInfo(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Uppercase field captions", null, false, true);
	}

	private static void preventFromSettingNull() {
		Object myObject = new Tutorial();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyFactory() {

					/*
					 * By default the generated UI will allow to set <null> on non-primitive values.
					 * However usually the developers would not allow to set <null> from their UI in
					 * spite of the fact that it is allowed by the language. The methods below allow
					 * to selectively disable the nullable facet of any value displayed in the
					 * generated UI.
					 */

					@Override
					protected boolean isNullValueDistinct(IParameterInfo param, IMethodInfo method,
							ITypeInfo containingType) {
						if (containingType.getName().equals("myClass") && method.getName().equals("myMethod")
								&& param.getName().equals("myNullableField")) {
							return true;
						} else {
							return false;
						}
					}

					@Override
					protected boolean isNullValueDistinct(IFieldInfo field, ITypeInfo containingType) {
						if (containingType.getName().equals("myClass") && field.getName().equals("myNullableField")) {
							return true;
						} else {
							return false;
						}
					}

					@Override
					protected boolean isNullReturnValueDistinct(IMethodInfo method, ITypeInfo containingType) {
						if (containingType.getName().equals("myClass")
								&& method.getName().equals("myNullableReturnValueMethod")) {
							return true;
						} else {
							return false;
						}
					}

				}.wrapTypeInfo(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Never null fields", null, false, true);
	}

	private static void hideSomeFieldsAndMethods() {
		Object myObject = new Tutorial();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyFactory() {

					/*
					 * This is how you can hide fields and methods:
					 */

					@Override
					protected List<IFieldInfo> getFields(ITypeInfo type) {
						if (type.getName().equals(Tutorial.class.getName())) {
							List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
							while (result.size() > 1) {
								result.remove(0);
							}
							return result;
						} else {
							return super.getFields(type);
						}
					}

					@Override
					protected List<IMethodInfo> getMethods(ITypeInfo type) {
						if (type.getName().equals(Tutorial.class.getName())) {
							List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(type));
							while (result.size() > 1) {
								result.remove(0);
							}
							return result;
						} else {
							return super.getMethods(type);
						}
					}

				}.wrapTypeInfo(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Hidden members", null, false, true);
	}

	private static void addVirtualFieldsAndMethods() {
		Object myObject = new Tutorial();
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
						if (type.getName().equals(Tutorial.class.getName())) {
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
						if (type.getName().equals(Tutorial.class.getName())) {
							List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(type));
							result.add(new MethodInfoProxy(IMethodInfo.NULL_METHOD_INFO) {

								@Override
								public String getName() {
									return "invokeAdditionalMethod";
								}

								@Override
								public String getCaption() {
									return "(Virtual) Invoke Additional Method";
								}

								@Override
								public Object invoke(Object object, InvocationData invocationData) {
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
		swingRenderer.openObjectDialog(null, myObject, "Added virtual members", null, false, true);
	}

	private static void overrideToStringMethod() {
		Object myObject = new Tutorial();
		ReflectionUI reflectionUI = new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new InfoProxyFactory() {

					/*
					 * If some of your classes "toString" methods (used by some field controls) are
					 * not implemented as you want then:
					 */

					@Override
					public String toString(ITypeInfo type, Object object) {
						return "OVERRIDEN: " + super.toString(type, object);
					}

				}.wrapTypeInfo(super.getTypeInfo(typeSource));
			}

		};
		SwingRenderer swingRenderer = new SwingRenderer(reflectionUI);
		swingRenderer.openObjectDialog(null, myObject, "Overriden toString() methods", null, false, true);
	}

	private static void customizeCopyCutPasteFeature() {
		Object myObject = new Tutorial();
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
		swingRenderer.openObjectDialog(null, myObject, "Disabled copy/cut/paste in lists", null, false, true);
	}

	/*
	 * Example fields
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