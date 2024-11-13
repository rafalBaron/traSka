package com.TraSka

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var userData: UserData? = null,
    var savedRoutes: List<Route>? = emptyList()
) {}