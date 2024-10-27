package org.blockinger.game.activities

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import org.blockinger.game.BlockBoardView
import org.blockinger.game.components.Controls
import org.blockinger.game.components.GameState

private const val TAG = "GameUi"

val LocalGameState = staticCompositionLocalOf<GameState?> {
    Log.e(TAG, "LocalGameState not initialised with value")
    null
}

val LocalControls = staticCompositionLocalOf<Controls?> {
    Log.e(TAG, "LocalControls not initialised with value")
    null
}

fun makeComposeUiView(c: Context, g: GameState, ctls: Controls, swapLayout: Boolean): ComposeView {
    val mp = ViewGroup.LayoutParams.MATCH_PARENT
    return ComposeView(c).apply {
        layoutParams = ViewGroup.LayoutParams(mp, mp)
        setContent {
            CompositionLocalProvider(
                LocalGameState provides g,
                LocalControls provides ctls,
            ) {
                GameUi(Modifier.fillMaxSize(), swapLayout)
            }
        }
    }
}

data class GameUiGuidelinePositions(
    val controlsTop: Float,
    val controlsUpperMid: Float,
    val controlsLowerMid: Float,
    val controlsBottom: Float,
    val controlsLeft: Float,
    val controlsRight: Float,

    val boardTop: Float,
    val boardBottom: Float,
    val boardLeft: Float,
    val boardRight: Float,
)

val NarrowLayoutGuidelinePositions = GameUiGuidelinePositions(
    controlsTop = 0.60f,
    controlsUpperMid = 0.83f,
    controlsLowerMid = 0.85f,
    controlsBottom = 0.96f,
    controlsLeft = 0.38f,
    controlsRight = 0.62f,
    boardTop = 0.0f,
    boardBottom = 0.6f,
    boardLeft = 0.0f,
    boardRight = 1.0f
)

val WideLayoutGuidelinePositions = GameUiGuidelinePositions(
    controlsTop = 0.17f,
    controlsUpperMid = 0.60f,
    controlsLowerMid = 0.64f,
    controlsBottom = 0.89f,
    controlsLeft = 0.24f,
    controlsRight = 0.76f,
    boardTop = 0.0f,
    boardBottom = 1.0f,
    boardLeft = 0.24f,
    boardRight = 0.76f
)

@Composable
fun GameUi(modifier: Modifier = Modifier, hardDropOnLeft: Boolean) {
    // TODO improve logic?
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val wide = screenWidth.dp >= 600.dp

    Scaffold(
        containerColor = Color.Transparent,
    ) { padding ->
        if (wide) {
        } else {
            NarrowGameUi(
                hardDropOnLeft = hardDropOnLeft,
                modifier = modifier.padding(padding)
            )
        }
    }
}

