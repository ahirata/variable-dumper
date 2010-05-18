package atarih.variabledumper.util;

public class Output {

	private String value;

	public Output assignedTo(String variableName) {
		return assignedTo("", variableName);
	}

	public Output assignedTo(String javaType, String variableName) {
		this.value = MessageUtils.getFormattedString("assignment.to", javaType, variableName, this.value);
		return this;
	}
	
	public Output assignedTo(String javaType, String genericType, String variableName) {
		this.value = MessageUtils.getFormattedString("assignment.to.generic", javaType, genericType, variableName, this.value); 
		return this;
	}
	
	public Output assignedTo(String javaType, String genericKey, String genericValue, String variableName) {
		this.value = MessageUtils.getFormattedString("assignment.to.map.declaration", javaType, genericKey, genericValue, variableName, this.value);
		return this;
	}
	
	public Output assignedTo(Output output) {
		return assignedTo(output.value);
	}
	
	public Output setTo(String variableName, String fieldName) {
		this.value = MessageUtils.getFormattedString("set.to", variableName, capitalize(fieldName), this.value); 
		return this;
	}

	public Output addTo(String variableName) {
		this.value = MessageUtils.getFormattedString("add.to", variableName, this.value);
		return this;
	}

	public Output putTo(String variableName) {
		this.value = MessageUtils.getFormattedString("put.to", variableName, this.value); 
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