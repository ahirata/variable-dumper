package atarih.variabledumper.ui.actions;

import static atarih.variabledumper.ui.console.VariableDumperConsoleOutput.print;
import static atarih.variabledumper.util.OutputUtils.arrayConstructor;
import static atarih.variabledumper.util.OutputUtils.arrayIndex;
import static atarih.variabledumper.util.OutputUtils.constructor;
import static atarih.variabledumper.util.OutputUtils.defaultConstructor;
import static atarih.variabledumper.util.OutputUtils.genericConstructor;
import static atarih.variabledumper.util.OutputUtils.value;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdi.internal.ArrayReferenceImpl;
import org.eclipse.jdi.internal.LongValueImpl;
import org.eclipse.jdi.internal.ObjectReferenceImpl;
import org.eclipse.jdi.internal.StringReferenceImpl;
import org.eclipse.jdt.internal.debug.core.model.JDIArrayValue;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIPrimitiveValue;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.core.model.JDIValue;
import org.eclipse.jdt.internal.debug.core.model.JDIVariable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.internal.ViewPluginAction;

import atarih.variabledumper.util.JDIReflectionUtils;
import atarih.variabledumper.util.Output;

public class VariableDumperAction implements IViewActionDelegate {
	
	private static final List<String> TYPES = Arrays.asList(new String[] {
			"java.lang.Boolean", 
			"java.lang.Byte", 
			"java.lang.Character",
			"java.lang.Short", 
			"java.lang.Integer", 
			"java.lang.Long", 
			"java.lang.Float", 
			"java.lang.Double"
	});

	private static final Map<String, String> COLLECTION_TYPES = new TreeMap<String, String>();
	
	static {
		COLLECTION_TYPES.put("java.util.List", "java.util.ArrayList");
		COLLECTION_TYPES.put("java.util.Set", "java.util.HashSet");
		COLLECTION_TYPES.put("java.util.Collection", "java.util.ArrayList");
	}
	
	private static final List<String> MAP_TYPES = Arrays.asList(new String[] { 
			"java.util.Map"
	});
	
	// just a test...
	IWorkbenchWindow activeWindow = null;
	
	IViewPart fview = null;
	
	MessageConsole messageConsole = null;
	
	@Override
    public void init(IViewPart view) {
		// just a test...
		activeWindow = view.getSite().getWorkbenchWindow();
		
		view.getViewSite();
		view.getSite();
	}
	
