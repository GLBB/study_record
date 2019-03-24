package cn.gl;

import java.util.Arrays;
import java.util.Collections;

public class NextPermutation {

    public static void nextPermutation(int[] nums) {
        if (nums.length==1) return;

        for (int i = nums.length-2; i >=0 ; i--) {
            if (nums[i] < nums[i+1]){
                int idx = nums.length -1;
                for (int j = nums.length-1; j >= i+1 ; j--) {
                    if (nums[j] > nums[i]) {
                        idx = j;
                        break;
                    }
                }
                // swap
                int temp = nums[i];
                nums[i] = nums[idx];
                nums[idx] = temp;
                // sort  i+1 ~ length-1
                sort(nums, i+1, nums.length-1);
                break;
            }
            if (i==0) Arrays.sort(nums);
        }
    }

    private static void sort(int[] nums, int startIdx, int endIdx){
        if (startIdx < endIdx) {
            int pivot = pivot(nums, startIdx, endIdx);
            sort(nums, startIdx, pivot-1);
            sort(nums, pivot+1, endIdx);
        }
    }


    public static int pivot(int[] nums, int start, int end){
        int pivot = nums[end];
        int i = start -1;
        for (int j = start; j <= end-1; j++) {
            if (nums[j] <= pivot) {
                i++;
                exchange(nums, i, j);
            }
        }
        exchange(nums, i+1, end);
        return i+1;
    }

    private static void exchange(int[] nums, int i, int j){
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    public static void main(String[] args) {
        int[] a1 = {1,2,3};
        int[] a2 = {1, 3, 2};
        int[] a3 = {3, 2, 1};
        int[] a4 = {1, 5, 1};
        int[] a5 = {5,4,7,5,3,2};
        int[] a6 = {2,2,0,4,3,1};
        nextPermutation(a6);
        System.out.println();
    }


}
