package nikita.input.manual;

import java.util.ArrayList;
import java.util.Scanner;

import nikita.exception.ExecutionException;
import nikita.input.file.JsonParser;
import nikita.math.LinearSystem;
import nikita.math.Matrix;
import nikita.math.Row;
import nikita.output.ConsolePrinter;
import nikita.validation.Validator;

public class ManualParser {

	Scanner scanner;

	public ManualParser(Scanner scanner) {
		this.scanner = scanner;
	}

	public LinearSystem parse() {
		int size = this.getSize();
		Matrix maxtrix = this.getMatrix(size);
		float absoluteAccuracy = this.getAbsoluteAccuracy();

		return new LinearSystem(absoluteAccuracy, maxtrix);
	}

	public int getSize() {
		while (true) {
			ConsolePrinter.print(String.format("Matrix size (%s): ", JsonParser.SIZE_EXPECTED));
			String input = this.getInput();
			if (!Validator.validateSize(input)) {
				ConsolePrinter.println(String.format("Provided: %s. Expected: %s.", input, JsonParser.SIZE_EXPECTED));
				continue;
			}
			return (Integer.parseInt(input));
		}
	}

	public Matrix getMatrix(int size) {
		ArrayList<Row> rows = new ArrayList<Row>();
		for (int i = 1; i <= size; i++) {
			ConsolePrinter.print(String.format("%d/%d ", i, size));
			Row row = this.getRow(size);
			rows.add(row);
		}
		return new Matrix(size, rows.toArray(new Row[rows.size()]));
	}

	public Row getRow(int size) {
		while (true) {
			ConsolePrinter.print(
					String.format("Coefficients of a row (%s) and result (%s): ", JsonParser.ROW_EXPECTED, JsonParser.RESULT_EXPECTED));
			String input = this.getInput();
			String[] values = input.split("\\s+");

			if (values.length < size + 1) {
				ConsolePrinter.println(String.format("Expected: %s + 1 values. Provided: %s.", size, values.length));
				continue;
			}

			float[] coefficients = new float[size];

			boolean invalidInput = false;
			for (int i = 0; i < size; i++) {
				String value = values[i];
				if (!Validator.validateRowValue(value)) {
					ConsolePrinter.println(String.format("Provided: %s. Expected: %s.", value, JsonParser.ROW_EXPECTED));
					invalidInput = true;
					break;
				}
				coefficients[i] = Float.parseFloat(value);
			}

			String resultStr = values[size];
			if (!Validator.validateResult(resultStr)) {
				ConsolePrinter.println(String.format("Provided: %s. Expected: %s.", resultStr, JsonParser.RESULT_EXPECTED));
				invalidInput = true;
			}
			float result = Float.parseFloat(resultStr);

			if (invalidInput) {
				continue;
			}

			return new Row(coefficients, result);
		}
	}

	public float getAbsoluteAccuracy() {
		while (true) {
			ConsolePrinter.print(String.format("Absolute accuracy (%s): ", JsonParser.ABSOLUTE_ACCURACY_EXPECTED));
			String input = this.getInput();
			if (!Validator.validateAbsoluteAccuracy(input)) {
				ConsolePrinter.println(String.format("Provided: %s. Expected: %s.", input, JsonParser.ABSOLUTE_ACCURACY_EXPECTED));
				continue;
			}
			return Float.parseFloat(input);
		}
	}


	public String getInput() {
		while (true) {
			ConsolePrinter.print(ConsolePrinter.INPUT_SYMBOL);
			String input = scanner.nextLine();
			if (input.isBlank()) {
				continue;
			} else if (input.trim().toLowerCase().equals("exit")) {
				throw new ExecutionException("Manual matrix creation terminated.");
			}
			return input;
		}
	}
}
