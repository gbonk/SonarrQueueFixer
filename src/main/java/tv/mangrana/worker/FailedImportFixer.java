package tv.mangrana.worker;

import tv.mangrana.sonarr.api.schema.queue.Record;

public class FailedImportFixer {
    private final String elementTitle;
    private final Record queueRecord;

    private FailedImportFixer(String elementTitle, Record queueRecord) {
        this.elementTitle = elementTitle;
        this.queueRecord = queueRecord;
    }

    static FailedImportFixer of(String elementTitle, Record queueRecord) {
        return new FailedImportFixer(elementTitle, queueRecord);
    }

    void fix() {
        System.out.printf("fixing: %s%n",elementTitle);
        System.out.printf(">> located in: %s%n%n",queueRecord.getOutputPath());
    }
}
