package com.clinica.api.personal_service.service;

public interface AdminPasswordService {

    void changePassword(Long adminId, String currentPassword, String newPassword);
}
