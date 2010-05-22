package atarih.variabledumper.ui.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.internal.debug.core.model.JDIVariable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.internal.ViewPluginAction;

import atarih.variabledumper.handler.TypeHandler;

public class VariableDumperAction implements IViewActionDelegate {
	
	// just a test...
	IWorkbenchWindow activeWindow = null;
	
	IViewPart fview = null;
	
	MessageConsole messageConsole = null;
	
	@Override
    public void init(IViewPart view) {	}
	
	@Override
	@SuppressWarnings("restriction")
    public void run(IAction action) {
		ViewPluginAction viewAction = (ViewPluginAction) action;

		TreeSelection treeSelection = (TreeSelection) viewAction.getSelection();
		Object elem = treeSelection.getFirstElement();
		
		if (elem instanceof JDIVariable) {
			JDIVariable variable = (JDIVariable) elem;
			
			try {
				TypeHandler typeHandler = new TypeHandler();

				typeHandler.handleVariable("", variable);
   
            } catch (DebugException e) {
	            e.printStackTrace();
            } catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
    }

	@Override
    public void selectionChanged(IAction action, ISelection selection) {
	    // TODO Auto-generated method stub
    }
}
