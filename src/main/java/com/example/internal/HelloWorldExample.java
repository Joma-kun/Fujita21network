//package com.example.internal;
//import org.neo4j.driver.*;
//
//import static org.neo4j.driver.Values.parameters;
//
//public class HelloWorldExample implements AutoCloseable {
//    private final Driver driver;
//
//    public HelloWorldExample(String uri, String user, String password) {
//        // Neo4jデータベースへの接続
//        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
//    }
//
//    @Override
//    public void close() throws RuntimeException {
//        // データベース接続のクローズ
//        driver.close();
//    }
//
//    public void printGreeting(final String message) {
//        // データベースセッションを利用してCypherクエリを実行
//        try (Session session = driver.session()) {
//            String greeting = session.writeTransaction(tx -> {
//                String query = "CREATE (a:Greeting) SET a.message = $message RETURN a.message + ', from node ' + id(a)";
//                Result result = tx.run(query, parameters("message", message));
//                return result.single().get(0).asString();
//            });
//            System.out.println(greeting);
//        }
//    }
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
////import org.neo4j.driver.*;
////
////import static org.neo4j.driver.Values.parameters;
////public class HelloWorldExample implements AutoCloseable {
////    private final Driver driver;
////
////    public HelloWorldExample(String uri, String user, String password) {
////        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
////    }
////
////
////    public void printGreeting(final String message) {
////        try (Session session = driver.session()) {
////            String greeting = session.writeTransaction(new TransactionWork<String>() {
////                @Override
////                public String execute(Transaction tx) {
////                    Result result = tx.run(
////                            "CREATE (a:Greeting) SET a.message = $message RETURN a.message + ', from node ' + id(a)",
////                            parameters("message", message)
////                    );
////                    return result.single().get(0).asString();
////                }
////            });
////            System.out.println(greeting);
////        }
////
////
////    }
////    @Override
////    public void close() throws RuntimeException {
//////        driver.close();
////    }
////}