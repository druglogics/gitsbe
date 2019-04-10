package eu.druglogics.gitsbe.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BooleanEquationTest {

    private BooleanEquation booleanEquation;

    @BeforeEach
    void init() {
        String equationString = "A *= ( ( ( B ) or C ) or D ) and not ( ( ( E ) or F ) or G )";
        booleanEquation = new BooleanEquation(equationString);
    }

    @Test
    void test_init_equation_from_multiple_interaction() {
        String targetName = "A";
        MultipleInteraction multipleInteraction = new MultipleInteraction(targetName);
        multipleInteraction.addActivatingRegulator("B");
        multipleInteraction.addActivatingRegulator("C");
        multipleInteraction.addInhibitoryRegulator("E");
        multipleInteraction.addInhibitoryRegulator("F");

        BooleanEquation booleanEquation1 = new BooleanEquation(multipleInteraction);

        assertEquals(4, booleanEquation1.getNumRegulators());
        assertEquals(4, booleanEquation1.getNumWhitelistedRegulators());
        assertEquals(0, booleanEquation1.getNumBlacklistedRegulators());

        ArrayList<String> activatingRegulatorsExpected = new ArrayList<>();
        activatingRegulatorsExpected.add("B");
        activatingRegulatorsExpected.add("C");
        assertEquals(activatingRegulatorsExpected, booleanEquation1.getActivatingRegulators());

        ArrayList<String> inhibitoryRegulatorsExpected = new ArrayList<>();
        inhibitoryRegulatorsExpected.add("E");
        inhibitoryRegulatorsExpected.add("F");
        assertEquals(inhibitoryRegulatorsExpected, booleanEquation1.getInhibitoryRegulators());

        String expectedEquationString = " A *=  (  (  B )  or C ) and not  (  ( E )  or F ) ";
        assertEquals(expectedEquationString, booleanEquation1.getBooleanEquation());
    }

    @Test
    void test_init_equation_from_string() {
        assertEquals(6, booleanEquation.getNumRegulators());
        assertEquals(6, booleanEquation.getNumWhitelistedRegulators());
        assertEquals(0, booleanEquation.getNumBlacklistedRegulators());

        ArrayList<String> activatingRegulatorsExpected = new ArrayList<>();
        activatingRegulatorsExpected.add("B");
        activatingRegulatorsExpected.add("C");
        activatingRegulatorsExpected.add("D");
        assertEquals(activatingRegulatorsExpected, booleanEquation.getActivatingRegulators());

        ArrayList<String> inhibitoryRegulatorsExpected = new ArrayList<>();
        inhibitoryRegulatorsExpected.add("E");
        inhibitoryRegulatorsExpected.add("F");
        inhibitoryRegulatorsExpected.add("G");
        assertEquals(inhibitoryRegulatorsExpected, booleanEquation.getInhibitoryRegulators());

        String expectedEquationString = " A *=  (  (  (  B )  or C )  or D ) " +
                "and not  (  (  ( E )  or F )  or G ) ";
        assertEquals(expectedEquationString, booleanEquation.getBooleanEquation());
    }

    @Test
    void test_get_equation_in_VC_format() {
        String expectedEquationString = " A *=  (  (  (  B )  | C )  | D ) " +
                "& !  (  (  ( E )  | F )  | G ) ";
        assertEquals(expectedEquationString, booleanEquation.getBooleanEquationVC());
    }

    @Test
    void test_convert_to_sif_lines() {
        ArrayList<String> sifLines = booleanEquation.convertToSifLines(" ");
        ArrayList<String> expectedSifLines = new ArrayList<>(
                Arrays.asList("B -> A", "C -> A", "D -> A", "E -| A", "F -| A", "G -| A")
        );
        assertArrayEquals(expectedSifLines.toArray(), sifLines.toArray());

        ArrayList<String> sifLinesTabSeperated = booleanEquation.convertToSifLines("\t");
        ArrayList<String> expectedSifLinesTabSeperated = new ArrayList<>(
                Arrays.asList("B\t->\tA", "C\t->\tA", "D\t->\tA", "E\t-|\tA", "F\t-|\tA", "G\t-|\tA")
        );
        assertArrayEquals(sifLinesTabSeperated.toArray(), expectedSifLinesTabSeperated.toArray());
    }

    @Test
    void test_get_single_interactions() {
        ArrayList<SingleInteraction> singleInteractions = booleanEquation.getSingleInteractions();

        assertThat(singleInteractions)
                .hasSize(6)
                .extracting("source", "target", "arc")
                .contains(tuple("B", "A", 1))
                .contains(tuple("C", "A", 1))
                .contains(tuple("D", "A", 1))
                .contains(tuple("E", "A", -1))
                .contains(tuple("F", "A", -1))
                .contains(tuple("G", "A", -1))
                .doesNotContain(tuple("A", "B", 1))
                .doesNotContain(tuple("A", "E", -1));
    }
}
