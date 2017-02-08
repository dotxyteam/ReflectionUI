import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import xy.reflect.ui.control.swing.SwingRenderer;

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
		public Test2 test2 = new Test2();
		public AbstrcatTestDescendant[] theArrayTreeTable = new AbstrcatTestDescendant[] { new Test2(), new Test3() };
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Collection theGenericVector = new Vector(Arrays.asList(new Test2(), new Test2()));
		public Map<Integer, String> theMap = new HashMap<Integer, String>(Collections.singletonMap(5, "five"));
		public Map<Integer, Test2> theTest2Map = new HashMap<Integer, Test2>();
		public List<File> theFileList = Arrays.asList(new File("./file1"));
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
		
		public void verify() throws Exception{
			if(theChoice == null){
				throw new Exception("The Choice is not made");
			}
		}
		
		public Object returnNull(){
			return null;
		}
		
		public Object returnObject(){
			return new Object();
		}
	}

	public static abstract class AbstrcatTestDescendant {
		public enum Day {
			MONDAY, TUESDAY, WEDNESDAY
		};

		public Day theDay;
		public short theShort = 0;
		public short the2ndShort = 1;

		public void verify() throws Exception{
			if(theDay == null){
				throw new Exception("The Day is not set");
			}
		}
	}

	public static class Test2 extends AbstrcatTestDescendant {
		public List<AbstrcatTestDescendant> theChildrenList = new ArrayList<ReflectionUITest.AbstrcatTestDescendant>();

		public void doNothing() {

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
		SwingRenderer.DEFAULT.openObjectFrame(new Test(), "test", null);
	}
}
