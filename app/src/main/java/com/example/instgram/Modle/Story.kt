package com.example.instgram.Modle

class Story
{
    private var imageurl: String = ""
    private var timestart: Long = 0
    private var timeend: Long = 0
    private var storyid: String = ""
    private var userid: String = ""

    constructor()
    constructor(imageurl: String, timestart: Long, timeend: Long, storyid: String, userid: String)
    {
        this.imageurl = imageurl
        this.timestart = timestart
        this.timeend = timeend
        this.storyid = storyid
        this.userid = userid
    }

    fun getImageurl():String
    {
        return imageurl
    }
    fun setImageurl(imageurl: String)
    {
        this.imageurl = imageurl
    }

    fun getTimestart():Long
    {
        return timestart
    }
    fun setTimestart(timestart: Long)
    {
        this.timestart = timestart
    }


    fun getTimeend():Long
    {
        return timeend
    }
    fun setTimeend(timeend: Long)
    {
        this.timeend = timeend
    }

    fun getStoryid():String
    {
        return storyid
    }
    fun setStoryid(storyid: String)
    {
        this.storyid = storyid
    }

    fun getUserid():String
    {
        return userid
    }
    fun setUserid(userid: String)
    {
        this.userid = userid
    }



}