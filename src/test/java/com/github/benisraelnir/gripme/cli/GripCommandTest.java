package com.github.benisraelnir.gripme.cli;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import com.github.benisraelnir.gripme.service.GitHubService;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class GripCommandTest {

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private GitHubService githubService;

    @Test
    void testDefaultValues() {
        GripCommand gripCommand = new GripCommand(context, githubService);
        CommandLine cmd = new CommandLine(gripCommand);
        cmd.parseArgs("README.md");

        assertEquals("README.md", gripCommand.getPath().toString());
        assertTrue(gripCommand.isBrowser());
        assertFalse(gripCommand.isWide());
        assertEquals(6419, gripCommand.getPort());
        assertNull(gripCommand.getUser());
        assertNull(gripCommand.getPass());
    }

    @Test
    void testCustomValues() {
        GripCommand gripCommand = new GripCommand(context, githubService);
        CommandLine cmd = new CommandLine(gripCommand);
        cmd.parseArgs(
            "README.md",
            "--port=8080",
            "--wide",
            "--no-browser",
            "--user=testuser",
            "--pass=testpass"
        );

        assertEquals("README.md", gripCommand.getPath().toString());
        assertFalse(gripCommand.isBrowser());
        assertTrue(gripCommand.isWide());
        assertEquals(8080, gripCommand.getPort());
        assertEquals("testuser", gripCommand.getUser());
        assertEquals("testpass", gripCommand.getPass());
    }

    @Test
    void testCallWithCredentials() {
        GripCommand gripCommand = new GripCommand(context, githubService);
        CommandLine cmd = new CommandLine(gripCommand);
        cmd.parseArgs(
            "README.md",
            "--user=testuser",
            "--pass=testpass"
        );

        assertEquals(0, gripCommand.call());
        verify(githubService).setCredentials("testuser", "testpass");
    }
}
