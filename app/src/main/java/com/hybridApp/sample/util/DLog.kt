package com.hybridApp.sample.util

import android.util.Log
import com.hybridApp.sample.BuildConfig

class DLog {
    companion object {
        private const val TAG = "belleforet"

        fun e(tr: Throwable) {
            Log.e(TAG, Log.getStackTraceString(tr))
        }

        fun e(tag: String, msg: String) {
            printLog(Log.ERROR, tag, msg)
        }

        fun e(msg: String) {
            e(TAG, msg)
        }

        fun w(tag: String, msg: String) {
            printLog(Log.WARN, tag, msg)
        }

        fun w(msg: String) {
            w(TAG, msg)
        }

        fun i(tag: String, msg: String) {
            printLog(Log.INFO, tag, msg)
        }

        fun i(msg: String) {
            i(TAG, msg)
        }

        fun d(tag: String, msg: String) {
            printLog(Log.DEBUG, tag, msg)
        }

        fun d(msg: String) {
            d(TAG, msg)
        }

        private fun printLog(level: Int, tag: String, msg: String) {
            if (BuildConfig.DEBUG.not()) {
                return
            }

            val logMsg = buildLogMsg(msg)
            when (level) {
                Log.ASSERT -> {}
                Log.ERROR -> {
                    Log.e(tag, logMsg)
                }
                Log.WARN -> {
                    Log.w(tag, logMsg)
                }
                Log.INFO -> {
                    Log.i(tag, logMsg)
                }
                Log.DEBUG -> {
                    Log.d(tag, logMsg)
                }
                Log.VERBOSE -> {
                    Log.v(tag, logMsg)
                }
                else -> {}
            }
        }

        private fun buildLogMsg(msg: String): String {
            val ste = Thread.currentThread().stackTrace[6]
            val sb = StringBuilder()
            sb.apply {
                append("[")
                append(ste.fileName.replace(".kt", ""))
                append("::")
                append(ste.methodName)
                append(":")
                append(ste.lineNumber)
                append("] ")
                append(msg)
            }
            return sb.toString()
        }

    }
}