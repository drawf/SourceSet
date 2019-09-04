package me.erwa.sourceset.framework.threadlocal

import java.lang.ref.WeakReference

/**
 * @author: drawf
 * @date: 2019-09-03
 * @see: <a href=""></a>
 * @description:
 */
open class MockThreadLocal<T> {

    /**
     * 往当前线程上绑定数据
     */
    fun set(value: T) {
        val t = Thread.currentThread() as MockThread
        val map = getMap(t)
        if (map != null)
            map.set(this, value as Any?)
        else
            createMap(t, value)
    }

    /**
     * 获取在当前线程上绑定的数据
     */
    fun get(): T? {
        // 获取当前的线程
        val t = Thread.currentThread() as MockThread
        // 获取当前线程持有的ThreadLocalMap
        val map = getMap(t)
        if (map != null) {
            // 如果map不为null，就使用自己作为key来获取value（MockThreadLocal的实例）
            val e = map.get(this)
            if (e != null) {
                return e as T?
            }
        }
        // 如果map为null，设置初始化的值，并返回该值
        return setInitialValue()
    }

    /**
     * 移除在当前线程上绑定的数据
     */
    fun remove() {
        val m = getMap(Thread.currentThread() as MockThread)
        m?.remove(this)
    }

    /**
     * 设置初始化的值
     */
    private fun setInitialValue(): T? {
        val value = initialValue()
        val t = Thread.currentThread() as MockThread
        val map = getMap(t)
        if (map != null)
            map.set(this, value as Any?)
        else
            createMap(t, value)
        return value
    }

    /**
     * 默认初始化的值，子类可复写该方法，自定义初始化值
     */
    open fun initialValue(): T? {
        return null
    }

    /**
     * 创建数据保存类，并赋值给线程
     */
    private fun createMap(t: MockThread, value: T?) {
        t.threadLocals = ThreadLocalMap(this, value as Any?)
    }

    /**
     * 获取线程中的数据保存类
     */
    private fun getMap(t: MockThread): ThreadLocalMap? {
        return t.threadLocals
    }

    /**
     * 定义该类，用于实际保存数据、处理数据
     */
    class ThreadLocalMap(firstKey: MockThreadLocal<*>, firstValue: Any?) {
        private var mMap: MutableMap<WeakReference<MockThreadLocal<*>>, Any?>? = null

        init {
            //首次初始化时，设置初始化值
            mMap = mutableMapOf(WeakReference(firstKey) to firstValue)
        }

        /**
         * 设置一个存储的数据
         */
        fun set(key: MockThreadLocal<*>, value: Any?) {
            //优先清除一次无用数据，防止内存泄漏
            expungeStaleEntry()
            if (mMap != null) {
                var keyExist = false
                mMap!!.forEach { (k, _) ->
                    //若相应的key已存在，只需替换该value即可
                    if (k.get() == key) {
                        mMap!![k] = value
                        keyExist = true
                    }
                }

                //若相应的key不存在，则保存新的数据
                if (!keyExist) {
                    mMap!![WeakReference(key)] = value
                }
            }
        }

        /**
         * 获取一个存储的数据
         */
        fun get(key: MockThreadLocal<*>): Any? {
            //优先清除一次无用数据，防止内存泄漏
            expungeStaleEntry()
            mMap?.forEach { (k, v) ->
                if (k.get() == key) {
                    return v
                }
            }
            return null
        }

        /**
         * 移除一个存储的数据
         */
        fun remove(key: MockThreadLocal<*>) {
            //优先清除一次无用数据，防止内存泄漏
            expungeStaleEntry()
            mMap?.forEach { (k, _) ->
                if (k.get() == key) {
                    mMap?.remove(k)
                }
            }
        }

        /**
         * 清除key的实际值（MockThreadLocal）已被GC回收的数据，防止内存泄漏
         * NOTE：当最后一次MockThreadLocal使用完后，一个好的习惯是主动调用remove方法移除绑定的数据，
         * 若不调用，那么本方法将再无机会被调用，依旧有内存泄漏的可能。
         */
        private fun expungeStaleEntry() {
            mMap?.forEach { (k, _) ->
                if (k.get() == null) {
                    mMap!!.remove(k)
                }
            }
        }

    }

}