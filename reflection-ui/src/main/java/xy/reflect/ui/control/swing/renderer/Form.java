
package xy.reflect.ui.control.swing.renderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.IAdvancedFieldControl;
import xy.reflect.ui.control.IAdvancedMethodControl;
import xy.reflect.ui.control.IFieldControlData;
import xy.reflect.ui.control.swing.builder.DialogBuilder;
import xy.reflect.ui.control.swing.builder.DialogBuilder.RenderedDialog;
import xy.reflect.ui.control.swing.menu.Menu;
import xy.reflect.ui.control.swing.util.AbstractControlButton;
import xy.reflect.ui.control.swing.util.BetterGridLayout;
import xy.reflect.ui.control.swing.util.ControlPanel;
import xy.reflect.ui.control.swing.util.ControlScrollPane;
import xy.reflect.ui.control.swing.util.ControlTabbedPane;
import xy.reflect.ui.control.swing.util.ClassicTabbedPane;
import xy.reflect.ui.control.swing.util.HyperlinkLabel;
import xy.reflect.ui.control.swing.util.ImagePanel;
import xy.reflect.ui.control.swing.util.ListTabbedPane;
import xy.reflect.ui.control.swing.util.ModificationStackControls;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ValidationSession;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.menu.AbstractActionMenuItemInfo;
import xy.reflect.ui.info.menu.IMenuElementInfo;
import xy.reflect.ui.info.menu.MenuInfo;
import xy.reflect.ui.info.menu.MenuModel;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.MethodsLayout;
import xy.reflect.ui.info.type.factory.FilteredTypeFactory;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.undo.SlaveModificationStack;
import xy.reflect.ui.util.Filter;
import xy.reflect.ui.util.MinimalListUpdater;
import xy.reflect.ui.util.BetterFutureTask;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.ValidationErrorWrapper;

/**
 * Instances of this class are forms allowing to edit any object. The layout and
 * the controls are generated according to type information ({@link ITypeInfo})
 * extracted form the underlying object.
 * 
 * A {@link ModificationStack} is used to record and revert/replay
 * modifications. It is also used as modification event producer in order to
 * refresh and then keep the field controls up to date when a modification is
 * detected.
 * 
 * @author olitank
 *
 */
public class Form extends ImagePanel {

	private static final long serialVersionUID = 1L;

	public static final String ACTION_MENU_ITEM_CONTEXT_FORM = Form.class.getName() + ".actionMenuItemContextForm";

	protected SwingRenderer swingRenderer;
	protected Object object;
	protected ITypeInfo filteredObjectType;
	protected ModificationStack modificationStack;
	protected boolean forwardingUpdateEventToTwinFormsDisabled = false;
	protected IInfoFilter infoFilter;
	protected SortedMap<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory = new TreeMap<InfoCategory, List<FieldControlPlaceHolder>>();
	protected SortedMap<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory = new TreeMap<InfoCategory, List<MethodControlPlaceHolder>>();

	protected Container categoriesControl;
	protected MinimalListUpdater<InfoCategory> categoriesVisibilityUpdater;
	protected JScrollPane scrollPane;
	protected IModificationListener fieldsUpdateListener = createFieldsUpdateListener();
	protected boolean visibilityEventsDisabled = false;
	protected List<IFormListener> listeners = new ArrayList<IFormListener>();
	protected JLabel statusBar;
	protected JMenuBar menuBar;
	protected boolean absolutelyVisible = false;
	protected BetterFutureTask<Boolean> currentValidationTask;
	protected MinimalListUpdater<MenuInfo> menuBarUpdater;

	/**
	 * Creates a form allowing to view/edit the given object.
	 * 
	 * @param swingRenderer The renderer used to generate this form controls.
	 * @param object        The object that will be viewed/edited through this form.
	 * @param infoFilter    A filter that will be used to exclude some
	 *                      fields/methods of the object.
	 */
	public Form(SwingRenderer swingRenderer, final Object object, IInfoFilter infoFilter) {
		final ITypeInfo objectType = swingRenderer.getReflectionUI()
				.getTypeInfo(swingRenderer.getReflectionUI().getTypeInfoSource(object));
		swingRenderer.showBusyDialogWhile(this, new Runnable() {
			@Override
			public void run() {
				objectType.onFormCreation(object, true);
			}
		}, getInitializationJobTitle());
		try {
			this.swingRenderer = swingRenderer;
			setObject(object);
			setInfoFilter(infoFilter);
			setModificationStack(new ModificationStack(null));
			getModificationStack().addListener(fieldsUpdateListener);
			handleVisibilityEvents();
			menuBar = createMenuBar();
			statusBar = createStatusBar();
			refresh(true);
		} finally {
			swingRenderer.showBusyDialogWhile(this, new Runnable() {
				@Override
				public void run() {
					objectType.onFormCreation(object, false);
				}
			}, getInitializationJobTitle());

		}
	}

	protected String getInitializationJobTitle() {
		return "Setting up...";
	}

	protected String getFinalizationJobTitle() {
		return "Cleaning up...";
	}

	public ITypeInfo getFilteredObjectType() {
		return filteredObjectType;
	}

	public BetterFutureTask<Boolean> getCurrentValidationTask() {
		return currentValidationTask;
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		ensureNoCurrentValidationTask();
	}

