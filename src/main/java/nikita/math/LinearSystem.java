package nikita.math;

import java.util.ArrayList;

import nikita.exception.ExecutionException;
import nikita.output.ConsolePrinter;

public class LinearSystem {
	float absoluteAccuracy;
	Matrix matrix;
	ArrayList<Approximation> approximations = new ArrayList<Approximation>();
	int iteration = 0;

	public LinearSystem(float absoluteAccuracy, Matrix matrix) {
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
		ArrayList<Float> values = new ArrayList<Float>();
		for (int i = 0; i < this.matrix.size; i++) {
			Row currentRow = this.matrix.rows[i];
			values.add(currentRow.result / currentRow.values[i]);
		}
		float[] primitiveValues = new float[values.size()];
		for (int i = 0; i < values.size(); i++) {
			primitiveValues[i] = values.get(i);
		}
		this.approximations.add(new Approximation(primitiveValues));
	}

	public void solve() {
		boolean converged = false;
		int maxIterations = 1000; // Ну чтобы слишком долго не считало если что

		while (!converged && iteration < maxIterations) {
			Approximation prevApproximation = this.approximations.get(this.approximations.size() - 1);
			float[] currentApprox = prevApproximation.values;
			float[] newApprox = new float[currentApprox.length];
			float[] diffVector = new float[currentApprox.length];
			float maxDiff = 0;

			// Новое приближение для каждого x
			for (int i = 0; i < matrix.size; i++) {
				Row currentRow = matrix.rows[i];
				float sum = 0;
				for (int j = 0; j < matrix.size; j++) {
					if (j != i) {
						sum += currentRow.values[j] * currentApprox[j];
					}
				}
				newApprox[i] = (currentRow.result - sum) / currentRow.values[i];

				// Абсолютная погрешность для каждого x
				float diff = Math.abs(newApprox[i] - currentApprox[i]);
				diffVector[i] = diff;
				if (diff > maxDiff) {
					maxDiff = diff;
				}
			}

			iteration++;
			printApproximation(iteration, newApprox, diffVector);
			ConsolePrinter.println("Max difference: " + maxDiff);
			ConsolePrinter.println(ConsolePrinter.LINE);

			this.approximations.add(new Approximation(newApprox));

			if (maxDiff < absoluteAccuracy) {
				converged = true;
			}
		}

		if (!converged) {
			throw new ExecutionException("The iterative method did not converge within the maximum allowed iterations.");
		}
	}

	private void printApproximation(int iter, float[] approx, float[] diffVector) {
		if (iter == 0) {
			ConsolePrinter.println("Initial Approximation:");
		} else {
			ConsolePrinter.println("Iteration " + iter + ":");
		}

		StringBuilder approximationStr = new StringBuilder();
		for (int i = 0; i < approx.length; i++) {
			approximationStr.append(String.format("x%d = %.10f\t", i + 1, approx[i]));
		}
		ConsolePrinter.println(approximationStr.toString());

		if (diffVector != null) {
			StringBuilder diffsStr = new StringBuilder("");
			for (int i = 0; i < diffVector.length; i++) {
				diffsStr.append(String.format("|x%d_diff| = %.10f\t", i + 1, diffVector[i]));
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
		sb.append(String.format("Matrix size: %d×%d\t Euclidian norm: %f", this.matrix.size, this.matrix.size,
				this.matrix.getEuclidianNorm()));
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
		sb.append(String.format("Absolute accuracy: %f", this.absoluteAccuracy));
		sb.append(System.lineSeparator());
		return sb.toString();
	}
}
