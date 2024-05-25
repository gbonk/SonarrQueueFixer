package tv.mangrana.worker;

import tv.mangrana.config.ConfigLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static tv.mangrana.config.ConfigLoader.ProjectConfiguration.TEST_MODE;
import static tv.mangrana.config.ConfigLoader.ProjectConfiguration.UPLOADS_PATHS;

class FileCopier {
    void hardLink(Path source, Path destination) {
        try {
            createDestinationFolderIfApply(destination);

            if (ConfigLoader.isDisabled(TEST_MODE))
                Files.createLink(destination, source);
        } catch (IOException e) {
            System.out.printf("error when creating hardlink with destination %s, error: %s%n",
                    destination, e.getMessage());
            e.printStackTrace();
        }
    }

    private void createDestinationFolderIfApply(Path destination) throws IOException {
        if (isTemporaryDestination(destination) && !Files.exists(destination)) {
            System.out.printf("destination folder %s will be created", destination);
            if (ConfigLoader.isDisabled(TEST_MODE))
                Files.createDirectory(destination);
        }
    }

    private boolean isTemporaryDestination(Path destination) {
        return destination.toString()
                .contains(UPLOADS_PATHS.name().toLowerCase());
    }
}
