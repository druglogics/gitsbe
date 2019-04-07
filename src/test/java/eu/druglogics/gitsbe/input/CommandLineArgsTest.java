package eu.druglogics.gitsbe.input;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandLineArgsTest {

    @Test
    void test_all_arguments() {
        CommandLineArgs args = new CommandLineArgs();

        String[] argv = {"-c", "config", "-m", "model", "-t", "train", "-n", "network"};
        JCommander.newBuilder().addObject(args).build().parse(argv);

        assertNull(args.getProjectName());
        assertEquals(args.getFilenameNetwork(), "network");
        assertEquals(args.getFilenameConfig(), "config");
        assertEquals(args.getFilenameModelOutputs(), "model");
        assertEquals(args.getFilenameTrainingData(), "train");
    }

    @Test
    void test_no_arguments() {
        assertThrows(ParameterException.class, () -> {
            CommandLineArgs args = new CommandLineArgs();
            JCommander.newBuilder().addObject(args).build().parse("");
        },"");
    }

    @Test
    void test_missing_required_arguments() {
        assertThrows(ParameterException.class, () -> {
            CommandLineArgs args = new CommandLineArgs();
            String[] argv = {"-p", "project", "-c", "config"};
            JCommander.newBuilder().addObject(args).build().parse(argv);
        }, "");
    }

}
