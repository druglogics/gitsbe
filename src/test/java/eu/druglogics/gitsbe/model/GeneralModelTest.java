package eu.druglogics.gitsbe.model;

import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GeneralModelTest {

    private GeneralModel generalModel;
    private GeneralModel generalModelSelfContained;

    @BeforeEach
    void init() {
        Logger mockLogger = mock(Logger.class);

        ArrayList<SingleInteraction> testInteractions1 = new ArrayList<>();
        testInteractions1.add(new SingleInteraction("A\t->\tB"));
        testInteractions1.add(new SingleInteraction("C\t-|\tB"));
        testInteractions1.add(new SingleInteraction("C\t->\tA"));
        testInteractions1.add(new SingleInteraction("B\t-|\tD"));
        testInteractions1.add(new SingleInteraction("D\t->\tC"));

        generalModelSelfContained = new GeneralModel(testInteractions1, mockLogger);

        // I,J are input nodes, F,K are output nodes
        ArrayList<SingleInteraction> testInteractions2 = new ArrayList<>();
        testInteractions2.add(new SingleInteraction("A\t->\tB"));
        testInteractions2.add(new SingleInteraction("C\t-|\tB"));
        testInteractions2.add(new SingleInteraction("C\t->\tA"));
        testInteractions2.add(new SingleInteraction("B\t-|\tD"));
        testInteractions2.add(new SingleInteraction("D\t->\tC"));
        testInteractions2.add(new SingleInteraction("D\t-|\tW"));
        testInteractions2.add(new SingleInteraction("W\t->\tF"));
        testInteractions2.add(new SingleInteraction("W\t->\tK"));
        testInteractions2.add(new SingleInteraction("I\t->\tW"));
        testInteractions2.add(new SingleInteraction("E\t->\tC"));
        testInteractions2.add(new SingleInteraction("J\t->\tE"));

        generalModel = new GeneralModel(testInteractions2, mockLogger);
    }

    @Test
    void test_build_multiple_interactions() {
        generalModel.buildMultipleInteractions();
        generalModelSelfContained.buildMultipleInteractions();

        assertThat(generalModel.getMultipleInteractions())
                .hasSize(10)
                .extracting("target")
                .contains("B", "A", "D", "C", "W", "F", "K", "E", "I", "J");
        assertThat(generalModelSelfContained.getMultipleInteractions())
                .hasSize(4)
                .extracting("target")
                .contains("B", "A", "D", "C")
                .doesNotContain("W", "F", "K", "E", "I", "J");
    }

    @Test
    void test_remove_inputs() {
        generalModel.removeInputs();
        generalModelSelfContained.removeInputs();

        assertThat(generalModel.getSingleInteractions())
                .hasSize(8)
                .extracting("source", "target", "arc")
                .contains(tuple("A", "B", 1))
                .contains(tuple("C", "B", -1))
                .contains(tuple("C", "A", 1))
                .contains(tuple("B", "D", -1))
                .contains(tuple("D", "C", 1))
                .contains(tuple("D", "W", -1))
                .contains(tuple("W", "F", 1))
                .contains(tuple("W", "K", 1))
                .doesNotContain(tuple("I", "W", 1))
                .doesNotContain(tuple("E", "C", 1))
                .doesNotContain(tuple("J", "E", 1));
        assertThat(generalModelSelfContained.getSingleInteractions())
                .hasSize(5)
                .extracting("source", "target", "arc")
                .contains(tuple("A", "B", 1))
                .contains(tuple("C", "B", -1))
                .contains(tuple("C", "A", 1))
                .contains(tuple("B", "D", -1))
                .contains(tuple("D", "C", 1));
    }

    @Test
    void test_remove_outputs() {
        generalModel.removeOutputs();
        generalModelSelfContained.removeOutputs();

        assertThat(generalModel.getSingleInteractions())
                .hasSize(7)
                .extracting("source", "target", "arc")
                .contains(tuple("A", "B", 1))
                .contains(tuple("C", "B", -1))
                .contains(tuple("C", "A", 1))
                .contains(tuple("B", "D", -1))
                .contains(tuple("D", "C", 1))
                .doesNotContain(tuple("D", "W", -1))
                .doesNotContain(tuple("W", "F", 1))
                .doesNotContain(tuple("W", "K", 1))
                .doesNotContain(tuple("I", "W", 1))
                .contains(tuple("E", "C", 1))
                .contains(tuple("J", "E", 1));
        assertThat(generalModelSelfContained.getSingleInteractions())
                .hasSize(5)
                .extracting("source", "target", "arc")
                .contains(tuple("A", "B", 1))
                .contains(tuple("C", "B", -1))
                .contains(tuple("C", "A", 1))
                .contains(tuple("B", "D", -1))
                .contains(tuple("D", "C", 1));
    }

    @Test
    void test_remove_both_inputs_and_outputs() {
        generalModel.removeInputsOutputs();
        generalModelSelfContained.removeOutputs();

        assertThat(generalModel.getSingleInteractions())
                .hasSize(5)
                .extracting("source", "target", "arc")
                .contains(tuple("A", "B", 1))
                .contains(tuple("C", "B", -1))
                .contains(tuple("C", "A", 1))
                .contains(tuple("B", "D", -1))
                .contains(tuple("D", "C", 1));
        assertThat(generalModelSelfContained.getSingleInteractions())
                .hasSize(5)
                .extracting("source", "target", "arc")
                .contains(tuple("A", "B", 1))
                .contains(tuple("C", "B", -1))
                .contains(tuple("C", "A", 1))
                .contains(tuple("B", "D", -1))
                .contains(tuple("D", "C", 1));
    }

    @Test
    void test_is_not_source() {
        assertFalse(generalModel.isNotASource("A"));
        assertFalse(generalModel.isNotASource("B"));
        assertFalse(generalModel.isNotASource("W"));
        assertFalse(generalModel.isNotASource("I"));
        assertFalse(generalModel.isNotASource("J"));

        // F,K are output nodes
        assertTrue(generalModel.isNotASource("F"));
        assertTrue(generalModel.isNotASource("K"));
    }

    @Test
    void test_is_not_target() {
        assertFalse(generalModel.isNotATarget("A"));
        assertFalse(generalModel.isNotATarget("B"));
        assertFalse(generalModel.isNotATarget("W"));
        assertFalse(generalModel.isNotATarget("F"));
        assertFalse(generalModel.isNotATarget("K"));

        // I,J are input nodes
        assertTrue(generalModel.isNotATarget("I"));
        assertTrue(generalModel.isNotATarget("J"));
    }
}
