package xy.reflect.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.customizer.CustomizationController;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.undo.ModificationStack;

public class ErrorDisplayTest {

	public static void main(String[] args) throws Exception {

		CustomizedUI reflectionUI = new CustomizedUI();
		File tmpCustomizationsFile = File.createTempFile(ErrorDisplayTest.class.getName(), ".icu");
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
				swingCustomizer.openObjectFrame(new ErrorDisplayTest());
			}
		});
	}

	private int classic = 0;
	private int spinned = 0;
	private Integer nullable = 0;
	private Integer nullableWithNullLabel = 0;
	private Number polymorphic = 0;
	private Object mutable = 0;
	private Child detached = new Child();
	private Child embedded = new Child();
	private List<Integer> list = new ArrayList<Integer>(Arrays.asList(0));
	private Date date = new GregorianCalendar(1983, Calendar.JANUARY, 20).getTime();
	private Date dateTime = new GregorianCalendar(1983, Calendar.JANUARY, 20, 5, 0, 0).getTime();

	private boolean accessErrorThrown = false;
	private boolean updateErrorThrown = false;

	public int getClassic() {
		if (accessErrorThrown) {
			throw new RuntimeException("getClassic");
		}
		return classic;
	}

	public void setClassic(int classic) {
		this.classic = classic;
		if (updateErrorThrown) {
			throw new RuntimeException("setClassic");
		}
	}

	public int getSpinned() {
		if (accessErrorThrown) {
			throw new RuntimeException("getSpinned");
		}
		return spinned;
	}

	public void setSpinned(int spinned) {
		this.spinned = spinned;
		if (updateErrorThrown) {
			throw new RuntimeException("setSpinned");
		}
	}

	public Integer getNullable() {
		if (accessErrorThrown) {
			throw new RuntimeException("getNullable");
		}
		return nullable;
	}

	public void setNullable(Integer nullable) {
		this.nullable = nullable;
		if (updateErrorThrown) {
			throw new RuntimeException("setNullable");
		}
	}

	public Integer getNullableWithNullLabel() {
		if (accessErrorThrown) {
			throw new RuntimeException("getNullableWithNullLabel");
		}
		return nullableWithNullLabel;
	}

	public void setNullableWithNullLabel(Integer nullableWithNullLabel) {
		this.nullableWithNullLabel = nullableWithNullLabel;
		if (updateErrorThrown) {
			throw new RuntimeException("setNullableWithNullLabel");
		}
	}

	public Number getPolymorphic() {
		if (accessErrorThrown) {
			throw new RuntimeException("getPolymorphic");
		}
		return polymorphic;
	}

	public void setPolymorphic(Number polymorphic) {
		this.polymorphic = polymorphic;
		if (updateErrorThrown) {
			throw new RuntimeException("setPolymorphic");
		}
	}

	public Object getMutable() {
		if (accessErrorThrown) {
			throw new RuntimeException("getMutable");
		}
		return mutable;
	}

	public void setMutable(Object mutable) {
		this.mutable = mutable;
		if (updateErrorThrown) {
			throw new RuntimeException("setMutable");
		}
	}

	public Child getDetached() {
		if (accessErrorThrown) {
			throw new RuntimeException("getDetached");
		}
		return detached;
	}

	public void setDetached(Child child) {
		this.detached = child;
		if (updateErrorThrown) {
			throw new RuntimeException("setDetached");
		}
	}

	public Child getEmbedded() {
		if (accessErrorThrown) {
			throw new RuntimeException("getEmbedded");
		}
		return embedded;
	}

	public void setEmbedded(Child embedded) {
		this.embedded = embedded;
		if (updateErrorThrown) {
			throw new RuntimeException("setEmbedded");
		}
	}

	public List<Integer> getList() {
		if (accessErrorThrown) {
			throw new RuntimeException("getList");
		}
		return list;
	}

	public void setList(List<Integer> list) {
		this.list = list;
		if (updateErrorThrown) {
			throw new RuntimeException("setList");
		}
	}

	public Date getDate() {
		if (accessErrorThrown) {
			throw new RuntimeException("getDate");
		}
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
		if (updateErrorThrown) {
			throw new RuntimeException("setDate");
		}
	}

	public Date getDateTime() {
		if (accessErrorThrown) {
			throw new RuntimeException("getDateTime");
		}
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
		if (updateErrorThrown) {
			throw new RuntimeException("setDateTime");
		}
	}

	public boolean isAccessErrorThrown() {
		return accessErrorThrown;
	}

	public void setAccessErrorThrown(boolean accessErrorThrown) {
		this.accessErrorThrown = accessErrorThrown;
	}

	public boolean isUpdateErrorThrown() {
		return updateErrorThrown;
	}

	public void setUpdateErrorThrown(boolean updateErrorThrown) {
		this.updateErrorThrown = updateErrorThrown;
	}

	public static class Child {
		public int value = 0;

		@Override
		public String toString() {
			return "Child [value=" + value + "]";
		}

	}

}
