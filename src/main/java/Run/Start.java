package Run;

import java.io.IOException;

import ModeliseData.ModeliseData;
import PrepareData.AccessingData;
import PrepareData.LoadingData;

public class Start {

	public static void main(String[] args) throws IOException, InterruptedException {
		
		AccessingData accessingData= new AccessingData();
		LoadingData loadingData = new LoadingData();
		ModeliseData modeliseData = new ModeliseData(loadingData.getSchema(),loadingData.getRecordReaderDataSetIterator(),loadingData.getTransformProcess());
		

	}

}
