package xy.reflect.ui.util.component;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AutoResizeTabbedPane extends JTabbedPane {

	protected static final long serialVersionUID = 1L;
	
	public AutoResizeTabbedPane() {
		super();
		addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				validate();
			}

		});
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (result == null) {
			return null;
		}
		int maxTabHeigh = 0;
		int currentTabHeight = 0;
		for (int i = 0; i < getTabCount(); i++) {
			Component tab = getComponentAt(i);
			if (tab != null) {
				Dimension tabSize = tab.getPreferredSize();
				if (tabSize != null) {
					maxTabHeigh = Math.max(maxTabHeigh, tabSize.height);
					if (i == getSelectedIndex()) {
						currentTabHeight = tabSize.height;
					}
				}
			}
		}
		result.height += (currentTabHeight - maxTabHeigh);
		return result;
	}

}