	@Override
	@SuppressWarnings("restriction")
    public void run(IAction action) {
		// just a test...
		Shell shell = activeWindow.getShell();
		ViewPluginAction viewAction = (ViewPluginAction) action;

		TreeSelection treeSelection = (TreeSelection) viewAction.getSelection();
		Object elem = treeSelection.getFirstElement();
		
		if (elem instanceof JDIVariable) {
			JDIVariable fieldVariable = (JDIVariable) elem;
			
			try {
				analyzeVariable("", fieldVariable);	            
            } catch (DebugException e) {
	            e.printStackTrace();
            } catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
    }

	private void analyzeVariable(String variableName, JDIVariable field) throws DebugException, ClassNotFoundException {
		IValue value = field.getValue();
		String javaType = field.getJavaType().getName();
		String fieldName = field.getName();
		
		handleTypes(variableName, fieldName, javaType, value);
    }

	// TODO - under construction...
	private void handleMap(String variableName, String fieldName, String javaType, IValue value) {

		JDIThread thread = JDIReflectionUtils.getUnderlyingThread(value); 
	    
		ObjectReferenceImpl entrySet = (ObjectReferenceImpl) JDIReflectionUtils.invokeMethod(value, "entrySet", null);
		ArrayReferenceImpl entryArray = (ArrayReferenceImpl) JDIReflectionUtils.invokeMethod(thread, entrySet, "toArray", (Object[]) null);
		
		boolean initialized = false;
		String genericKey = null;
		String genericValue = null;
		String localVariableName = null;
		
		for (int i=0; i<entryArray.getValues().size(); i++) {
			ObjectReferenceImpl entry = (ObjectReferenceImpl) entryArray.getValues().get(i);
			ObjectReferenceImpl refKey = (ObjectReferenceImpl) JDIReflectionUtils.invokeMethod(thread, entry, "getKey", (Object[]) null);
			ObjectReferenceImpl refValue = (ObjectReferenceImpl) JDIReflectionUtils.invokeMethod(thread, entry, "getValue", (Object[]) null);
			
			IValue entryKey = JDIValue.createValue((JDIDebugTarget)value.getDebugTarget(), refKey);
			IValue entryValue = JDIValue.createValue((JDIDebugTarget)value.getDebugTarget(), refValue);
			
			if (!initialized) {
				try { 
					genericKey = entryKey.getReferenceTypeName();
					genericValue = entryValue.getReferenceTypeName();
					
					String javaTypeImpl = ((JDIObjectValue)value).getReferenceTypeName().replaceFirst("<K,V>", "");
					
					String comparator = "";
					if (javaTypeImpl.equals("java.util.TreeMap")) {
						ObjectReferenceImpl mapComparator = (ObjectReferenceImpl) JDIReflectionUtils.invokeMethod(value, "comparator", null);
						if (mapComparator != null) {
							comparator = defaultConstructor(mapComparator.type().toString()).toString();
							
							// should we do something about inner classes other than static ones?
							comparator = comparator.replace("$", ".");
						}
					}
					if (variableName.equals("")) {
						localVariableName = fieldName;
						print(genericConstructor(javaTypeImpl, comparator, genericKey, genericValue).assignedTo(javaType, genericKey, genericValue, localVariableName));
						
					} else if (fieldName.equals("")) {
						localVariableName = variableName;
						print(genericConstructor(javaTypeImpl, comparator, genericKey, genericValue).assignedTo(javaType, genericKey, genericValue, localVariableName));
						
					} else {
						localVariableName = variableName+Output.capitalize(fieldName);
						
						print(genericConstructor(javaTypeImpl, comparator, genericKey, genericValue).assignedTo(javaType, genericKey, genericValue, localVariableName));
						print(value(localVariableName).setTo(variableName, fieldName));
					}
					
	                initialized = true;
                } catch (DebugException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                }
			} 
            
			String keyVariableName = localVariableName + "key" + i ;
			String valueVariableName = localVariableName + "value" + i ;

			try {
	            handleTypes("", keyVariableName, genericKey, entryKey);
	            handleTypes("", valueVariableName, genericValue, entryValue);
            } catch (DebugException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            } catch (ClassNotFoundException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
			
			print(value(keyVariableName + ", " + valueVariableName).putTo(localVariableName));
		}
	}

	private void handleTypes(String variableName, String fieldName, String javaType, IValue value) throws DebugException, ClassNotFoundException {
		
	    if (value.getClass().equals(JDINullValue.class)) {
	    	handleNullValue(variableName, fieldName, javaType);
			
		} else if (isWrapper(javaType)) {
			handleWrapper(variableName, fieldName, javaType, value);
			
		} else if (value.getClass().equals(JDIPrimitiveValue.class) || javaType.equals("java.lang.String")) {
			handlePrimitive(variableName, fieldName, javaType, value);
			
		} else if (javaType.equals("java.math.BigDecimal") || javaType.equals("java.math.BigInteger")) { 
			handleNumber(variableName, fieldName, javaType, value);
			
		} else if (javaType.equals("java.util.Date")) {
			handleDate(variableName, fieldName, javaType, value);
			
		} else if (value.getClass().equals(JDIArrayValue.class)) {
			handleArray(variableName, fieldName, javaType, value);
			print(value(""));
			
		} else if (isEnum(value)) {
			handleEnum(variableName, fieldName, javaType, value);
		    				
		} else if (value.getClass().equals(JDIObjectValue.class) && (this.isJavaInternalClass(javaType) && Collection.class.isAssignableFrom(Class.forName(javaType)))) {
			handleList(variableName, fieldName, javaType, value);
			print(value(""));
			
		} else if (value.getClass().equals(JDIObjectValue.class) && (this.isJavaInternalClass(javaType) && Map.class.isAssignableFrom(Class.forName(javaType)))) {
			handleMap(variableName, fieldName, javaType, value);
			print(value(""));
			
		} else {
			handleObject(variableName, fieldName, javaType, value);
			print(value(""));
			
		}
    }

	private void handleNumber(String variableName, String fieldName, String javaType, IValue value) {
		StringReferenceImpl stringValue = (StringReferenceImpl) JDIReflectionUtils.invokeMethod(value, "toString", null);
		String numberValue = "\"" + stringValue.value() + "\"";
		
		if (variableName.equals("")) {
			print(constructor(javaType, numberValue).assignedTo(javaType, fieldName));
		} else if (fieldName.equals("")){
			print(constructor(javaType, numberValue).assignedTo(variableName));
		} else {
			print(constructor(javaType, numberValue).setTo(variableName, fieldName));
		}
	}
	private void handleDate(String variableName, String fieldName, String javaType, IValue value) {
		
		org.eclipse.jdi.internal.LongValueImpl timeMillis = (LongValueImpl) JDIReflectionUtils.invokeMethod(value, "getTime", null);
		String dateValue = timeMillis.value() + "L";
		
		if (variableName.equals("")) {
			print(constructor(javaType, dateValue).assignedTo(javaType, fieldName));
		} else if (fieldName.equals("")){
			print(constructor(javaType, dateValue).assignedTo(variableName));
		} else {
			print(constructor(javaType, dateValue).setTo(variableName, fieldName));
		}
	}
	
	private void handleEnum(String variableName, String fieldName, String javaType, IValue value) throws DebugException {
		String enumValue = null;
		
		for (IVariable variable : value.getVariables()) {
			if (variable.toString().equals("name")) {
				enumValue = variable.getValue().getValueString();
				break;
			}
		}
		
		if (variableName.equals("")) {
	    	print(value(javaType + "." + enumValue).assignedTo(javaType, fieldName));
	    } else if (fieldName.equals("")) {
	    	print(value(javaType + "." + enumValue).assignedTo(variableName));
	    } else {
	    	print(value(javaType + "." + enumValue).setTo(variableName, fieldName));
	    }
	}
	
	private void handleObject(String variableName, String fieldName, String javaType, IValue value) throws DebugException, ClassNotFoundException {
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
				if (objectValue.getReferenceTypeName().equals("java.lang.Character")) {
					value = "'" + variable.getValue() + "'";

				} else {
					value = "\"" + variable.getValue() + "\"";
				}
				break;	
			}
		}
		return value;
	}

