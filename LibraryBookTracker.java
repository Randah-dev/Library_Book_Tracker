import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;
import java.nio.file.*;

public class LibraryBookTracker {
    // static variables to track statistics among all objects
    private static int validRecords = 0;
    private static int searchResults = 0;
    private static int booksAdded = 0;
    private static int errorsEncountered = 0;

    public static void main(String[] args) throws InterruptedException {
        try {
            if (args.length < 2) {
                throw new InsufficientArgumentsException("Need at least 2 arguments");
            }
            String filePath = args[0];
            if (!filePath.endsWith(".txt")) {
                throw new InvalidFileNameException("File name must end with .txt");
            }
            String operation = args[1];
            ArrayList<Book> allBooks = new ArrayList<>();
            // Load the file in a separate thread
            Thread fileThread = new Thread(new FileReaderThread(filePath, allBooks));
            Thread opThread = new Thread(new OperationAnalyzerThread(operation, allBooks, filePath));
            fileThread.start();
            fileThread.join();// wait the firs thread to finish

            opThread.start();
            opThread.join();

            // mangeprocess(filePath, allBooks);

            System.out.println("\n--- Final Statistics ---");
            System.out.println("Valid records processed: " + validRecords);
            System.out.println("Search results found: " + searchResults);
            System.out.println("Books added: " + booksAdded);
            System.out.println("Errors encountered: " + errorsEncountered);

        } catch (BookCatalogException e) {
            System.err.println("Failure: " + e.getMessage());
            if (args.length >= 2) {
                recordErrorToLog(args[0], args[1], e);
            }
        } finally {
            System.out.println("Thank you for using the Library Book Tracker.");
        }
    }

