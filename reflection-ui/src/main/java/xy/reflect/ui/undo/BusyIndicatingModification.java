package xy.reflect.ui.undo;

import java.awt.Component;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;

public class BusyIndicatingModification extends AbstractModificationProxy {

	protected SwingRenderer swingRenderer;
	protected Component busyDialogOwner;

	public BusyIndicatingModification(IModification base, SwingRenderer swingRenderer, Component busyDialogOwner) {
		super(base);
		this.swingRenderer = swingRenderer;
		this.busyDialogOwner = busyDialogOwner;
	}

	@Override
	public IModification applyAndGetOpposite() throws IrreversibleModificationException {
		final IModification[] baseOpposite = new IModification[1];
		swingRenderer.showBusyDialogWhile(busyDialogOwner, new Runnable() {
			@Override
			public void run() {
				baseOpposite[0] = base.applyAndGetOpposite();
			}
		}, getTitle());
		return new BusyIndicatingModification(baseOpposite[0], swingRenderer, busyDialogOwner);
	}
}