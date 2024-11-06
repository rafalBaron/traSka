package com.TraSka

import com.google.firebase.database.IgnoreExtraProperties
import java.sql.Time
import java.time.LocalDateTime

@IgnoreExtraProperties
data class User(
    var userData: UserData? = null,
    var savedRoutes: List<Route>? = emptyList()
) {}