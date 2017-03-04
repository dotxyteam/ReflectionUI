import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.SwingCustomizer;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.info.type.util.TypeInfoProxyFactory;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SystemProperties;

public class ReflectionUITest {

	public static class Test {
		public Object anyObject;
		public Exception theException = new NullPointerException();

		public String theChoice;
		public String[] theChoiceOptions = new String[] { "a", "z", "e", "r", "t", "y" };

		private String theString = "azerty";

		public int getTheBoundedInt() {
			return theBoundedInt;
		}

		public void setTheBoundedInt(int theBoundedInt) throws Exception {
			if (theBoundedInt > 10) {
				throw new Exception();
			}
			this.theBoundedInt = theBoundedInt;
		}

		public String getTheString() {
			return theString;
		}

		public void setTheString(String theString) {
			this.theString = theString;
		}

		public double getPI() {
			return Math.PI;
		}

		public String getExceptionneableInfo() throws Exception {
			return "ExceptionneableInfo";
		}

		private int theInt = 50;

		private int theBoundedInt = 0;

		public int getTheInt() {
			return theInt;
		}

		public void setTheInt(int theInt) {
			this.theInt = theInt;
		}

		public float theFloat = 0.5f;
		public Double theDouble = 145678e-2;
		public boolean theBooleanPrimitive;
		public Boolean theBooleanObject;
		public File theFile;
		private Date theDate = new Date();
		char c = 'a';
		public List<String> theStringList = new ArrayList<String>(Arrays.asList("a", "b", "c", "d"));
		public List<Boolean> theBooleanList = new ArrayList<Boolean>(Arrays.asList(true, false, true, false));
		public float[] theFloatArray = new float[] { 1.1f, 2.2f, 3.3f, 4.4f, 5.5f };
		public Test2 test2 = new Test2();
		public AbstrcatTestDescendant[] theArrayTreeTable = new AbstrcatTestDescendant[] { new Test2(), new Test3() };
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Collection theGenericVector = new Vector(Arrays.asList(new Test2(), new Test2()));
		public Map<Integer, String> theMap = new HashMap<Integer, String>(new HashMap<Integer, String>(){
			private static final long serialVersionUID = 1L;

			{
				put(0, "zero");
				put(1, "one");
				put(2, "two");
				
			}
		});
		public Map<Integer, Test2> theTest2Map = new HashMap<Integer, Test2>();
		public List<File> theFileList = Arrays.asList(new File("tmp"));
		public Set<Integer> theSet = new HashSet<Integer>(Arrays.asList(1, 2, 3));
		public Stack<Integer> theStack = new Stack<Integer>();

		public Test4 test4 = new Test4();

		public Date getTheDate() {
			return theDate;
		}

		public Date getTheDatePlus1Day() {
			return new Date(theDate.getTime() + 24 * 60 * 60 * 1000);
		}

		public void resettheStringList() {
			theStringList.clear();
		}

		public void incrementTheInt() {
			theInt++;
		}

		public void multiplyTheFloat(int factor) {
			theFloat *= factor;
		}

		public void doLongTask() {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void verify() throws Exception {
			if (theChoice == null) {
				throw new Exception("The Choice is not made");
			}
		}

		public Object returnNothing() {
			return null;
		}

		public Object returnObject() {
			return new Object();
		}

		public void throwException() throws Exception {
			throw new Exception(ReflectionUIUtils.getPrintedStackTrace(new AssertionError()));
		}

		public void callWithManyParams(int i, String s, Date d, Color c) throws Exception {
		}
	}

	public static abstract class AbstrcatTestDescendant {
		public enum Day {
			MONDAY, TUESDAY, WEDNESDAY
		};

		public Day theDay;
		public short theShort = 0;
		public short the2ndShort = 1;

		public void verify() throws Exception {
			if (theDay == null) {
				throw new Exception("The Day is not set");
			}
		}
	}

	public static class Test2 extends AbstrcatTestDescendant {
		public List<AbstrcatTestDescendant> theChildrenList = new ArrayList<ReflectionUITest.AbstrcatTestDescendant>();

		public void incrementTheShortSlowly() {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			theShort++;
		}
	}

	public static class Test3 extends AbstrcatTestDescendant {
		public String reference;

		public List<String> getValidReferences() {
			return Arrays.asList("ref1", "ref2", "ref3");
		}
	}

	public static class Test4 {

		private int i = 0;

		public void modify1() {
			i++;
		}

		public void modify2() {
			i++;
		}

		public void modify3() {
			i++;
		}

		public void modify4() {
			i++;
		}

		public void modify5() {
			i++;
		}

		public void modify6() {
			i++;
		}

		public void modify7() {
			i++;
		}

		public void modify8() {
			i++;
		}

		public void modify9() {
			i++;
		}

		public void modify10() {
			i++;
		}

		public void modify11() {
			i++;
		}

		public void modify12() {
			i++;
		}

		public void modify13() {
			i++;
		}

		public void modify14() {
			i++;
		}

		public void modify15() {
			i++;
		}

		public void modify16() {
			i++;
		}

		@Override
		public String toString() {
			return "value" + Integer.toString(i);
		}

	}

	public static void main(String[] args) {
		final InfoCustomizations infoCustomizations = InfoCustomizations.getDefault();
		ReflectionUI reflectionUI = new ReflectionUI() {
			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				ITypeInfo result = super.getTypeInfo(typeSource);
				if (SystemProperties.areDefaultInfoCustomizationsActive()) {
					result = infoCustomizations.get(this, result);
				}
				return new TypeInfoProxyFactory() {
					@Override
					public String toString() {
						return ReflectionUITest.class.getName();
					}

					@Override
					protected Runnable getUndoModification(IMethodInfo method, ITypeInfo containingType,
							final Object object, InvocationData invocationData) {
						if (containingType.getName().equals(Test2.class.getName())
								&& method.getName().equals("incrementTheShortSlowly")) {
							return new Runnable() {
								@Override
								public void run() {
									((Test2) object).theShort--;
								}
							};
						}
						return super.getUndoModification(method, containingType, object, invocationData);
					}
				}.get(result);
			}
		};
		SwingRenderer swingRenderer;
		if (SystemProperties.areDefaultInfoCustomizationsActive()) {
			swingRenderer = new SwingCustomizer(reflectionUI, infoCustomizations,
					SystemProperties.getDefaultInfoCustomizationsFilePath());
		} else {
			swingRenderer = new SwingRenderer(reflectionUI);
		}
		swingRenderer.openObjectFrame(new Test(), "test", null);
	}
}
