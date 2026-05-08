package com.guxiaopan.common

/**
 * 网络异常
 */
class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * 数据解析异常
 */
class ParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * 授权异常
 */
class AuthException(message: String) : Exception(message)

/**
 * 数据库异常
 */
class DatabaseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * 业务逻辑异常
 */
class BusinessException(message: String) : Exception(message)