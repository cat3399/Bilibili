package com.ke.biliblli.repository

import android.util.Base64
import com.ke.biliblli.api.BilibiliApi
import com.ke.biliblli.api.BilibiliHtmlApi
import com.ke.biliblli.api.response.BaseResponse
import com.ke.biliblli.api.response.CommentResponse
import com.ke.biliblli.api.response.DynamicResponse
import com.ke.biliblli.api.response.DynamicUpListResponse
import com.ke.biliblli.api.response.HistoryResponse
import com.ke.biliblli.api.response.HomeRecommendListResponse
import com.ke.biliblli.api.response.LaterWatchResponse
import com.ke.biliblli.api.response.LoginInfoResponse
import com.ke.biliblli.api.response.PageResponse
import com.ke.biliblli.api.response.PollQrcodeResponse
import com.ke.biliblli.api.response.QrCodeResponse
import com.ke.biliblli.api.response.RelationStatusResponse
import com.ke.biliblli.api.response.SearchHotWordListResponse
import com.ke.biliblli.api.response.SearchListResponse
import com.ke.biliblli.api.response.SearchSuggestResponse
import com.ke.biliblli.api.response.SeasonsListDataResponse
import com.ke.biliblli.api.response.UserArchivesResponse
import com.ke.biliblli.api.response.UserFavListResponse
import com.ke.biliblli.api.response.UserInfoResponse
import com.ke.biliblli.api.response.VideoInfoResponse
import com.ke.biliblli.api.response.VideoUrlResponse
import com.ke.biliblli.api.response.VideoViewResponse
import com.ke.biliblli.common.BilibiliRepository
import com.ke.biliblli.common.BilibiliStorage
import com.ke.biliblli.common.CrashHandler
import com.ke.biliblli.common.KePersistentCookieJar
import com.ke.biliblli.common.entity.WbiParams
import com.ke.biliblli.common.http.BilibiliProtoApi
import com.ke.biliblli.db.dao.SearchHistoryDao
import com.ke.biliblli.db.entity.SearchHistoryEntity
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.TreeMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BilibiliRepositoryImpl @Inject constructor(
    private val bilibiliApi: BilibiliApi,
    override val bilibiliProtoApi: BilibiliProtoApi,
    private val bilibiliHtmlApi: BilibiliHtmlApi,
    private val bilibiliStorage: BilibiliStorage,
    private val kePersistentCookieJar: KePersistentCookieJar,
    private val searchHistoryDao: SearchHistoryDao
) :
    BilibiliRepository {


    override suspend fun userFav(mid: Long): BaseResponse<UserFavListResponse> {
        return bilibiliApi.userFav(mid)
    }

//    override suspend fun getBuvid3(): String? {
//        return bilibiliApi.getBuvid().data?.buvid
//    }

    override suspend fun laterWatchList(): BaseResponse<LaterWatchResponse> {
        return bilibiliApi.laterWatch()
    }

    override suspend fun comments(
        index: Int,
        oid: Long,
        type: Int,
        sort: Int
    ): BaseResponse<CommentResponse> {
        return bilibiliApi.comments(oid, index, type, sort)
    }


    override suspend fun updateDynamicUpList(): BaseResponse<DynamicUpListResponse> {
        return bilibiliApi.updateDynamicUpList()
    }

//    override suspend fun dm(type: Int, oid: Long, index: Int): DmSegSDKReply {
//        return bilibiliProtoApi.dm(type, oid, index)
//    }

    override suspend fun userVideos(
        userId: Long,
        index: Int,
        size: Int,
        keywords: String
    ): BaseResponse<UserArchivesResponse> {
        return bilibiliApi.userVideos(userId, size, index, keywords)
    }

    override suspend fun seasonsSeriesList(
        userId: Long,
        index: Int,
        size: Int
    ): BaseResponse<SeasonsListDataResponse> {
        return bilibiliApi.seasonsSeriesList(userId, index, size)
    }


    override fun searchHistoryList(): Flow<List<String>> {
        return searchHistoryDao.all().map { list ->
            list.map { it.keywords }
        }
    }

    override suspend fun dynamicList(
        offset: String?,
        type: String,
        mid: Long?
    ): BaseResponse<DynamicResponse> {
        return bilibiliApi.dynamicList(offset, type, mid)
    }

    override suspend fun videoInfo(bvid: String): BaseResponse<VideoInfoResponse> {
        var current = bilibiliStorage.wbiParams

        val wbiParams = if (current?.canUse() != true) {
            createAndSaveWbiParams()
        } else {
            current
        }


        val now = System.currentTimeMillis() / 1000
        val treeMap = TreeMap<String, Any>()
        treeMap.put("bvid", bvid)

        val sign = WbiUtil.enc(treeMap, wbiParams.image, wbiParams.sub)

        return bilibiliApi.videoInfo(bvid, now, sign!!)
    }


    override suspend fun logout() {
        kePersistentCookieJar.deleteAllCookie()
    }

    override suspend fun search(keywords: String, index: Int): BaseResponse<SearchListResponse> {
        if (keywords.isNotEmpty()) {
            searchHistoryDao.insert(SearchHistoryEntity(keywords))
        }

        var current = bilibiliStorage.wbiParams

        val wbiParams = if (current?.canUse() != true) {
            createAndSaveWbiParams()
        } else {
            current
        }


        val now = System.currentTimeMillis() / 1000
        val treeMap = TreeMap<String, Any>()
        treeMap.put("search_type", "video")
        treeMap.put("keyword", keywords)
        treeMap.put("page", index.toString())
        val sign = WbiUtil.enc(treeMap, wbiParams.image, wbiParams.sub)
        treeMap.put("wst", now.toString())
        treeMap.put("w_rid", sign ?: "")

        val map = mutableMapOf<String, String>()
        treeMap.forEach {
            map[it.key] = it.value.toString()
        }

        return bilibiliApi.search(map)
    }


    override suspend fun reportHistory(aid: Long, cid: Long, progress: Long) {
        try {
            bilibiliApi.reportHistory(
                aid, cid, progress, csrf = kePersistentCookieJar.getCsrf()
            )
        } catch (e: Exception) {
            CrashHandler.handler(e)
        }

    }

    override suspend fun loginInfo(): BaseResponse<LoginInfoResponse> {
        val loginInfo = bilibiliApi.loginInfo()
        val params = loginInfo.data!!
        bilibiliStorage.wbiParams = WbiParams(
            params.json.image.substring(params.json.image.lastIndexOf("/")).split(".")[0],
            params.json.sub.substring(params.json.sub.lastIndexOf("/")).split(".")[0],
            System.currentTimeMillis()
        )

//        if (params.mid != null) {
//            bilibiliApi.userFav(params.mid!!)
//            bilibiliApi.userFavVideo(params.mid!!, 20, 1)
//        }
//        val uid = loginInfo.data?.mid
//        if (uid != null) {
//            bilibiliStorage.uid = uid
//            bilibiliStorage.eid = genAuroraEid(uid)
//        }
        return loginInfo
    }

    override suspend fun pageList(bvid: String): BaseResponse<List<PageResponse>> {
        return bilibiliApi.pageList(bvid)
    }

//    val spmPrefixExp = Regex("<meta name="spm_prefix" content="([^"]+?)">")

    val pattern =
        """<meta\s+name="spm_prefix"\s+content="([^"]+)"\s*/?>""".toRegex(RegexOption.IGNORE_CASE)


    override suspend fun searchKeyWords(): SearchHotWordListResponse {
        return bilibiliApi.searchKeyWords()
    }

    override suspend fun searchSuggest(term: String): SearchSuggestResponse {
        return bilibiliApi.searchSuggest(term)
    }

    override suspend fun initBuvid() {
        bilibiliHtmlApi.mainPage()
        ensureBuvid3Cookie()
    }

    private suspend fun ensureBuvid3Cookie() {
        val requestUrl = BilibiliApi.baseUrl.toHttpUrl()
        val hasBuvid3 = kePersistentCookieJar.loadForRequest(requestUrl)
            .any { it.name == "buvid3" && it.value.isNotEmpty() }
        if (hasBuvid3) {
            return
        }
        try {
            val response = bilibiliApi.fingerSpi()
            val buvid3 = response.data?.b3
            if (!buvid3.isNullOrEmpty()) {
                val expiresAt = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
                val cookie = Cookie.Builder()
                    .name("buvid3")
                    .value(buvid3)
                    .domain("bilibili.com")
                    .path("/")
                    .expiresAt(expiresAt)
                    .secure()
                    .build()
                kePersistentCookieJar.saveFromResponse(requestUrl, listOf(cookie))
            }
        } catch (e: Exception) {
            CrashHandler.handler(e)
        }
    }

    private fun genAuroraEid(uid: Long): String? {
        if (uid == 0L) {
            return null
        }
        val uidString = uid.toString()
        val resultBytes = ByteArray(uidString.length) { i ->
            (uidString[i].code xor "ad1va46a7lza"[i % 12].code).toByte()
        }
        var auroraEid = String(Base64.encode(resultBytes, Base64.NO_WRAP))
        auroraEid = auroraEid.replace(Regex("=*$"), "")
        return auroraEid
    }

    override suspend fun userNavNum(id: Long) {
        return bilibiliApi.userNavNum(id)
    }

    override suspend fun homeRecommendVideos(index: Int): BaseResponse<HomeRecommendListResponse> {
        return bilibiliApi.homeRecommendVideoList(index, index)
    }

    override suspend fun favDetail(id: Long, index: Int, size: Int) =
        bilibiliApi.favDetail(id, index, size)


    override suspend fun userFollowers(userId: Long, index: Int, size: Int) =
        bilibiliApi.userFollowers(userId, index, size)

    override suspend fun userFollowings(
        userId: Long,
        index: Int,
        size: Int
    ) = bilibiliApi.userFollowings(userId, index, size)

    override suspend fun videoView(bvid: String): BaseResponse<VideoViewResponse> {
        return bilibiliApi.videoView(bvid)
    }

    override suspend fun userRelationStatus(uid: Long): BaseResponse<RelationStatusResponse> {
        return bilibiliApi.userRelationStatus(uid)
    }

    override suspend fun userInfo(mid: Long): BaseResponse<UserInfoResponse> {

        var current = bilibiliStorage.wbiParams

        val wbiParams = if (current?.canUse() != true) {
            createAndSaveWbiParams()
        } else {
            current
        }

        val platform = "web"
        val location = "1550101"

        val now = System.currentTimeMillis() / 1000
        val treeMap = TreeMap<String, Any>()
        treeMap.put("mid", mid)
        treeMap.put("wts", now)
        treeMap.put("platform", platform)
        treeMap.put("web_location", location)
        treeMap.put("token", "")
        val sign = WbiUtil.enc(treeMap, wbiParams.image, wbiParams.sub)


        return bilibiliApi.userInfo(
            mid, platform, location, "", now, sign!!
        )
    }

    override suspend fun history(
        max: Long?,
        business: String?,
        at: Long?,
        type: String?
    ): BaseResponse<HistoryResponse> {
        return bilibiliApi.history(max, business, at, type)
    }

    override suspend fun videoUrl(cid: Long, bvid: String): BaseResponse<VideoUrlResponse> {
        var current = bilibiliStorage.wbiParams

        val wbiParams = if (current?.canUse() != true) {
            createAndSaveWbiParams()
        } else {
            current
        }

        val fnval = 4048
//            0b111111010000
        val qn = 126
        val tryLookEnabled = bilibiliStorage.tryLook

        val now = System.currentTimeMillis() / 1000
        val treeMap = TreeMap<String, Any>()
        treeMap.put("cid", cid)
        treeMap.put("bvid", bvid)
        treeMap.put("qn", qn)
//        treeMap.put("qn", 80)
        treeMap.put("fnval", fnval)//4048
        treeMap.put("fourk", 1)
        treeMap.put("voice_balance", 1)
        treeMap.put("gaia_source", "pre-load")
        treeMap.put("web_location", "1550101")
        treeMap.put("wts", now)
        if (tryLookEnabled) {
            treeMap["try_look"] = 1
        }

        val sign = WbiUtil.enc(treeMap, wbiParams.image, wbiParams.sub)

        return bilibiliApi.videoUrl(
            cid,
            bvid = bvid,
            qn = qn,
            fnval = fnval,
            tryLook = if (tryLookEnabled) 1 else null,
            wts = now,
            sign = sign ?: ""
        )

    }

    override suspend fun loginQrCode(): BaseResponse<QrCodeResponse> {
        return bilibiliApi.generateQrcode()
    }

    override suspend fun checkLogin(key: String): BaseResponse<PollQrcodeResponse> {
        return bilibiliApi.pollQrcode(key)
    }


    private suspend fun createAndSaveWbiParams(): WbiParams {
        val params = bilibiliApi.loginInfo().data!!

        val now = System.currentTimeMillis()

        val wbiParams = WbiParams(
            params.json.image.substring(params.json.image.lastIndexOf("/")).split(".")[0],
            params.json.sub.substring(params.json.sub.lastIndexOf("/")).split(".")[0],
            now
        )
        bilibiliStorage.wbiParams = wbiParams

        return wbiParams
    }

}


