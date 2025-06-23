
package xy.reflect.ui.info.type.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Factory that generates virtual enumeration type information from the list of
 * descendants of the specified polymorphic type. The base polymorphic type
 * itself is added as an item to the resulting enumeration if it is a concrete
 * type. Note that it is then wrapped with a proxy that trigger a
 * {@link RecursivePolymorphismDetectionException} when it is reused as the base
 * polymorphic type with another {@link PolymorphicTypeOptionsFactory}. This is
 * intended to prevent an infinite recursive enumeration of type options.
 * 
 * @author olitank
 *
 */
public class PolymorphicTypeOptionsFactory extends GenericEnumerationFactory {

	protected static final String POLYMORPHISM_EXPLORED_PROPERTY_KEY = PolymorphicTypeOptionsFactory.class.getName()
			+ ".POLYMORPHISM_EXPLORED_PROPERTY_KEY";

	protected ITypeInfo polymorphicType;

	public PolymorphicTypeOptionsFactory(ReflectionUI reflectionUI, ITypeInfo polymorphicType) {
		super(reflectionUI, getTypeOptionsCollector(reflectionUI, polymorphicType),
				"SubTypesEnumeration [polymorphicType=" + polymorphicType.getName() + "]", "", false, false);
		this.polymorphicType = polymorphicType;
	}

	protected static class TypeOptionsCollector implements Iterable<ITypeInfo> {
		protected ReflectionUI reflectionUI;
		protected ITypeInfo polymorphicType;

		public TypeOptionsCollector(ReflectionUI reflectionUI, ITypeInfo polymorphicType) {
			this.reflectionUI = reflectionUI;
			this.polymorphicType = polymorphicType;
		}

		@Override
		public Iterator<ITypeInfo> iterator() {
			List<ITypeInfo> result = new ArrayList<ITypeInfo>();
			if (polymorphicType.isConcrete()) {
				result.add(0, preventPolymorphismRecursivity(reflectionUI, polymorphicType));
			}
			result.addAll(listConcreteDescendantTypes(polymorphicType));
			return result.iterator();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((polymorphicType == null) ? 0 : polymorphicType.hashCode());
			result = prime * result + ((reflectionUI == null) ? 0 : reflectionUI.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TypeOptionsCollector other = (TypeOptionsCollector) obj;
			if (polymorphicType == null) {
				if (other.polymorphicType != null)
					return false;
			} else if (!polymorphicType.equals(other.polymorphicType))
				return false;
			if (reflectionUI == null) {
				if (other.reflectionUI != null)
					return false;
			} else if (!reflectionUI.equals(other.reflectionUI))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TypeOptionsCollector [polymorphicType=" + polymorphicType + ", reflectionUI=" + reflectionUI + "]";
		}

	}

	protected static Iterable<ITypeInfo> getTypeOptionsCollector(ReflectionUI reflectionUI,
			final ITypeInfo polymorphicType) {
		return new TypeOptionsCollector(reflectionUI, polymorphicType);

	}

	protected static List<ITypeInfo> listConcreteDescendantTypes(ITypeInfo polymorphicType) {
		List<ITypeInfo> result = new ArrayList<ITypeInfo>();
		List<ITypeInfo> subTypes = polymorphicType.getPolymorphicInstanceSubTypes();
		for (ITypeInfo subType : subTypes) {
			if (subType.isConcrete()) {
				result.add(subType);
			}
			result.addAll(listConcreteDescendantTypes(subType));
		}
		return result;
	}

	public static ITypeInfo preventPolymorphismRecursivity(ReflectionUI reflectionUI, final ITypeInfo type) {
		if (isPolymorphismRecursivityDetected(type)) {
			throw new RecursivePolymorphismDetectionException();
		}
		final ITypeInfoSource typeSource = type.getSource();
		final ITypeInfo unwrappedType = typeSource.buildTypeInfo(reflectionUI);
		final ITypeInfo[] blockedRecursivityType = new ITypeInfo[1];
		blockedRecursivityType[0] = new InfoProxyFactory() {
			@Override
			protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
				return Collections.emptyList();
			}

			@Override
			protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
				Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(type));
				result.put(POLYMORPHISM_EXPLORED_PROPERTY_KEY, Boolean.TRUE);
				return result;
			}

