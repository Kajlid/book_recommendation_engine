// package com.book_engine.app;
// import static org.junit.jupiter.api.Assertions.*;
// import org.junit.jupiter.api.*;
// import org.junit.jupiter.api.BeforeAll;

// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
// public class UserTest{
//     static final Database database = new Database();


//     @Test
//     public void testAddBookToUser(){
//         Float rating = 4.85f;
//         User testUser = new User("User");
//         String id = "Harry_Potter_and_the_Half-Blood_Prince_J.K._Rowling".toLowerCase();
//         testUser.addBook(id, database, rating);
//         System.out.println(testUser.books.get(0));
//         assertEquals(testUser.books.get(0), rating);
//     }

//     @Test
//     public void testAddMultipleBooks() {
//         User testUser = new User("User");
//         String id0 = "Harry_Potter_and_the_Half-Blood_Prince_J.K._Rowling".toLowerCase();
//         String id1 = "Harry_Potter_Collection_J.K._Rowling".toLowerCase();
//         String id2 = "The_Face_of_Another_Kōbō_Abe".toLowerCase();
        
//         String id4 = "A_Wild_Yearning_Penelope_Williamson".toLowerCase();
//         testUser.addBook(id0, database, 3.0f);
//         testUser.addBook(id1, database, 4.0f);
//         testUser.addBook(id2, database, 1.7f);
//         testUser.addBook(id4, database, 4.23f);
//         assertEquals(testUser.books.size(), 4);
//     }

    
// }