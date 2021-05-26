package xy.reflect.ui.control.swing.util;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Allows to resize only the last column when the table size changes. Was
 * created because {@link JTable#AUTO_RESIZE_LAST_COLUMN} works only for manual
 * column size changes, not for the whole table size changes.
 * 
 * @author olitank
 *
 */
public class TableLastColumnAutoResizer implements ComponentListener, MouseListener {

	protected Map<TableColumn, Integer> widthByColumnForRestorationAfterWholeTableResizing = new LinkedHashMap<TableColumn, Integer>();
	protected Map<TableColumn, Integer> widthByColumnForManualColumnResizingDetection = new LinkedHashMap<TableColumn, Integer>();
	protected JTable table;
	protected ScheduledExecutorService delayedColumnWidthRestorationExecutor = Executors
			.newSingleThreadScheduledExecutor(new DelayedColumnWidthRestorationThreadFactory());

	public void installOn(JTable table) {
		this.table = table;
		table.addComponentListener(this);
		table.getTableHeader().addMouseListener(this);
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if (!table.isDisplayable() || !table.isVisible()) {
			return;
		}
		if (columnListChangeDetected(widthByColumnForRestorationAfterWholeTableResizing)) {
			saveColumnWidths(widthByColumnForRestorationAfterWholeTableResizing);
		} else {
			delayedColumnWidthRestorationExecutor.schedule(new Runnable() {
				@Override
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							if (columnListChangeDetected(widthByColumnForRestorationAfterWholeTableResizing)) {
								return;
							}
							restoreColumnWidths(widthByColumnForRestorationAfterWholeTableResizing);
						}
					});
				}
			}, 500, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		saveColumnWidths(widthByColumnForManualColumnResizingDetection);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Map<TableColumn, Integer> newWidthByColumn = new LinkedHashMap<TableColumn, Integer>();
		saveColumnWidths(newWidthByColumn);
		if (!newWidthByColumn.equals(widthByColumnForManualColumnResizingDetection)) {
			saveColumnWidths(widthByColumnForRestorationAfterWholeTableResizing);
		}
	}

	protected boolean columnListChangeDetected(Map<TableColumn, Integer> initialWidthByColumn) {
		return !Arrays.equals(initialWidthByColumn.keySet().toArray(),
				Collections.list(table.getColumnModel().getColumns()).toArray());
	}

	protected void restoreColumnWidths(Map<TableColumn, Integer> widthByColumn) {
		TableColumnModel columnModel = table.getColumnModel();
		for (int i = 0; i < table.getColumnCount(); i++) {
			boolean lastColumn = (i == (table.getColumnCount() - 1));
			if (lastColumn) {
				break;
			}
			final TableColumn column = columnModel.getColumn(i);
			final int initialMaxWidth = column.getMaxWidth();
			final int initialMinWidth = column.getMinWidth();
			column.setMaxWidth(widthByColumn.get(column));
			column.setMinWidth(widthByColumn.get(column));
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					column.setMaxWidth(initialMaxWidth);
					column.setMinWidth(initialMinWidth);
				}
			});
		}
	}

	protected void saveColumnWidths(Map<TableColumn, Integer> widthByColumn) {
		widthByColumn.clear();
		TableColumnModel columnModel = table.getColumnModel();
		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			TableColumn column = columnModel.getColumn(i);
			widthByColumn.put(column, column.getWidth());
		}
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	protected static class DelayedColumnWidthRestorationThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			Thread result = new Thread(r,
					TableLastColumnAutoResizer.class.getName() + ".delayedColumnWidthRestorationExecutor");
			result.setDaemon(true);
			return result;
		}

	}

}