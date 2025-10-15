
package xy.reflect.ui.info.type.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * Factory that generates virtual enumeration (type information) from the list
 * of descendants of the specified polymorphic type. The base polymorphic type
 * itself is eligible as an item of the resulting enumeration. Abstract types
 * are filtered out if
 * {@link ITypeInfo#isPolymorphicInstanceAbstractTypeOptionAllowed()} returns
 * false for the #{@link #polymorphicType}.
 * 
 * Note that each item (type option) is wrapped by a proxy that triggers a
 * {@link RecursivePolymorphismDetectionException} when it is reused as the base
 * polymorphic type of another {@link PolymorphicTypeOptionsFactory}. This is
 * intended to prevent an infinite recursive enumeration of polymorphic type
 * options.
 * 
 * @author olitank
 *
 */
public class PolymorphicTypeOptionsFactory extends GenericEnumerationFactory {

	protected static final String POLYMORPHISM_EXPLORED_WITH_PROPERTY_KEY = PolymorphicTypeOptionsFactory.class
			.getName() + ".POLYMORPHISM_EXPLORED_PROPERTY_KEY";

	protected ITypeInfo polymorphicType;

	public PolymorphicTypeOptionsFactory(ReflectionUI reflectionUI, ITypeInfo polymorphicType) {
		super(reflectionUI,
				new TypeOptionsCollector(reflectionUI, polymorphicType,
						!polymorphicType.isPolymorphicInstanceAbstractTypeOptionAllowed()),
				"SubTypesEnumeration [polymorphicType=" + polymorphicType.getName() + "]", "", false, false);
		this.polymorphicType = polymorphicType;
	}

	protected static List<ITypeInfo> listDescendantTypes(ITypeInfo polymorphicType, boolean concreteOnly) {
		List<ITypeInfo> result = new ArrayList<ITypeInfo>();
		List<ITypeInfo> subTypes = polymorphicType.getPolymorphicInstanceSubTypes();
		for (ITypeInfo subType : subTypes) {
			if (!concreteOnly || subType.isConcrete()) {
				result.add(subType);
			}
			result.addAll(listDescendantTypes(subType, concreteOnly));
		}
		return result;
	}

	protected static ITypeInfo preventPolymorphismRecursivity(ReflectionUI reflectionUI, final ITypeInfo type) {
		if (isPolymorphismRecursivityDetected(type, reflectionUI)) {
			throw new RecursivePolymorphismDetectionException();
		}
		return getPolymorphismRecursivityPreventingFactory(reflectionUI).wrapTypeInfo(type);
	}

	protected static InfoProxyFactory getPolymorphismRecursivityPreventingFactory(ReflectionUI reflectionUI) {
		return new InfoProxyFactory() {
			@Override
			protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
				return Collections.emptyList();
			}

			@Override
			protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
				Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(type));
				result.put(POLYMORPHISM_EXPLORED_WITH_PROPERTY_KEY, reflectionUI);
				return result;
			}

			@Override
			public String getIdentifier() {
				return "PolymorphismExplorationDetector []";
			}

		};
	}

	public static boolean isPolymorphismRecursivityDetected(ITypeInfo type, ReflectionUI reflectionUI) {
		return reflectionUI.equals(type.getSpecificProperties().get(POLYMORPHISM_EXPLORED_WITH_PROPERTY_KEY));
	}

	/**
	 * @return the possible types (enumerated from this factory). Note that each
	 *         base type precede its sub-types in the result.
	 */
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
	 *         the given instance. Note that descendant types have precedence over
	 *         their ancestors and a type supporting the given instance may be
	 *         returned even if the actual instance type is not one of this factory
	 *         type options.
	 * @throws ReflectionUIError If any ambiguity or inconsistency is detected.
	 */
	public ITypeInfo guessSubType(Object instance) throws ReflectionUIError {
		ITypeInfo result = null;
		for (ITypeInfo type : MiscUtils.getReverse(getTypeOptions())) {
			if (type.supports(instance)) {
				if (result == null) {
					result = type;
				} else {
					List<ITypeInfo> typeDescendants = listDescendantTypes(
							getPolymorphismRecursivityPreventingFactory(reflectionUI).unwrapTypeInfo(type),
							!polymorphicType.isPolymorphicInstanceAbstractTypeOptionAllowed()).stream()
									.map(descendantType -> preventPolymorphismRecursivity(reflectionUI, descendantType))
									.collect(Collectors.toList());
					if (typeDescendants.contains(result)) {
						continue;
					}
					throw new ReflectionUIError(
							"Failed to guess the polymorphic value type: Ambiguity detected: More than 1 valid types found:"
									+ "\n- " + result.getName() + "\n- " + type.getName());
				}
			}
		}
		return result;
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

	protected static class TypeOptionsCollector implements Iterable<ITypeInfo> {
		protected ReflectionUI reflectionUI;
		protected ITypeInfo polymorphicType;
		protected boolean onlyContreteSubTypes;

		public TypeOptionsCollector(ReflectionUI reflectionUI, ITypeInfo polymorphicType,
				boolean onlyContreteSubTypes) {
			this.reflectionUI = reflectionUI;
			this.polymorphicType = polymorphicType;
			this.onlyContreteSubTypes = onlyContreteSubTypes;
		}

		@Override
		public Iterator<ITypeInfo> iterator() {
			List<ITypeInfo> result = new ArrayList<ITypeInfo>();
			if (!onlyContreteSubTypes || polymorphicType.isConcrete()) {
				result.add(0, preventPolymorphismRecursivity(reflectionUI, polymorphicType));
			}
			for (ITypeInfo descendantType : listDescendantTypes(polymorphicType, onlyContreteSubTypes)) {
				result.add(preventPolymorphismRecursivity(reflectionUI, descendantType));
			}
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

}
