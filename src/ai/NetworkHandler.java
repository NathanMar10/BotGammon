package ai;

import java.util.*;

import org.ejml.data.DMatrixRMaj;
import org.ejml.simple.SimpleMatrix;


public class NetworkHandler {

	/*

cleanup todo
	 * fix the variable declarations
	 * remove unecessary comments
	 * put pretty comments
	 * organize methods
	 * run backprop off of matrices
	 * handle the numerical stuff outside of the method
	 * consolidate derivative matrix methods
	 * make this static?
	 * Saving best network?
	 * make default network more streamlined / require less input -> also make qualitystate an interface
	 * im gonna handle ML through the training GUI but eventually i dont think i want to do that
	 */

	public static double stochasticLearningRate = .001;
	private final int layerCount;
	private final int[] nodesInLayer;
	
	public double[][][] totalWeightDerivatives;
	public double[][] totalBiasDerivatives;
	
	public Network network;
	private double learningRate, networkError, bestError = 1000, totalDeriv;
	
	// Change this to fit the application (if necessary)
	private static int[] defaultNetworkShape = {28, 300, 100, 1};
	
	// Constructors ============================================================
	public NetworkHandler(int[] nodesInLayer, double learningRate) {
		network = new Network(nodesInLayer);
		this.learningRate = learningRate;
		this.nodesInLayer = nodesInLayer;
		layerCount = nodesInLayer.length;

	}
	public NetworkHandler(double learningRate) {
		this(defaultNetworkShape, learningRate);
	}
	public NetworkHandler(String networkFile, double learningRate) {
		network = Network.readNetwork(networkFile);
		if (network == null) {
			network = new Network(defaultNetworkShape);
		}
		this.learningRate = learningRate;
		this.nodesInLayer = network.nodesInLayer;
		layerCount = nodesInLayer.length;
	}
	// Forward Propagation Methods =============================================
	public double[] forwardPropagate(double[] firstLayerValues) {
		network.values[0] = firstLayerValues;
		for (int i  = 1; i < layerCount; i++) {
			SimpleMatrix w = new SimpleMatrix(network.weights[i - 1]);
			SimpleMatrix p = new SimpleMatrix(new DMatrixRMaj(nodesInLayer[i - 1], 1, true, network.values[i - 1]));
			SimpleMatrix b = new SimpleMatrix(new DMatrixRMaj(nodesInLayer[i], 1, true, network.biases[i]));
			SimpleMatrix t;
			t = w.mult(p).plus(b);
			DMatrixRMaj theseValues = t.getMatrix();
			for (int j = 0; j < theseValues.numRows; j++) {
				network.values[i][j] = theseValues.get(j);
			}
			reLU(network.values[i]);
		}
		return network.values[layerCount - 1];
	}
	private void reLU(double[] vector) {
		for (int i = 0; i < vector.length; i++) {
			vector[i] = reLU(vector[i]);
		}
	}

	// Backward Propagation Methods ============================================
	public double backPropagate(double[][] data, double[][] expectedOutput) {
		//System.out.println(network);
		// input comes in as a 2d array of data and a 2 d array of outputs -> outputs are nx1 though for this implementation
		int testSets = 0;
		double totalError = 0;
		totalDeriv = 0;
		
		// For each entry in data and outputs, calculate the derivatives
		for (int set = 0; set < data.length; set++) {
			testSets++;
			//System.out.println(computeDerivatives(data[set], expectedOutput[set]));
			totalError += computeDerivatives(data[set], expectedOutput[set]); 
		}
		// Derivatives are totaled
		
		double avgDeriv = totalDeriv / testSets;
		
		for (int i = 0; i < totalWeightDerivatives.length; i++) {
			for (int j = 0; j < totalWeightDerivatives[i].length; j++) {
				for (int k = 0; k < totalWeightDerivatives[i][j].length; k++) {

					// value times learningrate SHOULD be the "associated error" within a situation
					/*
					 * TWD/testSets = average derivative for this weight across all test data -> how much a change in value will cause change in error across all sets
					 * IF i divide total error by this, i get how much a change in input will change value to minimize error
					 * 
					 * derivative = dOutputs / dInput -> i have a desired change in output, totalError / testSets
					 */
					
					network.weights[i][j][k] -= learningRate * totalWeightDerivatives[i][j][k] / testSets * totalError / testSets;
					network.biases[i][k] -=  learningRate * totalBiasDerivatives[i][k] / testSets * totalError / testSets;
					

					
					/*
					network.weights[i][j][k] -= learningRate * (totalWeightDerivatives[i][j][k] / testSets) * totalError / testSets / (totalDeriv / testSets);
					network.biases[i][k] -=  learningRate * (totalBiasDerivatives[i][k] / testSets) * totalError / testSets / (totalDeriv / testSets);
					/*
					//System.out.println(learningRate * (totalWeightDerivatives[i][j][k] / testSets) * totalError / testSets / (totalDeriv / testSets));
					/*
					network.weights[i][j][k] -= learningRate * totalWeightDerivatives[i][j][k] / totalDeriv / testSets;
					network.biases[i][k] -=  learningRate * totalBiasDerivatives[i][k] / totalDeriv / testSets;
					*/
					//System.out.println(totalWeightDerivatives.get(i)[j][k] / totalDeriv * learningRate / testSets);
				}
				
			}
			//System.out.println(Arrays.deepToString(totalWeightDerivatives.get(i)));
			//System.out.println(Arrays.toString(totalBiasDerivatives.get(i)));
		}
		
		//System.out.println(testSets);
		//System.out.println(totalError / testSets);
		networkError = totalError/testSets;
		//System.out.println(totalError);
		//System.out.println(testSets);
		return networkError;
	}
	
