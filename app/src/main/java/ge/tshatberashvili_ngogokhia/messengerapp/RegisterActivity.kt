package ge.tshatberashvili_ngogokhia.messengerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ge.tshatberashvili_ngogokhia.messengerapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.FirebaseApp



class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()


        binding.btnRegister.setOnClickListener {
            val nickname = binding.etNickname.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val profession = binding.etProfession.text.toString().trim()

            if (nickname.isEmpty() || password.isEmpty() || profession.isEmpty()) {
                Toast.makeText(this, "All fields required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = "$nickname@messengerapp.com"

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        val userMap = mapOf(
                            "displayName" to nickname,
                            "occupation" to profession
                        )
                        FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid!!)
                            .setValue(userMap)
                            .addOnCompleteListener { saveTask ->
                                if (saveTask.isSuccessful) {
                                    Toast.makeText(this, "User saved!", Toast.LENGTH_SHORT).show()

                                    val intent = Intent(this, ProfileActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Error saving user: ${saveTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }

        }
    }
}
