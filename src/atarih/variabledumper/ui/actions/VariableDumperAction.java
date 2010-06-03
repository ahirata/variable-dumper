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
import org.eclipse.ui.internal.ViewPluginAction;

import atarih.variabledumper.handler.TypeHandler;

public class VariableDumperAction implements IViewActionDelegate {

    // defaults to 500
    private static int TIMEOUT = 500;

    static {
        String timeoutProperty = System.getProperty("variableDumperTimeout");
        if (timeoutProperty != null && !timeoutProperty.isEmpty()) {
            try {
                TIMEOUT = Integer.parseInt(timeoutProperty);
            } catch (Exception e) { }
        }
    };

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
            } catch (Exception e) {
                e.printStackTrace();
                status = new Status(IStatus.ERROR, "variable-dumper", e.getMessage());
            }
            synchronized(this) {
                notifyAll();
            }
            return status;
        }
    }

    @Override
    public void init(IViewPart view) {  }

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
                synchronized (job) {
                    job.wait(TIMEOUT);
                }

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
