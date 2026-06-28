package com.xunfang.system.tools;

import org.springframework.cglib.beans.BeanMap;

import java.io.*;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
* @ClassName: ObjectUtils
* @Description: TODO(判断对象是否为空)
* @author 刘念
* @date 2019年7月25日 下午2:16:10
*
 */
public class ObjectUtils {

    /**
     * 判断对象是否非空
     * 
     * @param o
     * @return
     */
    public static boolean isNull(Object o) {
        return null == o;
    }

    /**
     * 判断集合是否非空
     * 
     * @param list
     * @return
     */
    public static boolean isNull(List<?> list) {

        return null == list || list.size() == 0;
    }

    /**
     * 判断集合是否非空
     * 
     * @param list
     * @return
     */
    public static boolean isNull(Set<?> set) {

        return null == set || set.size() == 0;
    }

    /**
     * 判断集合是否为空
     * 
     * @param map
     * @return
     */
    public static boolean isNull(Map<?, ?> map) {
        return null == map || map.size() == 0;
    }

    /**
     * 判断Long是否为空
     * 
     * @param lg
     * @return
     */
    public static boolean isNull(Long lg) {
        return null == lg || lg == 0;
    }

    /**
     * 判断Integer是否为空
     * 
     * @param it
     * @return
     */
    public static boolean isNull(Integer it) {
        return null == it || it == 0;
    }

    public static boolean isNull(File file) {
        return null == file || !file.exists();
    }

    /**
     * 判断数组是否为空
     * 
     * @param strs
     * @return
     */
    public static boolean isNull(Object[] strs) {
        return null == strs || strs.length == 0;
    }

    /**
     * 获取数字 空返回0
     * 
     * @param number
     * @return
     */
    public static Number getNumber(Number number) {

        return ObjectUtils.isNull(number) ? 0L : number;
    }

    /**
     * 数字格式化
     * 
     * @param number
     * @param pattern
     *            (转化格式，默认#.##，其它的自己上网查)
     * @return
     */
    public static String numberFormat(Number number, String... pattern) {
        if (isNull(pattern)) {
            return FORMAT.format(number);
        }

        return FORMAT.format(pattern[0]);
    }

    private static Format FORMAT = new DecimalFormat("#.##");

    /**
     * 克隆
     * 
     * @param o
     * @return
     */
    public static Object clone(Object o) {
        if (null == o) {
            return null;
        }

        // 将对象序列化后写在流里,因为写在流里面的对象是一份拷贝,
        // 原对象仍然在JVM里
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(o);
            ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            return ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != bos) {
                    bos.close();
                }
                if (null != oos) {
                    oos.close();
                }
                if (null != ois) {
                    ois.close();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 判断对象是否非空
     * 
     * @param o
     * @return
     */
    public static boolean isNotNull(Object o) {
        return !isNull(o);
    }

    /**
     * 判断集合是否非空
     * 
     * @param list
     * @return
     */
    public static boolean isNotNull(List<?> list) {

        return !isNull(list);
    }

    /**
     * 判断集合是否非空
     * 
     * @param list
     * @return
     */
    public static boolean isNotNull(Set<?> set) {

        return !isNull(set);
    }

    /**
     * 判断集合是否为空
     * 
     * @param map
     * @return
     */
    public static boolean isNotNull(Map<?, ?> map) {
        return !isNull(map);
    }

    /**
     * 判断Long是否为空
     * 
     * @param lg
     * @return
     */
    public static boolean isNotNull(Long lg) {
        return !isNull(lg);
    }

    /**
     * 判断Integer是否为空
     * 
     * @param it
     * @return
     */
    public static boolean isNotNull(Integer it) {
        return !isNull(it);
    }

    public static boolean isNotNull(File file) {
        return !isNull(file);
    }

    /**
     * 判断数组是否为空
     * 
     * @param strs
     * @return
     */
    public static boolean isNotNull(Object[] strs) {
        return !isNull(strs);
    }

    /**
     * 对象转Map
     * @param object 待转换的对象
     * @return k-v
     */
    public static Map<String, Object> beanToMap(Object object){
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (object != null) {
            BeanMap beanMap = BeanMap.create(object);
            for (Object key : beanMap.keySet()) {
                map.put(key+"", beanMap.get(key));
            }
        }
        return map;
    }

    /**
     * 求两个对象的属性的差集
     * @param obj1 第一个对象
     * @param obj2 第二个对象
     * @return 两个对象的不同属性Map
     */
    public static Map<String, Map<String,Object>> diffObj(Object obj1, Object obj2) {
        Map<String, Map<String,Object>> diffMap = new LinkedHashMap<>();
        try {

            Map<String,Object> map1 = beanToMap(obj1);
            Map<String,Object> map2 = beanToMap(obj2);
            Set<String> keySet1 = map1.keySet();
            Set<String> keySet2 = map2.keySet();

            // 判断key是否相同
            if(!keySet1.equals(keySet2)){
                throw new Exception("two objects with different internal properties.");
            }

            Map<String, Object> changeMap;

            // 根据 key 对比对象之间 val 的区别
            for (String key:keySet1){
                Object value1 = map1.getOrDefault(key, null);
                Object value2 = map2.getOrDefault(key, null);
                // 如果某个属性的值在两个对象中不同，则进行记录
                if ((value1 == null && value2 != null) || value1 != null && !value1.equals(value2)) {
                    changeMap = new LinkedHashMap<>();
                    changeMap.put("obj1", value1);
                    changeMap.put("obj2",value2);
                    diffMap.put(key, changeMap);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return diffMap;
    }

    /**
     * 判断列表是否只有1个项
     *
     * @param list 待判断列表
     * @return true or false
     */
    public static Boolean isOneItem(List list) {
        if (ObjectUtils.isNotNull(list)) {
            if (list.size() != 1) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
