
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
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

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoCustomizationsFactory;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.SystemProperties;

@SuppressWarnings("unused")
public class ReflectionUITest {

	public static class Test implements Serializable {

		private static final long serialVersionUID = 1L;

		public Color thecolor = Color.BLUE;
		public Object anyObject;
		public Object anyObject2;
		public Object anyObject3;
		public Exception theException = new Exception();
		public Exception theException2 = new ClassCastException();

		public String theChoice;
		public String[] theChoiceOptions = new String[] { "a", "z", "e", "r", "t", "y" };

		public enum TheEnum {
			VALUE1, VALUE2, VALUE3
		};

		public TheEnum theEnum = TheEnum.VALUE1;

		private String theString = "<b><u> azerty </u></b>";

		private int theBoundedInt = 0;

		public Test3 getAThrownError() {
			throw new RuntimeException();
		}

		public int getTheBoundedInt() {
			return theBoundedInt;
		}

		public void setTheBoundedInt(int theBoundedInt) throws Exception {
			if (theBoundedInt > 10) {
				throw new Exception("Must be < 10");
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

		public Image getBannerImage() throws IOException {
			return ImageIO.read(new File("website/Business_zonne_b.jpg"));
		}

		public String getExceptionneableInfo() throws Exception {
			return "ExceptionneableInfo";
		}

		private int theInt = 50;

		public int getTheInt() {
			return theInt;
		}

		public void setTheInt(int theInt) {
			// throw new RuntimeException();
			this.theInt = theInt;
		}

		public Image theImage = null;
		public byte theByte = 1;
		public Short theShort = 2;
		public Long theLong = 3l;
		public float theFloat = 0.5f;
		public Double theDouble = 0.05;
		public boolean theBooleanPrimitive;
		public Boolean theBooleanObject;
		public File theFile = new File("tmp");
		@SuppressWarnings("deprecation")
		private Date theDate = new Date(2000 - 1900, 01, 01);
		@SuppressWarnings("deprecation")
		public Date theModifiableDate = new Date(2000 - 1900, 01, 01);
		char c = 'a';
		public List<Exception> theExceptionList = new ArrayList<Exception>();
		public List<String> theStringList = new ArrayList<String>(Arrays.asList("a", "b", "c", "d"));
		public List<Boolean> theBooleanList = new ArrayList<Boolean>(Arrays.asList(true, false, true, false));
		public float[] theFloatArray = new float[] { 1.1f, 2.2f, 3.3f, 4.4f, 5.5f };
		public AbstrcatTestDescendant[] theArrayTreeTable = new AbstrcatTestDescendant[] {
				new Test2(new ArrayList<ReflectionUITest.AbstrcatTestDescendant>(
						Arrays.<AbstrcatTestDescendant>asList(new Test3(), new Test4()))),
				new Test3() };
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Collection theGenericVector = new Vector(Arrays.asList(new Test2(), new Test2(), new Test3()));
		public Map<Integer, String> theMap = new HashMap<Integer, String>(new HashMap<Integer, String>() {
			private static final long serialVersionUID = 1L;

			{
				put(0, "zero");
				put(1, "one");
				put(2, "two");

			}
		});
		public Map<FileNotFoundException, ArrayIndexOutOfBoundsException> theExceptionMap = new HashMap<FileNotFoundException, ArrayIndexOutOfBoundsException>();
		public Map<Integer, Test2> theTest2Map = new HashMap<Integer, Test2>();
		public List<File> theFileList = Arrays.asList(new File("tmp"));
		public Set<Integer> theSet = new HashSet<Integer>(Arrays.asList(1, 2, 3));
		public Stack<Integer> theStack = new Stack<Integer>();
		public List<File> theNullList = null;

		public Test2 test2 = new Test2();
		public Test3 test3 = null;
		public Test4 test4 = new Test4();

		public List<AbstrcatTestDescendant> theTestDescendantList = new ArrayList<AbstrcatTestDescendant>(
				Arrays.asList(new Test2(), new Test2(), new Test3()));
		public AbstrcatTestDescendant selectedItemInTestDescendantList;
		
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

		public void multiplyTheInt(int factor) {
			theInt *= factor;
		}

		public void doInterruptibleLongTask() {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void doUninterruptibleLongTask() {
			long startTime = System.currentTimeMillis();
			while (((System.currentTimeMillis() - startTime) < 10000)) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
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

		public Object returnAnyObject() {
			return anyObject;
		}

		public void throwException() throws Exception {
			throw new Exception(MiscUtils.getPrintedStackTrace(new AssertionError()));
		}

		public void callWithManyParams(int i, String s, Date d, Color c) throws Exception {
		}

		public Object echo(Object object) {
			return object;
		}

		public String fieldToImportAsParameter = "fieldToImportAsParameter value";

		public String echoFieldImportedAsParameter() {
			return fieldToImportAsParameter;
		}

		public String echoParameterExportedAsField(String parameterToExportAsField) {
			return parameterToExportAsField;
		}

		public String echoHiddenParameterDefaultValue(String parameterWithDefaultValue) {
			return parameterWithDefaultValue;
		}

	}

	public static abstract class AbstrcatTestDescendant implements Serializable {
		private static final long serialVersionUID = 1L;

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
		private static final long serialVersionUID = 1L;

		public List<AbstrcatTestDescendant> theChildrenList = new ArrayList<ReflectionUITest.AbstrcatTestDescendant>();

		public Test2() {
		}

		public Test2(List<AbstrcatTestDescendant> theChildrenList) {
			this.theChildrenList = theChildrenList;
		}

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
		private static final long serialVersionUID = 1L;

		public String reference;
		public String transactionState;

		public List<String> getValidReferences() {
			return Arrays.asList("ref1", "ref2", "ref3");
		}

		public void beginTransaction() {
			transactionState = "started";
		}

		public void commitTransaction() {
			transactionState = "committed";
		}

		public void rollbackTransaction() {
			transactionState = "rolled back";
		}

		@Override
		public String toString() {
			return "Test3 [reference=" + reference + ", transactionState=" + transactionState + "]";
		}

	}

	public static class Test4 extends AbstrcatTestDescendant {
		private static final long serialVersionUID = 1L;

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
		InfoCustomizations infoCustomizations = new InfoCustomizations();
		CustomizedUI reflectionUI = new CustomizedUI(infoCustomizations) {
			@Override
			protected ITypeInfo getTypeInfoBeforeCustomizations(ITypeInfo type) {
				ITypeInfo result = type;
				return new InfoProxyFactory() {

					@Override
					protected Runnable getNextInvocationUndoJob(IMethodInfo method, ITypeInfo containingType,
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
						return super.getNextInvocationUndoJob(method, containingType, object, invocationData);
					}
				}.wrapTypeInfo(result);
			}
		};
		SwingCustomizer renderer = new SwingCustomizer(reflectionUI,
				System.getProperty("custom-ui.project.directory", "./") + "unpackaged-src/default.icu");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				renderer.openObjectFrame(new Test(), null, null);
			}
		});
	}
}
