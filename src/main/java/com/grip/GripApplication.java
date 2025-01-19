package com.grip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.grip.command.GripCommandLine;

/**
 * Grip - Render local readme files before sending off to GitHub.
 *
 * Note: The original Python code manipulated sys.path to include the grip package:
 *   sys.path.insert(1, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
 * In this Java/Spring Boot version, this is handled automatically through Maven's
 * dependency management and Spring Boot's component scanning. The classpath is
 * configured in pom.xml and the application context is managed by Spring Boot.
 *
 * @see com.grip.command.GripCommandLine
 */
@SpringBootApplication
public class GripApplication {

	public static void main(String[] args) {
		GripCommandLine.main(args);
	}

	/**
	 * Copyright notice from original Python file:
	 * :copyright: (c) 2014-2022 by Joe Esposito.
	 * :license: MIT, see LICENSE for more details.
	 */
}
