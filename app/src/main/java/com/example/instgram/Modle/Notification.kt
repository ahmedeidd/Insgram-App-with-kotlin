package com.example.instgram.Modle

class Notification
{
    private var userid:String = ""
    private var text:String = ""
    private var postid:String = ""
    private var ispost = false

    constructor()
    constructor(userid: String, text: String, postid: String, ispost: Boolean) {
        this.userid = userid
        this.text = text
        this.postid = postid
        this.ispost = ispost
    }

    fun getUserid():String
    {
        return userid
    }
    fun setUserid(userid: String)
    {
        this.userid = userid
    }


    fun getText():String
    {
        return text
    }
    fun setText(text: String)
    {
        this.text = text
    }

    fun getPostid():String
    {
        return postid
    }
    fun setPostid(postid: String)
    {
        this.postid = postid
    }

    fun isIspost():Boolean
    {
        return ispost
    }
    fun setIspost(ispost: Boolean)
    {
        this.ispost = ispost
    }

}