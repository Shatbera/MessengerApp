package ge.tshatberashvili_ngogokhia.messengerapp.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ge.tshatberashvili_ngogokhia.messengerapp.User
import ge.tshatberashvili_ngogokhia.messengerapp.data.repo.LookupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


const val PAGE_SIZE = 13
private const val TAG = "SearchViewModel"
class SearchViewModel : ViewModel() {

    private val _loadingState = MutableLiveData(false)
    val loadingState = _loadingState as LiveData<Boolean>

    suspend fun loadData(query: String? = null): List<User> = withContext(Dispatchers.IO) {
        _loadingState.postValue(true)
        val users = LookupRepository.getUsers(query, null, PAGE_SIZE)

        Log.d(TAG, "loadData: $users")
        _loadingState.postValue(false)
        return@withContext users
    }


    suspend fun loadMore(query: String?, lastResult: String): List<User> =
        withContext(Dispatchers.IO) {
            return@withContext LookupRepository.getUsers(query, lastResult, PAGE_SIZE)
        }
}