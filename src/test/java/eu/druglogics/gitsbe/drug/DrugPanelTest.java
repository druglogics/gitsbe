package eu.druglogics.gitsbe.drug;

import eu.druglogics.gitsbe.input.Config;
import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.model.GeneralModel;
import eu.druglogics.gitsbe.model.SingleInteraction;
import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ClassLoaderUtils;

import javax.naming.ConfigurationException;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class DrugPanelTest {

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

	@AfterEach
	void reset_singleton() throws Exception {
		Field instance = DrugPanel.class.getDeclaredField("drugPanel");
		instance.setAccessible(true);
		instance.set(null, null);
	}

	@Test
	void test_get_instance_without_first_calling_init() {
		assertThrows(AssertionError.class, DrugPanel::getInstance);
	}

	@Test
	void test_init_twice() throws Exception {
		Logger mockLogger = mock(Logger.class);

		ClassLoader classLoader = getClass().getClassLoader();
		String filename = new File(classLoader.getResource("test_drugpanel").getFile()).getPath();

		// initializes DrugPanel Class
		DrugPanel.init(filename, mockLogger);

		// initialization cannot happen twice!
		assertThrows(AssertionError.class, () -> DrugPanel.init(filename, mockLogger));
	}

	@Test
	void test_drugs_and_targets_with_correct_input() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		String drugPanelFile = new File(classLoader.getResource("test_drugpanel").getFile()).getPath();
		Logger mockLogger = mock(Logger.class);

		DrugPanel.init(drugPanelFile, mockLogger);
		DrugPanel drugPanel = DrugPanel.getInstance();

		ArrayList<Drug> drugs = drugPanel.getDrugs();

		assertEquals(drugPanel.getDrugPanelSize(), 3);
		assertEquals(drugPanel.getDrugNames(), newArrayList("AA", "BB", "CC"));

		Drug drug1 = drugs.get(0);
		assertEquals(drug1.getName(), "AA");
		assertFalse(drug1.getEffect());
		assertEquals(drug1.getTargets(), newArrayList("A"));

		Drug drug2 = drugs.get(1);
		assertEquals(drug2.getName(), "BB");
		assertFalse(drug2.getEffect());
		assertEquals(drug2.getTargets(), newArrayList("B", "E"));

		Drug drug3 = drugs.get(2);
		assertEquals(drug3.getName(), "CC");
		assertFalse(drug3.getEffect());
		assertEquals(drug3.getTargets(), newArrayList("C", "F", "G"));
	}

	@Test
	void test_logger_error_with_incorrect_effect() {

		ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
			ClassLoader classLoader = getClass().getClassLoader();
			String drugPanelFile = new File(classLoader.getResource("test_drugpanel_wrong_format").getFile()).getPath();

			Logger mockLogger = mock(Logger.class);
			DrugPanel.init(drugPanelFile, mockLogger);
			DrugPanel.getInstance();
		});

		assertEquals(exception.getMessage(), "Drug effect: `perturbs` is neither `activates` or `inhibits`");
	}

	@Test
	void test_check_drug_targets() throws Exception {
		// DrugPanel
		ClassLoader classLoader = getClass().getClassLoader();
		String drugPanelFile = new File(classLoader.getResource("test_drugpanel").getFile()).getPath();
		Logger mockLogger = mock(Logger.class);

		DrugPanel.init(drugPanelFile, mockLogger);
		DrugPanel drugPanel = DrugPanel.getInstance();

		// Boolean Model
		ArrayList<SingleInteraction> testInteractions = new ArrayList<>();
		testInteractions.add(new SingleInteraction("A\t->\tB"));
		testInteractions.add(new SingleInteraction("C\t-|\tB"));
		testInteractions.add(new SingleInteraction("C\t->\tA"));
		testInteractions.add(new SingleInteraction("B\t-|\tD"));
		testInteractions.add(new SingleInteraction("D\t->\tC"));

		GeneralModel generalModel = new GeneralModel(testInteractions, mockLogger);
		generalModel.buildMultipleInteractions();

		BooleanModel booleanModel = new BooleanModel(generalModel,
			Config.getInstance().getAttractorTool(), mockLogger);

		doAnswer(invocation -> {
			Integer verbosity = invocation.getArgument(0);
			String message = invocation.getArgument(1);

			assertEquals(3, verbosity);
			if (message.contains("E"))
				assertEquals("Warning: Target `E` of Drug `BB` is not in the network file/model", message);
			else if (message.contains("F"))
				assertEquals("Warning: Target `F` of Drug `CC` is not in the network file/model", message);
			else if (message.contains("G"))
				assertEquals("Warning: Target `G` of Drug `CC` is not in the network file/model", message);
			return null;
		}).when(mockLogger).outputStringMessage(isA(Integer.class), isA(String.class));

		// `E`,`F` and `G` targets are not in the network model
		drugPanel.checkDrugTargets(booleanModel);
	}

	@Test
	void test_get_drug_targets() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		String drugPanelFile = new File(classLoader.getResource("test_drugpanel").getFile()).getPath();
		Logger mockLogger = mock(Logger.class);

		DrugPanel.init(drugPanelFile, mockLogger);
		DrugPanel drugPanel = DrugPanel.getInstance();

		assertEquals(drugPanel.getDrugTargets("AA"), newArrayList("A"));
		assertEquals(drugPanel.getDrugTargets("BB"), newArrayList("B","E"));
		assertEquals(drugPanel.getDrugTargets("CC"), newArrayList("C","F","G"));

		ConfigurationException exception = assertThrows(ConfigurationException.class,
			() -> drugPanel.getDrugTargets("DD"));

		assertEquals(exception.getMessage(), "There is no drug with name: `DD`");
	}

	@Test
	void test_get_drug_effect() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		String drugPanelFile = new File(classLoader.getResource("test_drugpanel").getFile()).getPath();
		Logger mockLogger = mock(Logger.class);

		DrugPanel.init(drugPanelFile, mockLogger);
		DrugPanel drugPanel = DrugPanel.getInstance();

		assertFalse(drugPanel.getDrugEffect("AA"));
		assertFalse(drugPanel.getDrugEffect("BB"));
		assertFalse(drugPanel.getDrugEffect("CC"));

		ConfigurationException exception = assertThrows(ConfigurationException.class,
			() -> drugPanel.getDrugEffect("DD"));

		assertEquals(exception.getMessage(), "There is no drug with name: `DD`");
	}
}
