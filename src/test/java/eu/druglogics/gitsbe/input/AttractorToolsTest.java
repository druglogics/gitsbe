package eu.druglogics.gitsbe.input;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;

class AttractorToolsTest {

    @Test
    void test_contains() {
        assertFalse(AttractorTools.contains("a_non_existent_tool"));
        assertFalse(AttractorTools.contains("biolqm_fixedpoint_tool"));

        assertTrue(AttractorTools.contains("bnet_reduction"));
        assertTrue(AttractorTools.contains("bnet_reduction_reduced"));

        assertEquals("bnet_reduction", AttractorTools.BNREDUCTION_FULL.getTool());
        assertEquals("bnet_reduction_reduced", AttractorTools.BNREDUCTION_REDUCED.getTool());
    }

    @Test
    void test_get_tools() {
        ArrayList<String> expectedTools = newArrayList("bnet_reduction", "bnet_reduction_reduced");
        assertEquals(expectedTools, AttractorTools.getTools());
    }
}
