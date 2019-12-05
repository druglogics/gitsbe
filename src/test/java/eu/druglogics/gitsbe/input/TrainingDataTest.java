package eu.druglogics.gitsbe.input;

import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.model.GeneralModel;
import eu.druglogics.gitsbe.model.SingleInteraction;
import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
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
    void test_constructor() throws IOException, ConfigurationException {
        Logger mockLogger = mock(Logger.class);
        TrainingData trainingData = new TrainingData(trainingDataFile, mockLogger);

        // check size
        assertEquals(trainingData.size(), 6);

        // check weight sum (take into account float arithmetic problems)
        DecimalFormat df = new DecimalFormat("#.0");
        assertEquals(Double.valueOf(df.format(trainingData.getWeightSum())), 2.7);

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

        ArrayList<String> expectedCondition5 = new ArrayList<>(Collections.singletonList("Drug(AA+BB) < min(Drug(AA),Drug(BB))"));
        ArrayList<String> expectedResponse5  = new ArrayList<>(Collections.singletonList("globaloutput:0.1"));
        assertEquals(obs.get(4).getCondition(), expectedCondition5);
        assertEquals(obs.get(4).getResponse(), expectedResponse5);

        ArrayList<String> expectedCondition6 = new ArrayList<>(Collections.singletonList("Drug(CC+BB) < product(Drug(CC),Drug(BB))"));
        ArrayList<String> expectedResponse6  = new ArrayList<>(Collections.singletonList("globaloutput:-0.2"));
        assertEquals(obs.get(5).getCondition(), expectedCondition6);
        assertEquals(obs.get(5).getResponse(), expectedResponse6);
    }

    @Test
    void test_no_exception_on_correctly_formatted_training_file() throws IOException, ConfigurationException {
        Logger mockLogger = mock(Logger.class);
        TrainingData trainingData = new TrainingData(trainingDataFile, mockLogger);
        assertDoesNotThrow(() -> trainingData.checkTrainingDataConsistency(booleanModel));
    }

    @Test
    void test_warning_message_node_in_response_not_in_model() throws IOException, ConfigurationException {
        Logger mockLogger = mock(Logger.class);

        TrainingData trainingData = new TrainingData(trainingDataFile, mockLogger);

        ArrayList<TrainingDataObservation> obs = trainingData.getObservations();
        ArrayList<String> nodes = booleanModel.getNodeNames();

        doAnswer(invocation -> {
            Integer verbosity = invocation.getArgument(0);
            String message = invocation.getArgument(1);

            assertEquals(3, verbosity);
            if (message.contains("Another:1"))
                assertEquals("Warning: Node `Another` defined in response `Another:1` is not in network file.", message);
            else
                assertEquals("Warning: Node `Another2` defined in response `Another2:0` is not in network file.", message);
            return null;
        }).when(mockLogger).outputStringMessage(isA(Integer.class), isA(String.class));

        // 'Another' and 'Another2' are not in the nodes
        trainingData.checkResponses(nodes, obs.get(0).getResponse());
    }

    @Test
    void test_check_conditions() {
        ConfigurationException exception1 = assertThrows(ConfigurationException.class, () -> {
            Logger mockLogger = mock(Logger.class);
            TrainingData trainingData = new TrainingData(trainingDataFile, mockLogger);

            ArrayList<TrainingDataObservation> obs = trainingData.getObservations();
            ArrayList<String> nodes_with_no_B = new ArrayList<>(Arrays.asList("A", "C"));

            // B is not in the set of nodes: {A,C}
            trainingData.checkConditions(nodes_with_no_B, obs.get(2).getCondition());
        });

        assertEquals(exception1.getMessage(), "Node `B` defined in condition `B:0` is not in network file.");

        ConfigurationException exception2 = assertThrows(ConfigurationException.class, () -> {
            ClassLoader classLoader = getClass().getClassLoader();
            Logger mockLogger = mock(Logger.class);
            String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_4").getFile()).getPath();

            TrainingData trainingData = new TrainingData(trainingWrongFormat, mockLogger);

            ArrayList<TrainingDataObservation> obs = trainingData.getObservations();

            trainingData.checkConditions(new ArrayList<>(), obs.get(0).getCondition());
        });

        assertEquals(exception2.getMessage(), "Only one condition defined: `xaxa` that has neither `-`, `:` or starts with `Drug`");

		ConfigurationException exception3 = assertThrows(ConfigurationException.class, () -> {
			ClassLoader classLoader = getClass().getClassLoader();
			Logger mockLogger = mock(Logger.class);
			String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_5").getFile()).getPath();

			TrainingData trainingData = new TrainingData(trainingWrongFormat, mockLogger);

			ArrayList<TrainingDataObservation> obs = trainingData.getObservations();

			trainingData.checkConditions(new ArrayList<>(), obs.get(0).getCondition());
		});

		assertEquals(exception3.getMessage(), "Only one condition defined: ` drug ` that has neither `-`, `:` or starts with `Drug`");
	}

    @Test
    void test_globaloutput_out_of_range() {
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            ClassLoader classLoader = getClass().getClassLoader();
            String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format").getFile()).getPath();
            Logger mockLogger = mock(Logger.class);
            new TrainingData(trainingWrongFormat, mockLogger);
        });

        assertEquals(exception.getMessage(), "Response has `globaloutput` outside the [-1,1] range: 10.0");

        ConfigurationException exception2 = assertThrows(ConfigurationException.class, () -> {
            ClassLoader classLoader = getClass().getClassLoader();
            String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_2").getFile()).getPath();
            Logger mockLogger = mock(Logger.class);
            new TrainingData(trainingWrongFormat, mockLogger);
        });

        assertEquals(exception2.getMessage(), "Response has `globaloutput` outside the [-1,1] range: -1.3");
    }

    @Test
    void test_response_value_out_of_range() {
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            ClassLoader classLoader = getClass().getClassLoader();
            String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_3").getFile()).getPath();
            Logger mockLogger = mock(Logger.class);
            new TrainingData(trainingWrongFormat, mockLogger);
        });

        assertEquals(exception.getMessage(), "Node `C` has value outside the [0,1] range: 1.1");
    }

    @Test
    void test_response_has_non_numeric_value() {
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            ClassLoader classLoader = getClass().getClassLoader();
            String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_8").getFile()).getPath();
            Logger mockLogger = mock(Logger.class);
            new TrainingData(trainingWrongFormat, mockLogger);
        });

        assertEquals(exception.getMessage(), "Node `A` has a non-numeric value: noNumber");

        ConfigurationException exception2 = assertThrows(ConfigurationException.class, () -> {
            ClassLoader classLoader = getClass().getClassLoader();
            String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_9").getFile()).getPath();
            Logger mockLogger = mock(Logger.class);
            new TrainingData(trainingWrongFormat, mockLogger);
        });

        assertEquals(exception2.getMessage(), "Response: `globaloutput:noNumber` has a non-numeric value: noNumber");
    }

    @Test
    void test_response_with_no_semicolon() {
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            ClassLoader classLoader = getClass().getClassLoader();
            String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_6").getFile()).getPath();
            Logger mockLogger = mock(Logger.class);
            new TrainingData(trainingWrongFormat, mockLogger);
        });

        assertEquals(exception.getMessage(), "Response: `globaloutput-0.2` does not contain `:`");

        ConfigurationException exception2 = assertThrows(ConfigurationException.class, () -> {
            ClassLoader classLoader = getClass().getClassLoader();
            String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_7").getFile()).getPath();
            Logger mockLogger = mock(Logger.class);
            new TrainingData(trainingWrongFormat, mockLogger);
        });

        assertEquals(exception2.getMessage(), "Response: `C0` does not contain `:`");
    }

    @Test
    void check_conditions_with_specified_drugs() throws IOException, ConfigurationException {
        Logger mockLogger = mock(Logger.class);
        ClassLoader classLoader = getClass().getClassLoader();
        String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_10").getFile()).getPath();
        TrainingData trainingData = new TrainingData(trainingWrongFormat, mockLogger);

        ConfigurationException exception1 = assertThrows(ConfigurationException.class,
            () -> trainingData.checkConditions(new ArrayList<>(), trainingData.getObservations().get(0).getCondition()));
        assertEquals(exception1.getMessage(), "Wrong format: `Drug(AA)somethingThatShouldntBeHere`");

        ConfigurationException exception2 = assertThrows(ConfigurationException.class,
            () -> trainingData.checkConditions(new ArrayList<>(), trainingData.getObservations().get(2).getCondition()));
        assertEquals(exception2.getMessage(), "Neither 1 nor 3 `Drug` keywords in condition: `Drug(AA+BB+CC) < min(Drug(AA),Drug(BB),Drug(CC))`");

        ConfigurationException exception3 = assertThrows(ConfigurationException.class,
            () -> trainingData.checkConditions(new ArrayList<>(), trainingData.getObservations().get(3).getCondition()));
        assertEquals(exception3.getMessage(), "Condition: `Drug(AA+BB) < max(Drug(AA),Drug(BB))` has neither of the strings: `< min(Drug` or `< product(Drug`");

        ConfigurationException exception4 = assertThrows(ConfigurationException.class,
            () -> trainingData.checkConditions(new ArrayList<>(), trainingData.getObservations().get(4).getCondition()));
        assertEquals(exception4.getMessage(), "Wrong format: `Drug(AA-BB) < min(Drug(AA),Drug(BB))`");

        ConfigurationException exception5 = assertThrows(ConfigurationException.class,
            () -> trainingData.checkConditions(new ArrayList<>(), trainingData.getObservations().get(5).getCondition()));
        assertEquals(exception5.getMessage(), "Wrong format: `Drug(AA+BB) < product(Drug(AA)Drug(BB))`");

        ConfigurationException exception6 = assertThrows(ConfigurationException.class,
            () -> trainingData.checkConditions(new ArrayList<>(), trainingData.getObservations().get(6).getCondition()));
        assertEquals(exception6.getMessage(), "In condition: `Drug(AA+BB) < min(Drug(FF),Drug(BB))` drug names don't match");

        ConfigurationException exception7 = assertThrows(ConfigurationException.class,
            () -> trainingData.checkConditions(new ArrayList<>(), trainingData.getObservations().get(7).getCondition()));
        assertEquals(exception7.getMessage(), "In condition: `Drug(AA+BB) < product(Drug(AA),Drug(FF))` drug names don't match");
    }
}
