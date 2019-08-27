import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

public class CommonUIDefaultsKeys {
	public static void main(String args[]) throws Exception {
		UIManager.LookAndFeelInfo looks[] = UIManager.getInstalledLookAndFeels();

		SortedSet<String> allDefaultUIKeys = new TreeSet<String>();

		for (UIManager.LookAndFeelInfo info : looks) {
			System.out.println("Including L&F " + info.getName());
			UIManager.setLookAndFeel(info.getClassName());
			UIDefaults defaults = UIManager.getDefaults();
			Enumeration<Object> newKeyObjects = defaults.keys();
			while (newKeyObjects.hasMoreElements()) {
				String key = newKeyObjects.nextElement().toString();
				allDefaultUIKeys.add(key);
			}
		}

		SortedSet<String> commonDefaultUIKeys = new TreeSet<String>(allDefaultUIKeys);
		for (String key : allDefaultUIKeys) {
			for (UIManager.LookAndFeelInfo info : looks) {
				UIManager.setLookAndFeel(info.getClassName());
				UIDefaults defaults = UIManager.getDefaults();
				boolean currentLafCntainsKey = false;
				Enumeration<Object> newKeyObjects = defaults.keys();
				while (newKeyObjects.hasMoreElements()) {
					String currentLafKey = newKeyObjects.nextElement().toString();
					if (currentLafKey.equals(key)) {
						currentLafCntainsKey = true;
						break;
					}
				}
				if (!currentLafCntainsKey) {
					commonDefaultUIKeys.remove(key);
					break;
				}
			}
		}

		System.out.println();
		System.out.println("================== Common UIDefaults Keys ==================");
		for (String key : commonDefaultUIKeys) {
			System.out.println("(Common) " + key);
		}

		System.out.println();
		System.out.println("================== All UIDefaults Keys ==================");
		for (String key : allDefaultUIKeys) {
			System.out.println(key);
		}
	}
}