package com.ProyectoAula.GymAssist.utils;

public class LevenshteinUtils {
    public static int calcularDistanciaLevenshtein(String palabra1, String palabra2){
        int[][] dp = new int[palabra1.length() + 1][palabra2.length() + 1];

        for(int i = 0; i <= palabra1.length(); i ++){
            for(int j = 0; j <= palabra2.length(); j ++){
                if (i == 0) {
                    dp[i][j] = j;
                }else if (j == 0) {
                    dp[i][j] = i;
                }else{
                    dp[i][j] = Math.min(dp[i - 1][j - 1] +
                    (palabra1.charAt(i - 1) == palabra2.charAt(j - 1) ? 0:1),
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }
        return dp[palabra1.length()][palabra2.length()];
    }
}
