package nikita.input.random;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Random;

import nikita.input.manual.ManualParser;
import nikita.math.LinearSystem;
import nikita.math.Matrix;
import nikita.math.Row;

public class RandomMatrixGenerator {
	ManualParser manualParser;
	private final Random random;
	private static final MathContext MATH_CONTEXT = new MathContext(20, RoundingMode.HALF_UP);

	public RandomMatrixGenerator(ManualParser manualParser) {
		this.manualParser = manualParser;
		this.random = new Random();
	}

	public LinearSystem parse() {
		int size = this.manualParser.getSize();
		Matrix matrix = getMatrix(size);
		// You can choose an absolute accuracy value; here we choose 1e-10.
		BigDecimal absoluteAccuracy = new BigDecimal("1E-10", MATH_CONTEXT);
		return new LinearSystem(absoluteAccuracy, matrix);
	}

	public Matrix getMatrix(int size) {
		Row[] rows = new Row[size];
		for (int i = 0; i < size; i++) {
			rows[i] = getRow(size, i);
		}
		return new Matrix(size, rows);
	}

	public Row getRow(int size, int rowIndex) {
		BigDecimal[] values = new BigDecimal[size];
		BigDecimal offDiagonalSum = BigDecimal.ZERO;

		for (int j = 0; j < size; j++) {
			if (j != rowIndex) {
				BigDecimal rnd = new BigDecimal(random.nextDouble() * 10).setScale(2, RoundingMode.HALF_UP);
				if (random.nextBoolean()) {
					rnd = rnd.negate();
				}
				values[j] = rnd;
				offDiagonalSum = offDiagonalSum.add(rnd.abs(), MATH_CONTEXT);
			}
		}

		double extra = 1 + random.nextDouble() * 9;
		BigDecimal diagValue = offDiagonalSum.add(new BigDecimal(extra, MATH_CONTEXT), MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP);
		values[rowIndex] = diagValue;

		BigDecimal result = new BigDecimal(random.nextDouble() * 100, MATH_CONTEXT).setScale(2, RoundingMode.HALF_UP);

		return new Row(values, result);
	}
}