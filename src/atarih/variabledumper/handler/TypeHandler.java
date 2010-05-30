package atarih.variabledumper.handler;

import static atarih.variabledumper.ui.console.VariableDumperConsoleOutput.print;
import static atarih.variabledumper.util.OutputUtils.arrayIndex;
import static atarih.variabledumper.util.OutputUtils.defaultConstructor;
import static atarih.variabledumper.util.OutputUtils.value;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
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
import atarih.variabledumper.util.OutputUtils;
import atarih.variabledumper.util.ValueHelper;

public class TypeHandler {

    private IProgressMonitor monitor;

    private final Map<String, String> collectionTypes = new TreeMap<String, String>(); {
        collectionTypes.put("java.util.List", "java.util.ArrayList");
        collectionTypes.put("java.util.Set", "java.util.HashSet");
        collectionTypes.put("java.util.Collection", "java.util.ArrayList");
    }

    private TreeMap<String, String> mapVariables = new TreeMap<String, String>();

    public void handleVariable(String variableName, JDIVariable variable) throws DebugException, ClassNotFoundException {

        IValue value = variable.getValue();
        String javaType = value.getReferenceTypeName();

        if (!javaType.equals("null") || javaType.contains("$")) {
            javaType = variable.getReferenceTypeName().replaceAll("\\$", ".");
        }

        String fieldName = variable.getName();

        handleTypes(variableName, fieldName, javaType, value);
    }

    private void handleTypes(String variableName, String fieldName, String javaType, IValue value) throws DebugException, ClassNotFoundException {

        if (!monitor.isCanceled()) {

            String existingVariable = mapVariables.get(value.toString());

            if (existingVariable != null) {
                OutputUtils.outputValue(variableName, fieldName, javaType, existingVariable);
            } else {

                if (!value.getClass().equals(JDINullValue.class) && !value.getClass().equals(JDIPrimitiveValue.class) && !javaType.equals("java.lang.String")) {
                    if (variableName.equals("")) {
                        mapVariables.put(value.toString(), fieldName);

                    } else if (fieldName.equals("")) {
                        mapVariables.put(value.toString(), variableName);

                    } else {
                        mapVariables.put(value.toString(), variableName+Output.capitalize(fieldName.replaceAll("\\$", "")));
                    }
                }

                if (value.getClass().equals(JDINullValue.class)) {
                    handleNullValue(variableName, fieldName, javaType);

                } else if (value.getClass().equals(JDIPrimitiveValue.class) || javaType.equals("java.lang.String")) {
                    handlePrimitive(variableName, fieldName, javaType, value);

                } else if (ValueHelper.isWrapper(value.getReferenceTypeName())) {
                    handleWrapper(variableName, fieldName, javaType, value);

                } else if (javaType.equals("java.lang.Number") || javaType.equals("java.math.BigDecimal") || javaType.equals("java.math.BigInteger")) {
                    handleNumber(variableName, fieldName, javaType, value);

                } else if (javaType.equals("java.util.Date") || javaType.equals("java.sql.Date")) {
                    handleDate(variableName, fieldName, javaType, value);

                } else if (ValueHelper.isEnum(value)) {
                    handleEnum(variableName, fieldName, javaType, value);

                } else if (value.getClass().equals(JDIArrayValue.class)) {
                    handleArray(variableName, fieldName, javaType, value);
                    print(value(""));

                } else if (value.getClass().equals(JDIObjectValue.class) && (ValueHelper.isJavaUtilClass(javaType) && Collection.class.isAssignableFrom(Class.forName(value.getReferenceTypeName().substring(0, value.getReferenceTypeName().indexOf("<")))))) {
                    handleList(variableName, fieldName, javaType, value);
                    print(value(""));

                } else if (value.getClass().equals(JDIObjectValue.class) && (ValueHelper.isJavaUtilClass(javaType) && Map.class.isAssignableFrom(Class.forName(value.getReferenceTypeName().substring(0, value.getReferenceTypeName().indexOf("<")))))) {
                    handleMap(variableName, fieldName, javaType, value);
                    print(value(""));

                } else {
                    handleObject(variableName, fieldName, javaType, value);
                    print(value(""));
                }
            }
        } else {
            throw new DebugException(Status.CANCEL_STATUS);
        }
    }

    private void handleNullValue(String variableName, String fieldName, String javaType) {
        OutputUtils.outputValue(variableName, fieldName, javaType, "null");
    }

