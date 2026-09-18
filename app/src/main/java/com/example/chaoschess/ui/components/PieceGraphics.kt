package com.example.chaoschess.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chaoschess.engine.models.Element
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.engine.models.PieceType
import com.example.ui.theme.CelestialCyan
import com.example.ui.theme.DragonCrimson
import com.example.ui.theme.DragonFire
import com.example.ui.theme.EmeraldArcane
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MysticPurple
import com.example.ui.theme.MysticPurpleLight

/**
 * Subtle ivory and ebony materials for readable tournament-style pieces.
 */
private data class PieceMaterial(
    val bodyBrush: (Float, Float) -> Brush,
    val highlightBrush: (Float, Float) -> Brush,
    val shadowColor: Color,
    val rimColor: Color,
    val trimColor: Color,
    val innerDarkColor: Color
)

private fun getPieceMaterial(owner: PieceColor): PieceMaterial {
    return if (owner == PieceColor.WHITE) {
        PieceMaterial(
            bodyBrush = { w, h ->
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF3F1EA),
                        Color(0xFFE2DED3),
                        Color(0xFFC9C4B8)
                    ),
                    start = Offset(w * 0.2f, h * 0.1f),
                    end = Offset(w * 0.85f, h * 0.9f)
                )
            },
            highlightBrush = { w, h ->
                Brush.radialGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0x00FFFFFF)),
                    center = Offset(w * 0.38f, h * 0.22f),
                    radius = w * 0.45f
                )
            },
            shadowColor = Color(0xFF777268),
            rimColor = Color(0xFFFFFFFF),
            trimColor = Color(0xFFB6B0A5),
            innerDarkColor = Color(0xFF8C877E)
        )
    } else {
        PieceMaterial(
            bodyBrush = { w, h ->
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF686763),
                        Color(0xFF454441),
                        Color(0xFF302F2C),
                        Color(0xFF1C1B19)
                    ),
                    start = Offset(w * 0.2f, h * 0.1f),
                    end = Offset(w * 0.85f, h * 0.9f)
                )
            },
            highlightBrush = { w, h ->
                Brush.radialGradient(
                    colors = listOf(Color(0x997F7E79), Color(0x001C1B19)),
                    center = Offset(w * 0.35f, h * 0.22f),
                    radius = w * 0.45f
                )
            },
            shadowColor = Color(0xFF11110F),
            rimColor = Color(0xFF85837E),
            trimColor = Color(0xFF242321),
            innerDarkColor = Color(0xFF171614)
        )
    }
}

