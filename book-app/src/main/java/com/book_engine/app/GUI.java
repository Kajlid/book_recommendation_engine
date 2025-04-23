package com.book_engine.app;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;
import com.book_engine.app.Database;
import com.book_engine.app.Book;


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
    
    
    Database database;

    public GUI(Database database) {
        this.database = database;

        // Set up the window attributes
        setTitle("Book recommendation system");
        setSize(800, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        
        
        // Initialize main panel attributes and add to window
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(mainPanel);

        

        // Search: input field and search button
        JTextField searchInput = new JTextField(40);
        Action searchAction = getSearchAction();

        searchInput.registerKeyboardAction( searchAction, 
                            "", 
                            KeyStroke.getKeyStroke( "ENTER" ), 
                            JComponent.WHEN_FOCUSED );
                            
        JButton searchButton = new JButton("search");

        // Set up menu
        menuBar.add(searchMenu);
        menuBar.add(userMenu);
        menuBar.add(quitItem);
        setJMenuBar(menuBar);


        searchResultPanel.setLayout(new BoxLayout(searchResultPanel, BoxLayout.Y_AXIS));
        searchResultPane.setLayout(new ScrollPaneLayout());
        searchResultPanel.setBorder( new EmptyBorder(10,10,10,10) );
        searchResultPane.setPreferredSize( new Dimension(400, 450));
        bookContentPane.setPreferredSize( new Dimension(400, 200));

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
        
        // Logo
        JPanel p1 = new JPanel(new BorderLayout(120, 50)); // First JPanel 
        p1.add(welcomeLabel, BorderLayout.PAGE_START);
        p1.add(searchInput, BorderLayout.LINE_START);
        p1.add(searchButton, BorderLayout.CENTER);
        mainPanel.add(p1, BorderLayout.PAGE_START);

        ArrayList<Book> books = new ArrayList<Book>();
        // TODO: get books from database
        books.add(database.getBookByID(0));
        books.add(database.getBookByID(1));
        books.add(database.getBookByID(2));


        JScrollPane searchResultPane = displayBookResults(books); // get search result Pane
        searchResultPanel.add(searchResultPane);

        searchResultPanel.setOpaque(true);
        searchResultPanel.setBackground(Color.WHITE);
        searchResultPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        mainPanel.add(searchResultPane, BorderLayout.CENTER);
        mainPanel.add(bookContentPane, BorderLayout.PAGE_END);
       
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
            }
        };
    } 


    /**
     * Adds all books to a JScrollPane that contains a listener for any selected books
     */
    public JScrollPane displayBookResults(ArrayList<Book> books){
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (int i = 0; i < books.size(); i++) {
            listModel.addElement(i + " " + books.get(i).title + " - " + books.get(i).author);
        }

        JList<String> resultList = new JList<>(listModel);

        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setLayoutOrientation(JList.VERTICAL);
        resultList.setVisibleRowCount(-1); 
        resultList.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        resultList.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        resultList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = resultList.getSelectedValue();
                Integer booknr = Integer.parseInt(selected.split(" ")[0]);
                displayBookContent(books.get(booknr));
            }
        });

        JScrollPane newsearchResultPane = new JScrollPane(resultList);
        return newsearchResultPane;
    }

    public void displayBookContent(Book book){
        bookContentTextArea.setText(book.displayContents());
    }
}