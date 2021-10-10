package com.kdonghee.noteappv2.thread

import android.os.Handler
import android.os.Looper
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

object ThreadPoolManager {

    private const val CORE_POOL_SIZE = 2
    private const val KEEP_ALIVE_TIME_IN_SEC = 60L

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private val threadNum = AtomicInteger(1)

    private val executor by lazy {
        ThreadPoolExecutor(
            CORE_POOL_SIZE,
            Int.MAX_VALUE,
            KEEP_ALIVE_TIME_IN_SEC,
            TimeUnit.SECONDS,
            SynchronousQueue(),
            ThreadFactory {
                val thread = Executors.defaultThreadFactory().newThread(it)
                thread.name = "note_shared-" + threadNum.incrementAndGet()
                return@ThreadFactory thread
            })
    }

    fun execute(runnable: Runnable) {
        executor.execute(runnable)
    }

    fun submit(runnable: Runnable): Future<*> {
        return executor.submit(runnable)
    }

    fun submitOnMainThread(runnable: Runnable) {
        mainHandler.post(runnable)
    }

    fun scheduleOnMainThread(runnable: Runnable, delay: Long) {
        mainHandler.postDelayed(runnable, delay)
    }
}