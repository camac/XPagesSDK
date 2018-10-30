package org.openntf.xsp.sdk.utils;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class StaticTextFieldEditor extends FieldEditor {

	private Label emptyLabel;
	
	public StaticTextFieldEditor(Composite parent, String label) {
		this.init("", label);
		createControl(parent);
	}
	
	@Override
	protected void adjustForNumColumns(int numColumns) {
		if(emptyLabel != null) {
			GridData gd = (GridData) emptyLabel.getLayoutData(); 

			gd.horizontalSpan = numColumns;
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
		}
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		this.emptyLabel = getLabelControl(parent);

        GridData gd = new GridData();

        gd.horizontalSpan = numColumns;
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        
        this.emptyLabel.setLayoutData(gd);
	}

	@Override
	protected void doLoad() {
		// Do Nothing
	}

	@Override
	protected void doLoadDefault() {
		// Do nothing		
	}

	@Override
	protected void doStore() {
		// Do nothing
		
	}

	@Override
	public int getNumberOfControls() {
		return 1;
	}

}
