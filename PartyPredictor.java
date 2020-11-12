import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * This class learns a decision tree to predict the political party of members of the
 * house of representatives given voting records.
 *
 * @author Annie K. Lamar and Sarah Walling-Bell
 * @version 4.29.2019
 */

public class PartyPredictor {

	// private static final String FILE = "./src/voting-data.tsv";
	private static final String FILE = "voting-data.tsv";
	private static final int NUM_ISSUES = 10;
	private static final int NUM_PEOPLE = 430;
	private static final int YEA = 0;
	private static final int NEA = 1;
	private static final int OTH = 2;
	private static final int REP = 3;
	private static final int DEM = 4;


	// Has dimensionality (NUM_PEOPLE)x(NUM_ISSUES + 1)
	// The last column is the label democrat or republican
	private static int[][] data;

	public static void main(String[] args) {
		readData();
		System.out.println("Training on all examples....");
		Tree tree = trainTree(data, data, -1);
		printTree(tree);
		System.out.println("Evaluating accuracy....");

		//pull out 10% at a time
		int start = 0;
		int end = 42;
		double masterAccuracy = 0;
		for (int i = 0; i < 10; i ++) {
			//copy array
			//We are so sorry, Dr. Chambers.
			//We know the space and time complexity is horrible.
			int examplesCounter = 0;
			int testCounter = 0;
			int[][] examples = new int[NUM_PEOPLE - 43][NUM_ISSUES];
			int[][] test = new int[43][NUM_ISSUES];
			for (int e = 0; e < data.length; e ++) {
				if (!(e >= start && e <= end)) {
					examples[examplesCounter] = data[e];
					examplesCounter ++;
				} else {
					test[testCounter] = data[e];
					testCounter ++;
				}
			}
			//train again
			Tree eval = trainTree(examples, examples, -1);
			//for each example in test set
			int[][] results = new int[test.length][2];
			for (int k = 0; k < test.length; k ++) {
				int result = searchTree(test[k], eval);
				results[k][0] = test[k][NUM_ISSUES];
				results[k][1] = result;
			}
			int correct = 0;
			for (int j = 0; j < results.length; j ++) {
				if (results[j][0] == results[j][1])  correct ++;
			}
			//calculate accuracy
			double acc = (double) correct / 43;
			//add accuracy to master accuracy
			masterAccuracy += acc;
			start += 43;
			end += 43;
		}
		System.out.println("Average accuracy: " + masterAccuracy*10+ "%");
	}

	/*****************************************************
	 * 					PRIVATE METHODS
	 *****************************************************/

	/**
	 * Search the decision tree with the input example.
	 * @param example the example to search the Decision tree with
	 * @param tree the tree to search
	 * @return Democrat or Republican
	 */
	private static int searchTree(int[] example, Tree tree) {
		Node currentNode = tree.getRoot();
		while (!currentNode.isLeaf()) {
			int issue = currentNode.getType();
			int vote = example[issue];
			ArrayList<Node> children = honeyILostTheKids(currentNode, tree);
			for (Node child : children) {
				if (child.getValue() == vote) {
					currentNode = child;
					if (currentNode.isLeaf()) {
						return currentNode.getType();
					}
				}
			}
		}
		return -1;
	}

	/**
	 * Print out the tree.
	 * @param tree the tree to print
	 */
	private static void printTree(Tree tree) {
		depthFirstSearch(tree, tree.getRoot());
	}

