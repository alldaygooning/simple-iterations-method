package nikita.input.file;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nikita.exception.ExecutionException;
import nikita.math.LinearSystem;
import nikita.math.Matrix;
import nikita.math.Row;
import nikita.validation.Validator;

public class JsonParser {

	private static final String JSON_EXAMPLE = "{\n" + "    \"matrix\": {\n" + "        \"size\": 5,\n" + "        \"content\": [\n"
			+ "            {\n" + "                \"row\": [1, 2, 3, 4, 5],\n" + "                \"result\": 10\n" + "            },\n"
			+ "            {\n" + "                \"row\": [1, 2, 3, 4, 5],\n" + "                \"result\": 11\n" + "            },\n"
			+ "            {\n" + "                \"row\": [1, 2, 3, 4, 5],\n" + "                \"result\": 12\n" + "            },\n"
			+ "            {\n" + "                \"row\": [1, 2, 3, 4, 5],\n" + "                \"result\": 13\n" + "            },\n"
			+ "            {\n" + "                \"row\": [1, 2, 3, 4, 5],\n" + "                \"result\": 14\n" + "            }\n"
			+ "        ]\n" + "    },\n" + "    \"absolute-accuracy\": 0.01\n" + "}";
	public static final String ABSOLUTE_ACCURACY_EXPECTED = "Positive float value";
	public static final String SIZE_EXPECTED = "Integer [1; 20]";
	public static final String ROW_EXPECTED = "Decimal number";
	public static final String RESULT_EXPECTED = "Decimal number";
	public static final String CONTENT_EXPECTED = "Array of entries with 'row' and 'result' keys of size 'size'";

	FileOperator fileOperator;

	public JsonParser() {
		this.fileOperator = new FileOperator();
	}

	public LinearSystem parse(String fileName) throws ExecutionException {
		try {
			String content = this.fileOperator.read(fileName);
			JSONObject jsonObject = new JSONObject(content);

			ArrayList<Row> rows = new ArrayList<Row>();

			float absoluteAccuracy = this.getAbsoluteAccuracy(jsonObject);
			JSONObject matrixObject = this.getMatrix(jsonObject);
			int size = 0;
			JSONArray contentArray = null;
			boolean shouldEdit = false;
			if (matrixObject != null) {
				size = this.getSize(matrixObject);
				if (size > 0) {
					contentArray = this.getContent(matrixObject, size);
					if (contentArray != null) {
						for (int i = 0; i < contentArray.length(); i++) {
							JSONObject contentEntry = contentArray.getJSONObject(i);
							Float result = this.getResult(contentEntry);
							float[] rowValues = this.getRow(contentEntry, size);
							if (result == null || rowValues == null) {
								shouldEdit = true;
							}
							else {
								Row row = new Row(rowValues, result);
								rows.add(row);
							}
						}
					}
				}
			}

			if (absoluteAccuracy == -1 || matrixObject == null || size == -1 || contentArray == null || shouldEdit) {
				this.edit(fileName, jsonObject);
				this.example();
				throw new JSONException(String.format("The JSON structure in file '%s' is invalid or incomplete. "
						+ "Please review the file following the provided example format.", fileName));
			}
			Matrix matrix = new Matrix(size, rows.toArray(new Row[rows.size()]));
			LinearSystem linearSystem = new LinearSystem(absoluteAccuracy, matrix);
			return linearSystem;

		} catch (FileNotFoundException fnfe) {
			throw new ExecutionException(
					String.format("The file '%s' could not be located. Please verify the file path and try again.", fileName));
		} catch (EOFException eofe) {
			throw new ExecutionException(
					String.format("The file '%s' ended unexpectedly. The file may be incomplete or corrupted.", fileName));
		} catch (IOException ioe) {
			throw new ExecutionException(
					String.format("An input/output error occurred while reading '%s': %s.", fileName, ioe.getMessage()));
		} catch (JSONException jsone) {
			throw new ExecutionException(String.format(
					"%s The file '%s' does not conform to the expected format. Refer to 'example.json' for the correct JSON structure.",
					jsone.getMessage(), fileName));
		}
	}

	private float getAbsoluteAccuracy(JSONObject jsonObject) throws JSONException, IOException {
		if (!jsonObject.has("absolute-accuracy")) {
			jsonObject.put("absolute-accuracy", String.format("Required field. Expected: %s.", JsonParser.ABSOLUTE_ACCURACY_EXPECTED));
			return -1;
		}

		try {
			float absoluteAccuracy = jsonObject.getFloat("absolute-accuracy");
			if (!Validator.validateAbsoluteAccuracy(absoluteAccuracy)) {
				throw new JSONException("");
			}
			return absoluteAccuracy;
		} catch (JSONException e) {
			jsonObject.put("absolute-accuracy", String.format("Provided: %s. Expected: %s.", jsonObject.get("absolute-accuracy"),
					JsonParser.ABSOLUTE_ACCURACY_EXPECTED));
			return -1;
		}
	}

