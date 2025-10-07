package com.ke.biliblli.common

import com.ke.biliblli.common.entity.DanmakuDensity
import com.ke.biliblli.common.entity.DanmakuFontSize
import com.ke.biliblli.common.entity.DanmakuPosition
import com.ke.biliblli.common.entity.DanmakuSpeed
import com.ke.biliblli.common.entity.VideoResolution
import com.ke.biliblli.common.entity.WbiParams

interface BilibiliStorage {

    var wbiParams: WbiParams?

    var danmakuEnable: Boolean

    var danmakuSpeed: DanmakuSpeed

    var danmakuDensity: DanmakuDensity

    var danmakuFontSize: DanmakuFontSize

    var danmakuPosition: DanmakuPosition

    var videoResolution: VideoResolution

    var danmakuColorful: Boolean

    var danmakuVersion: Int

    /**
     * 默认首页选项卡
     */
    var mainDefaultTab: Int

    /**
     * 是否直接播放
     */
    var directPlay: Boolean

    /**
     * 是否开启试看
     */
    var tryLook: Boolean


    var playerViewShowMiniProgressBar: Boolean

//    var uid: Long
//
//    var eid: String?
}