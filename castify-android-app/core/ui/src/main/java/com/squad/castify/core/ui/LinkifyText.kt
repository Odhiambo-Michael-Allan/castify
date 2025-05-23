package com.squad.castify.core.ui

import android.text.SpannableString
import android.text.style.URLSpan
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.squad.castify.core.designsystem.theme.CastifyTheme

import android.os.Build
import android.text.util.Linkify
import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/* Many thanks to: https://stackoverflow.com/questions/77873019/jetpack-compose-detect-links-in-a-text-in-composable-text-and-make-it-clickable */
@Composable
fun LinkifyText(
    text: String,
    modifier: Modifier = Modifier,
    linkColor: Color = MaterialTheme.colorScheme.primary,
    linkEntire: Boolean = false,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    clickable: Boolean = true,
    onClickLink: ((linkText: String) -> Unit)? = null
) {
    val uriHandler = LocalUriHandler.current
    val linkInfos = if (linkEntire) listOf(LinkInfo(text, 0, text.length)) else SpannableStr.getLinkInfos(
        text
    )
    val annotatedString = buildAnnotatedString {
        append(text)
        linkInfos.forEach {
            addStyle(
                style = SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline,
                    fontStyle = FontStyle.Italic
                ),
                start = it.start,
                end = it.end
            )
            addStringAnnotation(
                tag = "tag",
                annotation = it.url,
                start = it.start,
                end = it.end
            )
        }
    }
    if (clickable) {
        ClickableText(
            text = annotatedString,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            onTextLayout = onTextLayout,
            style = style,
            onClick = { offset ->
                val item =  annotatedString.getStringAnnotations(
                    start = offset,
                    end = offset,
                ).firstOrNull()

                if(linkEntire){
                    Log.e("TAG", "LinkifyText: Entire Link" )
                    onClickLink?.invoke("")
                }else{
                    if(item != null) {
                        uriHandler.openUri(item.item)
                        onClickLink?.invoke(annotatedString.substring(item.start, item.end))
                    }else{
                        Log.e("TAG", "LinkifyText: Entire Link" )
                        onClickLink?.invoke("")
                    }
                }
            }
        )
    } else {
        Text(
            text = annotatedString,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            onTextLayout = onTextLayout,
            style = style
        )
    }
}

@Composable
private fun ClickableText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    onClick: (Int) -> Unit
) {
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val pressIndicator = Modifier.pointerInput(onClick) {
        detectTapGestures { pos ->
            layoutResult.value?.let { layoutResult ->
                onClick(layoutResult.getOffsetForPosition(pos))
            }
        }
    }
    Text(
        text = text,
        modifier = modifier.then(pressIndicator),
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        onTextLayout = {
            layoutResult.value = it
            onTextLayout(it)
        },
        style = style
    )
}

private data class LinkInfo(
    val url: String,
    val start: Int,
    val end: Int
)

private class SpannableStr(source: CharSequence): SpannableString(source) {
    companion object {
        fun getLinkInfos(text: String): List<LinkInfo> {
            val spannableStr = SpannableStr(text)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Linkify.addLinks(spannableStr, Linkify.ALL) { str: String -> URLSpan(str)  }
            } else {
                Linkify.addLinks(spannableStr, Linkify.ALL)
            }
            return spannableStr.linkInfos
        }
    }
    private inner class Data(
        val what: Any?,
        val start: Int,
        val end: Int
    )
    private val spanList = mutableListOf<Data>()

    private val linkInfos: List<LinkInfo>
        get() = spanList.filter { it.what is URLSpan }.map {
            LinkInfo(
                (it.what as URLSpan).url,
                it.start,
                it.end
            )
        }

    override fun removeSpan(what: Any?) {
        super.removeSpan(what)
        spanList.removeAll { it.what == what }
    }

    override fun setSpan(what: Any?, start: Int, end: Int, flags: Int) {
        super.setSpan(what, start, end, flags)
        spanList.add(Data(what, start, end))
    }
}

@Preview
@Composable
private fun SummaryTextPreview() {
    CastifyTheme {
        Surface {
            LinkifyText(
                text = "I was a broke, university dropout, at 18 I built an industry leading social media marketing company, and at 27 I resigned as CEO. At 28 I co-founded Flight Story – a marketing and communications company, and thirdweb - a software platform, making it easy to build web3 applications. I then launched private equity fund, Flight Fund, to accelerate the next generation of European unicorns. During this time I decided to launch \u0027The Diary Of A CEO\u0027 podcast with the simple mission of providing an unfiltered journey into the remarkable stories and untold dimensions of the world’s most influential people, experts and thinkers.\n\nIf you enjoy the show, please hit the follow button! That\u0027s a small way you can help us carry on doing this, thank you for listening.\n\nMy New Book: https://g2ul0.app.link/DOAC\nIG:https://www.instagram.com/steven\nLI: https://www.linkedin.com/in/stevenbartlett-123"
            )
        }
    }
}