
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.SubFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.source.ITypeInfoSource;

public class TestSubFieldInfo {
	public static void main(String[] args) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new SwingRenderer(new ReflectionUI() {

					@Override
					public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
						return new InfoProxyFactory() {

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
						}.wrapTypeInfo(super.getTypeInfo(typeSource));
					}
				}).openObjectFrame(new A());
			}
		});
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
