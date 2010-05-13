package atarih.variabledumper.util;

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
}
