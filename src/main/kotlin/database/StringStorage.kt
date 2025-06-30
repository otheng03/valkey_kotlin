package valkey.kotlin.database

// TODO:
//  Implement expiration mechanisms
//  Implement flags
fun setGenericCommand(key: String, value: String) {
    val existingValue = lookupKeyWrite(key)
}

fun getGenericCommand() {

}