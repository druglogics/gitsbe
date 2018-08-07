package eu.druglogics.gitsbe;

public class OutputWeight {

	private String nodeName;
	private int weight;

	public OutputWeight(String nodeName, int weight) {
		this.nodeName = nodeName;
		this.weight = weight;
	}

	public String getName() {
		return nodeName;
	}

	public int getWeight() {
		return weight;
	}

}
