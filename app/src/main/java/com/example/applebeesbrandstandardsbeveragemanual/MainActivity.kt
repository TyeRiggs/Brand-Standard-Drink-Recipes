package com.example.applebeesbrandstandardsbeveragemanual

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// Custom Bar Color Palette
val DeepCharcoal = Color(0xFF1E1E24)  // Sidebar background
val OffBlack = Color(0xFF121214)      // Main recipe background
val CardDark = Color(0xFF2A2A32)      // Unselected drink items
val SelectionGold = Color(0xFFFBC02D) // Active drink / Starred header
val AccentOrange = Color(0xFFFF7043)  // Liquor menu highlights
val TextPrimary = Color(0xFFF5F5F7)   // Bright text for dark background
val TextMuted = Color(0xFF9E9E9E)     // Gray text for secondary details

data class DrinkRecipe(
    val name: String,
    val nameEs: String? = null,          // Enterprise Localized Variable
    val category: List<String>,
    val subCategory: List<String>,
    val baseSpirit: String? = null,
    val specificBrand: String? = null,
    val glassType: String? = null,
    val glassTypeEs: String? = null,     // Enterprise Localized Variable
    val ingredients: List<String>,
    val ingredientsEs: List<String>? = null, // Enterprise Localized Variable
    val instructions: String,
    val instructionsEs: String? = null,  // Enterprise Localized Variable
    val garnish: String? = null,
    val garnishEs: String? = null,        // Enterprise Localized Variable

    // Optional layout fields parsed directly from your Bar Mixes metadata strings
    val ratio: String? = null,
    val container: String? = null,
    val colorCode: String? = null,
    val shelfLife: String? = null
)

// ==========================================
// RETROFIT NETWORK INTERFACE
// ==========================================
interface MenuApiService {
    @GET("TyeRiggs/d0c439e1302610704bb3c8a1ca35c1ba/raw/drinks.json")
    suspend fun getLiveMenu(): List<DrinkRecipe>
}

object NetworkClient {
    private val customGson = com.google.gson.GsonBuilder()
        .registerTypeAdapter(DrinkRecipe::class.java, com.google.gson.JsonDeserializer { json, _, _ ->
            val obj = json.asJsonObject

            val categoryList = mutableListOf<String>()
            val categoryElement = obj.get("category")
            if (categoryElement != null && !categoryElement.isJsonNull) {
                if (categoryElement.isJsonArray) {
                    categoryElement.asJsonArray.forEach { categoryList.add(it.asString) }
                } else if (categoryElement.isJsonPrimitive) {
                    categoryList.add(categoryElement.asString)
                }
            }

            val subCategoryList = mutableListOf<String>()
            val subCategoryElement = obj.get("subCategory")
            if (subCategoryElement != null && !subCategoryElement.isJsonNull) {
                if (subCategoryElement.isJsonArray) {
                    subCategoryElement.asJsonArray.forEach { subCategoryList.add(it.asString) }
                } else if (subCategoryElement.isJsonPrimitive) {
                    subCategoryList.add(subCategoryElement.asString)
                }
            }

            val ingredientsList = mutableListOf<String>()
            val ingredientsElement = obj.get("ingredients")
            if (ingredientsElement != null && !ingredientsElement.isJsonNull && ingredientsElement.isJsonArray) {
                ingredientsElement.asJsonArray.forEach { ingredientsList.add(it.asString) }
            }

            val ingredientsEsList = mutableListOf<String>()
            val ingredientsEsElement = obj.get("ingredients_es")
            if (ingredientsEsElement != null && !ingredientsEsElement.isJsonNull && ingredientsEsElement.isJsonArray) {
                ingredientsEsElement.asJsonArray.forEach { ingredientsEsList.add(it.asString) }
            }

            DrinkRecipe(
                name = if (obj.has("name") && !obj.get("name").isJsonNull) obj.get("name").asString else "",
                nameEs = if (obj.has("name_es") && !obj.get("name_es").isJsonNull) obj.get("name_es").asString else null,
                category = categoryList,
                subCategory = subCategoryList,
                baseSpirit = if (obj.has("baseSpirit") && !obj.get("baseSpirit").isJsonNull) obj.get("baseSpirit").asString else null,
                specificBrand = if (obj.has("specificBrand") && !obj.get("specificBrand").isJsonNull) obj.get("specificBrand").asString else null,
                glassType = if (obj.has("glassType") && !obj.get("glassType").isJsonNull) obj.get("glassType").asString else null,
                glassTypeEs = if (obj.has("glassType_es") && !obj.get("glassType_es").isJsonNull) obj.get("glassType_es").asString else null,
                ingredients = ingredientsList,
                ingredientsEs = if (ingredientsEsList.isNotEmpty()) ingredientsEsList else null,
                instructions = if (obj.has("instructions") && !obj.get("instructions").isJsonNull) obj.get("instructions").asString else "",
                instructionsEs = if (obj.has("instructions_es") && !obj.get("instructions_es").isJsonNull) obj.get("instructions_es").asString else null,
                garnish = if (obj.has("garnish") && !obj.get("garnish").isJsonNull) obj.get("garnish").asString else null,
                garnishEs = if (obj.has("garnish_es") && !obj.get("garnish_es").isJsonNull) obj.get("garnish_es").asString else null,
                ratio = if (obj.has("ratio") && !obj.get("ratio").isJsonNull) obj.get("ratio").asString else null,
                container = if (obj.has("container") && !obj.get("container").isJsonNull) obj.get("container").asString else null,
                colorCode = if (obj.has("colorCode") && !obj.get("colorCode").isJsonNull) obj.get("colorCode").asString else null,
                shelfLife = if (obj.has("shelfLife") && !obj.get("shelfLife").isJsonNull) obj.get("shelfLife").asString else null
            )
        })
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://gist.githubusercontent.com/")
        .addConverterFactory(GsonConverterFactory.create(customGson))
        .build()

