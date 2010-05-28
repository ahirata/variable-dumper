package atarih.variabledumper.ui.console;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class VariableDumperConsoleOutput {

    public static MessageConsole findConsole(String name) {
        MessageConsole messageConsole = null;

        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager conMan = plugin.getConsoleManager();
        IConsole[] consoles = conMan.getConsoles();

        for (IConsole existingConsole : consoles) {
            if (name.equals(existingConsole.getName())) {
                messageConsole = (MessageConsole) existingConsole;
                break;
            }
        }

        if (messageConsole == null) {
            messageConsole = new MessageConsole(name, null);
            conMan.addConsoles(new IConsole[]{messageConsole});
        }

        return messageConsole;
    }

    public static void print(Object output) {
        // TODO - check if this isn't gonna lead us to a memory leak...
        MessageConsole console = findConsole("variable-dumper-console");
        console.activate();
        MessageConsoleStream out = console.newMessageStream();
        out.println(output.toString());
    }
}

