package net.neoforged.devlaunch;

import java.io.File;

/**
 * Loads the RenderDoc DLL, allowing attaching from the RenderDoc UI.
 * On Linux, this should have the same effect as LD_PRELOAD since at no point
 * have any OpenGL DLLs be loaded yet.
 */
final class RenderDocIntegration {
    private static final String SYSTEM_PROPERTY = "devLaunch.renderDocLibrary";

    private RenderDocIntegration() {
    }

    public static void init() {
        String renderDocLibrary = System.getProperty(SYSTEM_PROPERTY);
        if (renderDocLibrary == null) {
            return;
        }

        var libraryFile = new File(renderDocLibrary);
        if (!libraryFile.isFile()) {
            System.err.println("The RenderDoc library specified by the system property " + SYSTEM_PROPERTY
                               + " does not exist: " + libraryFile);
            System.exit(2);
        }

        System.out.println("Loading renderdoc from " + libraryFile.getAbsolutePath());
        System.load(libraryFile.getAbsolutePath());
    }
}
