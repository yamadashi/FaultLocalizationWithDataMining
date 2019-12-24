class ConceptWrapper {
    private Concept concept;
    private int[] ppcExtMask;

    public ConceptWrapper(Concept concept, int[] ppcExtMask) {
        this.concept = concept;
        this.ppcExtMask = ppcExtMask;
    }

    public Concept getConcept() {
        return concept;
    }

    public int[] getPPCExtMask() {
        return ppcExtMask;
    }
}