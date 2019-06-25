package util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.hutool.core.util.StringUtil;
import com.alibaba.fastjson.JSONObject;

import javax.websocket.Session;

public class WxUtil {

	private static final Logger LOG = LoggerFactory.getLogger(WxUtil.class);

	private static final String WX_API_DOMAIN = "https://api.weixin.qq.com";
	private static final String WX_API_URL = "https://api.weixin.qq.com/cgi-bin/";
	//获取微信的access_token
	private static final String GET_TOKEN_SUFFIX = "token";//获取access_token
	private static final String GET_TICKET_SUFFIX = "ticket/getticket";//获取票据jsapi_ticket
	private static final String GET_MENU_CRAETE_SUFFIX = "menu/create";// 同步菜单
	private static final String ADD_MATERIAL_SUFFIX = "material/add_material";//素材添加
	private static final String DEL_MATERIAL_SUFFIX = "material/del_material";//素材删除
	private static final String UPLOAD_IMAGE_SUFFIX = "media/get";// 上传图片
	private static final String SEND_TEMPLATE_MSG_SUFFIX = "message/template/send";//发送微信模板休息
	private static final String WX_oauth2_URL = "/sns/oauth2/access_token";//网页授权

	public static final String GET_WEBAUTH_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
	public static final String GET_WEIXIN_USER_URL = "https://api.weixin.qq.com/cgi-bin/user/info";//获取用户信息
	public static final String GET_PERSON_INFORMATION="https://api.weixin.qq.com/sns/userinfo";
	public static final String POST_MESSAGE="https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=";

	public static String GRANT_TYPE = null;
	public static String APP_ID = "******";
	public static String APP_SECRET = "******";
	public static String TYPE = null;
	public static String MY_TOKEN = null;
	public static String MY_SERVER_URL = null;
	public static String JGMC = null;
	public static String LXWM_LXDH = null;
	public static String ACCESS_TOKEN = null;
	public static String TIKECT = null;
	public static Session WS_SESSION = null;//判断管理员是否在线
	public static String CURRENT_USER_OPENID = null; //当前实时追踪的用户openid

	public static CloseableHttpClient httpClient = HttpClients.createDefault();

	public static JSONObject getToWx(String url, Map<String, String> params) {
		HttpGet httpGet = new HttpGet(url);
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(20000).setConnectTimeout(20000).build();// 设置请求和传输超时时间
		httpGet.setConfig(requestConfig);
		try {
			if (params != null && !params.isEmpty()) {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				Set<Entry<String, String>> sets = params.entrySet();
				Iterator<Entry<String, String>> it = sets.iterator();
				while (it.hasNext()) {
					Entry<String, String> ent = it.next();
					String key = ent.getKey();
					String value = ent.getValue();
					NameValuePair nvp = new BasicNameValuePair(key, value);
					nvps.add(nvp);
				}

				// Get请求 设置参数
				String str = EntityUtils.toString(new UrlEncodedFormEntity(nvps, "UTF-8"));
				url = httpGet.getURI().toString() + "?" + str;
			}

			httpGet.setURI(new URI(url));

			CloseableHttpResponse response = httpClient.execute(httpGet);
			int httpStatusCode = response.getStatusLine().getStatusCode();
			if (httpStatusCode == 200) {
				// 获得调用成功后 返回的数据
				HttpEntity entity = response.getEntity();
				if (null != entity) {
					String result = EntityUtils.toString(entity, "UTF-8");
					if (!StringUtil.isNullOrEmpty(result)) {
						JSONObject resultObj = JSONObject.parseObject(result);
						return resultObj;
					} else {
						throw new ClientProtocolException("response  string  is  blank ! ");
					}
				} else {
					throw new ClientProtocolException("HttpEntity  is  null ! ");
				}
			} else {
				throw new ClientProtocolException("error  status: " + httpStatusCode);
			}
		} catch (ParseException e) {
			LOG.error("发生错误",e);
		} catch (UnsupportedEncodingException e) {
			LOG.error("发生错误",e);
		} catch (URISyntaxException e) {
			LOG.error("发生错误",e);
		} catch (IOException e) {
			LOG.error("发生错误",e);
		}
		return null;
	}

