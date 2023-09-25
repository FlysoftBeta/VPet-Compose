package ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.contextMenuOpenDetector
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.ApplicationScope
import state.action.ActionType

private open class MenuItem

private class TextMenuItem(
    val text: String,
    val icon: ImageVector? = null,
    val expandable: Boolean = false,
    val onClick: () -> List<MenuItem>?,
) : MenuItem()

private class DividerMenuItem : MenuItem()

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
            TextMenuItem(actionResource.action.name) {
                state.startAction(actionResource.action)
                null
            }
        }
    }

    val actionMenu = remember(state.action) {
        listOfNotNull(
            *(state.action?.let { action ->
                arrayOf(TextMenuItem("结束${action.name}", Icons.Filled.Close) {
                    state.stopAction()
                    null
                }, DividerMenuItem())
            } ?: arrayOf()),
            TextMenuItem("睡觉", Icons.Filled.Bedtime) {
                state.sleep()
                null
            },
            TextMenuItem("学习", Icons.Filled.Book, expandable = true) { listActionsOfType(ActionType.STUDY) },
            TextMenuItem("玩耍", Icons.Filled.Casino, expandable = true) { listActionsOfType(ActionType.PLAY) },
            TextMenuItem("工作", Icons.Filled.Work, expandable = true) { listActionsOfType(ActionType.WORK) })
    }
    val othersMenu = remember {
        listOf(TextMenuItem("退出", Icons.Filled.ExitToApp) {
            exitApplication()
            null
        })
    }
    val mainMenu = remember(actionMenu, othersMenu) {
        listOf(
            TextMenuItem("状态", Icons.Filled.Dashboard, expandable = true) {
                stateCardExpanded = true
                null
            },
            // TODO: Implement store
            TextMenuItem("投喂", Icons.Filled.Fastfood) { null },
            TextMenuItem("互动", Icons.Filled.Workspaces, expandable = true) { actionMenu },
            TextMenuItem("其他", Icons.Filled.MoreVert, expandable = true) { othersMenu },
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
            Divider()
        }

        menu.second.forEach { item ->
            if (item is TextMenuItem)
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
            else if (item is DividerMenuItem)
                Divider()
        }
    }



    Box(modifier = Modifier.contextMenuOpenDetector { expanded = !expanded }) {
        content()
    }

    PetStatusCard(expanded = stateCardExpanded, onDismissRequest = {
        stateCardExpanded = false
    })
}