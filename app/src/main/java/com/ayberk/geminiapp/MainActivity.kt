package com.ayberk.geminiapp

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.ayberk.geminiapp.geminichatbot.ui.ChatUiEvent
import com.ayberk.geminiapp.geminichatbot.ui.ChatViewModel
import com.ayberk.geminiapp.ui.theme.GeminiAppTheme
import com.ayberk.geminiapp.ui.theme.green
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class MainActivity : ComponentActivity() {

    private val uriState = MutableStateFlow("")

    private val imagePicker = registerForActivityResult<PickVisualMediaRequest,Uri>(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            uriState.update { uri.toString() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeminiAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    Scaffold(
                        topBar = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black)
                                    .height(55.dp)
                                    .padding(horizontal = 16.dp)
                            ) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxHeight(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.robot),
                                        contentDescription = "Icon",
                                        modifier = Modifier.size(35.dp),
                                        tint = Color.White
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = stringResource(id = R.string.app_name),
                                        fontSize = 20.sp,
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    ){
                        ChatScreen(paddingValues = it)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatScreen(paddingValues: PaddingValues) {
        val chatViewModel = viewModel<ChatViewModel>()
        val chatState = chatViewModel.chatState.collectAsState().value

        val bitmap = getBitmap()
        var isLoading by remember { mutableStateOf(false) } // Yükleme durumu

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.Bottom
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                reverseLayout = true
            ){
                itemsIndexed(chatState.chatList) { index, chat ->
                    if (chat.isFromUser){
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(start = 16.dp),
                                color = Color.Black
                            )
                        }
                        UserChatItem(
                            prompt = chat.prompt, bitmap = chat.bitmap
                        )
                    }
                    else{
                        ModelChatItem(response = chat.prompt)
                        isLoading = false
                    }
                }
            }

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Column{
                    bitmap?.let {
                        Image(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(bottom = 2.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentDescription = "picked image",
                            contentScale = ContentScale.Crop,
                            bitmap = it.asImageBitmap(),
                        )
                    }

                    Icon(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                imagePicker.launch(
                                    PickVisualMediaRequest
                                        .Builder()
                                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        .build()
                                )
                            },
                        painter = painterResource(id = R.drawable.image) ,
                        contentDescription = "Add Photo",
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f),
                    value = chatState.prompt,
                    onValueChange = { chatViewModel.onEvent(ChatUiEvent.UpdatePrompt(it)) },
                    placeholder = { Text(text = "Bana Sor...") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = Color.Gray,
                        focusedBorderColor = Color.Gray,
                        unfocusedBorderColor = Color.Gray
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            isLoading = true
                            chatViewModel.onEvent(ChatUiEvent.SendPrompt(chatState.prompt, bitmap))
                            uriState.update { "" }
                        },
                    painter = painterResource(id = R.drawable.send) ,
                    contentDescription = "Send prompt",
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }

    @Composable
    fun UserChatItem(prompt: String,bitmap: Bitmap?) {
        Column(
            modifier = Modifier.padding(start = 100.dp, bottom = 22.dp)
        ) {
            bitmap?.let {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(bottom = 2.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentDescription = "image",
                    contentScale = ContentScale.Crop,
                    bitmap = it.asImageBitmap()
                )
            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(green)
                    .padding(16.dp)
                    .width(IntrinsicSize.Min),
                text = prompt,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    @Composable
    fun ModelChatItem(response: String) {
        Column(
            modifier = Modifier.padding(end = 100.dp, bottom = 22.dp)

        ) {
            Row (
                verticalAlignment = Alignment.Bottom, // Icon'u alt hizala
                modifier = Modifier
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.robot),
                    contentDescription = "Icon",
                    modifier = Modifier.size(35.dp),
                    tint = Color.Black
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .padding(16.dp),
                    text = response,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onTertiary
                )

            }
        }
    }

    @Composable
    private fun getBitmap(): Bitmap? {
        val uri = uriState.collectAsState().value

        val imageState : AsyncImagePainter.State = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .size(Size.ORIGINAL)
                .build()
        ).state

        if (imageState is AsyncImagePainter.State.Success){
            return imageState.result.drawable.toBitmap()
        }
        return null
    }
}
