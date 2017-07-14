package com.lightside.codeformatterproject;

import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {
        File readFile = new File("./src/main/resources/readfile");

        FileInputStream fis = new FileInputStream(readFile);
        char current;
        StringBuilder str = new StringBuilder();
        while (fis.available() > 0) {
            current = (char) fis.read();
            str.append(current);
            System.out.print(current);
        }
        System.out.println("");

        FileInputStream fileInpStr = new FileInputStream(readFile);
        boolean b = true;
        while (b) {
            if (fileInpStr.available() > 0) {
                char symb = (char) fileInpStr.read();
                System.out.print(symb);
            } else {
                b = false;
                fileInpStr.close();
            }
        }

    }
}
