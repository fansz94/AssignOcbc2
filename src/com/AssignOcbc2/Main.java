package com.AssignOcbc2;

import java.sql.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        System.out.println("=====WELCOME=====");

        Scanner scanner = new Scanner(System.in);

        try
        {

            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb?useSSL=false","root","root");
            Statement stmt = con.createStatement();

            System.out.print("Please Enter Username: ");
            String username = scanner.nextLine();

            System.out.print("Please Enter Password: ");
            String password = scanner.nextLine();

            String checkUsername = "INSERT INTO userlist (username, password, balance) SELECT * FROM (SELECT '"+ username + "'as username, '"+ password+ "'as password, '0'as balance) AS tmp WHERE NOT EXISTS ( SELECT username FROM userlist WHERE username = '"+ username + "') LIMIT 1";
            int data = stmt.executeUpdate(checkUsername);


            String getUserData = "SELECT * FROM userlist WHERE username='"+ username + "' and password='"+ password+ "'";
            ResultSet rs = stmt.executeQuery(getUserData);

            if(rs.next() && data > 0)
            {
                System.out.println("\nNew Account Created!");

                int id = rs.getInt("id");
                int balance = rs.getInt("balance");
                BankAccount action = new BankAccount(id,username, balance);
                action.showMenu();


            }else
            {
                int id = rs.getInt("id");
                int balance = rs.getInt("balance");
                BankAccount action = new BankAccount(id,username, balance);
                action.showMenu();

            }


        }catch (Exception e)
        {
            System.out.println("error message"+ e);
        }
    }
}


class BankAccount {

    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb?useSSL=false","root","root");
    Statement stmt = con.createStatement();



    int customerId;
    String customerName;
    int customerBal;

    BankAccount(Integer cid,String cname, Integer cbal) throws SQLException {
        customerId = cid;
        customerName = cname;
        customerBal = cbal;
    }

    void topup(int amount) {

        if (amount != 0) {
            String clearCompleteOwnTable = "DELETE FROM ownTo WHERE amount <= 0";

            try
            {
                stmt.executeUpdate(clearCompleteOwnTable);

                String query = "SELECT * FROM ownTo WHERE username='" + customerName + "'";
                ResultSet rs = stmt.executeQuery(query);

                if (rs.next())
                {
                    String ownTo = rs.getString("ownTo");
                    int amountOwn = rs.getInt("amount");

                    if (amountOwn >= amount && amountOwn != 0)
                    {
                        amountOwn = amountOwn - amount;

                        String updateBalance = "UPDATE ownto " + "SET amount = " + amountOwn + " WHERE id =" + customerId + " ";
                        String updateBalance2 = "UPDATE userlist " + "SET balance = balance +" + amount + " WHERE username ='"+ownTo+"'";

                        try {
                            stmt.executeUpdate(updateBalance);
                            stmt.executeUpdate(updateBalance2);

                            System.out.println("Transferred "+amount+" to "+ownTo );
                            System.out.println("Your balance is "+customerBal);
                            System.out.println("Owing "+(amountOwn)+" to "+ownTo);

                        } catch (SQLException throwables)
                        {
                            throwables.printStackTrace();
                        }
                    }else
                    {
                        customerBal = amount - amountOwn;
                        String updateBalance = "UPDATE ownto " + "SET amount = amount -" + amount + " WHERE id =" + customerId + " ";
                        String updateBalance2 = "UPDATE userlist " + "SET balance = balance +" + amountOwn + " WHERE username ='"+ownTo+"'";
                        String updateBalance3 = "UPDATE userlist " + "SET balance = "+customerBal+" WHERE id ="+customerId+"";

                        try {
                            stmt.executeUpdate(updateBalance);
                            stmt.executeUpdate(updateBalance2);
                            stmt.executeUpdate(updateBalance3);
                            System.out.println("Transferred "+amountOwn+" to "+ownTo );
                            System.out.println("Your balance is "+customerBal);
                            stmt.executeUpdate(clearCompleteOwnTable);

                        } catch (SQLException throwables)
                        {
                            throwables.printStackTrace();
                        }

                    }

                } else
                {
                    customerBal = customerBal + amount;
                    String updateBalance = "UPDATE userlist " + "SET balance = " + customerBal + " WHERE id =" + customerId + " ";
                    try {
                        stmt.executeUpdate(updateBalance);
                    } catch (SQLException throwables)
                    {
                        throwables.printStackTrace();
                    }
                    System.out.println("Your balance is RM " + customerBal);
                }
            }catch(Exception e)
            {
            System.out.println("error message" + e);
            }
        }

    }

