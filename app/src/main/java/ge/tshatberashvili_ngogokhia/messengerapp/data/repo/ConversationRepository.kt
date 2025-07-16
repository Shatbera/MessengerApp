package ge.tshatberashvili_ngogokhia.messengerapp.data.repo

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ge.tshatberashvili_ngogokhia.messengerapp.conversation.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.suspendCoroutine

object ConversationRepository {

    private const val TAG = "ConversationRepository"

    fun getMessageFlow(toUser: String): Flow<Message> = callbackFlow {
        val reference = Firebase.database.reference.child("messages").child(conversationId(toUser))
        val listener = reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue<Message>()
                Log.d(TAG, "onChildAdded: $message")
                message?.let {
                    message.isSentByMe =
                        message.authorId == FirebaseAuth.getInstance().currentUser!!.uid
                    trySend(message)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose() {
            reference.removeEventListener(listener)
        }
    }


    suspend fun addMessage(toUser: String, message: Message): Boolean {
        val convId = conversationId(toUser)
        val myId = FirebaseAuth.getInstance().currentUser!!.uid
        message.authorId = myId
        val key =
            Firebase.database.reference.child("messages").child(convId).push().key ?: return false

        val childUpdates = hashMapOf<String, Any>(
            "/messages/$convId/$key" to message,
            "/user-messages/$myId/$toUser" to message,
            "/user-messages/$toUser/$myId" to message
        )

        return suspendCoroutine {
            Firebase.database.reference.updateChildren(childUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    it.resumeWith(Result.success(true))
                } else {
                    Log.e(TAG, "addMessage: ", task.exception)
                    it.resumeWith(Result.success(false))
                }
            }
        }
    }

    private fun conversationId(toUser: String): String {
        val fromUser = FirebaseAuth.getInstance().currentUser!!.uid
        return if (fromUser < toUser) {
            "$fromUser-$toUser"
        } else {
            "$toUser-$fromUser"
        }
    }

}