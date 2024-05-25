package tv.mangrana.worker;

import tv.mangrana.exception.IncorrectWorkingReferencesException;
import tv.mangrana.sonarr.api.schema.queue.Record;
import tv.mangrana.sonarr.api.schema.series.SonarrSerie;
import tv.mangrana.utils.StringCaptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class FailedImportFixer {
    static final int MINIMUM_FILE_SIZE_TO_BE_CONSIDERED_A_VIDEO = 300000;
    
    private final FileCopier fileCopier;
    private final MissingFilesDetector missingFilesDetector;

    private final Path torrentPath;
    private final String torrentTitle;
    private final Path seasonPath;

    static Factory factory() {
        return new Factory();
    }
    static class Factory {
        FailedImportFixer newFixerFor(Record queueRecord, SonarrSerie serie){
            return new FailedImportFixer(queueRecord, serie);
        }
    }

    private FailedImportFixer(Record queueRecord, SonarrSerie serie) {
        fileCopier = new FileCopier();
        missingFilesDetector = new MissingFilesDetector();

        torrentPath = Path.of(queueRecord.getOutputPath());
        torrentTitle = queueRecord.getTitle();

        var seriePath = Path.of(serie.getPath());
        seasonPath = seriePath.resolve(tryGettingSeasonFolder());
    }

    private String tryGettingSeasonFolder() {
        try {
            return StringCaptor.getSeasonFolderNameFromSeason(torrentTitle);
        } catch (IncorrectWorkingReferencesException e) {
            e.printStackTrace();
            throw new SeasonFolderUnretrievable();
        }
    }

    void fix() throws IOException {
        System.out.printf("%nfixing: %s%n" , torrentTitle);
        List<Path> torrentFiles = getVideoFilesFrom(torrentPath);
        List<Path> sonarFiles = getVideoFilesFrom(seasonPath);
        List<Path> filesToCopy = missingFilesDetector.getMissingFilesAtDestination(torrentFiles, sonarFiles);
        filesToCopy.forEach(this::copy);
    }

    private List<Path> getVideoFilesFrom(Path path) throws IOException {
        if (!Files.exists(path)) {
            System.out.printf("path %s doesn't exist on the filesystem%n", path);
            return List.of();
        }
        System.out.println("going to explore "+path);
        try (var pathsWalk = Files.walk(path, 3)) {
            return pathsWalk
                    .filter(this::isFile)
                    .filter(this::isAVideo)
                    .toList();
        }
    }
    private boolean isFile(Path path) {
        return path.toFile().isFile();
    }
    private boolean isAVideo(Path path) {
        return path.toFile().length() > MINIMUM_FILE_SIZE_TO_BE_CONSIDERED_A_VIDEO;
    }

    private void copy(Path fileToCopy) {
        Path target = seasonPath.resolve(fileToCopy.getFileName());
        System.out.println("** going to hardlink file to "+target);
        fileCopier.hardLink(fileToCopy, target);
    }

    static class SeasonFolderUnretrievable extends RuntimeException {}
}
