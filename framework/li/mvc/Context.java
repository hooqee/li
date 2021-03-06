package li.mvc;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import li.dao.Page;
import li.model.Action;
import li.util.Convert;
import li.util.Files;
import li.util.Log;
import li.util.Reflect;
import li.util.Verify;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 访问HTTP请求上下文的工具类,使用ThreadLocal
 * 
 * @author li (limingwei@mail.com)
 * @version 0.1.1 (2012-07-20)
 * @see li.mvc.AbstractAction
 */
public class Context {
    private static final Properties PROPERTIES = Files.load("config.properties");

    static final String VIEW_TYPE = PROPERTIES.getProperty("view.type", "forward");
    static final String VIEW_PREFIX = PROPERTIES.getProperty("view.prefix", "");
    static final String VIEW_SUFFIX = PROPERTIES.getProperty("view.suffix", "");

    private static final ThreadLocal<HttpServletRequest> REQUEST = new ThreadLocal<HttpServletRequest>();
    private static final ThreadLocal<HttpServletResponse> RESPONSE = new ThreadLocal<HttpServletResponse>();
    private static final ThreadLocal<Action> ACTION = new ThreadLocal<Action>();

    private static final Log log = Log.init();

    /**
     * 初始化方法,会将request,response,action分别存入ThreadLocal
     */
    public static void init(ServletRequest request, ServletResponse response, Action action) {
        REQUEST.set((HttpServletRequest) request);
        RESPONSE.set((HttpServletResponse) response);
        ACTION.set(action);
    }

