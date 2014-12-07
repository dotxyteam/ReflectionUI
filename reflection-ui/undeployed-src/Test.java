import java.util.ArrayList;
import java.util.List;

import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedField;

public class Test {

	static class MyStringList extends ArrayList<String> {
		private static final long serialVersionUID = 1L;
		public List<String> children;
	}

	public static void main(String[] args) {
		TypeResolver typeResolver = new TypeResolver();
		ResolvedType resolvedType = typeResolver.resolve(MyStringList.class);
		// type itself has no parameterization (concrete non-generic class); but
		// it does implement List so:
		List<ResolvedType> params = resolvedType.typeParametersFor(List.class);
		System.out.println(params);

		MemberResolver memberResolver = new MemberResolver(typeResolver);
		ResolvedTypeWithMembers arrayListTypeWithMembers = memberResolver
				.resolve(resolvedType, null, null);
		for (ResolvedField resolvedField : arrayListTypeWithMembers
				.getMemberFields()) {
			System.out.println(resolvedField + ": " + resolvedField.getType());
		}
	}
}
