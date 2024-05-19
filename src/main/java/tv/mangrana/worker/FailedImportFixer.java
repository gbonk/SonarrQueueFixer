package tv.mangrana.worker;

import tv.mangrana.sonarr.api.schema.queue.Record;
import tv.mangrana.sonarr.api.schema.series.SonarrSerie;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FailedImportFixer {
    public static final int MINIMUM_FILE_SIZE_TO_BE_CONSIDERED_A_VIDEO = 300000;
    private final Record queueRecord;
    private final SonarrSerie serie;
    private final MissingFilesDetector missingFilesDetector = new MissingFilesDetector();

    private FailedImportFixer(Record queueRecord, SonarrSerie serie) {
        this.queueRecord = queueRecord;
        this.serie = serie;
    }

    static FailedImportFixer of(Record queueRecord, SonarrSerie serie) {
        return new FailedImportFixer(queueRecord, serie);
    }

    void fix() throws IOException {
        System.out.printf("%nfixing: %s%n" ,queueRecord.getTitle());
        System.out.printf(">> located in: %s%n", queueRecord.getOutputPath());

        var torrentPath = Path.of(queueRecord.getOutputPath());
        var sonarPath = Path.of(serie.getPath());
        List<Path> torrentFiles = getVideoFilesFrom(torrentPath);
        List<Path> sonarFiles = getVideoFilesFrom(sonarPath);

        missingFilesDetector.printDifferencesBetween(torrentFiles, sonarFiles);
    }

    private List<Path> getVideoFilesFrom(Path torrentPath) throws IOException {
        try (var pathWalk = Files.walk(torrentPath, 3)) {
            return pathWalk
                    .filter(p -> p.toFile().isFile())
                    .filter(p -> p.toFile().length() > MINIMUM_FILE_SIZE_TO_BE_CONSIDERED_A_VIDEO)
                    .collect(Collectors.toList());
        }
    }

}
