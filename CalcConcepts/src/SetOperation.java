class SetOperation {
    // 和集合
    public static int[] union(int[] s0, int[] s1) {
        int[] rtn = s0.clone();
        for (int i = 0; i < rtn.length; i++) {
            rtn[i] |= s1[i];
        }
        return rtn;
    }

    // 差集合(bit差ではなく集合の引き算)
    public static int[] difference(int[] s0, int[] s1) {
        int[] rtn = s0.clone();
        for (int i = 0; i < rtn.length; i++) {
            rtn[i] &= ~s1[i];
        }
        return rtn;
    }

    // 積集合
    public static int[] intersection(int[] s0, int[] s1) {
        int[] rtn = s0.clone();
        for (int i = 0; i < rtn.length; i++) {
            rtn[i] &= s1[i];
        }
        return rtn;
    }

    public static int[] xor(int[] s0, int[] s1) {
        int[] rtn = s0.clone();
        for (int i = 0; i < rtn.length; i++) {
            rtn[i] ^= s1[i];
        }
        return rtn;
    }

    // 立っているbitの数
    public static int size(int[] arr) {
        int rtn = 0;
        for (int e : arr) {
            rtn += Integer.bitCount(e);
        }
        return rtn;
    }

    // 第二引数以降で指定したindexのbitがたった集合を返す
    public static int[] makeSet(int intLen, int... indices) {
        int[] rtn = new int[intLen];
        final int INTSIZE = Integer.SIZE;
        for (int i : indices) {
            rtn[i / INTSIZE] |= 1 << (INTSIZE - 1 - (i % INTSIZE));
        }
        return rtn;
    }

    public static String toString(int[] arr) {
        final int INTSIZE = Integer.SIZE;
        String str = "{ ";
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < INTSIZE; j++) {
                if ((arr[i] & 1 << (INTSIZE - 1 - j)) != 0) {
                    str += ((i * INTSIZE + j) + " ");
                }
            }
        }
        return str + "}";
    }
}