    private void handlePrimitive(String variableName, String fieldName, String javaType, IValue value) throws DebugException {
        String primitiveValue = ValueHelper.getPrimitiveValue(javaType, value);

        primitiveValue = "(" + javaType + ")" + primitiveValue;

        OutputUtils.outputValue(variableName, fieldName, javaType, primitiveValue);
    }

    private void handleWrapper(String variableName, String fieldName, String javaType, IValue value) throws DebugException {
        String wrapperValue = ValueHelper.getWrapperValue(value);

        OutputUtils.outputConstructor(variableName, fieldName, javaType, value.getReferenceTypeName(), wrapperValue);
    }

    private void handleNumber(String variableName, String fieldName, String javaType, IValue value) throws DebugException {
        StringReferenceImpl stringValue = (StringReferenceImpl) JDIReflectionUtils.invokeMethod(value, "toString", null);
        String numberValue = "\"" + stringValue.value() + "\"";

        OutputUtils.outputConstructor(variableName, fieldName, javaType, value.getReferenceTypeName(), numberValue);
    }

    private void handleDate(String variableName, String fieldName, String javaType, IValue value) throws DebugException {

        LongValueImpl timeMillis = (LongValueImpl) JDIReflectionUtils.invokeMethod(value, "getTime", null);
        String dateValue = timeMillis.value() + "L";

        OutputUtils.outputConstructor(variableName, fieldName, javaType, value.getReferenceTypeName(), dateValue);
    }

    private void handleEnum(String variableName, String fieldName, String javaType, IValue value) throws DebugException {
        String enumValue = null;

        for (IVariable variable : value.getVariables()) {
            if (variable.toString().equals("name")) {
                enumValue = variable.getValue().getValueString();
                break;
            }
        }

        OutputUtils.outputValue(variableName, fieldName, javaType, javaType + "." + enumValue);
    }

    private void handleArray(String variableName, String fieldName, String javaType, IValue value) throws DebugException, ClassNotFoundException {
        IVariable[] variables = value.getVariables();

        String arrayType = javaType.replaceFirst("\\[\\]", "");

        String localVariableName = OutputUtils.outputArrayConstructor(variableName, fieldName, javaType, variables.length);

        for (int i=0; i<variables.length; i++) {
            handleTypes("", localVariableName + i, arrayType, variables[i].getValue());
            print(value(localVariableName + i).assignedTo(arrayIndex(variableName, fieldName, i)));
        }
    }

    private void handleList(String variableName, String fieldName, String javaType, IValue value) throws DebugException, ClassNotFoundException {
        ArrayReferenceImpl arrayList = (ArrayReferenceImpl) JDIReflectionUtils.invokeMethod(value, "toArray", (Object[]) null);

        String referenceType = collectionTypes.get(javaType.substring(0, javaType.indexOf("<")));
        referenceType = referenceType != null ? referenceType : javaType;
        String genericType = javaType.substring(javaType.indexOf("<")).replace("<", "").replace(">", "");

        String localVariableName = OutputUtils.outputGenericConstructor(variableName, fieldName, genericType, javaType, referenceType);

        if (arrayList.length() > 0) {
            for (int i = 0; i < arrayList.getValues().size(); i++) {
                ObjectReferenceImpl item = (ObjectReferenceImpl) arrayList.getValues().get(i);

                IValue itemValue = JDIValue.createValue((JDIDebugTarget) value.getDebugTarget(), item);

                String referenceTypeName = itemValue.getReferenceTypeName();

                String varName = fieldName + "item" + i;
                handleTypes("", varName, referenceTypeName, itemValue);
                print(value(varName).addTo(localVariableName));
            }
        }
    }

