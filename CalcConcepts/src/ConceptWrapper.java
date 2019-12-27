class ConceptWrapper extends Concept {

    public ConceptWrapper(int[] ex, int[] in, int[] ppcExtMask) {
        super(ex, in, ppcExtMask);
    }

    @Override
    public ConceptWrapper clone() {
        return new ConceptWrapper(extent.clone(), intent.clone(), ppcExtMask.clone());
    }

    @Override
    public boolean checkPPC(int current, Concept prev, int[] upto, int[] negObjs) {
        // 成功集合を保存する場合はfalseを返す
        int[] diff = SetOperation.xor(prev.getExtent(), extent);
        int[] diffofNegObjs = SetOperation.intersection(diff, negObjs);
        if (SetOperation.size(diffofNegObjs) == 0) {
            // ppcExtMaskの更新と探索の打ち切り
            prev.ppcExtMask[current / INTSIZE] |= (BIT << (INTSIZE - 1 - current % INTSIZE));
            ppcExtMask = prev.ppcExtMask.clone();
            return false;
        }

        return super.checkPPC(current, prev, upto, null);
    }
}