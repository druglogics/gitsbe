package eu.druglogics.gitsbe.input;

import eu.druglogics.gitsbe.drug.DrugPanel;
import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.model.GeneralModel;
import eu.druglogics.gitsbe.model.SingleInteraction;
import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.util.ClassLoaderUtils;

import javax.naming.ConfigurationException;
import java.io.File;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingDataTest {

    private BooleanModel booleanModel;
    private TrainingData trainingData;

    @BeforeAll
    static void init_config() throws Exception {
        Logger mockLogger = mock(Logger.class);

        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
        String filename = new File(classLoader.getResource("test_config").getFile()).getPath();

        Config.init(filename, mockLogger);
    }

    @AfterAll
    static void reset_config() throws IllegalAccessException, NoSuchFieldException {
        Field instance = Config.class.getDeclaredField("config");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @BeforeEach
    void init() throws Exception {
        Logger mockLogger = mock(Logger.class);

        ArrayList<SingleInteraction> testInteractions = new ArrayList<>();
        testInteractions.add(new SingleInteraction("A\t->\tB"));
        testInteractions.add(new SingleInteraction("C\t-|\tB"));
        testInteractions.add(new SingleInteraction("C\t->\tA"));
        testInteractions.add(new SingleInteraction("B\t-|\tD"));
        testInteractions.add(new SingleInteraction("D\t->\tC"));

        GeneralModel generalModel = new GeneralModel(testInteractions, mockLogger);
        generalModel.buildMultipleInteractions();

        this.booleanModel = new BooleanModel(generalModel, Config.getInstance().getAttractorTool(), mockLogger);

        ClassLoader classLoader = getClass().getClassLoader();
        String trainingDataFile = new File(classLoader.getResource("test_training").getFile()).getPath();
        TrainingData.init(trainingDataFile, mockLogger);
        this.trainingData = TrainingData.getInstance();

        String drugPanelFile = new File(classLoader.getResource("test_drugpanel_2").getFile()).getPath();
        DrugPanel.init(drugPanelFile, mockLogger);
    }

    @AfterEach
    void reset_drugpanel_singleton() throws Exception {
        Field instance = DrugPanel.class.getDeclaredField("drugPanel");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @AfterEach
    void reset_training_data_singleton() throws Exception {
        Field instance = TrainingData.class.getDeclaredField("trainingData");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    void test_get_instance_without_first_calling_init() throws Exception {
        reset_training_data_singleton();
        assertThrows(AssertionError.class, TrainingData::getInstance);
    }

    @Test
    void test_constructor() {
        // check size
        assertEquals(trainingData.size(), 6);

        // check weight sum (take into account float arithmetic problems)
        DecimalFormat df = new DecimalFormat("#.0");
        assertEquals(Double.valueOf(df.format(trainingData.getWeightSum())), 2.7);

        // check observations
        ArrayList<TrainingDataObservation> obs = trainingData.getObservations();

        ArrayList<String> expectedCondition1 = newArrayList("-");
        ArrayList<String> expectedResponse1  = newArrayList("A:0", "B:0", "C:1", "D:1", "Another:1", "Another2:0");

        assertEquals(obs.get(0).getCondition(), expectedCondition1);
        assertEquals(obs.get(0).getResponse(), expectedResponse1);

        ArrayList<String> expectedCondition2 = newArrayList("-");
        ArrayList<String> expectedResponse2  = newArrayList("globaloutput:1");
        assertEquals(obs.get(1).getCondition(), expectedCondition2);
        assertEquals(obs.get(1).getResponse(), expectedResponse2);

        ArrayList<String> expectedCondition3 = newArrayList("B:0");
        ArrayList<String> expectedResponse3  = newArrayList("globaloutput:0");
        assertEquals(obs.get(2).getCondition(), expectedCondition3);
        assertEquals(obs.get(2).getResponse(), expectedResponse3);

        ArrayList<String> expectedCondition4 = newArrayList("Drug(AA)");
        ArrayList<String> expectedResponse4  = newArrayList("globaloutput:0.3");
        assertEquals(obs.get(3).getCondition(), expectedCondition4);
        assertEquals(obs.get(3).getResponse(), expectedResponse4);

        ArrayList<String> expectedCondition5 = newArrayList("Drug(AA+BB) < min(Drug(AA),Drug(BB))");
        ArrayList<String> expectedResponse5  = newArrayList("globaloutput:0.1");
        assertEquals(obs.get(4).getCondition(), expectedCondition5);
        assertEquals(obs.get(4).getResponse(), expectedResponse5);

        ArrayList<String> expectedCondition6 = newArrayList("Drug(CC+BB) < product(Drug(CC),Drug(BB))");
        ArrayList<String> expectedResponse6  = newArrayList("globaloutput:-0.2");
        assertEquals(obs.get(5).getCondition(), expectedCondition6);
        assertEquals(obs.get(5).getResponse(), expectedResponse6);
    }

    @Test
    void test_no_exception_on_correctly_formatted_training_file() {
        assertDoesNotThrow(() -> trainingData.checkTrainingDataConsistency(booleanModel));
    }

    @Test
    void test_warning_message_node_in_response_not_in_model() throws ConfigurationException {
        Logger mockLogger = mock(Logger.class);

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
            ArrayList<TrainingDataObservation> obs = trainingData.getObservations();
            ArrayList<String> nodes_with_no_B = newArrayList("A", "C");

            // B is not in the set of nodes: {A,C}
            trainingData.checkConditions(nodes_with_no_B, obs.get(2).getCondition());
        });

        assertEquals(exception1.getMessage(), "Node `B` defined in condition `B:0` is not in network file.");

        ConfigurationException exception2 = assertThrows(ConfigurationException.class, () -> {
            reset_training_data_singleton();

            ClassLoader classLoader = getClass().getClassLoader();
            Logger mockLogger = mock(Logger.class);
            String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_4").getFile()).getPath();

            //reset_training_data_singleton();
            TrainingData.init(trainingWrongFormat, mockLogger);

            ArrayList<TrainingDataObservation> obs = TrainingData.getInstance().getObservations();

            TrainingData.getInstance().checkConditions(new ArrayList<>(), obs.get(0).getCondition());
        });

        assertEquals(exception2.getMessage(), "Only one condition defined: `xaxa` that has neither `-`, `:` or starts with `Drug`");

		ConfigurationException exception3 = assertThrows(ConfigurationException.class, () -> {
		    reset_training_data_singleton();

			ClassLoader classLoader = getClass().getClassLoader();
			Logger mockLogger = mock(Logger.class);
			String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_5").getFile()).getPath();

            TrainingData.init(trainingWrongFormat, mockLogger);

			ArrayList<TrainingDataObservation> obs = TrainingData.getInstance().getObservations();

            TrainingData.getInstance().checkConditions(new ArrayList<>(), obs.get(0).getCondition());
		});

		assertEquals(exception3.getMessage(), "Only one condition defined: ` drug ` that has neither `-`, `:` or starts with `Drug`");
	}

    @Test
    void test_globaloutput_out_of_range() {
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            reset_training_data_singleton();

            ClassLoader classLoader = getClass().getClassLoader();
            String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format").getFile()).getPath();
            Logger mockLogger = mock(Logger.class);
            TrainingData.init(trainingWrongFormat, mockLogger);
        });

        assertEquals(exception.getMessage(), "Response has `globaloutput` outside the [-1,1] range: 10.0");

        ConfigurationException exception2 = assertThrows(ConfigurationException.class, () -> {
            reset_training_data_singleton();

            ClassLoader classLoader = getClass().getClassLoader();
            String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_2").getFile()).getPath();
            Logger mockLogger = mock(Logger.class);
            TrainingData.init(trainingWrongFormat, mockLogger);
        });

        assertEquals(exception2.getMessage(), "Response has `globaloutput` outside the [-1,1] range: -1.3");
    }

    @Test
    void test_response_value_out_of_range() {
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            reset_training_data_singleton();

            ClassLoader classLoader = getClass().getClassLoader();
            String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_3").getFile()).getPath();
            Logger mockLogger = mock(Logger.class);
            TrainingData.init(trainingWrongFormat, mockLogger);
        });

        assertEquals(exception.getMessage(), "Node `C` has value outside the [0,1] range: 1.1");
    }

    @Test
    void test_response_has_non_numeric_value() {
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            reset_training_data_singleton();

            ClassLoader classLoader = getClass().getClassLoader();
            String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_8").getFile()).getPath();
            Logger mockLogger = mock(Logger.class);
            TrainingData.init(trainingWrongFormat, mockLogger);
        });

        assertEquals(exception.getMessage(), "Node `A` has a non-numeric value: noNumber");

        ConfigurationException exception2 = assertThrows(ConfigurationException.class, () -> {
            reset_training_data_singleton();

            ClassLoader classLoader = getClass().getClassLoader();
            String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_9").getFile()).getPath();
            Logger mockLogger = mock(Logger.class);
            TrainingData.init(trainingWrongFormat, mockLogger);
        });

        assertEquals(exception2.getMessage(), "Response: `globaloutput:noNumber` has a non-numeric value: noNumber");
    }

    @Test
    void test_response_with_no_semicolon() {
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            reset_training_data_singleton();
            ClassLoader classLoader = getClass().getClassLoader();
            String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_6").getFile()).getPath();
            Logger mockLogger = mock(Logger.class);
            TrainingData.init(trainingWrongFormat, mockLogger);
        });

        assertEquals(exception.getMessage(), "Response: `globaloutput-0.2` does not contain `:`");

        ConfigurationException exception2 = assertThrows(ConfigurationException.class, () -> {
            reset_training_data_singleton();
            ClassLoader classLoader = getClass().getClassLoader();
            String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_7").getFile()).getPath();
            Logger mockLogger = mock(Logger.class);
            TrainingData.init(trainingWrongFormat, mockLogger);
        });

        assertEquals(exception2.getMessage(), "Response: `C0` does not contain `:`");
    }

    @Test
    void check_conditions_with_specified_drugs() throws Exception {
        reset_training_data_singleton();

        Logger mockLogger = mock(Logger.class);
        ClassLoader classLoader = getClass().getClassLoader();
        String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_10").getFile()).getPath();
        TrainingData.init(trainingWrongFormat, mockLogger);

        TrainingData trainingDataWrongFormat = TrainingData.getInstance();

        ConfigurationException exception1 = assertThrows(ConfigurationException.class,
            () -> trainingDataWrongFormat.checkConditions(new ArrayList<>(),
                trainingDataWrongFormat.getObservations().get(0).getCondition()));
        assertEquals(exception1.getMessage(), "Wrong format: `Drug(AA)somethingThatShouldntBeHere`");

        ConfigurationException exception2 = assertThrows(ConfigurationException.class,
            () -> trainingDataWrongFormat.checkConditions(new ArrayList<>(),
                trainingDataWrongFormat.getObservations().get(1).getCondition()));
        assertEquals(exception2.getMessage(), "Drugpanel does not include drug: `PK`");

        ConfigurationException exception3 = assertThrows(ConfigurationException.class,
            () -> trainingDataWrongFormat.checkConditions(new ArrayList<>(),
                trainingDataWrongFormat.getObservations().get(2).getCondition()));
        assertEquals(exception3.getMessage(), "Neither 1 nor 3 `Drug` keywords in condition: `Drug(AA+BB+CC) < min(Drug(AA),Drug(BB),Drug(CC))`");

        ConfigurationException exception4 = assertThrows(ConfigurationException.class,
            () -> trainingDataWrongFormat.checkConditions(new ArrayList<>(),
                trainingDataWrongFormat.getObservations().get(3).getCondition()));
        assertEquals(exception4.getMessage(), "Condition: `Drug(AA+BB) < max(Drug(AA),Drug(BB))` has neither of the strings: `< min(Drug` or `< product(Drug`");

        ConfigurationException exception5 = assertThrows(ConfigurationException.class,
            () -> trainingDataWrongFormat.checkConditions(new ArrayList<>(),
                trainingDataWrongFormat.getObservations().get(4).getCondition()));
        assertEquals(exception5.getMessage(), "Wrong format: `Drug(AA-BB) < min(Drug(AA),Drug(BB))`");

        ConfigurationException exception6 = assertThrows(ConfigurationException.class,
            () -> trainingDataWrongFormat.checkConditions(new ArrayList<>(),
                trainingDataWrongFormat.getObservations().get(5).getCondition()));
        assertEquals(exception6.getMessage(), "Wrong format: `Drug(AA+BB) < product(Drug(AA)Drug(BB))`");

        ConfigurationException exception7 = assertThrows(ConfigurationException.class,
            () -> trainingDataWrongFormat.checkConditions(new ArrayList<>(),
                trainingDataWrongFormat.getObservations().get(6).getCondition()));
        assertEquals(exception7.getMessage(), "In condition: `Drug(AA+BB) < min(Drug(FF),Drug(BB))` drug names don't match");

        ConfigurationException exception8 = assertThrows(ConfigurationException.class,
            () -> trainingDataWrongFormat.checkConditions(new ArrayList<>(),
                trainingDataWrongFormat.getObservations().get(7).getCondition()));
        assertEquals(exception8.getMessage(), "In condition: `Drug(AA+BB) < product(Drug(AA),Drug(FF))` drug names don't match");

        ConfigurationException exception9 = assertThrows(ConfigurationException.class,
            () -> trainingDataWrongFormat.checkConditions(new ArrayList<>(),
                trainingDataWrongFormat.getObservations().get(8).getCondition()));
        assertEquals(exception9.getMessage(), "Drug `PK` is not in the drugpanel");

        ConfigurationException exception10 = assertThrows(ConfigurationException.class,
            () -> trainingDataWrongFormat.checkConditions(new ArrayList<>(),
                trainingDataWrongFormat.getObservations().get(9).getCondition()));
        assertEquals(exception10.getMessage(), "Drug `PI` is not in the drugpanel");

        ConfigurationException exception11 = assertThrows(ConfigurationException.class,
            () -> trainingDataWrongFormat.checkConditions(new ArrayList<>(),
                trainingDataWrongFormat.getObservations().get(10).getCondition()));
        assertEquals(exception11.getMessage(), "Drug `PK` is not in the drugpanel");
    }

    @Test
    void check_condition_with_specified_drugs_and_null_drugpanel() throws Exception {
        reset_training_data_singleton();

        Logger mockLogger = mock(Logger.class);
        ClassLoader classLoader = getClass().getClassLoader();
        String trainingWrongFormat = new File(classLoader.getResource("training_wrong_format_10").getFile()).getPath();

        // Un-initialize DrugPanel Class (drugpanel will become null thus)
        this.reset_drugpanel_singleton();

        TrainingData.init(trainingWrongFormat, mockLogger);
        TrainingData trainingDataWrongFormat = TrainingData.getInstance();

        ConfigurationException exception1 = assertThrows(ConfigurationException.class,
            () -> trainingDataWrongFormat.checkConditions(new ArrayList<>(),
                trainingDataWrongFormat.getObservations().get(1).getCondition()));
        assertEquals(exception1.getMessage(), "Drugpanel is null so no targets can be found for drug `PK` in condition: `Drug(PK)`");

        ConfigurationException exception2 = assertThrows(ConfigurationException.class,
            () -> trainingDataWrongFormat.checkConditions(new ArrayList<>(),
                trainingDataWrongFormat.getObservations().get(8).getCondition()));
        assertEquals(exception2.getMessage(), "Drugpanel is null so no targets can be found for the drugs in condition: `Drug(AA+PK) < min(Drug(AA),Drug(PK))`");

        ConfigurationException exception3 = assertThrows(ConfigurationException.class,
            () -> trainingDataWrongFormat.checkConditions(new ArrayList<>(),
                trainingDataWrongFormat.getObservations().get(9).getCondition()));
        assertEquals(exception3.getMessage(), "Drugpanel is null so no targets can be found for the drugs in condition: `Drug(PI+PK) < min(Drug(PI),Drug(PK))`");

        ConfigurationException exception4 = assertThrows(ConfigurationException.class,
                () -> trainingDataWrongFormat.checkConditions(new ArrayList<>(),
                    trainingDataWrongFormat.getObservations().get(10).getCondition()));
        assertEquals(exception4.getMessage(), "Drugpanel is null so no targets can be found for the drugs in condition: `Drug(AA+PK) < product(Drug(AA),Drug(PK))`");
    }
}
