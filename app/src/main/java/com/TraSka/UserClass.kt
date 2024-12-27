package com.TraSka

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var userData: UserData?,
    var savedRoutes: List<Route>? = emptyList(),
    var savedVehicles: List<Vehicle>? = emptyList()
) {}