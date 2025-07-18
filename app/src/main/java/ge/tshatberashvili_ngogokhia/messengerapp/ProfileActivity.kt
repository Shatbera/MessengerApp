package ge.tshatberashvili_ngogokhia.messengerapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import ge.tshatberashvili_ngogokhia.messengerapp.databinding.ActivityProfileBinding
import ge.tshatberashvili_ngogokhia.messengerapp.search.SearchActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        loadUserProfile()

        binding.btnSave.setOnClickListener {
            saveProfileChanges()
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }

        binding.ivProfile.setOnClickListener {
            openGallery()
        }

        binding.btnSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        val navHome = findViewById<ImageView>(R.id.nav_home)

        navHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.root.findViewById<ImageView>(R.id.nav_add).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid

        if (uid != null) {
            database.getReference("users")
                .child(uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val nickname = snapshot.child("nickname").value as? String
                    val profession = snapshot.child("profession").value as? String
                    val photoUrl = snapshot.child("photoUrl").value as? String

                    binding.etNickname.setText(nickname ?: "")
                    binding.etProfession.setText(profession ?: "")

                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(photoUrl)
                            .circleCrop()
                            .into(binding.ivProfile)
                    } else {
                        Glide.with(this)
                            .load(R.drawable.avatar_image_placeholder)
                            .circleCrop()
                            .into(binding.ivProfile)
                    }
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Error loading profile: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfileChanges() {
        val nickname = binding.etNickname.text.toString().trim()
        val profession = binding.etProfession.text.toString().trim()

        if (nickname.isEmpty() || profession.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = auth.currentUser?.uid

        if (uid != null) {
            val userMap = mapOf(
                "nickname" to nickname,
                "profession" to profession
            )

            database.getReference("users")
                .child(uid)
                .updateChildren(userMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error updating: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun logoutUser() {
        auth.signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data

            if (imageUri != null) {
                Glide.with(this)
                    .load(imageUri)
                    .circleCrop()
                    .into(binding.ivProfile)

                Log.d("ProfileActivity", "Picked URI: $imageUri")

                uploadImageToFirebase(imageUri)
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val storageRef = FirebaseStorage.getInstance()
                .getReference("profile_images/$uid.jpg")

            storageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Correct way: get download URL from taskSnapshot
                    taskSnapshot.storage.downloadUrl
                        .addOnSuccessListener { url ->
                            savePhotoUrlToDatabase(url.toString())
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this, "Failed to get download URL: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Upload failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun savePhotoUrlToDatabase(photoUrl: String) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            database.getReference("users")
                .child(uid)
                .child("photoUrl")
                .setValue(photoUrl)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile photo updated!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Failed to save photo URL: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 1001
    }
}