    void transfer(int amount)
    {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Transfer to? (Insert Username): ");
        String transferedToUser = scanner.nextLine();



        if(customerBal < amount){
            int successTransfered = customerBal;
            int amountOwn = customerBal - amount;
            customerBal = successTransfered - customerBal;

            System.out.println("Transferred "+successTransfered+" to "+transferedToUser );
            System.out.println("Your balance is "+customerBal);
            System.out.println("Owing "+(-amountOwn)+" to "+transferedToUser);
            String updateBalance = "UPDATE userlist " + "SET balance = 0 WHERE id ="+customerId+"";
            String updateBalance2 = "UPDATE userlist " + "SET balance = balance +"+successTransfered+" WHERE username ='"+transferedToUser+"'";


            String ownTo = "INSERT INTO ownto (id, username, ownTo, amount) SELECT * FROM (SELECT '"+customerId+"' as id, '"+customerName+"' as username, '"+transferedToUser+"' as ownTo, '"+(-amountOwn)+"' as amount) AS tmp WHERE NOT EXISTS ( SELECT ownTo FROM ownto WHERE ownTo = '"+transferedToUser+"') LIMIT 1";

            try
            {
                stmt.executeUpdate(updateBalance);
                stmt.executeUpdate(updateBalance2);
                stmt.executeUpdate(ownTo);

            } catch (SQLException throwables)
            {
                throwables.printStackTrace();
            }
        }else
        {

            try
            {
                String clearCompleteOwnTable = "DELETE FROM ownTo WHERE amount <= 0";
                String updateOwnTable = "UPDATE ownto " + "SET amount = amount -" + amount + " WHERE username='"+ transferedToUser + "' and ownTo='" + customerName + "'";
                stmt.executeUpdate(updateOwnTable);

                String query = "SELECT * FROM ownTo WHERE username='"+ transferedToUser + "' and ownTo='" + customerName + "'";
                ResultSet rs = stmt.executeQuery(query);

                if (rs.next())
                {
                    int amountOwingLeft = rs.getInt("amount");
                    String ownFrom = rs.getString("username");

                    if(amountOwingLeft > 0)
                    {
                        System.out.println("Owing "+ amountOwingLeft + " from "+ ownFrom);
                        System.out.println("Your balance is RM " + customerBal );
                    }else
                    {
                        amountOwingLeft = -(amountOwingLeft);
                        String updateBalance2 = "UPDATE userlist " + "SET balance = balance +" + amountOwingLeft + " WHERE username='"+customerName+"'";
                        stmt.executeUpdate(updateBalance2);
                        stmt.executeUpdate(clearCompleteOwnTable);

                        System.out.println("plus "+ (amountOwingLeft));

                    }

                }else
                {
                    customerBal = customerBal - amount;
                    String updateBalance = "UPDATE userlist " + "SET balance = "+customerBal+" WHERE id ="+customerId+" ";
                    String updateBalance2 = "UPDATE userlist " + "SET balance = balance +"+amount+" WHERE username ='"+transferedToUser+"'";

                    try {
                        stmt.executeUpdate(updateBalance);
                        stmt.executeUpdate(updateBalance2);

                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    System.out.println("Transferred " + amount +" to "+ transferedToUser );
                    System.out.println("Your balance is RM " + customerBal );
                }

            }catch(Exception e)
            {
                System.out.println("error message" + e);
            }

        }
    }


    void showMenu()
    {
        char option = '\0';
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nSuccessfully Logged In!\n"+ "\nHello, " + customerName + "!");

        try
        {

            String query = "SELECT * FROM ownTo WHERE ownTo='" + customerName + "'";
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next())
            {
              int amountOtherUserOwn = rs.getInt("amount");
                String ownFrom = rs.getString("username");
                System.out.println("Owing "+amountOtherUserOwn+" from " + ownFrom);

            }

        }catch(Exception e)
        {
            System.out.println("error message" + e);
        }

        System.out.println("Your balance is RM " + customerBal );

        try
        {

            String query2 = "SELECT * FROM ownTo WHERE username='" + customerName + "'";
            ResultSet rs2 = stmt.executeQuery(query2);
            if (rs2.next())
            {
                int amountOtherUserOwn = rs2.getInt("amount");
                String ownTo = rs2.getString("ownTo");
                System.out.println("Owing "+amountOtherUserOwn+" To " + ownTo);

            }
        }catch(Exception e)
        {
            System.out.println("error message" + e);
        }

        System.out.println("\n");
        System.out.println("A, Topup");
        System.out.println("B, Transfer");
        System.out.println("C, Exit");
        do
        {
            System.out.println("==============================================");
            System.out.print("Enter an option: ");
            option = scanner.next().charAt(0);
            System.out.println("\n");

            switch(option) {
                case 'A':
                    System.out.print("Enter an amount to topup: ");
                    int amount = scanner.nextInt();
                    topup(amount);
                    System.out.println("\n");
                    break;

                case 'B':

                    System.out.print("Enter an amount to transfer: ");
                    int amount2 = scanner.nextInt();



                    transfer(amount2);
                    System.out.println("\n");
                    break;

                case 'C':
                    System.out.println("============================");
                    break;

                default:
                    System.out.println("Invalid Option!! Please enter again");
                    break;
            }
        }while (option != 'C');
        System.out.println("thank you for using our services");
    }
}




