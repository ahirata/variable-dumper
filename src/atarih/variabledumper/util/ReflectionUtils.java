package atarih.variabledumper.util;

import java.lang.reflect.InvocationTargetException;

public class ReflectionUtils {
	public static <T> Object invokeMethod(T object, String methodName, Class<?>[] types, Object[] values) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
	    java.lang.reflect.Method method = object.getClass().getDeclaredMethod(methodName, types);
	    method.setAccessible(true);
	    return method.invoke(object, values);
    }
}
