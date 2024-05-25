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

            if (!ConfigLoader.isEnabled(TEST_MODE))
                Files.createLink(destination, source);
        } catch (IOException e) {
            System.out.printf("error when creating hardlink with destination %s, error: %s%n",
                    destination, e.getMessage());
            e.printStackTrace();
        }
    }

    private void createDestinationFolderIfApply(Path destinationFile) throws IOException {
        var destinationFolder = destinationFile.getParent();
        if (isTemporaryDestination(destinationFolder) && !Files.exists(destinationFolder)) {
            System.out.printf("destination folder %s will be created", destinationFolder);
            if (!ConfigLoader.isEnabled(TEST_MODE))
                Files.createDirectories(destinationFile);
        }
    }

    private boolean isTemporaryDestination(Path destination) {
        return destination.toString()
                .contains(UPLOADS_PATHS.name().toLowerCase());
    }
}
