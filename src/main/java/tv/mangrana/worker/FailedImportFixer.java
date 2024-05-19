package tv.mangrana.worker;

import tv.mangrana.sonarr.api.schema.queue.Record;

public class FailedImportFixer {
    private final Record queueRecord;

    private FailedImportFixer(Record queueRecord) {
        this.queueRecord = queueRecord;
    }

    static FailedImportFixer of(Record queueRecord) {
        return new FailedImportFixer(queueRecord);
    }

    void fix() {
        System.out.printf("fixing: %s%n" ,queueRecord.getTitle());
        System.out.printf(">> located in: %s%n%n", queueRecord.getOutputPath());
    }
}
