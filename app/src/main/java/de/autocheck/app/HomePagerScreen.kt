
package de.autocheck.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CarvitaGold = Color(0xFFD4AF37)
private val CarvitaBackground = Color(0xFF101010)

@Composable
fun HomePagerScreen(
    visible: Boolean,
    visitedStore: VisitedStore,
    introEnabled: Boolean,
    onIntroEnabledChange: (Boolean) -> Unit,
    onSelect: (Screen) -> Unit
) {
    val context = LocalContext.current

    val vehicleStore = remember {
        VehicleStore(context)
    }

    val menuStore = remember {
        HomeMenuLayoutStore(context)
    }

    val menuCatalog = remember {
        listOf(
            HomeMenuItem(
                id = "fuel",
                title = "Tanken & Verbrauch",
                contentDescription = "Tank-Icon",
                iconRes = R.drawable.tank_icoon,
                action = "Tank und Kilometer – Verbrauch"
            ),
            HomeMenuItem(
                id = "history",
                title = "Fahrzeughistorie",
                contentDescription = "Fahrzeughistorie-Icon",
                iconRes = R.drawable.fahrzeughistorie_icoon,
                action = "Fahrzeughistorie"
            ),
            HomeMenuItem(
                id = "pdf",
                title = "PDF & Dokumente",
                contentDescription = "PDF-Icon",
                iconRes = R.drawable.pdf_icoon,
                action = "PDF & Dokumente"
            ),
            HomeMenuItem(
                id = "contract",
                title = "CarLoca Kaufvertrag",
                contentDescription = "CarLoca Kaufvertrag",
                iconRes = R.drawable.kaufvertrag_icoon,
                action = "Kaufvertrag"
            ),
            HomeMenuItem(
                id = "plans",
                title = "Geplante Vorhaben",
                contentDescription = "Geplante Vorhaben",
                iconRes = R.drawable.geplantevorhaben_icoon,
                action = "Geplante Vorhaben"
            ),
            HomeMenuItem(
                id = "tuning",
                title = "Tuning und Extras",
                contentDescription = "Tuning und Extras",
                iconRes = R.drawable.tuningextras_icoon,
                action = "Tuning und Extras"
            ),
            HomeMenuItem(
                id = "electric",
                title = "Elektro und Verbrauch",
                contentDescription = "Elektro und Verbrauch",
                iconRes = R.drawable.elektro_icoon,
                action = "Elektro und Verbrauch"
            ),
            HomeMenuItem(
                id = "calendar",
                title = "Kalender",
                contentDescription = "Kalender",
                iconRes = R.drawable.kalender_icoon,
                action = "Kalender"
            )
        )
    }

    val menuItems = remember {
        mutableStateListOf<HomeMenuItem>().apply {
            val catalogById = menuCatalog.associateBy { it.id }

            menuStore.load().forEach { id ->
                catalogById[id]?.let { add(it) }
            }

            menuCatalog.forEach { item ->
                if (none { it.id == item.id }) {
                    add(item)
                }
            }
        }
    }

    var currentPage by remember {
        mutableStateOf(0)
    }

    var editMenu by remember {
        mutableStateOf(false)
    }

    var selectedAction by remember {
        mutableStateOf<String?>(null)
    }

    var pageFourSubmenu by remember {
        mutableStateOf<String?>(null)
    }

    var showFuelScreen by remember {
        mutableStateOf(false)
    }

    var showHistoryScreen by remember {
        mutableStateOf(false)
    }

    var showPdfScreen by remember {
        mutableStateOf(false)
    }

    var showPurchaseContractScreen by remember {
        mutableStateOf(false)
    }

    var showPremiumDialog by remember {
        mutableStateOf(false)
    }

    fun moveMenuItem(from: Int, to: Int) {
        if (
            from !in menuItems.indices ||
            to !in menuItems.indices ||
            from == to
        ) {
            return
        }

        val movedItem = menuItems.removeAt(from)
        menuItems.add(to, movedItem)

        menuStore.save(menuItems.map { it.id })
    }

    fun resetMenu() {
        menuStore.reset()
        menuItems.clear()
        menuItems.addAll(menuCatalog)
    }

    fun openMenuAction(action: String) {
        when (action) {
            "Tank und Kilometer – Verbrauch" -> {
                showFuelScreen = true
            }

            "Fahrzeughistorie" -> {
                showHistoryScreen = true
            }

            "PDF & Dokumente" -> {
                showPdfScreen = true
            }

            "Kaufvertrag" -> {
                showPurchaseContractScreen = true
            }

            else -> {
                selectedAction = action
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarvitaBackground)
            .pointerInput(
                currentPage,
                showFuelScreen,
                showHistoryScreen,
                showPdfScreen,
                showPurchaseContractScreen,
                showPremiumDialog,
                selectedAction,
                pageFourSubmenu,
                editMenu
            ) {
                var dragDistance = 0f

                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragDistance += dragAmount
                    },
                    onDragEnd = {
                        val overlayOpen =
                            showFuelScreen ||
                            showHistoryScreen ||
                            showPdfScreen ||
                            showPurchaseContractScreen ||
                            showPremiumDialog ||
                            selectedAction != null ||
                            pageFourSubmenu != null ||
                            editMenu

                        if (!overlayOpen) {
                            if (dragDistance < -70f) {
                                currentPage =
                                    (currentPage + 1).coerceAtMost(3)
                            } else if (dragDistance > 70f) {
                                currentPage =
                                    (currentPage - 1).coerceAtLeast(0)
                            }
                        }

                        dragDistance = 0f
                    },
                    onDragCancel = {
                        dragDistance = 0f
                    }
                )
            }
    ) {
        when {
            showFuelScreen -> {
                FuelConsumptionScreen(
                    onBack = {
                        showFuelScreen = false
                    }
                )
            }

            showHistoryScreen -> {
                val vehicles = remember {
                    vehicleStore.load()
                }

                VehicleHistoryScreen(
                    store = vehicleStore,
                    vehicles = vehicles,
                    activeVehicleId = vehicleStore.activeVehicleId(),
                    onBack = {
                        showHistoryScreen = false
                    }
                )
            }

            showPdfScreen -> {
                val vehicles = remember {
                    vehicleStore.load()
                }

                PdfDocumentsScreen(
                    store = vehicleStore,
                    vehicles = vehicles,
                    activeVehicleId = vehicleStore.activeVehicleId(),
                    onBack = {
                        showPdfScreen = false
                    }
                )
            }

            showPurchaseContractScreen -> {
                PurchaseContractScreen(
                    onBack = {
                        showPurchaseContractScreen = false
                    }
                )
            }

            pageFourSubmenu == "UNFALLBERICHT" -> {
                AccidentReportScreen(
                    onBack = {
                        pageFourSubmenu = null
                    }
                )
            }

            pageFourSubmenu == "DIGITALES HANDSCHUHFACH" -> {
                DigitalGloveboxGate(
                    onBack = {
                        pageFourSubmenu = null
                    }
                )
            }

            pageFourSubmenu != null -> {
                PageFourSubmenuScreen(
                    title = pageFourSubmenu!!,
                    onBack = {
                        pageFourSubmenu = null
                    }
                )
            }

            editMenu -> {
                HomeMenuEditor(
                    items = menuItems.toList(),
                    onMove = { from, to ->
                        moveMenuItem(from, to)
                    },
                    onReset = {
                        resetMenu()
                    },
                    onDone = {
                        editMenu = false
                    }
                )
            }

            currentPage == 0 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 42.dp)
                ) {
                    HomeScreen(
                        visible = visible,
                        visitedStore = visitedStore,
                        introEnabled = introEnabled,
                        onIntroEnabledChange = onIntroEnabledChange,
                        onSelect = onSelect,
                        onPremiumClick = {
                            showPremiumDialog = true
                        }
                    )
                }
            }

            currentPage == 1 -> {
                HomeMenuPageContent(
                    visible = visible,
                    imageRes = R.drawable.menuebild_seite2,
                    heading = "DEINE FAHRZEUGE. ALLES IM BLICK.",
                    items = menuItems.take(4),
                    onAction = { action ->
                        openMenuAction(action)
                    },
                    onEdit = {
                        editMenu = true
                    },
                    onLogoClick = {
                        currentPage = 0
                    }
                )
            }

            currentPage == 2 -> {
                HomeMenuPageContent(
                    visible = visible,
                    imageRes = R.drawable.menuebild_seite3,
                    heading = "WEITERE FAHRZEUGFUNKTIONEN",
                    items = menuItems.drop(4),
                    onAction = { action ->
                        openMenuAction(action)
                    },
                    onEdit = {
                        editMenu = true
                    },
                    onLogoClick = {
                        currentPage = 0
                    }
                )
            }

            currentPage == 3 -> {
                PageFourContent(
                    onAccidentReport = {
                        pageFourSubmenu = "UNFALLBERICHT"
                    },
                    onDigitalGlovebox = {
                        pageFourSubmenu = "DIGITALES HANDSCHUHFACH"
                    },
                    onLogoClick = {
                        currentPage = 0
                    }
                )
            }
        }

        if (
            !showFuelScreen &&
            !showHistoryScreen &&
            !showPdfScreen &&
            !showPurchaseContractScreen &&
            selectedAction == null &&
            pageFourSubmenu == null &&
            !editMenu
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(CarvitaBackground)
                    .navigationBarsPadding()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPage > 0) {
                        PageNavigationButton(
                            title = "ZURÜCK",
                            arrow = "‹",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                currentPage =
                                    (currentPage - 1).coerceAtLeast(0)
                            }
                        )
                    }

                    if (currentPage < 3) {
                        PageNavigationButton(
                            title = "WEITERE FUNKTIONEN",
                            arrow = "›",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                currentPage =
                                    (currentPage + 1).coerceAtMost(3)
                            }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { page ->
                        Box(
                            modifier = Modifier
                                .size(
                                    if (currentPage == page) 9.dp else 7.dp
                                )
                                .clip(CircleShape)
                                .background(
                                    if (currentPage == page) {
                                        CarvitaGold
                                    } else {
                                        Color.LightGray
                                    }
                                )
                                .clickable {
                                    currentPage = page
                                }
                        )
                    }

                    Text(
                        text = "Seite ${currentPage + 1} von 4",
                        color = Color(0xFFBBBBBB),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

    selectedAction?.let { action ->
        when (action) {
            "Kalender" -> {
                VehicleCalendarScreen(
                    onBack = {
                        selectedAction = null
                    }
                )
            }

            "Elektro und Verbrauch" -> {
                ElectricConsumptionScreen(
                    onBack = {
                        selectedAction = null
                    }
                )
            }

            else -> {
                VehicleFeatureScreen(
                    featureName = action,
                    onBack = {
                        selectedAction = null
                    }
                )
            }
        }
    }

    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = {
                showPremiumDialog = false
            },
            title = {
                Text(
                    text = "CARLOCA Premium",
                    color = CarvitaGold
                )
            },
            text = {
                Text(
                    "Werde CARLOCA-Premiummitglied und entdecke " +
                        "zusätzliche Funktionen. Die Mitgliedschaft " +
                        "und Bezahlung müssen noch eingerichtet werden."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPremiumDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPremiumDialog = false
                    }
                ) {
                    Text("Schließen")
                }
            }
        )
    }
}

