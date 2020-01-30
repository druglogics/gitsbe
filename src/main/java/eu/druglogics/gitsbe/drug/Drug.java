package eu.druglogics.gitsbe.drug;

import java.util.ArrayList;

/**
 * @author john
 * Simplified version of Drabme same-name Class
 */
public class Drug {
	private String name;
	private ArrayList<String> targets;
	private boolean effect;

	public Drug(String name) {
		this.name = name;
		targets = new ArrayList<>();
	}

	public String getName() {
		return this.name;
	}

	void addEffect(boolean effect) {
		this.effect = effect;
	}

	boolean getEffect() {
		return effect;
	}

	void addTargets(ArrayList<String> targets) {
		for (String target: targets) {
			addTarget(target);
		}
	}

	private void addTarget(String target) {
		targets.add(target);
	}

	public ArrayList<String> getTargets() {
		return targets;
	}

}