	private JSONObject getMatrix(JSONObject jsonObject) {
		if (!jsonObject.has("matrix")) {
			JSONObject matrixObject = new JSONObject();
			matrixObject.put("size", String.format("Required field. Expected: %s.", JsonParser.SIZE_EXPECTED));

			org.json.JSONArray contentArray = new org.json.JSONArray();
			for (int i = 0; i < 5; i++) {
				JSONObject contentEntry = new JSONObject();
				org.json.JSONArray rowArray = new org.json.JSONArray();
				for (int j = 0; j < 5; j++) {
					rowArray.put(JsonParser.ROW_EXPECTED);
				}
				contentEntry.put("row", rowArray);
				contentEntry.put("result", JsonParser.RESULT_EXPECTED);
				contentArray.put(contentEntry);
			}
			matrixObject.put("content", contentArray);
			jsonObject.put("matrix", matrixObject);
			return null;
		}
		return jsonObject.getJSONObject("matrix");
	}

	private int getSize(JSONObject matrixObject) {
		if (!matrixObject.has("size")) {
			matrixObject.put("size", String.format("Required field. Expected: %s.", JsonParser.SIZE_EXPECTED));
			return -1;
		}

		try {
			Object sizeObj = matrixObject.get("size");
			if (!(sizeObj instanceof Integer)) {
				throw new JSONException("Provided value is not an integer.");
			}

			int size = (Integer) sizeObj;
			if (!Validator.validateSize(size)) {
				throw new JSONException("Integer provided is not in the expected range [1;20].");
			}
			return size;
		} catch (JSONException e) {
			matrixObject.put("size", String.format("Provided: %s. Expected: %s.", matrixObject.get("size"), JsonParser.SIZE_EXPECTED));
			return -1;
		}
	}

	private JSONArray getContent(JSONObject matrixObject, int size) {
		if (!matrixObject.has("content")) {
			matrixObject.put("content", String.format("Required field. Expected: %s", JsonParser.CONTENT_EXPECTED));
			return null;
		}

		try {
			JSONArray contentArray = matrixObject.getJSONArray("content");
			if (contentArray.length() != size) {
				matrixObject.put("size", String.format("Provided: %s. Content length: %s.", size, contentArray.length()));
				return null;
			}
			return contentArray;
		} catch (JSONException e) {
			matrixObject.put("content", String.format("Provided: %s. Expected %s", matrixObject.get("content")));
			return null;
		}

	}

	private Float getResult(JSONObject contentEntry) throws JSONException {
		if (!contentEntry.has("result")) {
			contentEntry.put("result", String.format("Required field. Expected: %s.", JsonParser.RESULT_EXPECTED));
			return null;
		}
		try {
			float result = contentEntry.getFloat("result");
			if (!Validator.validateResult(result)) {
				throw new JSONException("Result value did not pass validation.");
			}
			return result;
		} catch (JSONException e) {
			contentEntry.put("result",
					String.format("Provided: %s. Expected: %s.", contentEntry.get("result"), JsonParser.RESULT_EXPECTED));
			return null;
		}
	}

	private float[] getRow(JSONObject contentEntry, int size) {
		if (!contentEntry.has("row")) {
			contentEntry.put("row", String.format("Required field. Expected: Array of %s of length %d.", JsonParser.ROW_EXPECTED, size));
			return null;
		}

		try {
			JSONArray rowArray = contentEntry.getJSONArray("row");
			if (rowArray.length() != size) {
				contentEntry.put("row", String.format("%s Provided array length: %s. Expected: %d in accordance with matrix 'size'.",
						rowArray, rowArray.length(), size));
				return null;
			}

			float[] rowValues = new float[size];
			for (int j = 0; j < rowArray.length(); j++) {
				Object rowValueObj = rowArray.get(j);
				try {
					float rowValue;
					if (rowValueObj instanceof Number) {
						rowValue = ((Number) rowValueObj).floatValue();
					} else {
						throw new JSONException("Row value is not numeric.");
					}
					if (!Validator.validateRowValue(rowValue)) {
						throw new JSONException("Row value did not pass validation.");
					}
					rowValues[j] = rowValue;
				} catch (JSONException e) {
					contentEntry.put("row", String.format("Provided: %s. Expected: %s.", rowValueObj, JsonParser.ROW_EXPECTED));
					return null;
				}
			}
			return rowValues;
		} catch (JSONException e) {
			contentEntry.put("row",
					String.format("Provided: %s. Expected: Array of %s.", contentEntry.get("row"), JsonParser.ROW_EXPECTED));
			return null;
		}
	}


	private void edit(String fileName, JSONObject jsonObject) throws JSONException, IOException {
		this.fileOperator.write(fileName, jsonObject.toString(2));
	}

	private void example() throws IOException {
		File exampleFile = new File("example.json");
		if (!exampleFile.exists()) {
			this.fileOperator.write("example.json", JSON_EXAMPLE);
		}
	}
}
