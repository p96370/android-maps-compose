import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


/**
 * A composable that displays a progress bar with an animated gradient.
 *
 * @param progress The current progress value (0f to 1f).
 * @param modifier The modifier to be applied to the progress bar.
 * @param colors The list of colors used in the animated gradient.
 * @param trackBrush An optional brush for the track line behind the progress.
 * @param strokeWidth The width of the progress bar and track line (in Dp).
 * @param glowRadius An optional Dp value for adding a glow effect.
 * @param strokeCap The cap style for the ends of the progress bar.
 * @param gradientAnimationSpeed The duration (milliseconds) for the gradient animation loop.
 * @param progressAnimSpec The animation spec for the progress line animation.
 */
@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier,
    colors: List<Color>,
    trackBrush: Brush? = SolidColor(Color.Black.copy(0.16f)),
    strokeWidth: Dp = 4.dp,
    glowRadius: Dp? = 4.dp,
    strokeCap: StrokeCap = StrokeCap.Round,
    gradientAnimationSpeed: Int = 2500,
    progressAnimSpec: AnimationSpec<Float> = tween(
        durationMillis = 720,
        easing = LinearOutSlowInEasing
    )
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = progressAnimSpec
    )

    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = gradientAnimationSpeed,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush: ShaderBrush = remember(offset) {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                val step = 1f / colors.size
                val start = step / 2

                val originalSpots = List(colors.size) { start + (step * it) }
                val transformedSpots = originalSpots.map { spot ->
                    val shiftedSpot = (spot + offset)
                    if (shiftedSpot > 1f) shiftedSpot - 1f else shiftedSpot
                }

                val pairs = colors.zip(transformedSpots).sortedBy { it.second }

                val margin = size.width / 2
                return LinearGradientShader(
                    colors = pairs.map { it.first },
                    colorStops = pairs.map { it.second },
                    from = Offset(-margin, 0f),
                    to = Offset(size.width + margin, 0f)
                )
            }
        }
    }

    Canvas(modifier) {
        val width = this.size.width
        val height = this.size.height

        val paint = Paint().apply {
            this.isAntiAlias = true
            this.style = PaintingStyle.Stroke
            this.strokeWidth = strokeWidth.toPx()
            this.strokeCap = strokeCap
            this.shader = brush.createShader(size)
        }

        glowRadius?.let { radius ->
            paint.asFrameworkPaint().apply {
                setShadowLayer(radius.toPx(), 0f, 0f, android.graphics.Color.WHITE)
            }
        }

        trackBrush?.let { tBrush ->
            drawLine(
                brush = tBrush,
                start =  Offset(0f, height / 2f),
                end = Offset(width, height / 2f),
                cap = strokeCap,
                strokeWidth = strokeWidth.toPx()
            )
        }

        if (animatedProgress > 0f) {
            drawIntoCanvas { canvas ->
                canvas.drawLine(
                    p1 = Offset(0f, height / 2f),
                    p2 = Offset(width * animatedProgress, height / 2f),
                    paint = paint
                )
            }
        }
    }
}