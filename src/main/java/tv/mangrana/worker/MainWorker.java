package tv.mangrana.worker;

import tv.mangrana.config.ConfigFileLoader;
import tv.mangrana.exception.IncorrectWorkingReferencesException;

public class MainWorker {

    private final QueueFixer queueFixer;

    public MainWorker() throws IncorrectWorkingReferencesException {
        ConfigFileLoader configFileLoader = new ConfigFileLoader();
        queueFixer = new QueueFixer(configFileLoader);
    }

    private void work() {
        queueFixer.fix();
    }

    public static void main(String[] args) throws IncorrectWorkingReferencesException {
        var worker = new MainWorker();
        worker.work();
    }
}
