package listenerproject;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
/**
 * 
 * @author hezuoan
 *
 */
public class ApplicationListener implements ServletContextListener {
    
    private static Logger log = Logger.getLogger(ApplicationListener.class);
    @Override
    public void contextInitialized(ServletContextEvent sce) {

//获取log4j配置文件的地址
		URL urlPath = ApplicationListener.class.getResource("/");
		String basePath = null;
		try {
			basePath = URLDecoder.decode(urlPath.toString(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String filePath = basePath + "log4j2.properties";
		filePath = filePath.substring(filePath.indexOf(":") + 2);
		PropertyConfigurator.configure(filePath);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) { }

}