    /**
     * Records errors to 'errors.log' in the catalog's directory.
     * 
     * @param catalogFilePath
     * @param offendingLine
     * @param e
     */
    private static void recordErrorToLog(String catalogFilePath, String offendingLine, Exception e) {
        Path catalogPath = Paths.get(catalogFilePath);
        Path parentDirectory = catalogPath.getParent();
        String errorLogPath;

        if (parentDirectory != null) {
            errorLogPath = parentDirectory.resolve("errors.log").toString();
        } else {
            errorLogPath = "errors.log";
        }

        try (PrintWriter logWriter = new PrintWriter(new FileWriter(errorLogPath, true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            String errorType = e.getClass().getSimpleName();
            String errorMessage = e.getMessage();

            logWriter.printf("[%s] INVALID INPUT: \"%s\" - %s: %s%n",
                    timestamp, offendingLine, errorType, errorMessage);

        } catch (IOException ioException) {
            System.err.println("Failed to write to error log: " + ioException.getMessage());
        }
    }

    private static void printHeader() {
        System.out.printf("%-30s %-20s %-15s %5s%n", "Title", "Author", "ISBN", "Copies");
        System.out.println("-".repeat(75));
    }

    private static void printBookRow(Book b) {
        System.out.printf("%-30s %-20s %-15s %5d%n",
                b.getTitle(), b.getAuthor(), b.getISBN(), b.getCopies());
    }

    /**
     * Adds a new book to the catalog, updates the file, and prints the updated
     * catalog.
     * 
     * @param record
     * @param books
     * @param catalogFilePath
     * @throws BookCatalogException
     */
    private static void addNewBook(String record, ArrayList<Book> books, String catalogFilePath)
            throws BookCatalogException {
        Book newBook = new Book(record);
        books.add(newBook);
        booksAdded++;

        books.sort((b1, b2) -> {
            int titleComparison = b1.getTitle().compareToIgnoreCase(b2.getTitle());
            if (titleComparison != 0) {
                return titleComparison;
            }
            // Titles are the same, compare by author
            return b1.getAuthor().compareToIgnoreCase(b2.getAuthor());
        });

        try (PrintWriter writer = new PrintWriter(new FileWriter(catalogFilePath))) {
            for (Book book : books) {
                writer.println(book.toString());
            }
            System.out.println("Book added successfully: " + newBook.getTitle() + " by " + newBook.getAuthor());
            printHeader();
            printBookRow(newBook);
        } catch (IOException e) {
            System.err.println("Failed to write to catalog: " + e.getMessage());
        }
    }

    /**
     * Loads the catalog file, creates Book objects for valid entries, and logs
     * errors for invalid entries.
     * 
     * @param filePath
     * @param books
     */
    private static void loadFile(String filePath, ArrayList<Book> books) {
        File file = new File(filePath);
        try {
            if (file.getParentFile() != null)
                file.getParentFile().mkdirs();
            if (!file.exists())
                file.createNewFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty())
                        continue; // Skip empty lines
                    try {
                        books.add(new Book(line));
                        validRecords++;
                    } catch (BookCatalogException e) {
                        errorsEncountered++;
                        recordErrorToLog(filePath, line, e);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error accessing file: " + e.getMessage());
        }
    }

    /**
     * Searches the catalog for books matching the query (by ISBN or title) and
     * prints results.
     * 
     * @param query
     * @param bookList
     * @param fileName
     * @throws DuplicateISBNException
     */
    private static void Search(String query, ArrayList<Book> bookList, String fileName) throws DuplicateISBNException {
        printHeader();
        String cleanQuery = query.trim();

        if (cleanQuery.matches("\\d{13}")) {
            // ISBN Search
            ArrayList<Book> results = new ArrayList<>();
            for (Book b : bookList) {
                if (b.getISBN().trim().equals(cleanQuery)) {
                    results.add(b);
                }
            }

            if (results.size() == 1) {
                printBookRow(results.get(0));
                searchResults = 1;
            } else if (results.size() > 1) {
                throw new DuplicateISBNException("Multiple books found with ISBN: " + cleanQuery);
            } else {
                System.out.println("No book found with ISBN: " + cleanQuery);
            }
        } else {
            // Title Search
            for (Book b : bookList) {
                if (b.getTitle().toLowerCase().contains(cleanQuery.toLowerCase())) {
                    printBookRow(b);
                    searchResults++;
                }
            }
            if (searchResults == 0) {
                System.out.println("No books found matching title: " + cleanQuery);
            }
        }
    }

    /**
     * 
     * @param filePath
     * @param operation
     */
    private static void mangeprocess(String filePath, String operation, ArrayList<Book> allBooks) {
        // ArrayList<Book> allBooks = new ArrayList<>(); shared list from main thread
        // loadFile(filePath, allBooks); will handle it in the main thread
        try {
            if (operation.contains(":") && operation.split(":").length >= 4) {
                addNewBook(operation, allBooks, filePath);
            } else {
                Search(operation, allBooks, filePath);
            }
        } catch (BookCatalogException e) {
            System.out.println("Error: " + e.getMessage());
            recordErrorToLog(filePath, operation, e);
            errorsEncountered++;
        }
    }
    
//inner classes for threads to handle file loading and operation analysis concurrently
    private static class FileReaderThread implements Runnable {
        private String filePath;
        private ArrayList<Book> sharedListBooks;

        public FileReaderThread(String filePath, ArrayList<Book> sharedListBooks) {
            this.filePath = filePath;
            this.sharedListBooks = sharedListBooks;
        }

        @Override
        public void run() {
            System.out.println("Starting load file thread");
            loadFile(filePath, sharedListBooks);
            System.out.println("File loading complete");

        }
    }

    private static class OperationAnalyzerThread implements Runnable {
        private String operation;
        private String filePath;
        private ArrayList<Book> sharedListBooks;

        public OperationAnalyzerThread(String operation, ArrayList<Book> shArrayListBooks, String filePath) {
            this.operation = operation;
            this.filePath = filePath;
            this.sharedListBooks = shArrayListBooks;
        }

        @Override
        public void run() {
            System.out.println("Starting operation analyzer thread");
            mangeprocess(filePath, operation, sharedListBooks);
            System.out.println("Operation Analyzer complete");
        }
    }
}