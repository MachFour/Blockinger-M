package org.blockinger.game.activities

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@Composable
private fun Test() {
    val interactionSource = remember { MutableInteractionSource() }
    val indicationNodeFactory = remember { GameButtonPressIndicationNodeFactory(
        bgBrush = Brush.verticalGradient(listOf(Color.Red, Color.Blue)),
        borderColour = Color.LightGray,
        cornerRadius = 4.dp,
    ) }
    Box(modifier = Modifier.clickable(
        onClick = {},
        interactionSource = interactionSource,
        indication = indicationNodeFactory,
    )) {
        Text("Clickable box")
    }
}

private data class GameButtonPressIndicationNodeFactory(
    val bgBrush: Brush,
    val borderColour: Color = Color.Unspecified,
    val borderWidth: Dp = 0.dp,
    val cornerRadius: Dp = 0.dp,
    val animator: AnimationSpec<Float> = snap()
): IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return GameButtonPressIndicationNode(
            interactionSource = interactionSource,
            bgBrush = bgBrush,
            borderColour = borderColour,
            borderWidth = borderWidth,
            cornerRadius = cornerRadius,
            animator = animator,
        )
    }
}

private class GameButtonPressIndicationNode(
    private val interactionSource: InteractionSource,
    private val bgBrush: Brush,
    private val borderColour: Color,
    private val borderWidth: Dp,
    private val cornerRadius: Dp,
    private val animator: AnimationSpec<Float>,
) : Modifier.Node(), DrawModifierNode {
    var currentPressPosition: Offset = Offset.Zero
    val animatedIndicationPercent = Animatable(0f)

    private suspend fun animateToPressed(pressPosition: Offset) {
        currentPressPosition = pressPosition
        animatedIndicationPercent.animateTo(1f, animator)
    }

    private suspend fun animateToResting() {
        animatedIndicationPercent.animateTo(0f, animator)
    }

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collectLatest { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> animateToPressed(interaction.pressPosition)
                    is PressInteraction.Release -> animateToResting()
                    is PressInteraction.Cancel -> animateToResting()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        val cornerRadiusPx = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
        drawRoundRect(
            brush = bgBrush,
            cornerRadius = cornerRadiusPx,
            alpha = animatedIndicationPercent.value,
        )
        if (borderWidth > 0.dp && borderColour.let { it.isSpecified && it != Color.Transparent }) {
            drawRoundRect(
                color = borderColour,
                cornerRadius = cornerRadiusPx,
                style = Stroke(width = borderWidth.toPx()),
                alpha = animatedIndicationPercent.value
            )
        }
        drawContent()
    }
}
