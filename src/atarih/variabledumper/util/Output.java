package atarih.variabledumper.util;

public class Output {

	private String value;

	public Output assignedTo(String variableName) {
		return assignedTo("", variableName);
	}

	public Output assignedTo(String javaType, String variableName) {
		this.value = javaType + " " + variableName + " = " + this.value + ";";
		return this;
	}
	
	public Output assignedTo(String javaType, String genericType, String variableName) {
		this.value = javaType + "<" + genericType + "> " + variableName + " = " + this.value + ";";
		return this;
	}

	public Output assignedTo(Output output) {
		return assignedTo(output.value);
	}
	
	public Output setTo(String variableName, String fieldName) {
		this.value = variableName + ".set" + capitalize(fieldName) + "(" + value + ");";
		return this;
	}

	public Output addTo(String variableName) {
		this.value = variableName + ".add(" + this.value + ");";
		return this;
	}

	public Output putTo(String variableName) {
		this.value = variableName + ".put(" + this.value + ")";
		return this;
	}
	
	public static String capitalize(String fieldName) {
		return fieldName.substring(0, 1).toUpperCase().concat(fieldName.substring(1, fieldName.length()));
	}

	protected void setValue(String value) {
    	this.value = value;
    }
	
	@Override
    public String toString() {
	    return value;
    }
}