
@Composable
private fun CarvitaSheetIcon(
    title: String,
    modifier: Modifier = Modifier
) {
    val bitmap = androidx.compose.ui.res.imageResource(
        id = R.drawable.bild_5
    )

    val crop = when (title) {
        "Mein Auto" -> 0 to 0
        "Reparaturen" -> 1 to 0
        "Pickerl/TÜV" -> 2 to 0
        "Wartungen" -> 0 to 1
        "Service und Intervalle" -> 1 to 1
        "Reifen" -> 2 to 1
        else -> 0 to 0
    }

    val cellWidth = bitmap.width / 3
    val cellHeight = bitmap.height / 2

    val left = crop.first * cellWidth
    val top = crop.second * cellHeight

    val sourceWidth = cellWidth
    val sourceHeight = (cellHeight * 0.78f).toInt()

    Canvas(modifier = modifier) {
        drawImage(
            image = bitmap,
            srcOffset = androidx.compose.ui.unit.IntOffset(
                left,
                top
            ),
            srcSize = androidx.compose.ui.unit.IntSize(
                sourceWidth,
                sourceHeight
            ),
            dstOffset = androidx.compose.ui.unit.IntOffset.Zero,
            dstSize = androidx.compose.ui.unit.IntSize(
                size.width.toInt(),
                size.height.toInt()
            ),
            filterQuality = androidx.compose.ui.graphics.FilterQuality.High
        )
    }
}
