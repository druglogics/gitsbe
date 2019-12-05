package eu.druglogics.gitsbe.drug;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DrugTest {
	private Drug testDrug;

	@BeforeEach
	void init() {
		this.testDrug = new Drug("PD");
	}

	@Test
	void test_get_name() {
		assertEquals(testDrug.getName(), "PD");
	}

	@Test
	void test_add_effect() {
		testDrug.addEffect(false);
		assertFalse(testDrug.getEffect());

		testDrug.addEffect(true);
		assertTrue(testDrug.getEffect());
	}

	@Test
	void test_add_empty_targets() {
		ArrayList<String> testTargets = new ArrayList<>();
		testDrug.addTargets(testTargets);

		assertTrue(testDrug.getTargets().isEmpty());
	}

	@Test
	void test_add_non_empty_targets() {
		ArrayList<String> testTargets = new ArrayList<>();
		testTargets.add("MAP2K1");
		testTargets.add("MAP2K2");

		testDrug.addTargets(testTargets);

		ArrayList<String> targets = testDrug.getTargets();

		assertFalse(targets.isEmpty());
		assertEquals(targets.size(), 2);
		assertEquals(targets.get(0), "MAP2K1");
		assertEquals(targets.get(1), "MAP2K2");
	}

}
