package eu.druglogics.gitsbe.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileDeleterTest {

	@TempDir
	File tempDir;

	@Test
	void test_activation_and_disabling() {
		FileDeleter fileDeleter = new FileDeleter(tempDir.getAbsolutePath());
		assertFalse(FileDeleter.isActive());

		fileDeleter.activate();
		assertTrue(FileDeleter.isActive());

		fileDeleter.disable();
		assertFalse(FileDeleter.isActive());
	}
}
