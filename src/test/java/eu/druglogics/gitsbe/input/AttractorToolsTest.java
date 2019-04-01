package eu.druglogics.gitsbe.input;

import org.junit.Assert;
import org.junit.Test;

public class AttractorToolsTest {

    @Test
    public void test_contains() {
        Assert.assertFalse(AttractorTools.contains("a_non_existent_tool"));
        Assert.assertFalse(AttractorTools.contains("biolqm_fixedpoint_tool"));

        Assert.assertTrue(AttractorTools.contains("bnet_reduction"));
        Assert.assertTrue(AttractorTools.contains("bnet_reduction_reduced"));

        Assert.assertEquals("bnet_reduction", AttractorTools.BNREDUCTION_FULL.getTool());
        Assert.assertEquals("bnet_reduction_reduced", AttractorTools.BNREDUCTION_REDUCED.getTool());
    }
}
