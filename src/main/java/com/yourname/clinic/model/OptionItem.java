package com.yourname.clinic.model;

public record OptionItem(Long id, String label) {
    @Override
    public String toString() {
        return label;
    }
}
