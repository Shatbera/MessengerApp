package ge.tshatberashvili_ngogokhia.messengerapp.conversation

import com.google.firebase.database.Exclude

data class Message(
    var authorId: String? = null,
    val message: String? = null,
    var timestamp: Long? = null,

    @Exclude
    var isSentByMe: Boolean? = null
)