
package xy.reflect.ui.info.type.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

	protected static final String POLYMORPHIC_TYPE_OPTIONS_COLLECTOR_PROPERTY_KEY = PolymorphicTypeOptionsFactory.class
			.getName() + ".POLYMORPHIC_TYPE_OPTIONS_COLLECTOR_PROPERTY_KEY";

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
		return new TypeOptionsCollector(reflectionUI, polymorphicType,
				!polymorphicType.isPolymorphicInstanceAbstractTypeOptionAllowed());
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

	protected static ITypeInfo preventPolymorphismRecursivity(ReflectionUI reflectionUI, ITypeInfo polymorphicType,
			ITypeInfo subType) {
		ITypeInfoSource typeSource = subType.getSource();
		ITypeInfo result = typeSource.buildTypeInfo(reflectionUI);
		result = getPolymorphismRecursivityDetectionFactory(reflectionUI, polymorphicType).wrapTypeInfo(result);
		result = reflectionUI
				.getTypeInfo(new PrecomputedTypeInfoSource(result, typeSource.getSpecificitiesIdentifier()));
		result = getPolymorphismRemovalFactory(reflectionUI, polymorphicType).wrapTypeInfo(result);
		return result;
	}

	protected static InfoProxyFactory getPolymorphismRecursivityDetectionFactory(ReflectionUI reflectionUI,
			ITypeInfo polymorphicType) {
		return new InfoProxyFactory() {
			String identifier;

			@Override
			public String getIdentifier() {
				if (identifier == null) {
					TypeOptionsCollector parentTypeOptionsCollector = (TypeOptionsCollector) polymorphicType
							.getSpecificProperties().get(POLYMORPHIC_TYPE_OPTIONS_COLLECTOR_PROPERTY_KEY);
					identifier = "PolymorphismExplorationDetector [polymorphicType=" + polymorphicType.getName()
							+ ", alreadyProposedOptions="
							+ ((parentTypeOptionsCollector == null) ? ""
									: StreamSupport.stream(parentTypeOptionsCollector.spliterator(), false)
											.map(ITypeInfo::getName).collect(Collectors.joining(",")))
							+ ", reflectionUI=" + reflectionUI + "]";
				}
				return identifier;
			}

			protected Map<String, Object> getSpecificProperties(ITypeInfo type) {
				Map<String, Object> result = new HashMap<String, Object>(super.getSpecificProperties(type));
				result.put(POLYMORPHIC_TYPE_OPTIONS_COLLECTOR_PROPERTY_KEY,
						getTypeOptionsCollector(reflectionUI, polymorphicType));
				return result;
			}

		};
	}

	protected static InfoProxyFactory getPolymorphismRemovalFactory(ReflectionUI reflectionUI,
			ITypeInfo polymorphicType) {
		return new InfoProxyFactory() {
			String identifier;

			@Override
			public String getIdentifier() {
				if (identifier == null) {
					TypeOptionsCollector parentTypeOptionsCollector = (TypeOptionsCollector) polymorphicType
							.getSpecificProperties().get(POLYMORPHIC_TYPE_OPTIONS_COLLECTOR_PROPERTY_KEY);
					identifier = "PolymorphismRemover [polymorphicType=" + polymorphicType.getName()
							+ ", alreadyProposedOptions="
							+ ((parentTypeOptionsCollector == null) ? ""
									: StreamSupport.stream(parentTypeOptionsCollector.spliterator(), false)
											.map(ITypeInfo::getName).collect(Collectors.joining(",")))
							+ ", reflectionUI=" + reflectionUI + "]";
				}
				return identifier;
			}

			@Override
			protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
				return Collections.emptyList();
			}

		};
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
					List<ITypeInfo> typeDescendants = listDescendantTypes(
							getPolymorphismRemovalFactory(reflectionUI, polymorphicType).unwrapTypeInfo(type),
							!polymorphicType.isPolymorphicInstanceAbstractTypeOptionAllowed()).stream()
									.map(descendantType -> preventPolymorphismRecursivity(reflectionUI, polymorphicType,
											descendantType))
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
				result.add(0, preventPolymorphismRecursivity(reflectionUI, polymorphicType, polymorphicType));
			}
			for (ITypeInfo descendantType : listDescendantTypes(polymorphicType, onlyContreteSubTypes)) {
				result.add(preventPolymorphismRecursivity(reflectionUI, polymorphicType, descendantType));
			}
			List<String> forbiddenSubTypeNames = new ArrayList<String>();
			TypeOptionsCollector parent = (TypeOptionsCollector) polymorphicType.getSpecificProperties()
					.get(POLYMORPHIC_TYPE_OPTIONS_COLLECTOR_PROPERTY_KEY);
			if (parent != null) {
				parent.forEach(type -> forbiddenSubTypeNames.add(type.getName()));
			}
			for (Iterator<ITypeInfo> resultIterator = result.iterator(); resultIterator.hasNext();) {
				ITypeInfo resultItem = resultIterator.next();
				if (forbiddenSubTypeNames.contains(resultItem.getName())) {
					resultIterator.remove();
				}
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