	private void handlePrimitive(String variableName, String fieldName, String javaType, IValue fieldValue) throws DebugException {
		String value = fieldValue.toString();
		 
		if (javaType.equals("char")) {
			value = "'" + value + "'";
		} else  if (javaType.equals("long")) {
			value += "L";

		} else if (javaType.equals("float")) {
			value += "F";

		} else if (javaType.equals("double")) {
			value += "D";

		}
		
		value = "(" + javaType + ")" + value;
		if (variableName.equals("")) {
			print(value(value).assignedTo(javaType, fieldName));
		} else if (fieldName.equals("")) {
			print(value(value).assignedTo(variableName));
		} else {
			print(value(value).setTo(variableName, fieldName));
		}
	}
	
	private void handleArray(String variableName, String fieldName, String javaType, IValue fieldValue) throws DebugException, ClassNotFoundException {
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
			String localArrayVariableName = null;
			if (variableName.equals("")) {
				localArrayVariableName = fieldName + i;
				
			} else if (fieldName.equals("")) {
				localArrayVariableName = variableName + i;

			} else {
				localArrayVariableName = variableName + Output.capitalize(fieldName) + i;
				
			}
			handleTypes("", localArrayVariableName, arrayType, variables[i].getValue());
			print(value(localArrayVariableName).assignedTo(arrayIndex(variableName, fieldName, i)));
		}
	}

	private void handleList(String variableName, String fieldName, String javaType, IValue value) throws DebugException, ClassNotFoundException {
		ArrayReferenceImpl arrayList = (ArrayReferenceImpl) JDIReflectionUtils.invokeMethod(value, "toArray", (Object[]) null);
		
		for (int i = 0; i < arrayList.getValues().size(); i++) {
			ObjectReferenceImpl item = (ObjectReferenceImpl) arrayList.getValues().get(i);
			IValue itemValue = JDIValue.createValue((JDIDebugTarget) value.getDebugTarget(), item);
			
			String referenceTypeName = itemValue.getReferenceTypeName();
			if (i == 0) {
				String typeImpl = COLLECTION_TYPES.containsKey(javaType) ? COLLECTION_TYPES.get(javaType) : javaType;
				print(genericConstructor(typeImpl, referenceTypeName).assignedTo(javaType, referenceTypeName, fieldName));					
			}
			
			String varName = "item" + i;
			handleTypes("", varName, referenceTypeName, itemValue);
			print(value(varName).addTo(fieldName));
		}
	}
	
	private boolean isWrapper(String type) {
		return TYPES.contains(type); 
	}
	
	private boolean isEnum(IValue value) throws DebugException {
		boolean isEnum = false;
		
		for (IVariable variable : value.getVariables()) {
			if (variable.toString().equals("ENUM$VALUES")) {
				isEnum = true;
				break;
			}
		}
		
		return isEnum;
	}	
	
	private boolean isJavaInternalClass(String clazz) {
		return clazz.startsWith("java.");
	}
	
	@Override
    public void selectionChanged(IAction action, ISelection selection) {
	    // TODO Auto-generated method stub
    }
}