	double stochasticDescent(double[][] data, double[] expectedOutput) {
		shuffleArray(data);
		double totalError = 0;
		int dataLength = data.length;
		for (int state = 0; state < dataLength; state++) {
			double output = forwardPropagate(data[state])[0];
			double[][][] weightDerivatives = new double[layerCount - 1][][];
			double[][] biasDerivatives = new double[layerCount][];
			double[][] valueDerivatives = new double[layerCount][];
			
			for (int i = 0; i < layerCount - 1; i++) {
				weightDerivatives[i] = new double[nodesInLayer[i + 1]][nodesInLayer[i]];
			}
			for (int i = 0; i < layerCount; i++) {
				biasDerivatives[i] = new double[nodesInLayer[i]];
				valueDerivatives[i] = new double[nodesInLayer[i]];
			}
				
				// Actually Calculating the Derivatives
			
			for (int i = 0; i < valueDerivatives[layerCount - 1].length; i++) {
				// Derivatives are all WRT output, not cost. That comes later.
				valueDerivatives[layerCount - 1][i] = 1;
			}
				
			// Iterates through each layer and sets the derivatives weights->values->biases
			for (int layer = layerCount - 2; layer >= 0; layer--) {

				// Weight Derivatives between layer and layer + 1
				for (int endNode = 0; endNode < weightDerivatives[layer].length; endNode++) {
					for (int startNode = 0; startNode < weightDerivatives[layer][endNode].length; startNode++) {
						// weight deriv = start node value * end node deriv
						weightDerivatives[layer][endNode][startNode] = network.values[layer][startNode] * valueDerivatives[layer + 1][endNode];
					}
				}
				
				
				// Value Derivatives on layer
				for (int startNode = 0; startNode < valueDerivatives[layer].length; startNode++) {
					// value derivative equals sum of weights times their respective following value derivatives
					double derivative = 0;
					// calcing derivative for each startNode, which entails looking through each endnode after it
					for (int endNode = 0; endNode < network.values[layer + 1].length; endNode++) {
						
						derivative += (valueDerivatives[layer + 1][endNode] * network.weights[layer][endNode][startNode]) * reLUDerivative(network.values[layer][startNode]);
						
						
					}
					valueDerivatives[layer][startNode] = derivative;
				}
				
				// then do bias derivatives
				for (int nodeNum = 0; nodeNum < biasDerivatives[layer].length; nodeNum++) {
					biasDerivatives[layer][nodeNum] = valueDerivatives[layer][nodeNum];
				}
				
				
				
				
			}
				
			// All derivatives should have been calculated here -> cool!
			
			double desiredChange = expectedOutput[state] - output;
			totalError += Math.abs(desiredChange);
			/*
			 * What to do here?
			 * I know how much i want to change and i know how much each nudge will do to the final output
			 * Therefore, just update derivative * desiredChange?
			 * derivative = change in weight / change in output
			 * desiredChange = desired change in output
			 * derivative * desiredChange = desiredchangeinweight
			 */
				
			for (int i = 0; i < weightDerivatives.length; i++) {
				for (int j = 0; j < weightDerivatives[i].length; j++) {
					for (int k = 0; k < weightDerivatives[i][j].length; k++) {


						network.weights[i][j][k] -= learningRate * weightDerivatives[i][j][k] * desiredChange;
						network.biases[i][k] -=  learningRate * biasDerivatives[i][k] * desiredChange;
						


					}
					
				}
			}
			
			
		}
		
		
		
		
		
		
		return totalError / dataLength;
	}
	
