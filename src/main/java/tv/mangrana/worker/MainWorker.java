package tv.mangrana.worker;

import tv.mangrana.config.ConfigFileLoader;
import tv.mangrana.exception.IncorrectWorkingReferencesException;
import tv.mangrana.sonarr.Sonarr;

public class MainWorker {

    private final QueueFixer queueFixer;

    public static void main(String[] args) throws IncorrectWorkingReferencesException {
        var worker = new MainWorker();
        worker.work();
    }

    private MainWorker() throws IncorrectWorkingReferencesException {
        var configLoader = ConfigFileLoader.getLoader();
        Sonarr.initService(configLoader);
        queueFixer = new QueueFixer();
    }

    private void work() {
        queueFixer.fix();
    }
}
