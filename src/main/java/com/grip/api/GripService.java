package com.grip.api;

import com.grip.core.Grip;
import com.grip.reader.ReadmeReader;
import com.grip.renderer.ReadmeRenderer;
import org.springframework.stereotype.Service;
import java.util.Objects;

import java.lang.reflect.Constructor;

/**
 * Service class for creating and managing Grip instances.
 * This class handles the instantiation and configuration of Grip objects.
 */
@Service
public class GripService {

    /**
     * Creates a new Grip instance with the specified configuration.
     *
     * @param gripClass The Grip class to instantiate
     * @param source The source reader for content
     * @param auth Authentication credentials
     * @param renderer Content renderer
     * @param assetManager Asset manager (optional)
     * @param renderWide Whether to render in wide mode
     * @param renderInline Whether to render inline
     * @param title Custom page title
     * @param autorefresh Whether to enable auto-refresh
     * @param quiet Whether to suppress output
     * @param theme Theme to use
     * @return Configured Grip instance
     */
    public Grip createGripInstance(Class<? extends Grip> gripClass,
                                 ReadmeReader source,
                                 AuthCredentials auth,
                                 ReadmeRenderer renderer,
                                 Object assetManager,
                                 Boolean renderWide,
                                 Boolean renderInline,
                                 String title,
                                 Boolean autorefresh,
                                 Boolean quiet,
                                 String theme) {
        try {
            Constructor<? extends Grip> constructor = gripClass.getConstructor(
                ReadmeReader.class, AuthCredentials.class, ReadmeRenderer.class,
                Object.class, Boolean.class, Boolean.class, String.class,
                Boolean.class, Boolean.class, String.class
            );

            // Apply default values matching Python implementation
            Boolean effectiveRenderWide = Objects.requireNonNullElse(renderWide, false);
            Boolean effectiveRenderInline = Objects.requireNonNullElse(renderInline, false);
            Boolean effectiveAutorefresh = Objects.requireNonNullElse(autorefresh, false);
            Boolean effectiveQuiet = Objects.requireNonNullElse(quiet, false);
            String effectiveTheme = Objects.requireNonNullElse(theme, "light");

            return constructor.newInstance(
                source, auth, renderer, assetManager,
                effectiveRenderWide, effectiveRenderInline, title,
                effectiveAutorefresh, effectiveQuiet, effectiveTheme
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Grip instance", e);
        }
    }
}