package eu.druglogics.gitsbe.model;

import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BooleanModelTest {

    private BooleanModel booleanModel;
    private BooleanModel booleanModelSelfContained;

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

        this.booleanModel = new BooleanModel(generalModel, mockLogger);
        this.booleanModelSelfContained = new BooleanModel(generalModelSelfContained, mockLogger);
    }

    @Test
    void test_init_boolean_model_from_gitsbe_file() {
        Logger mockLogger = mock(Logger.class);

        ClassLoader classLoader = getClass().getClassLoader();
        String filename = new File(classLoader.getResource("example.gitsbe").getFile()).getPath();

        BooleanModel booleanModel = new BooleanModel(filename, mockLogger);

        assertEquals("example", booleanModel.getModelName());
        assertThat(booleanModel.getBooleanEquations())
                .hasSize(6)
                .extracting("target", "activatingRegulators", "inhibitoryRegulators", "link")
                .contains(tuple("A", newArrayList("B", "C"), newArrayList("D"), "and"))
                .contains(tuple("B", newArrayList(), newArrayList("A"), ""))
                .contains(tuple("C", newArrayList("B"), newArrayList(), ""))
                .contains(tuple("D", newArrayList(), newArrayList("D"), ""))
                .contains(tuple("E", newArrayList("D"), newArrayList("A", "B", "C"), "and"))
                .contains(tuple("F", newArrayList("C"), newArrayList("A"), "or"));

        ArrayList<String[]> altNames = booleanModel.getMapAlternativeNames();

        assertEquals(6, altNames.size());
        assertEquals("A", altNames.get(0)[0]);
        assertEquals("x1", altNames.get(0)[1]);
        assertEquals("B", altNames.get(1)[0]);
        assertEquals("x2", altNames.get(1)[1]);
        assertEquals("C", altNames.get(2)[0]);
        assertEquals("x3", altNames.get(2)[1]);
        assertEquals("D", altNames.get(3)[0]);
        assertEquals("x4", altNames.get(3)[1]);
        assertEquals("E", altNames.get(4)[0]);
        assertEquals("x5", altNames.get(4)[1]);
        assertEquals("F", altNames.get(5)[0]);
        assertEquals("x6", altNames.get(5)[1]);
    }

    @Test
    void test_init_boolean_model_from_booleannet_file() {
        Logger mockLogger = mock(Logger.class);

        ClassLoader classLoader = getClass().getClassLoader();
        String filename = new File(classLoader.getResource("example.booleannet").getFile()).getPath();

        BooleanModel booleanModel = new BooleanModel(filename, mockLogger);

        assertEquals("example", booleanModel.getModelName());
        assertThat(booleanModel.getBooleanEquations())
                .hasSize(6)
                .extracting("target", "activatingRegulators", "inhibitoryRegulators", "link")
                .contains(tuple("A", newArrayList("B", "C"), newArrayList("D"), "and"))
                .contains(tuple("B", newArrayList(), newArrayList("A"), ""))
                .contains(tuple("C", newArrayList("B"), newArrayList(), ""))
                .contains(tuple("D", newArrayList(), newArrayList("D"), ""))
                .contains(tuple("E", newArrayList("D"), newArrayList("A", "B", "C"), "and"))
                .contains(tuple("F", newArrayList("C"), newArrayList("A"), "or"));

        ArrayList<String[]> altNames = booleanModel.getMapAlternativeNames();

        assertEquals(6, altNames.size());
        assertEquals("A", altNames.get(0)[0]);
        assertEquals("x1", altNames.get(0)[1]);
        assertEquals("B", altNames.get(1)[0]);
        assertEquals("x2", altNames.get(1)[1]);
        assertEquals("C", altNames.get(2)[0]);
        assertEquals("x3", altNames.get(2)[1]);
        assertEquals("D", altNames.get(3)[0]);
        assertEquals("x4", altNames.get(3)[1]);
        assertEquals("E", altNames.get(4)[0]);
        assertEquals("x5", altNames.get(4)[1]);
        assertEquals("F", altNames.get(5)[0]);
        assertEquals("x6", altNames.get(5)[1]);
    }

    @Test
    void test_get_index_of_equation() {
        assertEquals(0, booleanModelSelfContained.getIndexOfEquation("B"));
        assertEquals(1, booleanModelSelfContained.getIndexOfEquation("A"));
        assertEquals(2, booleanModelSelfContained.getIndexOfEquation("D"));
        assertEquals(3, booleanModelSelfContained.getIndexOfEquation("C"));
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
        ArrayList<String[]> altNames = booleanModel.getMapAlternativeNames();

        assertEquals(10, altNames.size());
        assertEquals("B", altNames.get(0)[0]);
        assertEquals("x1", altNames.get(0)[1]);
        assertEquals("A", altNames.get(1)[0]);
        assertEquals("x2", altNames.get(1)[1]);
        assertEquals("D", altNames.get(2)[0]);
        assertEquals("x3", altNames.get(2)[1]);
        assertEquals("J", altNames.get(9)[0]);
        assertEquals("x10", altNames.get(9)[1]);
    }

    @Test
    void test_get_model_booleannet() throws Exception {
        assertThat(booleanModel.getModelBooleannet())
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
        assertThat(booleanModel.getModelBooleannet())
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

    //TODO
    // exportModelToBoolNetFile -> put it in the code
    // mapAlternativeNames refactoring
}