@Composable
fun NarrowGameUi(
    hardDropOnLeft: Boolean,
    modifier: Modifier = Modifier,
) {
    val guidelinePositions = NarrowLayoutGuidelinePositions

    ConstraintLayout(modifier) {
        val controlsTopGuideline = createGuidelineFromTop(guidelinePositions.controlsTop)
        val controlsUpperMidGuideline = createGuidelineFromTop(guidelinePositions.controlsUpperMid)
        val controlsLowerMidGuideline = createGuidelineFromTop(guidelinePositions.controlsLowerMid)
        val controlsBottomGuideline = createGuidelineFromTop(guidelinePositions.controlsBottom)
        val controlsLeftGuideline = createGuidelineFromAbsoluteLeft(guidelinePositions.controlsLeft)
        val controlsRightGuideline = createGuidelineFromAbsoluteLeft(guidelinePositions.controlsRight)

        val boardTopGuideline = createGuidelineFromTop(guidelinePositions.boardTop)
        val boardBottomGuideline = createGuidelineFromTop(guidelinePositions.boardBottom)
        val boardLeftGuideline = createGuidelineFromAbsoluteLeft(guidelinePositions.boardLeft)
        val boardRightGuideline = createGuidelineFromAbsoluteLeft(guidelinePositions.boardRight)

        val boardView = createRef()
        val (moveLeftButton, moveRightButton) = createRefs()
        val (rotateLeftButton, rotateRightButton) = createRefs()
        val (softDropButton, hardDropButton) = createRefs()
        val pauseButton = createRef()

        MoveLeftButton(
            modifier = Modifier.constrainAs(moveLeftButton) {
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
                absoluteLeft.linkTo(parent.absoluteLeft, 4.dp)
                absoluteRight.linkTo(moveRightButton.absoluteLeft, 4.dp)
                horizontalChainWeight = 0.5f
                top.linkTo(controlsTopGuideline, 4.dp)
                bottom.linkTo(controlsUpperMidGuideline, 4.dp)
            },
        )
        MoveRightButton(
            modifier = Modifier.constrainAs(moveRightButton) {
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
                absoluteLeft.linkTo(moveLeftButton.absoluteRight, 4.dp)
                absoluteRight.linkTo(controlsLeftGuideline, 4.dp)
                horizontalChainWeight = 0.5f
                top.linkTo(controlsTopGuideline, 4.dp)
                bottom.linkTo(controlsUpperMidGuideline, 4.dp)
            },
        )
        RotateLeftButton(
            modifier = Modifier.constrainAs(rotateLeftButton) {
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
                absoluteLeft.linkTo(controlsRightGuideline, 4.dp)
                absoluteRight.linkTo(rotateRightButton.absoluteLeft, 4.dp)
                horizontalChainWeight = 0.5f
                top.linkTo(controlsTopGuideline, 4.dp)
                bottom.linkTo(controlsUpperMidGuideline, 4.dp)
            },
        )
        RotateRightButton(
            modifier = Modifier.constrainAs(rotateRightButton) {
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
                absoluteLeft.linkTo(rotateLeftButton.absoluteRight, 4.dp)
                absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                horizontalChainWeight = 0.5f
                top.linkTo(controlsTopGuideline, 4.dp)
                bottom.linkTo(controlsUpperMidGuideline, 4.dp)
            },
        )
        SoftDropButton(
            modifier = Modifier.constrainAs(softDropButton) {
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
                if (hardDropOnLeft) {
                    absoluteLeft.linkTo(controlsRightGuideline, 4.dp)
                    absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                } else {
                    absoluteLeft.linkTo(parent.absoluteLeft, 4.dp)
                    absoluteRight.linkTo(controlsLeftGuideline, 4.dp)
                }
                top.linkTo(controlsLowerMidGuideline, 4.dp)
                bottom.linkTo(controlsBottomGuideline, 4.dp)
            },
        )
        HardDropButton(
            modifier = Modifier.constrainAs(hardDropButton) {
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
                if (hardDropOnLeft) {
                    absoluteLeft.linkTo(parent.absoluteLeft, 4.dp)
                    absoluteRight.linkTo(controlsLeftGuideline, 4.dp)
                } else {
                    absoluteLeft.linkTo(controlsRightGuideline, 4.dp)
                    absoluteRight.linkTo(parent.absoluteRight, 4.dp)
                }
                top.linkTo(controlsLowerMidGuideline, 4.dp)
                bottom.linkTo(controlsBottomGuideline, 4.dp)
            },
        )


        PauseButton(
            modifier = Modifier.height(48.dp).constrainAs(pauseButton) {
                width = Dimension.fillToConstraints
                top.linkTo(controlsTopGuideline, 4.dp)
                absoluteLeft.linkTo(controlsLeftGuideline, 4.dp)
                absoluteRight.linkTo(controlsRightGuideline, 4.dp)
            }
        )

        Box(Modifier.constrainAs(boardView) {
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
                absoluteLeft.linkTo(boardLeftGuideline)
                absoluteRight.linkTo(boardRightGuideline)
                top.linkTo(boardTopGuideline)
                bottom.linkTo(boardBottomGuideline)
        }) {
            BoardView(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun BoardView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val activity = context.findActivity()
            BlockBoardView(context).apply {
                if (activity != null) {
                    init()
                    setHost(activity)
                    Log.d(TAG, "initialised BlockBoardView")
                } else {
                    Log.e(TAG, "could not initialise BlockBoardView: could not find GameActivity")
                }
            }
        },
        onRelease = { v ->
            v.holder.removeCallback(v)
            v.setHost(null)
        },
        onReset = NoOpUpdate,
    )
}

@Composable
fun GameUiPreviewHelper(isAltLayout: Boolean) {
    Surface(
        color = Color.Black,
        contentColor = Color.White,
    ) {
        GameUi(
            modifier = Modifier.fillMaxSize(),
            hardDropOnLeft = isAltLayout,
        )
    }
}

@Preview
@Composable
fun GameUiPreviewAlt() {
    GameUiPreviewHelper(true)
}

@Preview
@Composable
fun GameUiPreview() {
    GameUiPreviewHelper(false)
}

internal tailrec fun Context.findActivity(): GameActivity? =
    when (this) {
        is Activity -> this as GameActivity
        is ContextWrapper -> this.baseContext.findActivity()
        else -> {
            Log.e(TAG, "Could not find activity! (this is ${this.javaClass}")
            null
        }
    }