	public static JSONObject getToWx(String url) {
		return getToWx(url, null);
	}

	/**
	 * 发起post请求
	 *
	 * @return
	 */
	public static JSONObject postToWx(String url, String content) {
		String result =  postToWxStr(url, content);
		JSONObject resultObj = JSONObject.parseObject(result);
		return resultObj;
	}

	public static String postToWxStr(String url, String content) {
		LOG.error("开始发送数据");
		HttpPost httpPost = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(20000).setConnectTimeout(20000).build();// 设置请求和传输超时时间
		httpPost.setConfig(requestConfig);
		LOG.error("设置请求头部");
		if (!StringUtil.isNullOrEmpty(content)) {
			StringEntity myEntity = new StringEntity(content, "UTF-8");
			httpPost.setEntity(myEntity);
		}
		CloseableHttpResponse response;
		try {
			LOG.error("开始获取返回流");
			response = httpClient.execute(httpPost);
			LOG.error("已获取返回流");
			int httpStatusCode = response.getStatusLine().getStatusCode();
			if (httpStatusCode == 200) {
				// 获得调用成功后 返回的数据
				HttpEntity entity = response.getEntity();
				if (null != entity) {
					String result = EntityUtils.toString(entity, "UTF-8");
					if (!StringUtil.isNullOrEmpty(result)) {
						return result;
					} else {
						throw new ClientProtocolException(
								"response  string  is  blank ! ");
					}
				} else {
					throw new ClientProtocolException("HttpEntity  is  null ! ");
				}
			} else {
				throw new ClientProtocolException("error  status: "
						+ httpStatusCode);
			}
		} catch (ClientProtocolException e) {
			LOG.error("发送数据失败", e);
		} catch (IOException e) {
			LOG.error("发送数据失败", e);
		}
		return null;
	}


	public static String postToWxStrReturnOutTime(String url, String content) {
		HttpPost httpPost = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(20000).setConnectTimeout(20000).build();// 设置请求和传输超时时间
		httpPost.setConfig(requestConfig);
		if (!StringUtil.isNullOrEmpty(content)) {
			StringEntity myEntity = new StringEntity(content, "UTF-8");
			httpPost.setEntity(myEntity);
		}
		CloseableHttpResponse response;
		try {
			response = httpClient.execute(httpPost);
			int httpStatusCode = response.getStatusLine().getStatusCode();
			if (httpStatusCode == 200) {
				// 获得调用成功后 返回的数据
				HttpEntity entity = response.getEntity();
				if (null != entity) {
					String result = EntityUtils.toString(entity, "UTF-8");
					if (!StringUtil.isNullOrEmpty(result)) {
						return result;
					} else {
						throw new ClientProtocolException(
								"response  string  is  blank ! ");
					}
				} else {
					throw new ClientProtocolException("HttpEntity  is  null ! ");
				}
			} else {
				throw new ClientProtocolException("error  status: "
						+ httpStatusCode);
			}
		} catch (ClientProtocolException e) {
			LOG.error("发送数据失败", e);
		} catch (IOException e) {
			LOG.error("发送数据失败", e);
		}
		return null;
	}

