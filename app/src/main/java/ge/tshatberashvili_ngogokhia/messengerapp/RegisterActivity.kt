package ge.tshatberashvili_ngogokhia.messengerapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ge.tshatberashvili_ngogokhia.messengerapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val nickname = binding.etNickname.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val profession = binding.etProfession.text.toString().trim()

            if (nickname.isEmpty() || password.isEmpty() || profession.isEmpty()) {
                Toast.makeText(this, "All fields required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: Call Firebase registration logic here
        }
    }
}