    private void handleMap(String variableName, String fieldName, String javaType, IValue value) throws DebugException, ClassNotFoundException {

        String referenceType = value.getReferenceTypeName().replaceFirst("<K,V>", "");

        String comparator = "";
        if (referenceType.equals("java.util.TreeMap")) {
            ObjectReferenceImpl mapComparator = (ObjectReferenceImpl) JDIReflectionUtils.invokeMethod(value, "comparator", null);
            if (mapComparator != null) {
                comparator = defaultConstructor(mapComparator.type().toString()).toString();

                // should we do something about inner classes other than static ones?
                comparator = comparator.replace("$", ".");
            }
        }

        String genericType = javaType.substring(javaType.indexOf("<")).replace("<", "").replace(">", "");
        String genericKey = genericType.split(",")[0];
        String genericValue = genericType.split(",")[1];

        String localVariableName = OutputUtils.outputMapConstructor(variableName, fieldName, javaType, referenceType, genericKey, genericValue, comparator);

        JDIThread thread = JDIReflectionUtils.getUnderlyingThread(value);

        ObjectReferenceImpl entrySet = (ObjectReferenceImpl) JDIReflectionUtils.invokeMethod(value, "entrySet", null);
        ArrayReferenceImpl entryArray = (ArrayReferenceImpl) JDIReflectionUtils.invokeMethod(thread, entrySet, "toArray", (Object[]) null);

        if (entryArray.length() > 0) {
            for (int i=0; i<entryArray.getValues().size(); i++) {
                ObjectReferenceImpl entry = (ObjectReferenceImpl) entryArray.getValues().get(i);

                ObjectReferenceImpl refKey = (ObjectReferenceImpl) JDIReflectionUtils.invokeMethod(thread, entry, "getKey", (Object[]) null);
                IValue entryKey = JDIValue.createValue((JDIDebugTarget)value.getDebugTarget(), refKey);
                String keyVariableName = localVariableName + "key" + i ;
                handleTypes("", keyVariableName, genericKey, entryKey);

                ObjectReferenceImpl refValue = (ObjectReferenceImpl) JDIReflectionUtils.invokeMethod(thread, entry, "getValue", (Object[]) null);
                IValue entryValue = JDIValue.createValue((JDIDebugTarget)value.getDebugTarget(), refValue);
                String valueVariableName = localVariableName + "value" + i ;
                handleTypes("", valueVariableName, genericValue, entryValue);

                print(value(keyVariableName + ", " + valueVariableName).putTo(localVariableName));
            }
        }
    }

    private void handleObject(String variableName, String fieldName, String javaType, IValue value) throws DebugException, ClassNotFoundException {

        String localVariableName = null;
        IVariable outerObjectReference = isInnerClass(value);

        if (outerObjectReference != null) {
            String existingVariable = handleInnerClass(variableName, fieldName, javaType, outerObjectReference);

            localVariableName = OutputUtils.outputInnerConstructor(variableName, fieldName, javaType, value.getReferenceTypeName(), existingVariable);
        } else {
            localVariableName = OutputUtils.outputDefaultConstructor(variableName, fieldName, javaType);
        }

        JDIObjectValue objectValue = (JDIObjectValue) value;

        IVariable[] variables = objectValue.getVariables();

        if (variables != null && objectValue.getValueString().length() > 0) {
            if (!fieldName.equals("") && !variableName.equals("")) {
                print(value(localVariableName).setTo(variableName, fieldName));
            }
        }

        for (IVariable variable : variables) {
            if (variable instanceof JDIVariable) {
                if (!((JDIVariable)variable).isSynthetic()) {
                    if ((!((JDIVariable) variable).isFinal() || !((JDIVariable) variable).isStatic())) {
                        handleVariable(localVariableName, (JDIVariable) variable);
                    }
                }
            }
        }
    }

    private String handleInnerClass(String variableName, String fieldName, String javaType, IVariable outerObjectReference) throws DebugException, ClassNotFoundException {
        IValue value = outerObjectReference.getValue();

        String existingVariable = mapVariables.get(value.toString());

        if (existingVariable == null) {

            String outerObjectName = (variableName.equals("") ? fieldName : variableName+Output.capitalize(fieldName)).concat(outerObjectReference.getName()).replaceAll("\\$", "");

            handleTypes("", outerObjectName, ((JDIVariable) outerObjectReference).getReferenceTypeName(), value);
            existingVariable = mapVariables.get(value.toString());
        } else {
            existingVariable = "()";
        }

        return existingVariable;
    }

    private IVariable isInnerClass(IValue value) throws DebugException {
        IVariable variableReference = null;
        for (IVariable variable : value.getVariables()) {
            if (variable.getName().contains("this$")) {
                variableReference = variable;
                break;
            }
        }
        return variableReference;
    }

    public IProgressMonitor getMonitor() {
        return monitor;
    }

    public void setMonitor(IProgressMonitor pm) {
        this.monitor = pm;
    }
}
