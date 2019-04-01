package eu.druglogics.gitsbe.input;

import org.junit.Assert;
import org.junit.Test;

public class ConfigParametersTest {

    @Test
    public void test_get_parameter_names() {
        ConfigParameters parameters = new ConfigParameters();
        int expectedParNum = 30;
        Assert.assertEquals(expectedParNum, parameters.getParameters().length);
    }
}
