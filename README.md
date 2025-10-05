# App Overview
The app contains the **textbox** that displays a **toast message** when the **add button** is pressed while textbox is empty, as well as showing user friendly messages showing when there are **no items yet** and **no completed items**. When the **textbox is full**, the add button **adds a TodoItemRow** with a **checkbox**, the **item name** and a **delete button**. When the checkbox is empty on the TodoItemRow, the item stays in the **items** todo list section, when the checkbox is checked, the item moves to **completed items** todo list section.
# Concepts Used
Data class - TodoItem is wireframed with the data class, functions like an enum, easy to manage the structure of an object and create/delete copies whenever needed<br>
State - State is managed in order to preserve values when the screen rotates. rememberSaveable is used throughout the project to store and keep values<br>
State Hoisting - TodoScreen is a stateful parent that holds the items as a list with the input text, then passes the data to stateless children TodoInputBar and TodoItemRow, which have lambdas inside of them (like clicking the checkbox) that notifies the parent to update the state of the TodoItemRow.<br>
<img width="328" height="734" alt="image" src="https://github.com/user-attachments/assets/4d3730d7-ca11-4349-82ff-ddb5874c32d5" /><br><img width="337" height="754" alt="image" src="https://github.com/user-attachments/assets/e05854ff-5e5c-4ecb-bb90-405eb7bfc419" />









