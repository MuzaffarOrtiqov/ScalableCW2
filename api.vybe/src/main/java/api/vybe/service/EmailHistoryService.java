package api.vybe.service;

import api.vybe.entity.EmailHistoryEntity;
import api.vybe.enums.AppLanguage;
import api.vybe.enums.EmailType;
import api.vybe.exps.AppBadException;
import api.vybe.repository.EmailHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class EmailHistoryService {
    @Autowired
    private EmailHistoryRepository emailHistoryRepository;
    @Autowired
    private ResourceBundleMessageService resourceBundleMessageService;

    public void createEmailHistory(String email, String subject, String body, String code, EmailType emailType) {
        EmailHistoryEntity emailHistoryEntity = new EmailHistoryEntity();
        emailHistoryEntity.setEmail(email);
        emailHistoryEntity.setSubject(subject);
        emailHistoryEntity.setBody(body);
        emailHistoryEntity.setCode(code);
        emailHistoryEntity.setAttemptCount(0l);
        emailHistoryEntity.setEmailType(emailType);
        emailHistoryRepository.save(emailHistoryEntity);

    }

    public Long getEmailCount(String email) {
        LocalDateTime now = LocalDateTime.now();
        Long count = emailHistoryRepository.countByEmailAndCreatedDateBetween(email, now.minusMinutes(1), now);
        return count;
    }

    public void check(String email, String code, AppLanguage lang) {
        Optional<EmailHistoryEntity> optional = emailHistoryRepository.findTop1ByEmailOrderByCreatedDateDesc(email);

        if (optional.isEmpty()) {
            log.info("Email history does not exist with email: {}", email);
            throw new AppBadException(resourceBundleMessageService.getMessage("email.not.found", lang));
        }
        EmailHistoryEntity entity = optional.get();
        if (entity.getAttemptCount() > 3) {
            log.warn("Attempt count exceeds 3: {}", entity.getAttemptCount());
            throw new AppBadException(resourceBundleMessageService.getMessage("too.many.attempts", lang));
        }
        //check code
        if (!entity.getCode().equals(code)) {
            emailHistoryRepository.updateAttemptCount(entity.getId());
            log.info("Wrong password : email {}, saved code {}, provided code {} ", email, entity.getCode(), code);
            throw new AppBadException(resourceBundleMessageService.getMessage("no.matching.password", lang));
        }
        //check time
        LocalDateTime expDate = entity.getCreatedDate().plusMinutes(10);
        if (LocalDateTime.now().isAfter(expDate)) {
            log.warn("Sent email confirmation time expired : email {}, sent time: {}", email, entity.getCreatedDate()) ;
            throw new AppBadException(resourceBundleMessageService.getMessage("verification.time.expired", lang));
        }

    }




}
