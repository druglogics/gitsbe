package eu.druglogics.gitsbe.input;

import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.model.GeneralModel;
import eu.druglogics.gitsbe.model.SingleInteraction;
import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.util.ClassLoaderUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ModelOutputsTest {

    private BooleanModel booleanModel;

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

        this.booleanModel = new BooleanModel(generalModel, Config.getInstance().getAttractorTool(), mockLogger);
    }

    @AfterEach
    void reset_singleton() throws Exception {
        Field instance = ModelOutputs.class.getDeclaredField("modeloutputs");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    void test_get_instance_without_first_calling_init() {
        assertThrows(AssertionError.class, ModelOutputs::getInstance);
    }

    @Test
    void test_init_twice() throws Exception {
        Logger mockLogger = mock(Logger.class);

        ClassLoader classLoader = getClass().getClassLoader();
        String filename = new File(classLoader.getResource("test_modeloutputs").getFile()).getPath();

        // initializes ModelOutputs class
        ModelOutputs.init(filename, mockLogger);

        // initialization cannot happen twice!
        assertThrows(AssertionError.class, () -> ModelOutputs.init(filename, mockLogger));
    }

    @Test
    void test_reset() throws Exception {
        Logger mockLogger = mock(Logger.class);

        ClassLoader classLoader = getClass().getClassLoader();
        String filename = new File(classLoader.getResource("test_modeloutputs").getFile()).getPath();

        // initializes ModelOutputs class
        ModelOutputs.init(filename, mockLogger);

        // After resetting, there is no instance and I can re-initialize!
        ModelOutputs.reset();
        assertThrows(AssertionError.class, ModelOutputs::getInstance);
        ModelOutputs.init(filename, mockLogger);
    }

    @Test
    void test_model_output_data_checks() throws Exception {
        Logger mockLogger = mock(Logger.class);

        ClassLoader classLoader = getClass().getClassLoader();
        String filename = new File(classLoader.getResource("test_modeloutputs").getFile()).getPath();

        ModelOutputs.init(filename, mockLogger);

        ModelOutputs modeloutputs = ModelOutputs.getInstance();

        // check the node names from the test file and how many they are
        ArrayList<String> expectedNodeNames = newArrayList("F", "U", "K");
        assertEquals(expectedNodeNames, modeloutputs.getNodeNames());
        assertEquals(3, modeloutputs.size());

        // check the string array output for logging
        String[] expectedModelOutputsArray = {
                "F with weight: 1", "U with weight: -1", "K with weight: 1"
        };
        assertArrayEquals(expectedModelOutputsArray, modeloutputs.getModelOutputsVerbose());

        // check that the F,U,K nodes are indeed in the network defined above
        modeloutputs.checkModelOutputNodeNames(booleanModel);

        // check the minimum and maximum global output values
        assertEquals(-1.0, modeloutputs.calculateMinOutput());
        assertEquals(2.0, modeloutputs.calculateMaxOutput());
    }
}
