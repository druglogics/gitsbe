package eu.druglogics.gitsbe.input;

import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.model.GeneralModel;
import eu.druglogics.gitsbe.model.SingleInteraction;
import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ModelOutputsTest {

    private BooleanModel booleanModel;

    @BeforeEach
    void init() {
        Logger mockLogger = mock(Logger.class);

        // I,J are input nodes, F,U,K are output nodes
        ArrayList<SingleInteraction> testInteractions = new ArrayList<>();
        testInteractions.add(new SingleInteraction("A\t->\tB"));
        testInteractions.add(new SingleInteraction("C\t-|\tB"));
        testInteractions.add(new SingleInteraction("C\t->\tA"));
        testInteractions.add(new SingleInteraction("B\t-|\tD"));
        testInteractions.add(new SingleInteraction("D\t->\tC"));
        testInteractions.add(new SingleInteraction("D\t-|\tW"));
        testInteractions.add(new SingleInteraction("W\t->\tF"));
        testInteractions.add(new SingleInteraction("W\t->\tU"));
        testInteractions.add(new SingleInteraction("W\t->\tK"));
        testInteractions.add(new SingleInteraction("I\t->\tW"));
        testInteractions.add(new SingleInteraction("E\t->\tC"));
        testInteractions.add(new SingleInteraction("J\t->\tE"));

        GeneralModel generalModel = new GeneralModel(testInteractions, mockLogger);
        generalModel.buildMultipleInteractions();

        this.booleanModel = new BooleanModel(generalModel, mockLogger);
    }

    @Test
    void test_calculate_global_output() throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        String filename = new File(classLoader.getResource("test_modeloutputs").getFile()).getPath();

        Logger mockLogger = mock(Logger.class);
        ModelOutputs modeloutputs = new ModelOutputs(filename, mockLogger);

        // check the node names from the test file and how many they are
        ArrayList<String> expectedNodeNames = new ArrayList<>(
                Arrays.asList("F", "U", "K")
        );
        assertEquals(expectedNodeNames, modeloutputs.getNodeNames());
        assertEquals(3, modeloutputs.size());

        // check the string array output for logging
        String[] expectedModelOutputsArray = {
                "F with weight: 1", "U with weight: -1", "K with weight: 1"
        };
        assertArrayEquals(expectedModelOutputsArray, modeloutputs.getModelOutputs());

        // check that the F,U,K nodes are indeed in the network defined above
        modeloutputs.checkModelOutputNodeNames(booleanModel);

        // check the minimum and maximum global output values
        assertEquals(-1.0, modeloutputs.getMinOutput());
        assertEquals(2.0, modeloutputs.getMaxOutput());

        // F,U,K indexes are 5,6,7 (zero-indexed)
        // System.out.println(booleanModel.getNodeNames());

        // check that the value of global output is between 0 and 1
        ArrayList<String> stableStates = new ArrayList<>();
        stableStates.add("00000" + "000" + "000"); // none is active
        assertThat(modeloutputs.calculateGlobalOutput(stableStates, booleanModel))
                .isBetween((float) 0, (float) 1);

        stableStates.clear();
        stableStates.add("00000" + "100" + "000"); // F is active
        assertThat(modeloutputs.calculateGlobalOutput(stableStates, booleanModel))
                .isBetween((float) 0, (float) 1);

        stableStates.clear();
        stableStates.add("00000" + "010" + "000"); // U is active
        assertThat(modeloutputs.calculateGlobalOutput(stableStates, booleanModel))
                .isEqualTo((float) 0); // the minimum (normalized) global output: -1

        stableStates.clear();
        stableStates.add("00000" + "011" + "000"); // U and K are active
        assertThat(modeloutputs.calculateGlobalOutput(stableStates, booleanModel))
                .isBetween((float) 0, (float) 1);

        stableStates.clear();
        stableStates.add("00000" + "101" + "000"); // F and K are active
        assertThat(modeloutputs.calculateGlobalOutput(stableStates, booleanModel))
                .isEqualTo((float) 1); // the maximum (normalized) global output: 2

        stableStates.clear();
        stableStates.add("00000" + "111" + "000"); // F, U and K (all) are active
        assertThat(modeloutputs.calculateGlobalOutput(stableStates, booleanModel))
                .isBetween((float) 0, (float) 1);

        // check that the value of global output is between
        // 0 and 1 in the case of multiple stable states
        stableStates.add("00000" + "101" + "000");
        assertThat(modeloutputs.calculateGlobalOutput(stableStates, booleanModel))
                .isBetween((float) 0, (float) 1);

        stableStates.add("00000" + "100" + "000");
        assertThat(modeloutputs.calculateGlobalOutput(stableStates, booleanModel))
                .isBetween((float) 0, (float) 1);
    }
}
