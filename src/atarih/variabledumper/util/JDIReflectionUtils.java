package atarih.variabledumper.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdi.internal.ClassTypeImpl;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;

// TODO - must do some major refactoring...
public class JDIReflectionUtils {

    @SuppressWarnings("unchecked")
    public static Method getMethod(ClassTypeImpl clazz, String methodName, Object[] args) throws DebugException {
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
                e.printStackTrace();
                throw new DebugException(new Status(IStatus.ERROR, "variable-dumper", e.getMessage()));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new DebugException(new Status(IStatus.ERROR, "variable-dumper", e.getMessage()));
            }
        }
        return method;
    }

    public static Object invokeMethod(IValue value, String methodName, Object[] values) throws DebugException {

        JDIThread jdiThread = getUnderlyingThread(value);
        JDIObjectValue objectValue = null;

        objectValue = (JDIObjectValue) value;

        ObjectReference object = objectValue.getUnderlyingObject();

        Object result = invokeMethod(jdiThread, object, methodName, values);

        return result;
    }

    @SuppressWarnings("unchecked")
    public static Object invokeMethod(JDIThread jdiThread, ObjectReference object, String methodName, Object[] values) throws DebugException {
        Method method = getMethod((ClassTypeImpl) object.referenceType(), methodName, values);

        Class<?>[] reflectTypes = new Class[] {
                ClassType.class,
                ObjectReference.class,
                Method.class,
                List.class,
                boolean.class
        };

        List valuesList = values == null ? new ArrayList() : Arrays.asList(values);

        Object[] reflectObjects = new Object[] {
                null, object, method, valuesList,  Boolean.FALSE
        };

        Object result = null;

        try {
            result = ReflectionUtils.invokeMethod(jdiThread, "invokeMethod", reflectTypes, reflectObjects);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new DebugException(new Status(IStatus.ERROR, "variable-dumper", e.getMessage()));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new DebugException(new Status(IStatus.ERROR, "variable-dumper", e.getMessage()));
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new DebugException(new Status(IStatus.ERROR, "variable-dumper", e.getMessage()));
        }
        return result;
    }

    public static JDIThread getUnderlyingThread(IValue value) throws DebugException {
        IJavaThread thread = JDIModelPresentation.getEvaluationThread((IJavaDebugTarget)value.getDebugTarget());

        IThread iThread = thread.getTopStackFrame().getThread();

        return (JDIThread) iThread;
    }
}
