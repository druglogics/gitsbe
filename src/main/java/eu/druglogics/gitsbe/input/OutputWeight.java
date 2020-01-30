package eu.druglogics.gitsbe.input;

public class OutputWeight {

	private String nodeName;
	private int weight;

	OutputWeight(String nodeName, int weight) {
		this.nodeName = nodeName;
		this.weight = weight;
	}

	public String getNodeName() {
		return nodeName;
	}

	public int getWeight() {
		return weight;
	}

}