	 double computeDerivatives(double[] input, double[] expectedOutput) {
		double[] outputs = forwardPropagate(input);
		double[][][] weightDerivatives = new double[layerCount - 1][][];
		double[][] biasDerivatives = new double[layerCount][];
		double[][] valueDerivatives = new double[layerCount][];
		
		for (int i = 0; i < layerCount - 1; i++) {
			weightDerivatives[i] = new double[nodesInLayer[i + 1]][nodesInLayer[i]];
			// represent all of the derivatives for a layer's weights, first index is ending node
			// second index is starting node -> starting node should be on the layer of very first index?
		}
		for (int i = 0; i < layerCount; i++) {
			// represents derivatives for biases and values on a layer. values are values on that layer. baises are biases for each node on a layer?
			biasDerivatives[i] = new double[nodesInLayer[i]];
			valueDerivatives[i] = new double[nodesInLayer[i]];
		}
			
		//System.out.println(outputs[0] + ", " + expectedOutput[0]); //-> values are hilariously bad
		clearDerivatives();
		
		
		// ----------------------------------------Final Layer Value Derivatives
		// Sets the Value Derivatives for the output layer of the network
		for (int i = 0; i < valueDerivatives[layerCount - 1].length; i++) {
			// First layer value derivative should actually just be one?
			valueDerivatives[layerCount - 1][i] = outputs[i] < expectedOutput[i] ? -1 : 1;//(outputs[i] - expectedOutput[i]);
		}

		
		// ----------------------------------------N-1 layer Derivatives
		// Iterates through each layer and sets the derivatives weights->values->biases
		for (int layer = layerCount - 2; layer >= 0; layer--) {

			// Weight Derivatives between layer and layer + 1
			for (int endNode = 0; endNode < weightDerivatives[layer].length; endNode++) {
				for (int startNode = 0; startNode < weightDerivatives[layer][endNode].length; startNode++) {
					// weight deriv = start node value * end node deriv
					weightDerivatives[layer][endNode][startNode] = network.values[layer][startNode] * valueDerivatives[layer + 1][endNode];
				}
			}
			
			
			// Value Derivatives on layer
			for (int startNode = 0; startNode < valueDerivatives[layer].length; startNode++) {
				// value derivative equals sum of weights times their respective following value derivatives
				double derivative = 0;
				// calcing derivative for each startNode, which entails looking through each endnode after it
				for (int endNode = 0; endNode < network.values[layer + 1].length; endNode++) {
					
					derivative += (valueDerivatives[layer + 1][endNode] * network.weights[layer][endNode][startNode]) * reLUDerivative(network.values[layer][startNode]);
					
					
				}
				valueDerivatives[layer][startNode] = derivative;
			}
			
			// then do bias derivatives
			for (int nodeNum = 0; nodeNum < biasDerivatives[layer].length; nodeNum++) {
				biasDerivatives[layer][nodeNum] = valueDerivatives[layer][nodeNum];
			}
		}
		for (int i = 0; i < totalWeightDerivatives.length; i++) {
			for (int j = 0; j < totalWeightDerivatives[i].length; j++) {
				for (int k = 0; k < totalWeightDerivatives[i][j].length; k++) {
					totalWeightDerivatives[i][j][k] += weightDerivatives[i][j][k];
					totalBiasDerivatives[i][k] += biasDerivatives[i][k];
					totalDeriv += Math.abs(weightDerivatives[i][j][k]);
					totalDeriv += Math.abs(biasDerivatives[i][k]);
				}
			}
		}
		
		double error = 0;
		for (int i = 0; i < expectedOutput.length; i++) {
			error += outputs[i] - expectedOutput[i];
			//error += Math.pow(outputs[i] -  expectedOutput[i], 2);// * (outputs[i] < expectedOutput[i] ? -1: 1);
			//System.out.println(outputs[i]);
		}
		return error;
	}
	
	private void clearDerivatives() {
		totalWeightDerivatives = new double[layerCount - 1][][];
		totalBiasDerivatives = new double[layerCount][];
		for (int i = 0; i < layerCount - 1; i++) {
			totalWeightDerivatives[i] = new double[nodesInLayer[i + 1]][nodesInLayer[i]];
		}
		for (int i = 0; i < layerCount; i++) {
			totalBiasDerivatives[i] = new double[nodesInLayer[i]];
		}
	}
	
	private double reLU(double value) {
		return Math.max(value * .01, value);
	}
	private double reLUDerivative(double value) {
		if (value > 0) {
			return 1;
		} else {
			return 0.01;
		}
	}
	// I/O Methods
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(layerCount).append(" Layer Network: \nNode Count: ");
		// Nodes In Layers -----------------------------------------------------
		for (int nodeCount : nodesInLayer) {
			sb.append(nodeCount).append(" ");
		}
		// Node Values ---------------------------------------------------------
		sb.append("\nNode Values: \n");
		for (double[] values : network.values) {
			sb.append(Arrays.toString(values)).append("\n");
		}
		// Node Weights --------------------------------------------------------
		sb.append("Node Weights: \n");
		for (double[][] weights : network.weights) {
			for (double[] nodeWeights : weights) {
				sb.append(Arrays.toString(nodeWeights)).append("\n");
			}
			sb.append("-------------------------------\n");
		}
		// Node Biases ---------------------------------------------------------
		sb.append("Node Biases: \n");
		for (double[] biases : network.biases) {
			sb.append(Arrays.toString(biases)).append("\n");
		}
		return sb.toString();
	}	
	public Network getNetwork() {
		return network;
	}
	public void setNetwork(Network network) {
		this.network = network;
	}
	
	private void shuffleArray(double[][] data) {
		ArrayList<double[]> dataList = new ArrayList<double[]>();
		for (int i = 0; i < data.length; i++) {
			dataList.add(data[i]);
		}
		Collections.shuffle(dataList);
		for (int i = 0; i < data.length; i++) {
			data[i] = dataList.get(i);
		}
	}
}

