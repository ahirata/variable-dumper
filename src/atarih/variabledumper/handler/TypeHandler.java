package atarih.variabledumper.handler;

import static atarih.variabledumper.ui.console.VariableDumperConsoleOutput.print;
import static atarih.variabledumper.util.OutputUtils.arrayConstructor;
import static atarih.variabledumper.util.OutputUtils.arrayIndex;
import static atarih.variabledumper.util.OutputUtils.constructor;
import static atarih.variabledumper.util.OutputUtils.defaultConstructor;
import static atarih.variabledumper.util.OutputUtils.genericConstructor;
import static atarih.variabledumper.util.OutputUtils.value;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

import atarih.variabledumper.util.JDIReflectionUtils;
import atarih.variabledumper.util.Output;

public class TypeHandler {

	private Set<String> TYPES = new TreeSet<String>(Arrays.asList(new String[] {
			"java.lang.Boolean", 
			"java.lang.Byte", 
			"java.lang.Character",
			"java.lang.Short", 
			"java.lang.Integer", 
			"java.lang.Long", 
			"java.lang.Float", 
			"java.lang.Double"
	}));
	
	private Map<String, String> COLLECTION_TYPES = new TreeMap<String, String>(); {
		COLLECTION_TYPES.put("java.util.List", "java.util.ArrayList");
		COLLECTION_TYPES.put("java.util.Set", "java.util.HashSet");
		COLLECTION_TYPES.put("java.util.Collection", "java.util.ArrayList");
	}
	
	private TreeMap<String, String> mapVariables = new TreeMap<String, String>();
	
	public void handleVariable(String variableName, JDIVariable variable) throws DebugException, ClassNotFoundException {
		IValue value = variable.getValue();
		String javaType = variable.getValue().getReferenceTypeName();
		if (!javaType.equals("null") || javaType.contains("$")) {
			javaType = variable.getReferenceTypeName();
		}
		String fieldName = variable.getName();

		handleTypes(variableName, fieldName, javaType, value);
	}
	
	private void handleTypes(String variableName, String fieldName, String javaType, IValue value) throws DebugException, ClassNotFoundException {
		
		String existingVariable = mapVariables.get(value.toString());
		
		if (existingVariable != null) {
			outputValue(variableName, fieldName, javaType, existingVariable);
		} else {
			// hold a cache except for null values...
			if (!value.getClass().equals(JDINullValue.class)) {
				if (variableName.equals("")) {
					mapVariables.put(value.toString(), fieldName);

				} else if (fieldName.equals("")) {
					mapVariables.put(value.toString(), variableName);

				} else {
					mapVariables.put(value.toString(), variableName+Output.capitalize(fieldName));
				}
			}

			if (value.getClass().equals(JDINullValue.class)) {
				handleNullValue(variableName, fieldName, javaType);
				
			} else if (value.getClass().equals(JDIPrimitiveValue.class) || javaType.equals("java.lang.String")) {
				handlePrimitive(variableName, fieldName, javaType, value);
				
			} else if (isWrapper(value.getReferenceTypeName())) {
				handleWrapper(variableName, fieldName, javaType, value);
				
			} else if (javaType.equals("java.lang.Number") || javaType.equals("java.math.BigDecimal") || javaType.equals("java.math.BigInteger")) { 
				handleNumber(variableName, fieldName, javaType, value);
				
			} else if (javaType.equals("java.util.Date") || javaType.equals("java.sql.Date")) {
				handleDate(variableName, fieldName, javaType, value);
				
			} else if (isEnum(value)) {
				handleEnum(variableName, fieldName, javaType, value);
				
			} else if (value.getClass().equals(JDIArrayValue.class)) {
				handleArray(variableName, fieldName, javaType, value);
				print(value(""));
				
			} else if (value.getClass().equals(JDIObjectValue.class) && (this.isJavaUtilClass(javaType) && Collection.class.isAssignableFrom(Class.forName(value.getReferenceTypeName().substring(0, value.getReferenceTypeName().indexOf("<")))))) {
				handleList(variableName, fieldName, javaType, value);
				print(value(""));
				
			} else if (value.getClass().equals(JDIObjectValue.class) && (this.isJavaUtilClass(javaType) && Map.class.isAssignableFrom(Class.forName(value.getReferenceTypeName().substring(0, value.getReferenceTypeName().indexOf("<")))))) {
				handleMap(variableName, fieldName, javaType, value);
				print(value(""));
				
			} else {
				handleObject(variableName, fieldName, javaType, value);
				print(value(""));
			}
		}   
    }
	
