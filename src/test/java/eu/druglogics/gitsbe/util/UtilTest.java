package eu.druglogics.gitsbe.util;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.io.IOException;
import java.io.PrintStream;

import static eu.druglogics.gitsbe.util.Util.createDirectory;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
        // directory where the gitsbe code is (it exists because you are testing the code!)
        String userDir = System.getProperty("user.dir");
        PrintStream out = mock(PrintStream.class);
        System.setOut(out);

        assertDoesNotThrow(() -> Util.createDirectory(userDir));
        verify(out).println(ArgumentMatchers.contains("already exists - no need to recreate it!"));

        IOException exception = assertThrows(IOException.class, () -> createDirectory(""));
        assertEquals(exception.getMessage(), "Error in creating directory: , exiting.");
    }

    @Test
    void test_is_numeric_string() {
        assertTrue(Util.isNumericString("132"));
        assertTrue(Util.isNumericString("-12"));
        assertTrue(Util.isNumericString("-12.237482397"));
        assertTrue(Util.isNumericString("+12.234787"));

        assertFalse(Util.isNumericString("-d12"));
        assertFalse(Util.isNumericString("e12"));
        assertFalse(Util.isNumericString("jira"));
    }

    @Test
    void test_byte_array_to_string() {
        byte[] arr1 = {0,1,0,0,0,1,1,1,0,1};
        byte[] arr2 = {};
        byte[] arr3 = {0};
        byte[] arr4 = {1};
        byte[] arr5 = {-1,1,-128,127,100,0,-0};

        assertEquals(Util.byteArrayToString(arr1), "0100011101");
        assertEquals(Util.byteArrayToString(arr2), "");
        assertEquals(Util.byteArrayToString(arr3), "0");
        assertEquals(Util.byteArrayToString(arr4), "1");

        assertEquals(Util.byteArrayToString(arr5), "-11-12812710000");
    }

    @Test
    void test_is_string_all_dashes() {
        assertTrue(Util.isStringAllDashes("-"));
        assertTrue(Util.isStringAllDashes("--"));
        assertTrue(Util.isStringAllDashes("------"));

        assertFalse(Util.isStringAllDashes(""));
        assertFalse(Util.isStringAllDashes(" -"));
        assertFalse(Util.isStringAllDashes("0"));
        assertFalse(Util.isStringAllDashes("01-"));
        assertFalse(Util.isStringAllDashes("----2 "));
        assertFalse(Util.isStringAllDashes("0001110101"));
    }
}
