package eu.druglogics.gitsbe.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BooleanEquationTest {

    private BooleanEquation booleanEquationWithLink;
    private BooleanEquation booleanEquationNoLink;

    @BeforeEach
    void init() {
        String equationString = "A *= ( ( ( B ) or C ) or D ) and not ( ( ( E ) or F ) or G )";
        String equationStringNoLink = "A *= not ( B )";
        booleanEquationWithLink = new BooleanEquation(equationString);
        booleanEquationNoLink = new BooleanEquation(equationStringNoLink);
    }

    @Test
    void test_init_equation_from_multiple_interaction_with_link() {
        String targetName = "A";
        MultipleInteraction multipleInteraction = new MultipleInteraction(targetName);
        multipleInteraction.addActivatingRegulator("B");
        multipleInteraction.addActivatingRegulator("C");
        multipleInteraction.addInhibitoryRegulator("E");
        multipleInteraction.addInhibitoryRegulator("F");

        BooleanEquation booleanEquation = new BooleanEquation(multipleInteraction);

        assertEquals(4, booleanEquation.getNumRegulators());
        assertEquals(4, booleanEquation.getNumWhitelistedRegulators());
        assertEquals(2, booleanEquation.getNumWhitelistedActivatingRegulators());
        assertEquals(2, booleanEquation.getNumWhitelistedInhibitoryRegulators());
        assertEquals(0, booleanEquation.getNumBlacklistedRegulators());
        assertEquals("A", booleanEquation.getTarget());
        assertEquals("and", booleanEquation.getLink());

        ArrayList<String> activatingRegulatorsExpected = new ArrayList<>();
        activatingRegulatorsExpected.add("B");
        activatingRegulatorsExpected.add("C");
        assertEquals(activatingRegulatorsExpected, booleanEquation.getActivatingRegulators());

        ArrayList<String> inhibitoryRegulatorsExpected = new ArrayList<>();
        inhibitoryRegulatorsExpected.add("E");
        inhibitoryRegulatorsExpected.add("F");
        assertEquals(inhibitoryRegulatorsExpected, booleanEquation.getInhibitoryRegulators());

        String expectedEquationString = " A *=  (  (  B )  or C ) and not  (  ( E )  or F ) ";
        assertEquals(expectedEquationString, booleanEquation.getBooleanEquation());
    }

    @Test
    void test_init_equation_from_multiple_interaction_without_link() {
        String targetName = "A";
        MultipleInteraction multipleInteraction = new MultipleInteraction(targetName);
        multipleInteraction.addInhibitoryRegulator("B");

        BooleanEquation booleanEquation = new BooleanEquation(multipleInteraction);

        assertEquals(1, booleanEquation.getNumRegulators());
        assertEquals(1, booleanEquation.getNumWhitelistedRegulators());
        assertEquals(0, booleanEquation.getNumWhitelistedActivatingRegulators());
        assertEquals(1, booleanEquation.getNumWhitelistedInhibitoryRegulators());
        assertEquals(0, booleanEquation.getNumBlacklistedRegulators());
        assertEquals("A",booleanEquation.getTarget());
        assertTrue(booleanEquation.getLink().isEmpty());
        assertTrue(booleanEquation.getActivatingRegulators().isEmpty());

        ArrayList<String> inhibitoryRegulatorsExpected = new ArrayList<>();
        inhibitoryRegulatorsExpected.add("B");
        assertEquals(inhibitoryRegulatorsExpected, booleanEquation.getInhibitoryRegulators());

        String expectedEquationString = " A *=  not  ( B ) ";
        assertEquals(expectedEquationString, booleanEquation.getBooleanEquation());
    }

    @Test
    void test_init_equation_from_string_with_link() {
        assertEquals(6, booleanEquationWithLink.getNumRegulators());
        assertEquals(6, booleanEquationWithLink.getNumWhitelistedRegulators());
        assertEquals(0, booleanEquationWithLink.getNumBlacklistedRegulators());
        assertEquals("A", booleanEquationWithLink.getTarget());
        assertEquals("and", booleanEquationWithLink.getLink());

        ArrayList<String> activatingRegulatorsExpected = new ArrayList<>();
        activatingRegulatorsExpected.add("B");
        activatingRegulatorsExpected.add("C");
        activatingRegulatorsExpected.add("D");
        assertEquals(activatingRegulatorsExpected, booleanEquationWithLink.getActivatingRegulators());

        ArrayList<String> inhibitoryRegulatorsExpected = new ArrayList<>();
        inhibitoryRegulatorsExpected.add("E");
        inhibitoryRegulatorsExpected.add("F");
        inhibitoryRegulatorsExpected.add("G");
        assertEquals(inhibitoryRegulatorsExpected, booleanEquationWithLink.getInhibitoryRegulators());

        String expectedEquationString = " A *=  (  (  (  B )  or C )  or D ) " +
                "and not  (  (  ( E )  or F )  or G ) ";
        assertEquals(expectedEquationString, booleanEquationWithLink.getBooleanEquation());
    }

    @Test
    void test_init_equation_from_string_without_link() {
        assertEquals(1, booleanEquationNoLink.getNumRegulators());
        assertEquals(1, booleanEquationNoLink.getNumWhitelistedRegulators());
        assertEquals(0, booleanEquationNoLink.getNumBlacklistedRegulators());
        assertEquals("A", booleanEquationNoLink.getTarget());
        assertTrue(booleanEquationNoLink.getLink().isEmpty());
        assertTrue(booleanEquationNoLink.getActivatingRegulators().isEmpty());

        ArrayList<String> inhibitoryRegulatorsExpected = new ArrayList<>();
        inhibitoryRegulatorsExpected.add("B");
        assertEquals(inhibitoryRegulatorsExpected, booleanEquationNoLink.getInhibitoryRegulators());

        String expectedEquationString = " A *=  not  ( B ) ";
        assertEquals(expectedEquationString, booleanEquationNoLink.getBooleanEquation());
    }

    @Test
    void test_get_equation_in_VC_format() {
        String expectedEquationString1 = " A *=  (  (  (  B )  | C )  | D ) " +
                "& !  (  (  ( E )  | F )  | G ) ";
        assertEquals(expectedEquationString1, booleanEquationWithLink.getBooleanEquationVC());

        String expectedEquationString2 = " A *=  !  ( B ) ";
        assertEquals(expectedEquationString2, booleanEquationNoLink.getBooleanEquationVC());
    }

    @Test
    void test_convert_to_sif_lines() {
        ArrayList<String> sifLines1 = booleanEquationWithLink.convertToSifLines(" ");
        ArrayList<String> expectedSifLines1 = new ArrayList<>(
                Arrays.asList("B -> A", "C -> A", "D -> A", "E -| A", "F -| A", "G -| A")
        );
        assertEquals(expectedSifLines1, sifLines1);

        ArrayList<String> sifLines2 = booleanEquationNoLink.convertToSifLines(" ");
        ArrayList<String> expectedSifLines2 = new ArrayList<>();
        expectedSifLines2.add("B -| A");
        assertEquals(expectedSifLines2, sifLines2);

        ArrayList<String> sifLinesTabSeperated = booleanEquationWithLink.convertToSifLines("\t");
        ArrayList<String> expectedSifLinesTabSeperated = new ArrayList<>(
                Arrays.asList("B\t->\tA", "C\t->\tA", "D\t->\tA", "E\t-|\tA", "F\t-|\tA", "G\t-|\tA")
        );
        assertEquals(sifLinesTabSeperated, expectedSifLinesTabSeperated);
    }

    @Test
    void test_get_single_interactions() {
        ArrayList<SingleInteraction> singleInteractions1 = booleanEquationWithLink.getSingleInteractions();

        assertThat(singleInteractions1)
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

        ArrayList<SingleInteraction> singleInteractions2 = booleanEquationNoLink.getSingleInteractions();

        assertThat(singleInteractions2)
                .hasSize(1)
                .extracting("source", "target", "arc")
                .contains(tuple("B", "A", -1));
    }
}
