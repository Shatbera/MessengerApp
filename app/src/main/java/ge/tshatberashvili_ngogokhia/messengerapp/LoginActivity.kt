package ge.tshatberashvili_ngogokhia.messengerapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import ge.tshatberashvili_ngogokhia.messengerapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            val nickname = binding.etNickname.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (nickname.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = "$nickname@messengerapp.com"

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.btnGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

}
