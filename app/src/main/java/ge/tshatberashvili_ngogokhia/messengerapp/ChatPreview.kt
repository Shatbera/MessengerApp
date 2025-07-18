import ge.tshatberashvili_ngogokhia.messengerapp.User

data class ChatPreview(
    var user: User = User(),
    var lastMessage: String = "",
    var lastMessageTimestamp: Long = 0L
)
