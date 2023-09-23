import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.contextMenuOpenDetector
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PetMenu(content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text("投喂") },
            onClick = { expanded = false },
            leadingIcon = {
                Icon(
                    Icons.Filled.Fastfood,
                    contentDescription = null
                )
            })
        DropdownMenuItem(
            text = { Text("互动") },
            onClick = { expanded = false },
            leadingIcon = {
                Icon(
                    Icons.Filled.Workspaces,
                    contentDescription = null
                )
            })
        DropdownMenuItem(
            text = { Text("状态") },
            onClick = { expanded = false },
            leadingIcon = {
                Icon(
                    Icons.Filled.Dashboard,
                    contentDescription = null
                )
            })
        DropdownMenuItem(
            text = { Text("其他") },
            onClick = { expanded = false },
            leadingIcon = {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = null
                )
            })
    }

    Box(modifier = Modifier.contextMenuOpenDetector { expanded = !expanded }) {
        content()
    }
}