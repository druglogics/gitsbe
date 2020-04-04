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
        assertTrue(AttractorTools.contains("biolqm_stable_states"));
        assertTrue(AttractorTools.contains("biolqm_trapspaces"));
        assertTrue(AttractorTools.contains("mpbn_trapspaces"));

        assertEquals("bnet_reduction", AttractorTools.BNREDUCTION_FULL.getTool());
        assertEquals("bnet_reduction_reduced", AttractorTools.BNREDUCTION_REDUCED.getTool());
        assertEquals("biolqm_stable_states", AttractorTools.BIOLQM_STABLE_STATES.getTool());
        assertEquals("biolqm_trapspaces", AttractorTools.BIOLQM_TRAPSPACES.getTool());
        assertEquals("mpbn_trapspaces", AttractorTools.MPBN_TRAPSPACES.getTool());
    }

    @Test
    void test_get_tools() {
        ArrayList<String> expectedTools = newArrayList("bnet_reduction", "bnet_reduction_reduced",
            "biolqm_stable_states", "biolqm_trapspaces", "mpbn_trapspaces");
        assertEquals(expectedTools, AttractorTools.getTools());
    }
}
