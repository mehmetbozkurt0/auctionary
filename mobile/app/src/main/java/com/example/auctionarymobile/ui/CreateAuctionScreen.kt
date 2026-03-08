package com.example.auctionarymobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auctionarymobile.ui.theme.*
import com.example.auctionarymobile.viewmodel.MainViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.TopAppBarDefaults
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.auctionarymobile.model.toImageBitmap
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAuctionScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
){
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Watches") }
    val categories = listOf("Watches","Vehicles","Jewelery","Art","Other")
    var imageUri by remember {mutableStateOf<Uri?>(null)}
    var base64Image by remember { mutableStateOf("") }

    var launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val bytes = outputStream.toByteArray()
                base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            TopAppBar(
                title = { Text("Create Auction", fontWeight = FontWeight.Bold, color = LightTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LightTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                PaddingValues(16.dp)
                Button(
                    onClick = {
                        val priceVal = price.toDoubleOrNull()
                        if (title.isNotBlank() && priceVal != null) {
                            viewModel.createAuction(title, description, selectedCategory, base64Image, priceVal)
                            onSuccess()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Start Auction", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(2.dp, DividerColor, RoundedCornerShape(16.dp))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val bitmap = base64Image.toImageBitmap()
                if (bitmap != null){
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Choosen Product",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, contentDescription = "Add Photo", tint = PrimaryGold, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Upload Product Photo", color = LightTextSecondary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Text("Category", fontWeight = FontWeight.Bold, color = LightTextPrimary)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) PrimaryGold else Color.White,
                        border = if (!isSelected) BorderStroke(1.dp, DividerColor) else null,
                        modifier = Modifier.clickable { selectedCategory = category }
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else LightTextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGold, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGold, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Starting Price (₺)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGold, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}