@Composable
private fun CarLocaHomeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF181818)
        ),
        border = BorderStroke(
            1.dp,
            CarvitaGold.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bild_4),
                contentDescription = "CARLOCA – Zurück zur Startseite",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun PageNavigationButton(
    title: String,
    arrow: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(38.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF29251A)
        ),
        border = BorderStroke(1.dp, CarvitaGold),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (arrow == "‹") {
                Text(
                    text = arrow,
                    color = CarvitaGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(5.dp))
            }

            Text(
                text = title,
                color = CarvitaGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (arrow == "›") {
                Spacer(Modifier.width(5.dp))
                Text(
                    text = arrow,
                    color = CarvitaGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HomeMenuPageContent(
    visible: Boolean,
    imageRes: Int,
    heading: String,
    items: List<HomeMenuItem>,
    onAction: (String) -> Unit,
    onEdit: () -> Unit,
    onLogoClick: () -> Unit
) {
    val isThirdPage = heading == "WEITERE FAHRZEUGFUNKTIONEN"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(
                start = 18.dp,
                end = 18.dp,
                top = 8.dp,
                bottom = 150.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            if (isThirdPage) 5.dp else 6.dp
        )
    ) {
        Spacer(Modifier.height(2.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            )
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "CARLOCA Menü",
                modifier = if (isThirdPage) {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                },
                contentScale = if (isThirdPage) {
                    ContentScale.Crop
                } else {
                    ContentScale.FillBounds
                }
            )
        }

        Text(
            text = heading,
            color = CarvitaGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 2.dp)
        )

        val topRowItems = items.take(2)
        val bottomRowItems = items.drop(2).take(2)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            topRowItems.forEach { item ->
                ThirdPageButton(
                    modifier = Modifier.weight(1f),
                    iconRes = item.iconRes,
                    title = item.title,
                    contentDescription = item.contentDescription,
                    onClick = {
                        onAction(item.action)
                    },
                    onLongClick = onEdit
                )
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(
                animationSpec = tween(2000),
                initialOffsetX = { fullWidth -> -fullWidth }
            )
        ) {
            CarLocaHomeButton(
                onClick = onLogoClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            bottomRowItems.forEach { item ->
                ThirdPageButton(
                    modifier = Modifier.weight(1f),
                    iconRes = item.iconRes,
                    title = item.title,
                    contentDescription = item.contentDescription,
                    onClick = {
                        onAction(item.action)
                    },
                    onLongClick = onEdit
                )
            }
        }
    }
}

@Composable
private fun PageFourContent(
    onAccidentReport: () -> Unit,
    onDigitalGlovebox: () -> Unit,
    onLogoClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(
                start = 18.dp,
                end = 18.dp,
                top = 14.dp,
                bottom = 112.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "CARLOCA EXTRAS",
            color = CarvitaGold,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(2.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .clickable(onClick = onAccidentReport),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            ),
            border = BorderStroke(1.5.dp, CarvitaGold),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.unfallbericht
                ),
                contentDescription = "Unfallbericht öffnen",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        CarLocaHomeButton(
            onClick = onLogoClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .clickable(onClick = onDigitalGlovebox),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            ),
            border = BorderStroke(1.5.dp, CarvitaGold),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.digitales_handschuhfach
                ),
                contentDescription = "Digitales Handschuhfach öffnen",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun PageFourSubmenuScreen(
    title: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 24.dp,
                bottom = 16.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = CarvitaGold,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Hier werden später die weiteren Funktionen eingebaut.",
            color = Color.White,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))

        PageNavigationButton(
            title = "ZURÜCK",
            arrow = "‹",
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            onClick = onBack
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ThirdPageButton(
    modifier: Modifier = Modifier,
    iconRes: Int,
    title: String,
    contentDescription: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xE6222222)
        ),
        border = BorderStroke(
            1.dp,
            CarvitaGold.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(76.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(7.dp))

            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = "Öffnen ›",
                color = CarvitaGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
