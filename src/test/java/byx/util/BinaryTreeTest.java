package byx.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static byx.util.StackGuard.guard;
import static byx.util.StackGuard.run;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BinaryTreeTest {
    @Test
    public void testBinaryTree() {
        TreeNode node = buildTree();
        List<Integer> ans = new ArrayList<>();
        traverse1(node, ans);

        List<Integer> result = new ArrayList<>();
        traverse2(node, result, () -> {});
        assertEquals(ans, result);
    }

    @Test
    public void testHugeTree() {
        TreeNode node = buildHugeTree();
        assertThrows(StackOverflowError.class, () -> traverse1(node, new ArrayList<>()));
        assertThrows(StackOverflowError.class, () -> traverse2(node, new ArrayList<>(), () -> {}));

        List<Integer> result = new ArrayList<>();
        run(() -> traverse3(node, result, r -> {}), 1000);
        for (int i : result) {
            assertEquals(1, i);
        }
    }

    private void traverse1(TreeNode n, List<Integer> result) {
        if (n == null) {
            return;
        }

        result.add(n.val());
        traverse1(n.left(), result);
        traverse1(n.right(), result);
    }

    private void traverse2(TreeNode n, List<Integer> result, Runnable cont) {
        if (n == null) {
            cont.run();
            return;
        }

        result.add(n.val());
        traverse2(n.left(), result, () ->
            traverse2(n.right(), result, cont));
    }

    private void traverse3(TreeNode n, List<Integer> result, Cont<Void> cont) {
        guard(() -> traverse3(n, result, cont));
        if (n == null) {
            cont.accept(null);
            return;
        }

        result.add(n.val());
        traverse3(n.left(), result, r ->
            traverse3(n.right(), result, cont));
    }

    /**
     *      1
     *     / \
     *    2  3
     *   / \  \
     *  4  5  6
     */
    private TreeNode buildTree() {
        return new TreeNode(1, new TreeNode(2, new TreeNode(4), new TreeNode(5)), new TreeNode(3, null, new TreeNode(6)));
    }

    /**
     *         1
     *        /
     *       2
     *      /
     *     3
     *    /
     *  ...
     */
    private TreeNode buildHugeTree() {
        TreeNode n = new TreeNode(1);
        for (int i = 2; i <= 100000; i++) {
            n = new TreeNode(1, n, null);
        }
        return n;
    }
}

record TreeNode(int val, TreeNode left, TreeNode right) {
    public TreeNode(int val) {
        this(val, null, null);
    }
}
