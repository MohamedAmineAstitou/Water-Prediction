package PrepareData;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.records.reader.impl.transform.TransformProcessRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.analysis.DataAnalysis;
import org.datavec.api.transform.schema.Schema;
import org.datavec.api.transform.transform.normalize.Normalize;
import org.datavec.api.transform.ui.HtmlAnalysis;
import org.datavec.local.transforms.AnalyzeLocal;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;

public class LoadingData {
	private Schema schema;
	private Random random = new Random();
	private CSVRecordReader csvRecordReader;
	private FileSplit fileSplit;
	private RecordReaderDataSetIterator recordReaderDataSetIterator;
	private DataAnalysis dataAnalysis;
	private Schema finalSchema;
	private TransformProcess transformProcess;
	private int batchSize = 30;

	public LoadingData() throws IOException, InterruptedException {
		random.setSeed(0xC0FFEE);
		fileSplit = new FileSplit(new File("src/main/resources/Train/"),random);
		csvRecordReader= new CSVRecordReader();
		csvRecordReader.initialize(fileSplit);
		DefiningSchema();
		AnalyzingData();
		TransformData();
		VectorizingData();
	}
	public  void DefiningSchema() {

		schema = new Schema.Builder()
				.addColumnDouble("ph")
				.addColumnDouble("Hardness")
				.addColumnDouble("Solids")
				.addColumnDouble("Chloramines")
				.addColumnDouble("Sulfate")
				.addColumnDouble("Conductivity")
				.addColumnDouble("Organic_carbon")
				.addColumnDouble("Trihalomethanes")
				.addColumnDouble("Turbidity")
				.addColumnCategorical("Potability","0","1")
				.build();
	}

	public void AnalyzingData()  {
		dataAnalysis = AnalyzeLocal.analyze(schema, csvRecordReader);
		System.out.println(1);
		try {
			HtmlAnalysis.createHtmlAnalysisFile(dataAnalysis, new File("src/main/resources/analysis.html"));
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
	public void TransformData() {


		transformProcess = new TransformProcess.Builder(schema)
		
				.normalize("Sulfate", Normalize.Standardize, dataAnalysis)
				.normalize("Conductivity", Normalize.Log2Mean, dataAnalysis)
				.normalize("Hardness", Normalize.Standardize, dataAnalysis)
				.normalize("ph", Normalize.Standardize, dataAnalysis)
				
				.normalize("Solids", Normalize.Log2Mean, dataAnalysis)
				.normalize("Chloramines", Normalize.Standardize, dataAnalysis)
	
				.normalize("Organic_carbon", Normalize.Standardize, dataAnalysis)
				.normalize("Trihalomethanes", Normalize.Standardize, dataAnalysis)
				.normalize("Turbidity", Normalize.Standardize, dataAnalysis)
				.build();

		finalSchema = transformProcess.getFinalSchema();

		
	}
	public void VectorizingData() throws IOException, InterruptedException {
		TransformProcessRecordReader transformProcessRecordReader = new TransformProcessRecordReader(new CSVRecordReader(), transformProcess);
		transformProcessRecordReader.initialize(fileSplit);
		//Lit les données CSV et les Vectorise Choix de classification avec 2 probabilité (Potable, pas potable)
		recordReaderDataSetIterator = new RecordReaderDataSetIterator.Builder(transformProcessRecordReader, batchSize)
				.classification(finalSchema.getIndexOfColumn("Potability"), 2)
				.build();

	}
	public Schema getSchema() {
	
		return schema;
	}
	public RecordReaderDataSetIterator getRecordReaderDataSetIterator() {
		return recordReaderDataSetIterator;
	}
	public TransformProcess getTransformProcess() {
		return transformProcess;
	}


}


