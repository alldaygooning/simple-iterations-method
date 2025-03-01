package nikita.math;

import java.math.BigDecimal;

public class Row {
	BigDecimal[] values;
	BigDecimal result;

	public Row(BigDecimal[] values, BigDecimal result) {
		this.values = values;
		this.result = result;
	}

	@Override
	public String toString() {
		if (values == null || values.length == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		sb.append(formatValueWithSign(values[0], true));
		sb.append("x").append(getSubscript(1));

		for (int i = 1; i < values.length; i++) {
			// Check if the value is negative based on sign.
			if (values[i].compareTo(BigDecimal.ZERO) >= 0) {
				sb.append(" + ");
				sb.append(formatValueWithSign(values[i], false));
			} else {
				sb.append(" - ");
				// Use absolute value for formatting
				sb.append(formatValueWithSign(values[i].abs(), false));
			}
			sb.append("x").append(getSubscript(i + 1));
		}

		sb.append(" = ");
		sb.append(formatValue(result));

		return sb.toString();
	}

	public String valuesToString() {
		if (values == null || values.length == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			if (i > 0) {
				sb.append("\t");
			}
			sb.append(formatValue(values[i]));
		}
		return sb.toString();
	}

	private String formatValue(BigDecimal value) {
		// If the value is integral (scale <= 0 or zero fractional part), show as
		// integer.
		try {
			// Remove trailing zeros; if the value is an integer, the scale will be less
			// than or equal to 0.
			value = value.stripTrailingZeros();
		} catch (ArithmeticException e) {
			// Fallback in case of issues.
		}
		if (value.scale() <= 0) {
			return value.toBigInteger().toString();
		} else {
			return value.toPlainString();
		}
	}

	private String formatValueWithSign(BigDecimal value, boolean isFirst) {
		if (isFirst && value.compareTo(BigDecimal.ZERO) < 0) {
			return "-" + formatValue(value.abs());
		}
		return formatValue(value);
	}

	private String getSubscript(int num) {
		String numStr = String.valueOf(num);
		StringBuilder sb = new StringBuilder();
		for (char ch : numStr.toCharArray()) {
			sb.append(getSubscriptChar(ch));
		}
		return sb.toString();
	}

	private char getSubscriptChar(char ch) {
		switch (ch) {
		case '0':
			return '₀';
		case '1':
			return '₁';
		case '2':
			return '₂';
		case '3':
			return '₃';
		case '4':
			return '₄';
		case '5':
			return '₅';
		case '6':
			return '₆';
		case '7':
			return '₇';
		case '8':
			return '₈';
		case '9':
			return '₉';
		default:
			return ch;
		}
	}
}
