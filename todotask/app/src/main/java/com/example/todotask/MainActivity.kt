package com.example.todotask

import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todotask.ui.theme.TodoTaskTheme
import kotlinx.parcelize.Parcelize

// =================================================================================
// 1. Data Model
// =================================================================================
/**
 * Represents a single To-Do item.
 * @param id A unique identifier for the item.
 * @param label The text content of the task.
 * @param isCompleted Whether the task is marked as complete.
 *
 * It's Parcelable to be easily saved and restored by `rememberSaveable`.
 */
@Parcelize
data class TodoItem(
    val id: Int,
    val label: String,
    val isCompleted: Boolean = false
) : Parcelable


// =================================================================================
// 2. Main Activity
// =================================================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoTaskTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TodoScreen()
                }
            }
        }
    }
}

// =================================================================================
// 3. Stateful Parent Composable (View Logic)
// =================================================================================
@Composable
fun TodoScreen() {
    // Context for showing Toast messages
    val context = LocalContext.current

    // State for the text in the input field.
    // `rememberSaveable` ensures it survives configuration changes (e.g., rotation).
    var text by rememberSaveable { mutableStateOf("") }

    // State for the list of all To-Do items.
    // `rememberSaveable` with a custom `listSaver` is used to persist our custom TodoItem list.
    val items = rememberSaveable(
        saver = listSaver(
            save = { stateList ->
                // Don't save if it's not a mutable state list
                if (stateList.isNotEmpty()) {
                    val list = stateList.toList()
                    // Convert each item to a list of its properties
                    list.map { listOf(it.id, it.label, it.isCompleted) }
                } else {
                    emptyList()
                }
            },
            restore = { list ->
                // Restore the list from the saved properties
                list.map {
                    TodoItem(
                        id = it[0] as Int,
                        label = it[1] as String,
                        isCompleted = it[2] as Boolean
                    )
                }.toMutableStateList()
            }
        )
    ) {
        // Initial list is empty
        mutableStateListOf<TodoItem>()
    }


    // --- Event Handlers (Unidirectional Data Flow) ---

    val onAddItem = {
        val trimmedText = text.trim()
        if (trimmedText.isBlank()) {
            Toast.makeText(context, "Task name cannot be empty.", Toast.LENGTH_SHORT).show()
        } else {
            // Generate a unique ID (simple approach for this example)
            val newId = (items.maxOfOrNull { it.id } ?: 0) + 1
            items.add(TodoItem(id = newId, label = trimmedText))
            text = "" // Clear the input field
        }
    }

    val onItemCheckedChange: (TodoItem, Boolean) -> Unit = { item, isChecked ->
        val index = items.indexOf(item)
        if (index != -1) {
            items[index] = item.copy(isCompleted = isChecked)
        }
    }

    val onItemDeleted: (TodoItem) -> Unit = { item ->
        items.remove(item)
    }

    // --- UI Layout ---

    Column(modifier = Modifier.padding(16.dp)) {
        // This is the stateful parent passing state down and receiving events up.
        TodoInputBar(
            text = text,
            onTextChange = { newText -> text = newText },
            onAddItem = onAddItem
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Filter lists for display
        val activeItems = items.filter { !it.isCompleted }
        val completedItems = items.filter { it.isCompleted }

        // Active Items Section
        TodoListSection(
            title = "Items",
            items = activeItems,
            emptyMessage = "No items yet!",
            onItemCheckedChange = onItemCheckedChange,
            onItemDeleted = onItemDeleted
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Completed Items Section
        TodoListSection(
            title = "Completed Items",
            items = completedItems,
            emptyMessage = "No completed items yet.",
            onItemCheckedChange = onItemCheckedChange,
            onItemDeleted = onItemDeleted
        )
    }
}

// =================================================================================
// 4. Stateless Child Composables (UI Components)
// =================================================================================

/**
 * A stateless composable for the text input and "Add" button.
 * State is hoisted to the parent (`TodoScreen`).
 */
@Composable
fun TodoInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onAddItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text("Enter the task name") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onAddItem) {
            Text("Add")
        }
    }
}

/**
 * A stateless composable that displays a section header and a list of To-Do items.
 */
@Composable
fun TodoListSection(
    title: String,
    items: List<TodoItem>,
    emptyMessage: String,
    onItemCheckedChange: (TodoItem, Boolean) -> Unit,
    onItemDeleted: (TodoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Show section header only if the list is not empty.
        if (items.isNotEmpty()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Show the list or the empty state message.
        if (items.isEmpty()) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // LazyColumn is efficient for long lists.
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(items, key = { it.id }) { item ->
                    TodoItemRow(
                        item = item,
                        onCheckedChange = { isChecked -> onItemCheckedChange(item, isChecked) },
                        onDelete = { onItemDeleted(item) }
                    )
                    Divider()
                }
            }
        }
    }
}

/**
 * A stateless composable for displaying a single row in the To-Do list.
 */
@Composable
fun TodoItemRow(
    item: TodoItem,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isCompleted,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.label,
            fontSize = 18.sp,
            textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete task"
            )
        }
    }
}

// =================================================================================
// 5. Preview
// =================================================================================
@Preview(showBackground = true)
@Composable
fun TodoScreenPreview() {
    TodoTaskTheme {
        TodoScreen()
    }
}