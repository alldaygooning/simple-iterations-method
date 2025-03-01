package nikita.validation;

public class Validator {

	public static boolean validateSize(int size) {
		return (size >= 1 && size <= 20);
	}

	public static boolean validateAbsoluteAccuracy(float absoluteAccuracy) {
		return (absoluteAccuracy > 0);
	}

	public static boolean validateResult(float result) {
		return true;
	}

	public static boolean validateRowValue(float value) {
		return true;
	}

	public static boolean validateSize(String size) {
		try {
			int parsedSize = Integer.parseInt(size);
			return validateSize(parsedSize);
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean validateAbsoluteAccuracy(String absoluteAccuracy) {
		try {
			float parsedAccuracy = Float.parseFloat(absoluteAccuracy);
			return validateAbsoluteAccuracy(parsedAccuracy);
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean validateResult(String result) {
		try {
			float parsedResult = Float.parseFloat(result);
			return validateResult(parsedResult);
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean validateRowValue(String value) {
		try {
			float parsedValue = Float.parseFloat(value);
			return validateRowValue(parsedValue);
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
