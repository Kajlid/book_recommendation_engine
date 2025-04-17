package com.book_engine.app;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;


public class GUI extends JFrame {

    // Menus
    JMenuBar menuBar = new JMenuBar();
    JMenu searchMenu = new JMenu("Search");
    JMenu userMenu = new JMenu("User");
    JMenuItem quitItem = new JMenuItem("Quit");

    public JPanel resultWindow = new JPanel();
    public JTextArea docTextView = new JTextArea();
    private JScrollPane resultPane = new JScrollPane(resultWindow);
    private JScrollPane docViewPane = new JScrollPane(docTextView);

    public GUI() {
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

        // Action for quitting
        quitItem.addActionListener(e -> System.exit(0));

        JLabel welcomeLabel = new JLabel("Welcome to the book recommendation engine");
        
        // Logo
        JPanel p1 = new JPanel();
        p1.setPreferredSize(new Dimension(120, 50));
        //p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add(welcomeLabel, BorderLayout.PAGE_START);
        p1.add(searchInput, BorderLayout.LINE_START);
        p1.add(searchButton, BorderLayout.LINE_END);
        mainPanel.add(p1, BorderLayout.PAGE_START);

        // Results panel
        //JPanel p2 = new JPanel();
        //p2.add(resultPane, BorderLayout.CENTER);

        for (int i = 0; i < 50; i++) {
            JLabel label = new JLabel("Hello World!");
            resultWindow.add(label);
        }
        
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GUI();
        });
    }

    
}