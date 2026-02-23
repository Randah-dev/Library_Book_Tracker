public class Book {
    private String title;
    private String author;
    private String ISBN;
    private int copies;

    public Book(String fullLine) throws BookCatalogException {
        
        String[] parts = fullLine.split(":");

        
        if (parts.length < 4) {throw new MalformedBookEntryException("Book entry has missing fields");}

        if (parts[0].trim().isEmpty()) { throw new MalformedBookEntryException("Book entry has empty title");}

        if (parts[1].trim().isEmpty()) {throw new MalformedBookEntryException("Book entry has empty author"); }

        if (parts[2].trim().length() != 13 || !parts[2].trim().matches("\\d{13}")) {
            throw new InvalidISBNException("ISBN is not exactly 13 digits or contains non-numeric characters");
        }

        if (!parts[3].trim().matches("\\d+") || Integer.parseInt(parts[3].trim()) <= 0) {
            throw new MalformedBookEntryException("invalid copies(not a positive integer)");
        }

        this.title = parts[0].trim();
        this.author = parts[1].trim();
        this.ISBN = parts[2].trim();
        this.copies = Integer.parseInt(parts[3].trim());
    }

  
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getISBN() { return ISBN; }
    public int getCopies() { return copies; }

    @Override
    public String toString() {
        return title + ":" + author + ":" + ISBN + ":" + copies;
    }

}
