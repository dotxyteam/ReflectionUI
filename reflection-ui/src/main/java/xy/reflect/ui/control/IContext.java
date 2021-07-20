


package xy.reflect.ui.control;

import xy.reflect.ui.info.type.ITypeInfo;

/**
 * This class just allows to identify the context in which a control operates.
 * It is mainly used to provide different names to generated {@link ITypeInfo}s
 * according to the context and then allow to provide different customizations.
 * 
 * @author olitank
 *
 */
public interface IContext {

	IContext NULL_CONTEXT = new IContext() {
		@Override
		public String getIdentifier() {
			return "NULL_CONTEXT";
		}
	};

	/**
	 * @return the identifier of the current context.
	 */
	public String getIdentifier();
}
