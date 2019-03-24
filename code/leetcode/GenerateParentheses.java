package cn.gl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GenerateParentheses {

    public static void main(String[] args) {
        GenerateParentheses solution = new GenerateParentheses();
        List<String> list = solution.generateParenthesis(3);
        System.out.println(list);
    }

    public List<String> generateParenthesis(int n) {
        List<String> result = new ArrayList<>();
        if (n <= 0) return Collections.emptyList();
        generateParenthesis(n, n, result, "");
        return result;
    }

    /**
     * 相当于生成全部组合
     * @param left 剩余可以添加的'('
     * @param right 剩余可以添加的')'
     * @param list
     * @param str
     */
    private void generateParenthesis(int left, int right, List<String> list, String str) {
        if (left < 0 || right < 0 || left > right) return;
        if (left == 0 && right == 0) list.add(str);

        generateParenthesis(left-1, right, list, str+"(");
        generateParenthesis(left, right-1, list, str+ ")");
    }
}
