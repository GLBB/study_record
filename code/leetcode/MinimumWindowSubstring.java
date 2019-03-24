package cn.gl;

import java.util.HashMap;

public class MinimumWindowSubstring {

    public static void main(String[] args) {
        String S = "ADOBECODEBANC", T = "ABC";
        String S1 = "aa", T1 = "aa";
        String s = MinimumWindowSubstring.minWindow(S1, T1);
        System.out.println(s);
    }

    public static String minWindow(String s, String t) {
        HashMap<Character, Integer> map = prepare(t);
        int head = 0, tail = 0;
        int minIdx = -1;
        int minLength = Integer.MAX_VALUE;
        int total = t.length();
        for (tail = 0; tail < s.length(); tail++) {
            Integer integer = map.get(s.charAt(tail));
            if (integer != null) {
                map.put(s.charAt(tail), --integer);
                if (integer >= 0){
                    total--;
                }
                if (total==0) {
                    while (total==0) {
                        Integer headi = map.get(s.charAt(head));
                        if (headi!=null) {
                            if (tail - head + 1 < minLength) {
                                minIdx = head;
                                minLength = tail - head + 1;
                            }
                            map.put(s.charAt(head), ++headi);
                            if (headi<=0) {

                            }else {
                                total++;
                            }
                        }
                        head++;

                    }
                }
            }
        }
        if (minIdx == -1) return "";
        return s.substring(minIdx, minIdx+minLength);
    }

    private static HashMap<Character, Integer> prepare(String t){
        HashMap<Character, Integer> map = new HashMap<>();
        for (int i = 0; i < t.length(); i++) {
            Integer integer = map.get(t.charAt(i));
            if (integer == null) {
                map.put(t.charAt(i), 1);
            } else {
                map.put(t.charAt(i), ++integer);
            }
        }
        return map;
    }

}