	private void handleNullValue(String variableName, String fieldName, String javaType) {
		outputValue(variableName, fieldName, javaType, "null");
    }
	
	private void handlePrimitive(String variableName, String fieldName, String javaType, IValue value) throws DebugException {
		String primitiveValue = getPrimitiveValue(javaType, value);

		primitiveValue = "(" + javaType + ")" + primitiveValue;
		
		outputValue(variableName, fieldName, javaType, primitiveValue);
	}

	private void handleWrapper(String variableName, String fieldName, String javaType, IValue value) throws DebugException {
		String wrapperValue = getWrapperValue(value);
		
		outputConstructor(variableName, fieldName, javaType, value.getReferenceTypeName(), wrapperValue);
	}

	private void handleNumber(String variableName, String fieldName, String javaType, IValue value) throws DebugException {
		StringReferenceImpl stringValue = (StringReferenceImpl) JDIReflectionUtils.invokeMethod(value, "toString", null);
		String numberValue = "\"" + stringValue.value() + "\"";
		
		outputConstructor(variableName, fieldName, javaType, value.getReferenceTypeName(), numberValue);
	}
	
	private void handleDate(String variableName, String fieldName, String javaType, IValue value) throws DebugException {
		
		LongValueImpl timeMillis = (LongValueImpl) JDIReflectionUtils.invokeMethod(value, "getTime", null);
		String dateValue = timeMillis.value() + "L";
		
		outputConstructor(variableName, fieldName, javaType, value.getReferenceTypeName(), dateValue);
	}
	
	private void handleEnum(String variableName, String fieldName, String javaType, IValue value) throws DebugException {
		String enumValue = null;
		
		for (IVariable variable : value.getVariables()) {
			if (variable.toString().equals("name")) {
				enumValue = variable.getValue().getValueString();
				break;
			}
		}
		
		outputValue(variableName, fieldName, javaType, javaType + "." + enumValue);
	}
	
