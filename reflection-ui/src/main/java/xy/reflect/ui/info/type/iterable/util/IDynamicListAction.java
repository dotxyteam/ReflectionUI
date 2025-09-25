


package xy.reflect.ui.info.type.iterable.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.IValidationJob;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.util.ReflectionUIUtils;

/**
 * This interface allows to specify an action that can be performed on a list
 * instance according to the current selection of items. Such an action will
 * typically be available on the list control tool bar.
 * 
 * The current selection is provided as the 1st parameter of
 * {@link IListTypeInfo#getDynamicActions(List, xy.reflect.ui.util.Mapper)}.
 * 
 * Note that the owner object passed to
 * {@link #invoke(Object, xy.reflect.ui.info.method.InvocationData)} is
 * {@link IDynamicListAction#NO_OWNER}.
 * 
 * @author olitank
 *
 */
public interface IDynamicListAction extends IDynamicListFeauture, IMethodInfo {

	public static final Object NO_OWNER = new Object() {

		@Override
		public String toString() {
			return IDynamicListAction.class.getName() + ".NO_OWNER";
		}

	};
	
	public static final IDynamicListAction NULL_DYNAMIC_LIST_ACTION = new IDynamicListAction() {
		
		@Override
		public String getName() {
			return "NULL_DYNAMIC_LIST_ACTION";
		}

		@Override
		public DisplayMode getDisplayMode() {
			return DisplayMode.TOOLBAR_AND_CONTEXT_MENU;
		}

		@Override
		public List<ItemPosition> getPostSelection() {
			return null;
		}

		@Override
		public boolean isHidden() {
			return false;
		}

		@Override
		public boolean isRelevant(Object object) {
			return true;
		}

		@Override
		public boolean isEnabled(Object object) {
			return true;
		}

		@Override
		public IValidationJob getReturnValueAbstractFormValidationJob(Object object, Object returnValue) {
			return null;
		}

		@Override
		public boolean isControlReturnValueValiditionEnabled() {
			return false;
		}

		@Override
		public boolean isLastInvocationDataUsedByDefault() {
			return true;
		}

		@Override
		public void onControlVisibilityChange(Object object, boolean b) {
		}

		@Override
		public String getSignature() {
			return ReflectionUIUtils.buildMethodSignature(this);
		}

		@Override
		public String getParametersValidationCustomCaption() {
			return null;
		}

		@Override
		public String getConfirmationMessage(Object object, InvocationData invocationData) {
			return null;
		}

		@Override
		public String getExecutionSuccessMessage() {
			return null;
		}

		@Override
		public ResourcePath getIconImagePath() {
			return null;
		}

		@Override
		public boolean isNullReturnValueDistinct() {
			return false;
		}

		@Override
		public boolean isReturnValueDetached() {
			return false;
		}

		@Override
		public boolean isReturnValueIgnored() {
			return false;
		}

		@Override
		public String getOnlineHelp() {
			return null;
		}

		@Override
		public String getCaption() {
			return "";
		}

		@Override
		public boolean isReadOnly() {
			return false;
		}

		@Override
		public ValueReturnMode getValueReturnMode() {
			return ValueReturnMode.INDETERMINATE;
		}

		@Override
		public InfoCategory getCategory() {
			return null;
		}

		@Override
		public Map<String, Object> getSpecificProperties() {
			return Collections.emptyMap();
		}

		@Override
		public ITypeInfo getReturnValueType() {
			return null;
		}

		@Override
		public List<IParameterInfo> getParameters() {
			return Collections.emptyList();
		}

		@Override
		public Object invoke(Object object, InvocationData invocationData) {
			return null;
		}

		@Override
		public Runnable getNextInvocationUndoJob(Object object, InvocationData invocationData) {
			return null;
		}

		@Override
		public Runnable getPreviousInvocationCustomRedoJob(Object object, InvocationData invocationData) {
			return null;
		}

		@Override
		public void validateParameters(Object object, InvocationData invocationData) throws Exception {
		}

		@Override
		public String getNullReturnValueLabel() {
			return null;
		}

		@Override
		public String toString() {
			return getName();
		}
	};
	

	/**
	 * @return the list of item positions that should be selected after the
	 *         execution of the current action or null if the selection should not
	 *         be updated.
	 */
	List<ItemPosition> getPostSelection();

}
