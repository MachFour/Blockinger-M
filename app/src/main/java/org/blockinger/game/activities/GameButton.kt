package org.blockinger.game.activities

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private val pressedBackground = Brush.verticalGradient(
    colors = listOf(
        // #1a7bff
        Color.hsv(215f, 0.89f, 1f),
        // #017aff
        Color.hsv(211f, 1f, 1f),
        // #2bcaff
        Color.hsv(195f, 0.83f, 1f),
    )
)
private val nonPressedBorder = BorderStroke(2.dp, Color.DarkGray)
private val pressedBorder = BorderStroke(2.dp, Color.LightGray)


@Composable
fun GameIconButton(
    modifier: Modifier,
    @DrawableRes resId: Int,
    desc: String,
    onRelease: (() -> Unit)? = null,
    onPress: () -> Unit,
) {
    GameButton(
        modifier = modifier,
        onPress = onPress,
        onRelease = onRelease,
    ) {
        Image(
            bitmap = ImageBitmap.imageResource(resId),
            contentDescription = desc,
            contentScale = ContentScale.Inside,
            modifier = it
        )
    }
}

@Composable
fun GameButton(
    modifier: Modifier = Modifier,
    onRelease: (() -> Unit)? = null,
    onPress: () -> Unit,
    content: @Composable (modifier: Modifier) -> Unit,
) {
    val shape = RoundedCornerShape(4.dp)
    val hoverBackground = Color.LightGray.copy(alpha = 0.20f)
    val focusBackground = Color.LightGray.copy(alpha = 0.33f)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> { onPress() }
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    onRelease?.invoke()
                }
            }
        }
    }

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(
            propagateMinConstraints = true,
            modifier = modifier
                .semantics { role = Role.Button }
                .minimumInteractiveComponentSize()
                .then(
                    if (isPressed) {
                        Modifier
                            .border(pressedBorder, shape = shape)
                            .background(pressedBackground, shape = shape)
                    } else if (isFocused) {
                        Modifier
                            .border(nonPressedBorder, shape = shape)
                            .background(focusBackground, shape = shape)
                    } else if (isHovered) {
                        Modifier
                            .border(nonPressedBorder, shape = shape)
                            .background(hoverBackground, shape = shape)
                    } else {
                        Modifier.border(nonPressedBorder, shape = shape)
                    }
                )
                .clip(shape)
                .hoverable(interactionSource = interactionSource)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    //indication = indicationNodeFactory,
                ) {  }
        ) {
            content(Modifier
                .defaultMinSize(
                    minWidth = ButtonDefaults.MinWidth,
                    minHeight = ButtonDefaults.MinHeight
                )
                .padding(paddingValues = PaddingValues(4.dp))
            )
        }
    }
}

