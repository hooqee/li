package li.dao;

import java.util.Map;
import java.util.Map.Entry;

import li.model.Bean;
import li.model.Field;
import li.util.Convert;
import li.util.Files;
import li.util.Reflect;
import li.util.Verify;

/**
 * Dao的辅助类,用以组装SQL
 * 
 * @author li (limingwei@mail.com)
 * @version 0.1.8 (2012-05-08)
 */
public class QueryBuilder {
    private static final String MAP_ARG_SIGN = Files.load("config.properties").getProperty("dao.mapArgSign", "#");

    /**
     * 表示对象结构的beanMeta
     */
    protected Bean beanMeta;

    /**
     * 根据传入的ID,构建一个用于删除单条记录的SQL
     */
    public String deleteById(Object id) {
        return "DELETE FROM " + beanMeta.table + " WHERE " + beanMeta.getId().column + "=" + wrap(id);
    }

    /**
     * 根据传入的SQL,构建一个用于删除若干条记录的SQL
     * 
     * @param sql 传入的sql语句,可以包含'?'占位符和具名占位符
     * @param args 替换sql中占位符的值,或者对应具名占位符的Map
     * @see li.dao.QueryBuilder#setArgs(String, Object[])
     */
    public String deleteBySql(String sql, Object[] args) {
        if (!Verify.startWith(sql, "DELETE")) {
            sql = "DELETE FROM " + beanMeta.table + " " + sql;
        }
        return setArgs(sql, args);// 处理args
    }

    /**
     * 构造默认的COUNT(*)查询的SQL,查询表中的总记录数
     */
    public String countAll() {
        return "SELECT COUNT(*) FROM " + beanMeta.table;
    }

    /**
     * 根据传入的SQL,构造一个用于COUNT(*)查询的SQL
     * 
     * @param sql 传入的sql语句,可以包含'?'占位符和具名占位符
     * @param args 替换sql中占位符的值,或者对应具名占位符的Map
     * @see li.dao.QueryBuilder#setArgs(String, Object[])
     */
    public String countBySql(String sql, Object[] args) {
        if (!Verify.startWith(sql, "SELECT")) {// 不以SELECT开头
            sql = "SELECT COUNT(*) FROM " + beanMeta.table + " " + sql;
        } else if (!Verify.regex(sql.toUpperCase(), "COUNT\\(.*\\)")) {// 不包括COUNT(*)
            sql = "SELECT COUNT(*) FROM " + sql.substring(sql.toUpperCase().indexOf("FROM") + 4, sql.length()).trim();
        }
        int index = sql.toUpperCase().indexOf("LIMIT");
        if (index > 0) {
            sql = sql.substring(0, index);// 去掉limit部分
        }
        return setArgs(sql, args);// 处理args
    }

    /**
     * 使用传入的ID,构造一个用于查询一条记录的SQL
     */
    public String findById(Object id) {
        return "SELECT * FROM " + beanMeta.table + " WHERE " + beanMeta.getId().column + "=" + wrap(id);
    }

    /**
     * 使用传入的SQL和参数,构造一个用于查询一条记录的SQL
     */
    public String findBySql(String sql, Object[] args) {
        if (!Verify.startWith(sql, "SELECT")) {// 添加SELECT * FROM table 部分
            sql = "SELECT * FROM " + beanMeta.table + " " + sql;
        }
        return setArgs(sql, args);// 先处理别名,再处理page
    }

    /**
     * 使用传入的page,构造一个用于分页查询的SQL
     * 
     * @param page 分页对象
     */
    public String list(Page page) {
        return setPage("SELECT * FROM " + beanMeta.table, page);
    }

    /**
     * 根据传入的SQL和page,构造一个用于分页查询的SQL
     * 
     * @param page 分页对象
     * @param sql 传入的sql语句,可以包含'?'占位符和具名占位符
     * @param args 替换sql中占位符的值,或者对应具名占位符的Map
     * @see li.dao.QueryBuilder#setPage(String, Page)
     * @see li.dao.QueryBuilder#setArgs(String, Object[])
     */
    public String listBySql(Page page, String sql, Object[] args) {
        if (!Verify.startWith(sql, "SELECT")) {// 添加SELECT * FROM table 部分
            sql = "SELECT * FROM " + beanMeta.table + " " + sql;
        }
        return setPage(setArgs(sql, args), page);// 先处理别名,再处理args,最后处理page
    }

    /**
     * 根据传入的对象构建一个用于更新一条记录的SQL
     */
    public String update(Object entity) {
        String sql = "UPDATE " + beanMeta.table + " SET ";
        for (Field field : beanMeta.fields) {
            Object fieldValue = Reflect.get(entity, field.name);
            if (!beanMeta.getId().name.equals(field.name)) {// 更新所有属性,fieldValue可能为null
                sql += field.column + "=" + wrap(fieldValue) + ",";
            }
        }
        Object id = Reflect.get(entity, beanMeta.getId().name);
        return sql.substring(0, sql.length() - 1) + " WHERE " + beanMeta.getId().column + "=" + wrap(id);
    }

