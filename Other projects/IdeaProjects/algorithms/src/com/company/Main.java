package com.company;

public class Main {

    public static void main(String[] args) {

        // Max element in array.
        int[] tmp = {3,4,15, 6,2,0, 10, 22, 3, 5, 4,2,1,8,17};
	    int maxVal = findMaxEl(tmp);
        System.out.println("max value = " + maxVal);

        // Find arithmetical mean in array.
        double average = findAverageValue(tmp);
        System.out.println("average value = " + average);

        // Invert array.
        char[] charArr = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        charArr = invertArray(charArr);
        printArr("inverted: ", charArr);
    }

    public static int findMaxEl(int[] arr) {
        int max = arr[0];
        for (int i = 0; i <= arr.length-1; i++) {
            if (max < arr[i]) {
                max = arr[i];
            }
        }

        return max;
    }

    public static double findAverageValue(int[] arr) {
        double average = 0;
        int length = arr.length;
        double sum = 0;

        for (int i = 0; i <= length-1; i++) {
            sum += arr[i];
        }
        average = sum / length;

        return average;
    }

    public static char[] invertArray(char[] arr) {
        int n = arr.length;
        for (int i = 0; i < n/2; i++) {
            char temp = arr[i];
            arr[i] = arr[n-1-i];
            arr[n-1-i] = temp;
        }

        return arr;
    }

    public static void printArr(String way,char[] arr) {
        System.out.println(way);
        for (int i = 0; i < arr.length; i++) {
            System.out.println(arr[i]);
        }
    }

}
