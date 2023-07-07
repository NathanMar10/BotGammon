package ai;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Random;

public class Network {

	final int layerCount;
	final int[] nodesInLayer;
	double[][][] weights;
	double[][] biases, values;
	private static Random random = new Random();
	
	
	// Constructors ============================================================
	public Network(int[] nodesInLayer) {
		this.nodesInLayer = nodesInLayer;
		layerCount = nodesInLayer.length;
		
		createMatrices();
		generateWeights();
	}
	public Network(Network network) {
		this.nodesInLayer = network.nodesInLayer;
		layerCount = network.layerCount;
		
		createMatrices();
		copyValues(network);
	}
	// Private Constructor Helpers =============================================
	private void createMatrices() {
		weights = new double[layerCount - 1][][];
		biases = new double[layerCount][];
		values = new double[layerCount][];
		
		for (int i = 0; i < layerCount - 1; i++) {
			weights[i] = (new double[nodesInLayer[i + 1]][nodesInLayer[i]]);
		}
		for (int i = 0; i < layerCount; i++) {
			biases[i] = new double[nodesInLayer[i]];
			values[i] = new double[nodesInLayer[i]];
		}
	}
	private void generateWeights() {
		for (int i = 0; i < weights.length; i++) {
			for (int j = 0; j < weights[i].length; j++) {
				for (int k = 0; k < weights[i][j].length; k++) {
					weights[i][j][k] = random.nextDouble() * .2 - .1;
				}
			}
		}
	}
	private void copyValues(Network network) {
		for (int i = 0; i < weights.length; i++) {
			for (int j = 0; j < weights[i].length; j++) {
				for (int k = 0; k < weights[i][j].length; k++) {
					weights[i][j][k] = network.weights[i][j][k];			
				}
			}
		}
		for (int i = 0; i < biases.length; i++) {
			for (int j = 0; j < biases[i].length; j++) {
				biases[i][j] = network.biases[i][j];
				values[i][j] = network.values[i][j];
			}
		}
	}
	// I/O =====================================================================
	public static boolean saveNetwork(Network network, String fileName) {
		try {
			File saveFile = new File(fileName);
			saveFile.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile));
			writer.write(network.toString());
			writer.close();
			} catch (Exception e) {
				System.err.println("Error Writing TestSet to File");
				e.printStackTrace();
				return false;
			}
		return true;
	}
	public static Network readNetwork(String fileName) {
		Network newNetwork;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
			reader.readLine();
			// Nodes in Layer --------------------------------------------------
			reader.readLine();
			String nodeString = reader.readLine();
			String[] stringArr = nodeString.substring(1, nodeString.length() - 1).split(", ");
			int[] nodes = new int[stringArr.length];
			for (int i = 0; i < nodes.length; i++) {
				nodes[i] = Integer.parseInt(stringArr[i]);
			}
			// Weights ---------------------------------------------------------
			reader.readLine();
			String weightString = reader.readLine();
			weightString = weightString.substring(3, weightString.length() - 3);
			String [] layerWeights = weightString.split("\\]\\], \\[\\[");
			String [] tempArr, tempArr2;
			double [][][] weights = new double[nodes.length - 1][][];
			for (int layer = 0; layer < layerWeights.length; layer++) {
				tempArr = layerWeights[layer].split("\\], \\[");
				weights[layer] = new double[tempArr.length][];
				for (int node = 0; node < tempArr.length; node++) {
					tempArr2 = tempArr[node].split(", ");
					weights[layer][node] = new double[tempArr2.length];
					for (int pre = 0; pre < tempArr2.length; pre++) {
						weights[layer][node][pre] = Double.parseDouble(tempArr2[pre]);
					}
				}
			}
			// Biases ----------------------------------------------------------
			reader.readLine();
			String biasString = reader.readLine();
			biasString = biasString.substring(2, biasString.length() - 2);
			String [] layerBiases = biasString.split("\\], \\[");
			double [][] biases = new double[nodes.length][];
			for (int layer = 0; layer < layerBiases.length; layer++) {
				tempArr = layerBiases[layer].split(", ");
				biases[layer] = new double[tempArr.length];
				for (int node = 0; node < tempArr.length; node++) {
					biases[layer][node] = Double.parseDouble(tempArr[node]);
				}
			}
			// Creating New Network --------------------------------------------
			newNetwork = new Network(nodes);
			newNetwork.weights = weights;
			newNetwork.biases = biases;
			// Finishing Up ----------------------------------------------------
			reader.close();
			} catch (Exception e) {
				System.err.println("Error Reading Network from File");
				e.printStackTrace();
				newNetwork = null;
			}
		return newNetwork;
	}
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("-----------------------------------------\n");
		sb.append("Nodes In Layer:\n" + Arrays.toString(nodesInLayer));
		sb.append("\nWeights:\n" + Arrays.deepToString(weights));
		sb.append("\nBiases:\n" + Arrays.deepToString(biases));
		sb.append("\n-----------------------------------------\n");
		return sb.toString();
	}
}
