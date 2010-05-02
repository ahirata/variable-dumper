package atarih.variabledumper.ui.actions;

import static atarih.variabledumper.util.OutputUtils.arrayConstructor;
import static atarih.variabledumper.util.OutputUtils.constructor;
import static atarih.variabledumper.util.OutputUtils.defaultConstructor;
import static atarih.variabledumper.util.OutputUtils.print;
import static atarih.variabledumper.util.OutputUtils.value;

import java.util.Arrays;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdi.internal.TypeImpl;
import org.eclipse.jdt.internal.debug.core.model.JDIArrayValue;
import org.eclipse.jdt.internal.debug.core.model.JDIFieldVariable;
import org.eclipse.jdt.internal.debug.core.model.JDILocalVariable;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIPrimitiveValue;
import org.eclipse.jdt.internal.debug.core.model.JDIVariable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.ViewPluginAction;

// TODO - refactor: methods like getJavaType and getValue do lots of things
//        every time they're called and we are using them a bunch of times inside
//        the same method. we should store them using local variables
//		  after the first call.
public class VariableDumperAction implements IViewActionDelegate {
	
	private static final List<String> TYPES = Arrays.asList(new String[] {
			"java.lang.Boolean", 
			"java.lang.Byte", 
			"java.lang.Short", 
			"java.lang.Integer", 
			"java.lang.Long", 
			"java.lang.Float", 
			"java.lang.Double", 
			"java.lang.String"
	});
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
			JDIFieldVariable fieldVariable = (JDIFieldVariable) elem;
			
			try {
	            analyzeFieldVariable(fieldVariable.getName(), fieldVariable);
            } catch (DebugException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
			MessageDialog.openInformation(shell, "Dump it!", "Field Variable");
		} else if (elem instanceof JDILocalVariable) {
			JDILocalVariable localVariable = (JDILocalVariable) elem;
			
			try {
	            analyzeLocalVariable(localVariable);
            } catch (DebugException e) {
	            e.printStackTrace();
            }
			
			MessageDialog.openInformation(shell, "Dump it!", "Local Variable");	
		}
    }

	private void analyzeLocalVariable(JDILocalVariable localVariable) throws DebugException {	    
	    JDIObjectValue objectValue = (JDIObjectValue) localVariable.getValue();
	    
	    if (!TypeImpl.isPrimitiveSignature(objectValue.getSignature())) {
	    	String variableName = localVariable.getName();
	    	String javaType = localVariable.getJavaType().getName();
	    	
	    	print(defaultConstructor(javaType).assignedTo(javaType, variableName));

	    	for (IVariable variable : objectValue.getVariables()) {
	    		if (variable instanceof JDIFieldVariable) {
	    			analyzeFieldVariable(variableName, (JDIFieldVariable) variable);
	    		}
	    	}
	    } else {
			System.out.println("we dont dump primitive variables... doesnt make any sense... yet.");
		}
    }

	private void analyzeFieldVariable(String variableName, JDIFieldVariable field) throws DebugException {
		IValue value = field.getValue();
		String javaType = field.getJavaType().getName();
		if (isWrapper(javaType)) {
			handleWrapper(variableName, field);
		} else if (value.getClass().equals(JDIPrimitiveValue.class)) {
			handlePrimitive(variableName, field);
		} else if (value.getClass().equals(JDIArrayValue.class)) {
			handlePrimitiveArray(variableName, field);
		} else if (!value.getClass().equals(JDINullValue.class)) {
			
	    	String fieldName = field.getName();
	    	
			print(defaultConstructor(javaType).assignedTo(javaType, fieldName));
			
			JDIObjectValue objectValue = (JDIObjectValue) value;
			
			if (objectValue.getVariables() != null && objectValue.getValueString().length() > 0) {
				print(value(fieldName).setTo(variableName, fieldName));
			}
			for (IVariable variable : objectValue.getVariables()) {
				
				if (variable instanceof JDIFieldVariable) {
					analyzeFieldVariable(fieldName, (JDIFieldVariable) variable);
				}
			}
		} 
    }

	private void handleWrapper(String variableName, JDIVariable field) throws DebugException {
		String fieldName = field.getName();
		String javaType = field.getJavaType().getName();
		
		JDIObjectValue objectValue = (JDIObjectValue) field.getValue();
		if (javaType.equals("java.lang.String")) {
			String value = field.getValue().toString();
			print(constructor(javaType, value).setTo(variableName, fieldName));
		} else {
			for (IVariable variable : objectValue.getVariables()) {
				if (variable instanceof JDIFieldVariable && variable.getName().equals("value")) {
					String value = "\"" + variable.getValue() + "\"";
					print(constructor(javaType, value).setTo(variableName, fieldName));	
				}
			}
		}
	}
	
	private void handlePrimitive(String variableName, JDIVariable field) throws DebugException {
		String value = "(" + field.getJavaType().getName() +  ")"+ field.getValue();
		String fieldName = field.getName();
		print(value(value).setTo(variableName, fieldName));
	}
	
	private void handlePrimitiveArray(String variableName, JDIVariable field) throws DebugException {
		int arrayLength = field.getValue().getVariables().length;
		String javaType = field.getJavaType().getName();
		String arrayType = javaType.replaceAll("\\[\\]", "");
		String fieldName = field.getName();
		
		print(arrayConstructor(arrayType, arrayLength).assignedTo(javaType, fieldName));

		for (int i=0; i<arrayLength; i++) {
			String value = "(" + arrayType + ")" + field.getValue().getVariables()[i].getValue();
			print(value(value).assignedTo(fieldName + "[" +  i + "]"));
		}
		
		print(value(fieldName).setTo(variableName, fieldName));

	}
	private boolean isWrapper(String type) {
		return TYPES.contains(type); 
	}
	
	@Override
    public void selectionChanged(IAction action, ISelection selection) {
	    // TODO Auto-generated method stub
    }
}