	/**
	 * Use depth first search to print out the tree.
	 * @param tree the tree to search
	 * @param start the start node
	 */
	private static void depthFirstSearch(Tree tree, Node start) {
		Stack<Node> stack = new Stack<Node>();
		Set<Node> nodes = tree.getNodeSet();
		HashMap<Node, Boolean> visited = new HashMap<Node, Boolean>();
		for (Node node : nodes) {
			visited.put(node, false);
		}
		stack.push(start);
		start.changeTabCount(0);
		while (!stack.isEmpty()) {
			Node u = stack.pop();
			if (!visited.get(u)) {
				visited.put(u, true);
				//print information about the Node we just found!
				if (u.isLeaf()) {
					System.out.println(getTabs(u)+printValue(u) + " " + printOutputType(u));
				} else if (u.getValue() == -1) {
					System.out.println("Issue " + u.getType() + ":");
				} else {
					System.out.println(getTabs(u)+printValue(u) + " Issue " + u.getType() + ":");
				}
				ArrayList<Node> children = honeyILostTheKids(u, tree);
				for (Node child : children) {
						stack.push(child);
						child.changeTabCount(u.getTabCount()+1);
				}
			}
		}
	}

	private static String printValue(Node u) {
		if (u.getValue() == 0) return "+";
		if (u.getValue() == 1) return "-";
		return ".";
	}

	private static String printOutputType(Node u) {
		if (u.getType() == 3) return "R";
		return "D";
	}

	private static String getTabs(Node u) {
		String s = new String();
		int counter = u.getTabCount();
		for (int i = 0; i < counter; i ++) {
			s += "\t";
		}
		return s;
	}

	private static ArrayList<Node> honeyILostTheKids(Node parent, Tree tree) {
		Set<Node> nodes = tree.getNodeSet();
		ArrayList<Node> children = new ArrayList<Node>();
		for (Node node : nodes) {
			if (node.hasParent() == true && node.getParent().equals(parent)) {
					children.add(node);
			}
		}
		return children;
	}

	// Initializes and populates the data matrix from file.
	private static void readData(){
		data = new int[NUM_PEOPLE][NUM_ISSUES+1];
		try{
			int personId = 0;
			Scanner scan = new Scanner(new File(FILE));
			while(scan.hasNextLine()){
				assert(personId < NUM_PEOPLE);
				String line = scan.nextLine();
				String[] info = line.split("\t");
				assert(info.length == 3);

				// Store the voting record for this particular person
				String votingRecord = info[2];
				assert(votingRecord.length() == NUM_ISSUES);
				int i;
				for(i = 0; i < votingRecord.length(); i++){
					String vote = votingRecord.substring(i,i+1);
					if(vote.equals("+")){
						data[personId][i] = YEA;
					}
					else if(vote.equals("-")){
						data[personId][i] = NEA;
					}
					else if(vote.equals(".")){
						data[personId][i] = OTH;
					}
					else{
						System.out.println("Found unknown token");
					}
				}
				assert(i < data[personId].length);

				// Store the label (democrat or republican) for this person
				if(info[1].equals("D")){
					data[personId][i] = DEM;
				}
				else if(info[1].equals("R")){
					data[personId][i] = REP;
				}
				else{
					System.out.println("Found neither D nor R");
				}
				personId++;
			}
			scan.close();
		}
		catch(FileNotFoundException e){
			System.out.println(e);
		}
	}

	//train tree with whole dataset
	private static Tree trainTree(int[][] examples, int[][] parent_examples, int value) {

		//base cases
		if (examples.length == 0) {
			//Plurality value needs to return a node with TYPE = plurality type (Rep, Dem), and VALUE = value, IS A LEAF NODE
			return new Tree(pluralityValue(parent_examples, value));
		}
		else if (sameClassification(examples)) {
			int output = examples[0].length -1;
			//return a Tree with one node with remaining type, VALUE, and ISLEAFNODE
			return new Tree(new Node(examples[0][output], value, true));
		}
		else if ((examples[0].length-1) == 0) { //no more attributes left
			return new Tree(pluralityValue(examples, value));
		}
		//non-base case
		else {
			int A = maxImportance(examples);

			Node root = new Node(A, -1, false);
			Tree tree = new Tree(root);

			//for now this is always three, but in our final project, will be variable
			for (int i = 0; i < 3; i ++) { //for each value in A
				int[][] exs = getAllExamples(examples, A, i);

				if (exs.length != 0) {
					exs = removeAttribute(exs, A);
					Tree subtree = trainTree(exs, examples, i);
					//set the parent of the root of subtree to root of Tree
					subtree.getRoot().setParent(root);
					//set the value of that node to be i
					subtree.getRoot().setValue(i);
					//copy the subTree's nodeSet to the Tree's nodeSet
					tree.addToNodeSet(subtree.getNodeSet());
				} else {
					return new Tree(pluralityValue(examples, value));
				}

			}
			return tree;
		}
	}

