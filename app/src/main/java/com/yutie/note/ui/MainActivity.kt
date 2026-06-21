package com.yutie.note.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yutie.note.ui.theme.LocalNoteTheme
import com.yutie.note.ui.UserAgreementScreen
import com.yutie.note.ui.PrivacyPolicyScreen
import com.yutie.note.ui.AIToolboxScreen
import com.yutie.note.ui.AIToolScreen
import java.util.Locale

/**
 * 主 Activity
 * 应用入口
 */
class MainActivity : ComponentActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        // 在 attachBaseContext 中应用语言设置，这会在 onCreate 之前调用
        val context = applyLanguageSetting(newBase)
        super.attachBaseContext(context)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 启用全面屏，包括状态栏和导航栏
        enableEdgeToEdge()
        
        setContent {
            LocalNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                navController = navController,
                                viewModel = viewModel()
                            )
                        }
                        
                        composable(
                            route = "noteEdit/{noteId}",
                            arguments = listOf(
                                navArgument("noteId") {
                                    type = androidx.navigation.NavType.LongType
                                    defaultValue = 0L
                                }
                            ),
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
                            NoteEditScreen(
                                noteId = noteId,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "settings",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            SettingsScreen(navController = navController)
                        }

                        composable(
                            route = "featureGuide",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            FeatureGuideScreen(navController = navController)
                        }

                        composable(
                            route = "login",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            LoginScreen(
                                navController = navController,
                                onLoginSuccess = {
                                    // 登录成功后返回设置页面
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = "register",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            RegisterScreen(
                                navController = navController,
                                onRegisterSuccess = {
                                    // 注册成功后返回登录页面
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        // 用户协议页面
                        composable(
                            route = "user_agreement",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            UserAgreementScreen(navController = navController)
                        }
                        
                        // 隐私政策页面
                        composable(
                            route = "privacy_policy",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            PrivacyPolicyScreen(navController = navController)
                        }
                        
                        // 主题编辑器页面（Pro 功能）
                        composable(
                            route = "theme_editor",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            ThemeEditorScreen(navController = navController)
                        }
                        
                        // 官方主题商店页面（Pro 功能）
                        composable(
                            route = "theme_store",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            OfficialThemeStoreScreen(navController = navController)
                        }
                        
                        // 社区主题列表页面（APEX 功能）
                        composable(
                            route = "community_theme",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            CommunityThemeScreen(navController = navController)
                        }
                        
                        // 上传主题页面（APEX 功能）
                        composable(
                            route = "upload_theme",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            UploadThemeScreen(navController = navController)
                        }
                        
                        // 主题详情页（社区主题详情）
                        composable(
                            route = "theme_detail/{themeId}",
                            arguments = listOf(
                                navArgument("themeId") {
                                    type = androidx.navigation.NavType.StringType
                                }
                            ),
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) { backStackEntry ->
                            val themeId = backStackEntry.arguments?.getString("themeId") ?: return@composable
                            ThemeDetailScreen(
                                navController = navController,
                                themeId = themeId
                            )
                        }
                        
                        // 升级申请页面
                        composable(
                            route = "upgrade_application",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            UpgradeApplicationScreen(navController = navController)
                        }
                        
                        // 发布公告页面（管理员专属）
                        composable(
                            route = "publish_announcement",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            PublishAnnouncementScreen(navController = navController)
                        }
                        
                        // 历史公告页面
                        composable(
                            route = "history_announcements",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            HistoryAnnouncementsScreen(navController = navController)
                        }
                        
                        // 问题反馈页面
                        composable(
                            route = "feedback",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            FeedbackScreen(navController = navController)
                        }
                        
                        // 检查更新页面
                        composable(
                            route = "check_update",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            CheckUpdateScreen(navController = navController)
                        }
                        
                        // 语言设置页面
                        composable(
                            route = "language_settings",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            LanguageSettingsScreen(navController = navController)
                        }
                        
                        // AI 工具箱页面（APEX 功能）
                        composable(
                            route = "ai_toolbox",
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) {
                            AIToolboxScreen(navController = navController)
                        }
                        
                        // AI 工具详情页面
                        composable(
                            route = "ai_tool/{toolType}/{toolName}",
                            arguments = listOf(
                                navArgument("toolType") {
                                    type = androidx.navigation.NavType.StringType
                                },
                                navArgument("toolName") {
                                    type = androidx.navigation.NavType.StringType
                                }
                            ),
                            enterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(500)
                                ) + fadeIn(animationSpec = tween(500))
                            },
                            popExitTransition = {
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(500)
                                ) + fadeOut(animationSpec = tween(500))
                            }
                        ) { backStackEntry ->
                            val toolType = backStackEntry.arguments?.getString("toolType") ?: ""
                            val toolName = backStackEntry.arguments?.getString("toolName") ?: ""
                            AIToolScreen(
                                navController = navController,
                                toolType = toolType,
                                toolName = toolName
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 应用语言设置
     * 必须在 attachBaseContext 中调用，才能在 Activity 创建前生效
     */
    private fun applyLanguageSetting(baseContext: Context): Context {
        val prefs = baseContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val language = prefs.getString("app_language", "auto") ?: "auto"
        
        println("=== MainActivity 语言设置 ===")
        println("保存的语言：$language")
        
        if (language == "auto") {
            // 跟随系统
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            return baseContext
        }
        
        // 设置指定语言
        val localeList = LocaleListCompat.forLanguageTags(language)
        AppCompatDelegate.setApplicationLocales(localeList)
        
        // 返回更新后的 Context
        val locale = Locale(language)
        val configuration = baseContext.resources.configuration
        configuration.setLocales(android.os.LocaleList(locale))
        return baseContext.createConfigurationContext(configuration)
    }
}
