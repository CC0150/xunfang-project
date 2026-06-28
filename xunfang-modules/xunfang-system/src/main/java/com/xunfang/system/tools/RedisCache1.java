package com.xunfang.system.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 
* @ClassName: RedisCache
* @Description: TODO(RedisCache缓存工具类)
* @author 刘念
* @date 2023年5月8日 上午10:43:10
*
 */
@Component
public class RedisCache1 {
	private static final Logger logger = LoggerFactory.getLogger(RedisCache1.class);
	
    @SuppressWarnings("rawtypes")
	@Autowired
    private RedisTemplate redisTemplate;

	@SuppressWarnings("rawtypes")
	public void setRedisTemplate(RedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
    
    //=============================common============================  
    /** 
     * 指定缓存失效时间 
     * @param key 键 
     * @param time 时间(秒) 
     * @return 
     */  
	@SuppressWarnings("unchecked")
	public boolean expire(String key,long time){  
        try {  
            if(time>0){  
                redisTemplate.expire(key, time, TimeUnit.SECONDS);  
            }  
            return true;  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when expire cache - other exceptions...",e);
            return false;  
        }  
    }  

    /** 
     * 根据key 获取过期时间 
     * @param key 键 不能为null 
     * @return 时间(秒) 返回0代表为永久有效 
     */  
	@SuppressWarnings("unchecked")
    public long getExpire(String key){  
        return redisTemplate.getExpire(key,TimeUnit.SECONDS);  
    }  

    /** 
     * 判断key是否存在 
     * @param key 键 
     * @return true 存在 false不存在 
     */  
	@SuppressWarnings("unchecked")
    public boolean hasKey(String key){  
        try {  
            return redisTemplate.hasKey(key);  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when hasKey cache - other exceptions...",e);
            return false;  
        }  
    }  

    /** 
     * 删除缓存 
     * @param key 可以传一个值 或多个 
     */  
    @SuppressWarnings("unchecked")  
    public void del(String ... key){  
        if(key!=null&&key.length>0){  
            if(key.length==1){  
                redisTemplate.delete(key[0]);  
                logger.debug("[DEL]\tKey=" + key[0]);
            }else{  
                redisTemplate.delete(CollectionUtils.arrayToList(key));
                logger.debug("[DEL]\tKey=" + key);
            }  
        }  
    }  

    //============================String=============================  
    /** 
     * 普通缓存获取 
     * @param key 键 
     * @return 值 
     */  
    public Object get(String key){  
    	if(key==null){
    		return null;
    	}
    	Object o = redisTemplate.opsForValue().get(key);
    	logger.debug("[get]\tKey=" + key+"\tValue="+o);
        return o;  
    }  

    /** 
     * 普通缓存放入 
     * @param key 键 
     * @param value 值 
     * @return true成功 false失败 
     */  
    @SuppressWarnings("unchecked")
    public boolean set(String key,Object value) {  
         try {  
            redisTemplate.opsForValue().set(key, value);  
            logger.debug("[set]\tKey=" + key + "\tValue="+value);
            return true;  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when set cache - other exceptions...",e);
            return false;  
        }  

    }  

    /** 
     * 普通缓存放入并设置时间 
     * @param key 键 
     * @param value 值 
     * @param time 时间(秒) time要大于0 如果time小于等于0 将设置无限期 
     * @return true成功 false 失败 
     */  
    @SuppressWarnings("unchecked")
    public boolean set(String key,Object value,long time){  
        try {  
            if(time>0){  
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);  
                logger.debug("[set]\tKey=" + key + "\tValue="+value+"\tTime="+time);
            }else{  
                set(key, value);  
            }  
            return true;  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when set cache time - other exceptions...",e);
            return false;  
        }  
    }  

    /** 
     * 递增 
     * @param key 键 
     * @param by 要增加几(大于0) 
     * @return 
     */  
    @SuppressWarnings("unchecked")
    public long incr(String key, long delta){    
        if(delta<0){  
            throw new RuntimeException("递增因子必须大于0");  
        }  
        return redisTemplate.opsForValue().increment(key, delta);  
    }  

