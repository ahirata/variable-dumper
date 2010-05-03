package atarih.variabledumper.ui.actions;

import static atarih.variabledumper.util.OutputUtils.arrayConstructor;
import static atarih.variabledumper.util.OutputUtils.arrayIndex;
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
import org.eclipse.jdt.internal.debug.core.model.JDIArrayEntryVariable;
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

import atarih.variabledumper.util.Output;

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
		
		if (elem instanceof JDIVariable) {
			JDIVariable fieldVariable = (JDIVariable) elem;
			
			try {
				analyzeVariable("", fieldVariable);	            
            } catch (DebugException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
		}
    }

	private void analyzeVariable(String variableName, JDIVariable field) throws DebugException {
		IValue value = field.getValue();
		String javaType = field.getJavaType().getName();
		String fieldName = field.getName();
		
		handleTypes(variableName, fieldName, javaType, value); 
    }

	private void handleTypes(String variableName, String fieldName, String javaType, IValue value) throws DebugException {
	    if (value.getClass().equals(JDINullValue.class)) {
	    	handleNullValue(variableName, fieldName, javaType);
			
		} else if (isWrapper(javaType)) {
			handleWrapper(variableName, fieldName, javaType, value);
			
		} else if (value.getClass().equals(JDIPrimitiveValue.class) || javaType.equals("java.lang.String")) {
			handlePrimitive(variableName, fieldName, javaType, value);
			
		} else if (value.getClass().equals(JDIArrayValue.class)) {
			handleArray(variableName, fieldName, javaType, value);
			
		} else {
			handleObject(variableName, fieldName, javaType, value);
		}
    }

	private void handleObject(String variableName, String fieldName, String javaType, IValue value) throws DebugException {
	    String tempVariableName = fieldName;
	    if (variableName.equals("")) {
	    	tempVariableName = fieldName;
	    	print(defaultConstructor(javaType).assignedTo(javaType, tempVariableName));
	    } else if(fieldName.equals("")) {
	    	tempVariableName = variableName;
	    	print(defaultConstructor(javaType).assignedTo(tempVariableName));
	    } else {
	    	tempVariableName = variableName+Output.capitalize(fieldName);
	    	print(defaultConstructor(javaType).assignedTo(javaType, tempVariableName));
	    }
	     
	    JDIObjectValue objectValue = (JDIObjectValue) value;
	    
	    IVariable[] variables = objectValue.getVariables();
	    
	    if (variables != null && objectValue.getValueString().length() > 0) {
	    	if (!fieldName.equals("") && !variableName.equals("")) {
	    		print(value(tempVariableName).setTo(variableName, fieldName));
	    	}
	    }
	    for (IVariable variable : variables) {
	    	
	    	if (variable instanceof JDIVariable) {
	    		analyzeVariable(tempVariableName, (JDIVariable) variable);
	    	}
	    }
    }

	private void handleNullValue(String variableName, String fieldName, String javaType) {
	    if (variableName.equals("")) {
	    	print(value("null").assignedTo(javaType, fieldName));
	    } else if (fieldName.equals("")) {
	    	print(value("null").assignedTo(variableName));
	    } else {
	    	print(value("null").setTo(variableName, fieldName));
	    }
    }

	private void handleWrapper(String variableName, String fieldName, String javaType, IValue fieldValue) throws DebugException {
		String value = getWrapperValue(fieldValue);
		
		if (variableName.equals("")) {
			print(constructor(javaType, value).assignedTo(javaType, fieldName));
		} else if (fieldName.equals("")){
			print(constructor(javaType, value).assignedTo(variableName));
		} else {
			print(constructor(javaType, value).setTo(variableName, fieldName));
		}
	}
	
	private String getWrapperValue(IValue objectValue) throws DebugException {
		String value = null;
		for (IVariable variable : objectValue.getVariables()) {
			if (variable instanceof JDIVariable && variable.getName().equals("value")) {
				value = "\"" + variable.getValue() + "\"";
				break;	
			}
		}
		return value;
	}

	private void handlePrimitive(String variableName, String fieldName, String javaType, IValue fieldValue) throws DebugException {
		String value = "(" + javaType +  ")"+ fieldValue;
		if (variableName.equals("")) {
			print(value(value).assignedTo(javaType, fieldName));
		} else if (fieldName.equals("")) {
			print(value(value).assignedTo(variableName));
		} else {
			print(value(value).setTo(variableName, fieldName));
		}
	}
	
	private void handleArray(String variableName, String fieldName, String javaType, IValue fieldValue) throws DebugException {
		IVariable[] variables = fieldValue.getVariables();

		String arrayType = javaType.replaceFirst("\\[\\]", "");
		
		if (variableName.equals("")) {
			print(arrayConstructor(javaType, variables.length).assignedTo(javaType, fieldName));
		} else if (fieldName.equals("")) {
			print(arrayConstructor(javaType, variables.length).assignedTo(variableName));
		} else {
			print(arrayConstructor(javaType, variables.length).setTo(variableName, fieldName));
		}

		for (int i=0; i<variables.length; i++) {
			if (variableName.equals("")) {
				handleTypes(arrayIndex("", fieldName, i).toString(), "", arrayType, variables[i].getValue());
			} else if (fieldName.equals("")) {
				handleTypes(arrayIndex("", variableName, i).toString(), "", arrayType, variables[i].getValue());
			} else {
				handleTypes(arrayIndex(variableName, fieldName, i).toString(), "", arrayType, variables[i].getValue());
			}
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
