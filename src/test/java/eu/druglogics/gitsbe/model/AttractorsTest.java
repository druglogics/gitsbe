package eu.druglogics.gitsbe.model;

import eu.druglogics.gitsbe.input.Config;
import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.commons.util.ClassLoaderUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AttractorsTest {

	private BooleanModel booleanModel;
	private BooleanModel booleanModelSelfContained;
	private BooleanModel booleanModelFromExampleFile;
	private String boolNetFileCellCycle;
	private String boolNetFileYeast;
	private String boolNetMAPK;
	private String boolNetCASCADE1;

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

		ArrayList<SingleInteraction> testInteractions1 = new ArrayList<>();
		testInteractions1.add(new SingleInteraction("A\t->\tB"));
		testInteractions1.add(new SingleInteraction("C\t-|\tB"));
		testInteractions1.add(new SingleInteraction("C\t->\tA"));
		testInteractions1.add(new SingleInteraction("B\t-|\tD"));
		testInteractions1.add(new SingleInteraction("D\t->\tC"));

		GeneralModel generalModelSelfContained = new GeneralModel(testInteractions1, mockLogger);
		generalModelSelfContained.setModelName("self_contained_model");
		generalModelSelfContained.buildMultipleInteractions();

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

		GeneralModel generalModel = new GeneralModel(testInteractions2, mockLogger);
		generalModel.buildMultipleInteractions();
		generalModel.setModelName("test_model");

		this.booleanModel = new BooleanModel(generalModel, Config.getInstance().getAttractorTool(), mockLogger);
		this.booleanModelSelfContained = new BooleanModel(generalModelSelfContained,
			Config.getInstance().getAttractorTool(), mockLogger);

		ClassLoader classLoader = getClass().getClassLoader();
		String filename = new File(classLoader.getResource("example.bnet").getFile()).getPath();
		this.booleanModelFromExampleFile = new BooleanModel(filename, Config.getInstance().getAttractorTool(), mockLogger);

		this.boolNetFileCellCycle = new File(classLoader.getResource("cell_cycle.bnet").getFile()).getPath();
		this.boolNetFileYeast = new File(classLoader.getResource("davidich_yeast.bnet").getFile()).getPath();
		this.boolNetMAPK = new File(classLoader.getResource("grieco_mapk.bnet").getFile()).getPath();
		this.boolNetCASCADE1 = new File(classLoader.getResource("cascade.bnet").getFile()).getPath();
	}

	@TempDir
	File tempDir;

	@Test
	void test_fixpoints_biolqm() throws Exception {
		Logger mockLogger = mock(Logger.class);
		String attractorTool = "biolqm_stable_states";

		Attractors attractors = new Attractors(booleanModel, attractorTool, mockLogger);
		attractors.calculateAttractors(tempDir.getAbsolutePath());
		assertEquals(attractors.getAttractors(),
			newArrayList("0111000000", "0111000010", "0111000101", "0111000111"));
		assertTrue(attractors.hasAttractors());

		Attractors attractors1 = new Attractors(booleanModelSelfContained, attractorTool, mockLogger);
		attractors1.calculateAttractors(tempDir.getAbsolutePath());
		assertEquals(attractors1.getAttractors(), newArrayList("0111"));
		assertTrue(attractors1.hasAttractors());

		// no stable states
		Attractors attractors2 = new Attractors(booleanModelFromExampleFile, attractorTool, mockLogger);
		attractors2.calculateAttractors(tempDir.getAbsolutePath());
		assertFalse(attractors2.hasAttractors());

		// Cell cycle logical model has 1 stable state
		Attractors attractors3 = new Attractors(new BooleanModel(mockLogger), attractorTool, mockLogger);
		attractors3.calculateAttractorsFromBoolNetFile(boolNetFileCellCycle);
		assertEquals(attractors3.getAttractors(), newArrayList("0100010100"));
		assertTrue(attractors3.hasAttractors());

		// Yeast model
		Attractors attractors4 = new Attractors(new BooleanModel(mockLogger), attractorTool, mockLogger);
		attractors4.calculateAttractorsFromBoolNetFile(boolNetFileYeast);
		assertEquals(attractors4.getAttractors().size(), 12);
		assertTrue(attractors4.hasAttractors());
		assertTrue(attractors4.hasStableStates());
		assertEquals(attractors4.getAttractors(), attractors4.getStableStates());

		// MAPK Network on Cancer Cell Fate Decision
		Attractors attractors5 = new Attractors(new BooleanModel(mockLogger), attractorTool, mockLogger);
		attractors5.calculateAttractorsFromBoolNetFile(boolNetMAPK);
		assertEquals(attractors5.getAttractors().size(), 12);
		assertEquals(attractors5.getAttractors(), attractors5.getStableStates());

		// Cascade 1.0 (link operator mutated)
		Attractors attractors6 = new Attractors(new BooleanModel(mockLogger), attractorTool, mockLogger);
		attractors6.calculateAttractorsFromBoolNetFile(boolNetCASCADE1);
		assertEquals(attractors6.getStableStates(), newArrayList("00011110111100011100101000010001101011111101000010011010000111010000111000111"));
		assertTrue(attractors6.hasStableStates());
	}

	@Test
	void test_traspaces_biolqm() throws Exception {
		Logger mockLogger = mock(Logger.class);
		String attractorTool = "biolqm_trapspaces";

		Attractors attractors = new Attractors(booleanModel, attractorTool, mockLogger);
		attractors.calculateAttractors(tempDir.getAbsolutePath());
		assertThat(attractors.getAttractors())
			.containsExactlyInAnyOrder("0111000000", "0111000010", "0111000101", "0111000111");
		assertTrue(attractors.hasAttractors());
		assertEquals(attractors.getStableStates().size(), 4);

		// check that `getAttractorsWithNodes()` has as the first row
		// the node names in the same sequence as in the boolean model
		ArrayList<String> nodes = new ArrayList<>(Arrays.asList(attractors.getAttractorsWithNodes()[0]));
		assertEquals(nodes, booleanModel.getNodeNames());

		Attractors attractors1 = new Attractors(booleanModelSelfContained, attractorTool, mockLogger);
		attractors1.calculateAttractors(tempDir.getAbsolutePath());
		assertEquals(attractors1.getAttractors(), newArrayList("0111"));
		assertTrue(attractors1.hasAttractors());

		// model has only the full state trapspace (trivial - it's all dashes)
		// which is excluded (so in the end you get no attractors)
		Attractors attractors2 = new Attractors(booleanModelFromExampleFile, attractorTool, mockLogger);
		attractors2.calculateAttractors(tempDir.getAbsolutePath());
		assertFalse(attractors2.hasAttractors());
		assertEquals(attractors2.getStableStates().size(), 0);

		// Cell Cycle model
		Attractors attractors3 = new Attractors(new BooleanModel(mockLogger), attractorTool, mockLogger);
		attractors3.calculateAttractorsFromBoolNetFile(boolNetFileCellCycle);
		assertThat(attractors3.getAttractors()).containsExactlyInAnyOrder("0100010100", "10---0----");
		assertTrue(attractors3.hasAttractors());
		assertThat(attractors3.getStableStates()).containsOnly("0100010100");

		// Yeast model
		Attractors attractors4 = new Attractors(new BooleanModel(mockLogger), attractorTool, mockLogger);
		attractors4.calculateAttractorsFromBoolNetFile(boolNetFileYeast);
		assertEquals(attractors4.getAttractors().size(), 12);
		assertTrue(attractors4.hasAttractors());
		assertEquals(attractors4.getAttractors(), attractors4.getStableStates());

		// MAPK Network on Cancer Cell Fate Decision
		Attractors attractors5 = new Attractors(new BooleanModel(mockLogger), attractorTool, mockLogger);
		attractors5.calculateAttractorsFromBoolNetFile(boolNetMAPK);
		assertEquals(attractors5.getAttractors().size(), 18);
		assertEquals(attractors5.getStableStates().size(), 12);

		// Cascade 1.0 (link operator mutated)
		Attractors attractors6 = new Attractors(new BooleanModel(mockLogger), attractorTool, mockLogger);
		attractors6.calculateAttractorsFromBoolNetFile(boolNetCASCADE1);
		assertEquals(attractors6.getAttractors(), newArrayList("00011110111100011100101000010001101011111101000010011010000111010000111000111"));
		assertTrue(attractors6.hasAttractors());
	}
}
