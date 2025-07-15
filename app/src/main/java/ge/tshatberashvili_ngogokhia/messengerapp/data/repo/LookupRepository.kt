package ge.tshatberashvili_ngogokhia.messengerapp.data.repo

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ge.tshatberashvili_ngogokhia.messengerapp.User
import kotlin.coroutines.suspendCoroutine

object LookupRepository {

    private const val TAG = "LookupRepository"

    suspend fun getUsers(
        queryString: String? = null, lastResult: String? = null, limit: Int = 10
    ): List<User> = suspendCoroutine {
        Firebase.database.reference.child("users")
            .get()
            .addOnCompleteListener() { task ->
                val users = mutableListOf<User>()
                if (task.isSuccessful) {
                    Log.d(TAG, "Query successful, children count: ${task.result?.childrenCount}")

                    val allUsers = mutableListOf<User>()
                    for (user in task.result!!.children) {
                        Log.d(TAG, "Processing user: ${user.key} - ${user.value}")
                        val item = user.getValue(User::class.java)
                        if (item != null) {
                            item.userId = user.key!!
                            allUsers.add(item)
                        } else {
                            Log.e(TAG, "Failed to parse user: ${user.value}")
                        }
                    }

                    // could do this using firebase, might change later
                    var filteredUsers = allUsers
                    if (queryString != null && queryString.isNotEmpty()) {
                        filteredUsers = allUsers.filter { user ->
                            user.nickname?.lowercase()?.contains(queryString.lowercase()) == true
                        }.toMutableList()
                        Log.d(TAG, "Filtered to ${filteredUsers.size} users matching '$queryString'")
                    }

                    filteredUsers.sortBy { it.nickname?.lowercase() }

                    if (lastResult != null) {
                        val startIndex = filteredUsers.indexOfFirst { it.nickname == lastResult } + 1
                        if (startIndex > 0 && startIndex < filteredUsers.size) {
                            filteredUsers = filteredUsers.drop(startIndex).toMutableList()
                        } else {
                            filteredUsers = mutableListOf() // No more results
                        }
                    }

                    if (filteredUsers.size > limit) {
                        filteredUsers = filteredUsers.take(limit).toMutableList()
                    }

                    users.addAll(filteredUsers)
                    users.forEach { Log.d(TAG, "Added user: ${it.nickname}") }

                } else {
                    Log.e(TAG, "Error getting data", task.exception)
                }
                Log.d(TAG, "Returning ${users.size} users")
                it.resumeWith(Result.success(users))
            }
    }
}