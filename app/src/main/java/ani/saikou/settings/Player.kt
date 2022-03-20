package ani.saikou.settings

import java.io.Serializable

data class PlayerSettings(
    var videoInfo:Boolean = true,
    var defaultSpeed:Int = 5,

    //Behaviour
    var focusPause:Boolean = true,
    var gestures:Boolean = true,
    var doubleTap:Boolean = true,
    var seekTime:Int = 10,
    var skipTime:Int = 85,
    var cast:Boolean = false,
):Serializable
