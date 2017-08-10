package xy.reflect.ui.info.menu.builtin;

public class ExitMenuItem extends AbstractBuiltInActionMenuItem {

	public ExitMenuItem() {
		name = "Exit";
	}

	@Override
	public void execute(Object form, Object renderer) {
		System.exit(0);
	}

}
