package xy.reflect.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.customizer.CustomizationController;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.undo.ModificationStack;

public class SelectionTest {

	public static void main(String[] args) throws Exception {

		CustomizedUI reflectionUI = new CustomizedUI();
		File tmpCustomizationsFile = File.createTempFile(SelectionTest.class.getName(), ".icu");
		tmpCustomizationsFile.deleteOnExit();
		reflectionUI.getInfoCustomizations().saveToFile(tmpCustomizationsFile, null);
		final SwingCustomizer swingCustomizer = new SwingCustomizer(reflectionUI, tmpCustomizationsFile.getPath()) {

			@Override
			public boolean isCustomizationsEditorEnabled() {
				return true;
			}

			@Override
			public CustomizationController createCustomizationController() {
				return new CustomizationController(this) {

					ModificationStack modificationStack = new ModificationStack(null);

					@Override
					protected void openWindow() {
						refreshCustomizedControlsOnModification();
					}

					@Override
					protected void closeWindow() {
					}

					@Override
					public ModificationStack getModificationStack() {
						return modificationStack;
					}

				};
			}

		};
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				swingCustomizer.openObjectFrame(new SelectionTest());
			}
		});
	}

	private Node rootNode = new Node();

	public Node getRootNode() {
		return rootNode;
	}

	public void setRootNode(Node rootNode) {
		this.rootNode = rootNode;
	}

	public static class Node {
		private List<Integer> integerList = new ArrayList<Integer>();
		private Set<Integer> integerSet = new HashSet<Integer>();
		private Node[] childrenArray = new Node[0];

		public List<Integer> getIntegerList() {
			return integerList;
		}

		public void setIntegerList(List<Integer> integerList) {
			this.integerList = integerList;
		}

		public Set<Integer> getIntegerSet() {
			return integerSet;
		}

		public void setIntegerSet(Set<Integer> integerSet) {
			this.integerSet = integerSet;
		}

		public Node[] getChildrenArray() {
			return childrenArray;
		}

		public void setChildrenArray(Node[] childrenArray) {
			this.childrenArray = childrenArray;
		}

	}

}
