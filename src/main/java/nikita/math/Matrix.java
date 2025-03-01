package nikita.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

import nikita.exception.ExecutionException;

public class Matrix {
	int size;
	Row[] rows;

	// Define a MathContext for operations (you can customize the precision as
	private static final MathContext MATH_CONTEXT = new MathContext(20, RoundingMode.HALF_UP);

	public Matrix(int size, Row[] rows) {
		this.size = size;
		this.rows = rows;
	}

	public boolean isDiagonallyDominant() {
		boolean strict = false;
		for (int rowIndex = 0; rowIndex < this.size; rowIndex++) {
			BigDecimal diagonalValue = BigDecimal.ZERO;
			BigDecimal totalValue = BigDecimal.ZERO;
			for (int column = 0; column < this.size; column++) {
				BigDecimal currentValue = this.rows[rowIndex].values[column];
				if (column == rowIndex) {
					diagonalValue = currentValue.abs();
				} else {
					totalValue = totalValue.add(currentValue.abs(), MATH_CONTEXT);
				}
			}
			// If diagonal is less than the sum of the rest, it's not diagonally dominant.
			if (diagonalValue.compareTo(totalValue) < 0) {
				return false;
			} else if (diagonalValue.compareTo(totalValue) > 0) {
				strict = true;
			}
		}
		return strict;
	}

	public boolean isDiagonalDominanceEnforcable() {
		for (int rowIndex = 0; rowIndex < this.size; rowIndex++) {
			BigDecimal[] currentRow = this.rows[rowIndex].values;
			BigDecimal maxAbs = BigDecimal.ZERO;
			BigDecimal sumAllAbs = BigDecimal.ZERO;

			for (BigDecimal value : currentRow) {
				BigDecimal absValue = value.abs();
				sumAllAbs = sumAllAbs.add(absValue, MATH_CONTEXT);
				if (absValue.compareTo(maxAbs) > 0) {
					maxAbs = absValue;
				}
			}

			BigDecimal sumOtherAbs = sumAllAbs.subtract(maxAbs, MATH_CONTEXT);

			if (maxAbs.compareTo(sumOtherAbs) < 0) {
				return false;
			}
		}
		return true;
	}

	public void enforceDiagonalDominance() {
		int n = this.size;

		// Build a bipartite graph where left nodes represent rows and right nodes
		// represent columns.
		// If there is an edge between row r and column c, then the element in r at
		// index c is a candidate.
		boolean[][] graph = new boolean[n][n];

		for (int r = 0; r < n; r++) {
			BigDecimal[] currentRow = this.rows[r].values;
			BigDecimal totalAbs = BigDecimal.ZERO;
			for (int k = 0; k < n; k++) {
				totalAbs = totalAbs.add(currentRow[k].abs(), MATH_CONTEXT);
			}
			// Mark candidates in the bipartite graph
			for (int c = 0; c < n; c++) {
				BigDecimal pivotCandidate = currentRow[c].abs();
				BigDecimal otherAbs = totalAbs.subtract(pivotCandidate, MATH_CONTEXT);
				if (pivotCandidate.compareTo(otherAbs) >= 0) {
					graph[r][c] = true;
				}
			}
		}

		// Find a perfect matching. matchForColumn[c] = r indicates that row r is
		// matched to column c.
		int[] matchForColumn = new int[n];
		for (int i = 0; i < n; i++) {
			matchForColumn[i] = -1;
		}

		for (int r = 0; r < n; r++) {
			boolean[] seen = new boolean[n];
			if (!bipartiteMatch(r, graph, seen, matchForColumn, n)) {
				throw new ExecutionException("No perfect matching found. Diagonal dominance cannot be enforced.");
			}
		}

		Row[] newRows = new Row[n];

		// Rearrange rows so that each row's diagonal candidate is placed on the
		// diagonal.
		for (int c = 0; c < n; c++) {
			int r = matchForColumn[c];
			BigDecimal[] originalValues = this.rows[r].values;
			int length = originalValues.length;
			BigDecimal[] newValues = Arrays.copyOf(originalValues, length);
			// Find candidate index; if not already in the diagonal position, perform a
			// swap.
			int candidateIndex = findCandidateIndex(originalValues);
			if (candidateIndex != c && candidateIndex != -1 && candidateIndex < length) {
				BigDecimal tmp = newValues[c];
				newValues[c] = newValues[candidateIndex];
				newValues[candidateIndex] = tmp;
			}
			newRows[c] = new Row(newValues, this.rows[r].result);
		}
		this.rows = newRows;
	}

	private boolean bipartiteMatch(int row, boolean[][] graph, boolean[] seen, int[] matchForColumn, int n) {
		for (int col = 0; col < n; col++) {
			if (graph[row][col] && !seen[col]) {
				seen[col] = true;
				if (matchForColumn[col] < 0 || bipartiteMatch(matchForColumn[col], graph, seen, matchForColumn, n)) {
					matchForColumn[col] = row;
					return true;
				}
			}
		}
		return false;
	}

	// Finds the first column index where the candidate value (satisfying the
	// condition) is found.
	private int findCandidateIndex(BigDecimal[] rowValues) {
		int n = rowValues.length;
		BigDecimal totalAbs = BigDecimal.ZERO;
		for (int i = 0; i < n; i++) {
			totalAbs = totalAbs.add(rowValues[i].abs(), MATH_CONTEXT);
		}
		for (int i = 0; i < n; i++) {
			BigDecimal candidate = rowValues[i].abs();
			if (candidate.compareTo(totalAbs.subtract(candidate, MATH_CONTEXT)) >= 0) {
				return i;
			}
		}
		return -1;
	}

	// Returns the Euclidean norm (Frobenius norm) of the matrix.
	// Since BigDecimal doesn't have a built-in square root, we use a Newton-Raphson
	// approximation.
	public BigDecimal getEuclidianNorm() {
		BigDecimal total = BigDecimal.ZERO;
		for (Row row : this.rows) {
			for (BigDecimal value : row.values) {
				// value^2 = value * value
				total = total.add(value.multiply(value, MATH_CONTEXT), MATH_CONTEXT);
			}
		}
		return sqrt(total, MATH_CONTEXT);
	}

	// Newton–Raphson method to compute square root for BigDecimal
	private static BigDecimal sqrt(BigDecimal value, MathContext context) {
		if (value.compareTo(BigDecimal.ZERO) < 0) {
			throw new ArithmeticException("Square root of negative value");
		}
		if (value.equals(BigDecimal.ZERO)) {
			return BigDecimal.ZERO;
		}

		BigDecimal x = new BigDecimal(Math.sqrt(value.doubleValue()), context);
		BigDecimal two = new BigDecimal(2, context);
		// Iteratively apply Newton's method
		for (int i = 0; i < 20; i++) {
			x = x.add(value.divide(x, context)).divide(two, context);
		}
		return x;
	}

	@Override
	public String toString() {
		if (rows == null || rows.length == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < rows.length; i++) {
			String[] wrappers = getWrapper(i, rows.length);
			sb.append(wrappers[0]);
			sb.append(rows[i].valuesToString());
			sb.append(wrappers[1]);
			if (i < rows.length - 1) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	private String[] getWrapper(int index, int length) {
		if (length == 1) {
			return new String[] { "|", "|" };
		} else if (index == 0) {
			return new String[] { "/", "\\" };
		} else if (index + 1 == length) {
			return new String[] { "\\", "/" };
		}
		return new String[] { "|", "|" };
	}
}
