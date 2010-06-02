package atarih.variabledumper.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.internal.debug.core.model.JDIVariable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.ViewPluginAction;

import atarih.variabledumper.handler.TypeHandler;

public class VariableDumperAction implements IViewActionDelegate {

    private static final int TIMEOUT = 500;
    private IWorkbenchWindow activeWindow = null;

    private static final class VariableDumpJob extends Job {
        private final JDIVariable variable;

        private VariableDumpJob(String name, JDIVariable variable) {
            super(name);
            this.variable = variable;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            IStatus status = null;
            try {
                TypeHandler typeHandler = new TypeHandler();
                typeHandler.setMonitor(monitor);
                typeHandler.handleVariable("", variable);

                status = Status.OK_STATUS;
            } catch (DebugException e) {
                e.printStackTrace();
                status = e.getStatus();
            }
            return status;
        }
    }

    @Override
    public void init(IViewPart view) {
        activeWindow = view.getSite().getWorkbenchWindow();
    }

    @Override
    @SuppressWarnings("restriction")
    public void run(IAction action) {
        ViewPluginAction viewAction = (ViewPluginAction) action;

        TreeSelection treeSelection = (TreeSelection) viewAction.getSelection();
        Object elem = treeSelection.getFirstElement();

        if (elem instanceof JDIVariable) {
            final JDIVariable variable = (JDIVariable) elem;

            Job job = new VariableDumpJob("dump-variable", variable);

            job.schedule();

            try {
                Thread.sleep(TIMEOUT);
            } catch (InterruptedException e) {
                System.out.println("exception while sleeping");
                e.printStackTrace();
            }
            if (job.getState() != Job.NONE && job.getResult() == null) {
                job.cancel();
            }
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) { }
}
