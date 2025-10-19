
package xy.reflect.ui.info.type.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.PrecomputedTypeInfoSource;
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
 * Note that each item (returned by {@link #getTypeOptions()}) is adapted to
 * prevent an infinite recursive enumeration of polymorphic type options.
 * 
 * @author olitank
 *
 */
public class PolymorphicTypeOptionsFactory extends GenericEnumerationFactory {

	protected ITypeInfo polymorphicType;

	public PolymorphicTypeOptionsFactory(ReflectionUI reflectionUI, ITypeInfo polymorphicType) {
		super(reflectionUI, getTypeOptionsCollector(reflectionUI, polymorphicType),
				"SubTypesEnumeration [polymorphicType=" + polymorphicType.getName() + "]", "", false, false);
		this.polymorphicType = polymorphicType;
	}

	public static boolean isRelevantFor(ReflectionUI reflectionUI, ITypeInfo type) {
		return StreamSupport.stream(getTypeOptionsCollector(reflectionUI, type).spliterator(), false)
				.anyMatch(typeOption -> !typeOption.getName().equals(type.getName()));
	}

	protected static Iterable<ITypeInfo> getTypeOptionsCollector(ReflectionUI reflectionUI, ITypeInfo polymorphicType) {
		return new TypeOptionsCollector(reflectionUI, polymorphicType);
	}

	/**
	 * @return the possible types (enumerated from this factory). Each base type
	 *         precede its sub-types in the result.
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
					ITypeInfo unwrappedType = type.getName().equals(polymorphicType.getName()) ? polymorphicType
							: ReflectionUIUtils
									.listDescendantTypes(polymorphicType,
											!polymorphicType.isPolymorphicInstanceAbstractTypeOptionAllowed())
									.stream().filter(eachType -> eachType.getName().equals(type.getName())).findFirst()
									.get();
					List<String> typeDescendantNames = ReflectionUIUtils
							.listDescendantTypes(unwrappedType,
									!polymorphicType.isPolymorphicInstanceAbstractTypeOptionAllowed())
							.stream().map(eachType -> eachType.getName()).collect(Collectors.toList());
					if (typeDescendantNames.contains(result.getName())) {
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

	public static class TypeOptionsCollector implements Iterable<ITypeInfo> {

		protected static final String ALREADY_USED_TYPE_OPTION_COLLECTORS_PROPERTY_KEY = TypeOptionsCollector.class
				.getName() + ".ALREADY_USED_TYPE_OPTION_COLLECTORS_PROPERTY_KEY";

		protected ReflectionUI reflectionUI;
		protected ITypeInfo polymorphicType;
		protected List<ITypeInfo> typeOptionCache;

		public TypeOptionsCollector(ReflectionUI reflectionUI, ITypeInfo polymorphicType) {
			this.reflectionUI = reflectionUI;
			this.polymorphicType = polymorphicType;
		}

		@Override
		public Iterator<ITypeInfo> iterator() {
			typeOptionCache = collect();
			return typeOptionCache.iterator();
		}

		protected List<ITypeInfo> collect() {
			List<ITypeInfo> result = new ArrayList<ITypeInfo>();
			if (polymorphicType.isPolymorphicInstanceAbstractTypeOptionAllowed() || polymorphicType.isConcrete()) {
				result.add(0, preventPolymorphismRecursivity(reflectionUI, polymorphicType, polymorphicType));
			}
			for (ITypeInfo descendantType : ReflectionUIUtils.listDescendantTypes(polymorphicType,
					!polymorphicType.isPolymorphicInstanceAbstractTypeOptionAllowed())) {
				result.add(preventPolymorphismRecursivity(reflectionUI, polymorphicType, descendantType));
			}
			List<TypeOptionsCollector> alreadyUsedTypeOptionCollectors = getAlreadyUsedTypeOptionCollectors(
					polymorphicType.getSpecificProperties());
			if (alreadyUsedTypeOptionCollectors != null) {
				for (TypeOptionsCollector typeOptionsCollector : alreadyUsedTypeOptionCollectors) {
					Set<String> alreadyExploredOptionNames = typeOptionsCollector.collect().stream()
							.map(typeOption -> typeOption.getName()).collect(Collectors.toSet());
					result = result.stream()
							.filter(resultItem -> resultItem.getName().equals(polymorphicType.getName())
									|| !alreadyExploredOptionNames.contains(resultItem.getName()))
							.collect(Collectors.toList());
				}
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		protected List<TypeOptionsCollector> getAlreadyUsedTypeOptionCollectors(Map<String, Object> specificPrperties) {
			return (List<TypeOptionsCollector>) specificPrperties.get(ALREADY_USED_TYPE_OPTION_COLLECTORS_PROPERTY_KEY);
		}

		protected Map<String, Object> addAlreadyUsedTypeOptionCollector(Map<String, Object> specificPrperties,
				TypeOptionsCollector typeOptionsCollector) {
			Map<String, Object> result = new HashMap<String, Object>(specificPrperties);
			List<TypeOptionsCollector> typeOptionsCollectors = new ArrayList<TypeOptionsCollector>();
			List<TypeOptionsCollector> preExistingTypeOptionsCollectors = getAlreadyUsedTypeOptionCollectors(
					specificPrperties);
			if (preExistingTypeOptionsCollectors != null) {
				typeOptionsCollectors = new ArrayList<TypeOptionsCollector>(preExistingTypeOptionsCollectors);
			}
			typeOptionsCollectors.add(typeOptionsCollector);
			result.put(ALREADY_USED_TYPE_OPTION_COLLECTORS_PROPERTY_KEY, typeOptionsCollectors);
			return result;
		}

		protected ITypeInfo preventPolymorphismRecursivity(ReflectionUI reflectionUI, ITypeInfo polymorphicType,
				ITypeInfo subType) {
			ITypeInfoSource typeSource = subType.getSource();
			ITypeInfo result = typeSource.buildTypeInfo(reflectionUI);
			result = getPolymorphismRecursivityDetectionFactory(reflectionUI, polymorphicType).wrapTypeInfo(result);
			result = reflectionUI
					.getTypeInfo(new PrecomputedTypeInfoSource(result, typeSource.getSpecificitiesIdentifier()));
			result = getPolymorphismRemovalFactory(reflectionUI, polymorphicType).wrapTypeInfo(result);
			return result;
		}

		protected InfoProxyFactory getPolymorphismRecursivityDetectionFactory(ReflectionUI reflectionUI,
				ITypeInfo polymorphicType) {
			return new InfoProxyFactory() {

				@Override
				public String getIdentifier() {
					List<TypeOptionsCollector> alreadyUsedTypeOptionCollectors = getAlreadyUsedTypeOptionCollectors(
							polymorphicType.getSpecificProperties());
					return "PolymorphismExplorationDetector [polymorphicType=" + polymorphicType.getName()
							+ ", typeOptionsAlreadyCollectedFor="
							+ ((alreadyUsedTypeOptionCollectors == null) ? ""
									: alreadyUsedTypeOptionCollectors.stream()
											.map(typeOptionsCollector -> typeOptionsCollector.polymorphicType.getName())
											.collect(Collectors.joining(",")))
							+ ", reflectionUI=" + reflectionUI + "]";
				}

				protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
					return addAlreadyUsedTypeOptionCollector(super.getSpecificProperties(type),
							TypeOptionsCollector.this);
				}

			};
		}

		protected InfoProxyFactory getPolymorphismRemovalFactory(ReflectionUI reflectionUI, ITypeInfo polymorphicType) {
			return new InfoProxyFactory() {
				@Override
				public String getIdentifier() {
					List<TypeOptionsCollector> alreadyUsedTypeOptionCollectors = getAlreadyUsedTypeOptionCollectors(
							polymorphicType.getSpecificProperties());
					return "PolymorphismRemover [polymorphicType=" + polymorphicType.getName()
							+ ", typeOptionsAlreadyCollectedFor="
							+ ((alreadyUsedTypeOptionCollectors == null) ? ""
									: alreadyUsedTypeOptionCollectors.stream()
											.map(typeOptionsCollector -> typeOptionsCollector.polymorphicType.getName())
											.collect(Collectors.joining(",")))
							+ ", reflectionUI=" + reflectionUI + "]";
				}

				@Override
				protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
					return Collections.emptyList();
				}

			};
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
