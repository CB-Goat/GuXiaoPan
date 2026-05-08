package com.guxiaopan.util

import android.util.Log

/**
 * 日志工具类
 * 统一封装日志输出，便于后续扩展（如写入文件、上传等）
 */
object Logger {
    private const val TAG = "GuXiaoPan"
    private var isDebug = true

    fun setDebug(debug: Boolean) {
        isDebug = debug
    }

    fun d(message: String) {
        if (isDebug) Log.d(TAG, message)
    }

    fun d(tag: String, message: String) {
        if (isDebug) Log.d(tag, message)
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    fun w(message: String) {
        Log.w(TAG, message)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    fun e(message: String) {
        Log.e(TAG, message)
    }

    fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable) {
        Log.e(tag, message, throwable)
    }

    fun e(message: String, throwable: Throwable) {
        Log.e(TAG, message, throwable)
    }
}