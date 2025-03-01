package nikita.math;

import java.util.Arrays;

import nikita.exception.ExecutionException;

public class Matrix {
	int size;
	Row[] rows;

	public Matrix(int size, Row[] rows) {
		this.size = size;
		this.rows = rows;
	}

	public boolean isDiagonallyDominant() {
		boolean strict = false;
		for (int row = 0; row < this.size; row++) {
			float diagonalValue = 0;
			float totalValue = 0;
			for (int column = 0; column < this.size; column++) {
				float currentValue = this.rows[row].values[column];
				if (column == row) {
					diagonalValue = Math.abs(currentValue);
				} else {
					totalValue = totalValue + Math.abs(currentValue);
				}
			}
			if (diagonalValue < totalValue) {
				return false;
			} else if (diagonalValue > totalValue) {
				strict = true;
			}
		}
		return strict;
	}

	public boolean isDiagonalDominanceEnforcable() {
		for (int row = 0; row < this.size; row++) {
			float[] currentRow = this.rows[row].values;
			float maxAbs = 0;
			float sumAllAbs = 0;

			for (float value : currentRow) {
				float absValue = Math.abs(value);
				sumAllAbs += absValue;
				if (absValue > maxAbs) {
					maxAbs = absValue;
				}
			}

			float sumOtherAbs = sumAllAbs - maxAbs;

			if (maxAbs < sumOtherAbs) {
				return false;
			}
		}
		return true;
	}

	public void enforceDiagonalDominance() {
		int n = this.size;

		// Строю двудольны граф, где слева вершины - индексы рядов, справа - колонок
		// Если есть грань между рядом r и колонкой c, то кандидат в ряду r на месте c.
		boolean[][] graph = new boolean[n][n];

		for (int r = 0; r < n; r++) {
			float[] currentRow = this.rows[r].values;
			// Считаем сумму абсолютных значений в каждом ряду
			float totalAbs = 0;
			for (int k = 0; k < n; k++) {
				totalAbs += Math.abs(currentRow[k]);
			}
			// Проверить каждый элемент, может ли он быть кадидатом. Если да, то добавить
			// грань на граф
			for (int c = 0; c < n; c++) {
				float pivotCandidate = Math.abs(currentRow[c]);
				float otherAbs = totalAbs - pivotCandidate;
				if (pivotCandidate >= otherAbs) {
					graph[r][c] = true;
				}
			}
		}

		// Найти идеальную пары. В каждом ряду должна быть только одна колонка с
		// кандидатом.
		int[] matchForColumn = new int[n];
		for (int i = 0; i < n; i++) {
			matchForColumn[i] = -1; // Изначально везде не метч
		}

		for (int r = 0; r < n; r++) {
			boolean[] seen = new boolean[n];
			if (!bipartiteMatch(r, graph, seen, matchForColumn, n)) {
				throw new ExecutionException("No perfect matching found. Diagonal dominance cannot be enforced.");
			}
		}

		Row[] newRows = new Row[n];

		// Если ряды надо пересортировать, то ряд встает на то место, с какой колонкой
		// он заметчился
		for (int c = 0; c < n; c++) {
			int r = matchForColumn[c];
			float[] originalValues = this.rows[r].values;
			int length = originalValues.length;
			float[] newValues = Arrays.copyOf(originalValues, length);
			// Переставляет так, чтобы кандидат встал в диагональную позицию
			if (c < length && c != findCandidateIndex(originalValues)) {
				int candidateIndex = c;
				if (candidateIndex != c) {
					float tmp = newValues[c];
					newValues[c] = newValues[candidateIndex];
					newValues[candidateIndex] = tmp;
				}
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

	// Ищет первое место, куда можно поставить кандидата, чтобы он подошел
	private int findCandidateIndex(float[] rowValues) {
		int n = rowValues.length;
		float totalAbs = 0;
		for (int i = 0; i < n; i++) {
			totalAbs += Math.abs(rowValues[i]);
		}
		for (int i = 0; i < n; i++) {
			float candidate = Math.abs(rowValues[i]);
			if (candidate >= totalAbs - candidate) {
				return i;
			}
		}
		return -1;
	}

	// Фробениус?
	public float getEuclidianNorm() {
		double total = 0;
		for (Row row : this.rows) {
			for (double value : row.values) {
				total = total + Math.pow(value, 2);
			}
		}

		return (float) Math.pow(total, 0.5);
	}

	@Override
	public String toString() {
		if (rows == null || rows.length == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < rows.length; i++) {
			String[] wrappers = this.getWrapper(i, rows.length);
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
