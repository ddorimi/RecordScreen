package com.plcoding.recordscreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plcoding.recordscreen.ui.theme.RecordScreenTheme
import android.content.Intent
import androidx.compose.ui.platform.LocalContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecordScreenTheme {
                FocusNetHomeScreen()
            }
        }
    }
}

@Composable
fun FocusNetHomeScreen() {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF2D4059)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(300.dp)) // 🔹 Moves logo down independently

                // 🔹 Logo
                Image(
                    painter = painterResource(id = R.drawable.fnlogo),
                    contentDescription = "FocusNet Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(2.5f),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(30.dp)) // 🔹 Space between logo and button

                // 🔹 Start Detection Button (stays centered)
                Button(
                    onClick = { val intent = Intent(context, HazardMenuActivity::class.java)
                        context.startActivity(intent) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(30.dp),
                    modifier = Modifier
                        .height(55.dp)
                        .width(230.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(30.dp),
                            clip = false
                        )
                ) {
                    Text(
                        text = "Start Detection",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.weight(1f)) // 🔹 Pushes About Us to bottom

                Text(
                    text = "About Us",
                    fontSize = 14.sp,
                    color = Color.White,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .clickable {
                            // Navigate to AboutUsActivity
                            val intent = Intent(context, AboutUsActivity::class.java)
                            context.startActivity(intent)
                        }
                )
            }
        }
    }
}
