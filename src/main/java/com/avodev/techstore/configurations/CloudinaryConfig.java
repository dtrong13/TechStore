package com.avodev.techstore.configurations;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "deqr6xuvx",    // đổi theo Cloudinary của bạn
                "api_key", "285541717595153",       // đổi API KEY
                "api_secret", "0AOoDLNi53Pg1jIJFfiF9rE4boQ", // đổi API SECRET
                "secure", true
        ));
    }
}