    val apiService: MenuApiService = retrofit.create(MenuApiService::class.java)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = OffBlack,
                    surface = DeepCharcoal,
                    primary = SelectionGold
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = OffBlack) {
                    BarRecipeScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarRecipeScreen() {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("BarAppPrefs", Context.MODE_PRIVATE) }

    // Persistent Localization State
    var isSpanish by remember { mutableStateOf(sharedPreferences.getBoolean("app_lang_es", false)) }

    var liveRecipes by remember { mutableStateOf<List<DrinkRecipe>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var networkError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response = withContext(Dispatchers.IO) { NetworkClient.apiService.getLiveMenu() }
            liveRecipes = response
            isLoading = false
        } catch (e: Exception) {
            networkError = e.localizedMessage ?: "Failed to connect to menu server."
            isLoading = false
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedRecipe by remember { mutableStateOf<DrinkRecipe?>(null) }
    var favoriteDrinks by remember { mutableStateOf(sharedPreferences.getStringSet("favorites", emptySet()) ?: emptySet()) }

    var isFavoritesFolderExpanded by remember { mutableStateOf(false) }
    var expandedCategories by remember { mutableStateOf(setOf<String>()) }
    var expandedSubCategories by remember { mutableStateOf(setOf<String>()) }
    var isLiquorFolderExpanded by remember { mutableStateOf(false) }
    var expandedBaseSpirits by remember { mutableStateOf(setOf<String>()) }
    var expandedBrands by remember { mutableStateOf(setOf<String>()) }

    // Enterprise UI String Localization Matrix
    val uiSearchLabel = if (isSpanish) "Buscar receta..." else "Search menu..."
    val uiFavorites = if (isSpanish) "⭐ Favoritos" else "⭐ Favorites"
    val uiNoFavorites = if (isSpanish) "No hay favoritos guardados." else "No quick-access favorites pinned."
    val uiSearchTitle = if (isSpanish) "RESULTADOS DE BÚSQUEDA" else "SEARCH RESULTS"
    val uiNoResults = if (isSpanish) "No se encontró ninguna bebida." else "No matching drink or bottle found."
    val uiLiquorFolder = if (isSpanish) "🥃 Buscar Por Licor" else "🥃 Search By Liquor"
    val uiDefaultSelection = if (isSpanish) "Seleccione una categoría o licor para ver las especificaciones." else "Select a category or liquor lookup to view specs."
    val uiIngredientsHeader = if (isSpanish) "Ingredientes" else "Ingredients"
    val uiInstructionsHeader = if (isSpanish) "Instrucciones de Preparación" else "Build Instructions"
    val uiPrepRulesHeader = if (isSpanish) "Reglas de Preparación y Almacenamiento" else "Prep & Storage Rules"
    val uiLoadingText = if (isSpanish) "Descargando Manual de Bebidas en Vivo..." else "Downloading Live Beverage Manual..."

    val standardCategories = listOf(
        "Monthly Drinks", "Martinis", "Standard Drinks", "Shooters", "Muchos",
        "Margaritas", "Frozen Drinks", "Hot Drinks",
        "Ice Cream Drinks", "Wine Drinks", "Non-Alcoholic Drinks", "Bar Mixes"
    )

    // Smarter Search: Queries both language names simultaneously
    val filteredRecipes = liveRecipes.filter { recipe ->
        recipe.name.contains(searchQuery, ignoreCase = true) ||
                (recipe.nameEs?.contains(searchQuery, ignoreCase = true) == true)
    }

    val isSearching = searchQuery.trim().isNotEmpty()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = SelectionGold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(uiLoadingText, color = TextPrimary, fontSize = 16.sp)
            }
        }
    } else if (networkError != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text("⚠️ Network Error", color = AccentOrange, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(networkError ?: "", color = TextMuted, fontSize = 16.sp, modifier = Modifier.padding(bottom = 16.dp))
                Button(
                    onClick = { isLoading = true; networkError = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SelectionGold)
                ) {
                    Text(if (isSpanish) "Reintentar Conexión" else "Retry Connection", color = OffBlack)
                }
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxSize()) {

            // ==========================================
            // LEFT SIDEBAR: PURE NAVIGATION TREE
            // ==========================================
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
                    .background(DeepCharcoal)
                    .padding(16.dp)
                    .border(width = (0.5).dp, color = Color(0xFF2D2D34), shape = RoundedCornerShape(0.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MANUAL", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp)

                    Row(
                        modifier = Modifier
                            .background(Color(0xFF121214), RoundedCornerShape(24.dp))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(if (!isSpanish) SelectionGold else Color.Transparent, RoundedCornerShape(24.dp))
                                .clickable {
                                    isSpanish = false
                                    sharedPreferences.edit().putBoolean("app_lang_es", false).apply()
                                }
                                .padding(vertical = 4.dp, horizontal = 10.dp)
                        ) {
                            Text("EN", color = if (!isSpanish) OffBlack else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .background(if (isSpanish) SelectionGold else Color.Transparent, RoundedCornerShape(24.dp))
                                .clickable {
                                    isSpanish = true
                                    sharedPreferences.edit().putBoolean("app_lang_es", true).apply()
                                }
                                .padding(vertical = 4.dp, horizontal = 10.dp)
                        ) {
                            Text("ES", color = if (isSpanish) OffBlack else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(uiSearchLabel) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SelectionGold,
                        unfocusedBorderColor = Color(0xFF3A3A45),
                        focusedLabelColor = SelectionGold,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isSearching) {
                        item {
                            Text(
                                text = "$uiSearchTitle (${filteredRecipes.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelectionGold,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                                letterSpacing = 1.sp
                            )
                        }
                        items(filteredRecipes) { drink ->
                            DrinkMenuCard(drink, selectedRecipe, isSpanish) { selectedRecipe = drink }
                        }
                        if (filteredRecipes.isEmpty()) {
                            item { Text(uiNoResults, fontSize = 14.sp, color = TextMuted, modifier = Modifier.padding(8.dp)) }
                        }
                    } else {
                        // 1. ⭐ FAVORITES FOLDER
                        val favoriteRecipesList = filteredRecipes.filter { favoriteDrinks.contains(it.name) }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { isFavoritesFolderExpanded = !isFavoritesFolderExpanded }.padding(vertical = 14.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(uiFavorites, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SelectionGold)
                                Icon(imageVector = if (isFavoritesFolderExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = SelectionGold)
                            }
                            AnimatedVisibility(visible = isFavoritesFolderExpanded) {
                                Column(modifier = Modifier.padding(start = 12.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    favoriteRecipesList.forEach { drink ->
                                        DrinkMenuCard(drink, selectedRecipe, isSpanish) { selectedRecipe = drink }
                                    }
                                    if (favoriteRecipesList.isEmpty()) {
                                        Text(uiNoFavorites, fontSize = 14.sp, color = TextMuted, modifier = Modifier.padding(8.dp))
                                    }
                                }
                            }
                            HorizontalDivider(color = Color(0xFF2D2D34), thickness = 1.dp)
                        }

                        // 2. STANDARD ACCORDION CATEGORIES WITH NESTED CAMPAIGNS
                        items(standardCategories) { categoryName ->
                            val isCategoryExpanded = expandedCategories.contains(categoryName)
                            val categoryDrinks = liveRecipes.filter { it.category.contains(categoryName) }

                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { expandedCategories = if (isCategoryExpanded) expandedCategories - categoryName else expandedCategories + categoryName }.padding(vertical = 14.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Structural sidebar root folder labels remain 100% English
                                Text(categoryName, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Icon(imageVector = if (isCategoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextMuted)
                            }

                            AnimatedVisibility(visible = isCategoryExpanded) {
                                Column(modifier = Modifier.padding(start = 12.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (categoryName == "Margaritas") {
                                        listOf("Rocks", "Frozen").forEach { subCat ->
                                            val isSubExpanded = expandedSubCategories.contains(subCat)
                                            val subCatDrinks = categoryDrinks.filter { it.subCategory.contains(subCat) }

                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().clickable { expandedSubCategories = if (isSubExpanded) expandedSubCategories - subCat else expandedSubCategories + subCat }.padding(vertical = 8.dp, horizontal = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Subfolders remain strictly English
                                                    Text(subCat, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextMuted)
                                                    Icon(imageVector = if (isSubExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                                                }
                                                AnimatedVisibility(visible = isSubExpanded) {
                                                    Column(modifier = Modifier.padding(start = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        subCatDrinks.forEach { drink -> DrinkMenuCard(drink, selectedRecipe, isSpanish) { selectedRecipe = drink } }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else if (categoryName == "Monthly Drinks") {
                                        listOf("Current Campaign", "Previous Campaigns").forEach { subCat ->
                                            val isSubExpanded = expandedSubCategories.contains(subCat)
                                            val subCatDrinks = categoryDrinks.filter { it.subCategory.contains(subCat) }

                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().clickable { expandedSubCategories = if (isSubExpanded) expandedSubCategories - subCat else expandedSubCategories + subCat }.padding(vertical = 8.dp, horizontal = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Campaign subfolders remain strictly English
                                                    val folderColor = if (subCat == "Current Campaign") SelectionGold else TextMuted
                                                    Text(subCat, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = folderColor)
                                                    Icon(imageVector = if (isSubExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = folderColor, modifier = Modifier.size(18.dp))
                                                }
                                                AnimatedVisibility(visible = isSubExpanded) {
                                                    Column(modifier = Modifier.padding(start = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        subCatDrinks.forEach { drink -> DrinkMenuCard(drink, selectedRecipe, isSpanish) { selectedRecipe = drink } }
                                                        if (subCatDrinks.isEmpty()) {
                                                            Text(if (isSpanish) "No hay recetas guardadas." else "No recipes stored in this folder.", fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(8.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        categoryDrinks.forEach { drink -> DrinkMenuCard(drink, selectedRecipe, isSpanish) { selectedRecipe = drink } }
                                    }
                                }
                            }
                            HorizontalDivider(color = Color(0xFF2D2D34), thickness = 1.dp)
                        }

                        // 3. 🥃 SEARCH BY LIQUOR FOLDER
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { isLiquorFolderExpanded = !isLiquorFolderExpanded }.padding(vertical = 14.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(uiLiquorFolder, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentOrange)
                                Icon(imageVector = if (isLiquorFolderExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = AccentOrange)
                            }

                            AnimatedVisibility(visible = isLiquorFolderExpanded) {
                                Column(modifier = Modifier.padding(start = 12.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val availableSpirits = liveRecipes.mapNotNull { it.baseSpirit }.distinct().sorted()
                                    availableSpirits.forEach { spiritType ->
                                        val isSpiritExpanded = expandedBaseSpirits.contains(spiritType)
                                        val spiritDrinks = liveRecipes.filter { it.baseSpirit == spiritType }

                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clickable { expandedBaseSpirits = if (isSpiritExpanded) expandedBaseSpirits - spiritType else expandedBaseSpirits + spiritType }.padding(vertical = 8.dp, horizontal = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val visualSpiritLabel = if (isSpanish) {
                                                    when(spiritType) {
                                                        "Rum" -> "Ron"
                                                        "Whiskey" -> "Whisky"
                                                        else -> spiritType
                                                    }
                                                } else spiritType
                                                Text(visualSpiritLabel, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                                Icon(imageVector = if (isSpiritExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                                            }

                                            AnimatedVisibility(visible = isSpiritExpanded) {
                                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                                    val availableBrands = spiritDrinks.mapNotNull { recipe ->
                                                        recipe.specificBrand?.let { brand ->
                                                            brand.replace("Rum", "", ignoreCase = true)
                                                                .replace("Vodka", "", ignoreCase = true)
                                                                .replace("Tequila", "", ignoreCase = true)
                                                                .replace("Gin", "", ignoreCase = true)
                                                                .replace("Whiskey", "", ignoreCase = true).trim()
                                                        }
                                                    }.distinct().sorted()

                                                    availableBrands.forEach { brandName ->
                                                        val isBrandExpanded = expandedBrands.contains(brandName)
                                                        val brandDrinks = spiritDrinks.filter { recipe ->
                                                            recipe.specificBrand?.contains(brandName, ignoreCase = true) == true
                                                        }

                                                        Column {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth().clickable { expandedBrands = if (isBrandExpanded) expandedBrands - brandName else expandedBrands + brandName }.padding(vertical = 6.dp, horizontal = 4.dp),
                                                                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(brandName, fontSize = 14.sp, color = AccentOrange)
                                                                Icon(imageVector = if (isBrandExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF55555F), modifier = Modifier.size(16.dp))
                                                            }
                                                            AnimatedVisibility(visible = isBrandExpanded) {
                                                                Column(modifier = Modifier.padding(start = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                    brandDrinks.forEach { drink -> DrinkMenuCard(drink, selectedRecipe, isSpanish) { selectedRecipe = drink } }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = Color(0xFF2D2D34), thickness = 1.dp)
                        }
                    }
                }
            }

            // ==========================================
            // RIGHT SIDE: DISPLAY SCREEN & RECIPE VIEW
            // ==========================================
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .background(OffBlack)
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp)
            ) {
                selectedRecipe?.let { recipe ->
                    val isStarred = favoriteDrinks.contains(recipe.name)

                    // ALWAYS use the primary English name for display title
                    val recipeDisplayName = recipe.name

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1.1f)) {
                            Text(text = recipeDisplayName, fontSize = 38.sp, fontWeight = FontWeight.Black, color = TextPrimary, lineHeight = 44.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Structural folder tags and subcategories stay completely English on both sides
                            val categoriesJoined = recipe.category.joinToString(separator = " • ")
                            val subCatsJoined = recipe.subCategory.joinToString(separator = " • ")

                            val detailText = if (subCatsJoined.isNotEmpty()) {
                                "$categoriesJoined • $subCatsJoined"
                            } else {
                                categoriesJoined
                            }
                            Text(text = detailText.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelectionGold, letterSpacing = 1.5.sp)
                        }

                        IconButton(
                            onClick = {
                                val newFavorites = favoriteDrinks.toMutableSet()
                                if (isStarred) newFavorites.remove(recipe.name) else newFavorites.add(recipe.name)
                                favoriteDrinks = newFavorites
                                sharedPreferences.edit().putStringSet("favorites", newFavorites).apply()
                            },
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            Icon(
                                imageVector = if (isStarred) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = "Pin Favorite",
                                tint = if (isStarred) SelectionGold else Color(0xFF3A3A45),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val hardwareLabel = when {
                        !recipe.container.isNullOrEmpty() -> {
                            val prefix = if (isSpanish) "CONTENEDOR" else "CONTAINER"
                            "$prefix: ${recipe.container.uppercase()}"
                        }
                        !recipe.glassType.isNullOrEmpty() -> {
                            val prefix = if (isSpanish) "VASO" else "GLASS"
                            val glassName = if (isSpanish && !recipe.glassTypeEs.isNullOrEmpty()) recipe.glassTypeEs else recipe.glassType
                            "$prefix: ${glassName.uppercase()}"
                        }
                        else -> if (isSpanish) "RECIPIENTE: NO ESPECIFICADO" else "VESSEL: NOT SPECIFIED"
                    }

                    Surface(
                        color = Color(0xFF222228),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = hardwareLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp), letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = uiIngredientsHeader, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    val ingredientsToDisplay = if (isSpanish && recipe.ingredientsEs != null) recipe.ingredientsEs else recipe.ingredients
                    ingredientsToDisplay.forEach { ingredient ->
                        Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "  •  ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SelectionGold)
                            Text(text = ingredient, fontSize = 20.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }

                    val garnishText = if (isSpanish && !recipe.garnishEs.isNullOrEmpty()) recipe.garnishEs else recipe.garnish
                    garnishText?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(color = Color(0xFF1B2E1C), shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${if (isSpanish) "ADORNO" else "GARNISH"}: ${it.uppercase()}",
                                fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF81C784),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))
                    Text(text = uiInstructionsHeader, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    val instructionsToDisplay = if (isSpanish && !recipe.instructionsEs.isNullOrEmpty()) recipe.instructionsEs else recipe.instructions
                    Surface(color = Color(0xFF1E1E24), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = instructionsToDisplay, fontSize = 19.sp, lineHeight = 28.sp, color = TextPrimary,
                            modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Normal
                        )
                    }

                    if (!recipe.ratio.isNullOrEmpty() || !recipe.colorCode.isNullOrEmpty() || !recipe.shelfLife.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(36.dp))
                        Text(text = uiPrepRulesHeader, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(color = Color(0xFF222228), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (!recipe.ratio.isNullOrEmpty()) {
                                    val label = if (isSpanish) "Proporción de Lote" else "Batch Ratio"
                                    Text("📋 $label: ${recipe.ratio}", color = TextPrimary, fontSize = 17.sp)
                                }
                                if (!recipe.colorCode.isNullOrEmpty()) {
                                    val label = if (isSpanish) "Código de Color" else "Color Striping"
                                    Text("🎨 $label: ${recipe.colorCode}", color = TextPrimary, fontSize = 17.sp)
                                }
                                if (!recipe.shelfLife.isNullOrEmpty()) {
                                    val label = if (isSpanish) "Ciclo de Vida Útil" else "Shelf Life Cycle"
                                    Text("⏱️ $label: ${recipe.shelfLife}", color = AccentOrange, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                } ?: run {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiDefaultSelection, fontSize = 18.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun DrinkMenuCard(drink: DrinkRecipe, selectedRecipe: DrinkRecipe?, isSpanish: Boolean, onClick: () -> Unit) {
    val isSelected = selectedRecipe == drink

    // Cards in sidebar ALWAYS use English name as requested
    val cardTitle = drink.name

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) SelectionGold else CardDark)
    ) {
        Text(
            text = cardTitle, modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
            fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = if (isSelected) OffBlack else TextPrimary
        )
    }
}