    /** 
     * 递减 
     * @param key 键 
     * @param by 要减少几(小于0) 
     * @return 
     */  
    @SuppressWarnings("unchecked")
    public long decr(String key, long delta){    
        if(delta<0){  
            throw new RuntimeException("递减因子必须大于0");  
        }  
        return redisTemplate.opsForValue().increment(key, -delta);    
    }    

    //================================Map=================================  
    /** 
     * HashGet 
     * @param key 键 不能为null 
     * @param item 项 不能为null 
     * @return 值 
     */  
    @SuppressWarnings("unchecked")
    public Object hget(String key,String item){  
        return redisTemplate.opsForHash().get(key, item);  
    }  

    /** 
     * 获取hashKey对应的所有键值 
     * @param key 键 
     * @return 对应的多个键值 
     */  
    @SuppressWarnings("unchecked")
    public Map<Object,Object> hmget(String key){  
        return redisTemplate.opsForHash().entries(key);  
    }  

    /** 
     * HashSet 
     * @param key 键 
     * @param map 对应多个键值 
     * @return true 成功 false 失败 
     */  
    @SuppressWarnings("unchecked")
    public boolean hmset(String key, Map<String,Object> map){    
        try {  
            redisTemplate.opsForHash().putAll(key, map);  
            return true;  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when hmset cache - other exceptions...",e);
            return false;  
        }  
    }  

    /** 
     * HashSet 并设置时间 
     * @param key 键 
     * @param map 对应多个键值 
     * @param time 时间(秒) 
     * @return true成功 false失败 
     */  
    @SuppressWarnings("unchecked")
    public boolean hmset(String key, Map<String,Object> map, long time){    
        try {  
            redisTemplate.opsForHash().putAll(key, map);  
            if(time>0){  
                expire(key, time);  
            }  
            return true;  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when hmset cache time - other exceptions...",e);
            return false;  
        }  
    }  

    /** 
     * 向一张hash表中放入数据,如果不存在将创建 
     * @param key 键 
     * @param item 项 
     * @param value 值 
     * @return true 成功 false失败 
     */  
    @SuppressWarnings("unchecked")
    public boolean hset(String key,String item,Object value) {  
         try {  
            redisTemplate.opsForHash().put(key, item, value);  
            return true;  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when hset cache - other exceptions...",e);
            return false;  
        }  
    }  

    /** 
     * 向一张hash表中放入数据,如果不存在将创建 
     * @param key 键 
     * @param item 项 
     * @param value 值 
     * @param time 时间(秒)  注意:如果已存在的hash表有时间,这里将会替换原有的时间 
     * @return true 成功 false失败 
     */  
    @SuppressWarnings("unchecked")
    public boolean hset(String key,String item,Object value,long time) {  
         try {  
            redisTemplate.opsForHash().put(key, item, value);  
            if(time>0){  
                expire(key, time);  
            }  
            return true;  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when hset cache time - other exceptions...",e);
            return false;  
        }  
    }  

    /** 
     * 删除hash表中的值 
     * @param key 键 不能为null 
     * @param item 项 可以使多个 不能为null 
     */  
    @SuppressWarnings("unchecked")
    public void hdel(String key, Object... item){    
        redisTemplate.opsForHash().delete(key,item);  
    }   

    /** 
     * 判断hash表中是否有该项的值 
     * @param key 键 不能为null 
     * @param item 项 不能为null 
     * @return true 存在 false不存在 
     */  
    @SuppressWarnings("unchecked")
    public boolean hHasKey(String key, String item){  
        return redisTemplate.opsForHash().hasKey(key, item);  
    }   

