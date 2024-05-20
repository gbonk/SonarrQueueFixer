package tv.mangrana.worker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class FileCopier {
    void hardLink(Path source, Path destination) {
        try {
            Files.createLink(destination, source);
        } catch (IOException e) {
            System.out.printf("error when creating hardlink with destination %s, error: %s%n",
                    destination, e.getMessage());
            e.printStackTrace();
        }
    }
}
