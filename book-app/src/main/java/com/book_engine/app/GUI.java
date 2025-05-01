package com.book_engine.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneLayout;
import javax.swing.border.EmptyBorder;



/**
 * Class GUI that handles the graphical interface and makes 
 * calls to the BookRecommender
*/
public class GUI extends JFrame {

    String currentUserName = "Guest user";
    // Menus
    JMenuBar menuBar = new JMenuBar();
    JMenu searchMenu = new JMenu("Search");
    JMenu userMenu = new JMenu("User");
    JMenuItem quitItem = new JMenuItem("Quit");

    // Main panel that contains all subpanels
    JPanel mainPanel = new JPanel(new BorderLayout(10,10)); 
    //Panel for search results
    JPanel searchResultPanel = new JPanel(); 
    // Pane with search results and scroll bar inside searchResultPanel
    JScrollPane searchResultPane = new JScrollPane(searchResultPanel); 

    // TextArea for book content and description after selected
    JTextArea bookContentTextArea = new JTextArea(); 
    // Pane inside bookContextArea to be able to scroll through description of a booj
    JScrollPane bookContentPane = new JScrollPane(bookContentTextArea);

    // Search: input field and search button
    JTextField searchInput = new JTextField(30);
    
    
    BookRecommender bookRec;
    ArrayList<User> users; 

    public GUI(BookRecommender bookRec, ArrayList<User> users) { 
        // Set system properties for higher font resolution
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        this.bookRec = bookRec;
        this.users = users;

        // Set up the window attributes
        setTitle("Book recommendation system");
        setSize(800, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        
        // Initialize main panel attributes and add to window
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(mainPanel);
        
        Action searchAction = getSearchAction();

        searchInput.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        searchInput.registerKeyboardAction( searchAction, 
                            "", 
                            KeyStroke.getKeyStroke( "ENTER" ), 
                            JComponent.WHEN_FOCUSED );
                            
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> search());

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.setBackground(new Color(255, 226, 254));
        searchPanel.setBorder(new EmptyBorder(10, 0, 10, 0)); // top and bottom padding

        searchInput.setFont(new Font("Roboto", Font.PLAIN, 18));
        searchInput.setMaximumSize(new Dimension(600, 40));
        searchInput.setPreferredSize(new Dimension(400, 40));
        searchInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 200, 245), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchInput.setBackground(Color.WHITE);

        searchButton.setFont(new Font("Roboto", Font.BOLD, 18));
        searchButton.setBackground(new Color(140, 90, 160));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.addActionListener(e -> search());

        String[] usernames = getUsernames();

        final JComboBox<String> cb = new JComboBox<String>(usernames);

        cb.setMaximumSize(cb.getPreferredSize());
        cb.setAlignmentX(Component.CENTER_ALIGNMENT);

        cb.addActionListener(e -> {
            String selectedUser = (String) cb.getSelectedItem();
            if (selectedUser != null) {
                System.out.println("Selected User: " + selectedUser);
                displayInitialRecommendations(selectedUser);

            }
        });

        searchPanel.add(Box.createHorizontalStrut(10));
        searchPanel.add(searchInput);
        searchPanel.add(Box.createHorizontalStrut(12));
        searchPanel.add(searchButton);
        searchPanel.add(Box.createHorizontalStrut(12));
        searchPanel.add(cb);
        searchPanel.add(Box.createHorizontalStrut(10));

        // Set up menu
        menuBar.add(searchMenu);
        menuBar.add(userMenu);
        menuBar.add(quitItem);
        setJMenuBar(menuBar);

        // Search result styling
        searchResultPanel.setLayout(new BoxLayout(searchResultPanel, BoxLayout.Y_AXIS));
        searchResultPane.setLayout(new ScrollPaneLayout());
        searchResultPanel.setBorder(new EmptyBorder(10,10,10,10));
        searchResultPane.setPreferredSize(new Dimension(400, 450));
        
