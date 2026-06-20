package com.yutie.note.ui.editor

import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.linkify.LinkifyPlugin

/**
 * Markdown 预览面板
 */
@Composable
fun MarkdownPreviewPanel(
    content: String
) {
    val context = LocalContext.current
    
    val markwon = remember {
        Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .build()
    }
    
    AndroidView(
        factory = { ctx ->
            TextView(ctx).apply {
                setPadding(
                    (16 * ctx.resources.displayMetrics.density).toInt(),
                    (16 * ctx.resources.displayMetrics.density).toInt(),
                    (16 * ctx.resources.displayMetrics.density).toInt(),
                    (16 * ctx.resources.displayMetrics.density).toInt()
                )
                setLineSpacing(0f, 1.6f)
                textSize = 16f
            }
        },
        update = { textView ->
            val processedContent = content.replace("\n", "  \n")
            markwon.setMarkdown(textView, processedContent)
        },
        modifier = Modifier.fillMaxWidth()
    )
}
