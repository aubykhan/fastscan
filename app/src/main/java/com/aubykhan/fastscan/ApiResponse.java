package com.aubykhan.fastscan;

/**
 * Created by aubykhan on 9/27/17.
 */

public class ApiResponse {
    private boolean isIdentical;
    private double confidence;
    private String cnic;
    private String name;

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public boolean getIsIdentical() {
        return isIdentical;
    }

    public void setIsIdentical(boolean identical) {
        isIdentical = identical;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
