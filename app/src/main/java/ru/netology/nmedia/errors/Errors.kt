package ru.netology.nmedia.errors

import java.io.IOException
import java.sql.SQLException

sealed class AppError(var code: String) : RuntimeException() {
    companion object {
        fun from(e: Throwable): AppError = when (e) {
            is AppError -> e
            is SQLException -> DbException
            is IOException -> NetworkException
            else -> UnknownException
        }
    }
}

class ApiException(val status: Int, code: String) : AppError(code)
object NetworkException : AppError("error_network")
object DbException : AppError("error_db")
object UnknownException : AppError("error_unknown")