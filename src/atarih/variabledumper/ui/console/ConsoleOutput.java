package atarih.variabledumper.ui.console;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

// TODO - need to check how we can add a listener to kill the console 
// when the debug ends.
public class ConsoleOutput {
	
	private static MessageConsole console;
	
	private static MessageConsole findConsole(String name) {
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
		if (console == null) {
			console = findConsole("variable-dumper-console");
		}
		console.activate();
		MessageConsoleStream out = console.newMessageStream();
		out.println(output.toString());
	}
}
