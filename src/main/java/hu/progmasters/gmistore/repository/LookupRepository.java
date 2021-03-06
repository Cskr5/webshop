package hu.progmasters.gmistore.repository;

import hu.progmasters.gmistore.enums.DomainType;
import hu.progmasters.gmistore.model.LookupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LookupRepository extends JpaRepository<LookupEntity, Long> {
    @Query("SELECT l FROM LookupEntity l WHERE l.domainType = :domainType AND l.displayFlag = true " +
            "ORDER BY l.orderSequence, l.displayName")
    List<LookupEntity> findByDomainType(DomainType domainType);

    LookupEntity findLookupEntityByDisplayName(String name);

    @Query("SELECT l FROM LookupEntity l WHERE l.domainType = :domainType AND l.lookupKey = :lookupKey")
    Optional<LookupEntity> findByDomainTypeAndLookupKey(DomainType domainType, String lookupKey);

    @Query("SELECT l FROM LookupEntity l WHERE l.domainType = :domainType AND l.parent is null")
    List<LookupEntity> findByDomainTypeAndParentIsNull(DomainType domainType);

    @Query("SELECT l FROM LookupEntity l WHERE l.domainType = :domainType AND l.parent = :parent")
    List<LookupEntity> findByDomainTypeAndParent(DomainType domainType, LookupEntity parent);
}
