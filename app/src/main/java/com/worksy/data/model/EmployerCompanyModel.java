package com.worksy.data.model;

public class EmployerCompanyModel {
    private String companyName;
    private String companyAddress;
    private String industrySpecialization;
    private String foundedYear;
    private String companyBenefits;
    private String companyCulture;
    private String logoUrl;

    public EmployerCompanyModel() {
        // Default constructor required for Firestore
    }

    public EmployerCompanyModel(String companyName, String companyAddress, String industrySpecialization, String foundedYear, String companyBenefits, String companyCulture, String logoUrl) {
        this.companyName = companyName;
        this.companyAddress = companyAddress;
        this.industrySpecialization = industrySpecialization;
        this.foundedYear = foundedYear;
        this.companyBenefits = companyBenefits;
        this.companyCulture = companyCulture;
        this.logoUrl = logoUrl;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getIndustrySpecialization() {
        return industrySpecialization;
    }

    public void setIndustrySpecialization(String industrySpecialization) {
        this.industrySpecialization = industrySpecialization;
    }

    public String getFoundedYear() {
        return foundedYear;
    }

    public void setFoundedYear(String foundedYear) {
        this.foundedYear = foundedYear;
    }

    public String getCompanyBenefits() {
        return companyBenefits;
    }

    public void setCompanyBenefits(String companyBenefits) {
        this.companyBenefits = companyBenefits;
    }

    public String getCompanyCulture() {
        return companyCulture;
    }

    public void setCompanyCulture(String companyCulture) {
        this.companyCulture = companyCulture;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}

