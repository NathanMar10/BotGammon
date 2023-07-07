package ai;

public class GradientChecker {
	
	
	public static void main(String[] args) {
		NetworkHandler networkHandler = new NetworkHandler("bigBad_network.txt", Player.learningRate);
		double[] inputs = {-2, 0, 0, 0, 0, 5, 0, 3, 0, 0, 0, -5, 5, 0, 0, 0, -3,0, -5, 0, 0, 0, 0, 2, 0, 0, 0, 0};
		double[] outputs = {.5};
		
		
		
		networkHandler.computeDerivatives(inputs, outputs);
		
		for (int counter = 0; counter < 100; counter++) {
			int i, j;
			i = 1;
			j = 38;
			double inputChange = .000001;
			double origNetworkOutput = networkHandler.forwardPropagate(inputs)[0];
			double derivativeOfWeight = networkHandler.totalWeightDerivatives[i][j][counter];
			
		
			networkHandler.network.weights[i][j][counter] += inputChange;
			
			
			double[] changedNetworkOutput = networkHandler.forwardPropagate(inputs);
			
			System.out.println("--------\nOriginal Output: " + origNetworkOutput);
			System.out.println("New Output: " + changedNetworkOutput[0]);
			System.out.println("Change in Output: " + (origNetworkOutput - changedNetworkOutput[0]));
			System.out.println("Expected Change in Output: " + inputChange * derivativeOfWeight);
		}
		
		/*
		for (int counter = 0; counter < 100; counter++) {
			int i, j;
			i = 1;
			j = 67;
			double inputChange = .000001;
			double origNetworkOutput = networkHandler.forwardPropagate(inputs)[0];
			double derivativeOfWeight = networkHandler.totalWeightDerivatives[i][j][counter];
			
		
			networkHandler.network.weights[i][j][counter] += inputChange;
			
			
			double[] changedNetworkOutput = networkHandler.forwardPropagate(inputs);
			
			double originalError = origNetworkOutput - outputs[0];
			double newError = changedNetworkOutput[0] - outputs[0];
			System.out.println("--------\nOriginal Error: " + originalError);
			System.out.println("New Error: " + newError);
			System.out.println("Change in Error: " + (originalError - newError));
			System.out.println("Expected Change in Error: " + inputChange * derivativeOfWeight);
		}
		*/
		/*
		for (int counter = 0; counter < 150; counter++) {
			int i;
			i = 1;

			double inputChange = .000001;
			double origNetworkOutput = networkHandler.forwardPropagate(inputs)[0];
			double derivativeOfBias = networkHandler.totalBiasDerivatives[i][counter];
			
		
			networkHandler.network.biases[i][counter] += inputChange;
			
			
			double[] changedNetworkOutput = networkHandler.forwardPropagate(inputs);
			
			System.out.println("--------\nOriginal Output: " + origNetworkOutput);
			System.out.println("New Output: " + changedNetworkOutput[0]);
			System.out.println("Change in Output: " + (origNetworkOutput - changedNetworkOutput[0]));
			System.out.println("Expected Change in Output: " + inputChange * derivativeOfBias);
		}
		*/
	}
}
