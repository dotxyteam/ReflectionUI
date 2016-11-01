import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.info.annotation.Category;
import xy.reflect.ui.info.annotation.OnlineHelp;
import xy.reflect.ui.info.annotation.ValueOptionsForField;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.DefaultListStructuralInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.info.type.util.TypeInfoProxyFactory;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.AbstractMethodUndoModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.info.method.InvocationData;

@SuppressWarnings("unused")
public class ReflectionUITest {

	@OnlineHelp("This type is used as a test case")
	public static class Test {
		@OnlineHelp("Can be of any type")
		public Object anyObject;
		@OnlineHelp("Can be of 2 types")
		public Exception theException;

		@OnlineHelp("This is the string var")
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

		@OnlineHelp("Returns 'theString'")
		public String getTheString() {
			return theString;
		}

		@OnlineHelp("Sets the value of 'theString'")
		public void setTheString(String theString) {
			this.theString = theString;
		}

		@OnlineHelp("returns the mathematic PI constant value'")
		public double getPI() {
			return Math.PI;
		}

		@OnlineHelp("returns the mathematic PI constant value'")
		public String getExceptionneableInfo() throws Exception {
			return "ExceptionneableInfo";
		}

		private int theInt = 50;

		private int theBoundedInt = 0;

		@OnlineHelp("Returns 'theInt' value")
		public int getTheInt() {
			return theInt;
		}

		@OnlineHelp("Sets the value of 'theInt'")
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
		@Category("List")
		public List<String> theStringList = new ArrayList<String>(Arrays.asList("a", "b", "c", "d"));
		public Test2 test2 = new Test2();
		@Category("List")
		public AbstrcatTestDescendant[] theArrayTreeTable = new AbstrcatTestDescendant[] { new Test2(), new Test3() };
		@Category("List")
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Vector theGenericVector = new Vector(Arrays.asList(new Test2(), new Test2()));

		@Category("List")
		public Map<Integer, String> theMap = new HashMap<Integer, String>(Collections.singletonMap(5, "five"));

		@Category("List")
		public Map<Integer, Test2> theTest2Map = new HashMap<Integer, Test2>();

		@Category("List")
		public List<File> theFileList = Arrays.asList(new File("./file1"));

		@Category("List")
		public Set<Integer> theSet = new HashSet<Integer>(Arrays.asList(1, 2, 3));

		@Category("List")
		public Stack<Integer> theStack = new Stack<Integer>();

		public Test4 test4 = new Test4();

		public Date getTheDate() {
			return theDate;
		}
		public Date getTheDatePlus1Day() {
			return new Date(theDate.getTime() + 24 * 60 * 60 * 1000);
		}
		
		@OnlineHelp("clears the 1st list of Strings")
		public void resettheStringList() {
			theStringList.clear();
		}

		@OnlineHelp("adds 1 to the var 'theInt' value")
		public void incrementTheInt() {
			theInt++;
		}

		@OnlineHelp("multiplies the var 'theFloat' by the given factor")
		public void multiplyTheFloat(@OnlineHelp("the factor that will be applied") int factor) {
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

	public static abstract class AbstrcatTestDescendant {
		public enum Day {
			MONDAY, TUESDAY, WEDNESDAY
		};

		public Day theDay;
		public short theShort = 0;
		public short the2ndShort = 1;

	}

	@OnlineHelp("This type is used as a 2nd test case")
	public static class Test2 extends AbstrcatTestDescendant {
		public List<AbstrcatTestDescendant> theChildrenList = new ArrayList<ReflectionUITest.AbstrcatTestDescendant>();

		public void doNothing() {

		}
	}

	@OnlineHelp("This type is used as a 3rd test case")
	public static class Test3 extends AbstrcatTestDescendant {
		public String reference;

		@ValueOptionsForField("reference")
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
		SwingRenderer.DEFAULT.openObjectFrame(new Test(), "test", null);
	}
}
