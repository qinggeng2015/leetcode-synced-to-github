package cn.yklove;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author qinggeng
 */
public class Application {

    private static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        File propertiesFile = new File("application.properties");
        if (!propertiesFile.exists()) {
            logger.error("配置文件不存在!");
            System.exit(0);
        }
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFile));
        } catch (IOException e) {
            logger.error("配置文件读取失败，请检查格式!");
            System.exit(0);
        }

        ApplicationContext applicationContext = new ApplicationContext(properties);
        applicationContext.start();

    }

}
