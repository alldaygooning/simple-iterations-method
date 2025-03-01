package nikita.validation;

import java.math.BigDecimal;

public class Validator {

	public static boolean validateSize(int size) {
		return (size >= 1 && size <= 20);
	}

	public static boolean validateAbsoluteAccuracy(BigDecimal absoluteAccuracy) {
		return absoluteAccuracy.compareTo(BigDecimal.ZERO) > 0;
	}

	public static boolean validateResult(BigDecimal result) {
		return true;
	}

	public static boolean validateRowValue(BigDecimal value) {
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
			BigDecimal parsedAccuracy = new BigDecimal(absoluteAccuracy);
			return validateAbsoluteAccuracy(parsedAccuracy);
		} catch (NumberFormatException | NullPointerException e) {
			return false;
		}
	}

	public static boolean validateResult(String result) {
		try {
			BigDecimal parsedResult = new BigDecimal(result);
			return validateResult(parsedResult);
		} catch (NumberFormatException | NullPointerException e) {
			return false;
		}
	}

	public static boolean validateRowValue(String value) {
		try {
			BigDecimal parsedValue = new BigDecimal(value);
			return validateRowValue(parsedValue);
		} catch (NumberFormatException | NullPointerException e) {
			return false;
		}
	}
}
