package seepick.localsportsclub.service

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

// https://latlongdata.com/distance-calculator/
data class Location(
    val latitude: Double, // somewhere around 50 north
    val longitude: Double, // somewhere around 4 east
)

fun round(d: Double, floatingPoints: Int): Double {
    val x = 10.0.pow(floatingPoints.toDouble())
    return (d * x).roundToInt() / x
}

fun distance(point1: Location, point2: Location): Double {
    val theta = point1.longitude - point2.longitude
    var distance = sin(deg2rad(point1.latitude)) *
            sin(deg2rad(point2.latitude)) +
            cos(deg2rad(point1.latitude)) *
            cos(deg2rad(point2.latitude)) *
            cos(deg2rad(theta))
    distance = acos(distance)
    distance = rad2deg(distance)
    distance *= 60 * 1.1515
    distance *= 1.609344 // km translation
    return distance
}

private fun deg2rad(deg: Double): Double = deg * Math.PI / 180.0
private fun rad2deg(rad: Double): Double = rad * 180.0 / Math.PI
