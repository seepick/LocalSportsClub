/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package seepick.localsportsclub.view.common
// package androidx.compose.material.icons.filled


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.Filled.Visibility: ImageVector
    get() {
        if (_visibility != null) {
            return _visibility!!
        }
        _visibility = materialIcon(name = "Filled.Visibility") {
            materialPath {
                moveTo(12.0f, 4.5f)
                curveTo(7.0f, 4.5f, 2.73f, 7.61f, 1.0f, 12.0f)
                curveToRelative(1.73f, 4.39f, 6.0f, 7.5f, 11.0f, 7.5f)
                reflectiveCurveToRelative(9.27f, -3.11f, 11.0f, -7.5f)
                curveToRelative(-1.73f, -4.39f, -6.0f, -7.5f, -11.0f, -7.5f)
                close()
                moveTo(12.0f, 17.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
                reflectiveCurveToRelative(2.24f, -5.0f, 5.0f, -5.0f)
                reflectiveCurveToRelative(5.0f, 2.24f, 5.0f, 5.0f)
                reflectiveCurveToRelative(-2.24f, 5.0f, -5.0f, 5.0f)
                close()
                moveTo(12.0f, 9.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
                reflectiveCurveToRelative(-1.34f, -3.0f, -3.0f, -3.0f)
                close()
            }
        }
        return _visibility!!
    }

private var _visibility: ImageVector? = null

val Icons.Filled.VisibilityOff: ImageVector
    get() {
        if (_visibilityOff != null) {
            return _visibilityOff!!
        }
        _visibilityOff = materialIcon(name = "Filled.VisibilityOff") {
            materialPath {
                moveTo(12.0f, 7.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, 2.24f, 5.0f, 5.0f)
                curveToRelative(0.0f, 0.65f, -0.13f, 1.26f, -0.36f, 1.83f)
                lineToRelative(2.92f, 2.92f)
                curveToRelative(1.51f, -1.26f, 2.7f, -2.89f, 3.43f, -4.75f)
                curveToRelative(-1.73f, -4.39f, -6.0f, -7.5f, -11.0f, -7.5f)
                curveToRelative(-1.4f, 0.0f, -2.74f, 0.25f, -3.98f, 0.7f)
                lineToRelative(2.16f, 2.16f)
                curveTo(10.74f, 7.13f, 11.35f, 7.0f, 12.0f, 7.0f)
                close()
                moveTo(2.0f, 4.27f)
                lineToRelative(2.28f, 2.28f)
                lineToRelative(0.46f, 0.46f)
                curveTo(3.08f, 8.3f, 1.78f, 10.02f, 1.0f, 12.0f)
                curveToRelative(1.73f, 4.39f, 6.0f, 7.5f, 11.0f, 7.5f)
                curveToRelative(1.55f, 0.0f, 3.03f, -0.3f, 4.38f, -0.84f)
                lineToRelative(0.42f, 0.42f)
                lineTo(19.73f, 22.0f)
                lineTo(21.0f, 20.73f)
                lineTo(3.27f, 3.0f)
                lineTo(2.0f, 4.27f)
                close()
                moveTo(7.53f, 9.8f)
                lineToRelative(1.55f, 1.55f)
                curveToRelative(-0.05f, 0.21f, -0.08f, 0.43f, -0.08f, 0.65f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                curveToRelative(0.22f, 0.0f, 0.44f, -0.03f, 0.65f, -0.08f)
                lineToRelative(1.55f, 1.55f)
                curveToRelative(-0.67f, 0.33f, -1.41f, 0.53f, -2.2f, 0.53f)
                curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
                curveToRelative(0.0f, -0.79f, 0.2f, -1.53f, 0.53f, -2.2f)
                close()
                moveTo(11.84f, 9.02f)
                lineToRelative(3.15f, 3.15f)
                lineToRelative(0.02f, -0.16f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                lineToRelative(-0.17f, 0.01f)
                close()
            }
        }
        return _visibilityOff!!
    }

private var _visibilityOff: ImageVector? = null


public val Icons.Filled.Description: ImageVector
    get() {
        if (_description != null) {
            return _description!!
        }
        _description = materialIcon(name = "Filled.Description") {
            materialPath {
                moveTo(14.0f, 2.0f)
                lineTo(6.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(4.0f, 20.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 1.99f, 2.0f)
                lineTo(18.0f, 22.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(20.0f, 8.0f)
                lineToRelative(-6.0f, -6.0f)
                close()
                moveTo(16.0f, 18.0f)
                lineTo(8.0f, 18.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(16.0f, 14.0f)
                lineTo(8.0f, 14.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(13.0f, 9.0f)
                lineTo(13.0f, 3.5f)
                lineTo(18.5f, 9.0f)
                lineTo(13.0f, 9.0f)
                close()
            }
        }
        return _description!!
    }

private var _description: ImageVector? = null


public val Icons.Filled.SportsGymnastics: ImageVector
    get() {
        if (_sportsGymnastics != null) {
            return _sportsGymnastics!!
        }
        _sportsGymnastics = materialIcon(name = "Filled.SportsGymnastics") {
            materialPath {
                moveTo(4.0f, 6.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                reflectiveCurveToRelative(2.0f, 0.9f, 2.0f, 2.0f)
                reflectiveCurveTo(7.1f, 8.0f, 6.0f, 8.0f)
                reflectiveCurveTo(4.0f, 7.1f, 4.0f, 6.0f)
                close()
                moveTo(1.0f, 9.0f)
                horizontalLineToRelative(6.0f)
                lineToRelative(7.0f, -5.0f)
                lineToRelative(1.31f, 1.52f)
                lineTo(11.14f, 8.5f)
                horizontalLineTo(14.0f)
                lineTo(21.8f, 4.0f)
                lineTo(23.0f, 5.4f)
                lineTo(14.5f, 12.0f)
                lineTo(14.0f, 22.0f)
                horizontalLineToRelative(-2.0f)
                lineToRelative(-0.5f, -10.0f)
                lineTo(8.0f, 11.0f)
                horizontalLineTo(1.0f)
                verticalLineTo(9.0f)
                close()
            }
        }
        return _sportsGymnastics!!
    }

private var _sportsGymnastics: ImageVector? = null


public val Icons.Filled.FitnessCenter: ImageVector
    get() {
        if (_fitnessCenter != null) {
            return _fitnessCenter!!
        }
        _fitnessCenter = materialIcon(name = "Filled.FitnessCenter") {
            materialPath {
                moveTo(20.57f, 14.86f)
                lineTo(22.0f, 13.43f)
                lineTo(20.57f, 12.0f)
                lineTo(17.0f, 15.57f)
                lineTo(8.43f, 7.0f)
                lineTo(12.0f, 3.43f)
                lineTo(10.57f, 2.0f)
                lineTo(9.14f, 3.43f)
                lineTo(7.71f, 2.0f)
                lineTo(5.57f, 4.14f)
                lineTo(4.14f, 2.71f)
                lineTo(2.71f, 4.14f)
                lineToRelative(1.43f, 1.43f)
                lineTo(2.0f, 7.71f)
                lineToRelative(1.43f, 1.43f)
                lineTo(2.0f, 10.57f)
                lineTo(3.43f, 12.0f)
                lineTo(7.0f, 8.43f)
                lineTo(15.57f, 17.0f)
                lineTo(12.0f, 20.57f)
                lineTo(13.43f, 22.0f)
                lineToRelative(1.43f, -1.43f)
                lineTo(16.29f, 22.0f)
                lineToRelative(2.14f, -2.14f)
                lineToRelative(1.43f, 1.43f)
                lineToRelative(1.43f, -1.43f)
                lineToRelative(-1.43f, -1.43f)
                lineTo(22.0f, 16.29f)
                close()
            }
        }
        return _fitnessCenter!!
    }

private var _fitnessCenter: ImageVector? = null


public val Icons.Filled.HolidayVillage: ImageVector
    get() {
        if (_holidayVillage != null) {
            return _holidayVillage!!
        }
        _holidayVillage = materialIcon(name = "Filled.HolidayVillage") {
            materialPath {
                moveTo(18.0f, 20.0f)
                verticalLineTo(8.35f)
                lineTo(13.65f, 4.0f)
                horizontalLineToRelative(-2.83f)
                lineTo(16.0f, 9.18f)
                verticalLineTo(20.0f)
                horizontalLineTo(18.0f)
                close()
                moveTo(22.0f, 20.0f)
                verticalLineTo(6.69f)
                lineTo(19.31f, 4.0f)
                horizontalLineToRelative(-2.83f)
                lineTo(20.0f, 7.52f)
                verticalLineTo(20.0f)
                horizontalLineTo(22.0f)
                close()
                moveTo(8.0f, 4.0f)
                lineToRelative(-6.0f, 6.0f)
                verticalLineToRelative(10.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(-5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(5.0f)
                horizontalLineToRelative(5.0f)
                verticalLineTo(10.0f)
                lineTo(8.0f, 4.0f)
                close()
                moveTo(9.0f, 13.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(13.0f)
                close()
            }
        }
        return _holidayVillage!!
    }

private var _holidayVillage: ImageVector? = null


public val Icons.Filled.Cancel: ImageVector
    get() {
        if (_cancel != null) {
            return _cancel!!
        }
        _cancel = materialIcon(name = "Filled.Cancel") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.47f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.47f, 10.0f, -10.0f)
                reflectiveCurveTo(17.53f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(17.0f, 15.59f)
                lineTo(15.59f, 17.0f)
                lineTo(12.0f, 13.41f)
                lineTo(8.41f, 17.0f)
                lineTo(7.0f, 15.59f)
                lineTo(10.59f, 12.0f)
                lineTo(7.0f, 8.41f)
                lineTo(8.41f, 7.0f)
                lineTo(12.0f, 10.59f)
                lineTo(15.59f, 7.0f)
                lineTo(17.0f, 8.41f)
                lineTo(13.41f, 12.0f)
                lineTo(17.0f, 15.59f)
                close()
            }
        }
        return _cancel!!
    }

private var _cancel: ImageVector? = null
