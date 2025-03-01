package nikita.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;

import nikita.exception.ExecutionException;
import nikita.output.ConsolePrinter;

public class LinearSystem {
	BigDecimal absoluteAccuracy;
	Matrix matrix;
	ArrayList<Approximation> approximations = new ArrayList<>();
	int iteration = 0;

	// Define a MathContext for operations (customize precision if needed)
	private static final MathContext MATH_CONTEXT = new MathContext(20, RoundingMode.HALF_UP);

	public LinearSystem(BigDecimal absoluteAccuracy, Matrix matrix) {
		this.absoluteAccuracy = absoluteAccuracy;
		this.matrix = matrix;

		if (!this.matrix.isDiagonalDominanceEnforcable()) {
			throw new ExecutionException("No perfect matching found. Diagonal dominance cannot be enforced.");
		}
		this.matrix.enforceDiagonalDominance();

		ConsolePrinter.println(ConsolePrinter.LINE);
		ConsolePrinter.println(this.toString());
		ConsolePrinter.println(ConsolePrinter.LINE);

		this.getInitialApproximation();
		printApproximation(0, this.approximations.get(0).values, null);
		ConsolePrinter.println(ConsolePrinter.LINE);
		this.solve();
	}

	public void getInitialApproximation() {
		// Compute initial approximation: for each i, x_i^(0) = result / row[i][i]
		ArrayList<BigDecimal> valuesList = new ArrayList<>();
		for (int i = 0; i < this.matrix.size; i++) {
			Row currentRow = this.matrix.rows[i];
			// Use division with MathContext to avoid non-terminating decimal expansion
			// issues
			BigDecimal initial = currentRow.result.divide(currentRow.values[i], MATH_CONTEXT);
			valuesList.add(initial);
		}
		BigDecimal[] approxValues = valuesList.toArray(new BigDecimal[0]);
		this.approximations.add(new Approximation(approxValues));
	}

	public void solve() {
		boolean converged = false;
		int maxIterations = 1000; // Limit iterations to avoid infinite loops

		while (!converged && iteration < maxIterations) {
			Approximation prevApproximation = this.approximations.get(this.approximations.size() - 1);
			BigDecimal[] currentApprox = prevApproximation.values;
			BigDecimal[] newApprox = new BigDecimal[currentApprox.length];
			BigDecimal[] diffVector = new BigDecimal[currentApprox.length];
			BigDecimal maxDiff = BigDecimal.ZERO;

			// Compute new approximations for each variable
			for (int i = 0; i < matrix.size; i++) {
				Row currentRow = matrix.rows[i];
				BigDecimal sum = BigDecimal.ZERO;
				for (int j = 0; j < matrix.size; j++) {
					if (j != i) {
						sum = sum.add(currentRow.values[j].multiply(currentApprox[j], MATH_CONTEXT), MATH_CONTEXT);
					}
				}
				// newApprox[i] = (result - sum) / row[i][i]
				newApprox[i] = currentRow.result.subtract(sum, MATH_CONTEXT).divide(currentRow.values[i], MATH_CONTEXT);

				// Absolute difference |newApprox[i] - currentApprox[i]|
				BigDecimal diff = newApprox[i].subtract(currentApprox[i], MATH_CONTEXT).abs();
				diffVector[i] = diff;
				if (diff.compareTo(maxDiff) > 0) {
					maxDiff = diff;
				}
			}

			iteration++;
			printApproximation(iteration, newApprox, diffVector);
			ConsolePrinter.println("Max difference: " + maxDiff.toPlainString());
			ConsolePrinter.println(ConsolePrinter.LINE);

			this.approximations.add(new Approximation(newApprox));

			if (maxDiff.compareTo(absoluteAccuracy) < 0) {
				converged = true;
			}
		}

		if (!converged) {
			throw new ExecutionException("The iterative method did not converge within the maximum allowed iterations.");
		}
	}

	private void printApproximation(int iter, BigDecimal[] approx, BigDecimal[] diffVector) {
		if (iter == 0) {
			ConsolePrinter.println("Initial Approximation:");
		} else {
			ConsolePrinter.println("Iteration " + iter + ":");
		}

		StringBuilder approximationStr = new StringBuilder();
		for (int i = 0; i < approx.length; i++) {
			// Formatting BigDecimal to 10 decimal places
			String formattedValue = approx[i].setScale(10, RoundingMode.HALF_UP).toPlainString();
			approximationStr.append(String.format("x%d = %s\t", i + 1, formattedValue));
		}
		ConsolePrinter.println(approximationStr.toString());

		if (diffVector != null) {
			StringBuilder diffsStr = new StringBuilder();
			for (int i = 0; i < diffVector.length; i++) {
				String formattedDiff = diffVector[i].setScale(10, RoundingMode.HALF_UP).toPlainString();
				diffsStr.append(String.format("|x%d_diff| = %s\t", i + 1, formattedDiff));
			}
			ConsolePrinter.println(diffsStr.toString());
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(System.lineSeparator());
		sb.append("Parsed System of Linear Equations:");
		sb.append(System.lineSeparator()).append(System.lineSeparator());
		sb.append(String.format("Matrix size: %d×%d\t Euclidian norm: %s", this.matrix.size, this.matrix.size,
				this.matrix.getEuclidianNorm().toPlainString()));
		sb.append(System.lineSeparator()).append(System.lineSeparator());
		for (int i = 0; i < this.matrix.rows.length; i++) {
			if (this.matrix.rows.length == 1) {
				sb.append("| ");
			} else if (i == 0) {
				sb.append("/ ");
			} else if (i + 1 == this.matrix.rows.length) {
				sb.append("\\ ");
			} else {
				sb.append("| ");
			}
			sb.append(this.matrix.rows[i].toString());
			sb.append(System.lineSeparator());
		}
		sb.append(System.lineSeparator());
		sb.append(String.format("Absolute accuracy: %s", this.absoluteAccuracy.toPlainString()));
		sb.append(System.lineSeparator());
		return sb.toString();
	}
}
