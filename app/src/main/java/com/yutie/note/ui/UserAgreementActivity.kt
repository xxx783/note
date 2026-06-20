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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.yutie.note.R
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.ui.theme.LocalNoteTheme

/**
 * 用户协议页面（独立 Activity 模式）
 */
class UserAgreementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UserAgreementScreen(navController = NavHostController(this))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAgreementScreen(
    navController: androidx.navigation.NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 自定义标题栏
        CustomTitleBar(
            title = stringResource(R.string.str_user_agreement),
            showBackButton = true,
            onBackClick = {
                // 返回上一级
                navController.popBackStack()
            }
        )

        // 协议内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.str_user_agreement_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.str_user_agreement_welcome),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle(stringResource(R.string.str_ua_service))
            Text(
                text = stringResource(R.string.str_user_agreement_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle(stringResource(R.string.str_ua_registration))
            Text(
                text = "2.1 您需要注册并登录账户才能使用云端同步功能。\n" +
                        "2.2 您应保证注册信息的真实性和准确性。\n" +
                        "2.3 您应妥善保管账户信息，不得将账户出借或转让给他人使用。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle(stringResource(R.string.str_ua_behavior))
            Text(
                text = "3.1 您在使用本服务时，应遵守国家法律法规，不得制作、复制、发布、传播违法违规内容。\n" +
                        "3.2 您不得利用本服务从事任何侵犯他人合法权益的行为。\n" +
                        "3.3 您不得对本软件进行反向工程、反向编译或反汇编。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle(stringResource(R.string.str_ua_ip))
            Text(
                text = "4.1 本软件及其包含的所有内容（包括但不限于文字、图片、音频、视频、软件、程序等）的知识产权归开发者所有。\n" +
                        "4.2 未经开发者书面许可，您不得为商业目的复制、传播、修改本软件的任何内容。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle(stringResource(R.string.str_ua_disclaimer))
            Text(
                text = "5.1 因网络状况、通讯线路、服务器故障等任何非开发者原因导致的服务中断或数据丢失，开发者不承担责任。\n" +
                        "5.2 您应自行备份重要数据，开发者不对因不可抗力导致的数据丢失承担责任。\n" +
                        "5.3 您因使用本服务而遭受的损失，开发者不承担赔偿责任。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle(stringResource(R.string.str_ua_changes))
            Text(
                text = "6.1 开发者有权根据需要修改本协议内容，修改后的协议将通过应用内公告等方式通知您。\n" +
                        "6.2 如您继续使用本服务，即视为您已接受修改后的协议。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle(stringResource(R.string.str_ua_other))
            Text(
                text = "7.1 本协议的解释、效力及纠纷的解决，适用中华人民共和国法律。\n" +
                        "7.2 若您与开发者发生任何纠纷或争议，首先应友好协商解决；协商不成的，应提交开发者所在地有管辖权的人民法院诉讼解决。\n" +
                        "7.3 本协议自您注册或登录之日起生效。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text(
                text = stringResource(R.string.str_user_agreement_update_date),
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
