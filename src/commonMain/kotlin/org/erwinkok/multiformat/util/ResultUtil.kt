package org.erwinkok.multiformat.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline infix fun <V, U> Result<V>.flatMap(transform: (V) -> Result<U>): Result<U> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }

    return when {
        this.isSuccess -> transform(this.getOrThrow())
        else -> Result.failure(this.exceptionOrNull()!!)
    }
}

fun <V> Result<V>.expectNoErrors(): V = when {
    isSuccess -> getOrThrow()
    else -> error("No Errors expected, but got: ${exceptionOrNull()?.message}")
}

