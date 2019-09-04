package me.erwa.sourceset.framework.threadlocal

/**
 * @author: drawf
 * @date: 2019-09-03
 * @see: <a href=""></a>
 * @description:
 */
class MockThread(target: Runnable, name: String) : Thread(target, name) {

    //用于保存绑定到线上的数据
    var threadLocals: MockThreadLocal.ThreadLocalMap? = null

}