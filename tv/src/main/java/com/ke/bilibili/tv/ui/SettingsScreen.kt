package com.ke.bilibili.tv.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.FilterChip
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import com.ke.bilibili.tv.observeWithLifecycle
import com.ke.bilibili.tv.viewmodel.SettingsAction
import com.ke.bilibili.tv.viewmodel.SettingsEvent
import com.ke.bilibili.tv.viewmodel.SettingsState
import com.ke.bilibili.tv.viewmodel.SettingsViewModel
import com.ke.biliblli.common.Screen
import com.ke.biliblli.common.entity.DanmakuDensity
import com.ke.biliblli.common.entity.DanmakuFontSize
import com.ke.biliblli.common.entity.DanmakuPosition
import com.ke.biliblli.common.entity.DanmakuSpeed
import com.ke.biliblli.common.entity.VideoResolution
import com.ke.biliblli.common.event.MainTab

@Composable
fun SettingsRoute(navigate: (Any) -> Unit) {

    val viewModel = hiltViewModel<SettingsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    viewModel.event.observeWithLifecycle {
        when (it) {
            is SettingsEvent.ShowMessage -> {
                Toast.makeText(context.applicationContext, it.message, Toast.LENGTH_SHORT).show()
            }

            SettingsEvent.ToSplash -> {
                navigate(Screen.Splash)
            }
        }
    }

    SettingsScreen(state, {
        viewModel.handleAction(SettingsAction.UpdateVideoResolution(it))
    }, {
        viewModel.handleAction(SettingsAction.UpdateDanmakuSpeed(it))
    }, {
        viewModel.handleAction(SettingsAction.UpdateDanmakuDensity(it))
    }, {
        viewModel.handleAction(SettingsAction.UpdateDanmakuPosition(it))
    }, {
        viewModel.handleAction(SettingsAction.UpdateDanmakuFontSize(it))
    }, {
        viewModel.handleAction(SettingsAction.SetDanmakuEnable(it))
    }, {
        viewModel.handleAction(SettingsAction.SetDanmakuColorful(it))
    }, navigate, {
        viewModel.handleAction(SettingsAction.Logout)
    }, {
        viewModel.handleAction(SettingsAction.SetDefaultTab(it))
    }, {
        viewModel.handleAction(SettingsAction.ClearCache)
    }, {
        viewModel.handleAction(SettingsAction.SetPlayerViewShowMiniProgressBar(it))
    }, {
        viewModel.handleAction(SettingsAction.SetDirectPlay(it))
    }, {
        viewModel.handleAction(SettingsAction.SetTryLook(it))
    }, {
        viewModel.handleAction(SettingsAction.SetDanmakuVersion(it))
    })

    if (state.loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text("加载中", color = Color.White)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalTvMaterial3Api::class)
@Composable
private fun SettingsScreen(
    state: SettingsState,
    updateResolution: (VideoResolution) -> Unit = {},
    updateDanmakuSpeed: (DanmakuSpeed) -> Unit = {},
    updateDanmakuDensity: (DanmakuDensity) -> Unit = {},
    updateDanmakuPosition: (DanmakuPosition) -> Unit = {},
    updateDanmakuFontSize: (DanmakuFontSize) -> Unit = {},
    setDanmakuEnable: (Boolean) -> Unit = {},
    setDanmakuColorful: (Boolean) -> Unit = {},
    navigate: (Any) -> Unit,
    logout: () -> Unit,
    setDefaultTab: (MainTab) -> Unit,
    clearCache: () -> Unit,
    setPlayerViewShowMiniProgressBar: (Boolean) -> Unit,
    setDirectPlay: (Boolean) -> Unit,
    setTryLook: (Boolean) -> Unit,
    setDanmakuVersion: (Int) -> Unit
) {


    val titleTextStyle = MaterialTheme.typography.titleLarge.copy(color = Color.White)
    val columnModifier = Modifier
        .padding(16.dp)
        .background(color = MaterialTheme.colorScheme.surfaceVariant)
        .padding(16.dp)
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp
    ) {

        item {

            Column(
                modifier = columnModifier
            ) {
                Text(
                    "默认视频分辨率",
                    style = titleTextStyle,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VideoResolution.entries.reversed().forEach {
                        FilterChip(selected = it == state.videoResolution, onClick = {
                            updateResolution(it)
                        }, leadingIcon = {
                            if (it == state.videoResolution) {
                                Icon(Icons.Default.RadioButtonChecked, null)
                            } else {
                                Icon(Icons.Default.RadioButtonUnchecked, null)

                            }
                        }) {
                            Text(it.displayName)
                        }
                    }

                }
            }
        }

        item {

            Column(
                modifier = columnModifier
            ) {
                Text(
                    "首页默认选项卡",
                    style = titleTextStyle,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MainTab.entries.filter { it.canSetDefault }.forEach {
                        FilterChip(selected = it == state.defaultTab, onClick = {
//                            updateDanmakuFontSize(it)
                            setDefaultTab(it)
                        }, leadingIcon = {
                            if (it == state.defaultTab) {
                                Icon(Icons.Default.RadioButtonChecked, null)
                            } else {
                                Icon(Icons.Default.RadioButtonUnchecked, null)

                            }
                        }) {
                            Text(it.displayName)
                        }
                    }

                }
            }
        }


        item {


            ListItem(selected = false, onClick = {
                setDirectPlay(!state.directPlay)
            }, modifier = columnModifier, headlineContent = {
                Text("点击视频封面直接播放", style = titleTextStyle.copy(color = Color.Unspecified))
            }, trailingContent = {
                Switch(checked = state.directPlay, onCheckedChange = {

                })
            })

        }


        item {


            ListItem(selected = false, onClick = {
                setTryLook(!state.tryLook)
            }, modifier = columnModifier, headlineContent = {
                Text("启动试看", style = titleTextStyle.copy(color = Color.Unspecified))
            }, supportingContent = {
                Text("启用该配置后,最高画质仅支持1080p,但是较为稳定")
            }, trailingContent = {
                Switch(checked = state.tryLook, onCheckedChange = {

                })
            })

        }


        item {


            ListItem(selected = false, onClick = {
                setDanmakuEnable(!state.danmakuEnable)
            }, modifier = columnModifier, headlineContent = {
                Text("弹幕开关", style = titleTextStyle.copy(color = Color.Unspecified))
            }, trailingContent = {
                Switch(checked = state.danmakuEnable, onCheckedChange = {

                })
            })

        }

//        item {
//
//
//            ListItem(selected = false, onClick = {
//                setDanmakuVersion(if (state.danmakuVersion == 0) 1 else 0)
//            }, modifier = columnModifier, headlineContent = {
//                Text("弹幕优化", style = titleTextStyle.copy(color = Color.Unspecified))
//            }, supportingContent = {
//                Text("开启后，每行同一时间只显示一条弹幕")
//            }, trailingContent = {
//                Switch(checked = state.danmakuVersion == 1, onCheckedChange = {
//
//                })
//            })
//
//        }

        item {


            ListItem(selected = false, onClick = {
                setDanmakuColorful(!state.danmakuColorful)
            }, modifier = columnModifier, headlineContent = {
                Text("彩色弹幕", style = titleTextStyle.copy(color = Color.Unspecified))
            }, trailingContent = {
                Switch(checked = state.danmakuColorful, onCheckedChange = {

                })
            })

        }

        item {


            ListItem(selected = false, onClick = {
                setPlayerViewShowMiniProgressBar(!state.playerViewShowMiniProgressBar)
            }, modifier = columnModifier, headlineContent = {
                Text(
                    "播放页面底部迷你进度条",
                    style = titleTextStyle.copy(color = Color.Unspecified)
                )
            }, trailingContent = {
                Switch(checked = state.playerViewShowMiniProgressBar, onCheckedChange = {

                })
            })

        }

        item {

            Column(
                modifier = columnModifier
            ) {
                Text(
                    "弹幕位置",
                    style = titleTextStyle,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DanmakuPosition.entries.forEach {
                        FilterChip(selected = it == state.danmakuPosition, onClick = {
                            updateDanmakuPosition(it)
                        }, leadingIcon = {
                            if (it == state.danmakuPosition) {
                                Icon(Icons.Default.RadioButtonChecked, null)
                            } else {
                                Icon(Icons.Default.RadioButtonUnchecked, null)

                            }
                        }) {
                            Text(it.displayName)
                        }
                    }

                }
            }
        }

        item {

            Column(
                modifier = columnModifier
            ) {
                Text(
                    "弹幕字体大小",
                    style = titleTextStyle,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DanmakuFontSize.entries.forEach {
                        FilterChip(selected = it == state.danmakuFontSize, onClick = {
                            updateDanmakuFontSize(it)
                        }, leadingIcon = {
                            if (it == state.danmakuFontSize) {
                                Icon(Icons.Default.RadioButtonChecked, null)
                            } else {
                                Icon(Icons.Default.RadioButtonUnchecked, null)

                            }
                        }) {
                            Text(it.displayName)
                        }
                    }

                }
            }
        }

        item {

            Column(
                modifier = columnModifier
            ) {
                Text(
                    "弹幕密度",
                    style = titleTextStyle,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DanmakuDensity.entries.forEach {
                        FilterChip(selected = it == state.danmakuDensity, onClick = {
                            updateDanmakuDensity(it)
                        }, leadingIcon = {
                            if (it == state.danmakuDensity) {
                                Icon(Icons.Default.RadioButtonChecked, null)
                            } else {
                                Icon(Icons.Default.RadioButtonUnchecked, null)

                            }
                        }) {
                            Text(it.displayName)
                        }
                    }

                }
            }
        }

        item {

            Column(
                modifier = columnModifier
            ) {
                Text(
                    "弹幕速度",
                    style = titleTextStyle,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DanmakuSpeed.entries.forEach {
                        FilterChip(selected = it == state.danmakuSpeed, onClick = {
                            updateDanmakuSpeed(it)
                        }, leadingIcon = {
                            if (it == state.danmakuSpeed) {
                                Icon(Icons.Default.RadioButtonChecked, null)
                            } else {
                                Icon(Icons.Default.RadioButtonUnchecked, null)

                            }
                        }) {
                            Text(it.displayName)
                        }
                    }

                }
            }
        }

        item {
            Card(onClick = {
                navigate(Screen.UploadApk)
            }, modifier = columnModifier) {
                Text("上传更新安装包文件")
            }
        }

        item {
            Card(onClick = logout, modifier = columnModifier) {
                Text("退出登录")
            }
        }

        item {
            Card(onClick = clearCache, modifier = columnModifier) {
                Text("清除缓存：${state.cacheSize}")
            }
        }
    }
}

//@Preview(device = Devices.TV_1080p)
//@Composable
//private fun SettingsScreenPreview() {
//    BilibiliTheme {
//        SettingsScreen()
//    }
//}