object WbiUtil {
    private val mixinKeyEncTab = intArrayOf(
        46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49,
        33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13, 37, 48, 7, 16, 24, 55, 40,
        61, 26, 17, 0, 1, 60, 51, 30, 4, 22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11,
        36, 20, 34, 44, 52
    )

    private val hexDigits = "0123456789abcdef".toCharArray()

    private fun md5(input: String): String? {
        try {
            val md = MessageDigest.getInstance("MD5")
            val messageDigest = md.digest(input.toByteArray(StandardCharsets.UTF_8))
            val result = CharArray(messageDigest.size * 2)
            for (i in messageDigest.indices) {
                result[i * 2] = hexDigits[(messageDigest[i].toInt() shr 4) and 0xF]
                result[i * 2 + 1] = hexDigits[messageDigest[i].toInt() and 0xF]
            }
            return String(result)
        } catch (e: NoSuchAlgorithmException) {
            return null
        }
    }

    private fun getMixinKey(imgKey: String?, subKey: String): String {
        val s = imgKey + subKey
        val key = StringBuilder()
        for (i in 0..31) key.append(s.get(mixinKeyEncTab[i]))
        return key.toString()
    }

    private fun encodeURIComponent(o: Any): String {

        return URLEncoder.encode(o.toString()).replace("+", "%20")
    }


    fun enc(
        map: TreeMap<String, Any>,
        imgKey: String,
        subKey: String,
    ): String? {
        val mixinKey = getMixinKey(imgKey, subKey)


        val param = map.entries.joinToString("&") { it ->
            String.format(
                "%s=%s",
                it.key,
                encodeURIComponent(it.value)
            )
        }
        val s = param + mixinKey

        return md5(s)


    }
}