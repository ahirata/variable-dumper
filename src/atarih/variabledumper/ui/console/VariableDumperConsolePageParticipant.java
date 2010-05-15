package atarih.variabledumper.ui.console;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.console.actions.CloseConsoleAction;
import org.eclipse.ui.part.IPageBookViewPage;

public class VariableDumperConsolePageParticipant implements IConsolePageParticipant {
	
	private CloseConsoleAction fCloseAction;
	
	public void init(IPageBookViewPage page, IConsole console) {
		fCloseAction = new CloseConsoleAction(console);
		IActionBars bars = page.getSite().getActionBars();
		bars.getToolBarManager().appendToGroup(IConsoleConstants.LAUNCH_GROUP, this.fCloseAction);
	}

	public void dispose() {
		this.fCloseAction = null;
	}

	public void activated() {
	}

	public void deactivated() {
	}

	public Object getAdapter(Class adapter) {
		return null;
	}
}