	// TODO - refactor
	private void handleArray(String variableName, String fieldName, String javaType, IValue value) throws DebugException, ClassNotFoundException {
		IVariable[] variables = value.getVariables();

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

	// TODO - refactor
	private void handleList(String variableName, String fieldName, String javaType, IValue value) throws DebugException, ClassNotFoundException {
		ArrayReferenceImpl arrayList = (ArrayReferenceImpl) JDIReflectionUtils.invokeMethod(value, "toArray", (Object[]) null);

		for (int i = 0; i < arrayList.getValues().size(); i++) {
			ObjectReferenceImpl item = (ObjectReferenceImpl) arrayList.getValues().get(i);
			IValue itemValue = JDIValue.createValue((JDIDebugTarget) value.getDebugTarget(), item);
			
			String referenceTypeName = itemValue.getReferenceTypeName();
			if (i == 0) {
				String typeImpl = COLLECTION_TYPES.get(javaType.substring(0, javaType.indexOf("<")));
				typeImpl = typeImpl != null ? typeImpl : javaType;
				String genericType = javaType.substring(javaType.indexOf("<")).replace("<", "").replace(">", "");
				print(genericConstructor(typeImpl, genericType).assignedTo(javaType, fieldName));				
			}
			
			String varName = fieldName + "item" + i;
			handleTypes("", varName, referenceTypeName, itemValue);
			print(value(varName).addTo(fieldName));
		}
	}
	
	// TODO - refactor
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
					
					String genericType = javaType.substring(javaType.indexOf("<")).replace("<", "").replace(">", "");
					
					genericKey = genericType.split(",")[0];
					genericValue = genericType.split(",")[1];
					
					String javaTypeImpl = value.getReferenceTypeName().replaceFirst("<K,V>", "");
					
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
						print(genericConstructor(javaTypeImpl, comparator, genericKey, genericValue).assignedTo(javaType, localVariableName));
						
					} else if (fieldName.equals("")) {
						localVariableName = variableName;
						print(genericConstructor(javaTypeImpl, comparator, genericKey, genericValue).assignedTo(javaType, localVariableName));
						
					} else {
						localVariableName = variableName+Output.capitalize(fieldName);
						
						print(genericConstructor(javaTypeImpl, comparator, genericKey, genericValue).assignedTo(javaType, localVariableName));
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
	
	private void handleObject(String variableName, String fieldName, String javaType, IValue value) throws DebugException, ClassNotFoundException {
	    
	    String localVariableName = outputDefaultConstructor(variableName, fieldName, javaType);
	     
	    JDIObjectValue objectValue = (JDIObjectValue) value;
	    
	    IVariable[] variables = objectValue.getVariables();
	    
	    if (variables != null && objectValue.getValueString().length() > 0) {
	    	if (!fieldName.equals("") && !variableName.equals("")) {
	    		print(value(localVariableName).setTo(variableName, fieldName));
	    	}
	    }
	    
	    for (IVariable variable : variables) {
	    	
	    	if (variable instanceof JDIVariable) {
	    		handleVariable(localVariableName, (JDIVariable) variable);
	    	}
	    }
    }

	private String outputDefaultConstructor(String variableName, String fieldName, String javaType) {
	    String localVariableName = null;
	    
	    if (variableName.equals("")) {
	    	localVariableName = fieldName;
	    	print(defaultConstructor(javaType).assignedTo(javaType, localVariableName));
	    	
	    } else if (fieldName.equals("")) {
	    	localVariableName = variableName;
	    	print(defaultConstructor(javaType).assignedTo(localVariableName));
	    	
	    } else {
	    	localVariableName = variableName+Output.capitalize(fieldName);
	    	print(defaultConstructor(javaType).assignedTo(javaType, localVariableName));
	    }
	    
	    return localVariableName;
    }
	
	private void outputValue(String variableName, String fieldName, String javaType, String value) {
	    if (variableName.equals("")) {
			print(value(value).assignedTo(javaType, fieldName));
			
		} else if (fieldName.equals("")) {
			print(value(value).assignedTo(variableName));
			
		} else {
			print(value(value).setTo(variableName, fieldName));
			
		}
    }

	private void outputConstructor(String variableName, String fieldName, String receivingType, String referenceType, String value) throws DebugException {
	    if (variableName.equals("")) {
			print(constructor(referenceType, value).assignedTo(receivingType, fieldName));
			
		} else if (fieldName.equals("")){
			print(constructor(referenceType, value).assignedTo(variableName));
			
		} else {
			print(constructor(referenceType, value).setTo(variableName, fieldName));
		}
    }
	
	private String getPrimitiveValue(String javaType, IValue value) {
	    String primitiveValue = value.toString();
		 
		if (javaType.equals("char")) {
			primitiveValue = "'" + primitiveValue + "'";
		} else  if (javaType.equals("long")) {
			primitiveValue += "L";

		} else if (javaType.equals("float")) {
			primitiveValue += "F";

		} else if (javaType.equals("double")) {
			primitiveValue += "D";

		}
	    return primitiveValue;
    }
	
	private boolean isWrapper(String type) {
		return TYPES.contains(type); 
	}
	
	private String getWrapperValue(IValue value) throws DebugException {
		String wrapperValue = null;
		
		for (IVariable variable : value.getVariables()) {
			if (variable instanceof JDIVariable && variable.getName().equals("value")) {
				if (value.getReferenceTypeName().equals("java.lang.Character")) {
					wrapperValue = "'" + variable.getValue() + "'";

				} else {
					wrapperValue = "\"" + variable.getValue() + "\"";
				}
				
				break;	
			}
		}
		
		return wrapperValue;
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
	
	private boolean isJavaUtilClass(String clazz) {
		return clazz.startsWith("java.util");
	}
}
