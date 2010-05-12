package atarih.variabledumper.util;

import java.util.Arrays;


public class OutputUtils {

	public static Output defaultConstructor(String javaType) {
	    return constructor(javaType, "");
	}
	
	public static Output constructor(String javaType, String value) {
		Output output = new Output();
		output.setValue("new "+ javaType + "(" + value + ")");
		return output;
	}

	public static Output arrayConstructor(String javaType, int size) {
		Output output = new Output();
		output.setValue("new "+ javaType.replaceFirst("\\[\\]", "[" + size + "]"));
		return output;
	}

	public static Output genericConstructor(String javaType, String value, String... genericTypes) {
		Output output = new Output();
		String generics = Arrays.toString(genericTypes).replace("[", "").replace("]", "");
		output.setValue("new "+ javaType + "<" + generics + ">(" + value + ")");
		return output;
	}
	
	public static Output genericConstructor(String javaType, String genericType) {
		Output output = new Output();
		output.setValue("new "+ javaType + "<" + genericType + ">()");
		return output;
	}
	
	public static Output arrayIndex(String variable, String fieldName, int index) {
		Output output = new Output();
		
		if (variable.equals("")) {
			output.setValue(fieldName+ "[" + index + "]");
		} else {
			output.setValue(variable + ".get" + Output.capitalize(fieldName) + "()[" + index + "]");
		}
		
		return output;
	}
	
	public static Output value(String value) {
		Output output = new Output();
		output.setValue(value);
		return output;
	}
}
