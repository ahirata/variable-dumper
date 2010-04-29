package atarih.variabledumper.ui.actions;

import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdi.internal.ObjectReferenceImpl;
import org.eclipse.jdt.internal.debug.core.model.JDIFieldVariable;
import org.eclipse.jdt.internal.debug.core.model.JDILocalVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.ViewPluginAction;

import com.sun.jdi.Type;

public class VariableDumperAction implements IViewActionDelegate {
	// just a test...
	IWorkbenchWindow activeWindow = null;
	
	IViewPart fview = null;
	
	@Override
    public void init(IViewPart view) {
		// just a test...
		activeWindow = view.getSite().getWorkbenchWindow();
		
		view.getViewSite();
		view.getSite();
	}
	
	@Override
    public void run(IAction action) {
		// just a test...
		Shell shell = activeWindow.getShell();
		
		ViewPluginAction viewAction = (ViewPluginAction) action;

		TreeSelection treeSelection = (TreeSelection)viewAction.getSelection();
		
		Object elem = treeSelection.getFirstElement();
		
		if (elem instanceof JDIFieldVariable) {
			JDIFieldVariable localVariable = (JDIFieldVariable) elem;
			
			MessageDialog.openInformation(shell, "Dump it!", "Field Variable");
		} else if (elem instanceof JDILocalVariable) {
			JDILocalVariable localVariable = (JDILocalVariable) elem;

			// TODO - check how we can instantiate the the variable
			// since we are in a different class loader, regular reflection calls 
			// using localVariable.getJavaType().getName() directly don't apply.
			// Maybe casting localVariable to ObjectReferenceImpl and grabbing the classLoader
			// and to call Class.forName(). 
			// Or maybe it has some other specific method for doing that.
			// the method bellow virtualMachineImpl.allClasses does return the classes we want...
			// TODO - check how Expressions Plugin does it...
			try {
				List classes = ((ObjectReferenceImpl) ((JDIObjectValue)localVariable.getValue()).getUnderlyingObject()).virtualMachineImpl().allClasses();

				for (Type clazz : (List<Type>) classes) {
					if (clazz.name().contains("ClasseTeste")) {
						System.out.println(clazz.getClass().getName());
					}
				}
			} catch (DebugException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			MessageDialog.openInformation(shell, "Dump it!", "Local Variable");	
		}
    }

	@Override
    public void selectionChanged(IAction action, ISelection selection) {
	    // TODO Auto-generated method stub
    }
	
}
