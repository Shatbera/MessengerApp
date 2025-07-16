package ge.tshatberashvili_ngogokhia.messengerapp.conversation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ge.tshatberashvili_ngogokhia.messengerapp.Event
import ge.tshatberashvili_ngogokhia.messengerapp.data.repo.ConversationRepository
import ge.tshatberashvili_ngogokhia.messengerapp.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ConversationViewModel : ViewModel() {

    private var activeJob: Job? = null
    private val _messages = MutableLiveData<List<Message>>()
    val messages = _messages as LiveData<List<Message>>

    private val _error = MutableLiveData<Event<Boolean>>()
    val error = _error as LiveData<Event<Boolean>>

    private val _user = MutableLiveData<User>()
    val user = _user as LiveData<User>

    fun startConversation(toUserId: String) {
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            val messageList = mutableListOf<Message>()

            val user = getUserFromFirebase(toUserId)
            if (user == null) {
                _error.postValue(Event(true))
                return@launch
            }

            _user.postValue((user))

            ConversationRepository.getMessageFlow(toUserId).collect() {
                messageList.add(it)
                _messages.postValue(messageList)
            }
        }
    }

    private suspend fun getUserFromFirebase(userId: String): User? {
        return try {
            val snapshot = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .get()
                .await()

            if (snapshot.exists()) {
                val displayName = snapshot.child("displayName").getValue(String::class.java) ?: ""
                val occupation = snapshot.child("occupation").getValue(String::class.java) ?: ""

                User(
                    userId = userId,
                    nickname = displayName,
                    profession = occupation
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun addMessage(toUserId: String, message: Message) {
        viewModelScope.launch {
            _error.postValue(Event(!ConversationRepository.addMessage(toUserId, message.apply {
                this.timestamp = System.currentTimeMillis()
            })))
        }
    }
}