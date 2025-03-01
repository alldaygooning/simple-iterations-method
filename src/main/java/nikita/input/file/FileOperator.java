package nikita.input.file;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileOperator {

	public BufferedReader open(String filePath) throws FileNotFoundException {
		return new BufferedReader(new FileReader(filePath));
	}

	public String read(String path) throws IOException {
		BufferedReader bufferedReader = null;
		StringBuilder content = new StringBuilder();
		try {
			bufferedReader = open(path);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				content.append(line).append(System.lineSeparator());
			}
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
		return content.toString();
	}

	public void write(String path, String content) throws IOException {
		FileWriter writer = null;
		try {
			writer = new FileWriter(path);
			writer.write(content);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
}
