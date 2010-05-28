package atarih.variabledumper.util;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIVariable;

public class ValueHelper {

    private static final Set<String> PRIMITIVE_TYPES = new TreeSet<String>(Arrays.asList(new String[] {
            "java.lang.Boolean",
            "java.lang.Byte",
            "java.lang.Character",
            "java.lang.Short",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Float",
            "java.lang.Double"
    }));

    public static String getPrimitiveValue(String javaType, IValue value) {
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

    public static boolean isWrapper(String type) {
        return PRIMITIVE_TYPES.contains(type);
    }

    public static String getWrapperValue(IValue value) throws DebugException {
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

    public static boolean isEnum(IValue value) throws DebugException {
        boolean isEnum = false;

        for (IVariable variable : value.getVariables()) {
            if (variable.toString().equals("ENUM$VALUES")) {
                isEnum = true;
                break;
            }
        }

        return isEnum;
    }

    public static boolean isJavaUtilClass(String clazz) {
        return clazz.startsWith("java.util");
    }
}
