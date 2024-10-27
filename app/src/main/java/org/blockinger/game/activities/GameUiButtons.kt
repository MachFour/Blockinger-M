package org.blockinger.game.activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import org.blockinger.game.R

@Composable
@Preview
fun ButtonsPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            MoveLeftButton(modifier = Modifier.size(48.dp, 48.dp))
            MoveRightButton(modifier = Modifier.size(48.dp, 48.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RotateLeftButton(modifier = Modifier.size(96.dp, 96.dp))
            RotateRightButton(modifier = Modifier.size(96.dp, 96.dp))
        }

        SoftDropButton(modifier = Modifier.size(48.dp, 96.dp))
        HardDropButton(modifier = Modifier.size(96.dp, 48.dp))
        PauseButton(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun PauseButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    GameButton(
        modifier = modifier,
        onPress = { context.findActivity()?.finish() },
    ) {
        Box(modifier = it) {
            Text(
                text = stringResource(R.string.pausebutton),
                style = MaterialTheme.typography.bodySmall,
                fontSize = TextUnit(18f, TextUnitType.Sp),
                maxLines = 1,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun MoveLeftButton(modifier: Modifier = Modifier) {
    val controls = LocalControls.current
    GameIconButton(
        modifier = modifier,
        resId = R.drawable.left_button_icon_noburn,
        desc = "Move left button",
        onPress = { controls?.leftButtonPressed() },
        onRelease = { controls?.leftButtonReleased() },
    )
}

@Composable
fun MoveRightButton(modifier: Modifier = Modifier) {
    val controls = LocalControls.current
    GameIconButton(
        modifier = modifier,
        resId = R.drawable.right_button_icon_noburn,
        desc = "Move right button",
        onPress = { controls?.rightButtonPressed() },
        onRelease = { controls?.rightButtonReleased() },
    )
}

@Composable
fun RotateLeftButton(modifier: Modifier = Modifier) {
    val controls = LocalControls.current
    GameIconButton(
        modifier = modifier,
        resId = R.drawable.rotate_left_button_icon_noburn,
        desc = "Rotate left button",
        onPress = { controls?.rotateLeftPressed() },
        onRelease = { controls?.rotateLeftReleased() },
    )
}

@Composable
fun RotateRightButton(modifier: Modifier = Modifier) {
    val controls = LocalControls.current
    GameIconButton(
        modifier = modifier,
        resId = R.drawable.rotate_right_button_icon_noburn,
        desc = "Rotate right button",
        onPress = { controls?.rotateRightPressed() },
        onRelease = { controls?.rotateRightReleased() },
    )
}

@Composable
fun SoftDropButton(modifier: Modifier = Modifier) {
    val controls = LocalControls.current
    GameIconButton(
        modifier = modifier,
        resId = R.drawable.soft_drop_button_icon_noburn,
        desc = "Soft drop button",
        onPress = { controls?.downButtonPressed() },
        onRelease = { controls?.downButtonReleased() },
    )
}

@Composable
fun HardDropButton(modifier: Modifier = Modifier) {
    val controls = LocalControls.current
    GameIconButton(
        modifier = modifier,
        resId = R.drawable.hard_drop_button_icon_noburn,
        desc = "Hard drop button",
        onPress = { controls?.dropButtonPressed() },
        onRelease = { controls?.dropButtonReleased() },
    )
}

