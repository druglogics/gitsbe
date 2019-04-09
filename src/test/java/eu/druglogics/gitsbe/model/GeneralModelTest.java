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
    private ArrayList<SingleInteraction> testInteractions = new ArrayList<>();

    @BeforeEach
    void init() {
        Logger mockLogger = mock(Logger.class);
        this.testInteractions.add(new SingleInteraction("A -> B"));
        this.testInteractions.add(new SingleInteraction("C -| B"));
        this.testInteractions.add(new SingleInteraction("C -> A"));
        this.testInteractions.add(new SingleInteraction("B -| D"));
        this.testInteractions.add(new SingleInteraction("D -> C"));
        this.testInteractions.add(new SingleInteraction("D -| W"));
        this.testInteractions.add(new SingleInteraction("W -> F"));
        this.testInteractions.add(new SingleInteraction("E -> C"));
        this.testInteractions.add(new SingleInteraction("J -> E"));

        this.generalModel = new GeneralModel(testInteractions, mockLogger);
    }

    @Test
    void test_remove_inputs() {
        generalModel.removeInputs();
        ArrayList<SingleInteraction> singleInteractions = generalModel.getSingleInteractions();

        assertThat(singleInteractions)
                .hasSize(7)
                .extracting("source", "target", "arc")
                .contains(tuple("A", "B", 1))
                .contains(tuple("C", "B", -1))
                .contains(tuple("C", "A", 1))
                .contains(tuple("B", "D", -1))
                .contains(tuple("D", "C", 1))
                .contains(tuple("D", "W", -1))
                .contains(tuple("W", "F", 1))
                .doesNotContain(tuple("E", "C", 1))
                .doesNotContain(tuple("J", "E", 1));
    }

    @Test
    void test_remove_outputs() {
        generalModel.removeOutputs();
        ArrayList<SingleInteraction> singleInteractions = generalModel.getSingleInteractions();

        assertThat(singleInteractions)
                .hasSize(7)
                .extracting("source", "target", "arc")
                .contains(tuple("A", "B", 1))
                .contains(tuple("C", "B", -1))
                .contains(tuple("C", "A", 1))
                .contains(tuple("B", "D", -1))
                .contains(tuple("D", "C", 1))
                .doesNotContain(tuple("D", "W", -1))
                .doesNotContain(tuple("W", "F", 1))
                .contains(tuple("E", "C", 1))
                .contains(tuple("J", "E", 1));
    }

    @Test
    void test_remove_both_inputs_and_outputs() {
        generalModel.removeInputsOutputs();
        ArrayList<SingleInteraction> singleInteractions = generalModel.getSingleInteractions();

        assertThat(singleInteractions)
                .hasSize(5)
                .extracting("source", "target", "arc")
                .contains(tuple("A", "B", 1))
                .contains(tuple("C", "B", -1))
                .contains(tuple("C", "A", 1))
                .contains(tuple("B", "D", -1))
                .contains(tuple("D", "C", 1))
                .doesNotContain(tuple("D", "W", -1))
                .doesNotContain(tuple("W", "F", 1))
                .doesNotContain(tuple("E", "C", 1))
                .doesNotContain(tuple("J", "E", 1));
    }

    @Test
    void test_is_not_source() {
        assertFalse(generalModel.isNotASource("A"));
        assertFalse(generalModel.isNotASource("B"));
        assertFalse(generalModel.isNotASource("W"));
        assertFalse(generalModel.isNotASource("J"));
        // F is an output node
        assertTrue(generalModel.isNotASource("F"));
    }

    @Test
    void test_is_not_target() {
        assertFalse(generalModel.isNotATarget("A"));
        assertFalse(generalModel.isNotATarget("B"));
        assertFalse(generalModel.isNotATarget("W"));
        assertFalse(generalModel.isNotATarget("F"));
        // J is an input node
        assertTrue(generalModel.isNotATarget("J"));
    }
}
