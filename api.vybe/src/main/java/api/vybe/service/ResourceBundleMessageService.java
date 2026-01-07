package api.vybe.service;

import api.vybe.enums.AppLanguage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;


@Service
public class ResourceBundleMessageService {
    @Autowired
    private ResourceBundleMessageSource messageSource;

    public String getMessage(String code, AppLanguage lang) {
        return messageSource.getMessage(code, null, new Locale(lang.name()));
    }
    public String getMessage(String code, AppLanguage lang,Object... args) {
        return messageSource.getMessage(code, args, new Locale(lang.name()));
    }
}
