package com.book_engine.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneLayout;
import javax.swing.border.EmptyBorder;


/**
 * Class GUI that handles the graphical interface and makes 
 * calls to the BookRecommender
*/
public class GUI extends JFrame {

    // Menus
    JMenuBar menuBar = new JMenuBar();
    JMenu searchMenu = new JMenu("Search");
    JMenu userMenu = new JMenu("User");
    JMenuItem quitItem = new JMenuItem("Quit");

    // Main panel that contains all subpanels
    //JPanel mainPanel = new JPanel(new BorderLayout(20,30));  
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

    public GUI(BookRecommender bookRec) { 
        //this.database = database;
        this.bookRec = bookRec;

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
                            
        JButton searchButton = new JButton("search");
        searchButton.addActionListener(e -> search());

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
        bookContentPane.setPreferredSize(new Dimension(400, 200));
        bookContentTextArea.setText("\n  The contents of the document will appear here.");
        bookContentTextArea.setFont(new Font("Times New Roman",Font.BOLD, 14));
        bookContentTextArea.setLineWrap(true);
        bookContentTextArea.setWrapStyleWord(true);
        bookContentTextArea.setEditable(false); // non-editable content of book 
        bookContentTextArea.setBorder( new EmptyBorder(10,10,10,10) );

        // Action for quitting
        quitItem.addActionListener(e -> System.exit(0));

        // Page title
        JLabel welcomeLabel = new JLabel("Welcome to the book recommendation engine");
        welcomeLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
        
        JPanel p1 = new JPanel(new BorderLayout(120, 50)); // First JPanel 
        p1.add(welcomeLabel, BorderLayout.PAGE_START);
        p1.add(searchInput, BorderLayout.LINE_START);
        p1.add(searchButton, BorderLayout.CENTER);
        p1.setBackground(new java.awt.Color(255, 226, 254));
        mainPanel.add(p1, BorderLayout.PAGE_START);


        searchResultPanel.setOpaque(true);
        searchResultPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        mainPanel.add(searchResultPane, BorderLayout.CENTER);
        mainPanel.add(bookContentPane, BorderLayout.PAGE_END);

        mainPanel.setBackground(new java.awt.Color(255, 191, 254));
        setVisible(true); // Make it visible
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
                JLabel label = new JLabel("info");
                searchResultPanel.add(label);
                revalidate();
                repaint();
                search();
                
            }
        };
    } 

    /**
     * Function that is called every time a user makes a search 
     * either by pressing ENTER or pressing search.
     * First the results are retrieved from the BookRecommender
     * then the searchResultPanel is updated with the new results
     */
    private void search(){
        String query = searchInput.getText();
        ArrayList<Book> books;
        try {
            books = bookRec.search(query);
            searchResultPanel.remove(searchResultPane);
            searchResultPane = displayBookResults(books);
            searchResultPanel.add(searchResultPane);
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
    public JScrollPane displayBookResults(ArrayList<Book> books) {
        // List model to hold the book titles and authors
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (int i = 0; i < books.size(); i++) {
            listModel.addElement(i + " " + books.get(i).title + " - " + books.get(i).author);
        }

        // Create a JList to display the book titles and authors
        JList<String> resultList = new JList<>(listModel);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setLayoutOrientation(JList.VERTICAL);
        resultList.setVisibleRowCount(-1); 
        resultList.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        resultList.setFont(new Font("Times New Roman", Font.PLAIN, 16));
       
        // Add a listener to the list to handle book selection events
        resultList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = resultList.getSelectedValue();
                Integer booknr = Integer.parseInt(selected.split(" ")[0]);
                
                // Display the content of the selected book in the text area
                displayBookContent(books.get(booknr));
            }
        });

        JScrollPane newsearchResultPane = new JScrollPane(resultList);
        return newsearchResultPane;
    }

    /**
     * Display the content of a book in the text area
     * @param book The book to be displayed
     */
    public void displayBookContent(Book book){
        bookContentTextArea.setText(book.displayContents());
    }
}