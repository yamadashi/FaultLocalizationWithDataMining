class ConceptWrapper extends Concept {
    private int[] ppcExtMask;

    public ConceptWrapper(int[] ex, int[] in, int[] ppcExtMask) {
        super(ex, in);
        this.ppcExtMask = ppcExtMask;
    }

    public int[] getPPCExtMask() {
        return ppcExtMask;
    }

    @Override
    public ConceptWrapper clone() {
        return new ConceptWrapper(extent.clone(), intent.clone(), ppcExtMask.clone());
    }

    @Override
    public boolean checkPPC(int current, Concept prev, int[] upto, int[] negObjs) {
        // 成功集合を保存
        int[] extDiff = SetOperation.xor(prev.getExtent(), extent);
        int[] extDiffofNegObjs = SetOperation.intersection(extDiff, negObjs);
        if (SetOperation.size(extDiffofNegObjs) == 0) { // ppcExtMaskの更新と探索の打ち切り
            ConceptWrapper prevCW = (ConceptWrapper) prev; // アップキャストしてごめんなさい
            prevCW.ppcExtMask[current / INTSIZE] |= (BIT << (INTSIZE - 1 - current % INTSIZE));
            ppcExtMask = prevCW.ppcExtMask.clone();
            return false;
        }

        for (int i = 0; i < current / INTSIZE; i++) {
            int diff = intent[i] ^ prev.getIntent()[i];
            diff &= ~ppcExtMask[i];
            if (diff != 0) {
                return false;
            }
        }
        int diff = intent[current / INTSIZE] ^ prev.getIntent()[current / INTSIZE];
        diff &= ~ppcExtMask[current / INTSIZE];
        if ((diff & upto[current % INTSIZE]) != 0) {
            return false;
        }
        return true;
    }
}