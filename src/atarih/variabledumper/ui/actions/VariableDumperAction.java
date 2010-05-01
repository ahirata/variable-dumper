package atarih.variabledumper.ui.actions;

import java.util.Arrays;
import java.util.List;

import org.eclipse.debug.core.DebugException;
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
			JDIFieldVariable localVariable = (JDIFieldVariable) elem;
			
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
	    	String variableName = printConstructor(localVariable);

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
		if (!field.getValue().getClass().equals(JDIArrayValue.class) && !field.getValue().getClass().equals(JDIPrimitiveValue.class) && !isWrapper(field.getJavaType().getName()) && !field.getValue().getClass().equals(JDINullValue.class)) {
			String fieldName = printConstructor(field);			
			JDIObjectValue objectValue = (JDIObjectValue) field.getValue();
			if (objectValue.getVariables() != null && objectValue.getValueString().length() > 0) {
				System.out.println(variableName + "." + "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length()) + "(" + fieldName + ");");
			}
			for (IVariable variable : objectValue.getVariables()) {
				
				if (variable instanceof JDIFieldVariable) {
					analyzeFieldVariable(fieldName, (JDIFieldVariable) variable);
				}
			}
		} else if (isWrapper(field.getJavaType().getName())) {
			printWrapper(variableName, field);
		} else if (field.getValue().getClass().equals(JDIPrimitiveValue.class)) {
			printPrimitive(variableName, field);
		} else if (field.getValue().getClass().equals(JDIArrayValue.class)) {
			printArray(variableName, field);
		}
    }

	private String printConstructor(JDIVariable fieldVariable) throws DebugException {
		String fullname = fieldVariable.getJavaType().getName();
		String variableName = fieldVariable.getName();
		
	    System.out.println(fullname + " " + variableName + " = new "+ fullname + "();");
	    
	    return variableName; 
    }

	private void printWrapper(String variableName, JDIVariable field) throws DebugException {
		JDIObjectValue objectValue = (JDIObjectValue) field.getValue();
		if (field.getJavaType().getName().equals("java.lang.String")) {
			System.out.println(variableName + "." + "set" + field.getName().substring(0, 1).toUpperCase().concat(field.getName().substring(1, field.getName().length())) + "(new " + field.getJavaType().getName() + "(" + field.getValue() + "));");
		} else {
			for (IVariable variable : objectValue.getVariables()) {
				if (variable instanceof JDIFieldVariable && variable.getName().equals("value")) {
					System.out.println(variableName + "." + "set" + field.getName().substring(0, 1).toUpperCase().concat(field.getName().substring(1, field.getName().length())) + "(new " + field.getJavaType().getName() + "(\"" + variable.getValue() + "\"));");		
				}
			}
		}
	}
	
	private void printPrimitive(String variableName, JDIVariable field) throws DebugException {
		System.out.println(variableName + "." + "set" + field.getName().substring(0, 1).toUpperCase().concat(field.getName().substring(1, field.getName().length())) + "((" + field.getJavaType().getName() +  ")"+ field.getValue() + ");");
	}
	private void printArray(String variableName, JDIVariable field) throws DebugException {
		System.out.println(variableName + "." + "set" + field.getName().substring(0, 1).toUpperCase().concat(field.getName().substring(1, field.getName().length())) + "(new " + field.getJavaType().getName().replaceAll("\\[\\]", "") + "[" + field.getValue().getVariables().length + "]);");
		for (int i=0; i<field.getValue().getVariables().length; i++) {
			System.out.println(variableName + "." + "get" + field.getName().substring(0, 1).toUpperCase().concat(field.getName().substring(1, field.getName().length())) + "()[" + i + "] = (" +field.getJavaType().getName().replaceAll("\\[\\]", "")+ ")" + field.getValue().getVariables()[i].getValue() + ";");		
		}

	}
	private boolean isWrapper(String type) {
		return TYPES.contains(type); 
	}
	
	@Override
    public void selectionChanged(IAction action, ISelection selection) {
	    // TODO Auto-generated method stub
    }
	
}