			@Override
			public String getIdentifier() {
				return "PolymorphismExplorationDetector [polymorphicType=" + type.getName() + "]";
			}

			@Override
			protected ITypeInfoSource getSource(ITypeInfo type) {
				return new PrecomputedTypeInfoSource(blockedRecursivityType[0],
						typeSource.getSpecificitiesIdentifier());
			}

		}.wrapTypeInfo(unwrappedType);
		ITypeInfo result = reflectionUI.getTypeInfo(blockedRecursivityType[0].getSource());
		return result;
	}

	public static boolean isPolymorphismRecursivityDetected(ITypeInfo type) {
		return Boolean.TRUE.equals(type.getSpecificProperties().get(POLYMORPHISM_EXPLORED_PROPERTY_KEY));
	}

	public List<ITypeInfo> getTypeOptions() {
		List<ITypeInfo> result = new ArrayList<ITypeInfo>();
		for (Object item : getOrLoadItems()) {
			result.add((ITypeInfo) item);
		}
		return result;
	}

	/**
	 * @param instance The instance to analyze.
	 * @return the type information among {@link #getTypeOptions()} that best fits
	 *         the given instance. Note that the base polymorphic type may be a
	 *         valid option (because it is a concrete type for instance). Descendant
	 *         types have precedence over their ancestors. The actual instance type
	 *         may also be a sub-type of one of the type options.
	 * @throws ReflectionUIError If any ambiguity or inconsistency is detected.
	 */
	public ITypeInfo guessSubType(Object instance) throws ReflectionUIError {
		List<ITypeInfo> options = new ArrayList<ITypeInfo>(getTypeOptions());
		ITypeInfo polymorphicTypeAsValidOption = null;
		ITypeInfo validSubType = null;
		for (ITypeInfo type : options) {
			if (type.supports(instance)) {
				if (type.getName().equals(polymorphicType.getName())) {
					polymorphicTypeAsValidOption = type;
				} else {
					if (validSubType == null) {
						validSubType = type;
						continue;
					}
					if (listConcreteDescendantTypes(validSubType).contains(type)) {
						validSubType = type;
						continue;
					}
					if (listConcreteDescendantTypes(type).contains(validSubType)) {
						continue;
					}
					throw new ReflectionUIError(
							"Failed to guess the polymorphic value type: Ambiguity detected: More than 1 valid types found:"
									+ "\n- " + validSubType.getName() + "\n- " + type.getName());
				}
			}
		}
		if (validSubType != null) {
			ITypeInfo actualType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(instance));
			if (actualType.getName().equals(polymorphicType.getName())) {
				throw new ReflectionUIError(
						"Polymorphism inconsistency detected: The base type instance is supported by a sub-type : "
								+ "\n- Base polymorphic type: " + polymorphicType.getName() + "\n- Detected sub-type: "
								+ validSubType.getName() + "\n- Actual type: " + actualType.getName());
			}
			return validSubType;
		}
		if (polymorphicTypeAsValidOption != null) {
			return polymorphicTypeAsValidOption;
		}
		return null;
	}

	@Override
	protected ResourcePath getItemIconImagePath(Object arrayItem) {
		ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
		return polyTypesItem.getIconImagePath(null);
	}

	@Override
	protected String getItemOnlineHelp(Object arrayItem) {
		ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
		return polyTypesItem.getOnlineHelp();
	}

	@Override
	protected String getItemName(Object arrayItem) {
		ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
		return polyTypesItem.getName();
	}

	@Override
	protected String getItemCaption(Object arrayItem) {
		ITypeInfo polyTypesItem = (ITypeInfo) arrayItem;
		String result = polyTypesItem.getCaption();
		if (polyTypesItem.getName().equals(polymorphicType.getName())) {
			return ReflectionUIUtils.composeMessage("Basic", result);
		}
		return result;
	}

	@Override
	public String toString() {
		return "PolymorphicTypeOptionsFactory [polymorphicType=" + polymorphicType + "]";
	}

	public static class RecursivePolymorphismDetectionException extends ReflectionUIError {

		private static final long serialVersionUID = 1L;

	}

}
