package listenerproject;

import org.apache.log4j.Logger;
import service.WxService;

import javax.servlet.*;
import java.io.IOException;

public class InitFilter implements Filter {
    private static Logger log = Logger.getLogger(InitFilter.class);
    @Override  
    public void destroy() {  

    }  

    @Override  
    public void init(FilterConfig config) throws ServletException {
        WxService wxservice=new WxService();
        wxservice.doontime();    //每隔1个小时，获取一次微信的access_token
        //log.error("error信息");
    }

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {
		
	}  
}
