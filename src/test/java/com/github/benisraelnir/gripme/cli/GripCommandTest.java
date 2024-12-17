package com.github.benisraelnir.gripme.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import com.github.benisraelnir.gripme.service.GitHubService;
import picocli.CommandLine;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class GripCommandTest {

    @Mock
    private ConfigurableApplicationContext context;

    @Mock
    private GitHubService githubService;

    private GripCommand gripCommand;
    private CommandLine cmd;

    @BeforeEach
    void setUp() {
        gripCommand = new GripCommand(context, githubService);
        cmd = new CommandLine(gripCommand);
    }

    @Test
    void testDefaultValues() {
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
        cmd.parseArgs(
            "README.md",
            "--port=8080",
            "--wide",
            "--browser=false",
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
        cmd.parseArgs(
            "README.md",
            "--user=testuser",
            "--pass=testpass"
        );

        assertEquals(0, gripCommand.call());
        verify(githubService).setCredentials("testuser", "testpass");
    }
}
