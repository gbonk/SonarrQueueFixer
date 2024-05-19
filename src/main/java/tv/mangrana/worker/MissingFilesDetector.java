package tv.mangrana.worker;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MissingFilesDetector {

    void printDifferencesBetween(List<Path> torrentFiles, List<Path> sonarrFiles) {
        System.out.printf("going to compare %d torrent files with %d sonar files%n",
                torrentFiles.size(), sonarrFiles.size());
        Map<Long, List<Path>> torrentFileLengths = getFileLengthsMapFrom(torrentFiles);
        if (hasFilesWithSameSize(torrentFileLengths))
            return;
        Map<Long, List<Path>> sonarrFileLengths = getFileLengthsMapFrom(sonarrFiles);
        if (hasFilesWithSameSize(sonarrFileLengths))
            return;

        torrentFileLengths.keySet().stream()
                .filter(fileSize -> missingAtDestination(fileSize, sonarrFileLengths))
                .collect(Collectors.toSet());

    }

    Map<Long, List<Path>> getFileLengthsMapFrom(List<Path> files) {
        return files.stream()
                .collect(Collectors.groupingBy(p -> p.toFile().length()));
    }

    boolean hasFilesWithSameSize(Map<Long, List<Path>> torrentFileLengths) {
        return torrentFileLengths
                .values()
                .stream()
                .anyMatch(this::hasMultipleElements);
    }

    boolean hasMultipleElements(List<Path> paths) {
        if (paths.size() > 1) {
            var sampleFile = paths.get(0).toFile();
            System.out.printf("There is more than one file with the same size of %d. Name: %s %n",
                    sampleFile.length(), sampleFile.getName());
            return true;
        }
        return false;
    }

    boolean missingAtDestination(Long fileSize, Map<Long, List<Path>> torrentFileLengths) {
        return !torrentFileLengths.containsKey(fileSize);
    }
}