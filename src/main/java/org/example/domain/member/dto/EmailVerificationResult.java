package org.example.domain.member.dto;

public class EmailVerificationResult {
    private final boolean success;

    private EmailVerificationResult(boolean success) {
        this.success = success;
    }

    public static EmailVerificationResult of(boolean success) {
        return new EmailVerificationResult(success);
    }

    public boolean isSuccess() {
        return success;
    }
}

