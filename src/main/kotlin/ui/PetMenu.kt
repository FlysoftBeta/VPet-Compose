package ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.contextMenuOpenDetector
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.ApplicationScope
import state.action.ActionType

private data class MenuItem(
    val text: String,
    val icon: ImageVector? = null,
    val expandable: Boolean,
    val onClick: () -> List<MenuItem>?,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ApplicationScope.PetMenu(content: @Composable () -> Unit) {
    val resourceManager = LocalManagers.current.resourceManager
    val pet = resourceManager.pet
    val state = LocalPetState.current

    var stateCardExpanded by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val menuStack = remember { mutableStateListOf<List<MenuItem>>() }

    fun listActionsOfType(type: ActionType): List<MenuItem> {
        return pet.actions.filter { actionResource -> actionResource.action.type == type }.map { actionResource ->
            MenuItem(actionResource.action.name, null, false) {
                state.action = actionResource.action
                null
            }
        }
    }

    val actionMenu = remember {
        listOf(
            // TODO: Implement sleeping
            MenuItem("睡觉", Icons.Filled.Bedtime, false) { null },
            MenuItem("学习", Icons.Filled.Book, true) { listActionsOfType(ActionType.STUDY) },
            MenuItem("玩耍", Icons.Filled.Casino, true) { listActionsOfType(ActionType.PLAY) },
            MenuItem("工作", Icons.Filled.Work, true) { listActionsOfType(ActionType.WORK) })
    }
    val othersMenu = remember {
        listOf(MenuItem("退出", Icons.Filled.ExitToApp, false) {
            exitApplication()
            null
        })
    }
    val mainMenu = remember {
        listOf(
            // TODO: Implement state dashboard
            MenuItem("状态", Icons.Filled.Dashboard, true) {
                stateCardExpanded = true
                null
            },
            // TODO: Implement store
            MenuItem("投喂", Icons.Filled.Fastfood, false) { null },
            MenuItem("互动", Icons.Filled.Workspaces, true) { actionMenu },
            MenuItem("其他", Icons.Filled.MoreVert, true) { othersMenu },
        )
    }

    LaunchedEffect(expanded) {
        if (expanded) menuStack.clear()
    }

    DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
        val menu = (menuStack.lastOrNull()?.let { menu -> Pair(true, menu) } ?: Pair(false, mainMenu))

        // Go to parent menu
        if (menu.first) {
            DropdownMenuItem(text = { Text("返回") }, leadingIcon = {
                Icon(
                    Icons.Filled.ArrowBack, contentDescription = null
                )
            }, onClick = {
                menuStack.removeLastOrNull()
            })
        }

        menu.second.forEach { item ->
            DropdownMenuItem(text = { Text(item.text) }, leadingIcon = {
                item.icon?.let {
                    Icon(
                        it, contentDescription = null
                    )
                }
            }, trailingIcon = {
                if (item.expandable)
                    Icon(
                        Icons.Filled.ChevronRight, contentDescription = null
                    )
            }, onClick = {
                item.onClick()?.let { menu -> menuStack.add(menu) }
                expanded = item.expandable
            })
        }
    }



    Box(modifier = Modifier.contextMenuOpenDetector { expanded = !expanded }) {
        content()
    }

    StatusCard(expanded = stateCardExpanded, onDismissRequest = {
        stateCardExpanded = false
    })
}