package eu.druglogics.gitsbe.drug;

import eu.druglogics.gitsbe.model.BooleanModel;
import eu.druglogics.gitsbe.model.GeneralModel;
import eu.druglogics.gitsbe.model.SingleInteraction;
import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.Test;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class DrugPanelTest {

	@Test
	void test_drugs_and_targets_with_correct_input() throws IOException, ConfigurationException {
		ClassLoader classLoader = getClass().getClassLoader();
		String drugPanelFile = new File(classLoader.getResource("test_drugpanel").getFile()).getPath();
		Logger mockLogger = mock(Logger.class);

		DrugPanel drugPanel = new DrugPanel(drugPanelFile, mockLogger);

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
			new DrugPanel(drugPanelFile, mockLogger);
		});

		assertEquals(exception.getMessage(), "Drug effect: `perturbs` is neither `activates` or `inhibits`");
	}

	@Test
	void test_check_drug_targets() throws IOException, ConfigurationException {
		// DrugPanel
		ClassLoader classLoader = getClass().getClassLoader();
		String drugPanelFile = new File(classLoader.getResource("test_drugpanel").getFile()).getPath();
		Logger mockLogger = mock(Logger.class);

		DrugPanel drugPanel = new DrugPanel(drugPanelFile, mockLogger);

		// Boolean Model
		ArrayList<SingleInteraction> testInteractions = new ArrayList<>();
		testInteractions.add(new SingleInteraction("A\t->\tB"));
		testInteractions.add(new SingleInteraction("C\t-|\tB"));
		testInteractions.add(new SingleInteraction("C\t->\tA"));
		testInteractions.add(new SingleInteraction("B\t-|\tD"));
		testInteractions.add(new SingleInteraction("D\t->\tC"));

		GeneralModel generalModel = new GeneralModel(testInteractions, mockLogger);
		generalModel.buildMultipleInteractions();

		BooleanModel booleanModel = new BooleanModel(generalModel, mockLogger);

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
}