@Composable
fun ChessPieceView(
    pieceType: PieceType,
    owner: PieceColor,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isChampion: Boolean = false,
    isMystery: Boolean = false,
    isFrozen: Boolean = false,
    isDragon: Boolean = (pieceType == PieceType.DRAGON),
    element: Element = Element.NONE,
    isClone: Boolean = false,
    hasResurrected: Boolean = false
) {
    val material = getPieceMaterial(owner)

    // Smooth spring animation for tactile piece pickup / selection
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pieceScale"
    )

    val animatedElevation by animateFloatAsState(
        targetValue = if (isSelected) -4f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pieceElevation"
    )

    Box(
        modifier = modifier
            .scale(animatedScale)
            .offset(y = animatedElevation.dp),
        contentAlignment = Alignment.Center
    ) {
        val accentGlow = when {
            isDragon -> DragonCrimson
            isChampion -> GoldPrimary
            isFrozen -> CelestialCyan
            hasResurrected -> MysticPurple
            element == Element.FIRE -> DragonFire
            element == Element.ICE -> CelestialCyan
            element == Element.LIGHTNING -> GoldAccent
            element == Element.NATURE -> EmeraldArcane
            else -> null
        }

        Canvas(modifier = Modifier.fillMaxSize().padding(2.dp)) {
            val w = size.width
            val h = size.height

            // 1. Ambient Drop Shadow (Scales with elevation when selected)
            val shadowAlpha = if (isSelected) 0x8A000000.toInt() else 0x50000000
            val shadowYOffset = if (isSelected) h * 0.95f else h * 0.91f
            val shadowRadius = if (isSelected) w * 0.48f else w * 0.40f

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(shadowAlpha), Color(0x00000000)),
                    center = Offset(w * 0.5f, shadowYOffset),
                    radius = shadowRadius
                ),
                topLeft = Offset(w * 0.10f, h * 0.82f + (if (isSelected) 4f else 0f)),
                size = Size(w * 0.80f, h * 0.16f)
            )

            // 2. Render Realistic High-Contrast Staunton Piece
            when (pieceType) {
                PieceType.PAWN -> drawRealisticPawn(w, h, material)
                PieceType.KNIGHT -> drawRealisticKnight(w, h, material)
                PieceType.BISHOP -> drawRealisticBishop(w, h, material)
                PieceType.ROOK -> drawRealisticRook(w, h, material)
                PieceType.QUEEN -> drawRealisticQueen(w, h, material)
                PieceType.KING -> drawRealisticKing(w, h, material)
                PieceType.DRAGON -> drawRealisticDragon(w, h, material)
            }

            // 3. Accent Aura & Magical Glow Rings
            if (accentGlow != null) {
                drawCircle(
                    color = accentGlow,
                    radius = w * 0.45f,
                    style = Stroke(width = 2.5f)
                )
            }
        }

        // Overlay Badges
        if (isMystery) {
            Text(
                text = "?",
                color = MysticPurpleLight,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
        if (isChampion) {
            Text(
                text = "★",
                color = GoldAccent,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.TopStart)
            )
        }
        if (isFrozen) {
            Text(
                text = "❄",
                color = CelestialCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
        if (element != Element.NONE) {
            Text(
                text = element.badge,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
        if (isClone) {
            Text(
                text = "🧬",
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
        if (hasResurrected) {
            Text(
                text = "⚰️",
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

/**
 * Realistic Staunton Pawn:
 * Spherical dome head with 3D specular reflection, grooved collar ring, fluted waist, and weighted pedestal.
 */
private fun DrawScope.drawRealisticPawn(w: Float, h: Float, mat: PieceMaterial) {
    val bodyPath = Path().apply {
        moveTo(w * 0.20f, h * 0.88f)
        cubicTo(w * 0.20f, h * 0.84f, w * 0.24f, h * 0.80f, w * 0.30f, h * 0.77f)
        cubicTo(w * 0.38f, h * 0.64f, w * 0.40f, h * 0.48f, w * 0.38f, h * 0.42f)
        lineTo(w * 0.62f, h * 0.42f)
        cubicTo(w * 0.60f, h * 0.48f, w * 0.62f, h * 0.64f, w * 0.70f, h * 0.77f)
        cubicTo(w * 0.76f, h * 0.80f, w * 0.80f, h * 0.84f, w * 0.80f, h * 0.88f)
        close()
    }

    // Base & Body
    drawPath(bodyPath, mat.bodyBrush(w, h))
    drawPath(bodyPath, mat.shadowColor, style = Stroke(width = 1.6f, join = StrokeJoin.Round))

    // Tiered Base Rings
    drawRoundRect(
        brush = mat.bodyBrush(w, h),
        topLeft = Offset(w * 0.22f, h * 0.82f),
        size = Size(w * 0.56f, h * 0.06f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = mat.trimColor.copy(alpha = 0.7f),
        topLeft = Offset(w * 0.22f, h * 0.82f),
        size = Size(w * 0.56f, h * 0.06f),
        style = Stroke(width = 1.2f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )

    // Molded Collar Torus
    drawRoundRect(
        brush = mat.bodyBrush(w, h),
        topLeft = Offset(w * 0.33f, h * 0.38f),
        size = Size(w * 0.34f, h * 0.065f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
    )
    drawRoundRect(
        color = mat.trimColor,
        topLeft = Offset(w * 0.33f, h * 0.38f),
        size = Size(w * 0.34f, h * 0.065f),
        style = Stroke(width = 1.2f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
    )

    // Spherical Pawn Head with 3D Specular Highlight
    val headCenter = Offset(w * 0.50f, h * 0.23f)
    val headRadius = w * 0.17f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(mat.rimColor, mat.innerDarkColor),
            center = Offset(w * 0.44f, h * 0.18f),
            radius = headRadius * 1.3f
        ),
        radius = headRadius,
        center = headCenter
    )
    drawCircle(
        brush = mat.highlightBrush(w, h),
        radius = headRadius * 0.65f,
        center = Offset(w * 0.45f, h * 0.19f)
    )
    drawCircle(
        color = mat.shadowColor,
        radius = headRadius,
        center = headCenter,
        style = Stroke(width = 1.6f)
    )
}

/**
 * Realistic Staunton Knight:
 * Hand-carved tournament stallion with muscular neck arch, flowing mane tufts, alert ears, nostril and eye contour.
 */
private fun DrawScope.drawRealisticKnight(w: Float, h: Float, mat: PieceMaterial) {
    // Pedestal Base
    val basePath = Path().apply {
        moveTo(w * 0.18f, h * 0.88f)
        lineTo(w * 0.82f, h * 0.88f)
        cubicTo(w * 0.80f, h * 0.82f, w * 0.74f, h * 0.79f, w * 0.70f, h * 0.78f)
        lineTo(w * 0.30f, h * 0.78f)
        cubicTo(w * 0.26f, h * 0.79f, w * 0.20f, h * 0.82f, w * 0.18f, h * 0.88f)
        close()
    }
    drawPath(basePath, mat.bodyBrush(w, h))
    drawPath(basePath, mat.trimColor.copy(alpha = 0.8f), style = Stroke(width = 1.4f))

    // Realistic Stallion Silhouette
    val horsePath = Path().apply {
        moveTo(w * 0.28f, h * 0.78f)
        // Chest & throat
        cubicTo(w * 0.28f, h * 0.68f, w * 0.34f, h * 0.58f, w * 0.38f, h * 0.50f)
        // Lower jaw
        lineTo(w * 0.26f, h * 0.45f)
        // Muzzle & nose
        cubicTo(w * 0.22f, h * 0.39f, w * 0.24f, h * 0.33f, w * 0.30f, h * 0.31f)
        lineTo(w * 0.46f, h * 0.22f)
        // Forehead & Ears
        lineTo(w * 0.50f, h * 0.13f) // Ear tip
        lineTo(w * 0.56f, h * 0.19f) // Ear base
        lineTo(w * 0.58f, h * 0.14f) // Back ear
        lineTo(w * 0.63f, h * 0.21f)
        // Sculpted Mane Waves
        cubicTo(w * 0.76f, h * 0.32f, w * 0.78f, h * 0.48f, w * 0.74f, h * 0.62f)
        cubicTo(w * 0.72f, h * 0.70f, w * 0.72f, h * 0.75f, w * 0.72f, h * 0.78f)
        close()
    }
    drawPath(horsePath, mat.bodyBrush(w, h))
    drawPath(horsePath, mat.shadowColor, style = Stroke(width = 1.8f, join = StrokeJoin.Round))

    // Mane Tufts Carving
    val manePath = Path().apply {
        moveTo(w * 0.60f, h * 0.27f)
        cubicTo(w * 0.70f, h * 0.33f, w * 0.73f, h * 0.42f, w * 0.65f, h * 0.47f)
        moveTo(w * 0.62f, h * 0.45f)
        cubicTo(w * 0.72f, h * 0.50f, w * 0.73f, h * 0.60f, w * 0.66f, h * 0.65f)
    }
    drawPath(manePath, mat.trimColor.copy(alpha = 0.7f), style = Stroke(width = 2.0f, cap = StrokeCap.Round))

    // Stallion Eye & Nostril
    drawCircle(
        color = mat.trimColor,
        radius = w * 0.032f,
        center = Offset(w * 0.44f, h * 0.29f)
    )
    drawCircle(
        color = Color.White,
        radius = w * 0.012f,
        center = Offset(w * 0.435f, h * 0.285f)
    )
    // Nostril
    drawOval(
        color = mat.innerDarkColor,
        topLeft = Offset(w * 0.29f, h * 0.35f),
        size = Size(w * 0.04f, h * 0.025f)
    )
    // Jaw muscle contour
    val jawLine = Path().apply {
        moveTo(w * 0.38f, h * 0.48f)
        cubicTo(w * 0.46f, h * 0.44f, w * 0.48f, h * 0.38f, w * 0.46f, h * 0.34f)
    }
    drawPath(jawLine, mat.shadowColor, style = Stroke(width = 1.4f))
}

/**
 * Realistic Staunton Bishop:
 * Carved mitre with sharp cleft slit, spherical top pommel, fluted neck rings, and broad flared base.
 */
private fun DrawScope.drawRealisticBishop(w: Float, h: Float, mat: PieceMaterial) {
    val bodyPath = Path().apply {
        moveTo(w * 0.20f, h * 0.88f)
        lineTo(w * 0.80f, h * 0.88f)
        cubicTo(w * 0.76f, h * 0.82f, w * 0.70f, h * 0.78f, w * 0.66f, h * 0.75f)
        cubicTo(w * 0.58f, h * 0.62f, w * 0.58f, h * 0.52f, w * 0.62f, h * 0.47f)
        lineTo(w * 0.38f, h * 0.47f)
        cubicTo(w * 0.42f, h * 0.52f, w * 0.42f, h * 0.62f, w * 0.34f, h * 0.75f)
        cubicTo(w * 0.30f, h * 0.78f, w * 0.24f, h * 0.82f, w * 0.20f, h * 0.88f)
        close()
    }
    drawPath(bodyPath, mat.bodyBrush(w, h))
    drawPath(bodyPath, mat.shadowColor, style = Stroke(width = 1.6f, join = StrokeJoin.Round))

    // Base Rings
    drawRoundRect(
        brush = mat.bodyBrush(w, h),
        topLeft = Offset(w * 0.24f, h * 0.81f),
        size = Size(w * 0.52f, h * 0.055f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )

    // Dual Neck Collars
    drawRoundRect(
        brush = mat.bodyBrush(w, h),
        topLeft = Offset(w * 0.34f, h * 0.44f),
        size = Size(w * 0.32f, h * 0.05f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = mat.trimColor,
        topLeft = Offset(w * 0.34f, h * 0.44f),
        size = Size(w * 0.32f, h * 0.05f),
        style = Stroke(width = 1.2f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )

    // Mitre Oval Head with Realistic Depth
    val mitrePath = Path().apply {
        moveTo(w * 0.50f, h * 0.16f)
        cubicTo(w * 0.72f, h * 0.22f, w * 0.72f, h * 0.40f, w * 0.50f, h * 0.44f)
        cubicTo(w * 0.28f, h * 0.40f, w * 0.28f, h * 0.22f, w * 0.50f, h * 0.16f)
        close()
    }
    drawPath(mitrePath, mat.bodyBrush(w, h))
    drawPath(mitrePath, mat.shadowColor, style = Stroke(width = 1.8f))

    // Iconic Staunton Mitre Cleft (Angled Slit)
    val slitPath = Path().apply {
        moveTo(w * 0.42f, h * 0.23f)
        lineTo(w * 0.58f, h * 0.35f)
        lineTo(w * 0.54f, h * 0.37f)
        lineTo(w * 0.38f, h * 0.25f)
        close()
    }
    drawPath(slitPath, mat.innerDarkColor)
    drawLine(
        color = mat.trimColor,
        start = Offset(w * 0.42f, h * 0.23f),
        end = Offset(w * 0.58f, h * 0.35f),
        strokeWidth = 2.0f
    )

    // Top Pommel Spherical Finial
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(mat.rimColor, mat.trimColor),
            center = Offset(w * 0.48f, h * 0.13f),
            radius = w * 0.06f
        ),
        radius = w * 0.045f,
        center = Offset(w * 0.50f, h * 0.14f)
    )
    drawCircle(color = mat.shadowColor, radius = w * 0.045f, center = Offset(w * 0.50f, h * 0.14f), style = Stroke(1.2f))
}

/**
 * Realistic Staunton Rook:
 * Castle tower with 4 crenellations, interior parapet shelf, machicolated corbels, and ashlar stonework.
 */
private fun DrawScope.drawRealisticRook(w: Float, h: Float, mat: PieceMaterial) {
    val rookPath = Path().apply {
        // Base
        moveTo(w * 0.16f, h * 0.88f)
        lineTo(w * 0.84f, h * 0.88f)
        cubicTo(w * 0.80f, h * 0.82f, w * 0.74f, h * 0.78f, w * 0.72f, h * 0.76f)
        // Tower Body Trunk (Tapered)
        lineTo(w * 0.68f, h * 0.40f)
        // Flared Machicolation Corbel
        lineTo(w * 0.78f, h * 0.36f)
        lineTo(w * 0.78f, h * 0.19f)
        // Crenels / Embrasures
        lineTo(w * 0.67f, h * 0.19f)
        lineTo(w * 0.67f, h * 0.27f)
        lineTo(w * 0.57f, h * 0.27f)
        lineTo(w * 0.57f, h * 0.19f)
        lineTo(w * 0.43f, h * 0.19f)
        lineTo(w * 0.43f, h * 0.27f)
        lineTo(w * 0.33f, h * 0.27f)
        lineTo(w * 0.33f, h * 0.19f)
        lineTo(w * 0.22f, h * 0.19f)
        lineTo(w * 0.22f, h * 0.36f)
        lineTo(w * 0.32f, h * 0.40f)
        lineTo(w * 0.28f, h * 0.76f)
        cubicTo(w * 0.26f, h * 0.78f, w * 0.20f, h * 0.82f, w * 0.16f, h * 0.88f)
        close()
    }
    drawPath(rookPath, mat.bodyBrush(w, h))
    drawPath(rookPath, mat.shadowColor, style = Stroke(width = 1.8f, join = StrokeJoin.Miter))

    // Crenellation Interior Parapet Recess
    val parapetPath = Path().apply {
        moveTo(w * 0.24f, h * 0.27f)
        lineTo(w * 0.76f, h * 0.27f)
        lineTo(w * 0.76f, h * 0.30f)
        lineTo(w * 0.24f, h * 0.30f)
        close()
    }
    drawPath(parapetPath, mat.innerDarkColor.copy(alpha = 0.7f))

    // Corbel Molding Ring
    drawRoundRect(
        color = mat.trimColor,
        topLeft = Offset(w * 0.24f, h * 0.37f),
        size = Size(w * 0.52f, h * 0.04f),
        style = Stroke(1.4f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
    )

    // Ashlar Masonry Vertical Center Seam
    drawLine(
        color = mat.shadowColor.copy(alpha = 0.5f),
        start = Offset(w * 0.50f, h * 0.44f),
        end = Offset(w * 0.50f, h * 0.72f),
        strokeWidth = 1.4f
    )

    // Base Plinth Ring
    drawRoundRect(
        brush = mat.bodyBrush(w, h),
        topLeft = Offset(w * 0.20f, h * 0.81f),
        size = Size(w * 0.60f, h * 0.06f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = mat.trimColor,
        topLeft = Offset(w * 0.20f, h * 0.81f),
        size = Size(w * 0.60f, h * 0.06f),
        style = Stroke(1.2f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
}

/**
 * Realistic Staunton Queen:
 * Royal 9-pointed coronet arches with polished pearl finials, velvet cap shadow, hourglass waist, and tiered pedestal.
 */
private fun DrawScope.drawRealisticQueen(w: Float, h: Float, mat: PieceMaterial) {
    val queenPath = Path().apply {
        // Base
        moveTo(w * 0.16f, h * 0.88f)
        lineTo(w * 0.84f, h * 0.88f)
        cubicTo(w * 0.80f, h * 0.82f, w * 0.74f, h * 0.77f, w * 0.70f, h * 0.75f)
        // Hourglass Waist
        cubicTo(w * 0.62f, h * 0.64f, w * 0.58f, h * 0.52f, w * 0.65f, h * 0.44f)
        // Flared Royal Coronet
        lineTo(w * 0.86f, h * 0.24f)
        lineTo(w * 0.71f, h * 0.35f)
        lineTo(w * 0.59f, h * 0.21f)
        lineTo(w * 0.50f, h * 0.33f)
        lineTo(w * 0.41f, h * 0.21f)
        lineTo(w * 0.29f, h * 0.35f)
        lineTo(w * 0.14f, h * 0.24f)
        lineTo(w * 0.35f, h * 0.44f)
        cubicTo(w * 0.42f, h * 0.52f, w * 0.38f, h * 0.64f, w * 0.30f, h * 0.75f)
        cubicTo(w * 0.26f, h * 0.77f, w * 0.20f, h * 0.82f, w * 0.16f, h * 0.88f)
        close()
    }
    drawPath(queenPath, mat.bodyBrush(w, h))
    drawPath(queenPath, mat.shadowColor, style = Stroke(width = 1.8f, join = StrokeJoin.Round))

    // Velvet Inner Cap Shading
    val velvetCap = Path().apply {
        moveTo(w * 0.26f, h * 0.40f)
        cubicTo(w * 0.38f, h * 0.30f, w * 0.62f, h * 0.30f, w * 0.74f, h * 0.40f)
        cubicTo(w * 0.62f, h * 0.46f, w * 0.38f, h * 0.46f, w * 0.26f, h * 0.40f)
        close()
    }
    drawPath(velvetCap, mat.innerDarkColor.copy(alpha = 0.8f))

    // Pearl / Gem Finials on Coronet Spikes
    val pearls = listOf(
        Offset(w * 0.14f, h * 0.23f),
        Offset(w * 0.41f, h * 0.20f),
        Offset(w * 0.50f, h * 0.15f), // Center imperial pearl
        Offset(w * 0.59f, h * 0.20f),
        Offset(w * 0.86f, h * 0.23f)
    )
    pearls.forEach { pt ->
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, mat.trimColor),
                center = Offset(pt.x - 2f, pt.y - 2f),
                radius = w * 0.045f
            ),
            radius = w * 0.035f,
            center = pt
        )
        drawCircle(color = mat.shadowColor, radius = w * 0.035f, center = pt, style = Stroke(1.0f))
    }

    // Jewel Waist Band
    drawRoundRect(
        color = mat.trimColor,
        topLeft = Offset(w * 0.32f, h * 0.47f),
        size = Size(w * 0.36f, h * 0.045f),
        style = Stroke(1.4f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    drawRoundRect(
        brush = mat.bodyBrush(w, h),
        topLeft = Offset(w * 0.20f, h * 0.80f),
        size = Size(w * 0.60f, h * 0.06f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = mat.trimColor,
        topLeft = Offset(w * 0.20f, h * 0.80f),
        size = Size(w * 0.60f, h * 0.06f),
        style = Stroke(1.2f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
}

/**
 * Realistic Staunton King:
 * Regal Maltese Crown Cross with center jewel facet, globus cruciger, crenellated arches, broad regal torso and heavy base.
 */
private fun DrawScope.drawRealisticKing(w: Float, h: Float, mat: PieceMaterial) {
    val kingPath = Path().apply {
        // Base
        moveTo(w * 0.15f, h * 0.88f)
        lineTo(w * 0.85f, h * 0.88f)
        cubicTo(w * 0.80f, h * 0.82f, w * 0.74f, h * 0.76f, w * 0.70f, h * 0.74f)
        // Stately Broad Torso
        cubicTo(w * 0.62f, h * 0.60f, w * 0.60f, h * 0.46f, w * 0.68f, h * 0.38f)
        // Regal Crown Arches
        cubicTo(w * 0.64f, h * 0.26f, w * 0.56f, h * 0.24f, w * 0.50f, h * 0.25f)
        cubicTo(w * 0.44f, h * 0.24f, w * 0.36f, h * 0.26f, w * 0.32f, h * 0.38f)
        cubicTo(w * 0.40f, h * 0.46f, w * 0.38f, h * 0.60f, w * 0.30f, h * 0.74f)
        cubicTo(w * 0.26f, h * 0.76f, w * 0.20f, h * 0.82f, w * 0.15f, h * 0.88f)
        close()
    }
    drawPath(kingPath, mat.bodyBrush(w, h))
    drawPath(kingPath, mat.shadowColor, style = Stroke(width = 1.8f, join = StrokeJoin.Round))

    // Crown Velvet Inner Dome
    val domePath = Path().apply {
        moveTo(w * 0.34f, h * 0.36f)
        cubicTo(w * 0.42f, h * 0.28f, w * 0.58f, h * 0.28f, w * 0.66f, h * 0.36f)
        cubicTo(w * 0.58f, h * 0.42f, w * 0.42f, h * 0.42f, w * 0.34f, h * 0.36f)
        close()
    }
    drawPath(domePath, mat.innerDarkColor.copy(alpha = 0.85f))

    // Royal Orb (Globus Cruciger)
    val orbCenter = Offset(w * 0.50f, h * 0.20f)
    val orbRadius = w * 0.05f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, mat.trimColor),
            center = Offset(orbCenter.x - 2f, orbCenter.y - 2f),
            radius = orbRadius * 1.4f
        ),
        radius = orbRadius,
        center = orbCenter
    )
    drawCircle(color = mat.shadowColor, radius = orbRadius, center = orbCenter, style = Stroke(1.0f))

    // Majestic Maltese Cross Finial
    val crossY = h * 0.12f
    // Vertical beam
    drawLine(
        color = mat.trimColor,
        start = Offset(w * 0.50f, crossY - h * 0.065f),
        end = Offset(w * 0.50f, crossY + h * 0.055f),
        strokeWidth = 3.2f,
        cap = StrokeCap.Square
    )
    // Horizontal cross bar
    drawLine(
        color = mat.trimColor,
        start = Offset(w * 0.41f, crossY - h * 0.01f),
        end = Offset(w * 0.59f, crossY - h * 0.01f),
        strokeWidth = 3.2f,
        cap = StrokeCap.Square
    )
    // Center diamond glint
    drawCircle(
        color = Color.White,
        radius = 2.5f,
        center = Offset(w * 0.50f, crossY - h * 0.01f)
    )

    // Base Rings
    drawRoundRect(
        brush = mat.bodyBrush(w, h),
        topLeft = Offset(w * 0.18f, h * 0.79f),
        size = Size(w * 0.64f, h * 0.065f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = mat.trimColor,
        topLeft = Offset(w * 0.18f, h * 0.79f),
        size = Size(w * 0.64f, h * 0.065f),
        style = Stroke(1.4f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
}

/**
 * Realistic Dragon (Chaos Mode Special):
 * Draconic beast matching Staunton proportions with winged silhouette, horned head, and glowing eye.
 */
private fun DrawScope.drawRealisticDragon(w: Float, h: Float, mat: PieceMaterial) {
    val dragonPath = Path().apply {
        moveTo(w * 0.18f, h * 0.88f)
        lineTo(w * 0.82f, h * 0.88f)
        cubicTo(w * 0.80f, h * 0.80f, w * 0.74f, h * 0.76f, w * 0.72f, h * 0.75f)
        // Wing ridge
        cubicTo(w * 0.90f, h * 0.56f, w * 0.84f, h * 0.35f, w * 0.66f, h * 0.25f)
        // Horns
        lineTo(w * 0.74f, h * 0.12f)
        lineTo(w * 0.57f, h * 0.18f)
        lineTo(w * 0.60f, h * 0.09f)
        lineTo(w * 0.46f, h * 0.18f)
        // Fanged Snout
        lineTo(w * 0.20f, h * 0.28f)
        lineTo(w * 0.25f, h * 0.38f)
        lineTo(w * 0.38f, h * 0.38f)
        // Wing Spur
        lineTo(w * 0.26f, h * 0.52f)
        lineTo(w * 0.44f, h * 0.50f)
        // Scaled Chest
        cubicTo(w * 0.36f, h * 0.64f, w * 0.28f, h * 0.72f, w * 0.24f, h * 0.78f)
        close()
    }
    drawPath(dragonPath, mat.bodyBrush(w, h))
    drawPath(dragonPath, DragonCrimson.copy(alpha = 0.9f), style = Stroke(width = 2.0f, join = StrokeJoin.Round))

    // Glowing Dragon Eye
    drawCircle(DragonCrimson, radius = w * 0.045f, center = Offset(w * 0.42f, h * 0.26f))
    drawCircle(Color(0xFFFFEE55), radius = w * 0.020f, center = Offset(w * 0.41f, h * 0.255f))
}
