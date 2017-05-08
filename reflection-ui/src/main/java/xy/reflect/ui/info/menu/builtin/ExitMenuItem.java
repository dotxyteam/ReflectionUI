package xy.reflect.ui.info.menu.builtin;

public class ExitMenuItem extends AbstractBuiltInActionMenuItem {

	private static final long serialVersionUID = 1L;

	public ExitMenuItem() {
		name = "Exit";
	}

	@Override
	public void execute(Object object, Object renderer) {
		System.exit(0);
	}

}
