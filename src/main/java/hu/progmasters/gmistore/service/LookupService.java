package hu.progmasters.gmistore.service;

import hu.progmasters.gmistore.enums.DomainType;
import hu.progmasters.gmistore.model.LookupEntity;
import hu.progmasters.gmistore.repository.LookupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class LookupService {

    private final LookupRepository lookupRepository;

    @Autowired
    public LookupService(LookupRepository lookupRepository) {
        this.lookupRepository = lookupRepository;
    }

    public LookupEntity getPaymentMethodByKey(String key) {
        return lookupRepository.findByDomainTypeAndLookupKey(DomainType.PAYMENT_METHOD, key);
    }
}
