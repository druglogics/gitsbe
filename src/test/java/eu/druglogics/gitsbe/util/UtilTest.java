package eu.druglogics.gitsbe.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UtilTest {

    @Test
    void test_replace_operators() {
        String equation1 = " A *= B ";
        String equation2 = " A *= false ";
        String equation3 = " A *= not ( true ) ";
        String equation4 = " A *=  (  (  B )  or C ) and not  (  ( E )  or F ) ";
        String equation5 = " A *=  (  (  false )  or C ) or not  (  ( true )  and F ) ";

        String expectedEquation1 = " A *= B ";
        String expectedEquation2 = " A *= 0 ";
        String expectedEquation3 = " A *= ! ( 1 ) ";
        String expectedEquation4 = " A *=  (  (  B )  | C ) & !  (  ( E )  | F ) ";
        String expectedEquation5 = " A *=  (  (  0 )  | C ) | !  (  ( 1 )  & F ) ";

        assertEquals(expectedEquation1, Util.replaceOperators(equation1));
        assertEquals(expectedEquation2, Util.replaceOperators(equation2));
        assertEquals(expectedEquation3, Util.replaceOperators(equation3));
        assertEquals(expectedEquation4, Util.replaceOperators(equation4));
        assertEquals(expectedEquation5, Util.replaceOperators(equation5));
    }

    @Test
    void test_infer_input_dir() {
        String userWorkDir = System.getProperty("user.dir");

        assertEquals(userWorkDir, Util.inferInputDir("filename"));
        assertEquals(userWorkDir + "/example_run_ags", Util.inferInputDir("example_run_ags/filename"));
        assertEquals("/home/user/test/directory", Util.inferInputDir("/home/user/test/directory/filename"));
    }

    @Test
    void test_remove_last_char() {
        String testChar1 = "test";
        String testChar2 = "verbosity:";

        assertEquals("tes", Util.removeLastChar(testChar1));
        assertEquals("verbosity", Util.removeLastChar(testChar2));
    }

    @Test
    void test_get_repeated_string() {
        String emptyStr = "";
        String testStr  = "test";

        assertEquals("", Util.getRepeatedString(emptyStr, -1));
        assertEquals("", Util.getRepeatedString(emptyStr, 0));
        assertEquals("", Util.getRepeatedString(emptyStr, 1));
        assertEquals("", Util.getRepeatedString(emptyStr, 3));

        assertEquals("", Util.getRepeatedString(testStr, -1));
        assertEquals("", Util.getRepeatedString(testStr, 0));
        assertEquals("test", Util.getRepeatedString(testStr, 1));
        assertEquals("testtest", Util.getRepeatedString(testStr, 2));
        assertEquals("testtesttesttest", Util.getRepeatedString(testStr, 4));
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
