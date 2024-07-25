package tv.mangrana.worker;

import tv.mangrana.config.ConfigLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import static tv.mangrana.config.ConfigLoader.ProjectConfiguration.UPLOADS_PATHS;

class FileCopier {

    static final String UNIX_ALL_PERMISSIONS = "rwxrwxrwx";
    static final FileAttribute<Set<PosixFilePermission>> ALL_PERMITTED_FILE_ATTRIBUTE;
    static {
        var allPermitted = PosixFilePermissions.fromString(UNIX_ALL_PERMISSIONS);
        ALL_PERMITTED_FILE_ATTRIBUTE = PosixFilePermissions.asFileAttribute(allPermitted);
    }

    void hardLink(Path source, Path destination) {
        try {
            createDestinationFolderIfApply(destination);

            if (!ConfigLoader.isTestMode())
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
            System.out.printf("** destination folder %s will be created%n", destinationFolder);
            if (!ConfigLoader.isTestMode())
                Files.createDirectories(destinationFolder, ALL_PERMITTED_FILE_ATTRIBUTE);
        }
    }

    private boolean isTemporaryDestination(Path destination) {
        String temporaryFolderName = ConfigLoader.getLoader()
                .getConfig(UPLOADS_PATHS)
                .toLowerCase();
        return destination.toString()
                .contains(temporaryFolderName);
    }

}
