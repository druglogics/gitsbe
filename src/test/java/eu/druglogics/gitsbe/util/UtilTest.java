package eu.druglogics.gitsbe.util;

import org.junit.Assert;
import org.junit.Test;

public class UtilTest {

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
}
