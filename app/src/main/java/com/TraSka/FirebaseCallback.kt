package com.TraSka

interface FirebaseCallback {
    public abstract fun onResponse(user: User?)
}

open class myCallback : FirebaseCallback {
    override fun onResponse(user: User?) {
        TODO("Not yet implemented")
    }

}