        // Book content styling
        JPanel bookContentPanel = new JPanel(new BorderLayout());
        bookContentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 200, 245), 2),
            new EmptyBorder(16, 18, 16, 18)
        ));
        bookContentPanel.setBackground(Color.WHITE);

        bookContentTextArea.setFont(new Font("Roboto", Font.PLAIN, 15));
        bookContentTextArea.setLineWrap(true);
        bookContentTextArea.setWrapStyleWord(true);
        bookContentTextArea.setEditable(false);
        bookContentTextArea.setOpaque(false);
        bookContentTextArea.setBorder(null);
        JScrollPane bookContentScroll = new JScrollPane(bookContentTextArea);
        bookContentScroll.setBorder(null);
        bookContentPanel.add(bookContentScroll, BorderLayout.CENTER);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, searchResultPane, bookContentPanel);
        splitPane.setDividerLocation(500); 
        splitPane.setResizeWeight(0.7);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                if (getWidth() > 1000) {
                    splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                    splitPane.setDividerLocation(0.6);
                } else {
                    splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
                    splitPane.setDividerLocation(0.7);
                }
            }
        });
        

        // Action for quitting
        quitItem.addActionListener(e -> System.exit(0));

        // Page title
        JLabel welcomeLabel = new JLabel("Welcome to the book recommendation engine");
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(new Color(255, 226, 254));
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        welcomeLabel.setFont(new Font("Roboto", Font.BOLD, 18));
        welcomeLabel.setBorder(new EmptyBorder(8, 0, 8, 0));
        topPanel.add(welcomeLabel);
        topPanel.add(searchPanel);

        mainPanel.add(topPanel, BorderLayout.PAGE_START);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        searchResultPanel.setOpaque(true);
        searchResultPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        mainPanel.setBackground(new java.awt.Color(255, 191, 254));
        setVisible(true); // Make it visible
    }

    private String[] getUsernames(){
        String[] usernames = new String[users.size()];

        int i = 0;
        for (User user: this.users){
            usernames[i] = user.username;
            i++;
        }
        return usernames;
    }
    /**
     * Search action
     */
    private Action getSearchAction() {
        return new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
                
                // Empty the results window
                searchResultPanel.removeAll();

                // Create a new label and display it
                JLabel label = new JLabel();
                searchResultPanel.add(label);
                revalidate();
                repaint();
                search();
                
            }
        };
    } 

    /**
     * When a new user is selected, book recommendations are shown
     * based on the users previously read books but without a specific 
     * query 
     */
    private void displayInitialRecommendations(String selectedUser){
        try {
            currentUserName = selectedUser;
            ArrayList<Book> initialBooks = bookRec.initialRecommendations(currentUserName);
            displayBookResults(initialBooks); 
            searchResultPanel.revalidate();
            searchResultPanel.repaint();
            
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Function that is called every time a user makes a search 
     * either by pressing ENTER or pressing search.
     * First the results are retrieved from the BookRecommender
     * then the searchResultPanel is updated with the new results
     */
    private void search() {
        String query = searchInput.getText();
        ArrayList<Book> books;
        try {
            books = bookRec.search(query);
            displayBookResults(books); 
            searchResultPanel.revalidate();
            searchResultPanel.repaint();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Adds all books to a JScrollPane that contains a listener for any selected books
     * @param books The list of books to be displayed
     * @return JScrollPane containing the list of books
     */
    public void displayBookResults(ArrayList<Book> books) {
        searchResultPanel.removeAll();  // clear old results
    
        for (Book book : books) {
            BookResultPanel panel = new BookResultPanel(book);
            panel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    displayBookContent(book);
                }
            });
            searchResultPanel.add(panel);
            searchResultPanel.add(Box.createVerticalStrut(8));
        }
    }
    

    /**
     * Display the content of a book in the text area
     * @param book The book to be displayed
     */
    public void displayBookContent(Book book){
        bookContentTextArea.setText(book.displayContents());
        bookContentTextArea.setCaretPosition(0);
    }

    /**
     * Converts a rating to a string of stars
     * @param rating The rating to be converted
     * @return A string of stars representing the rating
     */
    public static String getStarString(double rating) {
        int stars = (int) Math.round(rating);  // round rating to nearest integer
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stars; i++) sb.append("★");
        for (int i = stars; i < 5; i++) sb.append("☆");
        return sb.toString();
    }
    
    
    private class BookResultPanel extends JPanel {
        private static final int COVER_WIDTH = 50;
        private static final int COVER_HEIGHT = 80;
    
        public BookResultPanel(Book book) {
            setLayout(new BorderLayout(14, 0));
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            setBackground(Color.WHITE);
            setAlignmentX(Component.LEFT_ALIGNMENT);
    
            // Add Roboto font
            Font robotoTitle = new Font("Roboto", Font.BOLD, 10);
            Font robotoAuthor = new Font("Roboto", Font.PLAIN, 8);
            Font robotoBig = new Font("Roboto", Font.BOLD, 14);
            Font robotoNormal = new Font("Roboto", Font.PLAIN, 14);
            Font robotoItalic = new Font("Roboto", Font.ITALIC, 12);
    
            // Book cover
            JPanel coverPanel = new JPanel() {
                /**
                 * Paints a book cover with a random background color, title, and author.
                 *
                 * @param g the Graphics context used for drawing
                 */
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);

                    // Select a random color for the cover
                    Random rand = new Random(book.title.hashCode());
                    g.setColor(new Color(
                        rand.nextInt(128) + 64, 
                        rand.nextInt(128) + 64, 
                        rand.nextInt(128) + 64
                    ));
                    setBackground(Color.WHITE);
                    g.fillRect(0, 0, COVER_WIDTH, COVER_HEIGHT);
    
                    // Add title to cover
                    g.setColor(Color.WHITE);
                    g.setFont(robotoTitle);
                    writeMultilineString(g, book.title, 8, 18, COVER_WIDTH-16, robotoTitle, 3); // up to 3 lines
                    
                    // Add author to cover
                    g.setFont(robotoAuthor);
                    writeMultilineString(g, book.author, 8, COVER_HEIGHT - 18, COVER_WIDTH-16, robotoAuthor, 1);
                }

                /**
                 * Helper function to draw a multi-line string within a given max width and line count.
                 * Appends "..." if needed.
                 *
                 * @param g         the Graphics context
                 * @param text      the text to render
                 * @param x         x-coordinate for drawing
                 * @param y         y-coordinate of the first line
                 * @param maxWidth  maximum width in pixels for each line
                 * @param font      font to use for rendering
                 * @param maxLines  maximum number of lines to render
                 */
                private void writeMultilineString(Graphics g, String text, int x, int y, int maxWidth, Font font, int maxLines) {
                    FontMetrics fm = g.getFontMetrics(font);
                    String[] words = text.split(" ");
                    StringBuilder line = new StringBuilder();
                    int offsetY = 0;
                    int lines = 0;

                    // Go through each word and check if it fits in the line
                    for (String word : words) {
                        String testLine = line.length() == 0 ? word : line + " " + word;
                        // Check if line exceeds max width
                        if (fm.stringWidth(testLine) > maxWidth) {
                            // Draw the current line
                            g.drawString(line.toString(), x, y + offsetY);
                            offsetY += fm.getHeight();
                            lines++;
                            // Add "..." if the line count exceeds maxLines
                            if (lines >= maxLines) {
                                g.drawString("...", x, y + offsetY);
                                return;
                            }
                            line = new StringBuilder(word);
                        } else {
                            // Add the word to the current line
                            if (line.length() > 0) line.append(" ");
                            line.append(word);
                        }
                    }
                    // Draw the last line
                    if (line.length() > 0 && lines < maxLines) {
                        g.drawString(line.toString(), x, y + offsetY);
                    }
                        
                }
                
            };
            coverPanel.setPreferredSize(new Dimension(COVER_WIDTH, COVER_HEIGHT));
    
            // Info Panel with title, author and rating
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);
    
            // Book title
            JLabel titleLabel = new JLabel("<html><body style='width:420px'>" + book.title + "</body></html>");
            titleLabel.setFont(robotoBig);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Book author
            JLabel authorLabel = new JLabel("by " + book.author);
            authorLabel.setFont(robotoItalic);
            authorLabel.setForeground(Color.GRAY);
            authorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
            // Book rating as stars
            String stars = getStarString(book.rating);
            JLabel starsLabel = new JLabel(stars);
            starsLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 22));
            starsLabel.setForeground(new Color(0xFFD700)); // Gold color for stars
            starsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Add components to the info panel
            infoPanel.add(Box.createVerticalStrut(8));
            infoPanel.add(titleLabel);
            infoPanel.add(authorLabel);
            infoPanel.add(starsLabel);
            infoPanel.add(Box.createVerticalStrut(8));
    
            add(coverPanel, BorderLayout.WEST);
            add(infoPanel, BorderLayout.CENTER);
    
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }
}
