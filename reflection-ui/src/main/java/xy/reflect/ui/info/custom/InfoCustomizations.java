
package xy.reflect.ui.info.custom;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.plugin.FileBrowserPlugin.FileBrowserConfiguration;
import xy.reflect.ui.info.ColorSpecification;
import xy.reflect.ui.info.IInfo;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.field.CapsuleFieldInfo;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.MethodReturnValueFieldInfo;
import xy.reflect.ui.info.field.ParameterAsFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.IMenuItemContainerInfo;
import xy.reflect.ui.info.method.DefaultConstructorInfo;
import xy.reflect.ui.info.method.DefaultMethodInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.PresetInvocationDataMethodInfo;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo.InitialItemValueCreationOption;
import xy.reflect.ui.info.type.iterable.item.DetachedItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.EmbeddedItemDetailsAccessMode;
import xy.reflect.ui.info.type.iterable.item.IListItemDetailsAccessMode;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.source.SpecificitiesIdentifier;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.Filter;
import xy.reflect.ui.util.IOUtils;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.Parameter;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SystemProperties;

/**
 * This class allows to specify declarative customizations of abstract UI model
 * elements. <BR>
 * Online help extracted from the CustomUI sub-project:
 * 
 * <H3>TypeCustomization</H3>
 * <UL>
 * <LI><B>baseTypeName: </B><HTML>
 * <P width="300">
 * Allows to specify the name of a type from which the customizations will be
 * inherited.
 * </P>
 * </HTML></LI>
 * <LI><B>categoriesStyle: </B><HTML>
 * <P width="300">
 * Specifies how categories are displayed.
 * </P>
 * </HTML></LI>
 * <LI><B>categoriesBackgroundColor: </B><HTML>
 * <P width="300">
 * Allows to specify the background color of the categories control of forms
 * generated from this type.
 * </P>
 * </HTML></LI>
 * <LI><B>categoriesForegroundColor: </B><HTML>
 * <P width="300">
 * Allows to specify the text color of the categories control of forms generated
 * from this type.
 * </P>
 * </HTML></LI>
 * <LI><B>undoManagementHidden: </B><HTML>
 * <P width="300">
 * If checked, undo/redo/reset/cancel actions will not be available for objects
 * of this type.
 * </P>
 * </HTML></LI>
 * <LI><B>anyPersistenceMemberIncluded: </B><HTML>
 * <P width="300">
 * If checked, the current type persistence methods (save() and load()) will be
 * available as method controls for objects of this type.
 * </P>
 * </HTML></LI>
 * <LI><B>anyDefaultObjectMemberIncluded: </B><HTML>
 * <P width="300">
 * If checked, default object fields and methods (getClass(), toString(), ...)
 * will be available.
 * </P>
 * </HTML></LI>
 * <LI><B>typeName: </B><HTML>
 * <P width="300">
 * The name of the current type.
 * </P>
 * </HTML></LI>
 * <LI><B>customTypeCaption: </B><HTML>
 * <P width="300">
 * Custom display name of this type. If not set, a default display name is
 * provided.
 * </P>
 * </HTML></LI>
 * <LI><B>immutableForced: </B><HTML>
 * <P width="300">
 * If checked, objects of this type will be considered as immutable and then not
 * editable.
 * </P>
 * </HTML></LI>
 * <LI><B>abstractForced: </B><HTML>
 * <P width="300">
 * If checked, objects of this type will be considered as abstract and then not
 * instanciable.
 * </P>
 * </HTML></LI>
 * <LI><B>copyForbidden: </B><HTML>
 * <P width="300">
 * If checked, it will not be possible to copy objects of this type (typically
 * from lists).
 * </P>
 * </HTML></LI>
 * <LI><B>fieldsLayout: </B><HTML>
 * <P width="300">
 * Allows to change the layout of this type field controls.
 * </P>
 * </HTML></LI>
 * <LI><B>methodsLayout: </B><HTML>
 * <P width="300">
 * Allows to change the layout of this type method controls.
 * </P>
 * </HTML></LI>
 * <LI><B>formWidth: </B><HTML>
 * <P width="300">
 * Allows to specify the default width of forms generated from this this type.
 * </P>
 * </HTML></LI>
 * <LI><B>formHeight: </B><HTML>
 * <P width="300">
 * Allows to specify the default height of forms generated from this this type.
 * </P>
 * </HTML></LI>
 * <LI><B>formSpacing: </B><HTML>
 * <P width="300">
 * Allows to specify the space (in pixels) between elements in forms generated
 * from this this type.
 * </P>
 * </HTML></LI>
 * <LI><B>formBackgroundColor: </B><HTML>
 * <P width="300">
 * Allows to specify the background color of forms generated from this type.
 * </P>
 * </HTML></LI>
 * <LI><B>formForegroundColor: </B><HTML>
 * <P width="300">
 * Allows to specify the text color of forms generated from this type.
 * </P>
 * </HTML></LI>
 * <LI><B>formBorderColor: </B><HTML>
 * <P width="300">
 * Allows to specify the border color of forms generated from this type.
 * </P>
 * </HTML></LI>
 * <LI><B>formBackgroundImagePath: </B><HTML>
 * <P width="300">
 * Allows to specify the background image of forms generated from this type.
 * </P>
 * </HTML></LI>
 * <LI><B>formEditorsBackgroundColor: </B><HTML>
 * <P width="300">
 * Allows to specify the background color of the editable parts of forms
 * generated from this type.
 * </P>
 * </HTML></LI>
 * <LI><B>formEditorsForegroundColor: </B><HTML>
 * <P width="300">
 * Allows to specify the text color of the editable parts of forms generated
 * from this type.
 * </P>
 * </HTML></LI>
 * <LI><B>formButtonBackgroundColor: </B><HTML>
 * <P width="300">
 * Allows to specify the background color of buttons of forms generated from
 * this type.
 * </P>
 * </HTML></LI>
 * <LI><B>formButtonForegroundColor: </B><HTML>
 * <P width="300">
 * Allows to specify the text color of buttons of forms generated from this
 * type.
 * </P>
 * </HTML></LI>
 * <LI><B>formButtonBorderColor: </B><HTML>
 * <P width="300">
 * Allows to specify the border color of buttons of forms generated from this
 * type.
 * </P>
 * </HTML></LI>
 * <LI><B>formButtonBackgroundImagePath: </B><HTML>
 * <P width="300">
 * Allows to specify the background image of buttons of forms generated from
 * this type.
 * </P>
 * </HTML></LI>
 * <LI><B>savingMethodName: </B><HTML>
 * <P width="300">
 * The name of a method of the current type that will be used to save instances
 * state. The method must have the following signature: void
 * &lt;name&gt;(java.io.OutputStream). By default the instances will be saved by
 * using standard serialization if available (the Java class must implement the
 * java.io.Serializable interface).
 * </P>
 * </HTML></LI>
 * <LI><B>loadingMethodName: </B><HTML>
 * <P width="300">
 * The name of a method of the current type that will be used to load instances
 * state. The method must have the following signature: void
 * &lt;name&gt;(java.io.InputStream). By default the instances will be saved by
 * using standard serialization if available (the Java class must implement the
 * java.io.Serializable interface).
 * </P>
 * </HTML></LI>
 * <LI><B>onlineHelp: </B><HTML>
 * <P width="300">
 * Help message displayed for the current type. An HTML display will be used if
 * the help message is surrounded by HTML tags (&lt;HTML&gt;...&lt;/HTML&gt;).
 * </P>
 * </HTML></LI>
 * <LI><B>iconImagePath: </B><HTML>
 * <P width="300">
 * Icon image displayed for this type.
 * </P>
 * </HTML></LI>
 * <LI><B>memberCategories: </B><HTML>
 * <P width="300">
 * List of categories in which field and method controls can be put in order to
 * make the forms generated from this type more intuitive and usable.
 * </P>
 * </HTML></LI>
 * <LI><B>polymorphicSubTypeFinders: </B><HTML>
 * <P width="300">
 * List of declared sub-types of this type. They will be proposed during the
 * creation of objects of this type.
 * </P>
 * </HTML></LI>
 * <LI><B>fieldsCustomizations: </B><HTML>
 * <P width="300">
 * List of discovered fields of this type.
 * </P>
 * </HTML></LI>
 * <LI><B>virtualFieldDeclarations: </B><HTML>
 * <P width="300">
 * List of virtual fields of this type.
 * </P>
 * </HTML></LI>
 * <LI><B>methodsCustomizations: </B><HTML>
 * <P width="300">
 * List of methods of this type.
 * </P>
 * </HTML></LI>
 * </UL>
 * <H3>FieldCustomization</H3>
 * <UL>
 * <LI><B>nullValueDistinctForced: </B><HTML>
 * <P width="300">
 * Specifies that the field control should allows to set the null value.
 * </P>
 * </HTML></LI>
 * <LI><B>nullValueLabel: </B><HTML>
 * <P width="300">
 * Text to be displayed when this field value is null.
 * </P>
 * </HTML></LI>
 * <LI><B>nullReplacement: </B><HTML>
 * <P width="300">
 * If specified, this value will be displayed when this field value is null.
 * </P>
 * </HTML></LI>
 * <LI><B>nullStatusFieldExported: </B><HTML>
 * <P width="300">
 * Specifies that a new boolean field should be generated to reflect the null
 * status of this field.
 * </P>
 * </HTML></LI>
 * <LI><B>importedNullStatusFieldName: </B><HTML>
 * <P width="300">
 * Specifies a boolean field that should be used to simulate a null value (when
 * false) whether the actual value of the current field is null or not.
 * </P>
 * </HTML></LI>
 * <LI><B>getterGenerated: </B><HTML>
 * <P width="300">
 * Generates a method for which the return value will be the value of this
 * field.
 * </P>
 * </HTML></LI>
 * <LI><B>setterGenerated: </B><HTML>
 * <P width="300">
 * Generates a method that will allow to set the value of this field.
 * </P>
 * </HTML></LI>
 * <LI><B>customSetterSignature: </B><HTML>
 * <P width="300">
 * Specifies a method that will be used to set this field value. Note that the
 * field control will then consider that the field is not &quot;get-only&quot;.
 * </P>
 * </HTML></LI>
 * <LI><B>fieldName: </B><HTML>
 * <P width="300">
 * The name of the current field.
 * </P>
 * </HTML></LI>
 * <LI><B>customFieldCaption: </B><HTML>
 * <P width="300">
 * Allows to specify the display name of this field. If not set a default
 * display name is provided.
 * </P>
 * </HTML></LI>
 * <LI><B>hidden: </B><HTML>
 * <P width="300">
 * Allows to hide this field.
 * </P>
 * </HTML></LI>
 * <LI><B>getOnlyForced: </B><HTML>
 * <P width="300">
 * Allows to specify that this field value (the reference or the primitive
 * value) cannot be set. Note that it does not mean that the field is read-only
 * unless its value is immutable.
 * </P>
 * </HTML></LI>
 * <LI><B>transientForced: </B><HTML>
 * <P width="300">
 * Allows to specify if this field update will affect the undo management
 * (checked) or not (unchecked).
 * </P>
 * </HTML></LI>
 * <LI><B>onlineHelp: </B><HTML>
 * <P width="300">
 * Allows to specify a help message to be displayed for this field. An HTML
 * display will be used if the help message is surrounded by HTML tags
 * (&lt;HTML&gt;...&lt;/HTML&gt;).
 * </P>
 * </HTML></LI>
 * <LI><B>category: </B><HTML>
 * <P width="300">
 * Allows to specify the category in which this field will be put. Note that the
 * possible categories are declared at the type level.
 * </P>
 * </HTML></LI>
 * <LI><B>categoryCaption: </B><HTML>
 * <P width="300">
 * Allows to specify the category in which this field will be put. Note that the
 * possible categories are declared at the type level.
 * </P>
 * </HTML></LI>
 * <LI><B>formControlCreationForced: </B><HTML>
 * <P width="300">
 * Forces this field to be displayed by a generic control (a form or a button
 * that will open a child dialog containing the form).
 * </P>
 * </HTML></LI>
 * <LI><B>displayArea: </B><HTML>
 * <P width="300">
 * Allows to specify how to distribute extra space. If the layout is smaller
 * than the area it needs to fill, the extra space is distributed to each field
 * control in proportion to its specified vertical/horizontal weight. A control
 * that has a weight of zero receives no extra space. If all the weights are
 * zero, all the extra space appears between the grids of the cell and the
 * edges. The default weight is 1. The value should be non-negative.
 * </P>
 * </HTML></LI>
 * <LI><B>encapsulationFieldName: </B><HTML>
 * <P width="300">
 * Specifies the name of the generated field that will encapsulate this field.
 * </P>
 * </HTML></LI>
 * <LI><B>typeConversion: </B><HTML>
 * <P width="300">
 * Specifies that this field value must be converted and how the conversion will
 * be managed.
 * </P>
 * </HTML></LI>
 * <LI><B>displayedAsSingletonList: </B><HTML>
 * <P width="300">
 * Specifies that this field value must be converted to a list containing a
 * single value. Note that he list will be empty if the value is null.
 * </P>
 * </HTML></LI>
 * <LI><B>valueOptionsFieldName: </B><HTML>
 * <P width="300">
 * Specifies that this field value will be chosen among enumerated values,
 * </P>
 * </HTML></LI>
 * <LI><B>customValueReturnMode: </B><HTML>
 * <P width="300">
 * Specifies how the value of this field is obtained. It will affect the way the
 * field value is modified. Typically calculated values cannot really be
 * modified and then are systematically reset (refreshed actually) after a
 * modification.
 * </P>
 * </HTML></LI>
 * <LI><B>duplicateGenerated: </B><HTML>
 * <P width="300">
 * Allows to duplicate this field.
 * </P>
 * </HTML></LI>
 * <LI><B>autoUpdatePeriodMilliseconds: </B><HTML>
 * <P width="300">
 * Specifies a periodic delay after which this field value will be automatically
 * refreshed.
 * </P>
 * </HTML></LI>
 * <LI><B>specificTypeCustomizations: </B><HTML>
 * <P width="300">
 * Customizations (list customization, enumeration customization, ...) that are
 * exclusively bound to the current field (not globally bound to the type of the
 * field).
 * </P>
 * </HTML></LI>
 * </UL>
 * <H3>MethodCustomization</H3>
 * <UL>
 * <LI><B>customValueReturnMode: </B><HTML>
 * <P width="300">
 * Specifies how the return value of this method is obtained. It will affect the
 * way this return value is modified. Typically calculated values cannot really
 * be modified and then are systematically reset (actually refreshed) after a
 * modification.
 * </P>
 * </HTML></LI>
 * <LI><B>enablementStatusFieldName: </B><HTML>
 * <P width="300">
 * Allows to control the enablement of the current method control by specifying
 * a sibling boolean field (in the same containing type) that will be used to
 * retrieve the control enablement status (true if enabled, false otherwise).
 * </P>
 * </HTML></LI>
 * <LI><B>confirmationMessage: </B><HTML>
 * <P width="300">
 * If provided, then a confirmation message will be displayed before performimg
 * the method action.
 * </P>
 * </HTML></LI>
 * <LI><B>parametersValidationCustomCaption: </B><HTML>
 * <P width="300">
 * Allows to change the 'validate' button text of the parameters dialog of this
 * method.
 * </P>
 * </HTML></LI>
 * <LI><B>returnValueFieldGenerated: </B><HTML>
 * <P width="300">
 * Allows to generate a field that will reflect the return value of this method.
 * Note that the method must not have any parameter.
 * </P>
 * </HTML></LI>
 * <LI><B>methodName: </B><HTML>
 * <P width="300">
 * The name of the current method.
 * </P>
 * </HTML></LI>
 * <LI><B>menuLocation: </B><HTML>
 * <P width="300">
 * Allows to choose a menu location for this method. Note that menu locations
 * are declared at the type level.
 * </P>
 * </HTML></LI>
 * <LI><B>nullReturnValueLabel: </B><HTML>
 * <P width="300">
 * Allows to specify a text to be used when displaying null return values of
 * this method.
 * </P>
 * </HTML></LI>
 * <LI><B>encapsulationFieldName: </B><HTML>
 * <P width="300">
 * Specifies the name of the generated field that will encapsulate this method.
 * </P>
 * </HTML></LI>
 * <LI><B>methodSignature: </B><HTML>
 * <P width="300">
 * The signature of the current method.
 * </P>
 * </HTML></LI>
 * <LI><B>customMethodCaption: </B><HTML>
 * <P width="300">
 * The display name of this method. If not set, a default display name is
 * provided.
 * </P>
 * </HTML></LI>
 * <LI><B>hidden: </B><HTML>
 * <P width="300">
 * Allows to hide this method.
 * </P>
 * </HTML></LI>
 * <LI><B>readOnlyForced: </B><HTML>
 * <P width="300">
 * Allows to specify if this method is read-only or not. Read-only methods will
 * not affect the undo management.
 * </P>
 * </HTML></LI>
 * <LI><B>invocationData: </B><HTML>
 * <P width="300">
 * Allows to generate zero-parameter methods that will use predefined parameter
 * values to call the current method.
 * </P>
 * </HTML></LI>
 * <LI><B>parameterizedFieldsGroup: </B><HTML>
 * <P width="300">
 * Allows to specify a list of fields of the parent type that will be displayed
 * as if they were parameters of the current method.
 * </P>
 * </HTML></LI>
 * <LI><B>onlineHelp: </B><HTML>
 * <P width="300">
 * Allows to display a help message for this method. An HTML display will be
 * used if the help message is surrounded by HTML tags
 * (&lt;HTML&gt;...&lt;/HTML&gt;).
 * </P>
 * </HTML></LI>
 * <LI><B>iconImagePath: </B><HTML>
 * <P width="300">
 * Specifies an icon image to be used for the display of this method.
 * </P>
 * </HTML></LI>
 * <LI><B>category: </B><HTML>
 * <P width="300">
 * Allows to specify the category in which this method will be put. Note that
 * the possible categories are declared at the type level.
 * </P>
 * </HTML></LI>
 * <LI><B>categoryCaption: </B><HTML>
 * <P width="300">
 * Allows to specify the category in which this method will be put. Note that
 * the possible categories are declared at the type level.
 * </P>
 * </HTML></LI>
 * <LI><B>detachedReturnValueForced: </B><HTML>
 * <P width="300">
 * If checked, then the return value dialog will not be blocking.
 * </P>
 * </HTML></LI>
 * <LI><B>ignoredReturnValueForced: </B><HTML>
 * <P width="300">
 * If checked, then the return value dialog will not be displayed.
 * </P>
 * </HTML></LI>
 * <LI><B>validating: </B><HTML>
 * <P width="300">
 * If checked, then this method will be executed during the owner object
 * validation. The method must throw an exception if the owner object state is
 * not valid. Note that the method is executed concurrently by a validation
 * thread while the owner object is possibly accessed/modified by another
 * thread.
 * </P>
 * </HTML></LI>
 * <LI><B>duplicateGenerated: </B><HTML>
 * <P width="300">
 * Allows to duplicate this method.
 * </P>
 * </HTML></LI>
 * <LI><B>runWhenObjectShown: </B><HTML>
 * <P width="300">
 * If checked, then this method will be executed when the owner object form is
 * shown.
 * </P>
 * </HTML></LI>
 * <LI><B>runWhenObjectHidden: </B><HTML>
 * <P width="300">
 * If checked, then this method will be executed when the owner object form is
 * hidden.
 * </P>
 * </HTML></LI>
 * <LI><B>transactionalRole: </B><HTML>
 * <P width="300">
 * Specifies whether and how the current method must be used to realize
 * transactions on host objects. Transactions are typically used to
 * accept/reject object dialog modifications. Possible values:
 * </P>
 * <UL>
 * <LI>
 * <P width="300">
 * BEGIN: the method will be used to initialize the transactions (typically when
 * opening an object dialog).
 * </P>
 * </LI>
 * <LI>
 * <P width="300">
 * COMMIT: the method will be used to complete the transactions (typically when
 * click on an object dialog 'OK' button).
 * </P>
 * </LI>
 * <LI>
 * <P width="300">
 * ROLLBACK: the method will be used to abort the transactions (typically when
 * click on an object dialog 'CANCEL' button).
 * </P>
 * </LI>
 * </UL>
 * <P width="300">
 * </P>
 * </HTML></LI>
 * </UL>
 * <H3>ListCustomization</H3>
 * <UL>
 * <LI><B>itemContructorSelectableforced: </B><HTML>
 * <P width="300">
 * Specifies that the item constructor should be asked when the user requests
 * the instanciation of an item. Note that known sub-classes constructors will
 * be proposed too.
 * </P>
 * </HTML></LI>
 * <LI><B>itemDetailsViewDisabled: </B><HTML>
 * <P width="300">
 * Allows to remove the ability to view item details.
 * </P>
 * </HTML></LI>
 * <LI><B>itemNullValueAllowed: </B><HTML>
 * <P width="300">
 * Whether null items can be inserted in the list or not.
 * </P>
 * </HTML></LI>
 * <LI><B>newItems: </B><HTML>
 * <P width="300">
 * Option describing how the UI reacts to item creation requests.
 * </P>
 * </HTML></LI>
 * <LI><B>allowedItemFieldShortcuts: </B><HTML>
 * <P width="300">
 * Allows to specify fields of the currently selected item that will be
 * accessible from the toolbar.
 * </P>
 * </HTML></LI>
 * <LI><B>allowedItemMethodShortcuts: </B><HTML>
 * <P width="300">
 * Allows to specify methods of the currently selected item that will be
 * accessible from the toolbar.
 * </P>
 * </HTML></LI>
 * <LI><B>fieldsExcludedFromItemDetails: </B><HTML>
 * <P width="300">
 * Allows to specify fields of the currently selected item that will be excluded
 * from the details view. The exclusion is based on the field name.
 * </P>
 * </HTML></LI>
 * <LI><B>methodsExcludedFromItemDetails: </B><HTML>
 * <P width="300">
 * Allows to specify methods of the currently selected item that will be
 * excluded from the details view. The exclusion is based on the method
 * signature.
 * </P>
 * </HTML></LI>
 * <LI><B>customDetailsAccessMode: </B><HTML>
 * <P width="300">
 * Allows to choose how to display the item details view. Typically the item
 * details view will be:
 * </P>
 * <UL>
 * <LI>
 * <P width="300">
 * embedded (in the same form as the list). or
 * </P>
 * </LI>
 * <LI>
 * <P width="300">
 * detached (in a child dialog).
 * </P>
 * </LI>
 * </UL>
 * <P width="300">
 * </P>
 * </HTML></LI>
 * <LI><B>customItemTypeFinder: </B><HTML>
 * <P width="300">
 * Specification of an item type that will replace the discovered item type.
 * Note that it must be compatible with the initial item type (typically a
 * sub-type).
 * </P>
 * </HTML></LI>
 * <LI><B>fieldColumnsAdded: </B><HTML>
 * <P width="300">
 * Allows to add columns corresponding the fields of the item type.
 * </P>
 * </HTML></LI>
 * <LI><B>itemTypeColumnAdded: </B><HTML>
 * <P width="300">
 * Allows to add a column displaying the name of the current item type. Useful
 * typically when there are multiple item types.
 * </P>
 * </HTML></LI>
 * <LI><B>positionColumnAdded: </B><HTML>
 * <P width="300">
 * Allows to add a column displaying the item position.
 * </P>
 * </HTML></LI>
 * <LI><B>listTypeName: </B><HTML>
 * <P width="300">
 * The name of this list type.
 * </P>
 * </HTML></LI>
 * <LI><B>stringValueColumnAdded: </B><HTML>
 * <P width="300">
 * Allows to add a column displaying the item toString() return value.
 * </P>
 * </HTML></LI>
 * <LI><B>columnCustomizations: </B><HTML>
 * <P width="300">
 * The list of discovered customizable columns.
 * </P>
 * </HTML></LI>
 * <LI><B>treeStructureDiscoverySettings: </B><HTML>
 * <P width="300">
 * Allows to display the sub-list(s) of items contained in lists of this type.
 * The value of an item field will be considered as a sub-list if:
 * </P>
 * <UL>
 * <LI>
 * <P width="300">
 * this value is a list,
 * </P>
 * </LI>
 * <LI>
 * <P width="300">
 * the &quot;Heterogeneous Sub-List(s)&quot; option is:
 * </P>
 * <UL>
 * <LI>
 * <P width="300">
 * checked or
 * </P>
 * </LI>
 * <LI>
 * <P width="300">
 * unchecked and the item type of this sub-list is the same as the current
 * (root) list item type.
 * </P>
 * </LI>
 * </UL>
 * <P width="300">
 * </P>
 * </LI>
 * </UL>
 * <P width="300">
 * If there is more than 1 discovered sub-list then an intermediary virtual
 * sub-list displaying the source item fields (their captions actually) will be
 * created.
 * </P>
 * </HTML></LI>
 * <LI><B>itemTypeName: </B><HTML>
 * <P width="300">
 * The name of the type of items.
 * </P>
 * </HTML></LI>
 * <LI><B>editOptions: </B><HTML>
 * <P width="300">
 * Specifies if lists of this type are editable and at what extent.
 * </P>
 * </HTML></LI>
 * <LI><B>length: </B><HTML>
 * <P width="300">
 * Specifies a custom length (vertical) of lists of this type.
 * </P>
 * </HTML></LI>
 * <LI><B>listSorted: </B><HTML>
 * <P width="300">
 * Specifies that items of lists of this type should be sorted. Note that these
 * items must be comparable (implement the java.lang.Comparable interface).
 * </P>
 * </HTML></LI>
 * </UL>
 * <H3>EnumerationCustomization</H3>
 * <UL>
 * <LI><B>enumerationTypeName: </B><HTML>
 * <P width="300">
 * The name of this enumeration type.
 * </P>
 * </HTML></LI>
 * <LI><B>dynamicEnumerationForced: </B><HTML>
 * <P width="300">
 * If checked, then the enumerated values will be regularly refreshed.
 * </P>
 * </HTML></LI>
 * <LI><B>itemCustomizations: </B><HTML>
 * <P width="300">
 * The list discovered customizable enumeration items.
 * </P>
 * </HTML></LI>
 * </UL>
 * 
 * @author olitank
 *
 */