	/**
	 * Returns a new node with the value of the most common output of all examples.
	 * @param examples
	 * @param value
	 * @return
	 */
	private static Node pluralityValue(int[][] examples, int value) {
		int output = examples[0].length -1;
		int pluralityType;
		int numReps = 0;
		int numDems = 0;
		for (int[] array : examples) {
			if (array[output] == 3) numReps ++;
			if (array[output] == 4) numDems ++;
		}
		if (numDems > numReps) pluralityType = 4;
		else pluralityType = 3;
		Node newNode = new Node(pluralityType, value, true);
		return newNode;
	}

	private static int[][] removeAttribute(int[][] examples, int attributeToRemove) {
		//return a new set of examples with that attribute removed
		//attributeToRemove is a COLUMN NUMBER
		int[][] newExamples = new int[examples.length][examples[0].length-1];
		for (int array = 0; array < examples.length; array ++) {
			int columnCounter = 0;
			for (int column = 0; column < examples[0].length; column++) {
				if (column != attributeToRemove) {
					newExamples[array][columnCounter] = examples[array][column];
					columnCounter ++;
				}
			}
		}
		return newExamples;
	}

	/**
	 * Returns all examples with the desired value of the desired attribute.
	 * @param examples set of examples
	 * @param colNumber the attribute number
	 * @param desiredNumber the value of the attribute
	 * @return all examples with the desired value of the desired attribute
	 */
	private static int[][] getAllExamples(int[][] examples, int colNumber, int desiredNumber) {
		int counter = 0;
		for (int[] array : examples) {
			if (array[colNumber] == desiredNumber) counter ++;
		}
		int[][] newExamples = new int[counter][NUM_ISSUES];
		int newCounter = 0;
		for (int[] array : examples) {
			if (array[colNumber] == desiredNumber) {
				newExamples[newCounter] = array;
				newCounter ++;
			}
		}
		return newExamples;
	}

	/**
	 * Returns true if all remaining examples have the same output.
	 * @param examples examples to consider
	 * @return true if all remaining examples have the same output, false otherwise
	 */
	private static boolean sameClassification(int[][] examples) {
		int output = examples[0].length - 1;
		int firstValue = 0;
		boolean firstLoop = true;
		for (int[] array : examples) {
			if (firstLoop) { //first pass through the loop
				firstValue = array[output];
				firstLoop = false;
			} else if (array[output] != firstValue) {
				return false;
			}
		}
		return true;
	}

