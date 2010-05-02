package atarih.variabledumper.util;

public class Output {

	private String value;

	public Output assignedTo(String javaType, String variableName) {
		this.value = javaType + " " + variableName + " = " + value + ";";
		
		return this;
	}
	
	public Output assignedTo(String variableName) {
		return assignedTo("", variableName);
	}
	
	public Output setTo(String variableName, String fieldName) {
		this.value = variableName + ".set" + capitalize(fieldName) + "(" + value + ");";
		
		return this;
	}
	
	protected void setValue(String value) {
    	this.value = value;
    }
	
	private static String capitalize(String fieldName) {
		return fieldName.substring(0, 1).toUpperCase().concat(fieldName.substring(1, fieldName.length()));
	}
	
	@Override
    public String toString() {
	    return value;
    }
}