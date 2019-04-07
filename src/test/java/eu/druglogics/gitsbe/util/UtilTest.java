package eu.druglogics.gitsbe.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

class UtilTest {

    @Disabled("until System Lambda library is ready...")
    @Test
    void test_null_bnet_env_var() {
        assertThrows(Exception.class, () -> {
            assertNull(System.getenv("BNET_HOME"));
            Util.checkBNET();
        }, "");
    }

    @Test
    void test_infer_input_dir() {
        String userWorkDir = System.getProperty("user.dir");

        assertEquals(userWorkDir, Util.inferInputDir("filename"));
        assertEquals(userWorkDir + "/example_run_ags", Util.inferInputDir("example_run_ags/filename"));
        assertEquals("/home/user/test/directory",Util.inferInputDir("/home/user/test/directory/filename"));
    }

    @Test
    void test_remove_last_char() {
        String testchar1 = "test";
        String testchar2 = "verbosity:";

        assertEquals("tes", Util.removeLastChar(testchar1));
        assertEquals("verbosity", Util.removeLastChar(testchar2));
    }

    @Test
    void test_remove_extension() {
        String testFile1 = "file.txt";
        String testFile2 = "/home/user/file.sif";

        assertEquals("file", Util.removeExtension(testFile1));
        assertEquals("file", Util.removeExtension(testFile2));
    }

    @Test
    void test_get_file_extension() {
        String filename1 = "file1.png";
        String filename2 = "file2.cmd";
        String filename3 = "file3.try.sif";
        String filename4 = "/home/user/file3.try.sif";

        assertEquals(".png", Util.getFileExtension(filename1));
        assertEquals(".cmd", Util.getFileExtension(filename2));
        assertEquals(".sif", Util.getFileExtension(filename3));
        assertEquals(".sif", Util.getFileExtension(filename4));
    }

    @Test
    void test_get_file_name() {
        String filename1 = "/home/usr/file1";
        String filename2 = "file2.cmd";
        String filename3 = "file3.try.sif";
        String filename4 = "/home/user/file4";

        assertEquals("file1", Util.getFileName(filename1));
        assertEquals("file2.cmd", Util.getFileName(filename2));
        assertEquals("file3.try.sif", Util.getFileName(filename3));
        assertEquals("file4", Util.getFileName(filename4));
    }

    @Test
    void test_create_directory() {
        assertThrows(IOException.class, () -> {
            String userDir = System.getProperty("user.dir");
            Util.createDirectory(userDir); // directory exists so it will just return
            Util.createDirectory("");
        });
    }
}
