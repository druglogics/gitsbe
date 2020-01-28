package eu.druglogics.gitsbe.model;

import eu.druglogics.gitsbe.input.ModelOutputs;
import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class BooleanModelTest {

    private BooleanModel booleanModel;
    private BooleanModel booleanModelSelfContained;
    private BooleanModel booleanModel3; // 3 output nodes

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

        // I,J are input nodes, F,U,K are output nodes
        ArrayList<SingleInteraction> testInteractions3 = new ArrayList<>();
        testInteractions3.add(new SingleInteraction("A\t->\tB"));
        testInteractions3.add(new SingleInteraction("C\t-|\tB"));
        testInteractions3.add(new SingleInteraction("C\t->\tA"));
        testInteractions3.add(new SingleInteraction("B\t-|\tD"));
        testInteractions3.add(new SingleInteraction("D\t->\tC"));
        testInteractions3.add(new SingleInteraction("D\t-|\tW"));
        testInteractions3.add(new SingleInteraction("W\t->\tF"));
        testInteractions3.add(new SingleInteraction("W\t->\tU"));
        testInteractions3.add(new SingleInteraction("W\t->\tK"));
        testInteractions3.add(new SingleInteraction("I\t->\tW"));
        testInteractions3.add(new SingleInteraction("E\t->\tC"));
        testInteractions3.add(new SingleInteraction("J\t->\tE"));

        GeneralModel generalModel3 = new GeneralModel(testInteractions3, mockLogger);
        generalModel3.buildMultipleInteractions();

        this.booleanModel = new BooleanModel(generalModel, "biolqm_stable_states", mockLogger);
        this.booleanModelSelfContained = new BooleanModel(generalModelSelfContained,
            "biolqm_stable_states", mockLogger);
        this.booleanModel3 = new BooleanModel(generalModel3, "biolqm_stable_states", mockLogger);
    }

    @Test
    void test_init_boolean_model_from_gitsbe_file() {
        Logger mockLogger = mock(Logger.class);

        ClassLoader classLoader = getClass().getClassLoader();
        String filename = new File(classLoader.getResource("example.gitsbe").getFile()).getPath();

        BooleanModel booleanModel = new BooleanModel(filename, "biolqm_stable_states", mockLogger);

        assertEquals(booleanModel.getModelName(), "example");
        assertEquals(booleanModel.getAttractorTool(), "biolqm_stable_states");
        assertThat(booleanModel.getBooleanEquations())
            .hasSize(6)
            .extracting("target", "activatingRegulators", "inhibitoryRegulators", "link")
            .contains(tuple("A", newArrayList("B", "C"), newArrayList("D"), "and"))
            .contains(tuple("B", newArrayList(), newArrayList("A"), ""))
            .contains(tuple("C", newArrayList("B"), newArrayList(), ""))
            .contains(tuple("D", newArrayList(), newArrayList("D"), ""))
            .contains(tuple("E", newArrayList("D"), newArrayList("A", "B", "C"), "and"))
            .contains(tuple("F", newArrayList("C"), newArrayList("A"), "or"));

        LinkedHashMap<String, String> map = booleanModel.getNodeNameToVariableMap();

        ArrayList<String> expectedNodes = newArrayList("A", "B", "C", "D", "E", "F");
        ArrayList<String> expectedVars = newArrayList("x1", "x2", "x3", "x4", "x5", "x6");

        assertEquals(6, map.size());
        assertEquals(expectedNodes, new ArrayList<>(map.keySet()));
        assertEquals(expectedVars, new ArrayList<>(map.values()));
    }

    @Test
    void test_init_boolean_model_from_booleannet_file() {
        Logger mockLogger = mock(Logger.class);

        ClassLoader classLoader = getClass().getClassLoader();
        String filename = new File(classLoader.getResource("example.booleannet").getFile()).getPath();

        BooleanModel booleanModel = new BooleanModel(filename, "biolqm_stable_states", mockLogger);

        assertEquals(booleanModel.getModelName(), "example");
        assertEquals(booleanModel.getAttractorTool(), "biolqm_stable_states");
        assertThat(booleanModel.getBooleanEquations())
            .hasSize(6)
            .extracting("target", "activatingRegulators", "inhibitoryRegulators", "link")
            .contains(tuple("A", newArrayList("B", "C"), newArrayList("D"), "and"))
            .contains(tuple("B", newArrayList(), newArrayList("A"), ""))
            .contains(tuple("C", newArrayList("B"), newArrayList(), ""))
            .contains(tuple("D", newArrayList(), newArrayList("D"), ""))
            .contains(tuple("E", newArrayList("D"), newArrayList("A", "B", "C"), "and"))
            .contains(tuple("F", newArrayList("C"), newArrayList("A"), "or"));

        LinkedHashMap<String, String> map = booleanModel.getNodeNameToVariableMap();

        ArrayList<String> expectedNodes = newArrayList("A", "B", "C", "D", "E", "F");
        ArrayList<String> expectedVars = newArrayList("x1", "x2", "x3", "x4", "x5", "x6");

        assertEquals(6, map.size());
        assertEquals(expectedNodes, new ArrayList<>(map.keySet()));
        assertEquals(expectedVars, new ArrayList<>(map.values()));
    }

    @Test
    void test_init_boolean_model_from_bnet_file() {
        Logger mockLogger = mock(Logger.class);

        ClassLoader classLoader = getClass().getClassLoader();
        String filename = new File(classLoader.getResource("example.bnet").getFile()).getPath();

        BooleanModel booleanModel = new BooleanModel(filename, "biolqm_stable_states", mockLogger);

        assertEquals(booleanModel.getModelName(), "example");
        assertEquals(booleanModel.getAttractorTool(), "biolqm_stable_states");

        assertThat(booleanModel.getBooleanEquations())
            .hasSize(6)
            .extracting("target", "activatingRegulators", "inhibitoryRegulators", "link")
            .contains(tuple("A", newArrayList("B", "C"), newArrayList("D"), "and"))
            .contains(tuple("B", newArrayList(), newArrayList("A"), ""))
            .contains(tuple("C", newArrayList("B"), newArrayList(), ""))
            .contains(tuple("D", newArrayList(), newArrayList("D"), ""))
            .contains(tuple("E", newArrayList("D"), newArrayList("A", "B", "C"), "and"))
            .contains(tuple("F", newArrayList("C"), newArrayList("A"), "or"));

        LinkedHashMap<String, String> map = booleanModel.getNodeNameToVariableMap();

        ArrayList<String> expectedNodes = newArrayList("A", "B", "C", "D", "E", "F");
        ArrayList<String> expectedVars = newArrayList("x1", "x2", "x3", "x4", "x5", "x6");

        assertEquals(6, map.size());
        assertEquals(expectedNodes, new ArrayList<>(map.keySet()));
        assertEquals(expectedVars, new ArrayList<>(map.values()));
    }

    @Test
    void test_get_index_of_equation() {
        assertEquals(0, booleanModelSelfContained.getIndexOfEquation("B"));
        assertEquals(0, booleanModelSelfContained.getIndexOfEquation("  B "));
        assertEquals(1, booleanModelSelfContained.getIndexOfEquation("A"));
        assertEquals(2, booleanModelSelfContained.getIndexOfEquation("D"));
        assertEquals(3, booleanModelSelfContained.getIndexOfEquation("C"));

        // `W` and `F` nodes are not in the self-contained boolean model
        assertEquals(-1, booleanModelSelfContained.getIndexOfEquation("W"));
        assertEquals(-1, booleanModelSelfContained.getIndexOfEquation("F"));
    }

    @Test
    void test_get_node_names() {
        assertThat(booleanModel.getNodeNames())
            .hasSize(10)
            .contains("B", "A", "D", "C", "W", "F", "K", "E", "I", "J");
        assertThat(booleanModelSelfContained.getNodeNames())
            .hasSize(4)
            .contains("B", "A", "D", "C");
    }

    @Test
    void test_get_boolean_equations() {
        assertThat(booleanModel.getBooleanEquations())
            .hasSize(10)
            .extracting("target", "activatingRegulators", "inhibitoryRegulators", "link")
            .contains(tuple("B", newArrayList("A"), newArrayList("C"), "and"))
            .contains(tuple("A", newArrayList("C"), newArrayList(), ""))
            .contains(tuple("D", newArrayList(), newArrayList("B"), ""))
            .contains(tuple("C", newArrayList("D", "E"), newArrayList(), ""))
            .contains(tuple("W", newArrayList("I"), newArrayList("D"), "and"))
            .contains(tuple("F", newArrayList("W"), newArrayList(), ""))
            .contains(tuple("K", newArrayList("W"), newArrayList(), ""))
            .contains(tuple("E", newArrayList("J"), newArrayList(), ""))
            .contains(tuple("I", newArrayList("I"), newArrayList(), ""))
            .contains(tuple("J", newArrayList("J"), newArrayList(), ""));
    }

    @Test
    void test_modify_equation_whose_target_exists() throws Exception {
        String equationToSet = "B *= false";
        booleanModel.modifyEquation(equationToSet);

        ArrayList<BooleanEquation> equations = booleanModel.getBooleanEquations();

        BooleanEquation equationOfTargetA = null;
        for (BooleanEquation equation: equations) {
            if (equation.getTarget().equals("B"))
                equationOfTargetA = new BooleanEquation(equation);
        }

        assertEquals(equationOfTargetA.getBooleanEquation(),   " B *=  (  false ) ");
        assertEquals("B", equationOfTargetA.getTarget());
        assertTrue(equationOfTargetA.getLink().isEmpty());
        assertThat(equationOfTargetA.getActivatingRegulators()).hasSize(1).contains("false");
        assertThat(equationOfTargetA.getInhibitoryRegulators()).hasSize(0);
    }

    @Test
    void test_modify_equation_whose_target_does_not_exist() {
        assertThrows(Exception.class, () -> {
            String equationNonExistentTarget = "NonTargetName *= BAD";
            booleanModel.modifyEquation(equationNonExistentTarget);
        });
    }

    @Test
    void test_get_alternative_names() {
        LinkedHashMap<String, String> map = booleanModel.getNodeNameToVariableMap();

        ArrayList<String> expectedNodes =
            newArrayList("B", "A", "D", "C", "W", "F", "K", "E", "I", "J");
        ArrayList<String> expectedVars =
            newArrayList("x1", "x2", "x3", "x4", "x5", "x6", "x7", "x8", "x9", "x10");

        assertEquals(10, map.size());
        assertEquals(expectedNodes, new ArrayList<>(map.keySet()));
        assertEquals(expectedVars, new ArrayList<>(map.values()));
    }

    @Test
    void test_get_model_booleannet() throws Exception {
        assertThat(booleanModel.getModelBooleanNet())
            .hasSize(10)
            .contains("B *= ( A ) and not ( C )",
                "A *= ( C )",
                "D *= not ( B )",
                "C *= ( ( D ) or E )",
                "W *= ( I ) and not ( D )",
                "F *= ( W )",
                "K *= ( W )",
                "E *= ( J )",
                "I *= ( I )",
                "J *= ( J )");

        booleanModel.modifyEquation("A *= true");
        booleanModel.modifyEquation("J *= false");
        assertThat(booleanModel.getModelBooleanNet())
            .hasSize(10)
            .contains("B *= ( A ) and not ( C )",
                "A *= ( true )",
                "D *= not ( B )",
                "C *= ( ( D ) or E )",
                "W *= ( I ) and not ( D )",
                "F *= ( W )",
                "K *= ( W )",
                "E *= ( J )",
                "I *= ( I )",
                "J *= ( false )");
    }

    @Test
    void test_get_model_veliz_cuba() throws Exception {
        assertThat(booleanModel.getModelVelizCuba())
            .hasSize(10)
            .contains("( x2 ) & ! ( x4 )",
                "( x4 )",
                "! ( x1 )",
                "( ( x3 ) | x8 )",
                "( x9 ) & ! ( x3 )",
                "( x5 )",
                "( x5 )",
                "( x10 )",
                "( x9 )",
                "( x10 )");

        booleanModel.modifyEquation("A *= true");
        booleanModel.modifyEquation("J *= false");
        assertThat(booleanModel.getModelVelizCuba())
            .hasSize(10)
            .contains("( x2 ) & ! ( x4 )",
                "( 1 )",
                "! ( x1 )",
                "( ( x3 ) | x8 )",
                "( x9 ) & ! ( x3 )",
                "( x5 )",
                "( x5 )",
                "( x10 )",
                "( x9 )",
                "( 0 )");
    }

    @Test
    void test_get_model_boolnet() throws Exception {
        assertThat(booleanModel.getModelBoolNet())
            .hasSize(10)
            .contains("B, ( A ) & ! ( C )",
                "A, ( C )",
                "D, ! ( B )",
                "C, ( ( D ) | E )",
                "W, ( I ) & ! ( D )",
                "F, ( W )",
                "K, ( W )",
                "E, ( J )",
                "I, ( I )",
                "J, ( J )");

        booleanModel.modifyEquation("A *= true");
        booleanModel.modifyEquation("J *= false");
        assertThat(booleanModel.getModelBoolNet())
            .hasSize(10)
            .contains("B, ( A ) & ! ( C )",
                "A, ( 1 )",
                "D, ! ( B )",
                "C, ( ( D ) | E )",
                "W, ( I ) & ! ( D )",
                "F, ( W )",
                "K, ( W )",
                "E, ( J )",
                "I, ( I )",
                "J, ( 0 )");
    }

    @Test
    void test_calculate_global_output() throws Exception {
        Logger mockLogger = mock(Logger.class);

        ClassLoader classLoader = getClass().getClassLoader();
        String filename = new File(classLoader.getResource("test_modeloutputs").getFile()).getPath();

        ModelOutputs.init(filename, mockLogger);

        // check the minimum and maximum global output values (weights: F:1, U:-1, K: 1)
        assertEquals(-1.0, ModelOutputs.getInstance().getMinOutput());
        assertEquals(2.0, ModelOutputs.getInstance().getMaxOutput());

        // F,U,K indexes are 5,6,7 (zero-indexed)
        // System.out.println(booleanModel3.getNodeNames());

        DecimalFormat df = new DecimalFormat("#.00000");

        // Single Stable State
        booleanModel3.attractors.setAttractors(newArrayList("00000" + "010" + "000")); // U is active
        Double gl0 = Double.valueOf(df.format(booleanModel3.calculateGlobalOutput()));
        assertEquals(gl0, 0.0); // the minimum (normalized) global output: -1

        booleanModel3.attractors.setAttractors(newArrayList("00000" + "000" + "000")); // none is active
        Double gl1 = Double.valueOf(df.format(booleanModel3.calculateGlobalOutput()));
        assertEquals(gl1, 0.33333);

        booleanModel3.attractors.setAttractors(newArrayList("00000" + "100" + "000")); // F is active
        Double gl2 = Double.valueOf(df.format(booleanModel3.calculateGlobalOutput()));
        assertEquals(gl2, 0.66667);

        booleanModel3.attractors.setAttractors(newArrayList("00000" + "001" + "000")); // K is active
        Double gl3 = Double.valueOf(df.format(booleanModel3.calculateGlobalOutput()));
        assertEquals(gl3, 0.66667);

        booleanModel3.attractors.setAttractors(newArrayList("00000" + "011" + "000")); // U and K are active
        Double gl4 = Double.valueOf(df.format(booleanModel3.calculateGlobalOutput()));
        assertEquals(gl4, 0.33333);

        booleanModel3.attractors.setAttractors(newArrayList("00000" + "101" + "000")); // F and K are active
        Double gl5 = Double.valueOf(df.format(booleanModel3.calculateGlobalOutput()));
        assertEquals(gl5, 1.0); // the maximum (normalized) global output: 2

        booleanModel3.attractors.setAttractors(newArrayList("00000" + "111" + "000")); // F, U and K (all) are active
        Double gl6 = Double.valueOf(df.format(booleanModel3.calculateGlobalOutput()));
        assertEquals(gl6, 0.66667);

        // Multiple Stable States
        booleanModel3.attractors.setAttractors(newArrayList("00000" + "111" + "000", "00000" + "101" + "000"));
        assertThat(booleanModel3.calculateGlobalOutput()).isBetween((float) 0, (float) 1);
        assertEquals(Double.valueOf(df.format(booleanModel3.calculateGlobalOutput())), 0.83333);

        booleanModel3.attractors.setAttractors(newArrayList("00000" + "111" + "000",
            "00000" + "101" + "000", "00000" + "100" + "000"));
        assertThat(booleanModel3.calculateGlobalOutput()).isBetween((float) 0, (float) 1);
        assertEquals(Double.valueOf(df.format(booleanModel3.calculateGlobalOutput())), 0.77778);

        // Single Trapspace
        booleanModel3.attractors.setAttractors(newArrayList("00000" + "---" + "000"));
        Double gl7 = Double.valueOf(df.format(booleanModel3.calculateGlobalOutput()));
        assertEquals(gl7, 0.5);

        booleanModel3.attractors.setAttractors(newArrayList("00000" + "-1-" + "000"));
        Double gl8 = Double.valueOf(df.format(booleanModel3.calculateGlobalOutput()));
        assertEquals(gl8, 0.33333);

        booleanModel3.attractors.setAttractors(newArrayList("00-10" + "-11" + "0--"));
        Double gl9 = Double.valueOf(df.format(booleanModel3.calculateGlobalOutput()));
        assertEquals(gl9, 0.5);

        booleanModel3.attractors.setAttractors(newArrayList("00-10" + "0-0" + "0--"));
        Double gl10 = Double.valueOf(df.format(booleanModel3.calculateGlobalOutput()));
        assertEquals(gl10, 0.16667); // second lowest value after '010' for single attractors!

        // Multiple attractors (stable state + trapspace)
        booleanModel3.attractors.setAttractors(newArrayList("00-10" + "0-0" + "0--", "00010" + "010" + "011"));
        Double gl11 = Double.valueOf(df.format(booleanModel3.calculateGlobalOutput()));
        assertEquals(gl11, 0.08333);

        booleanModel3.attractors.setAttractors(newArrayList("00-10" + "0-0" + "0--",
            "00010" + "010" + "011", "0---0" + "010" + "--1"));
        Double gl12 = Double.valueOf(df.format(booleanModel3.calculateGlobalOutput()));
        assertEquals(gl12, 0.05556);
    }

    @Test
    void test_change_model_name() {
        assertNull(booleanModel.getModelName());
        booleanModel.setModelName("random_name");

        assertEquals(booleanModel.getModelName(), "random_name");
    }

    @Test
    void test_change_link_operator() throws Exception {
        assertEquals(booleanModel.getBooleanEquations().get(0).getBooleanEquation(),
            " B *=  (  A ) and not  ( C ) ");

        booleanModel.changeLinkOperator(0);
        assertEquals(booleanModel.getBooleanEquations().get(0).getBooleanEquation(),
            " B *=  (  A ) or not  ( C ) ");

        booleanModel.changeLinkOperator(0);
        assertEquals(booleanModel.getBooleanEquations().get(0).getBooleanEquation(),
            " B *=  (  A ) and not  ( C ) ");

        assertThrows(IndexOutOfBoundsException.class, () -> booleanModel.changeLinkOperator(100));

        Exception exception = assertThrows(Exception.class, () -> booleanModel.changeLinkOperator(1));
        assertEquals(exception.getMessage(), "Link operator of equation:  A *=  (  C ) is neither `and` or `or`");
    }

    @Test
    void test_copy_constructor() throws Exception {
        Logger mockLogger = mock(Logger.class);
        BooleanModel copyModel = new BooleanModel(booleanModel, mockLogger);

        copyModel.changeLinkOperator(0); // B *= ( A ) and not ( C ), 0-indexed
        String equationToSet = "A *= B or not C"; // A *= ( C ), 1-indexed
        copyModel.modifyEquation(equationToSet);
        copyModel.getBooleanEquations().get(1).blacklistActivatingRegulator(0);

        // original model's equations have not changed
        assertEquals(booleanModel.getBooleanEquations().get(0).getLink(), "and");
        assertEquals(booleanModel.getBooleanEquations().get(1).getBooleanEquation(), " A *=  (  C ) ");
        assertEquals(booleanModel.getBooleanEquations().get(1).getNumRegulators(), 1);
        assertEquals(booleanModel.getBooleanEquations().get(1).getLink(), "");
        assertEquals(booleanModel.getBooleanEquations().get(1).getNumWhitelistedRegulators(), 1);
        assertEquals(booleanModel.getBooleanEquations().get(1).getNumWhitelistedActivatingRegulators(), 1);
        assertEquals(booleanModel.getBooleanEquations().get(1).getNumBlacklistedActivatingRegulators(), 0);
        assertEquals(booleanModel.getBooleanEquations().get(1).getNumWhitelistedInhibitoryRegulators(), 0);
        assertEquals(booleanModel.getBooleanEquations().get(1).getNumBlacklistedInhibitoryRegulators(), 0);

        // copy model's equations changed though!
        assertEquals(copyModel.getBooleanEquations().get(0).getLink(), "or");
        assertEquals(copyModel.getBooleanEquations().get(1).getBooleanEquation(), " A *=  not  ( C ) ");
        assertEquals(copyModel.getBooleanEquations().get(1).getNumRegulators(), 2);
        assertEquals(copyModel.getBooleanEquations().get(1).getLink(), "or"); // link it's still there of course!
        assertEquals(copyModel.getBooleanEquations().get(1).getNumWhitelistedRegulators(), 1);
        assertEquals(copyModel.getBooleanEquations().get(1).getNumWhitelistedActivatingRegulators(), 0);
        assertEquals(copyModel.getBooleanEquations().get(1).getNumBlacklistedActivatingRegulators(), 1);
        assertEquals(copyModel.getBooleanEquations().get(1).getNumWhitelistedInhibitoryRegulators(), 1);
        assertEquals(copyModel.getBooleanEquations().get(1).getNumBlacklistedInhibitoryRegulators(), 0);
    }
}
