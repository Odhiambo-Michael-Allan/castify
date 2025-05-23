package com.squad.castify.core.ui.bottomsheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.squad.castify.core.designsystem.icon.CastifyIcons
import com.squad.castify.core.designsystem.theme.CastifyTheme
import com.squad.castify.core.ui.DevicePreviews
import com.squad.castify.core.ui.R

@Composable
internal fun RemoveDownloadModalBottomSheetContent(
    modifier: Modifier = Modifier,
    onRemoveDownload: () -> Unit,
) {
    Card (
        modifier = modifier.fillMaxWidth(),
        onClick = onRemoveDownload,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(
                        id = R.string.remove_download
                    ),
                    fontWeight = FontWeight.SemiBold,
                )
            },
            leadingContent = {
                Icon(
                    imageVector = CastifyIcons.Cancel,
                    contentDescription = null
                )
            }
        )
    }
}

@DevicePreviews
@Composable
private fun RemoveDownloadModalBottomSheetContentPreview() {
    CastifyTheme {
        RemoveDownloadModalBottomSheetContent {}
    }
}
