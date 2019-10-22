class Entry {
    private SSN ssn;
    private String name, email;

    public Entry(String ssn, String name, String email) {
        this.ssn = new SSN(ssn);
        this.name = name;
        this.email = email;
    }

    public Entry(SSN ssn, String name, String email) {
        this.ssn = ssn;
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public SSN getSSN() {
        return ssn;
    }
}