    /**
     * 根据传入的对象构建一个用于更新一条记录的SQL,忽略对象中值为null的属性
     */
    public String updateIgnoreNull(Object entity) {
        String sql = "UPDATE " + beanMeta.table + " SET ";
        for (Field field : beanMeta.fields) {
            Object fieldValue = Reflect.get(entity, field.name);
            if (!beanMeta.getId().name.equals(field.name) && !Verify.isEmpty(fieldValue)) {// 更新所有属性,fieldValue可能为null
                sql += field.column + "=" + wrap(fieldValue) + ",";
            }
        }
        Object id = Reflect.get(entity, beanMeta.getId().name);
        return sql.substring(0, sql.length() - 1) + " WHERE " + beanMeta.getId().column + "=" + wrap(id);
    }

    /**
     * 根据传入的SQL,构建一个用于更新若干条记录的SQL
     * 
     * @param sql 传入的sql语句,可以包含'?'占位符和具名占位符
     * @param args 替换sql中占位符的值,或者对应具名占位符的Map
     * @see li.dao.QueryBuilder#setArgs(String, Object[])
     */
    public String updateBySql(String sql, Object[] args) {
        if (!Verify.startWith(sql, "UPDATE")) {
            sql = "UPDATE " + beanMeta.table + " " + sql;
        }
        return setArgs(sql, args);// 处理args
    }

    /**
     * 根据传入的对象构建一个插入一条记录的SQL
     */
    public String insert(Object entity) {
        String columns = " (", values = " VALUES (";
        for (Field field : beanMeta.fields) {
            Object fieldValue = Reflect.get(entity, field.name);
            columns += field.column + ",";
            values += wrap(fieldValue) + ",";
        }
        columns = columns.substring(0, columns.length() - 1) + ")";
        values = values.substring(0, values.length() - 1) + ")";
        return "INSERT INTO " + beanMeta.table + columns + values;
    }

    /**
     * 根据传入的对象构建一个插入一条记录的SQL,忽略为空的属性
     */
    public String insertIgnoreNull(Object entity) {
        String columns = " (", values = " VALUES (";
        for (Field field : beanMeta.fields) {
            Object fieldValue = Reflect.get(entity, field.name);
            if (!Verify.isEmpty(fieldValue)) {// 略过为null的属性
                columns += field.column + ",";
                values += wrap(fieldValue) + ",";
            }
        }
        columns = columns.substring(0, columns.length() - 1) + ")";
        values = values.substring(0, values.length() - 1) + ")";
        return "INSERT INTO " + beanMeta.table + columns + values;
    }

    /**
     * 如果args不为空的话,SQL会用args逐次替换SQL中的占位符
     * 
     * @param sql 传入的sql语句,可以包含'?'占位符或者具名参数占位符
     * @param args 替换sql中 '?' 或具名参数占位符的值
     * @see li.dao.QueryBuilder#setArgMap(String, Map)
     */
    public String setArgs(String sql, Object[] args) {
        if (null != sql && sql.length() > 0 && null != args && args.length > 0) {// 非空判断
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Map<?, ?>) {
                    sql = setArgMap(sql, (Map<?, ?>) args[i]);// 替换具名参数
                } else {
                    sql = sql.replaceFirst("[?]", wrap(args[i]));// 为参数加上引号后替换问号
                }
            }
        }
        return sql;
    }

    /**
     * 用Map中的值替换SQL中的具名参数
     * 
     * @param sql 传入的sql语句,可以包含#key占位符
     * @param argMap 替换sql中#key 的键值Map
     */
    public String setArgMap(String sql, Map<?, ?> argMap) {
        if (null != sql && sql.length() > 0 && null != argMap && argMap.size() > 0) {// 非空判断
            for (Entry<?, ?> arg : argMap.entrySet()) {
                sql = sql.replaceAll(MAP_ARG_SIGN + arg.getKey(), wrap(arg.getValue()));// 为参数加上引号后替换问号
            }
        }
        return sql;
    }

    /**
     * 处理SQL参数
     */
    public String wrap(Object arg) {
        if (null == arg) {
            return "NULL";
        } else if (arg instanceof Number || arg instanceof Boolean) {
            return arg + "";// 数字和Boolean不加引号
        } else if (arg instanceof java.util.Date) {// 日期
            return "'" + Convert.format((java.util.Date) arg) + "'";
        } else {
            return "'" + arg + "'";// 其他类型
        }
    }

    /**
     * 为SQL添加分页语句
     */
    public String setPage(String sql, Page page) {
        if (!Verify.contain(sql, "LIMIT") && null != page) {// 分页
            return sql + " LIMIT " + page.getFrom() + "," + page.getPageSize();
        }
        return sql;
    }
}