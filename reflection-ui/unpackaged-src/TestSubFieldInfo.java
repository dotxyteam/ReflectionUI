import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.SubFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.TypeInfoProxyFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;

public class TestSubFieldInfo {
	public static void main(String[] args) throws Exception {
		new SwingRenderer(new ReflectionUI() {

			@Override
			public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
				return new TypeInfoProxyFactory() {

					@Override
					protected List<IFieldInfo> getFields(ITypeInfo type) {
						if (type.getName().equals(A.class.getName())) {
							List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
							result.add(new SubFieldInfo(type, "b", "c"));
							return result;
						} else {
							return super.getFields(type);
						}
					}
				}.get(super.getTypeInfo(typeSource));
			}
		}).openObjectFrame(new A());
	}

	public static class A {
		public B b;
	}

	public static class B {
		public C c;
	}

	public static class C {
		public int x;
	}

}
