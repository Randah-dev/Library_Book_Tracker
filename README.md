# Library Book Tracker: Multi-threaded Implementation

A Java-based library management system designed to process and track book catalogs efficiently. The project focuses on handling large datasets using multi-threading and robust error management.

---

### Project Functionality
The system automates the management of library records by reading book data from external files, validating information (such as ISBN and titles), and maintaining an organized catalog. It provides features for adding, searching, and tracking execution statistics.

### Technical Implementation
* **Concurrency & Multi-threading:** Utilizes Java Threads and the `Runnable` interface to separate file reading and data analysis into concurrent tasks, optimizing performance.
* **File I/O & Persistence:** Implements automated file handling to read input data and maintain persistent records.
* **Error Handling & Logging:** Includes custom exception handling for data validation and an automated error logging system with timestamps for debugging.
* **Data Organization:** Ensures records are maintained in alphabetical order for efficient retrieval and search operations.

### Key Concepts
* Multithreaded processing in Java.
* Advanced File I/O and data validation.
* Custom Exception Handling and automated logging.
* Performance tracking and execution statistics.
