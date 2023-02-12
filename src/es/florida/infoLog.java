package es.florida;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class infoLog {

	private static final String INFO_TXT = "info.txt";

	public static void newFile() {
		File file = new File(INFO_TXT);
		try {
			if (file.exists()) {
				file.delete();
				file.createNewFile();
				write("Record: \n");
			} else {
				file.createNewFile();
				write("Records: ");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<String> fileContent() {

		ArrayList<String> content = new ArrayList<String>();
		File file = new File(INFO_TXT);

		try {

			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			String line = br.readLine();
			while (line != null) {
				content.add(line);
				line = br.readLine();
			}
			br.close();
			fr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return content;
	}

	public static void write(String log) throws IOException {

		System.out.println(log);

		ArrayList<String> content = fileContent();

		FileWriter fw = new FileWriter(INFO_TXT);
		BufferedWriter bw = new BufferedWriter(fw);

		for (String line : content) {
			bw.write(line + "\n");
		}

		bw.write(log + "\n");

		bw.close();
		fw.close();

	}

}