	protected void handleVisibilityEvents() {
		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				if (visibilityEventsDisabled) {
					return;
				}
				try {
					formShown();
				} catch (Throwable t) {
					Form.this.swingRenderer.handleException(Form.this, t);
				}
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				if (visibilityEventsDisabled) {
					return;
				}
				try {
					formHidden();
				} catch (Throwable t) {
					Form.this.swingRenderer.handleException(Form.this, t);
				}
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

		});
	}

	/**
	 * @return The object that is viewed/edited through this form.
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * Changes the object that is viewed/edited through this form. Note that this
	 * method does not refresh the form. {@link #refresh(boolean)} should be called
	 * after this method to actually display the new object values.
	 * 
	 * @param object The new object.
	 */
	public void setObject(Object object) {
		ensureNoCurrentValidationTask();
		this.object = object;
	}

	/**
	 * @return whether this form is currently visible on the screen. Note that
	 *         unlike the {@link #isVisible()} method, this method return value
	 *         interpretation does not depend on the parent visibility.
	 */
	public boolean isAbsolutelyVisible() {
		return absolutelyVisible;
	}

	/**
	 * @return the renderer used to generate this form controls.
	 */
	public SwingRenderer getSwingRenderer() {
		return swingRenderer;
	}

	/**
	 * @return the modification stack associated with this form.
	 */
	public ModificationStack getModificationStack() {
		return modificationStack;
	}

	/**
	 * Changes the modification stack associated with this form.
	 * 
	 * @param modificationStack The new modification stack.
	 */
	public void setModificationStack(ModificationStack modificationStack) {
		this.modificationStack = modificationStack;
	}

	/**
	 * @return whether the forwarding of field update events to twin forms (other
	 *         forms that display the same object as this form) is disabled or not.
	 */
	public boolean isForwardingUpdateEventToTwinFormsDisabled() {
		return forwardingUpdateEventToTwinFormsDisabled;
	}

	/**
	 * Updates whether the forwarding of field update events to twin forms (other
	 * forms that display the same object as this form) is disabled or not.
	 * 
	 * @param forwardingUpdateEventToTwinFormsDisabled The new listener status.
	 */
	public void setForwardingUpdateEventToTwinFormsDisabled(boolean forwardingUpdateEventToTwinFormsDisabled) {
		this.forwardingUpdateEventToTwinFormsDisabled = forwardingUpdateEventToTwinFormsDisabled;
	}

	/**
	 * @return the filter that is used to exclude some fields/methods of the object.
	 */
	public IInfoFilter getInfoFilter() {
		return infoFilter;
	}

	/**
	 * Changes the filter that is used to exclude some fields/methods of the object.
	 * Note that this method does not refresh the form. {@link #refresh(boolean)}
	 * should be called with the parameter refreshStructure=true after to actually
	 * apply the new filter.
	 * 
	 * @param infoFilter The new filter.
	 */
	public void setInfoFilter(IInfoFilter infoFilter) {
		this.infoFilter = infoFilter;
	}

	/**
	 * @return the menu bar component.
	 */
	public JMenuBar getMenuBar() {
		return menuBar;
	}

	/**
	 * @return the status bar component.
	 */
	public JLabel getStatusBar() {
		return statusBar;
	}

	/**
	 * @return the map the current field control place holders by category.
	 */
	public SortedMap<InfoCategory, List<FieldControlPlaceHolder>> getFieldControlPlaceHoldersByCategory() {
		return fieldControlPlaceHoldersByCategory;
	}

	/**
	 * @return the map the current method control place holders by category.
	 */
	public SortedMap<InfoCategory, List<MethodControlPlaceHolder>> getMethodControlPlaceHoldersByCategory() {
		return methodControlPlaceHoldersByCategory;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (result == null) {
			result = new Dimension(100, 100);
		}
		if (filteredObjectType != null) {
			Dimension configuredSize = new Dimension(filteredObjectType.getFormPreferredWidth(),
					filteredObjectType.getFormPreferredHeight());
			if (configuredSize.width > 0) {
				result.width = configuredSize.width;
			}
			if (configuredSize.height > 0) {
				result.height = configuredSize.height;
			}
		}
		return result;
	}

	@Override
	public Dimension getMinimumSize() {
		Dimension result = super.getMinimumSize();
		if (result != null) {
			Dimension preferredSize = getPreferredSize();
			if (preferredSize != null) {
				result.width = Math.min(result.width, preferredSize.width);
				result.height = Math.min(result.height, preferredSize.height);
			}
		}
		return result;

	}

	@Override
	public Dimension getMaximumSize() {
		Dimension result = super.getMaximumSize();
		if (result != null) {
			Dimension preferredSize = getPreferredSize();
			if (preferredSize != null) {
				result.width = Math.max(result.width, preferredSize.width);
				result.height = Math.max(result.height, preferredSize.height);
			}
		}
		return result;
	}

	/**
	 * Uses type information to check that the state of the underlying object is
	 * valid. Note that this method should not be called from the UI thread.
	 * 
	 * @param session If the state of the underlying object is not valid.
	 * @throws Exception If the state of the underlying object is not valid.
	 */
	public void validateForm(ValidationSession session) throws Exception {
		try {
			swingRenderer.getReflectionUI().getValidationErrorRegistry().attributing(object, (sessionArg) -> {
				ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
				List<InfoCategory> allCategories = collectCategories(fieldControlPlaceHoldersByCategory,
						methodControlPlaceHoldersByCategory);
				boolean categoriesDisplayed = shouldCategoriesBeDisplayed(allCategories);
				for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
					for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHoldersByCategory
							.get(category)) {
						if (swingRenderer.getReflectionUI().getValidationErrorRegistry()
								.isValidationCancelled(Thread.currentThread())) {
							return;
						}
						if (!fieldControlPlaceHolder.isVisible()) {
							continue;
						}
						if (!fieldControlPlaceHolder.getControlData().isControlValueValiditionEnabled()) {
							continue;
						}
						Component fieldControl = fieldControlPlaceHolder.getFieldControl();
						if (fieldControl instanceof IAdvancedFieldControl) {
							try {
								((IAdvancedFieldControl) fieldControl).validateControlData(session);
								if (!swingRenderer.getReflectionUI().getValidationErrorRegistry()
										.isValidationCancelled(Thread.currentThread())) {
									fieldControlPlaceHolder.setBorder(null);
								}
							} catch (Exception e) {
								if (!(e instanceof ValidationErrorWrapper)) {
									fieldControlPlaceHolder.setBorder(swingRenderer.getErrorBorder());
								}
								String contextCaption = null;
								IFieldInfo field = fieldControlPlaceHolder.getField();
								if (field.getCaption().length() > 0) {
									contextCaption = field.getCaption();
								}
								if (categoriesDisplayed) {
									contextCaption = category.getCaption();
								}
								throw new ValidationErrorWrapper(contextCaption, e);
							}
						}
					}
				}
				for (InfoCategory category : methodControlPlaceHoldersByCategory.keySet()) {
					for (MethodControlPlaceHolder methodControlPlaceHolder : methodControlPlaceHoldersByCategory
							.get(category)) {
						if (swingRenderer.getReflectionUI().getValidationErrorRegistry()
								.isValidationCancelled(Thread.currentThread())) {
							return;
						}
						if (!methodControlPlaceHolder.isVisible()) {
							continue;
						}
						if (!methodControlPlaceHolder.getControlData().isControlReturnValueValiditionEnabled()) {
							continue;
						}
						Component methodControl = methodControlPlaceHolder.getMethodControl();
						if (methodControl instanceof IAdvancedMethodControl) {
							try {
								((IAdvancedMethodControl) methodControl).validateControlData(session);
								if (!swingRenderer.getReflectionUI().getValidationErrorRegistry()
										.isValidationCancelled(Thread.currentThread())) {
									methodControlPlaceHolder.setBorder(null);
								}
							} catch (Exception e) {
								methodControlPlaceHolder.setBorder(swingRenderer.getErrorBorder());
								String contextCaption = null;
								IMethodInfo method = methodControlPlaceHolder.getMethod();
								if (method.getCaption().length() > 0) {
									contextCaption = method.getCaption();
								}
								if (categoriesDisplayed) {
									contextCaption = category.getCaption();
								}
								throw new ValidationErrorWrapper(contextCaption, e);
							}
						}
					}
				}
				ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
				type.validate(object, session);
			}, validationError -> swingRenderer.getReflectionUI().logDebug(validationError)).validate(session);
			if (!swingRenderer.getReflectionUI().getValidationErrorRegistry()
					.isValidationCancelled(Thread.currentThread())) {
				for (IFormListener l : listeners) {
					l.afterValidation(null);
				}
			}
		} catch (Exception e) {
			for (IFormListener l : listeners) {
				l.afterValidation(e);
			}
			throw e;
		}
	}

	/**
	 * Runs {@link #validateForm()} asynchronously and updates the status bar
	 * accordingly (displays a validation error if the form is not valid).
	 */
	public void validateFormInBackgroundAndReportOnStatusBar() {
		ensureNoCurrentValidationTask();
		currentValidationTask = swingRenderer.getReflectionUI().getValidationErrorRegistry()
				.createValidationTask(new Runnable() {
					boolean completedValidationStatusReported = false;

					@Override
					public void run() {
						final Icon initialStatusBarIcon = statusBar.getIcon();
						scheduleOngoingValidationStatusDisplay();
						try {
							validateForm(new ValidationSession());
						} catch (Exception e) {
							return;
						} catch (Throwable t) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									swingRenderer.handleException(Form.this, t);
								}
							});
						} finally {
							currentValidationTask = null;
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									statusBar.setIcon(initialStatusBarIcon);
									refreshValidityComponents();
									completedValidationStatusReported = true;
								}
							});
						}
					}

					void scheduleOngoingValidationStatusDisplay() {
						swingRenderer.getDelayedUpdateExecutorService().submit(new Runnable() {
							BetterFutureTask<Boolean> task = currentValidationTask;

							@Override
							public void run() {
								try {
									task.get(getOngoingValidationStausDisplayDelayMilliseconds(),
											TimeUnit.MILLISECONDS);
								} catch (TimeoutException e) {
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											if (completedValidationStatusReported) {
												return;
											}
											if (!statusBar.isVisible()) {
												showErrorOnStatusBar(
														new ReflectionUIError(getOngoingValidationMessage()));
											}
											statusBar.setIcon(getOngoingValidationIcon());
										}
									});
								} catch (CancellationException | ExecutionException e) {
									return;
								} catch (InterruptedException e) {
									throw new ReflectionUIError(e);
								}
							}
						});
					}
				});
		swingRenderer.getFormValidatorService().submit(currentValidationTask);
	}

	protected long getOngoingValidationStausDisplayDelayMilliseconds() {
		return 3000;
	}

	protected Icon getOngoingValidationIcon() {
		return SwingRendererUtils.ERROR_REFRESHING_ICON;
	}

	protected String getOngoingValidationMessage() {
		return "Validating...";
	}

	protected void refreshValidityComponents() {
		Exception validationError = swingRenderer.getReflectionUI().getValidationErrorRegistry()
				.getValidationError(object, null);
		showErrorOnStatusBar(validationError);
		setStandardOKButtonEnabled((validationError == null) || !filteredObjectType.isValidationRequired());
	}

	protected void setStandardOKButtonEnabled(boolean b) {
		if (SwingRendererUtils.findAncestorForms(this, swingRenderer).size() > 0) {
			return;
		}
		Window window = SwingRendererUtils.getWindowAncestorOrSelf(this);
		if (!(window instanceof RenderedDialog)) {
			return;
		}
		DialogBuilder dialogBuilder = ((RenderedDialog) window).getDialogBuilder();
		JButton button = dialogBuilder.getStandardOKButton();
		if (button == null) {
			return;
		}
		button.setEnabled(b);
	}

	protected void showErrorOnStatusBar(Exception error) {
		Exception previousError = (Exception) ((HyperlinkLabel) statusBar).getCustomValue();
		boolean statusUpdateRequired;
		try {
			statusUpdateRequired = !MiscUtils.sameExceptionOrBothNull(error, previousError);
		} catch (Exception e) {
			swingRenderer.getReflectionUI().logDebug(
					"WARNING: Failed to compare exceptions '" + error + "' and '" + previousError + "': " + e);
			statusUpdateRequired = true;
		}
		if (statusUpdateRequired) {
			String errorSummary;
			if (error == null) {
				errorSummary = null;
			} else {
				errorSummary = MiscUtils.getPrettyErrorMessage(error);
				errorSummary = MiscUtils.multiToSingleLine(errorSummary);
				errorSummary = MiscUtils.truncateNicely(errorSummary, getErrorSummaryMaximumLength());
			}
			if (errorSummary == null) {
				statusBar.setIcon(null);
				statusBar.setToolTipText(null);
				((HyperlinkLabel) statusBar).setRawTextAndLinkOpener(null, null);
				((HyperlinkLabel) statusBar).setCustomValue(null);
			} else {
				statusBar.setIcon(SwingRendererUtils.ERROR_ICON);
				statusBar.setToolTipText(SwingRendererUtils.adaptToolTipTextToMultiline(errorSummary));
				((HyperlinkLabel) statusBar).setRawTextAndLinkOpener(errorSummary,
						getOngoingValidationMessage().equals(error.getMessage()) ? null : new Runnable() {
							@Override
							public void run() {
								swingRenderer.openErrorDetailsDialog(statusBar,
										ValidationErrorWrapper.unwrapValidationError(error));
							}

						});
				((HyperlinkLabel) statusBar).setCustomValue(error);
			}
			SwingRendererUtils.handleComponentSizeChange(statusBar);
		}
		statusBar.setVisible(statusBar.getText() != null);
	}

	protected int getErrorSummaryMaximumLength() {
		return 150;
	}

	protected void formShown() {
		absolutelyVisible = true;
		swingRenderer.getAllDisplayedForms().add(this);
		swingRenderer.showBusyDialogWhile(this, new Runnable() {
			@Override
			public void run() {
				Form.this.filteredObjectType.onFormVisibilityChange(object, true);
			}
		}, getInitializationJobTitle());
	}

	protected void formHidden() {
		absolutelyVisible = false;
		swingRenderer.getAllDisplayedForms().remove(this);
		swingRenderer.showBusyDialogWhile(this, new Runnable() {
			@Override
			public void run() {
				Form.this.filteredObjectType.onFormVisibilityChange(object, false);
			}
		}, getFinalizationJobTitle());
	}

	protected boolean isModificationStackSlave() {
		return modificationStack instanceof SlaveModificationStack;
	}

	protected IModificationListener createFieldsUpdateListener() {
		return new AbstractSimpleModificationListener() {
			@Override
			public void afterClearInvalidation() {
				// no event
			}

			@Override
			protected void handleAnyEvent(IModification modification) {
				onFieldsUpdate((modification != null) && modification.isVolatile());
			}
		};
	}

	protected void onFieldsUpdate(boolean sourceModificationVolatile) {
		if (isModificationStackSlave()) {
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (isAbsolutelyVisible()) {
					refresh(false);
				}
				forwardUpdateEventToTwins(sourceModificationVolatile);
			}
		});
	}

	protected void forwardUpdateEventToTwins(boolean sourceModificationVolatile) {
		if (isForwardingUpdateEventToTwinFormsDisabled()) {
			return;
		}
		for (Form twinForm : SwingRendererUtils.findObjectDisplayedForms(object, swingRenderer)) {
			if (twinForm != Form.this) {
				ModificationStack twinFormModifStack = twinForm.getModificationStack();
				if (twinForm.isDisplayable()) {
					twinForm.setForwardingUpdateEventToTwinFormsDisabled(true);
					if (sourceModificationVolatile) {
						twinFormModifStack.push(IModification.VOLATILE_MODIFICATION);
					} else {
						twinFormModifStack.invalidate();
					}
					twinForm.setForwardingUpdateEventToTwinFormsDisabled(false);
				}
			}
		}
	}

	protected JScrollPane createMainScrollPane(Component content) {
		ControlScrollPane result = new ControlScrollPane(content);
		SwingRendererUtils.removeScrollPaneBorder(result);
		return result;
	}

	protected void layoutMembersControlPlaceHolders(
			Map<InfoCategory, List<FieldControlPlaceHolder>> fieldControlPlaceHoldersByCategory,
			Map<InfoCategory, List<MethodControlPlaceHolder>> methodControlPlaceHoldersByCategory,
			JPanel membersPanel) {
		if ((filteredObjectType.getFormPreferredWidth() != -1) || (filteredObjectType.getFormPreferredHeight() != -1)) {
			ControlPanel contentPanel = new ControlPanel();
			{
				membersPanel.setLayout(new BorderLayout());
				membersPanel.add(scrollPane = createMainScrollPane(contentPanel), BorderLayout.CENTER);
			}
			membersPanel = contentPanel;
		} else {
			scrollPane = null;
		}
		List<InfoCategory> allCategories = collectCategories(fieldControlPlaceHoldersByCategory,
				methodControlPlaceHoldersByCategory);
		if (shouldCategoriesBeDisplayed(allCategories)) {
			membersPanel.setLayout(new BorderLayout());
			categoriesControl = createCategoriesControl();
			categoriesVisibilityUpdater = createCategoriesVisibilityUpdater(allCategories);
			membersPanel.add(categoriesControl, BorderLayout.CENTER);
			Form form = SwingRendererUtils.findParentForm(categoriesControl, swingRenderer);
			if (form == null) {
				throw new ReflectionUIError();
			}
		} else {
			List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory.values()
					.stream().flatMap(List::stream).collect(Collectors.toList());
			if (fieldControlPlaceHolders == null) {
				fieldControlPlaceHolders = Collections.emptyList();
			}
			List<MethodControlPlaceHolder> methodControlPlaceHolders = methodControlPlaceHoldersByCategory.values()
					.stream().flatMap(List::stream).collect(Collectors.toList());
			if (methodControlPlaceHolders == null) {
				methodControlPlaceHolders = Collections.emptyList();
			}
			categoriesControl = null;
			categoriesVisibilityUpdater = null;
			layoutMembersControlPlaceHolders(fieldControlPlaceHolders, methodControlPlaceHolders, membersPanel);
		}
	}

	protected MinimalListUpdater<InfoCategory> createCategoriesVisibilityUpdater(List<InfoCategory> allCategories) {

		return new MinimalListUpdater<InfoCategory>() {
			Map<InfoCategory, Component> tabByCategory = new HashMap<InfoCategory, Component>();
			{
				for (final InfoCategory category : allCategories) {
					List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory
							.get(category);
					if (fieldControlPlaceHolders == null) {
						fieldControlPlaceHolders = Collections.emptyList();
					}
					List<MethodControlPlaceHolder> methodControlPlaceHolders = methodControlPlaceHoldersByCategory
							.get(category);
					if (methodControlPlaceHolders == null) {
						methodControlPlaceHolders = Collections.emptyList();
					}

					JPanel tab = new ControlPanel();
					tab.setName("categoryContainerControl [category=" + category.getCaption() + "]");
					tab.setLayout(new BorderLayout());

					JPanel tabContent = new ControlPanel();
					tabContent.setName("categoryContentControl [category=" + category.getCaption() + "]");
					tab.add(tabContent, BorderLayout.CENTER);
					layoutMembersControlPlaceHolders(fieldControlPlaceHolders, methodControlPlaceHolders, tabContent);
					tabByCategory.put(category, tab);
				}
			}

			@Override
			protected List<InfoCategory> collectKeys() {
				return allCategories;
			}

			@Override
			protected boolean shouldBePresent(InfoCategory category) {
				List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory
						.get(category);
				if (fieldControlPlaceHolders != null) {
					for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHolders) {
						if (fieldControlPlaceHolder.isVisible()) {
							return true;
						}
					}
				}
				List<MethodControlPlaceHolder> methodControlPlaceHolders = methodControlPlaceHoldersByCategory
						.get(category);
				if (methodControlPlaceHolders != null) {
					for (MethodControlPlaceHolder methodControlPlaceHolder : methodControlPlaceHolders) {
						if (methodControlPlaceHolder.isVisible()) {
							return true;
						}
					}
				}
				return false;
			}

			@Override
			protected void add(int tabIndex, InfoCategory category) {
				Component tab = tabByCategory.get(category);
				Image iconImage = swingRenderer.getCategoryIconImage(category);
				ImageIcon icon = (iconImage != null) ? new ImageIcon(iconImage) : null;
				if (categoriesControl instanceof ListTabbedPane) {
					((ListTabbedPane) categoriesControl).insertTab(
							swingRenderer.prepareMessageToDisplay(category.getCaption()), icon, tab, tabIndex);
				} else if (categoriesControl instanceof ClassicTabbedPane) {
					((ClassicTabbedPane) categoriesControl).insertTab(
							swingRenderer.prepareMessageToDisplay(category.getCaption()), icon, tab, "", tabIndex);
				} else if (categoriesControl instanceof ControlTabbedPane) {
					((ControlTabbedPane) categoriesControl).insertTab(
							swingRenderer.prepareMessageToDisplay(category.getCaption()), icon, tab, "", tabIndex);
				} else {
					throw new ReflectionUIError();
				}
			}

			@Override
			protected void remove(int index) {
				if (categoriesControl instanceof ListTabbedPane) {
					((ListTabbedPane) categoriesControl).removeTabAt(index);
				} else if (categoriesControl instanceof ClassicTabbedPane) {
					((ClassicTabbedPane) categoriesControl).removeTabAt(index);
				} else if (categoriesControl instanceof ControlTabbedPane) {
					((ControlTabbedPane) categoriesControl).removeTabAt(index);
				} else {
					throw new ReflectionUIError();
				}
			}

		};
	}

	protected boolean shouldCategoriesBeDisplayed(List<InfoCategory> categories) {
		return !((categories.size() == 1) && (swingRenderer.getNullInfoCategory().equals(categories.get(0))))
				&& (categories.size() > 0);
	}

	protected <T1, T2> List<InfoCategory> collectCategories(Map<InfoCategory, T1> t1ByCategory,
			Map<InfoCategory, T2> t2ByCategory) {
		SortedSet<InfoCategory> result = new TreeSet<InfoCategory>();
		result.addAll(t1ByCategory.keySet());
		result.addAll(t2ByCategory.keySet());
		return new ArrayList<InfoCategory>(result);
	}

	protected JLabel createStatusBar() {
		HyperlinkLabel result = new HyperlinkLabel() {

			private static final long serialVersionUID = 1L;

			private static final int PREFERRED_WIDTH = 300;

			@Override
			public Dimension getPreferredSize() {
				Dimension result = super.getPreferredSize();
				if (result != null) {
					result.width = PREFERRED_WIDTH;
				}
				return result;
			}

			@Override
			public Dimension getMaximumSize() {
				Dimension result = super.getMaximumSize();
				if (result != null) {
					result.width = Math.max(result.width, PREFERRED_WIDTH);
				}
				return result;
			}

			@Override
			public Dimension getMinimumSize() {
				Dimension result = super.getMinimumSize();
				if (result != null) {
					result.width = Math.min(result.width, PREFERRED_WIDTH);
				}
				return result;
			}

		};
		result.setFont(new JToolTip().getFont());
		result.setCustomValue(new Exception("INITIAL_UNDEFINED_ERROR"));
		result.setName("statusBar");
		result.setBackground(new Color(255, 200, 200));
		result.setOpaque(true);
		result.setForeground(new Color(255, 0, 0));
		result.setBorder(BorderFactory.createLineBorder(new Color(255, 0, 0), 1, true));
		return result;
	}

	protected JMenuBar createMenuBar() {
		JMenuBar result = new JMenuBar();
		result.setName("menuBar");
		return result;
	}

	protected int getExpectedCategoriesControlPlacement() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.STANDARD) {
			return JTabbedPane.TOP;
		} else if (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.CLASSIC) {
			return JTabbedPane.TOP;
		} else if (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.MODERN) {
			return JTabbedPane.TOP;
		} else if (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.STANDARD_VERTICAL) {
			return JTabbedPane.LEFT;
		} else if (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.CLASSIC_VERTICAL) {
			return JTabbedPane.LEFT;
		} else if (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.MODERN_VERTICAL) {
			return JTabbedPane.LEFT;
		} else {
			throw new ReflectionUIError();
		}
	}

	protected Container createCategoriesControl() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if ((type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.MODERN)
				|| (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.MODERN_VERTICAL)) {
			return new ListTabbedPane(ClassicTabbedPane.TOP) {

				private static final long serialVersionUID = 1L;
				private JButton nonSelectedCellRenderer;
				private JLabel selectedCellRenderer;

				{
					setOpaque(false);
					setName("categoriesControl [parent=" + this.getName() + "]");
				}

				@Override
				protected JScrollPane wrapListControl(@SuppressWarnings("rawtypes") JList listControl) {
					JScrollPane result = new ControlScrollPane(listControl);
					result.setBorder(null);
					return result;
				}

				@Override
				protected JPanel createCurrentComponentContainer() {
					JPanel result = super.createCurrentComponentContainer();
					result.setOpaque(false);
					return result;
				}

				@Override
				protected JButton createNonSelectedTabHeaderCellRendererComponent() {
					return nonSelectedCellRenderer = super.createNonSelectedTabHeaderCellRendererComponent();
				}

				@Override
				protected JLabel createSelectedTabHeaderCellRendererComponent() {
					return selectedCellRenderer = super.createSelectedTabHeaderCellRendererComponent();
				}

				protected void refreshCurrentComponentContainerBorder() {
					Color tabBorderColor = getControlsBorderColor();
					if (tabBorderColor != null) {
						currentComponentContainer.setBorder(BorderFactory.createLineBorder(tabBorderColor));
					} else {
						currentComponentContainer.setBorder(BorderFactory.createTitledBorder(""));
					}
				}

				protected void refreshNonSelectedCellRenderer() {
					nonSelectedCellRenderer.setForeground(getCategoriesCellForegroundColor());
					nonSelectedCellRenderer.setBorderPainted(false);
					nonSelectedCellRenderer.setBorder(BorderFactory.createEmptyBorder(getLayoutSpacing(),
							getLayoutSpacing(), getLayoutSpacing(), getLayoutSpacing()));
					Color backgroundColor = getCategoriesCellBackgroundColor();
					if (backgroundColor != null) {
						nonSelectedCellRenderer.setContentAreaFilled(true);
						nonSelectedCellRenderer.setBackground(backgroundColor);
					} else {
						nonSelectedCellRenderer.setContentAreaFilled(false);
						nonSelectedCellRenderer.setBackground(null);
					}
					Font labelCustomFont = getLabelCustomFont();
					{
						if (labelCustomFont != null) {
							nonSelectedCellRenderer
									.setFont(labelCustomFont.deriveFont(nonSelectedCellRenderer.getFont().getStyle(),
											nonSelectedCellRenderer.getFont().getSize()));
						} else {
							nonSelectedCellRenderer.setFont(new JButton().getFont());
						}
					}
				}

				protected void refreshSelectedCellRenderer() {
					selectedCellRenderer.setForeground(getCategoriesCellForegroundColor());
					Color backgroundColor = getCategoriesCellBackgroundColor();
					selectedCellRenderer.setBorder(BorderFactory.createEmptyBorder(getLayoutSpacing(),
							getLayoutSpacing(), getLayoutSpacing(), getLayoutSpacing()));
					if (backgroundColor != null) {
						selectedCellRenderer.setOpaque(true);
						selectedCellRenderer
								.setBackground(SwingRendererUtils.addColorActivationEffect(backgroundColor));
					} else {
						selectedCellRenderer.setOpaque(false);
						selectedCellRenderer.setBackground(null);
						selectedCellRenderer
								.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
										BorderFactory.createEmptyBorder(getLayoutSpacing() - 2, getLayoutSpacing() - 2,
												getLayoutSpacing() - 2, getLayoutSpacing() - 2)));
					}
					Font labelCustomFont = getLabelCustomFont();
					{
						if (labelCustomFont != null) {
							selectedCellRenderer
									.setFont(labelCustomFont.deriveFont(selectedCellRenderer.getFont().getStyle(),
											selectedCellRenderer.getFont().getSize()));
						} else {
							selectedCellRenderer.setFont(new JLabel().getFont());
						}
					}
				}

				protected void refreshNonSelectableArea() {
					Color backgroundColor = getCategoriesCellBackgroundColor();
					if (backgroundColor != null) {
						titleListControl.setOpaque(true);
						titleListControl.setBackground(backgroundColor);
						titleListControlWrapper.setOpaque(true);
						titleListControlWrapper.setBackground(backgroundColor);
					} else {
						titleListControl.setOpaque(false);
						titleListControlWrapper.setOpaque(false);
					}
				}

				@Override
				public void refresh() {
					refreshSelectedCellRenderer();
					refreshNonSelectedCellRenderer();
					refreshNonSelectableArea();
					refreshCurrentComponentContainerBorder();
					super.refresh();
				}

			};
		} else if ((type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.CLASSIC)
				|| (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.CLASSIC_VERTICAL)) {
			return new ClassicTabbedPane() {

				private static final long serialVersionUID = 1L;

				@Override
				protected Color getTabBorderColor() {
					Color result = getControlsBorderColor();
					if (result != null) {
						return result;
					}
					return super.getTabBorderColor();
				}

			};
		} else if ((type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.STANDARD)
				|| (type.getCategoriesStyle() == ITypeInfo.CategoriesStyle.STANDARD_VERTICAL)) {
			return new ControlTabbedPane();
		} else {
			throw new ReflectionUIError();
		}
	}

	protected void refreshCategoriesControlStructure() {
		if (categoriesControl != null) {
			if (categoriesControl instanceof ListTabbedPane) {
				((ListTabbedPane) categoriesControl).refresh();
				((ListTabbedPane) categoriesControl).setTabPlacement(getExpectedCategoriesControlPlacement());
			} else if (categoriesControl instanceof ClassicTabbedPane) {
				((ClassicTabbedPane) categoriesControl).setForeground(getCategoriesCellForegroundColor());
				((ClassicTabbedPane) categoriesControl).setBackground(getCategoriesCellBackgroundColor());
				((ClassicTabbedPane) categoriesControl)
						.setTabBackgroundPainted(getCategoriesCellBackgroundColor() != null);
				((ClassicTabbedPane) categoriesControl).updateUI();
				((ClassicTabbedPane) categoriesControl).setTabPlacement(getExpectedCategoriesControlPlacement());
			} else if (categoriesControl instanceof ControlTabbedPane) {
				((ControlTabbedPane) categoriesControl).setForeground(getCategoriesCellForegroundColor());
				((ControlTabbedPane) categoriesControl).setBackground(getCategoriesCellBackgroundColor());
				((ControlTabbedPane) categoriesControl).setTabPlacement(getExpectedCategoriesControlPlacement());
			} else {
				throw new ReflectionUIError();
			}
		}
	}

	protected Color getMainForeroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		if (reflectionUI.getApplicationInfo().getMainForegroundColor() != null) {
			return SwingRendererUtils.getColor(reflectionUI.getApplicationInfo().getMainForegroundColor());
		} else {
			return new JPanel().getForeground();
		}
	}

	protected Color getMainBorderColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		if (reflectionUI.getApplicationInfo().getMainBorderColor() != null) {
			return SwingRendererUtils.getColor(reflectionUI.getApplicationInfo().getMainBorderColor());
		} else {
			return null;
		}
	}

	public Font getLabelCustomFont() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		if (reflectionUI.getApplicationInfo().getLabelCustomFontResourcePath() != null) {
			return SwingRendererUtils.loadFontThroughCache(
					reflectionUI.getApplicationInfo().getLabelCustomFontResourcePath(),
					ReflectionUIUtils.getErrorLogListener(reflectionUI), swingRenderer);
		} else {
			return null;
		}
	}

	public Color getControlsForegroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getFormForegroundColor() == null) {
			return getMainForeroundColor();
		} else {
			return SwingRendererUtils.getColor(type.getFormForegroundColor());
		}
	}

	public Color getControlsBackgroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getFormBackgroundColor() == null) {
			/*
			 * Note that we do not return the main background color because we want the form
			 * to be transparent so that a window background image would be visible through
			 * the controls.
			 */
			return null;
		} else {
			return SwingRendererUtils.getColor(type.getFormBackgroundColor());
		}
	}

	public Color getControlsBorderColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getFormBorderColor() == null) {
			return getMainBorderColor();
		} else {
			return SwingRendererUtils.getColor(type.getFormBorderColor());
		}
	}

	public Image getControlsBackgroundImage() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getFormBackgroundImagePath() == null) {
			return null;
		} else {
			return SwingRendererUtils.loadImageThroughCache(type.getFormBackgroundImagePath(),
					ReflectionUIUtils.getErrorLogListener(reflectionUI), swingRenderer);
		}
	}

	protected Color getCategoriesCellBackgroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getCategoriesBackgroundColor() != null) {
			return SwingRendererUtils.getColor(type.getCategoriesBackgroundColor());
		} else {
			return getControlsBackgroundColor();
		}
	}

	protected Color getCategoriesCellForegroundColor() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.getCategoriesForegroundColor() != null) {
			return SwingRendererUtils.getColor(type.getCategoriesForegroundColor());
		} else {
			return getControlsForegroundColor();
		}
	}

	/**
	 * @return the position of the currently selected category (-1 if there is no
	 *         selected category) among all displayed categories
	 *         ({@link #getDisplayedCategories()}).
	 */
	public int getSelectedCategoryIndex() {
		if (categoriesControl != null) {
			if (categoriesControl instanceof ListTabbedPane) {
				return ((ListTabbedPane) categoriesControl).getSelectedIndex();
			} else if (categoriesControl instanceof ClassicTabbedPane) {
				return ((ClassicTabbedPane) categoriesControl).getSelectedIndex();
			} else if (categoriesControl instanceof ControlTabbedPane) {
				return ((ControlTabbedPane) categoriesControl).getSelectedIndex();
			} else {
				throw new ReflectionUIError();
			}
		}
		return -1;
	}

	/**
	 * Changes the currently selected category. If the specified category position
	 * is -1 then the category selection is cleared.
	 * 
	 * @param index The position among all displayed categories
	 *              ({@link #getDisplayedCategories()}) of the category that must be
	 *              selected (-1 if the category selection must be cleared).
	 */
	public void setSelectedCategoryIndex(int index) {
		if (categoriesControl != null) {
			if (categoriesControl instanceof ListTabbedPane) {
				((ListTabbedPane) categoriesControl).setSelectedIndex(index);
			} else if (categoriesControl instanceof ClassicTabbedPane) {
				((ClassicTabbedPane) categoriesControl).setSelectedIndex(index);
			} else if (categoriesControl instanceof ControlTabbedPane) {
				((ControlTabbedPane) categoriesControl).setSelectedIndex(index);
			} else {
				throw new ReflectionUIError();
			}
		}
	}

	/**
	 * @return all displayed categories.
	 */
	public List<InfoCategory> getDisplayedCategories() {
		if (categoriesVisibilityUpdater == null) {
			return Collections.emptyList();
		} else {
			return categoriesVisibilityUpdater.getPresentKeys();
		}
	}

	/**
	 * @param category A category among the displayed categories
	 *                 ({@link #getDisplayedCategories()}).
	 * @return the component that contains the controls of the specified category.
	 *         If there is not any displayed category then null is returned.
	 */
	public Component getCategoryComponent(InfoCategory category) {
		if (categoriesControl != null) {
			int categoryIndex = getDisplayedCategories().indexOf(category);
			if (categoryIndex != -1) {
				if (categoriesControl instanceof ListTabbedPane) {
					return ((ListTabbedPane) categoriesControl).getComponentAt(categoryIndex);
				} else if (categoriesControl instanceof ClassicTabbedPane) {
					return ((ClassicTabbedPane) categoriesControl).getComponentAt(categoryIndex);
				} else if (categoriesControl instanceof ControlTabbedPane) {
					return ((ControlTabbedPane) categoriesControl).getComponentAt(categoryIndex);
				} else {
					throw new ReflectionUIError();
				}
			}
		}
		return null;
	}

	/**
	 * @return the control used to categorize the form controls.
	 */
	public Container getCategoriesControl() {
		return categoriesControl;
	}

	/**
	 * @return the scroll pane that contains the form controls, or null if the
	 *         form's contents are not scrollable.
	 */
	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	protected SortedMap<InfoCategory, List<IMethodInfo>> getMethodsByCategory(List<IMethodInfo> methods) {
		return new TreeMap<InfoCategory, List<IMethodInfo>>(methods.stream()
				.collect(Collectors.groupingBy(method -> (method.getCategory() != null) ? method.getCategory()
						: swingRenderer.getNullInfoCategory())));
	}

	protected SortedMap<InfoCategory, List<IFieldInfo>> getFieldsByCategory(List<IFieldInfo> fields) {
		return new TreeMap<InfoCategory, List<IFieldInfo>>(fields.stream().collect(Collectors.groupingBy(
				field -> (field.getCategory() != null) ? field.getCategory() : swingRenderer.getNullInfoCategory())));
	}

	protected ITypeInfo buildFormFilteredType() {
		IInfoFilter infoFilter = this.infoFilter;
		if (infoFilter == null) {
			infoFilter = IInfoFilter.DEFAULT;
		}
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		final ITypeInfo rawType = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		ITypeInfo result = new FilteredTypeFactory(infoFilter) {

			List<IFieldInfo> fields;
			List<IMethodInfo> methods;
			MenuModel menuModel;
			{
				try {
					fields = super.getFields(rawType);
					methods = super.getMethods(rawType);
					menuModel = super.getMenuModel(rawType);
				} catch (final Throwable t) {
					swingRenderer.getReflectionUI().logError(t);
					fields = Collections.<IFieldInfo>singletonList(new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {
						@Override
						public Object getValue(Object object) {
							throw new ReflectionUIError(t);
						}

						@Override
						public String getCaption() {
							return "Error";
						}
					});
					methods = Collections.emptyList();
					menuModel = new MenuModel();
				}
			}

			@Override
			protected List<IFieldInfo> getFields(ITypeInfo type) {
				return fields;
			}

			@Override
			protected List<IMethodInfo> getMethods(ITypeInfo type) {
				return methods;
			}

			@Override
			protected MenuModel getMenuModel(ITypeInfo type) {
				return menuModel;
			}

		}.wrapTypeInfo(rawType);
		return result;
	}

	/**
	 * @param fieldName The name of the field to search for.
	 * @return the first visible control place holder associated with a field that
	 *         has the specified name.
	 */
	public FieldControlPlaceHolder getFieldControlPlaceHolder(String fieldName) {
		for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
			for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHoldersByCategory.get(category)) {
				if (!fieldControlPlaceHolder.isVisible()) {
					continue;
				}
				if (fieldName.equals(fieldControlPlaceHolder.getField().getName())) {
					return fieldControlPlaceHolder;
				}
			}
		}
		return null;
	}

	/**
	 * @param methodSignature The signature of the method to search for.
	 * @return the first visible control place holder associated with a method that
	 *         has the specified signature.
	 */
	public MethodControlPlaceHolder getMethodControlPlaceHolder(String methodSignature) {
		for (InfoCategory category : methodControlPlaceHoldersByCategory.keySet()) {
			for (MethodControlPlaceHolder methodControlPlaceHolder : methodControlPlaceHoldersByCategory
					.get(category)) {
				if (!methodControlPlaceHolder.isVisible()) {
					continue;
				}
				if (methodControlPlaceHolder.getMethod().getSignature().equals(methodSignature)) {
					return methodControlPlaceHolder;
				}
			}
		}
		return null;
	}

	protected void layoutMembersPanels(Container container, Container fieldsPanel, Container methodsPanel) {
		container.setLayout(new BorderLayout());
		if (fieldsPanel != null) {
			container.add(fieldsPanel, BorderLayout.CENTER);
		}
		if (methodsPanel != null) {
			container.add(methodsPanel, BorderLayout.SOUTH);
		}
	}

	protected void layoutMembersControlPlaceHolders(List<FieldControlPlaceHolder> fielControlPlaceHolders,
			final List<MethodControlPlaceHolder> methodControlPlaceHolders, JPanel membersPanel) {
		Container fieldsPanel = (fielControlPlaceHolders.size() == 0) ? null
				: createFieldsPanel(fielControlPlaceHolders);
		Container methodsPanel = (methodControlPlaceHolders.size() == 0) ? null
				: createMethodsPanel(methodControlPlaceHolders);
		layoutMembersPanels(membersPanel, fieldsPanel, methodsPanel);
	}

	/**
	 * Updates the state of the current form controls.
	 * 
	 * @param refreshStructure Whether the current form should update its structure
	 *                         to reflect the recent meta-data changes. Mainly used
	 *                         in design mode.
	 * 
	 */
	public void refresh(boolean refreshStructure) {
		ensureNoCurrentValidationTask();
		if (refreshStructure && detectStructureChange()) {
			InfoCategory initiallySelectedCategory = null;
			{
				if (getSelectedCategoryIndex() != -1) {
					initiallySelectedCategory = getDisplayedCategories().get(getSelectedCategoryIndex());
				}
			}
			try {
				removeAll();
				createMembersControlPlaceHolders();
				layoutMembersControlPlaceHolders(fieldControlPlaceHoldersByCategory,
						methodControlPlaceHoldersByCategory, this);
				realizeMembersControls();
				if (categoriesVisibilityUpdater != null) {
					categoriesVisibilityUpdater.update();
				}
				SwingRendererUtils.handleComponentSizeChange(this);
			} finally {
				if (initiallySelectedCategory != null) {
					int newIndex = getDisplayedCategories().indexOf(initiallySelectedCategory);
					if (newIndex != -1) {
						setSelectedCategoryIndex(newIndex);
					}
				}
			}
		} else {
			for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
				List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory
						.get(category);
				for (int i = 0; i < fieldControlPlaceHolders.size(); i++) {
					FieldControlPlaceHolder fieldControlPlaceHolder = fieldControlPlaceHolders.get(i);
					fieldControlPlaceHolder.refreshUI(refreshStructure);
					if (refreshStructure) {
						updateFieldControlLayoutInContainer(fieldControlPlaceHolder);
					}
				}
			}
			for (InfoCategory category : methodControlPlaceHoldersByCategory.keySet()) {
				List<MethodControlPlaceHolder> methodControlPlaceHolders = methodControlPlaceHoldersByCategory
						.get(category);
				for (int i = 0; i < methodControlPlaceHolders.size(); i++) {
					MethodControlPlaceHolder methodControlPlaceHolder = methodControlPlaceHolders.get(i);
					methodControlPlaceHolder.refreshUI(refreshStructure);
					if (refreshStructure) {
						updateMethodControlLayoutInContainer(methodControlPlaceHolder);
					}
				}
			}
			if (categoriesVisibilityUpdater != null) {
				categoriesVisibilityUpdater.update();
			}
		}
		if (refreshStructure) {
			updateFieldsPanelsLayout();
			refreshCategoriesControlStructure();
			setPreservingRatio(true);
			setFillingAreaWhenPreservingRatio(true);
			Color backgroundColor = getControlsBackgroundColor();
			Color foregroundColor = getControlsForegroundColor();
			Image image = getControlsBackgroundImage();
			setBackground(backgroundColor);
			setImage(image);
			setOpaque((backgroundColor != null) && (image == null));
			Color borderColor = getControlsBorderColor();
			{
				if (scrollPane != null) {
					if (borderColor != null) {
						scrollPane.setBorder(BorderFactory.createLineBorder(borderColor));
					} else {
						scrollPane.setBorder(new JScrollPane().getBorder());
					}
				}
				menuBar.setBackground(backgroundColor);
				menuBar.setOpaque(backgroundColor != null);
				menuBar.setForeground(foregroundColor);
				if (borderColor != null) {
					Border outsideBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor);
					Border insideBorder = BorderFactory.createEmptyBorder(getLayoutSpacing(), getLayoutSpacing(),
							getLayoutSpacing(), getLayoutSpacing());
					menuBar.setBorder(BorderFactory.createCompoundBorder(outsideBorder, insideBorder));
				} else {
					menuBar.setBorder(new JMenuBar().getBorder());
				}
				menuBar.removeAll();
				menuBarUpdater = null;
			}
			{
				int borderSpacing = getLayoutSpacing();
				Border insideBorder = BorderFactory.createEmptyBorder(borderSpacing, borderSpacing, borderSpacing,
						borderSpacing);
				Border outsideBorder = createStatusBar().getBorder();
				statusBar.setBorder(BorderFactory.createCompoundBorder(outsideBorder, insideBorder));
				Font labelCustomFont = getLabelCustomFont();
				{
					if (labelCustomFont != null) {
						statusBar.setFont(labelCustomFont.deriveFont(statusBar.getFont().getStyle(),
								statusBar.getFont().getSize()));
					} else {
						statusBar.setFont(createStatusBar().getFont());
					}
				}
			}
		}
		refreshValidityComponents();
		finalizeFormUpdate();
		for (IFormListener l : listeners) {
			l.onRefresh(refreshStructure);
		}
		filteredObjectType.onFormRefresh(object);
	}

	/**
	 * If a validation job is running, it will be interrupted after the execution of
	 * this method.
	 */
	public void ensureNoCurrentValidationTask() {
		if ((currentValidationTask != null) && !currentValidationTask.isDone()) {
			try {
				currentValidationTask.cancelRepeatedlyAndWait(100);
			} catch (InterruptedException e) {
				throw new ReflectionUIError(e);
			}
		}
	}

	protected void createMembersControlPlaceHolders() {
		fieldControlPlaceHoldersByCategory = new TreeMap<InfoCategory, List<FieldControlPlaceHolder>>(
				getFieldsByCategory(filteredObjectType.getFields()).entrySet().stream()
						.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
								.map(this::createFieldControlPlaceHolder).collect(Collectors.toList()))));
		methodControlPlaceHoldersByCategory = new TreeMap<InfoCategory, List<MethodControlPlaceHolder>>(
				getMethodsByCategory(filteredObjectType.getMethods()).entrySet().stream()
						.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
								.map(this::createMethodControlPlaceHolder).collect(Collectors.toList()))));
	}

	protected void realizeMembersControls() {
		for (List<FieldControlPlaceHolder> fieldControlPlaceHolders : fieldControlPlaceHoldersByCategory.values()) {
			for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHolders) {
				fieldControlPlaceHolder.initializeUI();
			}
		}
		for (List<MethodControlPlaceHolder> methodControlPlaceHolders : methodControlPlaceHoldersByCategory.values()) {
			for (MethodControlPlaceHolder methodControlPlaceHolder : methodControlPlaceHolders) {
				methodControlPlaceHolder.initializeUI();
			}
		}
	}

	protected boolean detectStructureChange() {

		SortedMap<InfoCategory, List<IFieldInfo>> oldFieldsByCategory = (fieldControlPlaceHoldersByCategory != null)
				? new TreeMap<InfoCategory, List<IFieldInfo>>(fieldControlPlaceHoldersByCategory.entrySet().stream()
						.collect(Collectors.toMap(Map.Entry::getKey,
								entry -> entry.getValue().stream().map(FieldControlPlaceHolder::getField)
										.collect(Collectors.toList()))))
				: null;
		SortedMap<InfoCategory, List<IMethodInfo>> oldMethodsByCategory = (methodControlPlaceHoldersByCategory != null)
				? new TreeMap<InfoCategory, List<IMethodInfo>>(methodControlPlaceHoldersByCategory.entrySet().stream()
						.collect(Collectors.toMap(Map.Entry::getKey,
								entry -> entry.getValue().stream().map(MethodControlPlaceHolder::getMethod)
										.collect(Collectors.toList()))))
				: null;

		boolean modificationsDetected = false;

		ITypeInfo oldObjectType = filteredObjectType;
		filteredObjectType = buildFormFilteredType();

		if (!filteredObjectType.equals(oldObjectType)) {
			modificationsDetected = true;
			setName("form [objectType=" + filteredObjectType.getName() + "]");
			modificationStack.setName(getName());
		}

		SortedMap<InfoCategory, List<IFieldInfo>> newFieldsByCategory = getFieldsByCategory(
				filteredObjectType.getFields());
		SortedMap<InfoCategory, List<IMethodInfo>> newMethodsByCategory = getMethodsByCategory(
				filteredObjectType.getMethods());

		if (!modificationsDetected) {
			if ((oldFieldsByCategory == null) || !new ArrayList<InfoCategory>(newFieldsByCategory.keySet())
					.equals(new ArrayList<InfoCategory>(oldFieldsByCategory.keySet()))) {
				modificationsDetected = true;
			}
		}

		if (!modificationsDetected) {
			if ((oldMethodsByCategory == null) || !new ArrayList<InfoCategory>(newMethodsByCategory.keySet())
					.equals(new ArrayList<InfoCategory>(oldMethodsByCategory.keySet()))) {
				modificationsDetected = true;
			}
		}

		if (!modificationsDetected) {
			for (InfoCategory category : newFieldsByCategory.keySet()) {
				List<IFieldInfo> newFields = newFieldsByCategory.get(category);
				List<IFieldInfo> oldFields = oldFieldsByCategory.get(category);
				if (oldFields.size() != newFields.size()) {
					modificationsDetected = true;
					break;
				}
				for (int i = 0; i < newFields.size(); i++) {
					if (!newFields.get(i).equals(oldFields.get(i))) {
						modificationsDetected = true;
						break;
					}
				}
				if (modificationsDetected) {
					break;
				}
			}
		}

		if (!modificationsDetected) {
			for (InfoCategory category : newMethodsByCategory.keySet()) {
				List<IMethodInfo> newMethods = newMethodsByCategory.get(category);
				List<IMethodInfo> oldMethods = oldMethodsByCategory.get(category);
				if (oldMethods.size() != newMethods.size()) {
					modificationsDetected = true;
					break;
				}
				for (int i = 0; i < newMethods.size(); i++) {
					if (!newMethods.get(i).equals(oldMethods.get(i))) {
						modificationsDetected = true;
						break;
					}
				}
				if (modificationsDetected) {
					break;
				}
			}
		}

		if (!modificationsDetected) {
			List<InfoCategory> allCategories = collectCategories(newFieldsByCategory, newMethodsByCategory);
			if ((allCategories.size() == 1) && (swingRenderer.getNullInfoCategory().equals(allCategories.get(0)))) {
				if (categoriesControl != null) {
					modificationsDetected = true;
				}
			} else if (allCategories.size() > 0) {
				if (categoriesControl == null) {
					modificationsDetected = true;
				}
			}
			if (categoriesControl != null) {
				Container newCategoriesControl = createCategoriesControl();
				if (newCategoriesControl.getClass() != categoriesControl.getClass()) {
					modificationsDetected = true;
				} else {
					if (newCategoriesControl instanceof ListTabbedPane) {
						if (((ListTabbedPane) categoriesControl)
								.getTabPlacement() != getExpectedCategoriesControlPlacement()) {
							modificationsDetected = true;
						}
					} else if (newCategoriesControl instanceof ClassicTabbedPane) {
						if (((ClassicTabbedPane) categoriesControl)
								.getTabPlacement() != getExpectedCategoriesControlPlacement()) {
							modificationsDetected = true;
						}
					} else if (newCategoriesControl instanceof ControlTabbedPane) {
						if (((ControlTabbedPane) categoriesControl)
								.getTabPlacement() != getExpectedCategoriesControlPlacement()) {
							modificationsDetected = true;
						}
					} else {
						throw new ReflectionUIError();
					}
				}
			}
		}

		if (!modificationsDetected) {
			if (((filteredObjectType.getFormPreferredWidth() != -1)
					|| (filteredObjectType.getFormPreferredHeight() != -1)) != (scrollPane != null)) {
				modificationsDetected = true;
			}
		}

		return modificationsDetected;
	}

	protected void finalizeFormUpdate() {
		updateMenuBar();
		if ((statusBar.getParent() != null) && statusBar.getParent().isDisplayable()) {
			validateFormInBackgroundAndReportOnStatusBar();
		}
	}

	/**
	 * Updates this form menu bar. If the menu bar does not have a parent component
	 * (probably because the current form is not a root form) then nothing is done.
	 */
	public void updateMenuBar() {
		if (menuBar.getParent() == null) {
			return;
		}
		if (menuBarUpdater == null) {
			menuBarUpdater = new MinimalListUpdater<MenuInfo>() {

				@Override
				protected List<MenuInfo> collectKeys() {
					MenuModel globalMenuModel = new MenuModel();
					addMenuContributionTo(globalMenuModel);
					return globalMenuModel.getMenus();
				}

				@Override
				protected boolean shouldBePresent(MenuInfo menuInfo) {
					return true;
				}

				@Override
				protected void add(int index, MenuInfo menuInfo) {
					menuBar.add(creatMenu(menuInfo), index);
				}

				@Override
				protected void remove(int index) {
					menuBar.remove(index);
				}

				@Override
				protected void refresh(MenuInfo current, int index) {
					((Menu) menuBar.getMenu(index)).refresh();
				}

			};
		}
		menuBarUpdater.update();
		menuBar.setVisible(menuBar.getComponentCount() > 0);
		SwingRendererUtils.handleComponentSizeChange(menuBar);
	}

	protected Menu creatMenu(MenuInfo menuInfo) {
		return new Menu(swingRenderer, this, menuInfo);
	}

	/**
	 * Import this form menu contributions into the specified menu model.
	 * 
	 * @param menuModel The menu model that will receive this form menu
	 *                  contributions.
	 */
	public void addMenuContributionTo(MenuModel menuModel) {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		MenuModel formMenuModel = type.getMenuModel();
		menuModel.importContributions(formMenuModel, new Filter<IMenuElementInfo>() {
			@Override
			public IMenuElementInfo get(IMenuElementInfo e) {
				if (e instanceof AbstractActionMenuItemInfo) {
					AbstractActionMenuItemInfo action = (AbstractActionMenuItemInfo) e;
					Map<String, Object> specificProperties = new HashMap<String, Object>(
							action.getSpecificProperties());
					specificProperties.put(ACTION_MENU_ITEM_CONTEXT_FORM, Form.this);
					action.setSpecificProperties(specificProperties);
				}
				return e;
			}
		});
		for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
			for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHoldersByCategory.get(category)) {
				if (!fieldControlPlaceHolder.isVisible()) {
					continue;
				}
				Component fieldControl = fieldControlPlaceHolder.getFieldControl();
				if (fieldControl instanceof IAdvancedFieldControl) {
					((IAdvancedFieldControl) fieldControl).addMenuContributions(menuModel);
				}
			}
		}
	}

	/**
	 * Requests that this form (actually the 1st focusable control of this form) get
	 * the input focus.
	 * 
	 * @return false if the focus change request is guaranteed to fail; true if it
	 *         is likely to succeed.
	 */
	public boolean requestFormFocus() {
		for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
			for (FieldControlPlaceHolder fieldControlPlaceHolder : fieldControlPlaceHoldersByCategory.get(category)) {
				if (!fieldControlPlaceHolder.isVisible()) {
					continue;
				}
				if (SwingRendererUtils.requestAnyComponentFocus(fieldControlPlaceHolder.getFieldControl(),
						swingRenderer)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param field The field for which the control place holder must be created.
	 * @return a new control place holder for the given field.
	 */
	protected FieldControlPlaceHolder createFieldControlPlaceHolder(IFieldInfo field) {
		return new FieldControlPlaceHolder(this, field);
	}

	/**
	 * @param method The method for which the control place holder must be created.
	 * @return a new control place holder for the given method.
	 */
	protected MethodControlPlaceHolder createMethodControlPlaceHolder(IMethodInfo method) {
		return new MethodControlPlaceHolder(this, method);
	}

	protected Container createFieldsPanel(List<FieldControlPlaceHolder> fielControlPlaceHolders) {
		JPanel fieldsPanel = new ControlPanel();
		fieldsPanel.setLayout(getFieldsPanelLayout());
		fieldsPanel.setBorder(getFieldsPanelBorder());
		for (int i = 0; i < fielControlPlaceHolders.size(); i++) {
			FieldControlPlaceHolder fieldControlPlaceHolder = fielControlPlaceHolders.get(i);
			{
				fieldsPanel.add(fieldControlPlaceHolder);
				fieldControlPlaceHolder.setLayoutInContainerUpdateNeeded(true);
				fieldControlPlaceHolder.setPositionInContainer(i);
				updateFieldControlLayoutInContainer(fieldControlPlaceHolder);
			}
		}
		fieldsPanel.setName("fieldsPanel [parent=" + this.getName() + "]");
		return fieldsPanel;
	}

	protected Container createMethodsPanel(final List<MethodControlPlaceHolder> methodControlPlaceHolders) {
		JPanel methodsPanel = new ControlPanel();
		methodsPanel.setLayout(getMethodsPanelLayout());
		methodsPanel.setBorder(getMethodsPanelBorder());
		for (MethodControlPlaceHolder methodControlPlaceHolder : methodControlPlaceHolders) {
			methodsPanel.add(methodControlPlaceHolder);
			updateMethodControlLayoutInContainer(methodControlPlaceHolder);
		}
		methodsPanel.setName("methodsPanel [parent=" + this.getName() + "]");
		return SwingRendererUtils.flowInLayout(methodsPanel, GridBagConstraints.CENTER);
	}

	protected LayoutManager getFieldsPanelLayout() {
		return new GridBagLayout();
	}

	protected LayoutManager getMethodsPanelLayout() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		MethodsLayout methodsOrientation = type.getMethodsLayout();
		int spacing = getLayoutSpacing();
		GridLayout newLayout;
		if (methodsOrientation == MethodsLayout.HORIZONTAL_FLOW) {
			newLayout = new BetterGridLayout(1, 0, spacing, spacing);
		} else if (methodsOrientation == MethodsLayout.VERTICAL_FLOW) {
			newLayout = new BetterGridLayout(0, 1, spacing, spacing);
		} else {
			throw new ReflectionUIError();
		}
		return newLayout;
	}

	protected Border getFieldsPanelBorder() {
		return null;
	}

	protected Border getMethodsPanelBorder() {
		int spacing = getLayoutSpacing();
		return new EmptyBorder(spacing, spacing, spacing, spacing);
	}

	protected void updateFieldsPanelsLayout() {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		ITypeInfo.FieldsLayout fieldsOrientation = type.getFieldsLayout();
		for (InfoCategory category : fieldControlPlaceHoldersByCategory.keySet()) {
			List<FieldControlPlaceHolder> fieldControlPlaceHolders = fieldControlPlaceHoldersByCategory.get(category);
			JPanel fieldsPanel = (JPanel) fieldControlPlaceHolders.stream().filter(Component::isVisible)
					.map(Component::getParent).findFirst().orElse(null);
			if (fieldsPanel == null) {
				continue;
			}
			boolean fieldsPanelFilled = false;
			for (int i = 0; i < fieldControlPlaceHolders.size(); i++) {
				FieldControlPlaceHolder fieldControlPlaceHolder = fieldControlPlaceHolders.get(i);
				if (!fieldControlPlaceHolder.isVisible()) {
					continue;
				}
				GridBagConstraints fieldControlPlaceHolderLayoutConstraints = ((GridBagLayout) fieldsPanel.getLayout())
						.getConstraints(fieldControlPlaceHolder);
				if (fieldsOrientation == ITypeInfo.FieldsLayout.VERTICAL_FLOW) {
					if (fieldControlPlaceHolderLayoutConstraints.weighty > 0) {
						fieldsPanelFilled = true;
						break;
					}
				} else if (fieldsOrientation == ITypeInfo.FieldsLayout.HORIZONTAL_FLOW) {
					if (fieldControlPlaceHolderLayoutConstraints.weightx > 0) {
						fieldsPanelFilled = true;
						break;
					}
				} else {
					throw new ReflectionUIError();
				}
			}
			Container membersPanel = fieldsPanel.getParent();
			((BorderLayout) membersPanel.getLayout()).removeLayoutComponent(fieldsPanel);
			((BorderLayout) membersPanel.getLayout()).addLayoutComponent(fieldsPanel,
					fieldsPanelFilled ? BorderLayout.CENTER : BorderLayout.NORTH);
		}
	}

	protected void updateFieldControlLayoutInContainer(FieldControlPlaceHolder fieldControlPlaceHolder) {
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		IFieldInfo field = fieldControlPlaceHolder.getField();
		ITypeInfo.FieldsLayout fieldsOrientation = type.getFieldsLayout();
		JPanel fieldsPanel = (JPanel) fieldControlPlaceHolder.getParent();
		int spacing = getLayoutSpacing();

		Component captionControl = fieldControlPlaceHolder.getSiblingCaptionControl();
		if (captionControl != null) {
			fieldsPanel.remove(captionControl);
			fieldControlPlaceHolder.setSiblingCaptionControl(null);
		}
		boolean shouldHaveSeparateCaptionControl = !fieldControlPlaceHolder.showsCaption()
				&& (fieldControlPlaceHolder.getField().getCaption().length() > 0);
		if (shouldHaveSeparateCaptionControl) {
			captionControl = createSeparateFieldCaptionControl(fieldControlPlaceHolder);
			captionControl.setVisible(fieldControlPlaceHolder.isVisible());
			GridBagConstraints captionControlLayoutConstraints = new GridBagConstraints();
			captionControlLayoutConstraints.insets = new Insets(spacing, spacing, spacing, spacing);
			if (fieldsOrientation == ITypeInfo.FieldsLayout.VERTICAL_FLOW) {
				captionControlLayoutConstraints.gridx = 0;
				captionControlLayoutConstraints.gridy = fieldControlPlaceHolder.getPositionInContainer();
			} else if (fieldsOrientation == ITypeInfo.FieldsLayout.HORIZONTAL_FLOW) {
				captionControlLayoutConstraints.gridy = 0;
				captionControlLayoutConstraints.gridx = fieldControlPlaceHolder.getPositionInContainer();
			} else {
				throw new ReflectionUIError();
			}
			captionControlLayoutConstraints.anchor = GridBagConstraints.WEST;
			fieldsPanel.add(captionControl, captionControlLayoutConstraints);
			fieldControlPlaceHolder.setSiblingCaptionControl(captionControl);
		}

		GridBagConstraints fieldControlPlaceHolderLayoutConstraints = new GridBagConstraints();
		fieldControlPlaceHolderLayoutConstraints.insets = new Insets(spacing, spacing, spacing, spacing);
		if (fieldsOrientation == ITypeInfo.FieldsLayout.VERTICAL_FLOW) {
			if (!shouldHaveSeparateCaptionControl) {
				fieldControlPlaceHolderLayoutConstraints.gridwidth = 2;
				fieldControlPlaceHolderLayoutConstraints.gridx = 0;
			} else {
				fieldControlPlaceHolderLayoutConstraints.gridx = 1;
			}
			fieldControlPlaceHolderLayoutConstraints.gridy = fieldControlPlaceHolder.getPositionInContainer();
		} else if (fieldsOrientation == ITypeInfo.FieldsLayout.HORIZONTAL_FLOW) {
			if (!shouldHaveSeparateCaptionControl) {
				fieldControlPlaceHolderLayoutConstraints.gridheight = 2;
				fieldControlPlaceHolderLayoutConstraints.gridy = 0;
			} else {
				fieldControlPlaceHolderLayoutConstraints.gridy = 1;
			}
			fieldControlPlaceHolderLayoutConstraints.gridx = fieldControlPlaceHolder.getPositionInContainer();
		} else {
			throw new ReflectionUIError();
		}
		fieldControlPlaceHolderLayoutConstraints.weightx = field.getDisplayAreaHorizontalWeight();
		fieldControlPlaceHolderLayoutConstraints.weighty = field.getDisplayAreaVerticalWeight();
		if (field.isDisplayAreaHorizontallyFilled() && field.isDisplayAreaVerticallyFilled()) {
			fieldControlPlaceHolderLayoutConstraints.fill = GridBagConstraints.BOTH;
		} else if (field.isDisplayAreaHorizontallyFilled()) {
			fieldControlPlaceHolderLayoutConstraints.fill = GridBagConstraints.HORIZONTAL;
		} else if (field.isDisplayAreaVerticallyFilled()) {
			fieldControlPlaceHolderLayoutConstraints.fill = GridBagConstraints.VERTICAL;
		} else {
			fieldControlPlaceHolderLayoutConstraints.fill = GridBagConstraints.NONE;
		}
		for (Form subForm : SwingRendererUtils.findDescendantForms(fieldControlPlaceHolder, swingRenderer)) {
			subForm.visibilityEventsDisabled = true;
		}
		try {
			((GridBagLayout) fieldsPanel.getLayout()).setConstraints(fieldControlPlaceHolder,
					fieldControlPlaceHolderLayoutConstraints);
		} finally {
			for (Form subForm : SwingRendererUtils.findDescendantForms(fieldControlPlaceHolder, swingRenderer)) {
				subForm.visibilityEventsDisabled = false;
			}
		}

		Component onlineHelpControl = fieldControlPlaceHolder.getSiblingOnlineHelpControl();
		if (onlineHelpControl != null) {
			fieldsPanel.remove(onlineHelpControl);
			fieldControlPlaceHolder.setSiblingOnlineHelpControl(null);
		}
		onlineHelpControl = createFieldOnlineHelpControl(fieldControlPlaceHolder);
		if (onlineHelpControl != null) {
			onlineHelpControl.setVisible(fieldControlPlaceHolder.isVisible());
			fieldControlPlaceHolder.setSiblingOnlineHelpControl(onlineHelpControl);
			GridBagConstraints layoutConstraints = new GridBagConstraints();
			layoutConstraints.insets = new Insets(spacing, spacing, spacing, spacing);
			if (fieldsOrientation == ITypeInfo.FieldsLayout.VERTICAL_FLOW) {
				layoutConstraints.gridx = 2;
				layoutConstraints.gridy = fieldControlPlaceHolder.getPositionInContainer();
			} else if (fieldsOrientation == ITypeInfo.FieldsLayout.HORIZONTAL_FLOW) {
				layoutConstraints.gridy = 2;
				layoutConstraints.gridx = fieldControlPlaceHolder.getPositionInContainer();
			} else {
				throw new ReflectionUIError();
			}
			fieldsPanel.add(onlineHelpControl, layoutConstraints);
		}

		SwingRendererUtils.handleComponentSizeChange(fieldsPanel);

		fieldControlPlaceHolder.setLayoutInContainerUpdateNeeded(false);

	}

	protected void updateMethodControlLayoutInContainer(MethodControlPlaceHolder methodControlPlaceHolder) {
		boolean layoutUpdated = false;
		JPanel methodsPanel = (JPanel) methodControlPlaceHolder.getParent();
		GridLayout newLayout = (GridLayout) getMethodsPanelLayout();
		GridLayout oldLayout = (GridLayout) methodsPanel.getLayout();
		if (!((oldLayout.getRows() == newLayout.getRows()) && (oldLayout.getColumns() == newLayout.getColumns())
				&& (oldLayout.getHgap() == newLayout.getHgap()) && (oldLayout.getVgap() == newLayout.getVgap()))) {
			methodsPanel.setLayout(newLayout);
			layoutUpdated = true;
		}
		EmptyBorder newBorder = (EmptyBorder) getMethodsPanelBorder();
		EmptyBorder oldBorder = (EmptyBorder) methodsPanel.getBorder();
		if (!oldBorder.getBorderInsets().equals(newBorder.getBorderInsets())) {
			layoutUpdated = true;
			methodsPanel.setBorder(newBorder);
		}
		if (layoutUpdated) {
			SwingRendererUtils.handleComponentSizeChange(methodsPanel);
		}
	}

	protected Component createFieldOnlineHelpControl(FieldControlPlaceHolder fieldControlPlaceHolder) {
		final IFieldControlData data = fieldControlPlaceHolder.getControlData();
		final String onlineHelp = data.getOnlineHelp();
		if ((onlineHelp == null) || (onlineHelp.length() == 0)) {
			return null;
		}
		final String title = ReflectionUIUtils.composeMessage(data.getCaption(), "Help");
		final JButton result = new AbstractControlButton() {

			private static final long serialVersionUID = 1L;

			@Override
			public Image retrieveBackgroundImage() {
				if (data.getButtonBackgroundImagePath() == null) {
					return null;
				} else {
					return SwingRendererUtils.loadImageThroughCache(data.getButtonBackgroundImagePath(),
							ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()), swingRenderer);
				}
			}

			@Override
			public Font retrieveCustomFont() {
				if (data.getButtonCustomFontResourcePath() == null) {
					return null;
				} else {
					return SwingRendererUtils.loadFontThroughCache(data.getButtonCustomFontResourcePath(),
							ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()), swingRenderer);
				}
			}

			@Override
			public Color retrieveBackgroundColor() {
				if (data.getButtonBackgroundColor() == null) {
					return null;
				} else {
					return SwingRendererUtils.getColor(data.getButtonBackgroundColor());
				}
			}

			@Override
			public Color retrieveForegroundColor() {
				if (data.getButtonForegroundColor() == null) {
					return null;
				} else {
					return SwingRendererUtils.getColor(data.getButtonForegroundColor());
				}
			}

			@Override
			public Color retrieveBorderColor() {
				if (data.getButtonBorderColor() == null) {
					return null;
				} else {
					return SwingRendererUtils.getColor(data.getButtonBorderColor());
				}
			}

			@Override
			public String retrieveText() {
				return "";
			}

			@Override
			public String retrieveToolTipText() {
				return swingRenderer.prepareMessageToDisplay(onlineHelp);
			}

			@Override
			public Icon retrieveIcon() {
				return SwingRendererUtils.HELP_ICON;
			}

		};
		result.setFocusable(false);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				swingRenderer.openInformationDialog(result, onlineHelp, title);
			}
		});
		result.setName("fieldOnlineHelpControl [field=" + fieldControlPlaceHolder.getField().getName() + ", parent="
				+ this.getName() + "]");
		return result;
	}

	protected Component createButtonBarOnlineHelpControl() {
		final ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		final ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		final String onlineHelp = type.getOnlineHelp();
		if ((onlineHelp == null) || (onlineHelp.length() == 0)) {
			return null;
		}
		final String title = ReflectionUIUtils.composeMessage(swingRenderer.getObjectTitle(object), "Help");
		final JButton result = new AbstractControlButton() {

			private static final long serialVersionUID = 1L;

			@Override
			public Image retrieveBackgroundImage() {
				if (type.getFormButtonBackgroundImagePath() != null) {
					return SwingRendererUtils.loadImageThroughCache(type.getFormButtonBackgroundImagePath(),
							ReflectionUIUtils.getErrorLogListener(reflectionUI), swingRenderer);
				}
				if (reflectionUI.getApplicationInfo().getMainButtonBackgroundImagePath() != null) {
					return SwingRendererUtils.loadImageThroughCache(
							reflectionUI.getApplicationInfo().getMainButtonBackgroundImagePath(),
							ReflectionUIUtils.getErrorLogListener(reflectionUI), swingRenderer);
				}
				return null;
			}

			@Override
			public Font retrieveCustomFont() {
				if (reflectionUI.getApplicationInfo().getButtonCustomFontResourcePath() == null) {
					return null;
				} else {
					return SwingRendererUtils.loadFontThroughCache(
							reflectionUI.getApplicationInfo().getButtonCustomFontResourcePath(),
							ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()), swingRenderer);
				}
			}

			@Override
			public Color retrieveBackgroundColor() {
				if (type.getFormButtonBackgroundColor() != null) {
					return SwingRendererUtils.getColor(type.getFormButtonBackgroundColor());
				}
				if (reflectionUI.getApplicationInfo().getMainButtonBackgroundColor() != null) {
					return SwingRendererUtils
							.getColor(reflectionUI.getApplicationInfo().getMainButtonBackgroundColor());
				}
				return null;
			}

			@Override
			public Color retrieveForegroundColor() {
				if (type.getFormButtonForegroundColor() != null) {
					return SwingRendererUtils.getColor(type.getFormButtonForegroundColor());
				}
				if (reflectionUI.getApplicationInfo().getMainButtonForegroundColor() != null) {
					return SwingRendererUtils
							.getColor(reflectionUI.getApplicationInfo().getMainButtonForegroundColor());
				}
				return null;
			}

			@Override
			public Color retrieveBorderColor() {
				if (type.getFormButtonBorderColor() != null) {
					return SwingRendererUtils.getColor(type.getFormButtonBorderColor());
				}
				if (reflectionUI.getApplicationInfo().getMainButtonBorderColor() != null) {
					return SwingRendererUtils.getColor(reflectionUI.getApplicationInfo().getMainButtonBorderColor());
				}
				return null;
			}

			@Override
			public String retrieveText() {
				return "";
			}

			@Override
			public String retrieveToolTipText() {
				return swingRenderer.prepareMessageToDisplay(onlineHelp);
			}

			@Override
			public Icon retrieveIcon() {
				return SwingRendererUtils.HELP_ICON;
			}

		};
		result.setFocusable(false);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				swingRenderer.openInformationDialog(result, onlineHelp, title);
			}
		});
		result.setName("buttonBarOnlineHelpControl [parent=" + this.getName() + "]");
		return result;
	}

	protected Component createSeparateFieldCaptionControl(FieldControlPlaceHolder fieldControlPlaceHolder) {
		IFieldControlData data = fieldControlPlaceHolder.getControlData();
		JLabel result = new JLabel(swingRenderer.prepareMessageToDisplay(data.getCaption() + ": "));
		if (data.getLabelForegroundColor() != null) {
			result.setForeground(SwingRendererUtils.getColor(data.getLabelForegroundColor()));
		}
		if (data.getLabelCustomFontResourcePath() != null) {
			result.setFont(SwingRendererUtils
					.loadFontThroughCache(data.getLabelCustomFontResourcePath(),
							ReflectionUIUtils.getErrorLogListener(swingRenderer.getReflectionUI()), swingRenderer)
					.deriveFont(result.getFont().getStyle(), result.getFont().getSize()));
		}
		result.setName("captionControl [field=" + fieldControlPlaceHolder.getField().getName() + ", parent="
				+ this.getName() + "]");
		return result;
	}

	protected int getLayoutSpacing() {
		return filteredObjectType.getFormSpacing();
	}

	/**
	 * @return the buttons that should be laid on the containing window button bar
	 *         when this form is the root one.
	 */
	public List<Component> createButtonBarControls() {
		if (object == null) {
			return null;
		}
		List<Component> result = new ArrayList<Component>();
		Component onlineHelpControl = createButtonBarOnlineHelpControl();
		if (onlineHelpControl != null) {
			result.add(onlineHelpControl);
		}
		ReflectionUI reflectionUI = swingRenderer.getReflectionUI();
		ITypeInfo type = reflectionUI.getTypeInfo(reflectionUI.getTypeInfoSource(object));
		if (type.isModificationStackAccessible()) {
			result.addAll(new ModificationStackControls(this).create(swingRenderer));
		}
		return result;
	}

	/**
	 * @return the modifiable list of listeners that will get the form events
	 *         notifications.
	 */
	public List<IFormListener> getListeners() {
		return listeners;
	}

	@Override
	public String toString() {
		return "Form [id=" + hashCode() + ", object=" + object + "]";
	}

	/**
	 * Listener class allowing to get notifications on some form events.
	 * 
	 * @author olitank
	 *
	 */
	public interface IFormListener {

		/**
		 * Called when the source form is refreshed.
		 * 
		 * @param refreshStructure The value of the parameter that was passed to
		 *                         {@link Form#refresh(boolean)}.
		 */
		void onRefresh(boolean refreshStructure);

		/**
		 * Called after the source form is validated.
		 * 
		 * @param validationError The validation error or null if the form is valid.
		 */
		void afterValidation(Exception validationError);

	}

}
