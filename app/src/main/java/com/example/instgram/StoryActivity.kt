package com.example.instgram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.instgram.Adapter.StoryAdapter
import com.example.instgram.Modle.Story
import com.example.instgram.Modle.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import jp.shts.android.storiesprogressview.StoriesProgressView
import kotlinx.android.synthetic.main.activity_story.*

class StoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {
    var currentUserId: String = ""
    var userId: String = ""
    var counter = 0

    var pressTime = 0L
    var limit = 500L

    var imagesList:List<String>? = null
    var storyIdsList:List<String>? = null

    var storiesProgressView: StoriesProgressView? = null

    private val onTouchListener = View.OnTouchListener { view, motionEvent ->
        when(motionEvent.action)
        {
            MotionEvent.ACTION_DOWN ->
            {
                pressTime = System.currentTimeMillis()
                storiesProgressView!!.pause()
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP ->
            {
                val now = System.currentTimeMillis()
                storiesProgressView!!.resume()
                return@OnTouchListener limit < now - pressTime
            }
        }
        false
    }


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        userId = intent.getStringExtra("userid")

        story_delete.visibility = View.GONE
        layout_seen.visibility = View.GONE
        if (userId == currentUserId)
        {
            story_delete.visibility = View.VISIBLE
            layout_seen.visibility = View.VISIBLE
        }

        getStories(userId)
        userInfo(userId)

        storiesProgressView = findViewById(R.id.stories_progress)

        val reverse: View = findViewById(R.id.reverse)
        reverse.setOnClickListener { storiesProgressView!!.reverse() }
        reverse.setOnTouchListener(onTouchListener)

        val skip: View = findViewById(R.id.skip)
        skip.setOnClickListener { storiesProgressView!!.skip() }
        skip.setOnTouchListener(onTouchListener)

        //seen number of user btn
        seen_number.setOnClickListener {
            val intent = Intent(this@StoryActivity,ShowUsersActivity::class.java)
            intent.putExtra("id",userId)
            intent.putExtra("storyid",storyIdsList!![counter])
            intent.putExtra("title","views")
            startActivity(intent)
        }

        //delete btn
        story_delete.setOnClickListener {
            val ref= FirebaseDatabase.getInstance().reference
                .child("Story").child(userId)
                .child(storyIdsList!![counter])

            ref.removeValue().addOnCompleteListener { task ->
                if(task.isSuccessful)
                {
                    Toast.makeText(this@StoryActivity,"Deleted",Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun getStories(storyId: String)
    {
        imagesList = ArrayList()
        storyIdsList = ArrayList()
        val ref= FirebaseDatabase.getInstance().reference
            .child("Story").child(userId)

        ref.addValueEventListener(object: ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                (imagesList as ArrayList<String>).clear()
                (storyIdsList as ArrayList<String>).clear()

                for (snapshot in p0.children)
                {
                    val story: Story? = snapshot.getValue<Story>(Story::class.java)
                    val currentTime = System.currentTimeMillis()

                    if (currentTime > story!!.getTimestart() && currentTime < story.getTimeend())
                    {
                        (imagesList as ArrayList<String>).add(story.getImageurl())
                        (storyIdsList as ArrayList<String>).add(story.getStoryid())
                    }

                }
                storiesProgressView!!.setStoriesCount((imagesList as ArrayList<String>).size)
                storiesProgressView!!.setStoryDuration(6000L)
                storiesProgressView!!.setStoriesListener(this@StoryActivity)
                storiesProgressView!!.startStories(counter)
                Picasso.get().load(imagesList!!.get(counter)).placeholder(R.drawable.profile).into(image_story)

                addViewToStory(storyIdsList!!.get(counter))
                seenNumber(storyIdsList!!.get(counter))
            }

        })

    }

    private fun addViewToStory(storyId: String)
    {
        val ref= FirebaseDatabase.getInstance().reference
            .child("Story").child(userId).child(storyId)
            .child("views").child(currentUserId).setValue(true)
    }

    private fun seenNumber(storyId: String)
    {
        val ref= FirebaseDatabase.getInstance().reference
            .child("Story").child(userId).child(storyId).child("views")

        ref.addValueEventListener(object: ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                seen_number.text ="" + p0.childrenCount
            }

        })
    }

    private fun userInfo(userId: String)
    {
        val usersRef= FirebaseDatabase.getInstance().getReference().child("Users").child(userId)
        usersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    val user=p0.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(story_profile_image)
                    story_username.text = user.getUsername()

                }
            }

        })
    }

    override fun onComplete()
    {
        finish()
    }

    override fun onPrev()
    {
        if (counter - 1 < 0)return
        Picasso.get().load(imagesList!![--counter]).placeholder(R.drawable.profile).into(image_story)
        seenNumber(storyIdsList!![counter])
    }

    override fun onNext()
    {
        Picasso.get().load(imagesList!![++counter]).placeholder(R.drawable.profile).into(image_story)
        addViewToStory(storyIdsList!![counter])
        seenNumber(storyIdsList!![counter])
    }

    override fun onDestroy()
    {
        super.onDestroy()
        storiesProgressView!!.destroy()
    }

    override fun onResume()
    {
        super.onResume()
        storiesProgressView!!.resume()
    }

    override fun onPause()
    {
        super.onPause()
        storiesProgressView!!.pause()
    }

}
