package xy.reflect.ui;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.imageio.ImageIO;

import org.ietf.jgss.GSSException;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.DefaultTextualTypeInfo;
import xy.reflect.ui.info.type.JavaTypeInfoSource;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfoSource;
import xy.reflect.ui.info.type.SimpleTypeInfoDelegator;

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
				Arrays.asList("1", "2", "3"));
		public Test2 test2 = new Test2();

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Vector theGenericVector = new Vector(Arrays.asList(new Test2(),
				new Test2()));

		public Map<Integer, String> theMap = new HashMap<Integer, String>();
		
		public List<File> theFileList = Arrays.asList(new File("."));

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
			public void doCustomValidation(Object object, IFieldInfo field) {
				if (field.getType() instanceof DefaultTextualTypeInfo) {
					DefaultTextualTypeInfo basicType = (DefaultTextualTypeInfo) field
							.getType();
					if (basicType.getJavaType() == int.class) {
						if ((Integer) field.getValue(object) < 0) {
							throw new AssertionError(
									"negative integers forbidden");
						}
					}
				}
			}

			@Override
			public Image getObjectIconImage(Object item) {
				try {
					return ImageIO
							.read(new URL(
									"http://icons.iconarchive.com/icons/graphicloads/100-flat-2/16/ok-2-icon.png"));
				} catch (IOException e) {
					throw new AssertionError(e);
				}
			}

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				if(!(typeSource instanceof JavaTypeInfoSource)){
					return super.getTypeInfo(typeSource);
				}
				JavaTypeInfoSource classSource = (JavaTypeInfoSource) typeSource;
				if (classSource.getJavaType() == Exception.class) {
					return new SimpleTypeInfoDelegator(
							super.getTypeInfo(typeSource)) {

						@Override
						public List<ITypeInfo> getPolymorphicInstanceTypes() {
							return Arrays.asList(
									getTypeInfo(new JavaTypeInfoSource(
											ParseException.class)),
									getTypeInfo(new JavaTypeInfoSource(
											GSSException.class)));
						}

					};
				} else {
					return super.getTypeInfo(typeSource);
				}
			}

		};
		editor.openObjectFrame(new Test(), "test", null);
	}
}
