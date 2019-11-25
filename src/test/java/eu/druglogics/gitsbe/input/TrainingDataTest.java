package eu.druglogics.gitsbe.input;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.model.GeneralModel;
import eu.druglogics.gitsbe.model.SingleInteraction;
import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TrainingDataTest {

    private BooleanModel booleanModel;
    private String trainingDataFile;

    @BeforeEach
    void init() {
        Logger mockLogger = mock(Logger.class);

        ArrayList<SingleInteraction> testInteractions = new ArrayList<>();
        testInteractions.add(new SingleInteraction("A\t->\tB"));
        testInteractions.add(new SingleInteraction("C\t-|\tB"));
        testInteractions.add(new SingleInteraction("C\t->\tA"));
        testInteractions.add(new SingleInteraction("B\t-|\tD"));
        testInteractions.add(new SingleInteraction("D\t->\tC"));

        GeneralModel generalModel = new GeneralModel(testInteractions, mockLogger);
        generalModel.buildMultipleInteractions();

        this.booleanModel = new BooleanModel(generalModel, mockLogger);

        ClassLoader classLoader = getClass().getClassLoader();
        this.trainingDataFile = new File(classLoader.getResource("test_training").getFile()).getPath();
    }

    @Test
    void test_constructor() throws IOException {
        Logger mockLogger = mock(Logger.class);
        TrainingData trainingData = new TrainingData(trainingDataFile, mockLogger);

        // check size
        assertEquals(trainingData.size(), 5);

        // check weight sum (take into account float arithmetic problems)
        DecimalFormat df = new DecimalFormat("#.0");
        assertEquals(Double.valueOf(df.format(trainingData.getWeightSum())), 2.5);

        // check observations
        ArrayList<TrainingDataObservation> obs = trainingData.getObservations();

        ArrayList<String> expectedCondition1 = new ArrayList<>(Collections.singletonList("-"));
        ArrayList<String> expectedResponse1  = new ArrayList<>(
            Arrays.asList("A:0", "B:0", "C:1", "D:1", "Another:1", "Another2:0")
        );
        assertEquals(obs.get(0).getCondition(), expectedCondition1);
        assertEquals(obs.get(0).getResponse(), expectedResponse1);

        ArrayList<String> expectedCondition2 = new ArrayList<>(Collections.singletonList("-"));
        ArrayList<String> expectedResponse2  = new ArrayList<>(Collections.singletonList("globaloutput:1"));
        assertEquals(obs.get(1).getCondition(), expectedCondition2);
        assertEquals(obs.get(1).getResponse(), expectedResponse2);

        ArrayList<String> expectedCondition3 = new ArrayList<>(Collections.singletonList("B:0"));
        ArrayList<String> expectedResponse3  = new ArrayList<>(Collections.singletonList("globaloutput:0"));
        assertEquals(obs.get(2).getCondition(), expectedCondition3);
        assertEquals(obs.get(2).getResponse(), expectedResponse3);

        ArrayList<String> expectedCondition4 = new ArrayList<>(Collections.singletonList("Drug(AA)"));
        ArrayList<String> expectedResponse4  = new ArrayList<>(Collections.singletonList("globaloutput:0.3"));
        assertEquals(obs.get(3).getCondition(), expectedCondition4);
        assertEquals(obs.get(3).getResponse(), expectedResponse4);

        ArrayList<String> expectedCondition5 = new ArrayList<>(Collections.singletonList("Drug(AA+BB) < min(Drug(AA), Drug(BB))"));
        ArrayList<String> expectedResponse5  = new ArrayList<>(Collections.singletonList("globaloutput:0.1"));
        assertEquals(obs.get(4).getCondition(), expectedCondition5);
        assertEquals(obs.get(4).getResponse(), expectedResponse5);
    }

    @Test
    void test_warning_message_node_in_response_not_in_model() throws IOException {
        Logger mockLogger = mock(Logger.class);

        TrainingData trainingData = new TrainingData(trainingDataFile, mockLogger);

        ArrayList<TrainingDataObservation> obs = trainingData.getObservations();
        ArrayList<String> nodes = booleanModel.getNodeNames();

        doAnswer(invocation -> {
            Integer verbosity = invocation.getArgument(0);
            String message = invocation.getArgument(1);

            assertEquals(3, verbosity);
            if (message.contains("Another:1"))
                assertEquals("Warning: Node Another defined in response Another:1 is not in network file.", message);
            else
                assertEquals("Warning: Node Another2 defined in response Another2:0 is not in network file.", message);
            return null;
        }).when(mockLogger).outputStringMessage(isA(Integer.class), isA(String.class));

        // 'Another' and 'Another2' are not in the nodes
        trainingData.checkResponses(nodes, obs.get(0).getResponse());
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void test_abort_node_in_condition_not_in_model() throws IOException {
        Logger mockLogger = mock(Logger.class);
        TrainingData trainingData = new TrainingData(trainingDataFile, mockLogger);

        ArrayList<TrainingDataObservation> obs = trainingData.getObservations();
        ArrayList<String> nodes_with_no_B = new ArrayList<>(Arrays.asList("A", "C"));

        // B is not in the empty set of nodes
        trainingData.checkConditions(nodes_with_no_B, obs.get(2).getCondition());
    }
}