@XmlRootElement
public class InfoCustomizations implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String UID_FIELD_NAME = "uniqueIdentifier";
	public static final Object INITIAL_STATE_FIELD_NAME = "initial";

	public static InfoCustomizations defaultInstance;
	/**
	 * Spell error to be fixed: should be "application" not "appplication". The
	 * customizations files should be migrated automatically.
	 */
	protected ApplicationCustomization appplicationCustomization = new ApplicationCustomization();
	protected List<TypeCustomization> typeCustomizations = new ArrayList<InfoCustomizations.TypeCustomization>();
	protected List<ListCustomization> listCustomizations = new ArrayList<InfoCustomizations.ListCustomization>();
	protected List<EnumerationCustomization> enumerationCustomizations = new ArrayList<InfoCustomizations.EnumerationCustomization>();

	protected transient Migrator migrator = new Migrator();

	/**
	 * @return the default instance of this class. Note that it may try to load the
	 *         default customization file according these system properties:
	 *         {@link SystemProperties#DEFAULT_INFO_CUSTOMIZATIONS_ACTIVE} and
	 *         {@link SystemProperties#DEFAULT_INFO_CUSTOMIZATIONS_FILE_PATH}
	 */
	public static InfoCustomizations getDefault() {
		if (defaultInstance == null) {
			defaultInstance = new InfoCustomizations() {

				private static final long serialVersionUID = 1L;

				@Override
				public String toString() {
					return "InfoCustomizations.DEFAULT";
				}
			};
			if (SystemProperties.areDefaultInfoCustomizationsActive()) {
				String filePath = SystemProperties.getDefaultInfoCustomizationsFilePath();
				File file = new File(filePath);
				if (file.exists()) {
					try {
						defaultInstance.loadFromFile(file, null);
					} catch (Throwable t) {
						throw new ReflectionUIError(t);
					}
				}
			}
		}
		return defaultInstance;
	}

	/**
	 * The default constructor. Builds an empty instance.
	 */
	public InfoCustomizations() {
	}

	public ApplicationCustomization getAppplicationCustomization() {
		return appplicationCustomization;
	}

	public void setAppplicationCustomization(ApplicationCustomization appplicationCustomization) {
		this.appplicationCustomization = appplicationCustomization;
	}

	public List<TypeCustomization> getTypeCustomizations() {
		return typeCustomizations;
	}

	public void setTypeCustomizations(List<TypeCustomization> typeCustomizations) {
		this.typeCustomizations = typeCustomizations;
	}

	public List<ListCustomization> getListCustomizations() {
		return listCustomizations;
	}

	public void setListCustomizations(List<ListCustomization> listCustomizations) {
		this.listCustomizations = listCustomizations;
	}

	public List<EnumerationCustomization> getEnumerationCustomizations() {
		return enumerationCustomizations;
	}

	public void setEnumerationCustomizations(List<EnumerationCustomization> enumerationCustomizations) {
		this.enumerationCustomizations = enumerationCustomizations;
	}

	public void loadFromFile(File input, Listener<String> debugLogListener) throws IOException {
		FileInputStream stream = new FileInputStream(input);
		try {
			loadFromStream(stream, debugLogListener);
		} finally {
			try {
				stream.close();
			} catch (Exception ignore) {
			}
		}
	}

	public void loadFromStream(InputStream input, Listener<String> debugLogListener) throws IOException {
		InfoCustomizations loaded;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(InfoCustomizations.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			loaded = (InfoCustomizations) jaxbUnmarshaller.unmarshal(input);
		} catch (Exception e) {
			throw new IOException(e);
		}
		appplicationCustomization = loaded.appplicationCustomization;
		typeCustomizations = loaded.typeCustomizations;
		listCustomizations = loaded.listCustomizations;
		enumerationCustomizations = loaded.enumerationCustomizations;

		fillXMLSerializationGap();
		migrator.migrate();
	}

	protected void fillXMLSerializationGap() {
		for (TypeCustomization t : typeCustomizations) {
			for (MethodCustomization mc : t.methodsCustomizations) {
				if (mc.menuLocation != null) {
					for (IMenuItemContainerCustomization container : InfoCustomizations
							.getAllMenuItemContainerCustomizations(t)) {
						if (((AbstractCustomization) mc.menuLocation).getUniqueIdentifier()
								.equals(((AbstractCustomization) container).getUniqueIdentifier())) {
							mc.menuLocation = container;
						}
					}
				}
			}
		}
	}

	public void saveToFile(File output, Listener<String> debugLogListener) throws IOException {
		saveToFile(output, debugLogListener, null);
	}

	public void saveToFile(File output, Listener<String> debugLogListener, final String comment) throws IOException {
		ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
		saveToStream(memoryStream, debugLogListener, comment);
		FileOutputStream stream = new FileOutputStream(output);
		try {
			stream.write(memoryStream.toByteArray());
		} finally {
			try {
				stream.close();
			} catch (Exception ignore) {
			}
		}
	}

	public void saveToStream(OutputStream output, Listener<String> debugLogListener) throws IOException {
		saveToStream(output, debugLogListener, null);
	}

	@SuppressWarnings("unchecked")
	public void saveToStream(OutputStream output, Listener<String> debugLogListener, final String comment)
			throws IOException {
		InfoCustomizations toSave = new InfoCustomizations();
		toSave.appplicationCustomization = (ApplicationCustomization) IOUtils
				.copyThroughSerialization((Serializable) appplicationCustomization);
		toSave.typeCustomizations = (List<TypeCustomization>) IOUtils
				.copyThroughSerialization((Serializable) typeCustomizations);
		toSave.listCustomizations = (List<ListCustomization>) IOUtils
				.copyThroughSerialization((Serializable) listCustomizations);
		toSave.enumerationCustomizations = (List<EnumerationCustomization>) IOUtils
				.copyThroughSerialization((Serializable) enumerationCustomizations);
		InfoCustomizations.clean(toSave, debugLogListener);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(InfoCustomizations.class);
			javax.xml.bind.Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			XMLStreamWriter jaxbXmlWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(output);
			jaxbXmlWriter.writeStartDocument();
			if (comment != null) {
				jaxbXmlWriter.writeCharacters("\n");
				jaxbXmlWriter.writeComment(comment);
			}
			jaxbXmlWriter.close();

			jaxbMarshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
			jaxbMarshaller.marshal(toSave, output);
		} catch (Exception e) {
			throw new IOException(e);
		}

	}

	public static void clean(InfoCustomizations infoCustomizations, Listener<String> debugLogListener) {
		for (TypeCustomization tc : new ArrayList<TypeCustomization>(infoCustomizations.getTypeCustomizations())) {
			for (FieldCustomization fc : new ArrayList<FieldCustomization>(tc.getFieldsCustomizations())) {
				clean(fc.getSpecificTypeCustomizations(), debugLogListener);
				if (fc.isInitial()) {
					tc.getFieldsCustomizations().remove(fc);
					continue;
				}
			}
			for (MethodCustomization mc : new ArrayList<MethodCustomization>(tc.getMethodsCustomizations())) {
				if (mc.isInitial()) {
					tc.getMethodsCustomizations().remove(mc);
					continue;
				}
			}
			if (tc.isInitial()) {
				if (debugLogListener != null) {
					debugLogListener
							.handle(InfoCustomizations.class.getName() + ": Serialization cleanup: Excluding " + tc);
				}
				infoCustomizations.getTypeCustomizations().remove(tc);
				continue;
			}

		}
		for (ListCustomization lc : new ArrayList<ListCustomization>(infoCustomizations.getListCustomizations())) {
			for (ColumnCustomization cc : new ArrayList<ColumnCustomization>(lc.getColumnCustomizations())) {
				if (cc.isInitial()) {
					lc.getColumnCustomizations().remove(cc);
					continue;
				}
			}
			if (lc.isInitial()) {
				if (debugLogListener != null) {
					debugLogListener
							.handle(InfoCustomizations.class.getName() + ": Serialization cleanup: Excluding " + lc);
				}
				infoCustomizations.getListCustomizations().remove(lc);
				continue;
			}

		}
		for (EnumerationCustomization ec : new ArrayList<EnumerationCustomization>(
				infoCustomizations.getEnumerationCustomizations())) {
			for (EnumerationItemCustomization ic : new ArrayList<EnumerationItemCustomization>(
					ec.getItemCustomizations())) {
				if (ic.isInitial()) {
					ec.getItemCustomizations().remove(ic);
					continue;
				}
			}
			if (ec.isInitial()) {
				if (debugLogListener != null) {
					debugLogListener
							.handle(InfoCustomizations.class.getName() + ": Serialization cleanup: Excluding " + ec);
				}
				infoCustomizations.getEnumerationCustomizations().remove(ec);
				continue;
			}
		}
	}

	public static boolean isSimilar(final AbstractCustomization c1, final AbstractCustomization c2,
			final String... excludedFieldNames) {
		return ReflectionUIUtils.equalsAccordingInfos(c1, c2, ReflectionUIUtils.STANDARD_REFLECTION, new IInfoFilter() {

			@Override
			public boolean excludeMethod(IMethodInfo method) {
				return false;
			}

			@Override
			public boolean excludeField(IFieldInfo field) {
				if (field.getName().equals(UID_FIELD_NAME)) {
					return true;
				}
				if (field.getName().equals(INITIAL_STATE_FIELD_NAME)) {
					return true;
				}
				if (Arrays.asList(excludedFieldNames).contains(field.getName())) {
					return true;
				}
				return false;
			}
		});
	}

	public static List<IMenuItemContainerCustomization> getAllMenuItemContainerCustomizations(TypeCustomization tc) {
		List<IMenuItemContainerCustomization> result = new ArrayList<IMenuItemContainerCustomization>();
		for (IMenuElementCustomization rootMenuElementCustomization : tc.getMenuModelCustomization()
				.getMenuCustomizations()) {
			if (rootMenuElementCustomization instanceof IMenuItemContainerCustomization) {
				result.addAll(getAllMenuItemContainerCustomizations(
						(IMenuItemContainerCustomization) rootMenuElementCustomization));
			}
		}
		return result;
	}

	public static List<IMenuItemContainerCustomization> getAllMenuItemContainerCustomizations(
			IMenuItemContainerCustomization from) {
		List<IMenuItemContainerCustomization> result = new ArrayList<IMenuItemContainerCustomization>();
		result.add(from);
		for (AbstractMenuItemCustomization item : from.getItemCustomizations()) {
			if (item instanceof IMenuItemContainerInfo) {
				result.addAll(getAllMenuItemContainerCustomizations((IMenuItemContainerCustomization) item));
			}
		}
		if (from instanceof MenuCustomization) {
			for (MenuItemCategoryCustomization item : ((MenuCustomization) from).getItemCategoryCustomizations()) {
				result.addAll(getAllMenuItemContainerCustomizations(item));
			}
		}
		return result;
	}

	public static List<String> getMemberCategoryCaptionOptions(InfoCustomizations infoCustomizations,
			AbstractMemberCustomization m) {
		TypeCustomization tc = findParentTypeCustomization(infoCustomizations, m);
		List<String> result = new ArrayList<String>();
		for (CustomizationCategory c : tc.getMemberCategories()) {
			result.add(c.getCaption());
		}
		return result;
	}

	public static TypeCustomization findParentTypeCustomization(InfoCustomizations infoCustomizations,
			AbstractMemberCustomization memberCustumization) {
		for (TypeCustomization tc : getTypeCustomizationsPlusFieldSpecificities(infoCustomizations)) {
			for (FieldCustomization fc : tc.getFieldsCustomizations()) {
				if (fc == memberCustumization) {
					return tc;
				}
			}
			for (MethodCustomization mc : tc.getMethodsCustomizations()) {
				if (mc == memberCustumization) {
					return tc;
				}
			}
		}
		return null;
	}

	public static List<TypeCustomization> getTypeCustomizationsPlusFieldSpecificities(
			InfoCustomizations infoCustomizations) {
		List<TypeCustomization> result = new ArrayList<TypeCustomization>();
		for (TypeCustomization tc : infoCustomizations.getTypeCustomizations()) {
			result.add(tc);
			for (FieldCustomization fc : tc.getFieldsCustomizations()) {
				result.addAll(getTypeCustomizationsPlusFieldSpecificities(fc.getSpecificTypeCustomizations()));
			}
		}
		return result;
	}

	public static boolean areInfoCustomizationsCreatedIfNotFound() {
		return SystemProperties.areInfoCustomizationsCreatedIfNotFound();
	}

	public static ParameterCustomization getParameterCustomization(MethodCustomization m, String paramName) {
		return getParameterCustomization(m, paramName, areInfoCustomizationsCreatedIfNotFound());
	}

	public static ParameterCustomization getParameterCustomization(MethodCustomization m, String paramName,
			boolean createIfNotFound) {
		if (m != null) {
			for (ParameterCustomization p : m.getParametersCustomizations()) {
				if (paramName.equals(p.getParameterName())) {
					return p;
				}
			}
			if (createIfNotFound) {
				ParameterCustomization p = new ParameterCustomization();
				p.setParameterName(paramName);
				m.getParametersCustomizations().add(p);
				return p;
			}
		}
		return null;
	}

	public static FieldCustomization getFieldCustomization(TypeCustomization t, String fieldName) {
		return getFieldCustomization(t, fieldName, areInfoCustomizationsCreatedIfNotFound());
	}

	public static FieldCustomization getFieldCustomization(TypeCustomization t, String fieldName,
			boolean createIfNotFound) {
		if (t != null) {
			for (FieldCustomization f : t.getFieldsCustomizations()) {
				if (fieldName.equals(f.getFieldName())) {
					return f;
				}
			}
			if (createIfNotFound) {
				FieldCustomization f = new FieldCustomization();
				f.setFieldName(fieldName);
				t.getFieldsCustomizations().add(f);
				return f;
			}
		}
		return null;
	}

	public static MethodCustomization getMethodCustomization(TypeCustomization t, String methodSignature) {
		return getMethodCustomization(t, methodSignature, areInfoCustomizationsCreatedIfNotFound());
	}

	public static MethodCustomization getMethodCustomization(TypeCustomization t, String methodSignature,
			boolean createIfNotFound) {
		if (t != null) {
			for (MethodCustomization m : t.getMethodsCustomizations()) {
				if (methodSignature.equals(m.getMethodSignature())) {
					return m;
				}
			}
			if (createIfNotFound) {
				MethodCustomization m = new MethodCustomization();
				m.setMethodSignature(methodSignature);
				t.getMethodsCustomizations().add(m);
				return m;
			}
		}
		return null;
	}

	public static TypeCustomization getTypeCustomization(InfoCustomizations infoCustomizations, String typeName) {
		return getTypeCustomization(infoCustomizations, typeName, areInfoCustomizationsCreatedIfNotFound());
	}

	public static TypeCustomization getTypeCustomization(InfoCustomizations infoCustomizations, String typeName,
			boolean createIfNotFound) {
		for (TypeCustomization t : infoCustomizations.getTypeCustomizations()) {
			if (typeName.equals(t.getTypeName())) {
				return t;
			}
		}
		if (createIfNotFound) {
			TypeCustomization t = new TypeCustomization();
			t.setTypeName(typeName);
			infoCustomizations.getTypeCustomizations().add(t);
			return t;
		}
		return null;
	}

	public static ListCustomization getListCustomization(InfoCustomizations infoCustomizations, String listTypeName,
			String itemTypeName) {
		return getListCustomization(infoCustomizations, listTypeName, itemTypeName,
				areInfoCustomizationsCreatedIfNotFound());
	}

	public static ListCustomization getListCustomization(InfoCustomizations infoCustomizations, String listTypeName,
			String itemTypeName, boolean createIfNotFound) {
		for (ListCustomization l : infoCustomizations.getListCustomizations()) {
			if (listTypeName.equals(l.getListTypeName())) {
				if (MiscUtils.equalsOrBothNull(l.getItemTypeName(), itemTypeName)) {
					return l;
				}
			}
		}
		if (createIfNotFound) {
			ListCustomization l = new ListCustomization();
			l.setListTypeName(listTypeName);
			l.setItemTypeName(itemTypeName);
			infoCustomizations.getListCustomizations().add(l);
			return l;
		}
		return null;
	}

	public static ColumnCustomization getColumnCustomization(ListCustomization l, String columnName) {
		return getColumnCustomization(l, columnName, areInfoCustomizationsCreatedIfNotFound());
	}

	public static ColumnCustomization getColumnCustomization(ListCustomization l, String columnName,
			boolean createIfNotFound) {
		for (ColumnCustomization c : l.getColumnCustomizations()) {
			if (columnName.equals(c.getColumnName())) {
				return c;
			}
		}
		if (createIfNotFound) {
			ColumnCustomization c = new ColumnCustomization();
			c.setColumnName(columnName);
			l.getColumnCustomizations().add(c);
			return c;
		}
		return null;
	}

	public static EnumerationItemCustomization getEnumerationItemCustomization(EnumerationCustomization e,
			String enumItemName) {
		return getEnumerationItemCustomization(e, enumItemName, areInfoCustomizationsCreatedIfNotFound());
	}

	public static EnumerationItemCustomization getEnumerationItemCustomization(EnumerationCustomization e,
			String enumItemName, boolean createIfNotFound) {
		for (EnumerationItemCustomization i : e.getItemCustomizations()) {
			if (enumItemName.equals(i.getItemName())) {
				return i;
			}
		}
		if (createIfNotFound) {
			EnumerationItemCustomization i = new EnumerationItemCustomization();
			i.setItemName(enumItemName);
			e.getItemCustomizations().add(i);
			return i;
		}
		return null;
	}

	public static EnumerationCustomization getEnumerationCustomization(InfoCustomizations infoCustomizations,
			String enumTypeName) {
		return getEnumerationCustomization(infoCustomizations, enumTypeName, areInfoCustomizationsCreatedIfNotFound());
	}

	public static EnumerationCustomization getEnumerationCustomization(InfoCustomizations infoCustomizations,
			String enumTypeName, boolean createIfNotFound) {
		for (EnumerationCustomization e : infoCustomizations.getEnumerationCustomizations()) {
			if (enumTypeName.equals(e.getEnumerationTypeName())) {
				return e;
			}
		}
		if (createIfNotFound) {
			EnumerationCustomization e = new EnumerationCustomization();
			e.setEnumerationTypeName(enumTypeName);
			infoCustomizations.getEnumerationCustomizations().add(e);
			return e;
		}
		return null;
	}

	public static <I extends IInfo> List<String> getInfosOrderAfterMove(List<I> list, I info, int offset) {
		int infoIndex = list.indexOf(info);
		int newInfoIndex = -1;
		int offsetSign = ((offset > 0) ? 1 : -1);
		InfoCategory infoCategory = ReflectionUIUtils.getCategory(info);
		int currentInfoIndex = infoIndex;
		for (int iOffset = 0; iOffset != offset; iOffset = iOffset + offsetSign) {
			int nextSameCategoryInfoIndex = -1;
			while (true) {
				currentInfoIndex += offsetSign;
				if ((offsetSign == -1) && (currentInfoIndex == -1)) {
					break;
				}
				if ((offsetSign == 1) && (currentInfoIndex == list.size())) {
					break;
				}
				I otherInfo = list.get(currentInfoIndex);
				if ((otherInfo instanceof IFieldInfo)) {
					if (((IFieldInfo) otherInfo).isHidden()) {
						continue;
					}
				}
				if ((otherInfo instanceof IMethodInfo)) {
					if (((IMethodInfo) otherInfo).isHidden()) {
						continue;
					}
				}
				InfoCategory otherInfoCategory = ReflectionUIUtils.getCategory(otherInfo);
				if (MiscUtils.equalsOrBothNull(infoCategory, otherInfoCategory)) {
					nextSameCategoryInfoIndex = currentInfoIndex;
					break;
				}
			}
			if (nextSameCategoryInfoIndex == -1) {
				break;
			} else {
				newInfoIndex = nextSameCategoryInfoIndex;
			}
		}

		if (newInfoIndex == -1) {
			throw new ReflectionUIError("Cannot move item: Limit reached");
		}

		List<I> resultList = new ArrayList<I>(list);
		resultList.remove(info);
		resultList.add(newInfoIndex, info);

		ArrayList<String> newOrder = new ArrayList<String>();
		for (I info2 : resultList) {
			String name = info2.getName();
			if (name == null) {
				throw new ReflectionUIError("Cannot move item: 'getName()' method returned <null> for item n°"
						+ (list.indexOf(info2) + 1) + " (caption='" + info2.getCaption() + "')");
			}
			newOrder.add(name);
		}
		return newOrder;
	}

	public static abstract class AbstractCustomization implements Serializable {
		private static final long serialVersionUID = 1L;

		public boolean isInitial() {
			try {
				return InfoCustomizations.isSimilar(this, getClass().newInstance());
			} catch (Exception e) {
				throw new ReflectionUIError(e);
			}
		}

		protected String uniqueIdentifier = new UID().toString();

		public String getUniqueIdentifier() {
			return uniqueIdentifier;
		}

		public void setUniqueIdentifier(String uniqueIdentifier) {
			this.uniqueIdentifier = uniqueIdentifier;
		}

	}

	public static abstract class AbstractInfoCustomization extends AbstractCustomization {

		private static final long serialVersionUID = 1L;

		protected Map<String, Object> specificProperties = new HashMap<String, Object>();

		public Map<String, Object> getSpecificProperties() {
			return specificProperties;
		}

		public void setSpecificProperties(Map<String, Object> specificProperties) {
			this.specificProperties = specificProperties;
		}

	}

	public static class VirtualFieldDeclaration extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected String fieldName;
		protected ITypeInfoFinder fieldTypeFinder;

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		@XmlElements({ @XmlElement(name = "javaClassBasedTypeInfoFinder", type = JavaClassBasedTypeInfoFinder.class),
				@XmlElement(name = "customTypeInfoFinder", type = CustomTypeInfoFinder.class) })
		public ITypeInfoFinder getFieldTypeFinder() {
			return fieldTypeFinder;
		}

		public void setFieldTypeFinder(ITypeInfoFinder fieldTypeFinder) {
			this.fieldTypeFinder = fieldTypeFinder;
		}

		public void validate() throws Exception {
			if ((fieldName == null) || (fieldName.length() == 0)) {
				throw new IllegalStateException("Field name not provided");
			}
			if (fieldTypeFinder == null) {
				throw new IllegalStateException("Field type not provided");
			}
		}

	}

	public static interface IMenuElementCustomization {

		public String getName();

	}

	public static interface IMenuItemContainerCustomization extends IMenuElementCustomization {
		public List<AbstractMenuItemCustomization> getItemCustomizations();
	}

	public static class MenuItemCategoryCustomization extends AbstractCustomization
			implements IMenuItemContainerCustomization {
		private static final long serialVersionUID = 1L;

		protected String name;
		protected List<AbstractMenuItemCustomization> itemCustomizations = new ArrayList<AbstractMenuItemCustomization>();

		public MenuItemCategoryCustomization() {
			name = "Category";
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@XmlElements({ @XmlElement(name = "menu", type = MenuCustomization.class),
				@XmlElement(name = "exitMenuItem", type = ExitMenuItemCustomization.class),
				@XmlElement(name = "helpMenuItem", type = HelpMenuItemCustomization.class),
				@XmlElement(name = "undoMenuItem", type = UndoMenuItemCustomization.class),
				@XmlElement(name = "redoMenuItem", type = RedoMenuItemCustomization.class),
				@XmlElement(name = "resetMenuItem", type = ResetMenuItemCustomization.class),
				@XmlElement(name = "openMenuItem", type = OpenMenuItemCustomization.class),
				@XmlElement(name = "saveMenuItem", type = SaveMenuItemCustomization.class),
				@XmlElement(name = "saveAsMenuItem", type = SaveAsMenuItemCustomization.class) })
		public List<AbstractMenuItemCustomization> getItemCustomizations() {
			return itemCustomizations;
		}

		public void setItemCustomizations(List<AbstractMenuItemCustomization> itemCustomizations) {
			this.itemCustomizations = itemCustomizations;
		}

		public void validate() {
			if ((name == null) || (name.trim().length() == 0)) {
				throw new ReflectionUIError("Name not provided");
			}
		}

	}

	public static abstract class AbstractMenuItemCustomization extends AbstractCustomization
			implements IMenuElementCustomization {
		private static final long serialVersionUID = 1L;

		protected String name = "";
		protected ResourcePath iconImagePath;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public ResourcePath getIconImagePath() {
			return iconImagePath;
		}

		public void setIconImagePath(ResourcePath iconImagePath) {
			this.iconImagePath = iconImagePath;
		}

		public void validate() {
			if ((name == null) || (name.trim().length() == 0)) {
				throw new ReflectionUIError("Name not provided");
			}
		}

	}

	public static abstract class AbstractStandardActionMenuItemCustomization extends AbstractMenuItemCustomization {

		private static final long serialVersionUID = 1L;

	}

	public static class ExitMenuItemCustomization extends AbstractStandardActionMenuItemCustomization {
		private static final long serialVersionUID = 1L;

		public ExitMenuItemCustomization() {
			name = "Exit";
		}

	}

	public static abstract class AbstractFileMenuItemCustomization extends AbstractStandardActionMenuItemCustomization {

		private static final long serialVersionUID = 1L;

		protected FileBrowserConfiguration fileBrowserConfiguration = new FileBrowserConfiguration();

		public FileBrowserConfiguration getFileBrowserConfiguration() {
			return fileBrowserConfiguration;
		}

		public void setFileBrowserConfiguration(FileBrowserConfiguration fileBrowserConfiguration) {
			this.fileBrowserConfiguration = fileBrowserConfiguration;
		}

	}

	public static abstract class AbstractSaveMenuItemCustomization extends AbstractFileMenuItemCustomization {

		protected static final long serialVersionUID = 1L;

	}

	public static class HelpMenuItemCustomization extends AbstractStandardActionMenuItemCustomization {

		private static final long serialVersionUID = 1L;

		public HelpMenuItemCustomization() {
			name = "Help";
		}

	}

	public static class OpenMenuItemCustomization extends AbstractFileMenuItemCustomization {

		protected static final long serialVersionUID = 1L;

		public OpenMenuItemCustomization() {
			name = "Open...";
			fileBrowserConfiguration.actionTitle = "Open";
		}
	}

	public static class SaveAsMenuItemCustomization extends AbstractSaveMenuItemCustomization {

		protected static final long serialVersionUID = 1L;

		public SaveAsMenuItemCustomization() {
			name = "Save As...";
			fileBrowserConfiguration.actionTitle = "Save";
		}

	}

	public static class SaveMenuItemCustomization extends AbstractSaveMenuItemCustomization {

		protected static final long serialVersionUID = 1L;

		public SaveMenuItemCustomization() {
			name = "Save";
			fileBrowserConfiguration.actionTitle = "Save";
		}

	}

	public static class RedoMenuItemCustomization extends AbstractStandardActionMenuItemCustomization {

		private static final long serialVersionUID = 1L;

		public RedoMenuItemCustomization() {
			name = "Redo";
		}

	}

	public static class ResetMenuItemCustomization extends AbstractStandardActionMenuItemCustomization {

		private static final long serialVersionUID = 1L;

		public ResetMenuItemCustomization() {
			name = "Reset";
		}

	}

	public static class UndoMenuItemCustomization extends AbstractStandardActionMenuItemCustomization {

		private static final long serialVersionUID = 1L;

		public UndoMenuItemCustomization() {
			name = "Undo";
		}

	}

	public static class MenuCustomization extends AbstractMenuItemCustomization
			implements IMenuItemContainerCustomization {
		private static final long serialVersionUID = 1L;

		protected List<AbstractMenuItemCustomization> itemCustomizations = new ArrayList<AbstractMenuItemCustomization>();
		protected List<MenuItemCategoryCustomization> itemCategoryCustomizations = new ArrayList<MenuItemCategoryCustomization>();

		public MenuCustomization() {
			name = "Menu";
		}

		@XmlElements({ @XmlElement(name = "menu", type = MenuCustomization.class),
				@XmlElement(name = "exitMenuItem", type = ExitMenuItemCustomization.class),
				@XmlElement(name = "helpMenuItem", type = HelpMenuItemCustomization.class),
				@XmlElement(name = "undoMenuItem", type = UndoMenuItemCustomization.class),
				@XmlElement(name = "redoMenuItem", type = RedoMenuItemCustomization.class),
				@XmlElement(name = "resetMenuItem", type = ResetMenuItemCustomization.class),
				@XmlElement(name = "openMenuItem", type = OpenMenuItemCustomization.class),
				@XmlElement(name = "saveMenuItem", type = SaveMenuItemCustomization.class),
				@XmlElement(name = "saveAsMenuItem", type = SaveAsMenuItemCustomization.class) })
		public List<AbstractMenuItemCustomization> getItemCustomizations() {
			return itemCustomizations;
		}

		public void setItemCustomizations(List<AbstractMenuItemCustomization> itemCustomizations) {
			this.itemCustomizations = itemCustomizations;
		}

		@XmlElement(name = "itemCategories")
		public List<MenuItemCategoryCustomization> getItemCategoryCustomizations() {
			return itemCategoryCustomizations;
		}

		public void setItemCategoryCustomizations(List<MenuItemCategoryCustomization> itemCategoryCustomizations) {
			this.itemCategoryCustomizations = itemCategoryCustomizations;
		}

		public void validate() {
			if ((name == null) || (name.trim().length() == 0)) {
				throw new ReflectionUIError("Name not provided");
			}
		}

	}

	public static class MenuModelCustomization extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected List<MenuCustomization> menuCustomizations = new ArrayList<MenuCustomization>();

		@XmlElement(name = "menus")
		public List<MenuCustomization> getMenuCustomizations() {
			return menuCustomizations;
		}

		public void setMenuCustomizations(List<MenuCustomization> menuCustomizations) {
			this.menuCustomizations = menuCustomizations;
		}

	}

	public enum FormSizeUnit {
		PIXELS, SCREEN_PERCENT
	}

	public static class FormSizeCustomization extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected FormSizeUnit unit = FormSizeUnit.SCREEN_PERCENT;
		protected int value = 50;

		public FormSizeUnit getUnit() {
			return unit;
		}

		public void setUnit(FormSizeUnit unit) {
			this.unit = unit;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((unit == null) ? 0 : unit.hashCode());
			result = prime * result + value;
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
			FormSizeCustomization other = (FormSizeCustomization) obj;
			if (unit != other.unit)
				return false;
			if (value != other.value)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "FormSizeCustomization [unit=" + unit + ", value=" + value + "]";
		}

	}

	public static class ApplicationCustomization extends AbstractInfoCustomization {
		private static final long serialVersionUID = 1L;

		protected String applicationName;
		protected String customApplicationCaption;
		protected String onlineHelp;
		protected ResourcePath mainBackgroundImagePath;
		protected ColorSpecification mainForegroundColor;
		protected ColorSpecification mainBackgroundColor;
		protected ColorSpecification mainBorderColor;
		protected ColorSpecification mainEditorBackgroundColor;
		protected ColorSpecification mainEditorForegroundColor;
		protected ResourcePath mainButtonBackgroundImagePath;
		protected ColorSpecification mainButtonForegroundColor;
		protected ColorSpecification mainButtonBackgroundColor;
		protected ColorSpecification mainButtonBorderColor;
		protected ColorSpecification titleForegroundColor;
		protected ColorSpecification titleBackgroundColor;
		protected ResourcePath iconImagePath;
		protected boolean systemIntegrationCrossPlatform;

		public String getApplicationName() {
			return applicationName;
		}

		public void setApplicationName(String applicationName) {
			this.applicationName = applicationName;
		}

		public boolean isSystemIntegrationCrossPlatform() {
			return systemIntegrationCrossPlatform;
		}

		public void setSystemIntegrationCrossPlatform(boolean systemIntegrationCrossPlatform) {
			this.systemIntegrationCrossPlatform = systemIntegrationCrossPlatform;
		}

		public String getCustomApplicationCaption() {
			return customApplicationCaption;
		}

		public ResourcePath getIconImagePath() {
			return iconImagePath;
		}

		public void setIconImagePath(ResourcePath iconImagePath) {
			this.iconImagePath = iconImagePath;
		}

		public void setCustomApplicationCaption(String customApplicationCaption) {
			this.customApplicationCaption = customApplicationCaption;
		}

		public String getOnlineHelp() {
			return onlineHelp;
		}

		public void setOnlineHelp(String onlineHelp) {
			this.onlineHelp = onlineHelp;
		}

		public ResourcePath getMainBackgroundImagePath() {
			return mainBackgroundImagePath;
		}

		public void setMainBackgroundImagePath(ResourcePath mainBackgroundImagePath) {
			this.mainBackgroundImagePath = mainBackgroundImagePath;
		}

		public ColorSpecification getMainForegroundColor() {
			return mainForegroundColor;
		}

		public void setMainForegroundColor(ColorSpecification mainForegroundColor) {
			this.mainForegroundColor = mainForegroundColor;
		}

		public ColorSpecification getMainBackgroundColor() {
			return mainBackgroundColor;
		}

		public void setMainBackgroundColor(ColorSpecification mainBackgroundColor) {
			this.mainBackgroundColor = mainBackgroundColor;
		}

		public ColorSpecification getMainEditorBackgroundColor() {
			return mainEditorBackgroundColor;
		}

		public void setMainEditorBackgroundColor(ColorSpecification mainEditorBackgroundColor) {
			this.mainEditorBackgroundColor = mainEditorBackgroundColor;
		}

		public ColorSpecification getMainEditorForegroundColor() {
			return mainEditorForegroundColor;
		}

		public void setMainEditorForegroundColor(ColorSpecification mainEditorForegroundColor) {
			this.mainEditorForegroundColor = mainEditorForegroundColor;
		}

		public ColorSpecification getMainBorderColor() {
			return mainBorderColor;
		}

		public void setMainBorderColor(ColorSpecification mainBorderColor) {
			this.mainBorderColor = mainBorderColor;
		}

		// for backward compatibility
		@Deprecated
		public ResourcePath getButtonBackgroundImagePath() {
			return mainButtonBackgroundImagePath;
		}

		// for backward compatibility
		@Deprecated
		public void setButtonBackgroundImagePath(ResourcePath mainButtonBackgroundImagePath) {
			this.mainButtonBackgroundImagePath = mainButtonBackgroundImagePath;
		}

		public ResourcePath getMainButtonBackgroundImagePath() {
			return mainButtonBackgroundImagePath;
		}

		public void setMainButtonBackgroundImagePath(ResourcePath mainButtonBackgroundImagePath) {
			this.mainButtonBackgroundImagePath = mainButtonBackgroundImagePath;
		}

		// for backward compatibility
		@Deprecated
		public ColorSpecification getButtonForegroundColor() {
			return mainButtonForegroundColor;
		}

		// for backward compatibility
		@Deprecated
		public void setButtonForegroundColor(ColorSpecification mainButtonForegroundColor) {
			this.mainButtonForegroundColor = mainButtonForegroundColor;
		}

		public ColorSpecification getMainButtonForegroundColor() {
			return mainButtonForegroundColor;
		}

		public void setMainButtonForegroundColor(ColorSpecification mainButtonForegroundColor) {
			this.mainButtonForegroundColor = mainButtonForegroundColor;
		}

		// for backward compatibility
		@Deprecated
		public ColorSpecification getButtonBackgroundColor() {
			return mainButtonBackgroundColor;
		}

		// for backward compatibility
		@Deprecated
		public void setButtonBackgroundColor(ColorSpecification mainButtonBackgroundColor) {
			this.mainButtonBackgroundColor = mainButtonBackgroundColor;
		}

		public ColorSpecification getMainButtonBackgroundColor() {
			return mainButtonBackgroundColor;
		}

		public void setMainButtonBackgroundColor(ColorSpecification mainButtonBackgroundColor) {
			this.mainButtonBackgroundColor = mainButtonBackgroundColor;
		}

		// for backward compatibility
		@Deprecated
		public ColorSpecification getButtonBorderColor() {
			return mainButtonBorderColor;
		}

		// for backward compatibility
		@Deprecated
		public void setButtonBorderColor(ColorSpecification mainButtonBorderColor) {
			this.mainButtonBorderColor = mainButtonBorderColor;
		}

		public ColorSpecification getMainButtonBorderColor() {
			return mainButtonBorderColor;
		}

		public void setMainButtonBorderColor(ColorSpecification mainButtonBorderColor) {
			this.mainButtonBorderColor = mainButtonBorderColor;
		}

		public ColorSpecification getTitleForegroundColor() {
			return titleForegroundColor;
		}

		public void setTitleForegroundColor(ColorSpecification titleForegroundColor) {
			this.titleForegroundColor = titleForegroundColor;
		}

		public ColorSpecification getTitleBackgroundColor() {
			return titleBackgroundColor;
		}

		public void setTitleBackgroundColor(ColorSpecification titleBackgroundColor) {
			this.titleBackgroundColor = titleBackgroundColor;
		}

		@Override
		public String toString() {
			return "ApplicationCustomization []";
		}

	}

	public static class TypeCustomization extends AbstractInfoCustomization implements Comparable<TypeCustomization> {
		private static final long serialVersionUID = 1L;

		protected String typeName;
		protected String baseTypeName;
		protected String customTypeCaption;
		protected String onlineHelp;
		protected List<FieldCustomization> fieldsCustomizations = new ArrayList<InfoCustomizations.FieldCustomization>();
		protected List<MethodCustomization> methodsCustomizations = new ArrayList<InfoCustomizations.MethodCustomization>();
		protected List<String> customFieldsOrder;
		protected List<String> customMethodsOrder;
		protected List<CustomizationCategory> memberCategories = new ArrayList<CustomizationCategory>();
		protected boolean undoManagementHidden = false;
		protected boolean immutableForced = false;
		protected boolean abstractForced = false;
		protected List<ITypeInfoFinder> polymorphicSubTypeFinders = new ArrayList<ITypeInfoFinder>();
		protected ResourcePath iconImagePath;
		protected ITypeInfo.FieldsLayout fieldsLayout;
		protected ITypeInfo.MethodsLayout methodsLayout;
		protected ITypeInfo.CategoriesStyle categoriesStyle;
		protected MenuModelCustomization menuModelCustomization = new MenuModelCustomization();
		protected boolean anyDefaultObjectMemberIncluded = false;
		protected boolean anyPersistenceMemberIncluded = false;
		protected List<VirtualFieldDeclaration> virtualFieldDeclarations = new ArrayList<VirtualFieldDeclaration>();
		protected FormSizeCustomization formWidth;
		protected FormSizeCustomization formHeight;
		protected ResourcePath formBackgroundImagePath;
		protected ColorSpecification formForegroundColor;
		protected ColorSpecification formBackgroundColor;
		protected ColorSpecification formBorderColor;
		protected ColorSpecification formEditorsForegroundColor;
		protected ColorSpecification formEditorsBackgroundColor;
		protected ColorSpecification categoriesForegroundColor;
		protected ColorSpecification categoriesBackgroundColor;
		protected ColorSpecification formButtonBackgroundColor;
		protected ColorSpecification formButtonForegroundColor;
		protected ColorSpecification formButtonBorderColor;
		protected ResourcePath formButtonBackgroundImagePath;
		protected String savingMethodName;
		protected String loadingMethodName;
		protected boolean copyForbidden = false;
		protected Integer formSpacing;

		@Override
		public boolean isInitial() {
			TypeCustomization defaultTypeCustomization = new TypeCustomization();
			defaultTypeCustomization.typeName = typeName;
			return InfoCustomizations.isSimilar(this, defaultTypeCustomization, "typeName");
		}

		public Integer getFormSpacing() {
			return formSpacing;
		}

		public void setFormSpacing(Integer formSpacing) {
			this.formSpacing = formSpacing;
		}

		public String getBaseTypeName() {
			return baseTypeName;
		}

		public void setBaseTypeName(String baseTypeName) {
			this.baseTypeName = baseTypeName;
		}

		public boolean isCopyForbidden() {
			return copyForbidden;
		}

		public void setCopyForbidden(boolean copyForbidden) {
			this.copyForbidden = copyForbidden;
		}

		public String getSavingMethodName() {
			return savingMethodName;
		}

		public void setSavingMethodName(String savingMethodName) {
			this.savingMethodName = savingMethodName;
		}

		public List<String> getSavingMethodNameOptions() {
			Class<?> javaType;
			try {
				javaType = ClassUtils.getCachedClassforName(typeName);
			} catch (ClassNotFoundException e) {
				return Collections.emptyList();
			}
			List<String> result = new ArrayList<String>();
			for (Method method : javaType.getMethods()) {
				if (Modifier.isStatic(method.getModifiers())) {
					continue;
				}
				if (!method.getReturnType().equals(void.class)) {
					continue;
				}
				List<Parameter> parameters = ClassUtils.getJavaParameters(method);
				if (parameters.size() != 1) {
					continue;
				}
				Parameter outputStreamParameter = parameters.get(0);
				if (!outputStreamParameter.getType().equals(OutputStream.class)) {
					continue;
				}
				result.add(method.getName());
			}
			Collections.sort(result);
			return result;
		}

		public String getLoadingMethodName() {
			return loadingMethodName;
		}

		public void setLoadingMethodName(String loadingMethodName) {
			this.loadingMethodName = loadingMethodName;
		}

		public List<String> getLoadingMethodNameOptions() {
			Class<?> javaType;
			try {
				javaType = ClassUtils.getCachedClassforName(typeName);
			} catch (ClassNotFoundException e) {
				return Collections.emptyList();
			}
			List<String> result = new ArrayList<String>();
			for (Method method : javaType.getMethods()) {
				if (Modifier.isStatic(method.getModifiers())) {
					continue;
				}
				if (!method.getReturnType().equals(void.class)) {
					continue;
				}
				List<Parameter> parameters = ClassUtils.getJavaParameters(method);
				if (parameters.size() != 1) {
					continue;
				}
				Parameter inputStreamParameter = parameters.get(0);
				if (!inputStreamParameter.getType().equals(InputStream.class)) {
					continue;
				}
				result.add(method.getName());
			}
			Collections.sort(result);
			return result;
		}

		public boolean isAnyPersistenceMemberIncluded() {
			return anyPersistenceMemberIncluded;
		}

		public void setAnyPersistenceMemberIncluded(boolean anyPersistenceMemberIncluded) {
			this.anyPersistenceMemberIncluded = anyPersistenceMemberIncluded;
		}

		public ITypeInfo.CategoriesStyle getCategoriesStyle() {
			return categoriesStyle;
		}

		public void setCategoriesStyle(ITypeInfo.CategoriesStyle categoriesStyle) {
			this.categoriesStyle = categoriesStyle;
		}

		public ResourcePath getFormBackgroundImagePath() {
			return formBackgroundImagePath;
		}

		public void setFormBackgroundImagePath(ResourcePath formBackgroundImagePath) {
			this.formBackgroundImagePath = formBackgroundImagePath;
		}

		public ColorSpecification getFormBackgroundColor() {
			return formBackgroundColor;
		}

		public void setFormBackgroundColor(ColorSpecification formBackgroundColor) {
			this.formBackgroundColor = formBackgroundColor;
		}

		public ColorSpecification getFormForegroundColor() {
			return formForegroundColor;
		}

		public void setFormForegroundColor(ColorSpecification formForegroundColor) {
			this.formForegroundColor = formForegroundColor;
		}

		public ColorSpecification getFormBorderColor() {
			return formBorderColor;
		}

		public void setFormBorderColor(ColorSpecification formBorderColor) {
			this.formBorderColor = formBorderColor;
		}

		public ColorSpecification getFormEditorsForegroundColor() {
			return formEditorsForegroundColor;
		}

		public void setFormEditorsForegroundColor(ColorSpecification formEditorsForegroundColor) {
			this.formEditorsForegroundColor = formEditorsForegroundColor;
		}

		public ColorSpecification getFormEditorsBackgroundColor() {
			return formEditorsBackgroundColor;
		}

		public void setFormEditorsBackgroundColor(ColorSpecification formEditorsBackgroundColor) {
			this.formEditorsBackgroundColor = formEditorsBackgroundColor;
		}

		public ColorSpecification getFormButtonBackgroundColor() {
			return formButtonBackgroundColor;
		}

		public void setFormButtonBackgroundColor(ColorSpecification formButtonBackgroundColor) {
			this.formButtonBackgroundColor = formButtonBackgroundColor;
		}

		public ColorSpecification getFormButtonForegroundColor() {
			return formButtonForegroundColor;
		}

		public void setFormButtonForegroundColor(ColorSpecification formButtonForegroundColor) {
			this.formButtonForegroundColor = formButtonForegroundColor;
		}

		public ColorSpecification getFormButtonBorderColor() {
			return formButtonBorderColor;
		}

		public void setFormButtonBorderColor(ColorSpecification formButtonBorderColor) {
			this.formButtonBorderColor = formButtonBorderColor;
		}

		public ResourcePath getFormButtonBackgroundImagePath() {
			return formButtonBackgroundImagePath;
		}

		public void setFormButtonBackgroundImagePath(ResourcePath formButtonBackgroundImagePath) {
			this.formButtonBackgroundImagePath = formButtonBackgroundImagePath;
		}

		public ColorSpecification getCategoriesForegroundColor() {
			return categoriesForegroundColor;
		}

		public void setCategoriesForegroundColor(ColorSpecification categoriesForegroundColor) {
			this.categoriesForegroundColor = categoriesForegroundColor;
		}

		public ColorSpecification getCategoriesBackgroundColor() {
			return categoriesBackgroundColor;
		}

		public void setCategoriesBackgroundColor(ColorSpecification categoriesBackgroundColor) {
			this.categoriesBackgroundColor = categoriesBackgroundColor;
		}

		public FormSizeCustomization getFormWidth() {
			return formWidth;
		}

		public void setFormWidth(FormSizeCustomization formWidth) {
			this.formWidth = formWidth;
		}

		public FormSizeCustomization getFormHeight() {
			return formHeight;
		}

		public void setFormHeight(FormSizeCustomization formHeight) {
			this.formHeight = formHeight;
		}

		public List<VirtualFieldDeclaration> getVirtualFieldDeclarations() {
			return virtualFieldDeclarations;
		}

		public void setVirtualFieldDeclarations(List<VirtualFieldDeclaration> virtualFieldDeclarations) {
			this.virtualFieldDeclarations = virtualFieldDeclarations;
		}

		public boolean isAnyDefaultObjectMemberIncluded() {
			return anyDefaultObjectMemberIncluded;
		}

		public void setAnyDefaultObjectMemberIncluded(boolean anyDefaultObjectMemberIncluded) {
			this.anyDefaultObjectMemberIncluded = anyDefaultObjectMemberIncluded;
		}

		@XmlElement(name = "menuModel")
		public MenuModelCustomization getMenuModelCustomization() {
			return menuModelCustomization;
		}

		public void setMenuModelCustomization(MenuModelCustomization menuModelCustomization) {
			this.menuModelCustomization = menuModelCustomization;
		}

		public ITypeInfo.FieldsLayout getFieldsLayout() {
			return fieldsLayout;
		}

		public void setFieldsLayout(ITypeInfo.FieldsLayout fieldsLayout) {
			this.fieldsLayout = fieldsLayout;
		}

		public ITypeInfo.MethodsLayout getMethodsLayout() {
			return methodsLayout;
		}

		public void setMethodsLayout(ITypeInfo.MethodsLayout methodsLayout) {
			this.methodsLayout = methodsLayout;
		}

		public ResourcePath getIconImagePath() {
			return iconImagePath;
		}

		public void setIconImagePath(ResourcePath iconImagePath) {
			this.iconImagePath = iconImagePath;
		}

		public String getTypeName() {
			return typeName;
		}

		public void setTypeName(String typeName) {
			this.typeName = typeName;
		}

		public boolean isAbstractForced() {
			return abstractForced;
		}

		public void setAbstractForced(boolean abtractForced) {
			this.abstractForced = abtractForced;
		}

		public boolean isImmutableForced() {
			return immutableForced;
		}

		public void setImmutableForced(boolean immutableForced) {
			this.immutableForced = immutableForced;
		}

		@XmlElements({ @XmlElement(name = "javaClassBasedTypeInfoFinder", type = JavaClassBasedTypeInfoFinder.class),
				@XmlElement(name = "customTypeInfoFinder", type = CustomTypeInfoFinder.class) })
		public List<ITypeInfoFinder> getPolymorphicSubTypeFinders() {
			return polymorphicSubTypeFinders;
		}

		public void setPolymorphicSubTypeFinders(List<ITypeInfoFinder> polymorphicSubTypeFinders) {
			this.polymorphicSubTypeFinders = polymorphicSubTypeFinders;
		}

		public List<CustomizationCategory> getMemberCategories() {
			return memberCategories;
		}

		public void setMemberCategories(List<CustomizationCategory> memberCategories) {
			this.memberCategories = memberCategories;
		}

		public List<String> getCustomFieldsOrder() {
			return customFieldsOrder;
		}

		public void setCustomFieldsOrder(List<String> customFieldsOrder) {
			this.customFieldsOrder = customFieldsOrder;
		}

		public List<String> getCustomMethodsOrder() {
			return customMethodsOrder;
		}

		public void setCustomMethodsOrder(List<String> customMethodsOrder) {
			this.customMethodsOrder = customMethodsOrder;
		}

		public String getCustomTypeCaption() {
			return customTypeCaption;
		}

		public void setCustomTypeCaption(String customTypeCaption) {
			this.customTypeCaption = customTypeCaption;
		}

		public List<FieldCustomization> getFieldsCustomizations() {
			return fieldsCustomizations;
		}

		public void setFieldsCustomizations(List<FieldCustomization> fieldsCustomizations) {
			this.fieldsCustomizations = fieldsCustomizations;
		}

		public List<MethodCustomization> getMethodsCustomizations() {
			return methodsCustomizations;
		}

		public void setMethodsCustomizations(List<MethodCustomization> methodsCustomizations) {
			this.methodsCustomizations = methodsCustomizations;
		}

		public String getOnlineHelp() {
			return onlineHelp;
		}

		public void setOnlineHelp(String onlineHelp) {
			this.onlineHelp = onlineHelp;
		}

		public boolean isUndoManagementHidden() {
			return undoManagementHidden;
		}

		public void setUndoManagementHidden(boolean undoManagementHidden) {
			this.undoManagementHidden = undoManagementHidden;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((typeName == null) ? 0 : typeName.hashCode());
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
			TypeCustomization other = (TypeCustomization) obj;
			if (typeName == null) {
				if (other.typeName != null)
					return false;
			} else if (!typeName.equals(other.typeName))
				return false;
			return true;
		}

		@Override
		public int compareTo(TypeCustomization o) {
			return MiscUtils.compareNullables(typeName, o.typeName);
		}

		@Override
		public String toString() {
			return "TypeCustomization [typeName=" + typeName + "]";
		}

	}

	public static class CustomizationCategory extends AbstractCustomization implements Serializable {

		private static final long serialVersionUID = 1L;

		protected String caption;
		protected ResourcePath iconImagePath;

		public String getCaption() {
			return caption;
		}

		public void setCaption(String caption) {
			this.caption = caption;
		}

		public ResourcePath getIconImagePath() {
			return iconImagePath;
		}

		public void setIconImagePath(ResourcePath iconImagePath) {
			this.iconImagePath = iconImagePath;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((caption == null) ? 0 : caption.hashCode());
			result = prime * result + ((iconImagePath == null) ? 0 : iconImagePath.hashCode());
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
			CustomizationCategory other = (CustomizationCategory) obj;
			if (caption == null) {
				if (other.caption != null)
					return false;
			} else if (!caption.equals(other.caption))
				return false;
			if (iconImagePath == null) {
				if (other.iconImagePath != null)
					return false;
			} else if (!iconImagePath.equals(other.iconImagePath))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "CustomizationCategory [caption=" + caption + ", iconImagePath=" + iconImagePath + "]";
		}

	}

	public static abstract class AbstractMemberCustomization extends AbstractInfoCustomization {
		private static final long serialVersionUID = 1L;

		protected boolean hidden = false;
		protected String categoryCaption;
		protected String onlineHelp;

		public boolean isHidden() {
			return hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}

		public String getCategoryCaption() {
			return categoryCaption;
		}

		public void setCategoryCaption(String categoryCaption) {
			this.categoryCaption = categoryCaption;
		}

		// for backward compatibility
		@Deprecated
		public CustomizationCategory getCategory() {
			if (categoryCaption == null) {
				return null;
			} else {
				CustomizationCategory result = new CustomizationCategory();
				result.setCaption(categoryCaption);
				return result;
			}
		}

		// for backward compatibility
		@Deprecated
		public void setCategory(CustomizationCategory category) {
			if (category == null) {
				this.categoryCaption = null;
			} else {
				this.categoryCaption = category.getCaption();
			}
		}

		public String getOnlineHelp() {
			return onlineHelp;
		}

		public void setOnlineHelp(String onlineHelp) {
			this.onlineHelp = onlineHelp;
		}
	}

	public static class FieldTypeSpecificities extends InfoCustomizations {
		private static final long serialVersionUID = 1L;

	}

	public static class ConversionMethodFinder extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected static final String NON_STATIC_METHOD_OWNER_AS_PARAMETER_NAME = "(The method owner)";

		protected String conversionClassName;
		protected String conversionMethodSignature;
		protected int chosenParameterPosition = -1;
		protected Map<Integer, TextualStorage> otherParameterValueStorages = new HashMap<Integer, InfoCustomizations.TextualStorage>();

		public String getConversionClassName() {
			return conversionClassName;
		}

		public void setConversionClassName(String conversionClassName) {
			this.conversionClassName = conversionClassName;
		}

		public String getConversionMethodSignature() {
			return conversionMethodSignature;
		}

		public void setConversionMethodSignature(String conversionMethodSignature) {
			this.conversionMethodSignature = conversionMethodSignature;
		}

		public int getChosenParameterPosition() {
			return chosenParameterPosition;
		}

		public void setChosenParameterPosition(int chosenParameterPosition) {
			this.chosenParameterPosition = chosenParameterPosition;
		}

		public Map<Integer, TextualStorage> getOtherParameterValueStorages() {
			return otherParameterValueStorages;
		}

		public void setOtherParameterValueStorages(Map<Integer, TextualStorage> otherParameterValueStorages) {
			this.otherParameterValueStorages = otherParameterValueStorages;
		}

		public List<String> getConversionMethodSignatureOptions() {
			Class<?> conversionClass;
			try {
				conversionClass = ClassUtils.getCachedClassforName(conversionClassName);
			} catch (Exception e) {
				return Collections.emptyList();
			}
			List<String> result = new ArrayList<String>();
			for (Constructor<?> ctor : conversionClass.getConstructors()) {
				if (ctor.getParameterTypes().length >= 1) {
					result.add(ReflectionUIUtils.buildMethodSignature(
							new DefaultConstructorInfo(ReflectionUIUtils.STANDARD_REFLECTION, ctor)));
				}
			}
			for (Method method : conversionClass.getMethods()) {
				if (Modifier.isStatic(method.getModifiers())) {
					if (method.getParameterTypes().length >= 1) {
						if (!method.getReturnType().equals(void.class)) {
							result.add(ReflectionUIUtils.buildMethodSignature(
									new DefaultMethodInfo(ReflectionUIUtils.STANDARD_REFLECTION, method)));
						}
					}
				} else {
					if (method.getParameterTypes().length >= 0) {
						if (!method.getReturnType().equals(void.class)) {
							result.add(ReflectionUIUtils.buildMethodSignature(
									new DefaultMethodInfo(ReflectionUIUtils.STANDARD_REFLECTION, method)));
						}
					}
				}
			}
			Collections.sort(result);
			return result;
		}

		public Filter<Object> find() {
			if ((conversionClassName == null) || (conversionClassName.length() == 0)) {
				return null;
			}
			try {
				final Class<?> conversionClass = ClassUtils.getCachedClassforName(conversionClassName);
				if ((conversionMethodSignature == null) || (conversionMethodSignature.length() == 0)) {
					throw new ReflectionUIError("Conversion method not specified!");
				}
				final String conversionMethodName = ReflectionUIUtils
						.extractMethodNameFromSignature(conversionMethodSignature);
				String[] conversionMethodParameterTypeNames = ReflectionUIUtils
						.extractMethodParameterTypeNamesFromSignature(conversionMethodSignature);
				final Class<?>[] conversionMethodParameterTypes = new Class<?>[conversionMethodParameterTypeNames.length];
				for (int i = 0; i < conversionMethodParameterTypeNames.length; i++) {
					conversionMethodParameterTypes[i] = ClassUtils
							.getCachedClassforName(conversionMethodParameterTypeNames[i]);
				}
				if (conversionMethodName == null) {
					throw new ReflectionUIError("Malformed method signature: '" + conversionMethodSignature + "'");
				}
				if (conversionMethodName.length() == 0) {
					return new ConstructorBasedConverter(this, conversionClass, conversionMethodParameterTypes);
				} else {
					return new MethodBasedConverter(this, conversionClass, conversionMethodName,
							conversionMethodParameterTypes);
				}
			} catch (Throwable t) {
				throw new ReflectionUIError(t);
			}
		}

		protected Executable getExecutable(Filter<Object> converter) {
			if (converter instanceof ConstructorBasedConverter) {
				return ((ConstructorBasedConverter) converter).getConstructor();
			} else if (converter instanceof MethodBasedConverter) {
				return ((MethodBasedConverter) converter).getMethod();
			} else {
				throw new ReflectionUIError();
			}
		}

		protected String getParameterName(Executable executable, int parameterPosition) {
			if (executable instanceof Constructor) {
				DefaultConstructorInfo constructorInfo = new DefaultConstructorInfo(
						ReflectionUIUtils.STANDARD_REFLECTION, (Constructor<?>) executable);
				List<IParameterInfo> paramInfos = constructorInfo.getParameters();
				if (parameterPosition >= paramInfos.size()) {
					return null;
				}
				IParameterInfo paramInfo = paramInfos.get(parameterPosition);
				return paramInfo.getName();
			} else if (executable instanceof Method) {
				Method method = (Method) executable;
				DefaultMethodInfo methodInfo = new DefaultMethodInfo(ReflectionUIUtils.STANDARD_REFLECTION, method);
				List<IParameterInfo> paramInfos = methodInfo.getParameters();
				if (Modifier.isStatic(method.getModifiers())) {
					if (parameterPosition >= paramInfos.size()) {
						return null;
					}
					IParameterInfo paramInfo = paramInfos.get(parameterPosition);
					return paramInfo.getName();
				} else {
					if (parameterPosition == 0) {
						return NON_STATIC_METHOD_OWNER_AS_PARAMETER_NAME;
					}
					if ((parameterPosition - 1) >= paramInfos.size()) {
						return null;
					}
					IParameterInfo paramInfo = paramInfos.get((parameterPosition - 1));
					return paramInfo.getName();
				}
			} else {
				throw new ReflectionUIError();
			}
		}

		protected int getParameterCount(Executable executable) {
			if (executable instanceof Constructor) {
				Constructor<?> constructor = (Constructor<?>) executable;
				return constructor.getParameterTypes().length;
			} else if (executable instanceof Method) {
				Method method = (Method) executable;
				if (Modifier.isStatic(method.getModifiers())) {
					return method.getParameterTypes().length;
				} else {
					return method.getParameterTypes().length + 1;
				}
			} else {
				throw new ReflectionUIError();
			}
		}

		public boolean isParameterPluralityResolvingRequired() {
			Filter<Object> converter;
			try {
				converter = find();
			} catch (Throwable t) {
				return false;
			}
			if (converter == null) {
				return false;
			}
			Executable executable = getExecutable(converter);
			if (executable == null) {
				return false;
			}
			return getParameterCount(executable) > 1;
		}

		public List<String> getChosenParameterNameOptions() {
			if (!isParameterPluralityResolvingRequired()) {
				return Collections.emptyList();
			}
			Filter<Object> converter;
			try {
				converter = find();
			} catch (Throwable t) {
				return Collections.emptyList();
			}
			if (converter == null) {
				return Collections.emptyList();
			}
			Executable executable = getExecutable(converter);
			if (executable == null) {
				return Collections.emptyList();
			}
			List<String> result = new ArrayList<String>();
			int paramCount = getParameterCount(executable);
			for (int paramPosition = 0; paramPosition < paramCount; paramPosition++) {
				result.add(getParameterName(executable, paramPosition));
			}
			return result;
		}

		@XmlTransient
		public String getChosenParameterName() {
			if (!isParameterPluralityResolvingRequired()) {
				return null;
			}
			if (chosenParameterPosition == -1) {
				return null;
			}
			Filter<Object> converter;
			try {
				converter = find();
			} catch (Throwable t) {
				return null;
			}
			if (converter == null) {
				return null;
			}
			Executable executable = getExecutable(converter);
			if (executable == null) {
				return null;
			}
			return getParameterName(executable, chosenParameterPosition);
		}

		public void setChosenParameterName(String parameterName) {
			if (!isParameterPluralityResolvingRequired()) {
				throw new ReflectionUIError();
			}
			Filter<Object> converter;
			try {
				converter = find();
			} catch (Throwable t) {
				throw new ReflectionUIError(t);
			}
			if (converter == null) {
				throw new ReflectionUIError();
			}
			Executable executable = getExecutable(converter);
			if (executable == null) {
				throw new ReflectionUIError();
			}
			int paramCount = getParameterCount(executable);
			for (int paramPosition = 0; paramPosition < paramCount; paramPosition++) {
				String currentParameterName = getParameterName(executable, paramPosition);
				if (parameterName.equals(currentParameterName)) {
					chosenParameterPosition = paramPosition;
					break;
				}
			}
		}

		@XmlTransient
		public Map<String, TextualStorage> getOtherParameterValueStorageByName() {
			if (!isParameterPluralityResolvingRequired()) {
				return null;
			}
			Filter<Object> converter;
			try {
				converter = find();
			} catch (Throwable t) {
				return null;
			}
			if (converter == null) {
				return null;
			}
			Executable executable = getExecutable(converter);
			if (executable == null) {
				return null;
			}
			Map<String, TextualStorage> result = new LinkedHashMap<String, InfoCustomizations.TextualStorage>();
			int paramCount = getParameterCount(executable);
			for (int paramPosition = 0; paramPosition < paramCount; paramPosition++) {
				if (paramPosition == chosenParameterPosition) {
					continue;
				}
				TextualStorage storage = otherParameterValueStorages.get(paramPosition);
				if (storage == null) {
					storage = new TextualStorage();
				}
				result.put(getParameterName(executable, paramPosition), storage);
			}
			return result;
		}

		public void setOtherParameterValueStorageByName(Map<String, TextualStorage> map) {
			if (!isParameterPluralityResolvingRequired()) {
				throw new ReflectionUIError();
			}
			Filter<Object> converter;
			try {
				converter = find();
			} catch (Throwable t) {
				throw new ReflectionUIError(t);
			}
			if (converter == null) {
				throw new ReflectionUIError();
			}
			Executable executable = getExecutable(converter);
			if (executable == null) {
				throw new ReflectionUIError();
			}
			otherParameterValueStorages.clear();
			int paramCount = getParameterCount(executable);
			for (int paramPosition = 0; paramPosition < paramCount; paramPosition++) {
				String parameterName = getParameterName(executable, paramPosition);
				TextualStorage storage = map.get(parameterName);
				if (storage == null) {
					continue;
				}
				otherParameterValueStorages.put(paramPosition, storage);
			}
		}

		public void validate() {
			Filter<Object> converter;
			try {
				converter = find();
			} catch (Throwable t) {
				throw new ReflectionUIError(t);
			}
			if (converter == null) {
				return;
			}
			Executable executable = getExecutable(converter);
			if (executable == null) {
				throw new ReflectionUIError("Failed to retrieve the conversion method/constructor!");
			}
			if (isParameterPluralityResolvingRequired()) {
				if (getChosenParameterName() == null) {
					throw new ReflectionUIError("Conversion input parameter not selected!");
				}
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((conversionClassName == null) ? 0 : conversionClassName.hashCode());
			result = prime * result + ((conversionMethodSignature == null) ? 0 : conversionMethodSignature.hashCode());
			result = prime * result + ((getChosenParameterName() == null) ? 0 : getChosenParameterName().hashCode());
			result = prime * result + ((getOtherParameterValueStorageByName() == null) ? 0
					: getOtherParameterValueStorageByName().hashCode());
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
			ConversionMethodFinder other = (ConversionMethodFinder) obj;
			if (conversionClassName == null) {
				if (other.conversionClassName != null)
					return false;
			} else if (!conversionClassName.equals(other.conversionClassName))
				return false;
			if (conversionMethodSignature == null) {
				if (other.conversionMethodSignature != null)
					return false;
			} else if (!conversionMethodSignature.equals(other.conversionMethodSignature))
				return false;
			if (getChosenParameterName() == null) {
				if (other.getChosenParameterName() != null)
					return false;
			} else if (!getChosenParameterName().equals(other.getChosenParameterName()))
				return false;
			if (getOtherParameterValueStorageByName() == null) {
				if (other.getOtherParameterValueStorageByName() != null)
					return false;
			} else if (!getOtherParameterValueStorageByName().equals(other.getOtherParameterValueStorageByName()))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ConversionMethodFinder [conversionClassName=" + conversionClassName + ", conversionMethodSignature="
					+ conversionMethodSignature
					+ (isParameterPluralityResolvingRequired()
							? (", chosenParameterName=" + getChosenParameterName()
									+ ", otherParameterValueStorageByName=" + getOtherParameterValueStorageByName())
							: "")
					+ "]";
		}

		protected static class MethodBasedConverter implements Filter<Object> {

			protected Method method;
			protected int parameterCount;
			protected boolean parameterPluralityResolvingRequired;
			protected int chosenParameterPosition;
			protected Map<Integer, TextualStorage> otherParameterValueStorages;

			public MethodBasedConverter(ConversionMethodFinder conversionMethodFinder, Class<?> theClass,
					String methodName, Class<?>[] parameterTypes) {
				try {
					this.method = theClass.getMethod(methodName, parameterTypes);
				} catch (NoSuchMethodException e) {
					throw new ReflectionUIError(e);
				} catch (SecurityException e) {
					throw new ReflectionUIError(e);
				}
				this.parameterCount = conversionMethodFinder.getParameterCount(method);
				this.parameterPluralityResolvingRequired = parameterCount > 1;
				this.otherParameterValueStorages = conversionMethodFinder.getOtherParameterValueStorages();
				this.chosenParameterPosition = conversionMethodFinder.getChosenParameterPosition();
			}

			@Override
			public Object get(Object obj) {
				try {
					Method method = getMethod();
					if (parameterPluralityResolvingRequired) {
						Object[] paramValues = new Object[parameterCount];
						for (int paramPosition = 0; paramPosition < paramValues.length; paramPosition++) {
							if (paramPosition == chosenParameterPosition) {
								paramValues[paramPosition] = obj;
							} else {
								TextualStorage storage = otherParameterValueStorages.get(paramPosition);
								if (storage == null) {
									continue;
								}
								paramValues[paramPosition] = storage.load();
							}
						}
						if (Modifier.isStatic(method.getModifiers())) {
							return method.invoke(null, paramValues);
						} else {
							return method.invoke(paramValues[0],
									Arrays.copyOfRange(paramValues, 1, paramValues.length));
						}
					} else {
						if (Modifier.isStatic(method.getModifiers())) {
							return method.invoke(null, obj);
						} else {
							return method.invoke(obj);
						}
					}
				} catch (InvocationTargetException e) {
					throw new ReflectionUIError(e.getTargetException());
				} catch (Exception e) {
					throw new ReflectionUIError("Failed to convert '" + obj + "' with " + this + ": " + e.toString(),
							e);
				}
			}

			public Method getMethod() {
				return method;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + chosenParameterPosition;
				result = prime * result + ((method == null) ? 0 : method.hashCode());
				result = prime * result
						+ ((otherParameterValueStorages == null) ? 0 : otherParameterValueStorages.hashCode());
				result = prime * result + parameterCount;
				result = prime * result + (parameterPluralityResolvingRequired ? 1231 : 1237);
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
				MethodBasedConverter other = (MethodBasedConverter) obj;
				if (chosenParameterPosition != other.chosenParameterPosition)
					return false;
				if (method == null) {
					if (other.method != null)
						return false;
				} else if (!method.equals(other.method))
					return false;
				if (otherParameterValueStorages == null) {
					if (other.otherParameterValueStorages != null)
						return false;
				} else if (!otherParameterValueStorages.equals(other.otherParameterValueStorages))
					return false;
				if (parameterCount != other.parameterCount)
					return false;
				if (parameterPluralityResolvingRequired != other.parameterPluralityResolvingRequired)
					return false;
				return true;
			}

			@Override
			public String toString() {
				return "MethodBasedConverter [method=" + method + ", parameterCount=" + parameterCount
						+ ", parameterPluralityResolvingRequired=" + parameterPluralityResolvingRequired
						+ ", chosenParameterPosition=" + chosenParameterPosition + ", otherParameterValueStorages="
						+ otherParameterValueStorages + "]";
			}

		}

		protected static class ConstructorBasedConverter implements Filter<Object> {

			protected Constructor<?> constrcutor;
			protected int parameterCount;
			protected boolean parameterPluralityResolvingRequired;
			protected int chosenParameterPosition;
			protected Map<Integer, TextualStorage> otherParameterValueStorages;

			public ConstructorBasedConverter(ConversionMethodFinder conversionMethodFinder, Class<?> theClass,
					Class<?>[] parameterTypes) {
				try {
					this.constrcutor = theClass.getDeclaredConstructor(parameterTypes);
				} catch (NoSuchMethodException e) {
					throw new ReflectionUIError(e);
				} catch (SecurityException e) {
					throw new ReflectionUIError(e);
				}
				this.parameterCount = conversionMethodFinder.getParameterCount(constrcutor);
				this.parameterPluralityResolvingRequired = parameterCount > 1;
				this.otherParameterValueStorages = conversionMethodFinder.getOtherParameterValueStorages();
				this.chosenParameterPosition = conversionMethodFinder.getChosenParameterPosition();
			}

			@Override
			public Object get(Object obj) {
				try {
					Constructor<?> constructor = getConstructor();
					if (parameterPluralityResolvingRequired) {
						Object[] paramValues = new Object[parameterCount];
						for (int paramPosition = 0; paramPosition < paramValues.length; paramPosition++) {
							if (paramPosition == chosenParameterPosition) {
								paramValues[paramPosition] = obj;
							} else {
								TextualStorage storage = otherParameterValueStorages.get(paramPosition);
								if (storage == null) {
									continue;
								}
								paramValues[paramPosition] = storage.load();
							}
						}
						return constructor.newInstance(paramValues);
					} else {
						return constructor.newInstance(obj);
					}
				} catch (InvocationTargetException e) {
					throw new ReflectionUIError(e.getTargetException());
				} catch (Exception e) {
					throw new ReflectionUIError("Failed to convert '" + obj + "' with " + this + ": " + e.toString(),
							e);
				}
			}

			public Constructor<?> getConstructor() {
				return constrcutor;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + chosenParameterPosition;
				result = prime * result + ((constrcutor == null) ? 0 : constrcutor.hashCode());
				result = prime * result
						+ ((otherParameterValueStorages == null) ? 0 : otherParameterValueStorages.hashCode());
				result = prime * result + parameterCount;
				result = prime * result + (parameterPluralityResolvingRequired ? 1231 : 1237);
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
				ConstructorBasedConverter other = (ConstructorBasedConverter) obj;
				if (chosenParameterPosition != other.chosenParameterPosition)
					return false;
				if (constrcutor == null) {
					if (other.constrcutor != null)
						return false;
				} else if (!constrcutor.equals(other.constrcutor))
					return false;
				if (otherParameterValueStorages == null) {
					if (other.otherParameterValueStorages != null)
						return false;
				} else if (!otherParameterValueStorages.equals(other.otherParameterValueStorages))
					return false;
				if (parameterCount != other.parameterCount)
					return false;
				if (parameterPluralityResolvingRequired != other.parameterPluralityResolvingRequired)
					return false;
				return true;
			}

			@Override
			public String toString() {
				return "ConstructorBasedConverter [constrcutor=" + constrcutor + ", parameterCount=" + parameterCount
						+ ", parameterPluralityResolvingRequired=" + parameterPluralityResolvingRequired
						+ ", chosenParameterPosition=" + chosenParameterPosition + ", otherParameterValueStorages="
						+ otherParameterValueStorages + "]";
			}

		};

	}

	public static class TypeConversion extends Mapping {
		private static final long serialVersionUID = 1L;

		protected ITypeInfoFinder newTypeFinder;
		protected boolean nullValueConverted = false;

		public boolean isNullValueConverted() {
			return nullValueConverted;
		}

		public void setNullValueConverted(boolean nullValueConverted) {
			this.nullValueConverted = nullValueConverted;
		}

		@XmlTransient
		public List<Mapping> getPreMappings() {
			List<Mapping> result = new ArrayList<InfoCustomizations.Mapping>();
			Mapping currentMapping = this;
			while (currentMapping.getPreMapping() != null) {
				result.add(currentMapping.getPreMapping());
				currentMapping = currentMapping.getPreMapping();
			}
			Collections.reverse(result);
			return result;
		}

		public void setPreMappings(List<Mapping> preMappings) {
			preMappings = new ArrayList<InfoCustomizations.Mapping>(preMappings);
			Collections.reverse(preMappings);
			Mapping currentMapping = this;
			for (Mapping preMapping : preMappings) {
				currentMapping.setPreMapping(preMapping);
				currentMapping = preMapping;
			}
			currentMapping.setPreMapping(null);
		}

		@XmlElements({ @XmlElement(name = "javaClassBasedTypeInfoFinder", type = JavaClassBasedTypeInfoFinder.class),
				@XmlElement(name = "customTypeInfoFinder", type = CustomTypeInfoFinder.class) })
		public ITypeInfoFinder getNewTypeFinder() {
			return newTypeFinder;
		}

		public void setNewTypeFinder(ITypeInfoFinder newTypeFinder) {
			this.newTypeFinder = newTypeFinder;
		}

		public ITypeInfo findNewType(ReflectionUI reflectionUI, SpecificitiesIdentifier specificitiesIdentifier) {
			if (newTypeFinder != null) {
				return newTypeFinder.find(reflectionUI, specificitiesIdentifier);
			} else {
				return reflectionUI
						.buildTypeInfo(new JavaTypeInfoSource(reflectionUI, Object.class, specificitiesIdentifier));
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((newTypeFinder == null) ? 0 : newTypeFinder.hashCode());
			result = prime * result + (nullValueConverted ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			TypeConversion other = (TypeConversion) obj;
			if (newTypeFinder == null) {
				if (other.newTypeFinder != null)
					return false;
			} else if (!newTypeFinder.equals(other.newTypeFinder))
				return false;
			if (nullValueConverted != other.nullValueConverted)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TypeConversion [newTypeFinder=" + newTypeFinder + ", nullValueConverted=" + nullValueConverted
					+ "]";
		}

	}

	public static class Mapping extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected Mapping preMapping;
		protected ConversionMethodFinder conversionMethodFinder;
		protected ConversionMethodFinder reverseConversionMethodFinder;

		public Filter<Object> buildOverallConversionMethod() {
			Filter<Object> result = null;
			if (conversionMethodFinder != null) {
				result = conversionMethodFinder.find();
			}
			if (preMapping != null) {
				Filter<Object> preConversionMethod = preMapping.buildOverallConversionMethod();
				if (preConversionMethod != null) {
					if (result == null) {
						result = preConversionMethod;
					} else {
						result = new Filter.Chain<Object>(preConversionMethod, result);
					}
				}
			}
			return result;
		}

		public Filter<Object> buildOverallReverseConversionMethod() {
			Filter<Object> result = null;
			if (reverseConversionMethodFinder != null) {
				result = reverseConversionMethodFinder.find();
			}
			if (preMapping != null) {
				Filter<Object> postReverseConversionMethod = preMapping.buildOverallReverseConversionMethod();
				if (postReverseConversionMethod != null) {
					if (result == null) {
						result = postReverseConversionMethod;
					} else {
						result = new Filter.Chain<Object>(result, postReverseConversionMethod);
					}
				}
			}
			return result;
		}

		public ConversionMethodFinder getConversionMethodFinder() {
			return conversionMethodFinder;
		}

		public void setConversionMethodFinder(ConversionMethodFinder conversionMethodFinder) {
			this.conversionMethodFinder = conversionMethodFinder;
		}

		public ConversionMethodFinder getReverseConversionMethodFinder() {
			return reverseConversionMethodFinder;
		}

		public void setReverseConversionMethodFinder(ConversionMethodFinder reverseConversionMethodFinder) {
			this.reverseConversionMethodFinder = reverseConversionMethodFinder;
		}

		public Mapping getPreMapping() {
			return preMapping;
		}

		public void setPreMapping(Mapping preMapping) {
			this.preMapping = preMapping;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((conversionMethodFinder == null) ? 0 : conversionMethodFinder.hashCode());
			result = prime * result + ((preMapping == null) ? 0 : preMapping.hashCode());
			result = prime * result
					+ ((reverseConversionMethodFinder == null) ? 0 : reverseConversionMethodFinder.hashCode());
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
			Mapping other = (Mapping) obj;
			if (conversionMethodFinder == null) {
				if (other.conversionMethodFinder != null)
					return false;
			} else if (!conversionMethodFinder.equals(other.conversionMethodFinder))
				return false;
			if (preMapping == null) {
				if (other.preMapping != null)
					return false;
			} else if (!preMapping.equals(other.preMapping))
				return false;
			if (reverseConversionMethodFinder == null) {
				if (other.reverseConversionMethodFinder != null)
					return false;
			} else if (!reverseConversionMethodFinder.equals(other.reverseConversionMethodFinder))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Mapping [preMapping=" + preMapping + ", conversionMethodFinder=" + conversionMethodFinder
					+ ", reverseConversionMethodFinder=" + reverseConversionMethodFinder + "]";
		}

	}

	public static class FieldCustomization extends AbstractMemberCustomization
			implements Comparable<FieldCustomization> {
		private static final long serialVersionUID = 1L;

		protected String fieldName;
		protected String customFieldCaption;
		protected boolean nullValueDistinctForced = false;
		protected boolean getOnlyForced = false;
		protected boolean transientForced = false;
		protected String customSetterSignature;
		protected String valueOptionsFieldName;
		protected ValueReturnMode customValueReturnMode;
		protected String nullValueLabel;
		protected String encapsulationFieldName;
		protected boolean getterGenerated;
		protected boolean setterGenerated;
		protected boolean displayedAsSingletonList = false;
		protected boolean nullStatusFieldExported = false;
		protected String importedNullStatusFieldName;
		protected FieldTypeSpecificities specificTypeCustomizations = new FieldTypeSpecificities();
		protected boolean formControlEmbeddingForced = false;
		protected boolean formControlCreationForced = false;
		protected TypeConversion typeConversion;
		protected TextualStorage nullReplacement = new TextualStorage();
		protected boolean duplicateGenerated = false;
		protected Long autoUpdatePeriodMilliseconds;
		protected Double displayAreaHorizontalWeight;
		protected Double displayAreaVerticalWeight;

		@Override
		public boolean isInitial() {
			FieldCustomization defaultFieldCustomization = new FieldCustomization();
			defaultFieldCustomization.fieldName = fieldName;
			return InfoCustomizations.isSimilar(this, defaultFieldCustomization);
		}

		public Double getDisplayAreaHorizontalWeight() {
			return displayAreaHorizontalWeight;
		}

		public void setDisplayAreaHorizontalWeight(Double displayAreaHorizontalWeight) {
			this.displayAreaHorizontalWeight = displayAreaHorizontalWeight;
		}

		public Double getDisplayAreaVerticalWeight() {
			return displayAreaVerticalWeight;
		}

		public void setDisplayAreaVerticalWeight(Double displayAreaVerticalWeight) {
			this.displayAreaVerticalWeight = displayAreaVerticalWeight;
		}

		public Long getAutoUpdatePeriodMilliseconds() {
			return autoUpdatePeriodMilliseconds;
		}

		public void setAutoUpdatePeriodMilliseconds(Long autoUpdatePeriodMilliseconds) {
			this.autoUpdatePeriodMilliseconds = autoUpdatePeriodMilliseconds;
		}

		public boolean isDuplicateGenerated() {
			return duplicateGenerated;
		}

		public void setDuplicateGenerated(boolean duplicateGenerated) {
			this.duplicateGenerated = duplicateGenerated;
		}

		public TextualStorage getNullReplacement() {
			return nullReplacement;
		}

		public void setNullReplacement(TextualStorage nullReplacement) {
			this.nullReplacement = nullReplacement;
		}

		public TypeConversion getTypeConversion() {
			return typeConversion;
		}

		public void setTypeConversion(TypeConversion typeConversion) {
			this.typeConversion = typeConversion;
		}

		public String getEncapsulationFieldName() {
			return encapsulationFieldName;
		}

		public void setEncapsulationFieldName(String encapsulationFieldName) {
			this.encapsulationFieldName = encapsulationFieldName;
		}

		public boolean isFormControlCreationForced() {
			return formControlCreationForced;
		}

		public void setFormControlCreationForced(boolean formControlCreationForced) {
			this.formControlCreationForced = formControlCreationForced;
		}

		public boolean isFormControlEmbeddingForced() {
			return formControlEmbeddingForced;
		}

		public void setFormControlEmbeddingForced(boolean formControlEmbeddingForced) {
			this.formControlEmbeddingForced = formControlEmbeddingForced;
		}

		public String getCustomSetterSignature() {
			return customSetterSignature;
		}

		public void setCustomSetterSignature(String customSetterSignature) {
			this.customSetterSignature = customSetterSignature;
		}

		public boolean isGetterGenerated() {
			return getterGenerated;
		}

		public void setGetterGenerated(boolean getterGenerated) {
			this.getterGenerated = getterGenerated;
		}

		public boolean isSetterGenerated() {
			return setterGenerated;
		}

		public void setSetterGenerated(boolean setterGenerated) {
			this.setterGenerated = setterGenerated;
		}

		public FieldTypeSpecificities getSpecificTypeCustomizations() {
			return specificTypeCustomizations;
		}

		public void setSpecificTypeCustomizations(FieldTypeSpecificities specificTypeCustomizations) {
			this.specificTypeCustomizations = specificTypeCustomizations;
		}

		public boolean isNullStatusFieldExported() {
			return nullStatusFieldExported;
		}

		public void setNullStatusFieldExported(boolean nullStatusFieldExported) {
			this.nullStatusFieldExported = nullStatusFieldExported;
		}

		public String getImportedNullStatusFieldName() {
			return importedNullStatusFieldName;
		}

		public void setImportedNullStatusFieldName(String importedNullStatusFieldName) {
			this.importedNullStatusFieldName = importedNullStatusFieldName;
		}

		public boolean isDisplayedAsSingletonList() {
			return displayedAsSingletonList;
		}

		public void setDisplayedAsSingletonList(boolean displayedAsSingletonList) {
			this.displayedAsSingletonList = displayedAsSingletonList;
		}

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public boolean isNullValueDistinctForced() {
			return nullValueDistinctForced;
		}

		public void setNullValueDistinctForced(boolean nullValueDistinctForced) {
			this.nullValueDistinctForced = nullValueDistinctForced;
		}

		public ValueReturnMode getCustomValueReturnMode() {
			return customValueReturnMode;
		}

		public void setCustomValueReturnMode(ValueReturnMode customValueReturnMode) {
			this.customValueReturnMode = customValueReturnMode;
		}

		public String getNullValueLabel() {
			return nullValueLabel;
		}

		public void setNullValueLabel(String nullValueLabel) {
			this.nullValueLabel = nullValueLabel;
		}

		public boolean isGetOnlyForced() {
			return getOnlyForced;
		}

		public void setGetOnlyForced(boolean getOnlyForced) {
			this.getOnlyForced = getOnlyForced;
		}

		public boolean isTransientForced() {
			return transientForced;
		}

		public void setTransientForced(boolean transientForced) {
			this.transientForced = transientForced;
		}

		public String getCustomFieldCaption() {
			return customFieldCaption;
		}

		public void setCustomFieldCaption(String customFieldCaption) {
			this.customFieldCaption = customFieldCaption;
		}

		public String getValueOptionsFieldName() {
			return valueOptionsFieldName;
		}

		public void setValueOptionsFieldName(String valueOptionsFieldName) {
			this.valueOptionsFieldName = valueOptionsFieldName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
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
			FieldCustomization other = (FieldCustomization) obj;
			if (fieldName == null) {
				if (other.fieldName != null)
					return false;
			} else if (!fieldName.equals(other.fieldName))
				return false;
			return true;
		}

		@Override
		public int compareTo(FieldCustomization o) {
			return MiscUtils.compareNullables(fieldName, o.fieldName);
		}

		@Override
		public String toString() {
			return "FieldCustomization [fieldName=" + fieldName + "]";
		}

	}

	public static class TextualStorage extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected String data;
		protected Mapping preConversion;

		public TextualStorage() {
		}

		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}

		public Mapping getPreConversion() {
			return preConversion;
		}

		public void setPreConversion(Mapping preConversion) {
			this.preConversion = preConversion;
		}

		public void save(Object object) {
			if (object == null) {
				this.data = null;
			} else {
				if (preConversion != null) {
					Filter<Object> conversionMethod = preConversion.buildOverallConversionMethod();
					object = conversionMethod.get(object);
				}
				this.data = IOUtils.serializeToHexaText(object);
			}
		}

		public Object load() {
			if (data == null) {
				return null;
			} else {
				Object result = IOUtils.deserializeFromHexaText(data);
				if (preConversion != null) {
					Filter<Object> reverseConversionMethod = preConversion.buildOverallReverseConversionMethod();
					result = reverseConversionMethod.get(result);
				}
				return result;
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((data == null) ? 0 : data.hashCode());
			result = prime * result + ((preConversion == null) ? 0 : preConversion.hashCode());
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
			TextualStorage other = (TextualStorage) obj;
			if (data == null) {
				if (other.data != null)
					return false;
			} else if (!data.equals(other.data))
				return false;
			if (preConversion == null) {
				if (other.preConversion != null)
					return false;
			} else if (!preConversion.equals(other.preConversion))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TextualStorage [data=" + ((data == null) ? null : MiscUtils.truncateNicely(data, 30))
					+ ", preConversion=" + preConversion + "]";
		}

	}

	public enum TransactionalRole {
		BEGIN, COMMIT, ROLLBACK
	}

	public static class MethodCustomization extends AbstractMemberCustomization
			implements Comparable<MethodCustomization> {
		private static final long serialVersionUID = 1L;

		protected String methodSignature;
		protected String customMethodCaption;
		protected boolean readOnlyForced = false;
		protected boolean validating = false;
		protected boolean runWhenObjectShown = false;
		protected boolean runWhenObjectHidden = false;
		protected List<ParameterCustomization> parametersCustomizations = new ArrayList<InfoCustomizations.ParameterCustomization>();
		protected ValueReturnMode customValueReturnMode;
		protected String nullReturnValueLabel;
		protected boolean returnValueFieldGenerated = false;
		protected boolean detachedReturnValueForced = false;
		protected String encapsulationFieldName;
		protected List<String> parameterizedFieldNames = new ArrayList<String>();
		protected ResourcePath iconImagePath;
		protected IMenuItemContainerCustomization menuLocation;
		protected boolean ignoredReturnValueForced = false;
		protected List<TextualStorage> serializedInvocationDatas = new ArrayList<TextualStorage>();
		protected boolean duplicateGenerated = false;
		protected String confirmationMessage;
		protected String parametersValidationCustomCaption;
		protected TransactionalRole transactionalRole;
		protected String enablementStatusFieldName;

		@Override
		public boolean isInitial() {
			MethodCustomization defaultMethodCustomization = new MethodCustomization();
			defaultMethodCustomization.methodSignature = methodSignature;
			return InfoCustomizations.isSimilar(this, defaultMethodCustomization);
		}

		public String getEnablementStatusFieldName() {
			return enablementStatusFieldName;
		}

		public void setEnablementStatusFieldName(String enablementStatusFieldName) {
			this.enablementStatusFieldName = enablementStatusFieldName;
		}

		public TransactionalRole getTransactionalRole() {
			return transactionalRole;
		}

		public void setTransactionalRole(TransactionalRole transactionalRole) {
			this.transactionalRole = transactionalRole;
		}

		public String getParametersValidationCustomCaption() {
			return parametersValidationCustomCaption;
		}

		public void setParametersValidationCustomCaption(String parametersValidationCustomCaption) {
			this.parametersValidationCustomCaption = parametersValidationCustomCaption;
		}

		public boolean isRunWhenObjectShown() {
			return runWhenObjectShown;
		}

		public void setRunWhenObjectShown(boolean runWhenObjectShown) {
			this.runWhenObjectShown = runWhenObjectShown;
		}

		public boolean isRunWhenObjectHidden() {
			return runWhenObjectHidden;
		}

		public void setRunWhenObjectHidden(boolean runWhenObjectHidden) {
			this.runWhenObjectHidden = runWhenObjectHidden;
		}

		public String getConfirmationMessage() {
			return confirmationMessage;
		}

		public void setConfirmationMessage(String confirmationMessage) {
			this.confirmationMessage = confirmationMessage;
		}

		public boolean isDuplicateGenerated() {
			return duplicateGenerated;
		}

		public void setDuplicateGenerated(boolean duplicateGenerated) {
			this.duplicateGenerated = duplicateGenerated;
		}

		public List<TextualStorage> getSerializedInvocationDatas() {
			return serializedInvocationDatas;
		}

		public void setSerializedInvocationDatas(List<TextualStorage> serializedInvocationDatas) {
			this.serializedInvocationDatas = serializedInvocationDatas;
		}

		public boolean isIgnoredReturnValueForced() {
			return ignoredReturnValueForced;
		}

		public void setIgnoredReturnValueForced(boolean ignoredReturnValueForced) {
			this.ignoredReturnValueForced = ignoredReturnValueForced;
		}

		@XmlElements({ @XmlElement(name = "menu", type = MenuCustomization.class),
				@XmlElement(name = "menuItemCategory", type = MenuItemCategoryCustomization.class) })
		public IMenuItemContainerCustomization getMenuLocation() {
			return menuLocation;
		}

		public void setMenuLocation(IMenuItemContainerCustomization menuLocation) {
			this.menuLocation = menuLocation;
		}

		public String getMethodName() {
			if (methodSignature == null) {
				return null;
			}
			return ReflectionUIUtils.extractMethodNameFromSignature(methodSignature);
		}

		public void setMethodName(String methodName) {
			if (methodSignature == null) {
				return;
			}
			String returnTypeName = ReflectionUIUtils.extractMethodReturnTypeNameFromSignature(methodSignature);
			String[] parameterTypeNames = ReflectionUIUtils
					.extractMethodParameterTypeNamesFromSignature(methodSignature);
			this.methodSignature = ReflectionUIUtils.buildMethodSignature(returnTypeName, methodName,
					Arrays.asList(parameterTypeNames));
		}

		public ResourcePath getIconImagePath() {
			return iconImagePath;
		}

		public void setIconImagePath(ResourcePath iconImagePath) {
			this.iconImagePath = iconImagePath;
		}

		public String getEncapsulationFieldName() {
			return encapsulationFieldName;
		}

		public void setEncapsulationFieldName(String encapsulationFieldName) {
			this.encapsulationFieldName = encapsulationFieldName;
		}

		public boolean isReturnValueFieldGenerated() {
			return returnValueFieldGenerated;
		}

		public void setReturnValueFieldGenerated(boolean returnValueFieldGenerated) {
			this.returnValueFieldGenerated = returnValueFieldGenerated;
		}

		public List<String> getParameterizedFieldNames() {
			return parameterizedFieldNames;
		}

		public void setParameterizedFieldNames(List<String> parameterizedFieldNames) {
			this.parameterizedFieldNames = parameterizedFieldNames;
		}

		public boolean isDetachedReturnValueForced() {
			return detachedReturnValueForced;
		}

		public void setDetachedReturnValueForced(boolean detachedReturnValueForced) {
			this.detachedReturnValueForced = detachedReturnValueForced;
		}

		public boolean isValidating() {
			return validating;
		}

		public void setValidating(boolean validating) {
			this.validating = validating;
		}

		public boolean isReadOnlyForced() {
			return readOnlyForced;
		}

		public void setReadOnlyForced(boolean readOnlyForced) {
			this.readOnlyForced = readOnlyForced;
		}

		public ValueReturnMode getCustomValueReturnMode() {
			return customValueReturnMode;
		}

		public void setCustomValueReturnMode(ValueReturnMode customReturnValueReturnMode) {
			this.customValueReturnMode = customReturnValueReturnMode;
		}

		public String getNullReturnValueLabel() {
			return nullReturnValueLabel;
		}

		public void setNullReturnValueLabel(String nullReturnValueLabel) {
			this.nullReturnValueLabel = nullReturnValueLabel;
		}

		public String getMethodSignature() {
			return methodSignature;
		}

		public void setMethodSignature(String methodSignature) {
			this.methodSignature = methodSignature;
		}

		public String getCustomMethodCaption() {
			return customMethodCaption;
		}

		public void setCustomMethodCaption(String customMethodCaption) {
			this.customMethodCaption = customMethodCaption;
		}

		public List<ParameterCustomization> getParametersCustomizations() {
			return parametersCustomizations;
		}

		public void setParametersCustomizations(List<ParameterCustomization> parametersCustomizations) {
			this.parametersCustomizations = parametersCustomizations;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((methodSignature == null) ? 0 : methodSignature.hashCode());
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
			MethodCustomization other = (MethodCustomization) obj;
			if (methodSignature == null) {
				if (other.methodSignature != null)
					return false;
			} else if (!methodSignature.equals(other.methodSignature))
				return false;
			return true;
		}

		@Override
		public int compareTo(MethodCustomization o) {
			return MiscUtils.compareNullables(getMethodName(), o.getMethodName());
		}

		@Override
		public String toString() {
			return "MethodCustomization [methodSignature=" + methodSignature + "]";
		}

	}

	public static class ParameterCustomization extends AbstractInfoCustomization
			implements Comparable<ParameterCustomization> {
		private static final long serialVersionUID = 1L;

		protected String parameterName;
		protected String customParameterCaption;
		protected boolean hidden = false;
		protected boolean nullValueDistinctForced = false;
		protected String onlineHelp;
		protected boolean displayedAsField;
		protected TextualStorage defaultValue = new TextualStorage();

		public TextualStorage getDefaultValue() {
			return defaultValue;
		}

		public void setDefaultValue(TextualStorage defaultValue) {
			this.defaultValue = defaultValue;
		}

		public boolean isDisplayedAsField() {
			return displayedAsField;
		}

		public void setDisplayedAsField(boolean displayedAsField) {
			this.displayedAsField = displayedAsField;
		}

		public String getParameterName() {
			return parameterName;
		}

		public void setParameterName(String parameterName) {
			this.parameterName = parameterName;
		}

		public boolean isHidden() {
			return hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}

		public boolean isNullValueDistinctForced() {
			return nullValueDistinctForced;
		}

		public void setNullValueDistinctForced(boolean nullValueDistinctForced) {
			this.nullValueDistinctForced = nullValueDistinctForced;
		}

		public String getCustomParameterCaption() {
			return customParameterCaption;
		}

		public void setCustomParameterCaption(String customParameterCaption) {
			this.customParameterCaption = customParameterCaption;
		}

		public String getOnlineHelp() {
			return onlineHelp;
		}

		public void setOnlineHelp(String onlineHelp) {
			this.onlineHelp = onlineHelp;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((parameterName == null) ? 0 : parameterName.hashCode());
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
			ParameterCustomization other = (ParameterCustomization) obj;
			if (parameterName == null) {
				if (other.parameterName != null)
					return false;
			} else if (!parameterName.equals(other.parameterName))
				return false;
			return true;
		}

		@Override
		public int compareTo(ParameterCustomization o) {
			return MiscUtils.compareNullables(parameterName, o.parameterName);
		}

		@Override
		public String toString() {
			return "ParameterCustomization [parameterName=" + parameterName + "]";
		}

	}

	public static class ListItemFieldShortcut extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected String fieldName;
		protected boolean alwaysShown = true;
		protected String customFieldCaption;

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public boolean isAlwaysShown() {
			return alwaysShown;
		}

		public void setAlwaysShown(boolean alwaysShown) {
			this.alwaysShown = alwaysShown;
		}

		public String getCustomFieldCaption() {
			return customFieldCaption;
		}

		public void setCustomFieldCaption(String customFieldCaption) {
			this.customFieldCaption = customFieldCaption;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
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
			ListItemFieldShortcut other = (ListItemFieldShortcut) obj;
			if (fieldName == null) {
				if (other.fieldName != null)
					return false;
			} else if (!fieldName.equals(other.fieldName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ListItemFieldShortcut [fieldName=" + fieldName + "]";
		}

	}

	public static class ListItemMethodShortcut extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected String methodSignature;
		protected boolean alwaysShown = true;
		protected String customMethodCaption;
		protected ResourcePath customIconImagePath;

		public String getMethodSignature() {
			return methodSignature;
		}

		public void setMethodSignature(String methodSignature) {
			this.methodSignature = methodSignature;
		}

		public boolean isAlwaysShown() {
			return alwaysShown;
		}

		public void setAlwaysShown(boolean alwaysShown) {
			this.alwaysShown = alwaysShown;
		}

		public String getCustomMethodCaption() {
			return customMethodCaption;
		}

		public void setCustomMethodCaption(String customMethodCaption) {
			this.customMethodCaption = customMethodCaption;
		}

		public ResourcePath getCustomIconImagePath() {
			return customIconImagePath;
		}

		public void setCustomIconImagePath(ResourcePath customIconImagePath) {
			this.customIconImagePath = customIconImagePath;
		}

		public void validate() {
			if ((methodSignature != null) && (methodSignature.length() > 0)) {
				if (ReflectionUIUtils.extractMethodNameFromSignature(methodSignature) == null) {
					throw new ReflectionUIError("Malformed method signature: '" + methodSignature + "'");
				}
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((methodSignature == null) ? 0 : methodSignature.hashCode());
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
			ListItemMethodShortcut other = (ListItemMethodShortcut) obj;
			if (methodSignature == null) {
				if (other.methodSignature != null)
					return false;
			} else if (!methodSignature.equals(other.methodSignature))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ListItemMethodShortcut [methodSignature=" + methodSignature + "]";
		}

	}

	public static class InfoFilter extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected String value = "";
		protected boolean regularExpression = false;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public boolean isRegularExpression() {
			return regularExpression;
		}

		public void setRegularExpression(boolean regularExpression) {
			this.regularExpression = regularExpression;
		}

		public boolean matches(String s) {
			if (regularExpression) {
				Pattern pattern = Pattern.compile(value);
				Matcher matcher = pattern.matcher(s);
				return matcher.matches();
			} else {
				return s.equals(value);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (regularExpression ? 1231 : 1237);
			result = prime * result + ((value == null) ? 0 : value.hashCode());
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
			InfoFilter other = (InfoFilter) obj;
			if (regularExpression != other.regularExpression)
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "InfoFilter [value=" + value + ", regularExpression=" + regularExpression + "]";
		}

	}

	public static class ListInstanciationOption extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected ITypeInfoFinder customInstanceTypeFinder;

		@XmlElements({ @XmlElement(name = "javaClassBasedTypeInfoFinder", type = JavaClassBasedTypeInfoFinder.class),
				@XmlElement(name = "customTypeInfoFinder", type = CustomTypeInfoFinder.class) })
		public ITypeInfoFinder getCustomInstanceTypeFinder() {
			return customInstanceTypeFinder;
		}

		public void setCustomInstanceTypeFinder(ITypeInfoFinder customInstanceTypeFinder) {
			this.customInstanceTypeFinder = customInstanceTypeFinder;
		}

	}

	public static class ListEditOptions extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected boolean itemCreationEnabled = true;
		protected boolean itemDeletionEnabled = true;
		protected boolean itemMoveEnabled = true;
		protected ListInstanciationOption listInstanciationOption;

		public ListInstanciationOption getListInstanciationOption() {
			return listInstanciationOption;
		}

		public void setListInstanciationOption(ListInstanciationOption listInstanciationOption) {
			this.listInstanciationOption = listInstanciationOption;
		}

		public boolean isItemCreationEnabled() {
			return itemCreationEnabled;
		}

		public void setItemCreationEnabled(boolean itemCreationEnabled) {
			this.itemCreationEnabled = itemCreationEnabled;
		}

		public boolean isItemDeletionEnabled() {
			return itemDeletionEnabled;
		}

		public void setItemDeletionEnabled(boolean itemDeletionEnabled) {
			this.itemDeletionEnabled = itemDeletionEnabled;
		}

		public boolean isItemMoveEnabled() {
			return itemMoveEnabled;
		}

		public void setItemMoveEnabled(boolean itemMoveEnabled) {
			this.itemMoveEnabled = itemMoveEnabled;
		}

	}

	public static class EnumerationItemCustomization extends AbstractInfoCustomization
			implements Comparable<EnumerationItemCustomization> {

		private static final long serialVersionUID = 1L;

		protected String itemName;
		protected String customCaption;
		protected boolean hidden;
		protected ResourcePath iconImagePath;

		@Override
		public boolean isInitial() {
			EnumerationItemCustomization defaultEnumerationItemCustomization = new EnumerationItemCustomization();
			defaultEnumerationItemCustomization.itemName = itemName;
			return InfoCustomizations.isSimilar(this, defaultEnumerationItemCustomization);

		}

		public String getItemName() {
			return itemName;
		}

		public void setItemName(String itemName) {
			this.itemName = itemName;
		}

		public String getCustomCaption() {
			return customCaption;
		}

		public void setCustomCaption(String customCaption) {
			this.customCaption = customCaption;
		}

		public ResourcePath getIconImagePath() {
			return iconImagePath;
		}

		public void setIconImagePath(ResourcePath iconImagePath) {
			this.iconImagePath = iconImagePath;
		}

		public boolean isHidden() {
			return hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}

		@Override
		public int compareTo(EnumerationItemCustomization o) {
			int result = MiscUtils.compareNullables(itemName, o.itemName);
			return result;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((itemName == null) ? 0 : itemName.hashCode());
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
			EnumerationItemCustomization other = (EnumerationItemCustomization) obj;
			if (itemName == null) {
				if (other.itemName != null)
					return false;
			} else if (!itemName.equals(other.itemName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "EnumerationItemCustomization [itemName=" + itemName + "]";
		}

	}

	public static class EnumerationCustomization extends AbstractCustomization
			implements Comparable<EnumerationCustomization> {

		private static final long serialVersionUID = 1L;
		protected String enumerationTypeName;
		protected List<EnumerationItemCustomization> itemCustomizations = new ArrayList<EnumerationItemCustomization>();
		protected boolean dynamicEnumerationForced = false;
		protected List<String> itemsCustomOrder;

		@Override
		public boolean isInitial() {
			EnumerationCustomization defaultEnumerationCustomization = new EnumerationCustomization();
			defaultEnumerationCustomization.enumerationTypeName = enumerationTypeName;
			return InfoCustomizations.isSimilar(this, defaultEnumerationCustomization);
		}

		public String getEnumerationTypeName() {
			return enumerationTypeName;
		}

		public void setEnumerationTypeName(String enumerationTypeName) {
			this.enumerationTypeName = enumerationTypeName;
		}

		public List<EnumerationItemCustomization> getItemCustomizations() {
			return itemCustomizations;
		}

		public void setItemCustomizations(List<EnumerationItemCustomization> itemCustomizations) {
			this.itemCustomizations = itemCustomizations;
		}

		public List<String> getItemsCustomOrder() {
			return itemsCustomOrder;
		}

		public void setItemsCustomOrder(List<String> itemsCustomOrder) {
			this.itemsCustomOrder = itemsCustomOrder;
		}

		public boolean isDynamicEnumerationForced() {
			return dynamicEnumerationForced;
		}

		public void setDynamicEnumerationForced(boolean dynamicEnumerationForced) {
			this.dynamicEnumerationForced = dynamicEnumerationForced;
		}

		@Override
		public int compareTo(EnumerationCustomization o) {
			int result = MiscUtils.compareNullables(enumerationTypeName, o.enumerationTypeName);
			return result;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((enumerationTypeName == null) ? 0 : enumerationTypeName.hashCode());
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
			EnumerationCustomization other = (EnumerationCustomization) obj;
			if (enumerationTypeName == null) {
				if (other.enumerationTypeName != null)
					return false;
			} else if (!enumerationTypeName.equals(other.enumerationTypeName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "EnumerationCustomization [enumerationTypeName=" + enumerationTypeName + "]";
		}

	}

	public enum ListLengthUnit {
		PIXELS, SCREEN_PERCENT
	}

	/**
	 * Spell error to be fixed: should be "Length" not "Lenght". The customizations
	 * files should be migrated automatically.
	 */
	public static class ListLenghtCustomization extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected ListLengthUnit unit = ListLengthUnit.SCREEN_PERCENT;
		protected int value = 40;

		public ListLengthUnit getUnit() {
			return unit;
		}

		public void setUnit(ListLengthUnit unit) {
			this.unit = unit;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((unit == null) ? 0 : unit.hashCode());
			result = prime * result + value;
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
			ListLenghtCustomization other = (ListLenghtCustomization) obj;
			if (unit != other.unit)
				return false;
			if (value != other.value)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ListLenghtCustomization [unit=" + unit + ", value=" + value + "]";
		}

	}

	public static class ListCustomization extends AbstractCustomization implements Comparable<ListCustomization> {
		private static final long serialVersionUID = 1L;

		protected String listTypeName;
		protected String itemTypeName;
		protected boolean itemTypeColumnAdded = false;
		protected boolean positionColumnAdded = false;
		protected boolean fieldColumnsAdded = false;
		protected boolean stringValueColumnAdded = false;
		protected List<ColumnCustomization> columnCustomizations = new ArrayList<ColumnCustomization>();
		protected List<String> columnsCustomOrder;
		protected TreeStructureDiscoverySettings treeStructureDiscoverySettings;
		protected List<ListItemFieldShortcut> allowedItemFieldShortcuts = new ArrayList<ListItemFieldShortcut>();
		protected List<ListItemMethodShortcut> allowedItemMethodShortcuts = new ArrayList<ListItemMethodShortcut>();
		protected List<InfoFilter> methodsExcludedFromItemDetails = new ArrayList<InfoFilter>();
		protected List<InfoFilter> fieldsExcludedFromItemDetails = new ArrayList<InfoFilter>();
		protected boolean itemDetailsViewDisabled = false;
		protected ListEditOptions editOptions = new ListEditOptions();
		protected boolean listSorted = false;
		protected IListItemDetailsAccessMode customDetailsAccessMode;
		protected ListLenghtCustomization length = null;
		protected boolean itemNullValueAllowed = false;
		protected InitialItemValueCreationOption initialItemValueCreationOption;
		protected ITypeInfoFinder customItemTypeFinder;

		@Override
		public boolean isInitial() {
			ListCustomization defaultListCustomization = new ListCustomization();
			defaultListCustomization.listTypeName = listTypeName;
			defaultListCustomization.itemTypeName = itemTypeName;
			return InfoCustomizations.isSimilar(this, defaultListCustomization);
		}

		@XmlElements({ @XmlElement(name = "javaClassBasedTypeInfoFinder", type = JavaClassBasedTypeInfoFinder.class),
				@XmlElement(name = "customTypeInfoFinder", type = CustomTypeInfoFinder.class) })
		public ITypeInfoFinder getCustomItemTypeFinder() {
			return customItemTypeFinder;
		}

		public void setCustomItemTypeFinder(ITypeInfoFinder customItemTypeFinder) {
			this.customItemTypeFinder = customItemTypeFinder;
		}

		public InitialItemValueCreationOption getInitialItemValueCreationOption() {
			return initialItemValueCreationOption;
		}

		public void setInitialItemValueCreationOption(InitialItemValueCreationOption initialItemValueCreationOption) {
			this.initialItemValueCreationOption = initialItemValueCreationOption;
		}

		public boolean isItemNullValueAllowed() {
			return itemNullValueAllowed;
		}

		public void setItemNullValueAllowed(boolean itemNullValueAllowed) {
			this.itemNullValueAllowed = itemNullValueAllowed;
		}

		public boolean isItemContructorSelectableforced() {
			return initialItemValueCreationOption == InitialItemValueCreationOption.CREATE_INITIAL_VALUE_ACCORDING_USER_PREFERENCES;
		}

		public void setItemContructorSelectableforced(boolean itemContructorSelectableforced) {
			if (itemContructorSelectableforced) {
				initialItemValueCreationOption = InitialItemValueCreationOption.CREATE_INITIAL_VALUE_ACCORDING_USER_PREFERENCES;
			} else {
				if (initialItemValueCreationOption == InitialItemValueCreationOption.CREATE_INITIAL_VALUE_ACCORDING_USER_PREFERENCES) {
					initialItemValueCreationOption = InitialItemValueCreationOption.CREATE_INITIAL_VALUE_AUTOMATICALLY;
				}
			}
		}

		public ListLenghtCustomization getLength() {
			return length;
		}

		public void setLength(ListLenghtCustomization length) {
			this.length = length;
		}

		@XmlElements({ @XmlElement(name = "detachedDetailsAccessMode", type = DetachedItemDetailsAccessMode.class),
				@XmlElement(name = "embeddedDetailsAccessMode", type = EmbeddedItemDetailsAccessMode.class) })
		public IListItemDetailsAccessMode getCustomDetailsAccessMode() {
			return customDetailsAccessMode;
		}

		public void setCustomDetailsAccessMode(IListItemDetailsAccessMode customDetailsAccessMode) {
			this.customDetailsAccessMode = customDetailsAccessMode;
		}

		@XmlElement(nillable = true)
		public ListEditOptions getEditOptions() {
			return editOptions;
		}

		public void setEditOptions(ListEditOptions editOptions) {
			this.editOptions = editOptions;
		}

		public boolean isListSorted() {
			return listSorted;
		}

		public void setListSorted(boolean listSorted) {
			this.listSorted = listSorted;
		}

		public List<InfoFilter> getFieldsExcludedFromItemDetails() {
			return fieldsExcludedFromItemDetails;
		}

		public void setFieldsExcludedFromItemDetails(List<InfoFilter> fieldsExcludedFromItemDetails) {
			this.fieldsExcludedFromItemDetails = fieldsExcludedFromItemDetails;
		}

		public List<InfoFilter> getMethodsExcludedFromItemDetails() {
			return methodsExcludedFromItemDetails;
		}

		public void setMethodsExcludedFromItemDetails(List<InfoFilter> methods) {
			this.methodsExcludedFromItemDetails = methods;
		}

		public List<ListItemFieldShortcut> getAllowedItemFieldShortcuts() {
			return allowedItemFieldShortcuts;
		}

		public void setAllowedItemFieldShortcuts(List<ListItemFieldShortcut> allowedItemFieldShortcuts) {
			this.allowedItemFieldShortcuts = allowedItemFieldShortcuts;
		}

		public List<ListItemMethodShortcut> getAllowedItemMethodShortcuts() {
			return allowedItemMethodShortcuts;
		}

		public void setAllowedItemMethodShortcuts(List<ListItemMethodShortcut> allowedItemMethodShortcuts) {
			this.allowedItemMethodShortcuts = allowedItemMethodShortcuts;
		}

		public List<String> getColumnsCustomOrder() {
			return columnsCustomOrder;
		}

		public void setColumnsCustomOrder(List<String> columnsCustomOrder) {
			this.columnsCustomOrder = columnsCustomOrder;
		}

		public TreeStructureDiscoverySettings getTreeStructureDiscoverySettings() {
			return treeStructureDiscoverySettings;
		}

		public void setTreeStructureDiscoverySettings(TreeStructureDiscoverySettings treeStructureDiscoverySettings) {
			this.treeStructureDiscoverySettings = treeStructureDiscoverySettings;
		}

		public String getListTypeName() {
			return listTypeName;
		}

		public void setListTypeName(String listTypeName) {
			this.listTypeName = listTypeName;
		}

		public String getItemTypeName() {
			return itemTypeName;
		}

		public void setItemTypeName(String itemTypeName) {
			this.itemTypeName = itemTypeName;
		}

		public List<ColumnCustomization> getColumnCustomizations() {
			return columnCustomizations;
		}

		public void setColumnCustomizations(List<ColumnCustomization> columnCustomizations) {
			this.columnCustomizations = columnCustomizations;
		}

		public boolean isItemDetailsViewDisabled() {
			return itemDetailsViewDisabled;
		}

		public void setItemDetailsViewDisabled(boolean itemDetailsViewDisabled) {
			this.itemDetailsViewDisabled = itemDetailsViewDisabled;
		}

		public boolean isItemTypeColumnAdded() {
			return itemTypeColumnAdded;
		}

		public void setItemTypeColumnAdded(boolean itemTypeColumnAdded) {
			this.itemTypeColumnAdded = itemTypeColumnAdded;
		}

		public boolean isPositionColumnAdded() {
			return positionColumnAdded;
		}

		public void setPositionColumnAdded(boolean positionColumnAdded) {
			this.positionColumnAdded = positionColumnAdded;
		}

		public boolean isFieldColumnsAdded() {
			return fieldColumnsAdded;
		}

		public void setFieldColumnsAdded(boolean fieldColumnsAdded) {
			this.fieldColumnsAdded = fieldColumnsAdded;
		}

		public boolean isStringValueColumnAdded() {
			return stringValueColumnAdded;
		}

		public void setStringValueColumnAdded(boolean stringValueColumnAdded) {
			this.stringValueColumnAdded = stringValueColumnAdded;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((itemTypeName == null) ? 0 : itemTypeName.hashCode());
			result = prime * result + ((listTypeName == null) ? 0 : listTypeName.hashCode());
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
			ListCustomization other = (ListCustomization) obj;
			if (itemTypeName == null) {
				if (other.itemTypeName != null)
					return false;
			} else if (!itemTypeName.equals(other.itemTypeName))
				return false;
			if (listTypeName == null) {
				if (other.listTypeName != null)
					return false;
			} else if (!listTypeName.equals(other.listTypeName))
				return false;
			return true;
		}

		@Override
		public int compareTo(ListCustomization o) {
			int result = MiscUtils.compareNullables(listTypeName, o.listTypeName);
			if (result == 0) {
				result = MiscUtils.compareNullables(itemTypeName, o.itemTypeName);
			}
			return result;
		}

		@Override
		public String toString() {
			return "ListCustomization [listTypeName=" + listTypeName + ", itemTypeName=" + itemTypeName + "]";
		}

	}

	public static class TreeStructureDiscoverySettings extends AbstractCustomization {
		private static final long serialVersionUID = 1L;

		protected boolean heterogeneousTree = true;
		protected ITypeInfoFinder customBaseNodeTypeFinder;
		protected List<InfoFilter> excludedSubListFields = new ArrayList<InfoFilter>();
		protected boolean singleSubListFieldNameNeverDisplayedAsTreeNode = true;

		public boolean isSingleSubListFieldNameNeverDisplayedAsTreeNode() {
			return singleSubListFieldNameNeverDisplayedAsTreeNode;
		}

		public void setSingleSubListFieldNameNeverDisplayedAsTreeNode(
				boolean singleSubListFieldNameNeverDisplayedAsTreeNode) {
			this.singleSubListFieldNameNeverDisplayedAsTreeNode = singleSubListFieldNameNeverDisplayedAsTreeNode;
		}

		public boolean isHeterogeneousTree() {
			return heterogeneousTree;
		}

		public void setHeterogeneousTree(boolean heterogeneousTree) {
			this.heterogeneousTree = heterogeneousTree;
		}

		@XmlElements({ @XmlElement(name = "javaClassBasedTypeInfoFinder", type = JavaClassBasedTypeInfoFinder.class),
				@XmlElement(name = "customTypeInfoFinder", type = CustomTypeInfoFinder.class) })
		public ITypeInfoFinder getCustomBaseNodeTypeFinder() {
			return customBaseNodeTypeFinder;
		}

		public void setCustomBaseNodeTypeFinder(ITypeInfoFinder customBaseNodeTypeFinder) {
			this.customBaseNodeTypeFinder = customBaseNodeTypeFinder;
		}

		public List<InfoFilter> getExcludedSubListFields() {
			return excludedSubListFields;
		}

		public void setExcludedSubListFields(List<InfoFilter> excludedSubListFields) {
			this.excludedSubListFields = excludedSubListFields;
		}

	}

	public static class ColumnCustomization extends AbstractCustomization implements Comparable<ColumnCustomization> {

		private static final long serialVersionUID = 1L;
		protected String columnName;
		protected String customCaption;
		protected boolean hidden = false;
		protected Integer minimalCharacterCount;

		@Override
		public boolean isInitial() {
			ColumnCustomization defaultColumnCustomization = new ColumnCustomization();
			defaultColumnCustomization.columnName = columnName;
			return InfoCustomizations.isSimilar(this, defaultColumnCustomization);
		}

		public String getColumnName() {
			return columnName;
		}

		public void setColumnName(String columnName) {
			this.columnName = columnName;
		}

		public boolean isHidden() {
			return hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}

		public String getCustomCaption() {
			return customCaption;
		}

		public void setCustomCaption(String customCaption) {
			this.customCaption = customCaption;
		}

		public Integer getMinimalCharacterCount() {
			return minimalCharacterCount;
		}

		public void setMinimalCharacterCount(Integer minimalCharacterCount) {
			this.minimalCharacterCount = minimalCharacterCount;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
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
			ColumnCustomization other = (ColumnCustomization) obj;
			if (columnName == null) {
				if (other.columnName != null)
					return false;
			} else if (!columnName.equals(other.columnName))
				return false;
			return true;
		}

		@Override
		public int compareTo(ColumnCustomization o) {
			return MiscUtils.compareNullables(columnName, o.columnName);
		}

		@Override
		public String toString() {
			return "ColumnCustomization [columnName=" + columnName + "]";
		}

	}

	public static interface ITypeInfoFinder {
		ITypeInfo find(ReflectionUI reflectionUI, SpecificitiesIdentifier specificitiesIdentifier);

		String getCriteria();
	}

	public static class JavaClassBasedTypeInfoFinder extends AbstractCustomization implements ITypeInfoFinder {

		private static final long serialVersionUID = 1L;
		protected String className;

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		@Override
		public String getCriteria() {
			return "className=" + ((className == null) ? "" : className);
		}

		public void validate() throws ClassNotFoundException {
			if ((className == null) || (className.length() == 0)) {
				throw new ReflectionUIError("Class name not specified !");
			}
			ClassUtils.getCachedClassforName(className);
		}

		@Override
		public ITypeInfo find(ReflectionUI reflectionUI, SpecificitiesIdentifier specificitiesIdentifier) {
			Class<?> javaType;
			try {
				javaType = ClassUtils.getCachedClassforName(className);
			} catch (ClassNotFoundException e) {
				throw new ReflectionUIError(e);
			}
			return reflectionUI.buildTypeInfo(new JavaTypeInfoSource(reflectionUI, javaType, specificitiesIdentifier));
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((className == null) ? 0 : className.hashCode());
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
			JavaClassBasedTypeInfoFinder other = (JavaClassBasedTypeInfoFinder) obj;
			if (className == null) {
				if (other.className != null)
					return false;
			} else if (!className.equals(other.className))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "JavaClassBasedTypeInfoFinder [className=" + className + "]";
		}

	}

	public static class CustomTypeInfoFinder extends AbstractCustomization implements ITypeInfoFinder {

		private static final long serialVersionUID = 1L;
		protected String implementationClassName;

		public String getImplementationClassName() {
			return implementationClassName;
		}

		public void setImplementationClassName(String implementationClassName) {
			this.implementationClassName = implementationClassName;
		}

		@Override
		public String getCriteria() {
			return "implementationClassName=" + ((implementationClassName == null) ? "" : implementationClassName);
		}

		public void validate() throws ClassNotFoundException {
			if ((implementationClassName == null) || (implementationClassName.length() == 0)) {
				throw new ReflectionUIError("Implementation class name not specified !");
			}
			ClassUtils.getCachedClassforName(implementationClassName);
		}

		@Override
		public ITypeInfo find(ReflectionUI reflectionUI, SpecificitiesIdentifier specificitiesIdentifier) {
			try {
				Class<?> implementationClass = ClassUtils.getCachedClassforName(implementationClassName);
				return (ITypeInfo) implementationClass.newInstance();
			} catch (Exception e) {
				throw new ReflectionUIError("Failed to instanciate class implenation class: " + e.toString(), e);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((implementationClassName == null) ? 0 : implementationClassName.hashCode());
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
			CustomTypeInfoFinder other = (CustomTypeInfoFinder) obj;
			if (implementationClassName == null) {
				if (other.implementationClassName != null)
					return false;
			} else if (!implementationClassName.equals(other.implementationClassName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "CustomTypeInfoFinder [implementationClassName=" + implementationClassName + "]";
		}

	}

	/**
	 * Maintains backward compatibility as much as possible with
	 * {@link InfoCustomizations} files by detecting and upgrading specific
	 * customizations.
	 * 
	 * @author olitank
	 *
	 */
	protected class Migrator {

		public void migrate() {
			boolean migrated = false;
			for (TypeCustomization tc : getTypeCustomizations()) {
				if (migrate(tc)) {
					migrated = true;
				}
			}
			if (migrated) {
				System.err.println("[" + InfoCustomizations.class.getName() + "] DEPRECATION NOTICE: " + this
						+ " was migrated automatically. The customizations file must be saved to remain valid with future releases.");
			}
		}

		private boolean migrate(TypeCustomization tc) {
			boolean migrated = false;
			if (tc.getTypeName().contains("NonNullableInstance")) {
				tc.setTypeName(tc.getTypeName().replace("NonNullableInstance", "MutableInstance"));
				migrated = true;
			}
			for (FieldCustomization fc : tc.getFieldsCustomizations()) {
				if (migrate(fc, tc)) {
					migrated = true;
				}
			}
			for (MethodCustomization mc : tc.getMethodsCustomizations()) {
				if (migrate(mc, tc)) {
					migrated = true;
				}
			}
			return migrated;
		}

		private boolean migrate(FieldCustomization fc, TypeCustomization containingTc) {
			boolean migrated = false;
			if (fc.getNullReplacement() != null) {
				if (fixHistoricalImageIconClassSwappingIssue(fc.getNullReplacement())) {
					migrated = true;
				}
			}
			for (TypeCustomization fieldTc : fc.getSpecificTypeCustomizations().getTypeCustomizations()) {
				if (migrate(fieldTc)) {
					migrated = true;
				}
			}
			return migrated;
		}

		private boolean migrate(MethodCustomization mc, TypeCustomization containingTc) {
			boolean migrated = false;
			if (mc.isReturnValueFieldGenerated()) {
				if (upgradeReturnValueFieldName(mc, containingTc)) {
					migrated = true;
				}
			}
			if (mc.getSerializedInvocationDatas().size() > 0) {
				if (upgradePresetMethodNames(mc, containingTc)) {
					migrated = true;
				}
			}
			for (ParameterCustomization pc : mc.getParametersCustomizations()) {
				if (migrate(pc, mc, containingTc)) {
					migrated = true;
				}
			}
			return migrated;
		}

		private boolean migrate(ParameterCustomization pc, MethodCustomization mc, TypeCustomization containingTc) {
			boolean migrated = false;
			if (pc.isDisplayedAsField()) {
				if (upgradeParameterFieldName(pc, mc, containingTc)) {
					migrated = true;
				}
			}
			return migrated;
		}

		private boolean upgradeParameterFieldName(ParameterCustomization pc, MethodCustomization mc,
				TypeCustomization containingTc) {
			boolean migrated = false;
			for (FieldCustomization siblingFc : containingTc.getFieldsCustomizations()) {
				if (siblingFc.getFieldName().equals(ParameterAsFieldInfo
						.buildLegacyParameterFieldName(mc.getMethodName(), pc.getParameterName()))) {
					String oldFieldName = siblingFc.getFieldName();
					siblingFc.setFieldName(ParameterAsFieldInfo.buildParameterFieldName(mc.getMethodSignature(),
							pc.getParameterName()));
					if (containingTc.getCustomFieldsOrder() != null) {
						MiscUtils.replaceItem(containingTc.getCustomFieldsOrder(), oldFieldName,
								siblingFc.getFieldName());
					}
					if (siblingFc.getEncapsulationFieldName() != null) {
						FieldCustomization capsuleFc = getFieldCustomization(containingTc,
								siblingFc.getEncapsulationFieldName(), false);
						if (capsuleFc != null) {
							String capsuleTypeName = CapsuleFieldInfo
									.buildTypeName(siblingFc.getEncapsulationFieldName(), containingTc.getTypeName());
							TypeCustomization capsuleTc = getTypeCustomization(InfoCustomizations.this, capsuleTypeName,
									false);
							if (capsuleTc != null) {
								upgradeParameterFieldName(pc, mc, capsuleTc);
							}
						}
					}
					migrated = true;
				}
			}
			return migrated;
		}

		private boolean upgradePresetMethodNames(MethodCustomization mc, TypeCustomization containingTc) {
			boolean migrated = false;
			List<TextualStorage> serializedInvocationDatas = mc.getSerializedInvocationDatas();
			for (int invocationDataIndex = 0; invocationDataIndex < serializedInvocationDatas
					.size(); invocationDataIndex++) {
				for (MethodCustomization siblingMc : containingTc.getMethodsCustomizations()) {
					if (siblingMc.getMethodName().equals(PresetInvocationDataMethodInfo
							.buildLegacyPresetMethodName(mc.getMethodName(), invocationDataIndex))) {
						String oldMethodSignature = siblingMc.getMethodSignature();
						siblingMc.setMethodName(PresetInvocationDataMethodInfo
								.buildPresetMethodName(mc.getMethodSignature(), invocationDataIndex));
						if (containingTc.getCustomMethodsOrder() != null) {
							MiscUtils.replaceItem(containingTc.getCustomMethodsOrder(), oldMethodSignature,
									siblingMc.getMethodSignature());
						}
						if (siblingMc.getEncapsulationFieldName() != null) {
							FieldCustomization capsuleFc = getFieldCustomization(containingTc,
									siblingMc.getEncapsulationFieldName(), false);
							if (capsuleFc != null) {
								String capsuleTypeName = CapsuleFieldInfo.buildTypeName(
										siblingMc.getEncapsulationFieldName(), containingTc.getTypeName());
								TypeCustomization capsuleTc = getTypeCustomization(InfoCustomizations.this,
										capsuleTypeName, false);
								if (capsuleTc != null) {
									upgradePresetMethodNames(mc, capsuleTc);
								}
							}
						}
						migrated = true;
					}
				}
			}
			return migrated;
		}

		private boolean upgradeReturnValueFieldName(MethodCustomization mc, TypeCustomization containingTc) {
			boolean migrated = false;
			for (FieldCustomization siblingFc : containingTc.getFieldsCustomizations()) {
				if (siblingFc.getFieldName()
						.equals(MethodReturnValueFieldInfo.buildLegacyReturnValueFieldName(mc.getMethodName()))) {
					String oldFieldName = siblingFc.getFieldName();
					siblingFc.setFieldName(
							MethodReturnValueFieldInfo.buildMethodReturnValueFieldName(mc.getMethodSignature()));
					if (containingTc.getCustomFieldsOrder() != null) {
						MiscUtils.replaceItem(containingTc.getCustomFieldsOrder(), oldFieldName,
								siblingFc.getFieldName());
					}
					if (siblingFc.getEncapsulationFieldName() != null) {
						FieldCustomization capsuleFc = getFieldCustomization(containingTc,
								siblingFc.getEncapsulationFieldName(), false);
						if (capsuleFc != null) {
							String capsuleTypeName = CapsuleFieldInfo
									.buildTypeName(siblingFc.getEncapsulationFieldName(), containingTc.getTypeName());
							TypeCustomization capsuleTc = getTypeCustomization(InfoCustomizations.this, capsuleTypeName,
									false);
							if (capsuleTc != null) {
								upgradeReturnValueFieldName(mc, capsuleTc);
							}
						}
					}
					migrated = true;
				}
			}
			return migrated;
		}

		private boolean fixHistoricalImageIconClassSwappingIssue(TextualStorage textualStorage) {
			if (textualStorage.getData() != null) {
				Mapping preConversion = textualStorage.getPreConversion();
				if (preConversion != null) {
					ConversionMethodFinder reverseConversionMethodFinder = preConversion
							.getReverseConversionMethodFinder();
					if (reverseConversionMethodFinder != null) {
						if (xy.reflect.ui.util.ImageIcon.class.getName()
								.equals(reverseConversionMethodFinder.getConversionClassName())) {
							String reverseConversionMethodSignature = reverseConversionMethodFinder
									.getConversionMethodSignature();
							if (reverseConversionMethodSignature != null) {
								if (ReflectionUIUtils.buildMethodSignature(Image.class.getName(), "getImage",
										Collections.emptyList()).equals(reverseConversionMethodSignature)) {
									try {
										byte[] binary = DatatypeConverter.parseBase64Binary(textualStorage.getData());
										try {
											ByteArrayInputStream bais = new ByteArrayInputStream(binary);
											ObjectInputStream ois = new ObjectInputStream(bais);
											if (!(ois.readObject() instanceof xy.reflect.ui.util.ImageIcon)) {
												throw new ClassCastException();
											}
										} catch (Exception e) {
											ByteArrayInputStream bais = new ByteArrayInputStream(binary);
											ObjectInputStream ois = IOUtils.getClassSwappingObjectInputStream(bais,
													javax.swing.ImageIcon.class.getName(),
													xy.reflect.ui.util.ImageIcon.class.getName());
											xy.reflect.ui.util.ImageIcon xyIcon = (xy.reflect.ui.util.ImageIcon) ois
													.readObject();
											Filter<Object> reverseConversionMethod = preConversion
													.buildOverallReverseConversionMethod();
											Image image = (Image) reverseConversionMethod.get(xyIcon);
											textualStorage.save(image);
											return true;
										}
									} catch (Exception e) {
										throw new ReflectionUIError(e);
									}
								}
							}
						}
					}
				}
			}
			return false;
		}

	}

}
