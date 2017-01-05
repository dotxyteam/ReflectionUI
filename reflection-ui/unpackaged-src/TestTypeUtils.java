import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.swing.JPanel;

import org.apache.commons.lang3.reflect.TypeUtils;

public class TestTypeUtils<A, B, C> extends ArrayList<B> {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		ParameterizedType stringArrayListClass = TypeUtils.parameterize(TestTypeUtils.class, Integer.class,
				String.class, JPanel.class);
		Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(stringArrayListClass, Collection.class);
		for (TypeVariable<?> v : typeArguments.keySet()) {
			if (v.getGenericDeclaration().equals(Collection.class)) {
				System.out.println(typeArguments.get(v));
			}
		}
	}

}
