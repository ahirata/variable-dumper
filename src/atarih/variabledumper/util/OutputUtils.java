package atarih.variabledumper.util;

import org.eclipse.debug.core.DebugException;

public class OutputUtils {

	public static Output defaultConstructor(String javaType) throws DebugException {
	    return constructor(javaType, "");
	}
	
	public static Output constructor(String javaType, String value) {
		Output output = new Output();
		
		output.setValue("new "+ javaType + "(" + value + ")");
		
		return output;
	}

	public static Output arrayConstructor(String javaType, int size) {
		Output output = new Output();
		
		output.setValue("new "+ javaType + "[" + size + "]");
		
		return output;
	}

	public static Output value(String value) {
		Output output = new Output();
		
		output.setValue(value);
		
		return output;
	}
	
	public static void print(Object output) {
		System.out.println(output.toString());
	}
}
