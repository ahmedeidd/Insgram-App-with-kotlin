package com.example.instgram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instgram.Adapter.CommentAdapter
import com.example.instgram.Modle.Comment
import com.example.instgram.Modle.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_comments.*

class CommentsActivity : AppCompatActivity()
{
    private var postId = ""
    private var publisherId = ""
    private var firebaseUser:FirebaseUser? = null
    private var commentAdapter:CommentAdapter? = null
    private var commentList:MutableList<Comment>?=null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        val intent= intent
        postId=intent.getStringExtra("postId")
        publisherId=intent.getStringExtra("publisherId")

        firebaseUser=FirebaseAuth.getInstance().currentUser

        var recyclerView:RecyclerView
        recyclerView=findViewById(R.id.recycler_view_comments)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager

        commentList=ArrayList()
        commentAdapter= CommentAdapter(this,commentList)
        recyclerView.adapter=commentAdapter

        userInfo()
        readcomments()
        getPostImage()

        post_comment.setOnClickListener {
            if (add_comment.text.toString() == "")
            {
                Toast.makeText(this@CommentsActivity,"write comment",Toast.LENGTH_LONG).show()
            }
            else
            {
                addComment()
            }
        }
    }

    private fun addComment()
    {
        val commentsRef= FirebaseDatabase.getInstance().reference
            .child("Comments").child(postId)

        val commentMap=HashMap<String,Any>()
        commentMap["comment"] = add_comment.text.toString()
        commentMap["publisher"] = firebaseUser!!.uid

        commentsRef.push().setValue(commentMap)

        addNotification()

        add_comment.text.clear()

    }

    private fun userInfo()
    {
        val usersRef= FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        usersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    val user=p0.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_image_comment)

                }
            }

        })
    }

    private fun getPostImage()
    {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
            .child(postId).child("postimage")
        postRef.addValueEventListener(object : ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    val image = p0.value.toString()
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(post_image_comment)
                }
            }

        })
    }

    private fun readcomments()
    {
        val commentsRef= FirebaseDatabase.getInstance().reference
            .child("Comments").child(postId)

        commentsRef.addValueEventListener(object:ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    commentList!!.clear()
                    for (snapshot in p0.children)
                    {
                        val comment=snapshot.getValue(Comment::class.java)
                        commentList!!.add(comment!!)
                    }
                    commentAdapter!!.notifyDataSetChanged()
                }
            }
        })
    }

    private fun addNotification()
    {
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(publisherId)

        val notiMap = HashMap<String,Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "commented: " + add_comment.text.toString()
        notiMap["postid"] = postId
        notiMap["ispost"] = true

        notiRef.push().setValue(notiMap)
    }
}
