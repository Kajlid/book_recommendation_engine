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

    public JPanel resultWindow = new JPanel();
    public JTextArea docTextView = new JTextArea(); //The content of a specified book
    private JScrollPane resultPane = new JScrollPane(resultWindow); // Contains the search results 
    private JScrollPane docViewPane = new JScrollPane(docTextView);
    Database database = null;

    public GUI(Database database) {
        this.database = database;
        setTitle("Book recommendation system");
        setSize(600, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(mainPanel);

        // Search: input field and search button
        JTextField searchInput = new JTextField(16);
        Action search = new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
                // Empty the results window
                resultWindow.removeAll();
                JLabel label = new JLabel( "info" );
                resultWindow.add( label );
                revalidate();
                repaint();
            }
        };
        searchInput.registerKeyboardAction( search,
                            "",
                            KeyStroke.getKeyStroke( "ENTER" ),
                            JComponent.WHEN_FOCUSED );

                            
        JButton searchButton = new JButton("search");
        mainPanel.add(searchInput, BorderLayout.CENTER);
        mainPanel.add(searchButton, BorderLayout.LINE_END);

        // Set up menu
        menuBar.add(searchMenu);
        menuBar.add(userMenu);
        menuBar.add(quitItem);
        setJMenuBar(menuBar);

        resultWindow.setLayout(new BoxLayout(resultWindow, BoxLayout.Y_AXIS));
        resultPane.setLayout(new ScrollPaneLayout());
        resultPane.setBorder( new EmptyBorder(10,10,10,0) );
        resultPane.setPreferredSize( new Dimension(400, 450));
        docViewPane.setPreferredSize( new Dimension(400, 100));


        docTextView.setText("\n  The contents of the document will appear here.");
        docTextView.setLineWrap(true);
        docTextView.setWrapStyleWord(true);
        docTextView.setEditable(false); // non-editable content of book 

        // Action for quitting
        quitItem.addActionListener(e -> System.exit(0));

        JLabel welcomeLabel = new JLabel("Welcome to the book recommendation engine");
        
        // Logo
        JPanel p1 = new JPanel(); // First JPanel 
        p1.setPreferredSize(new Dimension(120, 50));
        p1.add(welcomeLabel, BorderLayout.PAGE_START);
        p1.add(searchInput, BorderLayout.LINE_START);
        p1.add(searchButton, BorderLayout.LINE_END);
        mainPanel.add(p1, BorderLayout.PAGE_START);

        ArrayList<Book> books = new ArrayList<Book>();
        books.add(database.getBookByID(0));
        books.add(database.getBookByID(1));
        books.add(database.getBookByID(2));


        JScrollPane resultPane = displayBookResults(books); // get search result Pane
        resultWindow.add(resultPane);

        
        resultWindow.setOpaque(true);
        resultWindow.setBackground(Color.WHITE);
        resultPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


        //p2.add(docViewPane, BorderLayout.CENTER);
        mainPanel.add(resultPane, BorderLayout.CENTER);

        JPanel p3 = new JPanel();
        
        p3.add(docViewPane);
        mainPanel.add(p3, BorderLayout.PAGE_END);
       
        setVisible(true); // Make it visible
    }


    /**
     * Adds all books to a JScrollPane that contains a listener for any selected books
     */
    public JScrollPane displayBookResults(ArrayList<Book> books){
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (int i = 0; i < books.size(); i++) {
            listModel.addElement(i + " " + books.get(i).title);
        }

        JList<String> resultList = new JList<>(listModel);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setLayoutOrientation(JList.VERTICAL);
        resultList.setVisibleRowCount(-1); 
        resultList.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        resultList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        resultList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = resultList.getSelectedValue();
                Integer booknr = Integer.parseInt(selected.split(" ")[0]);
                displayBookContent(books.get(booknr));
                //System.out.println("User selected: " + selected);
            }
        });
        JScrollPane newResultPane = new JScrollPane(resultList);
        newResultPane.setPreferredSize(new Dimension(400, 450));
        return newResultPane;
    }

    public void displayBookContent(Book book){
        docTextView.setText(book.displayContents());
        repaint();
        revalidate();
    }

    
}