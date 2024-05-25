package tv.mangrana.config;

import tv.mangrana.exception.IncorrectWorkingReferencesException;

public class ConfigLoader extends CommonConfigFileLoader<ConfigLoader.ProjectConfiguration> {

    private static final String CONFIG_FILE = "SonarrFixerConfig.yml";
    private static ConfigLoader service;

    private ConfigLoader() throws IncorrectWorkingReferencesException {
        super(ProjectConfiguration.class);
    }

    public static ConfigLoader getLoader() throws IncorrectWorkingReferencesException {
        if (service==null)
            service = new ConfigLoader();
        return service;
    }

    public static String get(ProjectConfiguration configParam) throws IncorrectWorkingReferencesException {
        return getLoader().getConfig(configParam);
    }

    public static boolean isEnabled(ProjectConfiguration configParam) {
        return get(configParam).equals("true");
    }

    public enum ProjectConfiguration {
        UPLOADS_PATHS,
        TEST_MODE
    }

    @Override
    protected String getConfigFileName() {
        return CONFIG_FILE;
    }

}
