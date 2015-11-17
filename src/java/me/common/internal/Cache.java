package me.common.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public abstract class Cache {

	public abstract void init();
	public abstract void put(String key, Object object);

	public abstract Object get(String key);

	public abstract void remove(String key);
	public abstract void clear();


	public abstract int getTotalNum();

	public abstract List<String> getMatchKey(String key);

	public abstract void put(String key, Object object, long expire);

	/**
	 * 对象序列化
	 * 
	 * @param value
	 * @return
	 */
	protected byte[] value(Object value) {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ObjectOutputStream object = new ObjectOutputStream(output);
			object.writeObject(value);

			return output.toByteArray();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 对象反序列化
	 * 
	 * @param value
	 * @return
	 */
	protected Object object(byte[] value) {
		try {
			ByteArrayInputStream input = new ByteArrayInputStream(value);
			ObjectInputStream object = new ObjectInputStream(input);
			return object.readObject();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 加入有效期
	 * 
	 * @param object
	 * @param expire
	 * @return
	 */
	protected byte[] assembleValue(Object object, long expire) {
        byte[] value = value(object);
        byte[] cacheValue = new byte[value.length + 8];
        long expireTime = Long.MAX_VALUE;
        if (expire >= 0) {
            expireTime = System.currentTimeMillis() + expire;
        }
        cacheValue[0] = (byte) ((expireTime >> 56) & 0xFF);
        cacheValue[1] = (byte) ((expireTime >> 48) & 0xFF);
        cacheValue[2] = (byte) ((expireTime >> 40) & 0xFF);
        cacheValue[3] = (byte) ((expireTime >> 32) & 0xFF);
        cacheValue[4] = (byte) ((expireTime >> 24) & 0xFF);
        cacheValue[5] = (byte) ((expireTime >> 16) & 0xFF);
        cacheValue[6] = (byte) ((expireTime >> 8) & 0xFF);
        cacheValue[7] = (byte) ((expireTime) & 0xFF);
        System.arraycopy(value, 0, cacheValue, 8, value.length);
        return cacheValue;
    }
	
	protected Object disassembleValue(String key, byte[] cacheValue) {
	    if (cacheValue == null) {
	        return null;
	    }
	    long expireTime = ((long) (cacheValue[0] & 0xFF) << 56)
	              + ((long) (cacheValue[1] & 0xFF) << 48)
	              + ((long) (cacheValue[2] & 0xFF) << 40)
	              + ((long) (cacheValue[3] & 0xFF) << 32)
	              + ((long) (cacheValue[4] & 0xFF) << 24)
	              + ((long) (cacheValue[5] & 0xFF) << 16)
	              + ((long) (cacheValue[6] & 0xFF) << 8)
	              + ((long) (cacheValue[7] & 0xFF));
	    if (expireTime >= System.currentTimeMillis()) {
	        byte[] value = new byte[cacheValue.length - 8];
	        System.arraycopy(cacheValue, 8, value, 0, value.length);
	        return object(value);
	    } else {
	        remove(key);
	        return null;
	    }
	}

}
