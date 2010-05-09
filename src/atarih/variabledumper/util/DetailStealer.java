package atarih.variabledumper.util;

import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.ui.DebugUIMessages;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;
import org.eclipse.jdt.internal.debug.ui.JavaDetailFormattersManager;

public class DetailStealer {
	/**
	 * @see IDebugModelPresentation#computeDetail(IValue, IValueDetailListener)
	 */
	private void computeDetail(IValue value, IValueDetailListener listener) {
		IJavaThread thread = JDIModelPresentation.getEvaluationThread((IJavaDebugTarget)value.getDebugTarget());
		if (thread == null) {
			listener.detailComputed(value, DebugUIMessages.JDIModelPresentation_no_suspended_threads); 
		} else {
			JavaDetailFormattersManager.getDefault().computeValueDetail((IJavaValue)value, thread, listener);
		}
	}
	
	/**
	 * Returns the detail value for the given variable or <code>null</code>
	 * if none can be computed.
	 * @param variable the variable to compute the detail for
	 * @return the detail value for the variable
	 */
	public String getVariableDetail(IJavaValue value) {
		final String[] detail= new String[1];
		final Object lock= new Object();
		computeDetail(value, new IValueDetailListener() {
		    /* (non-Javadoc)
		     * @see org.eclipse.debug.ui.IValueDetailListener#detailComputed(org.eclipse.debug.core.model.IValue, java.lang.String)
		     */
		    public void detailComputed(IValue computedValue, String result) {
		        synchronized (lock) {
		            detail[0]= result;
		            lock.notifyAll();
		        }
		    }
		});
		synchronized (lock) {
		    if (detail[0] == null) {
		        try {
		            lock.wait(5000);
		        } catch (InterruptedException e1) {
		            // Fall through
		        }
		    }
		}
		return detail[0];
	}
}
