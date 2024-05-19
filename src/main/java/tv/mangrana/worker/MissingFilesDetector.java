package tv.mangrana.worker;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MissingFilesDetector {

    private Map<Long, List<Path>> sonarrFilesByLength;

    List<Path> getMissingFilesAtDestination(List<Path> torrentFiles, List<Path> sonarrFiles) {
        System.out.printf("going to compare %d torrent files with %d sonar files%n",
                torrentFiles.size(), sonarrFiles.size());
        Map<Long, List<Path>> torrentFileLengths = getFileLengthsMapFrom(torrentFiles);
        if (hasFilesWithSameSize(torrentFileLengths))
            return torrentFiles;
        sonarrFilesByLength = getFileLengthsMapFrom(sonarrFiles);
        if (hasFilesWithSameSize(sonarrFilesByLength))
            return torrentFiles;

        return resolveMissingFiles(torrentFileLengths);
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
            System.out.printf("!!! There is more than one file with the same size of %d. Name: %s %n",
                    sampleFile.length(), sampleFile.getName());
            return true;
        }
        return false;
    }

    private List<Path> resolveMissingFiles(Map<Long, List<Path>> torrentFilesByLength) {
        return torrentFilesByLength.entrySet()
                .stream()
                .filter(this::missingAtDestination)
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .toList();
    }

    private boolean missingAtDestination(Map.Entry<Long, List<Path>> torrentFileEntry) {
        return !sonarrFilesByLength.containsKey(torrentFileEntry.getKey());
    }

}