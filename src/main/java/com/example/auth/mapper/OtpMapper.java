package com.example.auth.mapper;

import com.example.auth.entity.PasswordResetOtp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface OtpMapper {

    @Mapping(target = "email", source = "email")
    @Mapping(target = "otpCode", source = "otpCode")
    @Mapping(target = "expiresAt", source = "expiresAt")
    @Mapping(target = "used", constant = "false")
    @Mapping(target = "id", ignore = true)
    PasswordResetOtp createPasswordResetOtp(String email, String otpCode, LocalDateTime expiresAt);
}
