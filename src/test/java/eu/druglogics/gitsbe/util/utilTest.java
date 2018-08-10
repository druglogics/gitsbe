package eu.druglogics.gitsbe.util;

import org.junit.Assert;
import org.junit.Test;

public class utilTest {

    @Test
    public void test_inferInputDir() {
        String userWorkDir = System.getProperty("user.dir");

        Assert.assertEquals(userWorkDir, Util.inferInputDir("filename"));
        Assert.assertEquals(userWorkDir + "/example_run_ags", Util.inferInputDir("example_run_ags/filename"));
    }
}
