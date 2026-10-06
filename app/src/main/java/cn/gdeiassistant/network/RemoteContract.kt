package cn.gdeiassistant.network

/** A server resource identity must come from the response, never from a local clock. */
fun requireRemoteId(value: Number?): String {
    require(value != null && value.toLong() > 0) { "Missing or invalid resource ID" }
    return value.toString()
}
