package ModeliseData;

import java.io.File;
import java.io.IOException;


import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.records.reader.impl.transform.TransformProcessRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.transform.TransformProcess;
import org.datavec.api.transform.schema.Schema;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;

import org.nd4j.linalg.learning.config.Nesterovs;

import org.nd4j.linalg.lossfunctions.impl.LossMCXENT;


public class ModeliseData {

	private static final int seed =12345;
	private static final int numOutputs = 50;
	private static final double learningRate = 0.01;
	static Schema finalSchema;
	private MultiLayerConfiguration configuration;
	private MultiLayerNetwork model;
	private TransformProcess transformProcess;
	
	public ModeliseData(Schema finalSchema, RecordReaderDataSetIterator recordReaderDataSetIterator,TransformProcess transformProcess) throws IOException, InterruptedException {
		this.transformProcess=transformProcess;
		this.finalSchema = finalSchema;
		configuration = getDeepDenseLayerNetworkConfiguration();
		model = new MultiLayerNetwork(configuration);
		model.init();
		generateGraphe();

		model.fit(recordReaderDataSetIterator, 250);
		Test();

	}



	private static MultiLayerConfiguration getDeepDenseLayerNetworkConfiguration() {	
		MultiLayerConfiguration multiLayerConfiguration= new NeuralNetConfiguration.Builder()
				.seed(seed)
				// Algorithme d'optimisation
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
				// extension de l'algorithme d'optimisation
				.updater(new Nesterovs(learningRate,0.9))
				// Vitesse D'apprentissage
				.l2(0.001)
				// Gestion du poids de chaqu'un des neuronnes
				.weightInit(WeightInit.XAVIER)
				// Fonction d'activation
				.activation(Activation.RELU)
				// Chaque Dense Layer est un neurone et bénéfici des fonctions instancié au préalable
				.list(new DenseLayer.Builder().nOut(numOutputs).build(),
						new DenseLayer.Builder().nOut(numOutputs).build(),
						new DenseLayer.Builder().nOut(numOutputs).build(),
						new DenseLayer.Builder().nOut(numOutputs).build(),
						new DenseLayer.Builder().nOut(numOutputs).build(),
						new DenseLayer.Builder().nOut(numOutputs).build(),
						new DenseLayer.Builder().nOut(numOutputs).build(),
						new DenseLayer.Builder().nOut(numOutputs).build(),
						new DenseLayer.Builder().nOut(numOutputs).build(),
						new DenseLayer.Builder().nOut(numOutputs).build(),
						// Noeuds de sortie donc fonction de Cout 
						new OutputLayer.Builder(new LossMCXENT()).nOut(2).activation(Activation.SOFTMAX).build())
				.setInputType(InputType.feedForward(finalSchema.numColumns() - 1))
				.build();
		return multiLayerConfiguration;
	}
	
	private void Test() throws IOException, InterruptedException {

	TransformProcessRecordReader transformProcessRecordReader = new TransformProcessRecordReader(new CSVRecordReader(), transformProcess);
		transformProcessRecordReader.initialize( new FileSplit(new File("src/main/resources/Test/")));
		// On récupère
		RecordReaderDataSetIterator recordReaderDataSetIterator = new RecordReaderDataSetIterator.Builder(transformProcessRecordReader, 30)
				.classification(finalSchema.getIndexOfColumn("Potability"), 2)
				.build();
		// Pour Evaluer un modèle il suffit d'évaluer l'ensemble des test
		Evaluate(recordReaderDataSetIterator);
	}
	
	private void generateGraphe() {
		//Instanciation des variable
		UIServer uiServer = UIServer.getInstance();
		StatsStorage statsStorage = new InMemoryStatsStorage();
		uiServer.attach(statsStorage);
		// On ajoute des Listener qui permettent de suivre les résultats obtenue
		// On aura des message Log toutes les 50 itérations
		model.addListeners(new ScoreIterationListener(50));
		// Le graphique sera mis à jour tout les 100 itérations
		model.addListeners(new StatsListener(statsStorage, 100));

	}
	
	private void Evaluate(RecordReaderDataSetIterator recordReaderDataSetIterator)   {

			// Pour Evaluer un modèle il suffit d'évaluer l'ensemble des test
			Evaluation evaluate = model.evaluate(recordReaderDataSetIterator);
			System.out.println(evaluate.stats());
		}
}
