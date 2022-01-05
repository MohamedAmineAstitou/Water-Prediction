package PrepareData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AccessingData {
	private final static String RESOURCES_PATH = "src/main/resources/";
	private final static String WATER_FILE_NAME = "water_potability_modifié.csv";
	private final static String TRAIN_PATH="src/main/resources/Train/";
	private final static String TEST_PATH="src/main/resources/Test/";
	private File inputFile;
	private List<String> lines;

	public AccessingData() throws IOException {



		ReadFiles();
		SplitFile();
	}



	private void ReadFiles() {
		inputFile = new File(RESOURCES_PATH+WATER_FILE_NAME);
		try {
			lines = Files.readAllLines(inputFile.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private void SplitFile() {
		// 30% de données test
		double splitPourcentage = 0.7;

		String header = lines.get(0);
		lines.remove(0);

		int splitPosition = (int)Math.round(splitPourcentage * lines.size());

		//Sépare les fichier de manière random pour la cohérence des données
		final Random random = new Random();
		random.setSeed(0xC0FFEE);

		Collections.shuffle(lines, random);

		final ArrayList<String> train = new ArrayList<String>(lines.subList(0, splitPosition));
		final ArrayList<String> test = new ArrayList<String>(lines.subList(splitPosition, lines.size()));
		

		int i = 0;
		try 
		{
			for (String line : train) {
				Files.write(Paths.get(TRAIN_PATH+(i++)+".csv"), line.getBytes());
			}

			for (String line : test) {

				Files.write(Paths.get(TEST_PATH+(i++)+".csv"), line.getBytes());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
