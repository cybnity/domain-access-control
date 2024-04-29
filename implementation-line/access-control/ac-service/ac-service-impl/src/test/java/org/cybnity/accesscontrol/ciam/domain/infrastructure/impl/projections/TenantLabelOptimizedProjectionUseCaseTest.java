package org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.projections;

import org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.mock.TenantTransactionsRepositoryMock;
import org.cybnity.accesscontrol.domain.service.api.model.TenantTransaction;
import org.cybnity.accesscontrol.domain.service.api.model.TenantTransactionsCollection;
import org.cybnity.framework.domain.IdentifierStringBased;
import org.cybnity.framework.domain.infrastructure.IDomainRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test behavior and capabilities of the projection.
 */
public class TenantLabelOptimizedProjectionUseCaseTest {

    private TenantLabelOptimizedProjectionImpl projection;
    private IDomainRepository<TenantTransactionsCollection> viewsRepository;

    @BeforeEach
    public void initResources() {
        viewsRepository = TenantTransactionsRepositoryMock.instance();
        // TODO Change for use of the standard repository based on adapter to ArangoDB
        //viewsRepository = TenantTransactionsRepository.instance();
        projection = new TenantLabelOptimizedProjectionImpl(viewsRepository, null);
    }

    @AfterEach
    public void cleanResources() {
        projection = null;
        viewsRepository = null;
    }

    /**
     * Verify that the latest version dated Tenant is find by label from original unordered tenants collection that have been re-ordered to provide the more young version (latest).
     */
    @Test
    public void givenSeveralUnOrderedTenantTransactions_whenFindByLabel_thenLatestVersion() throws Exception {
        // Prepare 5 collections of tenants
        String basicLabel = "CYBNITY_";
        Date moreYoungPreparedSample = null;
        String labelEligibleToCheck = basicLabel + 4;

        for (int i = 0; i < 5; i++) {
            TenantTransactionsCollection tenantCollection = prepareSample(basicLabel + i);
            // Save in repository
            viewsRepository.save(tenantCollection);

            // Prepare future evaluable label to search and verify
            if (labelEligibleToCheck.equals(basicLabel + i)) {
                // Find more young prepared sample of the tenant sample
                for (TenantTransaction tenantItem : tenantCollection.versions()) {
                    if (moreYoungPreparedSample == null) {
                        moreYoungPreparedSample = tenantItem.versionedAt;
                    } else {
                        if (tenantItem.versionedAt.compareTo(moreYoungPreparedSample) > 0) {
                            // Replace with more young date
                            moreYoungPreparedSample = tenantItem.versionedAt;
                        }
                    }
                }
            }
        }

        // Attempt to find one tenant latest version (more young prepared sample) from its label into the read-model projection
        TenantTransaction tenantView = projection.findByLabel(labelEligibleToCheck,null, null);
        // Check that is the latest version of the ordered collection
        Assertions.assertNotNull(tenantView);
        // Check that is more young sample version which was returned by the projection
        Assertions.assertEquals(moreYoungPreparedSample, tenantView.versionedAt, "It's not the latest version returned by projection!");
    }

    /**
     * Prepare a sample of tenant history collection
     *
     * @param label Tenant label to assign to sample version generated.
     * @return A collection.
     */
    private TenantTransactionsCollection prepareSample(String label) {
        // Create sample of tenant randomly identified version
        TenantTransactionsCollection sampleCollection = new TenantTransactionsCollection(IdentifierStringBased.generate(null).value().toString());
        // Prepare a collection a multiple tenant transactions with bad order (mixed dates)
        for (int t = 0; t < 15; t++) {
            sampleCollection.add(label,
                    /* Simulate versions generated when tenant activity status have been changed */ (t % 2 == 0) ? Boolean.TRUE : Boolean.FALSE,
                    Date.from(randomTimestamp()) /* unordered date of version*/);
        }
        return sampleCollection;
    }

    public static Instant randomTimestamp() {
        return Instant.ofEpochSecond(ThreadLocalRandom.current().nextInt());
    }
}
