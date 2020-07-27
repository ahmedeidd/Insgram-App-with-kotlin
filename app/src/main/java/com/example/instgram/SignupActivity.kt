package com.example.instgram

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.android.synthetic.main.activity_signup.*

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        signin_link_btn.setOnClickListener {
            startActivity(Intent(this,SigninActivity::class.java))
        }
        signup_btn.setOnClickListener {
            CreateAccount()
        }
    }

    private fun CreateAccount()
    {
        val fullname=fullname_signup.text.toString()
        val username=username_signup.text.toString()
        val email=email_signup.text.toString()
        val password=password_signup.text.toString()
        when
        {
            TextUtils.isEmpty(fullname)-> Toast.makeText(this,"full name is required",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(username)-> Toast.makeText(this,"user name is required",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(email)-> Toast.makeText(this,"email is required",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password)-> Toast.makeText(this,"password is required",Toast.LENGTH_LONG).show()
            else ->{
                val progressDialog=ProgressDialog(this@SignupActivity)
                progressDialog.setTitle("Sign Up")
                progressDialog.setMessage("please wait")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()
                val mAuth:FirebaseAuth=FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener {task ->
                        if (task.isSuccessful)
                        {
                            saveUserInfo(fullname,username,email,progressDialog)
                        }
                        else
                        {
                            val message=task.exception.toString()
                            Toast.makeText(this,"Error:$message",Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun saveUserInfo(fullname: String, username: String, email: String,progressDialog:ProgressDialog)
    {
        val currentUserID=FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef:DatabaseReference=FirebaseDatabase.getInstance().reference.child("Users")
        val userMap=HashMap<String,Any>()
        userMap["uid"]=currentUserID
        userMap["fullname"]=fullname.toLowerCase()
        userMap["username"]=username.toLowerCase()
        userMap["email"]=email
        userMap["bio"]="hey I am using Instgram EID"
        userMap["image"]="https://firebasestorage.googleapis.com/v0/b/instgram-eid.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=ff455b0b-2e2c-4ec3-8250-985e74c12a1a"
        usersRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener {task ->
                if(task.isSuccessful)
                {
                    progressDialog.dismiss()
                    Toast.makeText(this,"Account has been created",Toast.LENGTH_LONG).show()

                    //
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserID)
                        .child("Following").child(currentUserID)
                        .setValue(true)
                    //

                    val intent=Intent(this@SignupActivity,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    val message=task.exception.toString()
                    Toast.makeText(this,"Error:$message",Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
    }
}