    /** 
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回 
     * @param key 键 
     * @param item 项 
     * @param by 要增加几(大于0) 
     * @return 
     */  
    @SuppressWarnings("unchecked")
    public double hincr(String key, String item,double by){    
        return redisTemplate.opsForHash().increment(key, item, by);  
    }  

    /** 
     * hash递减 
     * @param key 键 
     * @param item 项 
     * @param by 要减少记(小于0) 
     * @return 
     */  
    @SuppressWarnings("unchecked")
    public double hdecr(String key, String item,double by){    
        return redisTemplate.opsForHash().increment(key, item,-by);    
    }    

    //============================set=============================  
    /** 
     * 根据key获取Set中的所有值 
     * @param key 键 
     * @return 
     */  
    @SuppressWarnings("unchecked")
    public Set<Object> sGet(String key){  
        try {  
            return redisTemplate.opsForSet().members(key);  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when sGet cache - other exceptions...",e);
            return null;  
        }  
    }  

    /** 
     * 根据value从一个set中查询,是否存在 
     * @param key 键 
     * @param value 值 
     * @return true 存在 false不存在 
     */  
    @SuppressWarnings("unchecked")
    public boolean sHasKey(String key,Object value){  
        try {  
            return redisTemplate.opsForSet().isMember(key, value);  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when sHasKey cache - other exceptions...",e);
            return false;  
        }  
    }  

    /** 
     * 将数据放入set缓存 
     * @param key 键 
     * @param values 值 可以是多个 
     * @return 成功个数 
     */  
    @SuppressWarnings("unchecked")
    public long sSet(String key, Object...values) {  
        try {  
            return redisTemplate.opsForSet().add(key, values);  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when sSet cache - other exceptions...",e);
            return 0;  
        }  
    }  

    /** 
     * 将set数据放入缓存 
     * @param key 键 
     * @param time 时间(秒) 
     * @param values 值 可以是多个 
     * @return 成功个数 
     */  
    @SuppressWarnings("unchecked")
    public long sSetAndTime(String key,long time,Object...values) {  
        try {  
            Long count = redisTemplate.opsForSet().add(key, values);  
            if(time>0) expire(key, time);  
            return count;  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when sSetAndTime cache - other exceptions...",e);
            return 0;  
        }  
    }  

    /** 
     * 获取set缓存的长度 
     * @param key 键 
     * @return 
     */  
    @SuppressWarnings("unchecked")
    public long sGetSetSize(String key){  
        try {  
            return redisTemplate.opsForSet().size(key);  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when sGetSetSize cache - other exceptions...",e);
            return 0;  
        }  
    }  

    /** 
     * 移除值为value的 
     * @param key 键 
     * @param values 值 可以是多个 
     * @return 移除的个数 
     */  
    @SuppressWarnings("unchecked")
    public long setRemove(String key, Object ...values) {  
        try {  
            Long count = redisTemplate.opsForSet().remove(key, values);  
            return count;  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when setRemove cache - other exceptions...",e);
            return 0;  
        }  
    }  
    //===============================list=================================  

    /** 
     * 获取list缓存的内容 
     * @param key 键 
     * @param start 开始 
     * @param end 结束  0 到 -1代表所有值 
     * @return 
     */  
    @SuppressWarnings("unchecked")
    public List<Object> lGet(String key,long start, long end){  
        try {  
            return redisTemplate.opsForList().range(key, start, end);  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when lGet cache - other exceptions...",e);
            return null;  
        }  
    }  

    /** 
     * 获取list缓存的长度 
     * @param key 键 
     * @return 
     */  
    @SuppressWarnings("unchecked")
    public long lGetListSize(String key){  
        try {  
            return redisTemplate.opsForList().size(key);  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when lGetListSize cache - other exceptions...",e);
            return 0;  
        }  
    }  

    /** 
     * 通过索引 获取list中的值 
     * @param key 键 
     * @param index 索引  index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推 
     * @return 
     */  
    @SuppressWarnings("unchecked")
    public Object lGetIndex(String key,long index){  
        try {  
            return redisTemplate.opsForList().index(key, index);  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when lGetIndex cache - other exceptions...",e);
            return null;  
        }  
    }  

