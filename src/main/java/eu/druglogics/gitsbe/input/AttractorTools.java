package eu.druglogics.gitsbe.input;

import java.util.ArrayList;

public enum AttractorTools {

    BNREDUCTION_FULL("bnet_reduction"),
    BNREDUCTION_REDUCED("bnet_reduction_reduced"),
    BIOLQM_STABLE_STATES("biolqm_stable_states"),
    BIOLQM_TRAPSPACES("biolqm_trapspaces"),
    MPBN_TRAPSPACES("mpbn_trapspaces");

    private String tool;

    AttractorTools(String tool) {
        this.tool = tool;
    }

    public String getTool() {
        return this.tool;
    }

    public static ArrayList<String> getTools() {
        ArrayList<String> tools = new ArrayList<>();
        for (AttractorTools attractorToolValue : AttractorTools.values())
            tools.add(attractorToolValue.getTool());
        return tools;
    }

    public static boolean contains(String tool) {
        for (AttractorTools attractorToolValue : AttractorTools.values()) {
            if (attractorToolValue.getTool().equals(tool)) {
                return true;
            }
        }
        return false;
    }
}
