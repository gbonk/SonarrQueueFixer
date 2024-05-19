package tv.mangrana.worker;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MissingFilesDetector {

    private Map<Long, List<Path>> sonarrFilesByLength;
    private Map<Long, List<Path>> torrentFileLengths;

    List<Path> getMissingFilesAtDestination(List<Path> torrentFiles, List<Path> sonarrFiles) {
        System.out.printf("going to compare %d torrent files with %d sonar files%n",
                torrentFiles.size(), sonarrFiles.size());
        digestTorrentFiles(torrentFiles);
        digestSonarrFiles(sonarrFiles);
        return resolveMissingFiles();
    }

    private void digestTorrentFiles(List<Path> torrentFiles) {
        torrentFileLengths = getFileLengthsMapFrom(torrentFiles);
        throwIfDuplicatedSizes(torrentFileLengths);
    }

    private void digestSonarrFiles(List<Path> sonarrFiles) {
        sonarrFilesByLength = getFileLengthsMapFrom(sonarrFiles);
        throwIfDuplicatedSizes(sonarrFilesByLength);
    }

    Map<Long, List<Path>> getFileLengthsMapFrom(List<Path> files) {
        return files.stream()
                .collect(Collectors.groupingBy(p -> p.toFile().length()));
    }

    private void throwIfDuplicatedSizes(Map<Long, List<Path>> torrentFileLengths) {
        if (hasFilesWithSameSize(torrentFileLengths))
            throw new InsecureScenario();
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

    private List<Path> resolveMissingFiles() {
        return torrentFileLengths.entrySet()
                .stream()
                .filter(this::missingAtDestination)
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .toList();
    }

    private boolean missingAtDestination(Map.Entry<Long, List<Path>> torrentFileEntry) {
        return !sonarrFilesByLength.containsKey(torrentFileEntry.getKey());
    }

    private static final class InsecureScenario extends RuntimeException {}
}