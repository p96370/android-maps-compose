package com.isi.sameway.firebase

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.isi.sameway.driver.RouteStatus
import org.lighthousegames.logging.logging
import kotlin.math.pow
import kotlin.math.sqrt

object FirebaseDatabaseHelper {
    private val database =
        FirebaseDatabase.getInstance("https://sameway-2-default-rtdb.europe-west1.firebasedatabase.app/")
    private val usersRef = database.getReference("users")
    private val log = logging()


    fun addCurrentUser() {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        // Check if the user already exists
        val userId = user.uid
        val userName = user.displayName ?: "Unknown"

        val childRef = usersRef.child(userId)


        childRef.get().addOnSuccessListener { dataSnapshot ->
            if (!dataSnapshot.exists()) {
                // Data does not exist, so add a new user
                val newUser = User(
                    id = userId, name = userName, rating = 5.0f // Default rating
                )

                // Add the new user to the database
                childRef.setValue(newUser).addOnSuccessListener {
                    log.d { "User added successfully to the database." }
                }.addOnFailureListener { e ->
                    log.e { "Failed to add user: ${e.message}" }
                }
            } else {
                log.d { "User already exists in the database." }
            }
        }.addOnFailureListener { e ->
            log.e { "Failed to check user existence: ${e.message}" }
        }
    }

    fun addRoute(route: Route) {
        getUserRef().child("route").setValue(route)
    }

    private fun getUserRef(): DatabaseReference {
        val user = FirebaseAuth.getInstance().currentUser!!
        return usersRef.child(user.uid)
    }

    fun requestAccessFromDriver(route: Route, start: LatLng, end: LatLng, requestedTime: Int, routeSegmentSize: Int) {
        val databaseRouteForClients = usersRef.child(route.userId).child("route").child("clients")
        databaseRouteForClients.get().addOnSuccessListener {
            if (it.exists()) {
                databaseRouteForClients.child(it.childrenCount.toString()).setValue(Client(
                    user = FirebaseAuth.getInstance().currentUser!!.uid,
                    start = start,
                    end = end,
                    status = "Waiting",
                    requestedTime = requestedTime,
                    routeSegmentSize = routeSegmentSize
                ))
                it.child(it.childrenCount.toString())
            } else {
                databaseRouteForClients.setValue(listOf(Client(
                    user = FirebaseAuth.getInstance().currentUser!!.uid,
                    start = start,
                    end = end,
                    status = "Waiting",
                    requestedTime = requestedTime,
                    routeSegmentSize = routeSegmentSize)))
            }

        }
    }

    fun updateAccessByDriver(user: String, newStatus: RouteStatus) {
        getUserRef().child("route").child("clients").get().addOnSuccessListener {
            it.children.forEach { clientSnapshot ->
                if (clientSnapshot.child("user").value == user) {
                 clientSnapshot.child("status").ref.setValue(newStatus.msg)
                }
            }
        }
    }

    fun updateUserRating(userId: String, newRating: Float) {
        usersRef.child(userId).child("rating").setValue(newRating)
    }

    fun updateCarPosition(position: Int) {
        getUserRef().child("route").child("carPosition").setValue(position)
    }

    fun updateRoadStarted(started: Boolean) {
        getUserRef().child("route").child("roadStarted").setValue(started)
    }

    fun updateRouteStatus(status: String) {
        getUserRef().child("route").child("status").setValue(status)
    }

    fun deleteRoute() {
        getUserRef().child("route").removeValue()
    }

    fun observeUsers(callback: (List<User>) -> Unit) {
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                callback(users)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun observeRoutes(callback: (List<Route>) -> Unit) {
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val routes = snapshot.children.mapNotNull { userSnapshot ->
                    val routeSnapshot = userSnapshot.child("route")
                    if (routeSnapshot.exists()) {
                        // Extract route data for each user
                        val userId = routeSnapshot.child("userId").getValue<String>() ?: "Unknown"
                        val coordinates = routeSnapshot.child("coordinates").children.mapNotNull {
                            it.getValue(Coordinate::class.java)
                        }
                        val startingTime =
                            routeSnapshot.child("startingTime").getValue(Int::class.java) ?: 0
                        val status =
                            routeSnapshot.child("status").getValue(String::class.java) ?: "Unknown"
                        val clients = routeSnapshot.child("clients").children.mapNotNull {
                            it.getValue(FirebaseClient::class.java)
                        }
                        val carPosition =
                            routeSnapshot.child("carPosition").getValue(Int::class.java) ?: 0
                        val roadStarted =
                            routeSnapshot.child("roadStarted").getValue(Boolean::class.java) ?: false
                        // Create a Route object
                        Route(
                            userId = userId,
                            coordinates = coordinates.map { LatLng(it.latitude, it.longitude) },
                            startingTime = startingTime,
                            status = status,
                            clients = clients.map {Client(user = it.user, start = it.start.toLatLng(), end = it.end.toLatLng(), status = it.status, requestedTime = it.requestedTime)},
                            carPosition = carPosition,
                            roadStarted = roadStarted
                        )
                    } else {
                        null
                    }
                }
                callback(routes)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun observeIncomingClients(callback: (List<Client>) -> Unit) {
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = FirebaseAuth.getInstance().currentUser!!
                val uid = user.uid
                val clients = snapshot.child(uid).child("route").child("clients").children.mapNotNull { clientSnapshot ->
                    val status = clientSnapshot.child("status").getValue(String::class.java) ?: "Unknown"
                    val userId = clientSnapshot.child("user").getValue(String::class.java) ?: "Unknown"
                    val start = clientSnapshot.child("start").getValue(Coordinate::class.java) ?: Coordinate()
                    val end = clientSnapshot.child("end").getValue(Coordinate::class.java) ?: Coordinate()
                    val requestedTime = clientSnapshot.child("requestedTime").getValue(Int::class.java) ?: 0

                    Client(user = userId, start = start.toLatLng(), end = end.toLatLng(), status = status, requestedTime = requestedTime)
                }
                callback(clients)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun signOut(context: Context) {
        AuthUI.getInstance().signOut(context)
    }

    fun delete(context: Context) {
        AuthUI.getInstance().delete(context)
    }

    fun signIn(signInLauncher: ActivityResultLauncher<Intent>) {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder()
                .setScopes(listOf("profile", "email"))
                .build(),
        )
        val signInIntent =
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build()
        signInLauncher.launch(signInIntent)
    }
}

private data class Coordinate(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val stability: Int = 0,
) {
    constructor() : this(0.0, 0.0, 0)

    fun toLatLng() = LatLng(latitude, longitude)
}

fun gaussianDistance(pointA: LatLng, pointB: LatLng): Double = sqrt(
    (pointA.latitude - pointB.latitude).pow(2) + (pointA.longitude - pointB.longitude).pow(2)
)