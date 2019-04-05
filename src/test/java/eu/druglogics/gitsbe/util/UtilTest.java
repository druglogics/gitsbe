package eu.druglogics.gitsbe.util;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

public class UtilTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    // System Rules (JUnit 4) or System Lambda (JUnit 5)
    @Ignore("until I find a way to set an enviroment variable to null for testing")
    @Test
    public void test_null_bnet_env_var() throws Exception {
        exception.expect(Exception.class);

        Assert.assertNull(System.getenv("BNET_HOME"));
        Util.checkBNET();
    }

    @Test
    public void test_infer_input_dir() {
        String userWorkDir = System.getProperty("user.dir");

        Assert.assertEquals(userWorkDir, Util.inferInputDir("filename"));
        Assert.assertEquals(userWorkDir + "/example_run_ags", Util.inferInputDir("example_run_ags/filename"));
        Assert.assertEquals("/home/user/test/directory",Util.inferInputDir("/home/user/test/directory/filename"));
    }

    @Test
    public void test_remove_last_char() {
        String testchar1 = "test";
        String testchar2 = "verbosity:";

        Assert.assertEquals("tes", Util.removeLastChar(testchar1));
        Assert.assertEquals("verbosity", Util.removeLastChar(testchar2));
    }

    @Test
    public void test_remove_extension() {
        String testFile1 = "file.txt";
        String testFile2 = "/home/user/file.sif";

        Assert.assertEquals("file", Util.removeExtension(testFile1));
        Assert.assertEquals("file", Util.removeExtension(testFile2));
    }

    @Test
    public void test_get_file_extension() {
        String filename1 = "file1.png";
        String filename2 = "file2.cmd";
        String filename3 = "file3.try.sif";
        String filename4 = "/home/user/file3.try.sif";

        Assert.assertEquals(".png", Util.getFileExtension(filename1));
        Assert.assertEquals(".cmd", Util.getFileExtension(filename2));
        Assert.assertEquals(".sif", Util.getFileExtension(filename3));
        Assert.assertEquals(".sif", Util.getFileExtension(filename4));
    }

    @Test
    public void test_get_file_name() {
        String filename1 = "/home/usr/file1";
        String filename2 = "file2.cmd";
        String filename3 = "file3.try.sif";
        String filename4 = "/home/user/file4";

        Assert.assertEquals("file1", Util.getFileName(filename1));
        Assert.assertEquals("file2.cmd", Util.getFileName(filename2));
        Assert.assertEquals("file3.try.sif", Util.getFileName(filename3));
        Assert.assertEquals("file4", Util.getFileName(filename4));
    }

    @Test
    public void test_create_directory() throws IOException{
        exception.expect(IOException.class);

        String userDir = System.getProperty("user.dir");
        Util.createDirectory(userDir); // directory exists so it will just return
        Util.createDirectory("");
    }
}
