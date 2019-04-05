package eu.druglogics.gitsbe.input;

public enum AttractorTools {

    BNREDUCTION_FULL("bnet_reduction"),
    BNREDUCTION_REDUCED("bnet_reduction_reduced");

    private String tool;

    AttractorTools(String tool) {
        this.tool = tool;
    }

    public String getTool() {
        return this.tool;
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