	public static JSONObject uploadToWx(String url, String filePath) {
		HttpPost httpPost = new HttpPost(url);
		FileBody bin = new FileBody(new File(filePath));

		LOG.error("文件名称" + bin.getFilename());
		LOG.error("文件名称是否存在" + bin.getFile().exists());
		HttpEntity reqEntity = MultipartEntityBuilder.create()
				.addPart("media", bin).build();
		httpPost.setEntity(reqEntity);
		System.out.println("发起请求的页面地址 " + httpPost.getRequestLine());
		// 发起请求 并返回请求的响应
		CloseableHttpResponse response = null;
		CloseableHttpClient httpClient2 = HttpClients.createDefault();
		try {
			response = httpClient2.execute(httpPost);
			System.out.println("----------------------------------------");
			// 打印响应状态
			System.out.println(response.getStatusLine());
			// 获取响应对象
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				// 打印响应长度
				System.out.println("Response content length: "
						+ resEntity.getContentLength());
				// 打印响应内容
				String result = EntityUtils.toString(resEntity,
						Charset.forName("UTF-8"));
				System.out.println(result);
				JSONObject jo = JSONObject.parseObject(result);
				return jo;
			}
			// 销毁
			EntityUtils.consume(resEntity);
		} catch (ClientProtocolException e) {
			LOG.error("httpClient 发送数据到微信异常", e);
			e.printStackTrace();
		} catch (IOException e) {
			LOG.error("流异常", e);
			e.printStackTrace();
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				LOG.error("关闭流异常", e);
				e.printStackTrace();
			}
			try {
				httpClient2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 建行支付请求
	 * @param url
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String postToCCB(String url, Map<String, Object> params) throws Exception {
		HttpPost httpPost = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(20000).setConnectTimeout(20000).build();// 设置请求和传输超时时间
		httpPost.setConfig(requestConfig);
		if (params != null) {
			if (params != null && !params.isEmpty()) {
				List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
				Set<Entry<String, Object>> sets = params.entrySet();
				Iterator<Entry<String, Object>> it = sets.iterator();
				while (it.hasNext()) {
					Entry<String, Object> ent = it.next();
					String key = ent.getKey();
					String value = ent.getValue().toString();
					BasicNameValuePair nvp = new BasicNameValuePair(key, value);
					nvps.add(nvp);
				}

				// POST请求 设置参数
				StringEntity myEntity = new UrlEncodedFormEntity(nvps, "UTF-8");
				httpPost.setEntity(myEntity);
				///url = httpPost.getURI().toString() + "?" + str;
			}
		}
		CloseableHttpResponse response;
		try {
			response = httpClient.execute(httpPost);
			int httpStatusCode = response.getStatusLine().getStatusCode();
			if (httpStatusCode == 200) {
				// 获得调用成功后 返回的数据
				HttpEntity entity = response.getEntity();
				if (null != entity) {
					String result = EntityUtils.toString(entity, "UTF-8");
					if (result != null) {
						return result;
					} else {
						throw new ClientProtocolException(
								"response  string  is  blank ! ");
					}
				} else {
					throw new ClientProtocolException("HttpEntity  is  null ! ");
				}
			} else {
				throw new ClientProtocolException("error  status: "
						+ httpStatusCode);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getTokenUrl () {
		return WX_API_URL + GET_TOKEN_SUFFIX;
	}

	public static String getTicketUrl () {
		return WX_API_URL + GET_TICKET_SUFFIX;
	}

	public static String getWebTokenUrl() { return GET_WEBAUTH_URL;}

	public static String getPersonInformationUrl() { return GET_PERSON_INFORMATION;}  //网页授权

	public static String getPersonInformationByIdUrl() { return GET_WEIXIN_USER_URL;}  //通过openid直接获取

	public static String postMessageUrl(){return POST_MESSAGE+WxUtil.ACCESS_TOKEN;}


	public static String createMenuUrl() {
		return WX_API_URL + GET_MENU_CRAETE_SUFFIX;
	}

	public static String addMaterialUrl() {
		return WX_API_URL + ADD_MATERIAL_SUFFIX;
	}

	public static String delMaterialUrl() {
		return WX_API_URL + DEL_MATERIAL_SUFFIX;
	}

	public static String uploadImageUrl() {
		return WX_API_URL + UPLOAD_IMAGE_SUFFIX;
	}

	public static String oauth2AuthorizeUrl () {
		return WX_API_DOMAIN + WX_oauth2_URL;
	}

	public static String sendTemplateMsgUrl () {
		return WX_API_URL + SEND_TEMPLATE_MSG_SUFFIX;
	}
}
