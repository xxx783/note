package com.yutie.note.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.yutie.note.R
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.ui.theme.LocalNoteTheme

/**
 * 隐私政策页面（独立 Activity 模式）
 */
class PrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PrivacyPolicyScreen(navController = NavHostController(this))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    navController: androidx.navigation.NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 自定义标题栏
        CustomTitleBar(
            title = stringResource(R.string.str_privacy_policy),
            showBackButton = true,
            onBackClick = {
                // 返回上一级
                navController.popBackStack()
            }
        )

        // 政策内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.str_privacy_policy_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.str_privacy_policy_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle(stringResource(R.string.str_privacy_info_collection))
            Text(
                text = "1.1 账户信息：注册时，我们会收集您的邮箱地址和密码，用于账户管理和身份验证。\n" +
                        "1.2 笔记内容：您创建的笔记内容会存储在本地的数据库中，登录后可选择同步到云端。\n" +
                        "1.3 使用数据：我们不会收集您的使用习惯、位置信息等个人数据。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle(stringResource(R.string.str_privacy_info_usage))
            Text(
                text = "2.1 我们仅将收集的信息用于提供和改进我们的服务。\n" +
                        "2.2 我们不会将您的个人信息出售或出租给第三方。\n" +
                        "2.3 我们不会使用您的信息进行营销推广。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle(stringResource(R.string.str_privacy_info_storage))
            Text(
                text = "3.1 本地存储：您的笔记数据存储在手机本地的 Room 数据库中，仅您本人可以访问。\n" +
                        "3.2 云端存储：登录后，您可以选择将笔记同步到 Supabase 云端服务器。云端数据受 Supabase 的安全保护。\n" +
                        "3.3 存储期限：您的数据将保存至您注销账户或删除数据为止。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle(stringResource(R.string.str_privacy_info_security))
            Text(
                text = "4.1 我们采用 HTTPS 加密传输保护您的数据安全。\n" +
                        "4.2 您的密码经过加密处理后存储。\n" +
                        "4.3 请您妥善保管账户信息，不要将账户信息泄露给他人。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle(stringResource(R.string.str_privacy_info_sharing))
            Text(
                text = "5.1 除以下情况外，我们不会与任何第三方共享您的个人信息：\n" +
                        "   - 获得您的明确同意\n" +
                        "   - 根据法律法规要求或政府部门的要求\n" +
                        "   - 为维护社会公共利益或保护他人合法权益\n" +
                        "5.2 我们不会向广告商、数据公司等商业机构提供您的个人信息。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle(stringResource(R.string.str_privacy_your_rights))
            Text(
                text = "6.1 访问权：您可以随时查看您的笔记数据。\n" +
                        "6.2 删除权：您可以删除任何笔记，或删除整个账户。\n" +
                        "6.3 更正权：您可以随时修改您的笔记内容。\n" +
                        "6.4 注销权：您可以随时注销账户，注销后我们将删除您的所有数据。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle(stringResource(R.string.str_privacy_minors))
            Text(
                text = "7.1 我们非常重视未成年人的个人信息保护。\n" +
                        "7.2 若您是未成年人，请在监护人的陪同下使用本服务。\n" +
                        "7.3 若您是未成年人的监护人，请监督未成年人的使用行为。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle(stringResource(R.string.str_privacy_updates))
            Text(
                text = "8.1 我们可能会适时更新本隐私政策。\n" +
                        "8.2 更新后的政策将通过应用内公告等方式通知您。\n" +
                        "8.3 若您继续使用本服务，即视为您已接受更新后的政策。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle(stringResource(R.string.str_privacy_contact_us))
            Text(
                text = "如您对本隐私政策有任何疑问、意见或建议，欢迎通过应用内反馈功能联系我们。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text(
                text = stringResource(R.string.str_privacy_update_date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