    /** 
     * 将list放入缓存 
     * @param key 键 
     * @param value 值 
     * @param time 时间(秒) 
     * @return 
     */  
    @SuppressWarnings("unchecked")
    public boolean lSet(String key, Object value) {  
        try {  
            redisTemplate.opsForList().rightPush(key, value);  
            return true;  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when lSet cache - other exceptions...",e);
            return false;  
        }  
    }  

    /** 
     * 将list放入缓存 
     * @param key 键 
     * @param value 值 
     * @param time 时间(秒) 
     * @return 
     */  
    @SuppressWarnings("unchecked")
    public boolean lSet(String key, Object value, long time) {  
        try {  
            redisTemplate.opsForList().rightPush(key, value);  
            if (time > 0) expire(key, time);  
            return true;  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when lSet cache time - other exceptions...",e);
            return false;  
        }  
    }  

    /** 
     * 将list放入缓存 
     * @param key 键 
     * @param value 值 
     * @param time 时间(秒) 
     * @return 
     */  
    @SuppressWarnings("unchecked")
    public boolean lSet(String key, List<Object> value) {  
        try {  
            redisTemplate.opsForList().rightPushAll(key, value);  
            return true;  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when lSet cache - other exceptions...",e);
            return false;  
        }  
    }  

    /** 
     * 将list放入缓存 
     * @param key 键 
     * @param value 值 
     * @param time 时间(秒) 
     * @return 
     */  
    @SuppressWarnings("unchecked")
    public boolean lSet(String key, List<Object> value, long time) {  
        try {  
            redisTemplate.opsForList().rightPushAll(key, value);  
            if (time > 0) expire(key, time);  
            return true;  
        } catch (Exception e) {  
            e.printStackTrace();  
            return false;  
        }  
    }  

    /** 
     * 根据索引修改list中的某条数据 
     * @param key 键 
     * @param index 索引 
     * @param value 值 
     * @return 
     */  
    @SuppressWarnings("unchecked")
    public boolean lUpdateIndex(String key, long index,Object value) {  
        try {  
            redisTemplate.opsForList().set(key, index, value);  
            return true;  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when lUpdateIndex cache - other exceptions...",e);
            return false;  
        }  
    }   

    /** 
     * 移除N个值为value  
     * @param key 键 
     * @param count 移除多少个 
     * @param value 值 
     * @return 移除的个数 
     */  
    @SuppressWarnings("unchecked")
    public long lRemove(String key,long count,Object value) {  
        try {  
            Long remove = redisTemplate.opsForList().remove(key, count, value);  
            return remove;  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when lRemove cache - other exceptions...",e);
            return 0;  
        }  
    } 
    
    /** 
     * 根据通配符获取的keys集合
     * @param  prefix 通配符
     * @return 
     */
    @SuppressWarnings("unchecked")
	public Set<String> getKeys(String prefix){
    	if(null==prefix || "".equals(prefix)){
    		prefix = "*";
    	}
    	return redisTemplate.keys(prefix);
    }
    
    /** 
     * 获取随机key
     * @return 
     */
    public Object getRandomKey(){
    	return redisTemplate.randomKey();
    }
    
    /**
     * 
    * @Title: clear
    * @Description: TODO(一键删除当前redis中所有缓存)
    * @return
    * boolean
    * @author 刘念 
    * @date 2023年6月1日 上午9:04:54
     */
    @SuppressWarnings("unchecked")
	public boolean flushAll(){
        try {  
        	// 获取所有的key
            Set<String> keys = getKeys("");
            // 如果存在key，则逐个删除
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.debug("[DEBUG]redis flushAll keys:"+keys);
            } 
            return true;  
        } catch (Exception e) {  
            logger.error("[ERROR]exception when redis flushAll...",e);
            return false;  
        }  
    }
}
