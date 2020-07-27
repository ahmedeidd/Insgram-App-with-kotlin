package com.example.instgram.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instgram.Adapter.PostAdapter
import com.example.instgram.Adapter.StoryAdapter
import com.example.instgram.Modle.Post
import com.example.instgram.Modle.Story

import com.example.instgram.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment()
{
    private var postAdapter:PostAdapter?=null
    private var postList:MutableList<Post>?=null
    private var followingList:MutableList<String>?=null

    //for story
    private var storyAdapter:StoryAdapter? = null
    private var storyList:MutableList<Story>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        var recyclerView:RecyclerView?=null
        recyclerView=view.findViewById(R.id.recyler_view_home)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager=LinearLayoutManager(context)
        linearLayoutManager.reverseLayout=true
        linearLayoutManager.stackFromEnd=true
        recyclerView.layoutManager=linearLayoutManager

        postList = ArrayList()
        postAdapter=context?.let { PostAdapter(it,postList as ArrayList<Post>) }
        recyclerView.adapter=postAdapter

        //for story rcyler view
        var recyclerViewStory:RecyclerView?=null
        recyclerViewStory=view.findViewById(R.id.recyler_view_story)
        val linearLayoutManager2 = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        recyclerViewStory.layoutManager=linearLayoutManager2

        storyList = ArrayList()
        storyAdapter = context?.let { StoryAdapter(it,storyList as ArrayList<Story>) }
        recyclerViewStory.adapter = storyAdapter



        checkFollowings()

        return view
    }

    private fun checkFollowings()
    {
        followingList=ArrayList()

        val followingRef= FirebaseDatabase.getInstance().reference
                .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Following")

        followingRef.addValueEventListener(object :ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError)
            {
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    (followingList as ArrayList<String>).clear()
                    for (snapshot in p0.children)
                    {
                        snapshot.key?.let { (followingList as ArrayList<String>).add(it) }
                    }
                    retrievePosts()
                    retrieveStories()
                }
            }

        })
    }

    private fun retrieveStories()
    {
        val storyRef = FirebaseDatabase.getInstance().reference.child("Story")
        storyRef.addValueEventListener(object :ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError)
            {
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                val timeCurrent = System.currentTimeMillis()
                (storyList as ArrayList<Story>).clear()
                (storyList as ArrayList<Story>).add(Story("",0,0,"",FirebaseAuth.getInstance().currentUser!!.uid))

                for (id in followingList!!)
                {
                    var currentstory = 0
                    var story:Story? = null
                    for (snapshot in p0.child(id).children)
                    {
                        story = snapshot.getValue(Story::class.java)
                        if (timeCurrent > story!!.getTimestart() && timeCurrent < story.getTimeend())
                        {
                            currentstory++
                        }
                    }
                    if (currentstory > 0)
                    {
                        (storyList as ArrayList<Story>).add(story!!)
                    }
                }
                storyAdapter!!.notifyDataSetChanged()
            }

        })
    }

    private fun retrievePosts()
    {
        val postsRef=FirebaseDatabase.getInstance().reference.child("Posts")
        postsRef.addValueEventListener(object :ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError)
            {
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                postList?.clear()
                for (snapshot in p0.children)
                {
                    val post=snapshot.getValue(Post::class.java)
                    for (id in (followingList as ArrayList<String>))
                    {
                        if (post!!.getPublisher() == id)
                        {
                            postList!!.add(post)
                        }
                        postAdapter!!.notifyDataSetChanged()
                    }
                }
            }

        })
    }

}
