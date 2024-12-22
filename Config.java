import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    

    @SuppressWarnings("CallToPrintStackTrace")
    private static final Properties properties = new Properties();

    public static void chargerConfiguration(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            properties.load(fis);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public static String[] getArray(String key) {
        return properties.getProperty(key).split(",");
    }

    public static Properties getProperties() {
        return properties;
    }
}