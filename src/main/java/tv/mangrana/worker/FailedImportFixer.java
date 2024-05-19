package tv.mangrana.worker;

import tv.mangrana.sonarr.api.schema.queue.Record;
import tv.mangrana.sonarr.api.schema.series.SonarrSerie;

public class FailedImportFixer {
    private final Record queueRecord;

    private FailedImportFixer(Record queueRecord) {
        this.queueRecord = queueRecord;
    }

    static FailedImportFixer of(Record queueRecord, SonarrSerie serie) {
        return new FailedImportFixer(queueRecord);
    }

    void fix() {
        System.out.printf("fixing: %s%n" ,queueRecord.getTitle());
        System.out.printf(">> located in: %s%n%n", queueRecord.getOutputPath());
    }
}
