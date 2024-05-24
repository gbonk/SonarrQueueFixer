package tv.mangrana.worker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static tv.mangrana.config.ConfigFileLoader.ProjectConfiguration.UPLOADS_PATHS;

class FileCopier {
    void hardLink(Path source, Path destination) {
        try {
            createDestinationFolderIfApply(destination);
            Files.createLink(destination, source);
        } catch (IOException e) {
            System.out.printf("error when creating hardlink with destination %s, error: %s%n",
                    destination, e.getMessage());
            e.printStackTrace();
        }
    }

    private void createDestinationFolderIfApply(Path destination) throws IOException {
        if (isTemporaryDestination(destination) && !Files.exists(destination))
            Files.createDirectory(destination);
    }

    private boolean isTemporaryDestination(Path destination) {
        return destination.toString()
                .contains(UPLOADS_PATHS.name().toLowerCase());
    }
}
