package data

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

inline fun <E> Result<E>.unpackOr(fallback: (exception: Exception) -> E): E = when (this) {
    is Result.Error -> fallback(exception)
    is Result.Success -> data
}