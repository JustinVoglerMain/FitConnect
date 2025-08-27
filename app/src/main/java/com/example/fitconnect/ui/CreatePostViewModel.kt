package com.example.fitconnect.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreatePostViewModel : ViewModel() {
    private val _postContent = MutableLiveData<String>()
    val postContent: LiveData<String> get() = _postContent

    fun setPostContent(content: String) {
        _postContent.value = content
    }

}