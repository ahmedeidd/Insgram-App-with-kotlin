package com.example.instgram

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_add_post.*

class AddStoryActivity : AppCompatActivity()
{
    private var myUrl=""
    private var imageUri: Uri?=null
    private var storageStoryPicRef: StorageReference?=null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        storageStoryPicRef= FirebaseStorage.getInstance().reference.child("Story Pictures")

        CropImage.activity()
            .setAspectRatio(9,16)
            .start(this@AddStoryActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null)
        {
            val result=CropImage.getActivityResult(data)
            imageUri=result.uri

            uploadStory()
        }
    }

    private fun uploadStory()
    {
        when
        {
            imageUri == null -> Toast.makeText(this,"select image", Toast.LENGTH_LONG).show()
            else -> {
                var progressDialog= ProgressDialog(this)
                progressDialog.setTitle("Adding story")
                progressDialog.setMessage("please wait , we are adding your story")
                progressDialog.show()

                val fileRef=storageStoryPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")

                var uploadTask: StorageTask<*>
                uploadTask=fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation<com.google.firebase.storage.UploadTask.TaskSnapshot, com.google.android.gms.tasks.Task<android.net.Uri>> { task ->
                    if (!task.isSuccessful)
                    {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener (OnCompleteListener<Uri>{ task ->
                    if (task.isSuccessful)
                    {
                        val downloadUrl=task.result
                        myUrl=downloadUrl.toString()

                        val ref= FirebaseDatabase.getInstance().reference.child("Story")
                            .child(FirebaseAuth.getInstance().currentUser!!.uid)

                        val storyId=(ref.push().key).toString()

                        val timeEnd = System.currentTimeMillis() + 86400000 //one day

                        val storytMap = HashMap<String,Any>()
                        storytMap["userid"]=FirebaseAuth.getInstance().currentUser!!.uid
                        storytMap["timestart"]=ServerValue.TIMESTAMP
                        storytMap["timeend"]= timeEnd
                        storytMap["imageurl"]=myUrl
                        storytMap["storyid"]=storyId

                        ref.child(storyId).updateChildren(storytMap)

                        Toast.makeText(this,"Story uploaded successfully", Toast.LENGTH_LONG).show()

                        val intent=Intent(this@AddStoryActivity,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    }
                    else
                    {
                        progressDialog.dismiss()
                    }
                } )
            }
        }
    }
}