	private static int maxImportance(int[][] examples) {
		/**
		 * Step one: calculate entropy of entire set
		 * H(S)=(-1)[(#REP/#tot)*log_2(#REP/#tot) + (#DEM/#tot)*log_2(#DEM/#tot)]
		 */
		double numReps = count(examples, 3);
		double numDems = count(examples, 4);

		double total = examples.length;

		double repDivide = numReps/total;
		double demDivide = numDems/total;

		double logRepDivide = Math.log(repDivide) / Math.log(2);
		double logDemDivide = Math.log(demDivide) / Math.log(2);

		double H = (-1)*((repDivide*logRepDivide) + (demDivide*logDemDivide));

		/**
		 * Step two: calculate R(A) for each attribute (i.e. column in this case)
		 */
		double[] RAvals = new double[examples[0].length-1];
		for (int a = 0; a < examples[0].length - 1; a ++) {

			//Step 2.1 calculate H(Sk) for each value of attribute a
			//value: yea
			int[][] yeaExamples = getAllExamples(examples, a, 0);
			numReps = count(yeaExamples, 3);
			numDems = count(yeaExamples, 4);

			total = yeaExamples.length; //how many yeas on issue a

			repDivide = numReps/total;
			demDivide = numDems/total;

			logRepDivide = Math.log(repDivide) / Math.log(2);
			logDemDivide = Math.log(demDivide) / Math.log(2);

			double Hyea = (-1)*((repDivide*logRepDivide) + (demDivide*logDemDivide));
			if (Double.isNaN(Hyea)) Hyea = 0.0;

			//value: nea
			int[][] neaExamples = getAllExamples(examples, a, 1);
			numReps = count(neaExamples, 3);
			numDems = count(neaExamples, 4);

			total = neaExamples.length; //how many yeas on issue a

			repDivide = numReps/total;
			demDivide = numDems/total;

			logRepDivide = Math.log(repDivide) / Math.log(2);
			logDemDivide = Math.log(demDivide) / Math.log(2);

			double Hnea = (-1)*((repDivide*logRepDivide) + (demDivide*logDemDivide));
			if (Double.isNaN(Hnea)) Hnea = 0.0;

			//value: abstain
			int[][] absExamples = getAllExamples(examples, a, 2);
			numReps = count(absExamples, 3);
			numDems = count(absExamples, 4);

			total = absExamples.length; //how many yeas on issue a

			repDivide = numReps/total;
			demDivide = numDems/total;

			logRepDivide = Math.log(repDivide) / Math.log(2);
			logDemDivide = Math.log(demDivide) / Math.log(2);

			double Habs = (-1)*((repDivide*logRepDivide) + (demDivide*logDemDivide));
			if (Double.isNaN(Habs)) Habs = 0.0;
			//Step 2.2 calculate R(A) for attribute a
			double numYea = countAttributeValues(examples, a, 0);
			double numNea = countAttributeValues(examples, a, 1);
			double numAbs = countAttributeValues(examples, a, 2);
			total = examples.length;
			double yeaPortion = (numYea/total)*Hyea;
			double neaPortion = (numNea/total)*Hnea;
			double absPortion = (numAbs/total)*Habs;
			double R = yeaPortion + neaPortion + absPortion;
			RAvals[a] = R;
		}
		/**
		 * Step three: calculate information gain for each attribute
		 */
		double[] IG = new double[examples[0].length-1];
		for (int i = 0; i < RAvals.length; i ++) {
			IG[i] = H - RAvals[i];
		}
		/**
		 * Step four: get max information gain
		 */
		double max = -100;
		boolean firstTime = true;
		int attributeNumber = Integer.MIN_VALUE;
		for (int g = 0; g < IG.length; g ++) {
			if (firstTime == true) {
				max = IG[g];
				attributeNumber = g;
				firstTime = false;
			}
			else if (IG[g] > max) {
				max = IG[g];
				attributeNumber = g;
			}
		}
		return attributeNumber;
	}

	private static double count(int[][] examples, int output) {
		double number = 0.0;
		for (int[] array : examples) {
			if (array[examples[0].length-1] == output) number += 1.0;
		}
		return number;
	}

	private static double countAttributeValues(int[][] examples, int attribute, int input) {
		double count = 0.0;
		for (int[] array : examples) {
			if (array[attribute] == input) count += 1.0;
		}
		return count;
	}

	/**
	 * Nested Node class.
	 * @author Annie K. Lamar
	 * @version 4.25.2019
	 */
	private static class Node {

