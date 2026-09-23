package com.ecommerce.user.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class GeographicAddress {

    @Column(name = "address_line", nullable = false, length = 255)
    private String addressLine;

    @Column(length = 100)
    private String ward;

    @Column(length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    protected GeographicAddress() {
    }

    public GeographicAddress(String addressLine, String ward, String district, String city, String postalCode) {
        this.addressLine = addressLine;
        this.ward = ward;
        this.district = district;
        this.city = city;
        this.postalCode = postalCode;
    }

    public String getAddressLine() { return addressLine; }
    public String getWard() { return ward; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public String getPostalCode() { return postalCode; }
}
