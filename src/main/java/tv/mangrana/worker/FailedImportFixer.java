package tv.mangrana.worker;

import tv.mangrana.exception.IncorrectWorkingReferencesException;
import tv.mangrana.sonarr.api.schema.queue.Record;
import tv.mangrana.sonarr.api.schema.series.SonarrSerie;
import tv.mangrana.utils.StringCaptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FailedImportFixer {
    public static final int MINIMUM_FILE_SIZE_TO_BE_CONSIDERED_A_VIDEO = 300000;
    private final Record queueRecord;
    private final MissingFilesDetector missingFilesDetector = new MissingFilesDetector();
    private final Path torrentPath;
    private final Path seriePath;
    private final FileCopier fileCopier;
    private Path seasonPath;

    static Factory factory() {
        return new Factory();
    }
    public static class Factory {
        FailedImportFixer newFixerFor(Record queueRecord, SonarrSerie serie){
            return new FailedImportFixer(queueRecord, serie);
        }
    }

    private FailedImportFixer(Record queueRecord, SonarrSerie serie) {
        this.queueRecord = queueRecord;
        this.torrentPath = Path.of(queueRecord.getOutputPath());
        this.seriePath = Path.of(serie.getPath());
        fileCopier = new FileCopier();
    }

    void fix() throws IOException {
        System.out.printf("%nfixing: %s%n" ,queueRecord.getTitle());
        List<Path> torrentFiles = resolveTorrentFiles();
        List<Path> sonarFiles = resolveSonarrFiles();
        List<Path> filesToCopy = missingFilesDetector.getMissingFilesAtDestination(torrentFiles, sonarFiles);
        filesToCopy.forEach(this::copy);
    }

    private List<Path> resolveTorrentFiles() throws IOException {
        return getVideoFilesFrom(Path.of(queueRecord.getOutputPath()));
    }

    private List<Path> resolveSonarrFiles() throws IOException {
        seasonPath = seriePath.resolve(getSeasonFolder());
        return getVideoFilesFrom(seasonPath);
    }

    private String getSeasonFolder() {
        try {
            return StringCaptor.getSeasonFolderNameFromSeason(torrentPath.getFileName().toString());
        } catch (IncorrectWorkingReferencesException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Path> getVideoFilesFrom(Path path) throws IOException {
        System.out.println("going to explore "+path);
        try (var pathWalk = Files.walk(path, 3)) {
            return pathWalk
                    .filter(p -> p.toFile().isFile())
                    .filter(p -> p.toFile().length() > MINIMUM_FILE_SIZE_TO_BE_CONSIDERED_A_VIDEO)
                    .collect(Collectors.toList());
        }
    }

    private void copy(Path fileToCopy) {
        Path target = seasonPath.resolve(fileToCopy.getFileName());
        System.out.println("** going to hardlink file to "+target);
        fileCopier.hardLink(fileToCopy, target);
    }
}
