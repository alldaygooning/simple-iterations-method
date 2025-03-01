package nikita.math;

public class Row {
	float[] values;
	float result;

	public Row(float[] values, float result) {
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
			if (values[i] >= 0) {
				sb.append(" + ");
				sb.append(formatValueWithSign(values[i], false));
			} else {
				sb.append(" - ");
				sb.append(formatValueWithSign(Math.abs(values[i]), false));
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

	private String formatValue(float value) {
		if (value == (int) value) {
			return String.valueOf((int) value);
		} else {
			return String.valueOf(value);
		}
	}

	private String formatValueWithSign(float value, boolean isFirst) {
		if (isFirst && value < 0) {
			return "-" + formatValue(Math.abs(value));
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
