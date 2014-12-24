package xy.reflect.ui.util.component;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import xy.reflect.ui.util.ReflectionUIError;

public class SimpleLayout extends GridBagLayout {

	protected static final long serialVersionUID = 1L;

	public enum Kind {
		ROW, COLUMN;
	}

	protected Kind kind;
	protected int addedComponentCount = 0;

	public SimpleLayout(Kind kind) {
		this.kind = kind;
	}

	public static void add(Container container, Component component) {
		SimpleLayout simpleLayout = (SimpleLayout) container.getLayout();
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx = 1.0;
		gc.weighty = 1.0;
		if (simpleLayout.kind == Kind.COLUMN) {
			gc.gridx = 0;
			gc.gridy = simpleLayout.addedComponentCount;
			gc.fill = GridBagConstraints.HORIZONTAL;
		} else if (simpleLayout.kind == Kind.ROW) {
			gc.gridx = simpleLayout.addedComponentCount;
			gc.gridy = 0;
			gc.fill = GridBagConstraints.VERTICAL;
		} else {
			throw new ReflectionUIError();
		}
		int SPACING = 5;
		gc.insets = new Insets(SPACING, SPACING, SPACING, SPACING);
		container.add(component, gc);
		simpleLayout.addedComponentCount++;
	}
}