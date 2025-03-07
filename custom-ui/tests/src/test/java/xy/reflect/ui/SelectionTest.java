package xy.reflect.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.customizer.CustomizationController;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.MiscUtils;
import xy.reflect.ui.util.Wildcard;

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

	private SortedSet<Node> rootNodes = new TreeSet<SelectionTest.Node>();
	private Node selectedNode;
	private String nodeNameFilter;

	private static Predicate<Node> getNodeNameFilterFunction(String nodeNameFilter) {
		return new Predicate<SelectionTest.Node>() {
			@Override
			public boolean test(Node node) {
				return (nodeNameFilter == null) || (nodeNameFilter.length() == 0)
						|| Wildcard.match(node.getName(), "*" + nodeNameFilter + "*")
						|| (node.getChildren().size() > 0);
			}
		};
	}

	public SortedSet<Node> getRootNodes() {
		return new TreeSet<SelectionTest.Node>(MiscUtils.getFilteredSet(rootNodes, getNodeNameFilterFunction(nodeNameFilter)));
	}

	public void setRootNodes(SortedSet<Node> rootNodes) {
		this.rootNodes = new TreeSet<SelectionTest.Node>(
				MiscUtils.inferNewNonFilteredSet(this.rootNodes, getNodeNameFilterFunction(nodeNameFilter), rootNodes));
	}

	public Node getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(Node selectedNode) {
		this.selectedNode = selectedNode;
	}

	public String getNodeNameFilter() {
		return nodeNameFilter;
	}

	public void setNodeNameFilter(String nodeNameFilter) {
		this.nodeNameFilter = nodeNameFilter;
		if (rootNodes != null) {
			for (Node node : rootNodes) {
				node.setNodeNameFilter(nodeNameFilter);
			}
		}
	}

	public static class Node implements Comparable<Node> {
		private String name;
		private int[] intArray = new int[0];
		private Set<Integer> integerSet = new HashSet<Integer>();
		private List<String> stringList = new ArrayList<String>();
		private SortedSet<Node> children = new TreeSet<SelectionTest.Node>();
		private String nodeNameFilter;

		public Node(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int[] getIntArray() {
			return intArray;
		}

		public void setIntArray(int[] intArray) {
			this.intArray = intArray;
		}

		public Set<Integer> getIntegerSet() {
			return integerSet;
		}

		public void setIntegerSet(Set<Integer> integerSet) {
			this.integerSet = integerSet;
		}

		public List<String> getStringList() {
			return stringList;
		}

		public void setStringList(List<String> stringList) {
			this.stringList = stringList;
		}

		public SortedSet<Node> getChildren() {
			return new TreeSet<SelectionTest.Node>(
					MiscUtils.getFilteredSet(children, getNodeNameFilterFunction(nodeNameFilter)));
		}

		public void setChildren(SortedSet<Node> children) {
			this.children = new TreeSet<SelectionTest.Node>(
					MiscUtils.inferNewNonFilteredSet(this.children, getNodeNameFilterFunction(nodeNameFilter), children));
		}

		public String getNodeNameFilter() {
			return nodeNameFilter;
		}

		public void setNodeNameFilter(String nodeNameFilter) {
			this.nodeNameFilter = nodeNameFilter;
			if (children != null) {
				for (Node node : children) {
					node.setNodeNameFilter(nodeNameFilter);
				}
			}
		}

		@Override
		public int compareTo(Node o) {
			if ((name == null) && (o.name == null)) {
				return 0;
			}
			if ((name != null) && (o.name == null)) {
				return 1;
			}
			if ((name == null) && (o.name != null)) {
				return -1;
			}
			return name.compareTo(o.name);
		}

		@Override
		public String toString() {
			return "Node [name=" + name + "]";
		}

	}

}
