package nikita;

import java.util.Scanner;

import nikita.exception.ExecutionException;
import nikita.input.file.JsonParser;
import nikita.input.manual.ManualParser;
import nikita.math.LinearSystem;
import nikita.output.ConsolePrinter;

public class Main {
	public static void main(String[] args) {

		Scanner scanner = new Scanner(System.in);
		JsonParser jsonParser = new JsonParser();
		ManualParser manualParser = new ManualParser(scanner);

		if (args.length != 0) {
			try {
				LinearSystem linearSystem = jsonParser.parse(args[0]);
			} catch (ExecutionException e) {
				ConsolePrinter.println(e.getMessage());
			}
		}

		ConsolePrinter.println("Entering interactive mode...");
		ConsolePrinter.println("Command options:");
		ConsolePrinter.println("file [file-name] \t - \t Execute calculations using parameters specified in the .json file.");
		ConsolePrinter.println("manual \t \t \t - \t Execute caclculations using manually-provided input parameters.");
		ConsolePrinter.println("exit \t \t \t - \t Terminate the program.");

		LinearSystem linearSystem = null;
		while (true) {
			ConsolePrinter.print(ConsolePrinter.INPUT_SYMBOL);
			String input = scanner.nextLine().trim().toLowerCase();

			if (input.isBlank()) {
				continue;
			}

			String[] tokens = input.split("\\s+", 2);
			String command = tokens[0];

			switch (command) {
			case "manual":
				try {
					linearSystem = manualParser.parse();
				} catch (ExecutionException e) {
					ConsolePrinter.println(e.getMessage());
				}
				break;
			case "file":
				if (tokens.length < 2 || tokens[1].isBlank()) {
					ConsolePrinter.println("Error: No file specified. Usage: file <fileName>");
				} else {
					String fileName = tokens[1].trim();
					try {
						linearSystem = jsonParser.parse(fileName);
					} catch (ExecutionException e) {
						ConsolePrinter.println(e.getMessage());
					}
				}
				break;
			case "exit":
				scanner.close();
				return;
			default:
				ConsolePrinter.println("Unrecognized command: " + input);
				break;
			}

		}
	}
}
