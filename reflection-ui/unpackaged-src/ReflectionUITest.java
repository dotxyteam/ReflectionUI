import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import javax.imageio.ImageIO;

import org.ietf.jgss.GSSException;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.annotation.Category;
import xy.reflect.ui.info.annotation.Documentation;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.AbstractMethodUndoModification;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.DefaultListStructuralInfo;
import xy.reflect.ui.info.type.DefaultTextualTypeInfo;
import xy.reflect.ui.info.type.IListTypeInfo;
import xy.reflect.ui.info.type.IListTypeInfo.IListStructuralInfo;
import xy.reflect.ui.info.type.JavaTypeInfoSource;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfoSource;
import xy.reflect.ui.info.type.TabularTreetStructuralInfo;
import xy.reflect.ui.info.type.TypeInfoProxyConfiguration;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.IModification;

@SuppressWarnings("unused")
public class ReflectionUITest {

	@Documentation("This type is used as a test case")
	public static class Test {
		@Documentation("Can be of any type")
		public Object anyObject;
		@Documentation("Can be of 2 types")
		public Exception theException;

		@Documentation("This is the string var")
		private String theString = "azerty";

		@Documentation("Returns 'theString'")
		public String getTheString() {
			return theString;
		}

		@Documentation("Sets the value of 'theString'")
		public void setTheString(String theString) {
			this.theString = theString;
		}

		@Documentation("returns the mathematic PI constant value'")
		public double getPI() {
			return Math.PI;
		}

		private int theInt = 50;

		@Documentation("Returns 'theInt' value")
		public int getTheInt() {
			return theInt;
		}

		@Documentation("Sets the value of 'theInt'")
		public void setTheInt(int theInt) {
			this.theInt = theInt;
		}

		public float theFloat = 0.5f;
		public Double theDouble = 145678e-2;
		public boolean theBooleanPrimitive;
		public Boolean theBooleanObject;
		public File theFile;
		char c = 'a';
		@Category("List")
		public List<String> theStringList = new ArrayList<String>(
				Arrays.asList("a", "b", "c"));
		public Test2 test2 = new Test2();
		@Category("List")
		public Test2[] theArrayTreeTable = new Test2[] { new Test2(),
				new Test2() };
		@Category("List")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Vector theGenericVectorTree = new Vector(Arrays.asList(
				new Test2(), new Test2()));

		@Category("List")
		public Map<Integer, String> theMap = new HashMap<Integer, String>();

		@Category("List")
		public List<File> theFileList = Arrays.asList(new File("."));

		@Category("List")
		public Set<Integer> theSet = new HashSet<Integer>(
				Arrays.asList(1, 2, 3));

		@Category("List")
		public Stack<Integer> theStack = new Stack<Integer>();

		@Documentation("clears the 1st list of Strings")
		public void resettheStringList() {
			theStringList.clear();
		}

		@Documentation("adds 1 to the var 'theInt' value")
		public void incrementTheInt() {
			theInt++;
		}

		@Documentation("multiplies the var 'theFloat' by the given factor")
		public void multiplyTheFloat(
				@Documentation("the factor that will be applied") int factor) {
			theFloat *= factor;
		}

		public void doLongTask() {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	@Documentation("This type is used as a 2nd test case")
	public static class Test2 {
		public enum Day {
			MONDAY, TUESDAY, WEDNESDAY
		};

		public Day theDay;
		public short theShort = 0;
		public short the2ndShort = 1;
		public List<Test2> theChildrenList = new ArrayList<ReflectionUITest.Test2>();
	}

	public static void main(String[] args) {
		ReflectionUI editor = new ReflectionUI() {

			ReflectionUI thisReflectionUI = this;

			@Override
			public Image getObjectIconImage(Object item) {
				try {
					return ImageIO.read(ReflectionUITest.class
							.getResource("icon.gif"));
				} catch (IOException e) {
					throw new AssertionError(e);
				}
			}

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new TypeInfoProxyConfiguration() {

					@Override
					public List<ITypeInfo> getPolymorphicInstanceSubTypes(
							ITypeInfo type) {
						if (type.getName().equals(Exception.class.getName())) {
							return Arrays.asList(
									getTypeInfo(new JavaTypeInfoSource(
											ParseException.class)),
									getTypeInfo(new JavaTypeInfoSource(
											GSSException.class)));
						} else {
							return super.getPolymorphicInstanceSubTypes(type);
						}
					}

					@Override
					protected IModification getUndoModification(
							final IMethodInfo method, ITypeInfo containingType,
							final Object object,
							final Map<Integer, Object> valueByParameterPosition) {
						if (method.getName().equals("incrementTheInt")) {
							return new AbstractMethodUndoModification(object,
									method, valueByParameterPosition) {
								@Override
								protected void revertMethod() {
									Test test = (Test) object;
									test.theInt--;
								}
							};
						} else if (method.getName().equals("multiplyTheFloat")) {
							return new AbstractMethodUndoModification(object,
									method, valueByParameterPosition) {
								@Override
								protected void revertMethod() {
									Test test = (Test) object;
									test.theFloat /= (Integer) valueByParameterPosition
											.get(0);
								}
							};
						} else {
							return super.getUndoModification(method,
									containingType, object,
									valueByParameterPosition);
						}
					}
					
				}.get(super.getTypeInfo(typeSource));
			}
		};
		editor.openObjectFrame(new Test(), "test", null);
	}
}