    /**
     * 从request,sesstion,servletContext中取出Attributes转为Map
     */
    public static Map<String, Object> getAttributes() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("application", getServletContext());
        map.put("servletContext", getServletContext());
        map.put("request", getRequest());
        map.put("response", getResponse());
        map.put("session", getSession());
        Enumeration<?> servletContextEnumeration = getServletContext().getAttributeNames();
        while (servletContextEnumeration.hasMoreElements()) {// 1. 存入servletContext的值
            String name = (String) servletContextEnumeration.nextElement();
            map.put(name, getServletContext().getAttribute(name));
        }
        Enumeration<?> sessionEnumeration = getSession().getAttributeNames();
        while (sessionEnumeration.hasMoreElements()) {// 2. 存入session的值
            String name = (String) sessionEnumeration.nextElement();
            map.put(name, getSession().getAttribute(name));
        }
        Enumeration<?> requestEnumeration = getRequest().getAttributeNames();
        while (requestEnumeration.hasMoreElements()) {// 3. 存入request的值
            String name = (String) requestEnumeration.nextElement();
            map.put(name, getRequest().getAttribute(name));
        }
        return map;
    }

    /**
     * 返回Action引用
     */
    public static Action getAction() {
        return ACTION.get();
    }

    /**
     * 返回request引用
     */
    public static HttpServletRequest getRequest() {
        return REQUEST.get();
    }

    /**
     * 返回response引用
     */
    public static HttpServletResponse getResponse() {
        return RESPONSE.get();
    }

    /**
     * 返回ServletContext
     */
    public static ServletContext getServletContext() {
        return getSession().getServletContext();// for servlet 2.5 -
    }

    /**
     * 返回Session引用
     */
    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    /**
     * 返回基本类型的数组参数
     */
    public static <T> T[] getArray(Class<T> type, String key) {
        return Convert.toType(type, (Object[]) getRequest().getParameterValues(key));
    }

    /**
     * 根据QueryString中的页码参数构建一个Page,或者一个默认的Page
     */
    public static Page getPage(String pageNumberKey) {
        Page page = (Page) getSession().getAttribute("page");
        if (null == page) {
            page = new Page();
        }
        String pnStr = getRequest().getParameter(pageNumberKey);
        page.setPageNumber(Verify.isEmpty(pnStr) ? 1 : Integer.valueOf(pnStr));
        return page;
    }

    /**
     * 从 request 的 parameters中得到数据组装成一个type类型的对象
     * 
     * @param type 对象类型
     * @param prefix 查询 Parameters 时候的 key 的前缀
     */
    public static <T> T get(Class<T> type, String prefix) {
        T t = Reflect.born(type);
        Set<Entry<String, String[]>> parameterSet = getRequest().getParameterMap().entrySet();
        for (Entry<String, String[]> entry : parameterSet) {// 迭代ParameterMap
            if (Verify.isEmpty(prefix) || Verify.startWith(entry.getKey(), prefix)) {// 前缀为空或者参数Key以前缀开头
                String fieldName = entry.getKey().substring(prefix.length());// 属性名
                Reflect.set(t, fieldName, entry.getValue()[0]);
            }
        }
        return t;
    }

    /**
     * 将QueryString中对应key的参数设置到request里面
     */
    public static void keepParams(String... keys) {
        for (String key : keys) {
            getRequest().setAttribute(key, getRequest().getParameter(key));
        }
    }

    /**
     * 返回项目文件系统跟路径
     */
    public static String getRootPath() {
        return getServletContext().getRealPath("/");
    }

    /**
     * 返回项目HTTP根路径
     */
    public static String getRootUrl() {
        return getRequest().getScheme() + "://" + getRequest().getServerName() + ":" + getRequest().getServerPort() + getRequest().getContextPath() + "/";
    }

    /**
     * 主视图方法,以冒号分割前缀表示视图类型
     * 
     * @see #forward(String)
     * @see #freemarker(String)
     * @see #redirect(String)
     * @see #write(Object)
     */
    public static String view(String path) {
        String viewType = path.contains(":") ? path.split(":")[0] : VIEW_TYPE;// 视图类型
        String viewPath = path.startsWith(viewType + ":") ? path.split(viewType + ":")[1] : path;// path冒号后的部分或者path
        if ("forward".equals(viewType) || "fw".equals(viewType)) {// forward视图
            forward(VIEW_PREFIX + viewPath + VIEW_SUFFIX);
        } else if ("freemarker".equals(viewType) || "fm".equals(viewType)) {// freemarker视图
            freemarker(VIEW_PREFIX + viewPath + VIEW_SUFFIX);
        } else if ("redirect".equals(viewType) || "rd".equals(viewType)) {// redirect跳转
            redirect(viewPath);
        } else if ("write".equals(viewType) || "wt".equals(viewType)) {// 向页面write数据
            write(viewPath);
        } else {
            throw new RuntimeException("view error, not supported viewtype: " + path);
        }
        return "~!@#DONE";
    }

    /**
     * 执行客户端跳转
     */
    public static String redirect(String path) {
        log.debug("redirect to ?", path);
        try {
            getResponse().sendRedirect(path);
        } catch (Exception e) {
            throw new RuntimeException(e + " ", e);
        }
        return "~!@#DONE";
    }

    /**
     * 返回forward视图
     */
    public static String forward(String path) {
        log.debug("forward to ?", path);
        try {
            getRequest().getRequestDispatcher(path).forward(getRequest(), getResponse());
        } catch (Exception e) {
            throw new RuntimeException(e + " ", e);
        }
        return "~!@#DONE";
    }

    /**
     * 返回freemarker视图
     */
    public static String freemarker(String path) {
        try {
            Configuration configuration = (Configuration) Log.get("~!@#FREEMARKER_CONFIGURATION"); // 从缓存中查找freemarkerConfiguration
            if (null == configuration) { // 缓存中没有
                log.info("freemarker initializing ..");
                configuration = new Configuration();// 初始化freemarkerConfiguration
                configuration.setServletContextForTemplateLoading(getServletContext(), "/");// 设置模板加载跟路径
                configuration.setSettings(new Properties() {
                    private static final long serialVersionUID = 1L;
                    {
                        put("default_encoding", "UTF-8");// 默认参数
                        putAll(Files.load("freemarker.properties"));// freemarker.properties中的参数设置
                    }
                });// 加载自定义配置
                Log.put("~!@#FREEMARKER_CONFIGURATION", configuration); // 缓存freemarkerConfiguration
            }
            Template template = configuration.getTemplate(path);// 加载模板
            template.process(getAttributes(), getResponse().getWriter());
            log.debug("freemarker to: ?", path);
        } catch (Throwable e) {
            throw new RuntimeException(e + " ", e);
        }
        return "~!@#DONE";
    }

    /**
     * 把 content写到页面上
     */
    public static void write(Object content) {
        if (!Verify.isEmpty(content)) {
            String contentStr = content.toString();
            final String JSON_REGEX = "^[\\[]*[{]+.*[}]+[]]*$", XML_REGEX = "^<.*>$";
            if (Verify.regex(contentStr, XML_REGEX)) {// 如果内容是XML
                getResponse().setContentType("text/xml;charset=UTF-8");
            } else if (Verify.regex(contentStr, JSON_REGEX)) {// 如果内容是JSON
                getResponse().setContentType("application/json;charset=UTF-8");
            } else {
                getResponse().setContentType("text/plain;charset=UTF-8");
            }
            try {
                getResponse().getWriter().write(contentStr);
            } catch (Exception e) {
                throw new RuntimeException(e + " ", e);
            }
        }
    }
}