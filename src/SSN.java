class SSN {
    private String ssn;

    public SSN(String ssn) {
        this.ssn = ssn;
    }

    public String getSSN() {
        return ssn;
    }

    @Override
    public int hashCode() {
        return SHA1Hasher.computeDigest(ssn);
    }
}
