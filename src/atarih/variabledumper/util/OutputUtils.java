package atarih.variabledumper.util;

import static atarih.variabledumper.ui.console.VariableDumperConsoleOutput.print;

import java.util.Arrays;

public class OutputUtils {

    public static Output defaultConstructor(String javaType) {
        return constructor(javaType, "");
    }

    public static Output constructor(String javaType, String value) {
        Output output = new Output();
        output.setValue(MessageUtils.getFormattedString("constructor", javaType, value));
        return output;
    }

    public static Output arrayConstructor(String javaType, int size) {
        Output output = new Output();
        output.setValue(MessageUtils.getFormattedString("array.constructor", javaType.replaceFirst("\\[\\]", ""), String.valueOf(size)));
        return output;
    }

    public static Output genericConstructor(String javaType, String value, String... genericTypes) {
        Output output = new Output();
        String generics = Arrays.toString(genericTypes).replace("[", "").replace("]", "");
        output.setValue(MessageUtils.getFormattedString("generic.constructor", javaType, generics, value));
        return output;
    }

    public static Output genericConstructor(String javaType, String genericType) {
        Output output = new Output();
        output.setValue(MessageUtils.getFormattedString("generic.constructor", javaType, genericType, ""));
        return output;
    }

    public static Output arrayIndex(String variable, String fieldName, int index) {
        Output output = new Output();

        if (variable.equals("")) {
            output.setValue(MessageUtils.getFormattedString("array.index", fieldName, String.valueOf(index)));
        } else {
            output.setValue(MessageUtils.getFormattedString("array.index.variable", variable, Output.capitalize(fieldName), String.valueOf(index)));
        }

        return output;
    }

    public static Output value(String value) {
        Output output = new Output();
        output.setValue(value);
        return output;
    }

    public static void outputValue(String variableName, String fieldName, String javaType, String value) {
        if (variableName.equals("")) {
            print(value(value).assignedTo(javaType, fieldName));

        } else if (fieldName.equals("")) {
            print(value(value).assignedTo(variableName));

        } else {
            print(value(value).setTo(variableName, fieldName));

        }
    }

    public static String outputDefaultConstructor(String variableName, String fieldName, String javaType) {
        String localVariableName = outputConstructor(variableName, fieldName, javaType, javaType, "");

        return localVariableName;
    }

    public static String outputConstructor(String variableName, String fieldName, String receivingType, String referenceType, String value) {
        String localVariableName = null;
        if (variableName.equals("")) {
            localVariableName = fieldName;
            print(constructor(referenceType, value).assignedTo(receivingType, fieldName));

        } else if (fieldName.equals("")){
            localVariableName = variableName;
            print(constructor(referenceType, value).assignedTo(variableName));

        } else {
            localVariableName = variableName+Output.capitalize(fieldName);
            print(constructor(referenceType, value).setTo(variableName, fieldName));
        }
        return localVariableName;
    }

    public static String outputInnerConstructor(String variableName, String fieldName, String receivingType, String referenceType, String parent) {
        String localVariableName = null;
        Output constructor = defaultConstructor(referenceType.substring(referenceType.indexOf("$")+1));
        constructor.setValue(parent + "." + constructor.toString());

        if (variableName.equals("")) {
            localVariableName = fieldName;
            print(constructor.assignedTo(receivingType, localVariableName));

        } else if (fieldName.equals("")) {
            localVariableName = variableName;
            print(constructor.assignedTo(localVariableName));

        } else {
            localVariableName = variableName+Output.capitalize(fieldName);
            print(constructor.assignedTo(receivingType, localVariableName));
        }

        return localVariableName;
    }

    public static String outputArrayConstructor(String variableName, String fieldName, String javaType, int arrayLength) {
        String localVariableName = null;

        if (variableName.equals("")) {
            localVariableName = fieldName;
            print(arrayConstructor(javaType, arrayLength).assignedTo(javaType, fieldName));

        } else if (fieldName.equals("")) {
            localVariableName = variableName;
            print(arrayConstructor(javaType, arrayLength).assignedTo(variableName));

        } else {
            localVariableName = variableName+Output.capitalize(fieldName);

            print(arrayConstructor(javaType, arrayLength).assignedTo(javaType, localVariableName));
            print(value(localVariableName).setTo(variableName, fieldName));
        }

        return localVariableName;
    }

    public static String outputGenericConstructor(String variableName, String fieldName, String genericType, String receivingType, String referenceType) {
        String localVariableName = null;

        if (variableName.equals("")) {
            localVariableName = fieldName;
            print(genericConstructor(referenceType, genericType).assignedTo(receivingType, fieldName));

        } else if (fieldName.equals("")){
            localVariableName = variableName;
            print(genericConstructor(referenceType, genericType).assignedTo(variableName));

        } else {
            localVariableName = variableName+Output.capitalize(fieldName);
            print(genericConstructor(referenceType, genericType).assignedTo(receivingType, localVariableName));
            print(value(localVariableName).setTo(variableName, fieldName));
        }

        return localVariableName;
    }

    public static String outputMapConstructor(String variableName, String fieldName, String receivingType, String referenceType,
            String genericKey, String genericValue, String comparator) {

        String localVariableName = null;

        if (variableName.equals("")) {
            localVariableName = fieldName;
            print(genericConstructor(referenceType, comparator, genericKey, genericValue).assignedTo(receivingType, localVariableName));

        } else if (fieldName.equals("")) {
            localVariableName = variableName;
            print(genericConstructor(referenceType, comparator, genericKey, genericValue).assignedTo(receivingType, localVariableName));

        } else {
            localVariableName = variableName+Output.capitalize(fieldName);

            print(genericConstructor(referenceType, comparator, genericKey, genericValue).assignedTo(receivingType, localVariableName));
            print(value(localVariableName).setTo(variableName, fieldName));
        }

        return localVariableName;
    }

}
