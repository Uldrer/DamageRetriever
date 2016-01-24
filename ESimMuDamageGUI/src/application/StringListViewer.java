package application;

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

public class StringListViewer extends ListViewer {

	public StringListViewer(Composite parent, int style) {
		super(parent, style);
	}
	
	public String getStringSelection() {
		int[] selections = listGetSelectionIndices();
		
		List item = getList();
		
		for (Integer i : selections) {
			return item.getItem(i);
		}
		return "";
		
	}

}
