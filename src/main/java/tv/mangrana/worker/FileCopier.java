package tv.mangrana.worker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileCopier {

    private void hardLink(Path source, Path destination) {
        try {
            Files.createLink(destination, source);
        } catch (IOException e) {
            System.out.printf("error when creating hardlink with destination %s, error: %s%n",
                    destination, e.getMessage());
            e.printStackTrace();
        }
    }
}
class ProjectPath {
    static Path of(String path) {
        String projectPath = System.getProperty("user.dir");
        return Path.of(projectPath+path);
    }
}
