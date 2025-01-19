package com.grip.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for executing commands and capturing their output.
 * This class provides functionality similar to Python's subprocess.Popen.
 */
public class CommandExecutor {

    /**
     * Exception thrown when command execution fails.
     */
    public static class CommandExecutionException extends RuntimeException {
        private final int returnCode;
        private final String stdout;
        private final String stderr;

        public CommandExecutionException(int returnCode, String stdout, String stderr, String message) {
            super(message);
            this.returnCode = returnCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public int getReturnCode() {
            return returnCode;
        }

        public String getOutput() {
            return stdout;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }
    }

    /**
     * Executes the default 'grip' command with the given arguments.
     * Similar to Python's run() function in test_cli.py.
     *
     * @param args The command arguments
     * @return The command's output as a string
     * @throws CommandExecutionException if the command fails
     */
    public static String run(String... args) {
        return run(null, "grip", args);
    }

    /**
     * Executes a specified command with the given arguments.
     *
     * @param command The command to execute
     * @param args The command arguments
     * @return The command's output as a string
     * @throws CommandExecutionException if the command fails
     */
    public static String run(String command, String... args) {
        return run(null, command, args);
    }

    /**
     * Executes a command with stdin input and arguments.
     *
     * @param stdin The input to send to the process's stdin (null if none)
     * @param command The command to execute
     * @param args The command arguments
     * @return The command's output as a string
     * @throws CommandExecutionException if the command fails or if there's an error writing to stdin
     */
    private static class ProcessHandle implements AutoCloseable {
        private final Process process;
        private final BufferedReader stdoutReader;
        private final BufferedReader stderrReader;
        private final OutputStreamWriter stdinWriter;

        public ProcessHandle(Process process) {
            this.process = process;
            this.stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            this.stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
            this.stdinWriter = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8);
        }

        @Override
        public void close() throws IOException {
            try {
                stdinWriter.close();
            } finally {
                try {
                    stdoutReader.close();
                } finally {
                    try {
                        stderrReader.close();
                    } finally {
                        process.destroy();
                    }
                }
            }
        }

        public String readLine(BufferedReader reader) throws IOException {
            String line = reader.readLine();
            if (line != null) {
                // Normalize line endings (convert \r\n and \r to \n)
                return line.replace("\r\n", "\n").replace("\r", "\n");
            }
            return null;
        }

        public void writeStdin(String input) throws IOException {
            stdinWriter.write(input);
            stdinWriter.flush();
        }

        public String captureOutput(BufferedReader reader) throws IOException {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = readLine(reader)) != null) {
                output.append(line).append("\n");
            }
            return output.toString();
        }
    }

    public static String run(String stdin, String command, String... args) {
        // Combine command and args into a single array
        String[] fullCommand = new String[args.length + 1];
        fullCommand[0] = command;
        System.arraycopy(args, 0, fullCommand, 1, args.length);

        ProcessBuilder processBuilder = new ProcessBuilder(fullCommand);

        try (ProcessHandle handle = new ProcessHandle(processBuilder.start())) {
            // Handle stdin if provided
            if (stdin != null) {
                handle.writeStdin(stdin);
            }
            
            // Capture stdout and stderr
            String stdout = handle.captureOutput(handle.stdoutReader);
            String stderr = handle.captureOutput(handle.stderrReader);

            // Wait for process to complete
            int returnCode = handle.process.waitFor();

            if (returnCode != 0) {
                throw new CommandExecutionException(returnCode, stdout, stderr,
                        "Command failed with return code " + returnCode + ": " + String.join(" ", command));
            }

            return stdout;
        } catch (IOException | InterruptedException e) {
            throw new CommandExecutionException(-1, "", e.getMessage(),
                    "Failed to execute command: " + String.join(" ", command) + "\n" + e.getMessage());
        }
    }
}