//package com.AssignOcbc2;
//
//import java.sql.*;
//import java.util.Scanner;
//
//public class Main {
//
//    public static void main(String[] args) {
//
//        System.out.println("=====WELCOME=====");
//
//        Scanner scanner = new Scanner(System.in);
//
//        try
//        {
//
//            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb?useSSL=false","root","root");
//            Statement stmt = con.createStatement();
//
//            System.out.print("Please Enter Username: ");
//            String username = scanner.nextLine();
//
//            System.out.print("Please Enter Password: ");
//            String password = scanner.nextLine();
//
//            String checkUsername = "INSERT INTO userlist (username, password, balance) SELECT * FROM (SELECT '"+ username + "'as username, '"+ password+ "'as password, '0'as balance) AS tmp WHERE NOT EXISTS ( SELECT username FROM userlist WHERE username = '"+ username + "') LIMIT 1";
//            int data = stmt.executeUpdate(checkUsername);
//
//
//            String getUserData = "SELECT * FROM userlist WHERE username='"+ username + "' and password='"+ password+ "'";
//            ResultSet rs = stmt.executeQuery(getUserData);
//
//            if(rs.next() && data > 0)
//            {
//                System.out.println("New Account Created!");
//
//                int id = rs.getInt("id");
//                int balance = rs.getInt("balance");
//                BankAccount action = new BankAccount(id,username, balance);
//                action.showMenu();
//
//
//            }else
//            {
//                System.out.println("success logged in!");
//                int id = rs.getInt("id");
//                int balance = rs.getInt("balance");
//                BankAccount action = new BankAccount(id,username, balance);
//                action.showMenu();
//
//            }
//
//
//        }catch (Exception e)
//        {
//            System.out.println("error message"+ e);
//        }
//    }
//}
//
//
//class BankAccount {
//
//    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/mydb?useSSL=false","root","root");
//    Statement stmt = con.createStatement();
//
//    int customerId;
//    String customerName;
//    int customerBal;
//
//    BankAccount(Integer cid,String cname, Integer cbal) throws SQLException {
//        customerId = cid;
//        customerName = cname;
//        customerBal = cbal;
//    }
//
//    void deposit(int amount) {
//        if(amount != 0)
//        {
//            customerBal = customerBal + amount;
//            String updateBalance = "UPDATE userlist " + "SET balance = "+customerBal+" WHERE id ="+customerId+" ";
//            int data = 0;
//            try {
//                data = stmt.executeUpdate(updateBalance);
//            } catch (SQLException throwables) {
//                throwables.printStackTrace();
//            }
//            System.out.println("Your balance is RM " + customerBal );
//
//        }
//    }
//
//
//    void showMenu()
//    {
//        char option = '\0';
//        Scanner scanner = new Scanner(System.in);
//
//        System.out.println("\nSuccessfully Logged In!\n"+ "\nHello, " + customerName + "!");
//        System.out.println("Your balance is RM " + customerBal );
//        System.out.println("\n");
//        System.out.println("A, Deposit");
//        System.out.println("B, Transfer");
//        System.out.println("C, Exit");
//        do
//        {
//            System.out.println("==============================================");
//            System.out.println("Enter an option");
//            option = scanner.next().charAt(0);
//            System.out.println("\n");
//
//            switch(option) {
//                case 'A':
//                    System.out.print("Enter an amount to deposit: ");
//                    int amount = scanner.nextInt();
//                    deposit(amount);
//                    System.out.println("\n");
//                    break;
//
//
//
//                case 'C':
//                    System.out.println("============================");
//                    break;
//
//                default:
//                    System.out.println("Invalid Option!! Please enter again");
//                    break;
//            }
//        }while (option != 'C');
//        System.out.println("thank you for using our services");
//    }
//}
