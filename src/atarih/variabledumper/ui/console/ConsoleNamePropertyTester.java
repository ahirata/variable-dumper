package atarih.variabledumper.ui.console;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.console.IConsole;

public class ConsoleNamePropertyTester extends PropertyTester {

    /* (non-Javadoc)
     * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
     */
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        IConsole console = (IConsole) receiver;
        String name = console.getName();
        return name != null ? name.equals(expectedValue) : false;
    }
}
