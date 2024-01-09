package org.erwinkok

fun expectError(message: String, executable: () -> Result<Any>) {
    val result = executable()
    when {
        result.isSuccess -> error("Code ran successfully, but expected exception")
        result.isFailure -> check(message == result.exceptionOrNull()?.message) { "Error message '${result.exceptionOrNull()?.message}' was not equal to '$message'" }
    }
}
suspend fun suspendExpectError(message: String, executable: suspend () -> Result<Any>) {
    val result = executable()
    when {
        result.isSuccess -> error("Code ran successfully, but expected exception")
        result.isFailure -> check(message == result.exceptionOrNull()?.message) { "Error message '${result.exceptionOrNull()?.message}' was not equal to expected '$message'" }
    }
}