		/**
		 * If the node is a non-leaf node, the attributeType is the issue number.
		 * In other examples, an attributeType is Patrons?, Hungry? Fri/Sat?
		 * If the node is a leaf node, the attributeType is 3 (REP) or 4 (DEM).
		 * In other examples, this is Yes/no.
		 * In figure 18.6, the value of the Hungry node is Full, and the type is "Hungry.".
		 * The attributeValue is 0 (YEA), 1 (NEA), 2 (OTH).
		 * In other examples, this is "Italian." IT IS THE ATTRIBUTE YOU SPLIT ON TO GET TO THIS NODE.
		 */
		private int attributeType; //issue number for non-leaf nodes, for leaf-nodes, this is the output
		private int attributeValue; //the int that represents the value of the attribute
		private boolean isLeafNode;
		private Node parent;
		private int tabMeBaby;

		/**
		 * First constructor for Node objects.
		 * @param attributeType attribute number for non-leaf nodes, output for leaf nodes
		 * @param attributeValue attribute that got us to this node
		 * @param isLeafNode boolean to represent if this is a leaf node
		 */
		private Node(int attributeType, int attributeValue, boolean isLeafNode) {
			this.attributeType = attributeType;
			this.attributeValue = attributeValue;
			this.isLeafNode = isLeafNode;
		}

		/**
		 * Second constructor for Node objects.
		 * @param attributeType attribute number for non-leaf nodes, output for leaf nodes
		 * @param attributeValue attribute that got us to this node
		 * @param isLeafNode boolean to represent if this is a leaf node
		 * @param parent the parent of the new Node
		 */
		private Node(Node parent, int attributeType, int attributeValue, boolean isLeafNode) {
			this.attributeType = attributeType;
			this.attributeValue = attributeValue;
			this.parent = parent;
			this.isLeafNode = isLeafNode;
			//tabMeBaby = parent.getTabCount() + 1;
		}

		private int getTabCount() {
			return tabMeBaby;
		}

		private void changeTabCount(int newCount) {
			tabMeBaby = newCount;
		}

		private Node getParent() {
			return parent;
		}

		private void setParent(Node newParent) {
			parent = newParent;
		}

		private int getType() {
			return attributeType;
		}

		private int getValue() {
			return attributeValue;
		}

		private void setValue(int newValue) {
			attributeValue = newValue;
		}

		private boolean isLeaf() {
			return isLeafNode;
		}

		private boolean hasParent() {
			if (parent != null) return true;
			return false;
		}
	}

	/**
	 * The Tree class can create a Tree object.
	 * A Tree object has a root and a nodeSet, a collection of the all the nodes in the tree.
	 * You can add a new Node or add an existing nodeSet to the Tree object.
	 * You can also get back the root or the nodeSet.
	 * @author Annie K. Lamar
	 * @version 4.29.2019
	 */
	private static class Tree {
		private Node root; //root of the Tree
		private Set<Node> nodeSet; //set of all Nodes in the Tree

		/**
		 * Constructor for Tree objects.
		 * @param root the root of the Tree.
		 */
		private Tree(Node root) {
			this.root = root;
			nodeSet = new HashSet<Node>();
			nodeSet.add(root);
		}

		/**
		 * Returns the root of the Tree.
		 * @return the root of the Tree.
		 */
		private Node getRoot() {
			return root;
		}

		/**
		 * Returns the nodeSet of the Tree.
		 * @return the nodeSet of the Tree.
		 */
		private Set<Node> getNodeSet() {
			return nodeSet;
		}

		/**
		 * Adds a set of Nodes to the nodeSet of the Tree.
		 * @param nodes the set of Nodes to add to the nodeSet.
		 */
		private void addToNodeSet(Set<Node> nodes) {

			for (Node n : nodes) {
				boolean goodToAdd = true;
				for (Node node : nodeSet) {
					if (n.isLeaf() == false && node.isLeaf() == false && n.getType() == node.getType() && n.getParent().equals(node.getParent())) {
						goodToAdd = false;
					}
				}
				if (goodToAdd == true) nodeSet.add(n);
			}
		}

	}
}
