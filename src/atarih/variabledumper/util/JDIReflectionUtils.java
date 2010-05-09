package atarih.variabledumper.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdi.internal.ClassTypeImpl;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.core.model.JDIVariable;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;

// TODO - must do some major refactoring...
public class JDIReflectionUtils {

	public static Method getMethod(ClassTypeImpl clazz, String methodName, Object[] args) {
		Method method = null;
		
	    for (Method methodItem : (List<Method>) clazz.methodsByName(methodName)) {
	    	try {
	    		if ((args == null || args.length == 0) && methodItem.argumentTypes().isEmpty()) {
	    			method = methodItem;
	    			break;
	    		} else  if (args != null && args.length == methodItem.argumentTypes().size()) {
	    			method = methodItem;
		    		for (int i=0; i<args.length; i++) {
		    			if (!args[i].getClass().isAssignableFrom(Class.forName(methodItem.argumentTypes().get(i).toString()))) {
		    				method = null;
		    				break;
		    			}
		    		}
	    		}
            } catch (ClassNotLoadedException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            } catch (ClassNotFoundException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
		}
	    return method;
    }
	
	public static Object invokeMethod(JDIVariable field, String methodName, Object[] values) {
		
		JDIThread jdiThread = getUnderlyingThread(field);
		JDIObjectValue objectValue = null;
        
		try {
	        objectValue = (JDIObjectValue) field.getValue();
        } catch (DebugException e1) {
	        // TODO Auto-generated catch block
	        e1.printStackTrace();
        }
        ObjectReference object = objectValue.getUnderlyingObject();

		Object result = invokeMethod(jdiThread, object, methodName, values);
        
		return result;
	}

	public static Object invokeMethod(JDIThread jdiThread, ObjectReference object, String methodName, Object[] values) {
	    Method method = getMethod((ClassTypeImpl) object.referenceType(), methodName, values);
		
		Class<?>[] reflectTypes = new Class[] {
				ClassType.class,
				ObjectReference.class,
				Method.class,
				List.class,
				boolean.class
		};
		
		List valuesList = null;
		if (values == null) {
			valuesList = new ArrayList();
		} else {
			valuesList = Arrays.asList(values);
		}
		Object[] reflectObjects = new Object[] {
				null, object, method, valuesList,  Boolean.FALSE
		};
		
		
		Object result = null;
		
		try {
	        result = ReflectionUtils.invokeMethod(jdiThread, "invokeMethod", reflectTypes, reflectObjects);
        } catch (NoSuchMethodException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (IllegalAccessException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (InvocationTargetException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	    return result;
    }
	
	public static JDIThread getUnderlyingThread(JDIVariable object) {
		JDIStackFrame stackFrame = null;
        try {
	        stackFrame = (JDIStackFrame) ReflectionUtils.invokeMethod(object, "getStackFrame", (Class<?>[]) null, (Object[]) null);
        } catch (NoSuchMethodException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (IllegalAccessException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (InvocationTargetException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		return ((JDIThread)stackFrame.getThread());		
	}
}
