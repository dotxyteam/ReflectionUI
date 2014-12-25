package xy.reflect.ui;

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

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.DefaultTextualTypeInfo;
import xy.reflect.ui.info.type.JavaTypeInfoSource;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfoSource;
import xy.reflect.ui.info.type.TypeInfoProxy;

@SuppressWarnings("unused")
public class ReflectionUITest {

	public static class Test {
		public Object anyObject;
		public Exception theException;
		public String theString = "azerty";
		public int theInt = 50;
		public float theFloat = 0.5f;
		public Double theDouble = 145678e-2;
		public boolean theBooleanPrimitive;
		public Boolean theBooleanObject;
		public File theFile;
		char c = 'a';
		public List<String> theStringList = new ArrayList<String>(
				Arrays.asList("a", "b", "c"));
		public Test2 test2 = new Test2();

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Vector theGenericVector = new Vector(Arrays.asList(new Test2(),
				new Test2()));

		public Map<Integer, String> theMap = new HashMap<Integer, String>();

		public List<File> theFileList = Arrays.asList(new File("."));

		public Set<Integer> theSet = new HashSet<Integer>(
				Arrays.asList(1, 2, 3));

		public Stack<Integer> theStack = new Stack<Integer>();

		public void resettheStringList() {
			theStringList.clear();
		}

		public void incrementTheInt() {
			theInt++;
		}

		public void multiplyTheFloat(int factor) {
			theFloat *= factor;
		}

	}

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
				if (!(typeSource instanceof JavaTypeInfoSource)) {
					return super.getTypeInfo(typeSource);
				}
				JavaTypeInfoSource classSource = (JavaTypeInfoSource) typeSource;
				if (classSource.getJavaType() == Exception.class) {
					return new TypeInfoProxy() {

						@Override
						public List<ITypeInfo> getPolymorphicInstanceSubTypes(
								ITypeInfo type) {
							return Arrays.asList(
									getTypeInfo(new JavaTypeInfoSource(
											ParseException.class)),
									getTypeInfo(new JavaTypeInfoSource(
											GSSException.class)));
						}

					}.get(super.getTypeInfo(typeSource));
				} else {
					return super.getTypeInfo(typeSource);
				}
			}

		};
		editor.openObjectFrame(new Test(), "test", null);
	}
}
