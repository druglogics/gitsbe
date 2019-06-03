package eu.druglogics.gitsbe.input;

import eu.druglogics.gitsbe.util.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ConfigTest {

    @AfterEach
    void reset_singleton() throws Exception {
        Field instance = Config.class.getDeclaredField("config");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    void test_get_instance_without_first_calling_init() {
        assertThrows(AssertionError.class, Config::getInstance);
    }

    @Test
    void test_get_instance_with_first_calling_init() throws Exception {
        Logger mockLogger = mock(Logger.class);

        ClassLoader classLoader = getClass().getClassLoader();
        String filename = new File(classLoader.getResource("test_config").getFile()).getPath();

        Config.init(filename, mockLogger);

        assertEquals(3, Config.getInstance().getVerbosity());
        assertTrue(Config.getInstance().useParallelSimulations());
        assertEquals(10, Config.getInstance().getGenerations());
        assertEquals(5, Config.getInstance().getCrossovers());
    }

    @Test
    void test_init_twice() throws Exception {
        Logger mockLogger = mock(Logger.class);

        ClassLoader classLoader = getClass().getClassLoader();
        String filename = new File(classLoader.getResource("test_config").getFile()).getPath();

        // initializes config class
        Config.init(filename, mockLogger);

        // initialization cannot happen twice!
        assertThrows(AssertionError.class, () -> Config.init(filename, mockLogger));
    }
}
